package net.micmu.mcmods.micsiege.core;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.village.VillageCollection;
import net.minecraft.village.VillageDoorInfo;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.biome.Biome;

import net.micmu.mcmods.micsiege.Config;

/**
 *
 * @author Micmu
 */
public class SiegeZombies extends SiegeAIBase {
    private BlockPos siegePos;
    private Boolean huskCache;
    private float timeAngle = -1.0F;

    /**
     *
     */
    SiegeZombies() {
    }

    /**
     *
     */
    @Override
    protected boolean onInitializeWorld() {
        int d = getWorld().provider.getDimension();
        return (d != -1) && (d != 1); // No Zombie Siege in Nether and End
    }

    /**
     *
     */
    @Override
    protected void onStop() {
        siegePos = null;
        huskCache = null;
        timeAngle = -1.0F;
    }

    /**
     *
     */
    @Override
    protected boolean checkDaytime(boolean precise) {
        if (precise) {
            if (timeAngle < 0.0F) {
                timeAngle = 0.5F;
                if (Config.randomizeTime)
                    timeAngle += getRNG().nextFloat() * 0.2F;
            }
            return isCelestialAngle(timeAngle);
        } else {
            return !getWorld().isDaytime();
        }
    }

    /**
     *
     */
    @Override
    protected boolean checkChance() {
        int c = Config.chancesPerNight;
        return (c > 0) && ((c >= 100) || (c > getRNG().nextInt(100)));
    }

    /**
     *
     */
    @Override
    protected boolean tryStart() {
        final BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        final BlockPos.MutableBlockPos spawn = new BlockPos.MutableBlockPos();
        final VillageCollection villageCol = getWorld().getVillageCollection();
        final Random rnd = getRNG();
        final SiegeCore core = SiegeCore.getInstance();
        Village village = null;
        BlockPos center;
        float f;
        float q;
        boolean b;
        int i;
        int r;
        double radius = Config.villageDistanceMultiplier + (0.5D - (0.1D * rnd.nextDouble()));
        if (radius < 0.1D)
            radius = 0.1D;
        else if (radius > 1.9D)
            radius = 1.9D;
        for (EntityPlayer player : getRandomizedPlayers()) {
            m.setPos(MathHelper.floor(player.posX), MathHelper.floor(player.posY), MathHelper.floor(player.posZ));
            village = villageCol.getNearestVillage(m, 1);
            if ((village != null) && isValidVillage(village)) {
                center = village.getCenter();
                f = (float)village.getVillageRadius();
                b = false;
                for (i = 0; i < 10; i++) {
                    q = rnd.nextFloat() * ((float)Math.PI * 2.0F);
                    spawn.setPos(center.getX() + (int)((double)(MathHelper.cos(q) * f) * radius), center.getY(), center.getZ() + (int)((double)(MathHelper.sin(q) * f) * radius));
                    b = false;
                    for (Village vx : villageCol.getVillageList()) {
                        if ((vx != village) && !vx.isAnnihilated()) {
                            r = vx.getVillageRadius();
                            if (spawn.distanceSq(vx.getCenter()) < (double)(4 * r * r)) {
                                b = true;
                                break;
                            }
                        }
                    }
                    if (!b)
                        break;
                }
                if (b)
                    return false;
                if (locateSpawnPos(m, village, spawn) != null) {
                    setVillage(village);
                    siegePos = spawn.toImmutable();
                    huskCache = null;
                    setTickCount(determineSpawnCount(village));
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     */
    @Override
    protected void tick() {
        EntityZombie z;
        int c = Config.zombieVillagerChance;
        if ((c > 0) && ((c >= 100) || (c > getRNG().nextInt(100)))) {
            z = new EntityZombieVillager(getWorld());
        } else if (isHuskBiome() && getRNG().nextBoolean()) {
            z = new EntityHusk(getWorld());
        } else {
            z = new EntityZombie(getWorld());
        }
        spawnAttacker(z, locateSpawnPos(null, getVillage(), siegePos));
    }

    /**
     *
     * @return
     */
    private boolean isHuskBiome() {
        if (huskCache == null) {
            boolean f = false;
            List<Biome.SpawnListEntry> spawns = getWorld().getBiome(siegePos).getSpawnableList(EnumCreatureType.MONSTER);
            if ((spawns != null) && !spawns.isEmpty()) {
                for (Biome.SpawnListEntry e : spawns) {
                    if (EntityHusk.class.isAssignableFrom(e.entityClass)) {
                        f = true;
                        break;
                    }
                }
            }
            huskCache = Boolean.valueOf(f);
        }
        return huskCache.booleanValue();
    }

    /**
     *
     * @param village
     * @return
     */
    private boolean isValidVillage(Village village) {
        return (village.getNumVillageDoors() >= Config.villageMinDoors) && (village.getTicksSinceLastDoorAdding() >= 20) && (village.getNumVillagers() >= Config.villageMinVillagers);
    }

    /**
     *
     * @param village
     * @return
     */
    private int determineSpawnCount(Village village) {
        int i = Config.zombieSpawnCount;
        if ((i < 1) || (i > 1000))
            return 0;
        switch (getWorld().getDifficulty()) {
            case EASY:
                i = Math.round((float)i * 0.5F);
                break;
            case NORMAL:
                i = Math.round((float)i * 0.75F);
                break;
            default:
                //
        }
        int p = (i > 2) ? (i / 5) : 0;
        return (p <= 0) ? i : (i - p + getRNG().nextInt(p + 1));
    }

    /**
     *
     * @param m
     * @param village
     * @param pos
     * @return
     */
    private Vec3d locateSpawnPos(BlockPos.MutableBlockPos m, Village village, BlockPos pos) {
        final World world = getWorld();
        final Random rnd = getRNG();
        if (m == null)
            m = new BlockPos.MutableBlockPos();
        BlockPos center = village.getCenter();
        int i = village.getVillageRadius();
        double r = (double)(4 * i * i);
        Block b;
        for (i = 0; i < 40; i++) {
            m.setPos(pos.getX() + rnd.nextInt(16) - 8, pos.getY() + rnd.nextInt(10) - 5, pos.getZ() + rnd.nextInt(16) - 8);
            if ((m.distanceSq(center) < r) && world.isBlockLoaded(m, false)) {
                b = world.getBlockState(m).getBlock();
                if (b.isPassable(world, m) && WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, world, m))
                    if (!isDoorNearby(village, m) || (world.getTopSolidOrLiquidBlock(m).getY() <= m.getY())) // Prevent spawning INSIDE houses and f*king up poor testificates
                        return new Vec3d((double)m.getX() + 0.5D, (double)m.getY(), (double)m.getZ() + 0.5D);
            }
        }
        return null;
    }

    /**
     *
     * @param p
     * @return
     */
    private boolean isDoorNearby(Village village, BlockPos p) {
        for (VillageDoorInfo di : village.getVillageDoorInfoList())
            if (di.getDistanceToInsideBlockSq(p) <= 150)
                return true;
        return false;
    }
}
