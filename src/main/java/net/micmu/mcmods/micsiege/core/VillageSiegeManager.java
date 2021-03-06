package net.micmu.mcmods.micsiege.core;

import net.minecraft.village.Village;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import net.micmu.mcmods.micsiege.Config;
import net.micmu.mcmods.micsiege.MicSiegeMod;

/**
 *
 * @author Micmu
 */
final class VillageSiegeManager extends VillageSiege {
    private final World world;
    private final SiegeAIBase[] sieges;
    private int tick;
    private SiegeAIBase active = null;

    /**
     *
     * @param world
     * @param sieges
     */
    VillageSiegeManager(World world, SiegeAIBase[] sieges) {
        super(null);
        this.world = world;
        this.sieges = sieges;
        this.tick = 100 + world.rand.nextInt(150);
    }

    /**
     *
     */
    @Override
    public void tick() {
        if (tick > 0) {
            tick--;
        } else if (this.active != null) {
            final SiegeAIBase s = this.active;
            try {
                switch (s.getActive()) {
                    case 1:
                        s.onStart();
                        s.setActive(2);
                        if (MicSiegeMod.LOG.isTraceEnabled())
                            MicSiegeMod.LOG.trace("Siege [" + s.getClass().getName() + "] spawn logic started in village @ " + s.getVillagePos());
                        return;
                    case 2:
                        if ((world.getDifficulty() == EnumDifficulty.PEACEFUL) || (!s.isForced() && !s.checkDaytime(false)) || !s.decTickCount()) {
                            s.setActive(3);
                        } else {
                            s.tick();
                            if (s.getTickCount() <= 0)
                                s.setActive(3);
                        }
                        tick = 2 + s.getRNG().nextInt(3);
                        return;
                    default:
                        s.onStop();
                        if (MicSiegeMod.LOG.isTraceEnabled())
                            MicSiegeMod.LOG.trace("Siege [" + s.getClass().getName() + "] spawn logic finished" + ((s.isSiegeAverted() ? " (siege averted)." : (" (mobs spawned: " + s.getSpawnCount() + ")."))));
                }
            } catch (Exception e) {
                MicSiegeMod.LOG.error("Siege [" + s.getClass().getName() + "] spawn logic terminated because of an exception.", e);
            }
            s.setActive(0);
            s.resetTick();
            this.active = null;
            tick = 100 + world.rand.nextInt(150);
        } else if (triggerSiege(null) == 0) {
            tick = 91 + world.rand.nextInt(10);
        }
    }

    /**
     *
     * @param forceVillage
     * @return
     */
    int triggerSiege(Village forceVillage) {
        if (active != null)
            return -2;
        if (world.getDifficulty() == EnumDifficulty.PEACEFUL)
            return -3;
        if ((forceVillage != null) || (Config.chancesPerNight > 0)) {
            for (SiegeAIBase s : this.sieges) {
                if (s.getActive() == 0) {
                    s.setWorld(world);
                    if (forceVillage != null) {
                        s.setVillage(forceVillage);
                        s.setForced(true);
                    }
                    if (s.startTick()) {
                        s.setActive(1);
                        active = s;
                        tick = 3;
                        return 1;
                    }
                    s.setWorld(null);
                }
            }
        }
        return -4;
    }
}
