package net.micmu.mcmods.micsiege.core;

import java.lang.reflect.Field;

import net.minecraft.village.VillageSiege;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import net.micmu.mcmods.micsiege.MicSiegeMod;

/**
 *
 * @author Micmu
 */
public class SiegeCore {
    private static final SiegeCore INSTANCE = new SiegeCore();

    private boolean exceptionDumped = false;
    private Field injectField = null;

    /**
     *
     * @return
     */
    public static SiegeCore getInstance() {
        return INSTANCE;
    }

    /**
     *
     */
    private SiegeCore() {
    }

    /**
     *
     * @param world
     * @return
     */
    public boolean setupWorld(WorldServer world) {
        final int dimension = world.provider.getDimension();
        try {
            Field f = this.injectField;
            if (f == null) {
                f = ReflectionHelper.findField(WorldServer.class, "field_175740_d", "villageSiege");
                f.setAccessible(true);
                this.injectField = f;
            }
            VillageSiege siege = (VillageSiege)f.get(world);
            if ((dimension == -1) || (dimension == 1)) {
                // Disable village siege mechanics in Nether and End
                if (!(siege instanceof VillageSiegeDisable)) {
                    f.set(world, new VillageSiegeDisable());
                    if (MicSiegeMod.LOG.isDebugEnabled())
                        MicSiegeMod.LOG.debug("Disabled Zombie Siege mechanics for world: " + dimension + " [" + world.getClass().getName() + "]");
                    return true;
                }
            } else if (!(siege instanceof VillageSiegeManager)) {
                f.set(world, new VillageSiegeManager(world));
                if (MicSiegeMod.LOG.isDebugEnabled())
                    MicSiegeMod.LOG.debug("Injected replacement Zombie Siege mechanics for world: " + dimension + " [" + world.getClass().getName() + "]");
                return true;
            }
        } catch (Throwable t) {
            this.injectField = null;
            if (!exceptionDumped) {
                exceptionDumped = true;
                MicSiegeMod.LOG.error("Minecraft/Forge compatiblitiy issue?", t);
            }
            MicSiegeMod.LOG.error("*** Failed to set up Zombie Siege mechanics! Enchanced Zombie Siege mechanics will not work! ***");
        }
        return false;
    }
}
