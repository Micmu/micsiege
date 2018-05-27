package net.micmu.mcmods.micsiege.core;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.Village;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;

import net.micmu.mcmods.micsiege.Config;
import net.micmu.mcmods.micsiege.MicSiegeMod;

/**
 *
 * @author Micmu
 */
public abstract class SiegeAIBase {
    private static final Predicate<EntityPlayer> VALID_PLAYER = new Predicate<EntityPlayer>() {
        @Override
        public boolean apply(@Nullable EntityPlayer p) {
            return EntitySelectors.NOT_SPECTATING.apply(p) && !p.isDead && !p.isPlayerSleeping();
        }
    };
    private int active;
    private int checkStage = -1;
    private int spawnCount;
    private World world;
    private Village village;
    private String villageName;
    private boolean exceptFlag = false;
    private int tickCount;
    private long nextLigtning = -1L;

    /**
     *
     * @return
     */
    boolean startTick() {
        onCheck();
        if (checkDaytime(false)) {
            if ((checkStage == 0) && checkDaytime(true))
                checkStage = checkChance() ? 1 : 2;
            if ((checkStage == 1) && tryStart()) {
                checkStage = 3;
                return true;
            }
            return false;
        } else {
            checkStage = 0;
            return false;
        }
    }

    /**
     *
     */
    void resetTick() {
        world = null;
        village = null;
        villageName = null;
        exceptFlag = false;
        spawnCount = 0;
        tickCount = 0;
        nextLigtning = -1L;
    }

    /**
     *
     * @return
     */
    boolean decTickCount() {
        int t = getTickCount();
        if (t <= 0)
            return false;
        setTickCount(t - 1);
        return true;
    }

    /**
     *
     * @return
     */
    boolean isSiegeAverted() {
        return (spawnCount == 0);
    }

    /**
     *
     * @param precise
     * @return
     */
    protected abstract boolean checkDaytime(boolean precise);

    /**
     *
     * @return
     */
    protected abstract boolean checkChance();

    /**
     *
     * @return
     */
    protected abstract boolean tryStart();

    /**
     *
     */
    protected abstract void tick();

    /**
     *
     */
    protected void onCheck() {
    }

    /**
     *
     * @return
     */
    protected boolean onInitializeWorld() {
        return true;
    }

    /**
     *
     */
    protected void onStart() {
    }

    /**
     *
     */
    protected void onStop() {
    }

    /**
     *
     * @param active
     */
    void setActive(int active) {
        this.active = active;
    }

    /**
     *
     * @return
     */
    int getActive() {
        return active;
    }

    /**
     *
     * @param world
     */
    void setWorld(World world) {
        this.world = world;
    }

    /**
     *
     * @return
     */
    public World getWorld() {
        return world;
    }

    /**
     *
     * @return
     */
    public Random getRNG() {
        return world.rand;
    }

    /**
     *
     * @return
     */
    public Village getVillage() {
        return village;
    }

    /**
     *
     * @param village
     */
    protected void setVillage(Village village) {
        if (village != this.village) {
            this.village = village;
            this.villageName = null;
        }
    }

    /**
     *
     */
    protected void stopSiege() {
        setTickCount(0);
    }

    /**
     *
     * @param c
     */
    protected void setTickCount(int c) {
        tickCount = c;
    }

    /**
     *
     * @return
     */
    public int getTickCount() {
        return tickCount;
    }

    /**
     *
     * @return
     */
    protected List<EntityPlayer> getRandomizedPlayers() {
        List<EntityPlayer> lst = world.getPlayers(EntityPlayer.class, VALID_PLAYER);
        int toi = lst.size();
        if (toi > 1) {
            Random rnd = getRNG();
            int a;
            int b;
            EntityPlayer tmp;
            for (int i = 1; i < toi; i++) {
                a = rnd.nextInt(toi);
                b = rnd.nextInt(toi);
                if (a != b) {
                    tmp = lst.get(a);
                    lst.set(a, lst.get(b));
                    lst.set(b, tmp);
                }
            }
        }
        return lst;
    }

    /**
     *
     * @param creature
     * @param pos
     * @return
     */
    protected boolean spawnAttacker(EntityCreature creature, Vec3d pos) {
        if (pos == null)
            return false;
        try {
            creature.setLocationAndAngles(pos.x, pos.y, pos.z, getRNG().nextFloat() * 360.0F, 0.0F);
            creature.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(creature)), (IEntityLivingData)null);
            if (creature instanceof EntityZombie) {
                // Prevent zombies breaking doors
                if (Config.zombiePreventBreakDoors && ((EntityZombie)creature).isBreakDoorsTaskSet())
                    ((EntityZombie)creature).setBreakDoorsAItask(false);
                // Setup random profession for Zombie Villagers
                if (creature instanceof EntityZombieVillager) {
                    VillagerProfession p = getRandomVillagerProfession();
                    if (p != null)
                        ((EntityZombieVillager)creature).setForgeProfession(p);
                }
            }
            if (world.spawnEntity(creature)) {
                spawnCount++;
                if (Config.zombiePreventDespawn)
                    creature.enablePersistence();
                Village v = getVillage();
                if (v != null) {
                    // Make them move towards the village
                    creature.setHomePosAndDistance(v.getCenter(), v.getVillageRadius());
                    // Setup homing AI if not present, so it will move towards the village.
                    // Zombies already have it, so don't check for them.
                    if (!(creature instanceof EntityZombie)) {
                        boolean needsHoming = true;
                        for (EntityAITasks.EntityAITaskEntry e : creature.tasks.taskEntries) {
                            if (e.action instanceof EntityAIMoveTowardsRestriction) {
                                needsHoming = false;
                                break;
                            }
                        }
                        if (needsHoming)
                            creature.tasks.addTask(5, new EntityAIMoveTowardsRestriction(creature, 1.0D));
                    }
                }
                if (Config.lightningStrikes) {
                    // Make kaboom every now and then
                    long now = world.getTotalWorldTime();
                    if (now >= nextLigtning) {
                        nextLigtning = now + (long)(19 + getRNG().nextInt(12));
                        world.addWeatherEffect(new EntityLightningBolt(getWorld(), pos.x, pos.y, pos.z, true));
                    }
                }
                return true;
            }
        } catch (Exception ex) {
            if (!exceptFlag) {
                exceptFlag = true;
                MicSiegeMod.LOG.error("Failed to spawn siege mob", ex);
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public int getSpawnCount() {
        return spawnCount;
    }

    /**
     *
     * @return
     */
    BlockPos getVillagePos() {
        Village v = getVillage();
        return (v != null) ? v.getCenter() : null;
    }

    /**
     *
     * @param f
     * @return
     */
    public boolean isCelestialAngle(float f) {
        final float x = world.getCelestialAngle(0.0F);
        return (x > (f - 0.001F)) && (x < (f + 0.015F));
    }

    /**
     *
     * @return
     */
    private VillagerRegistry.VillagerProfession getRandomVillagerProfession() {
        Set<Map.Entry<ResourceLocation, VillagerRegistry.VillagerProfession>> entries = ForgeRegistries.VILLAGER_PROFESSIONS.getEntries();
        if (entries.isEmpty())
            return null;
        ResourceLocation loc;
        VillagerProfession p = null;
        int e = 1 + getRNG().nextInt(entries.size());
        Iterator<Map.Entry<ResourceLocation, VillagerRegistry.VillagerProfession>> it = entries.iterator();
        for (int i = 0; i < e; i++)
            p = it.next().getValue();
        return p;
    }
}
