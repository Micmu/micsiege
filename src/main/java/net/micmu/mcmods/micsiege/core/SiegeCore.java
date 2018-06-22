package net.micmu.mcmods.micsiege.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.village.VillageSiege;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import net.micmu.mcmods.micsiege.MicSiegeMod;

/**
 *
 * @author Micmu
 */
public class SiegeCore {
    private static final SiegeCore INSTANCE = new SiegeCore();

    private final Set<Class<? extends SiegeAIBase>> siegeClasses = new HashSet<>(4);
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
        // The default siege... only 1 so far.
        registerSiege(SiegeZombies.class);
    }

    /**
     *
     * @param siegeAIClass
     */
    public void registerSiege(@Nonnull Class<? extends SiegeAIBase> siegeAIClass) {
        siegeClasses.add(siegeAIClass);
    }

    /**
     *
     * @param siegeAIClass
     */
    public void unregisterSiege(@Nonnull Class<? extends SiegeAIBase> siegeAIClass) {
        siegeClasses.remove(siegeAIClass);
    }

    /**
     *
     * @param world
     * @return
     */
    public boolean initializeWorld(@Nonnull WorldServer world) {
        final int dimension = world.provider.getDimension();
        final Field f = getSiegeField();
        if (f == null)
            return false;
        try {
            VillageSiege siege = (VillageSiege)f.get(world);
            List<SiegeAIBase> handlers = createSiegeHandlers(world);
            if (handlers.isEmpty()) {
                // Disable village siege mechanics in Nether and End
                if (!(siege instanceof VillageSiegeDisable)) {
                    f.set(world, new VillageSiegeDisable());
                    if (MicSiegeMod.LOG.isDebugEnabled())
                        MicSiegeMod.LOG.debug("Disabled Zombie Siege mechanics for world " + dimension + ".");
                    return true;
                }
            } else if (!(siege instanceof VillageSiegeManager)) {
                f.set(world, new VillageSiegeManager(world, handlers.toArray(new SiegeAIBase[handlers.size()])));
                if (MicSiegeMod.LOG.isDebugEnabled()) {
                    StringBuilder sb = new StringBuilder(128);
                    sb.append("Injected replacement Zombie Siege mechanics for world ");
                    sb.append(dimension);
                    sb.append(" (");
                    boolean v = false;
                    for (SiegeAIBase b : handlers) {
                        if (v)
                            sb.append(", ");
                        else
                            v = true;
                        sb.append(b.getClass().getName());
                    }
                    sb.append(')');
                    MicSiegeMod.LOG.debug(sb.toString());
                }
                return true;
            }
        } catch (Throwable t) {
            reflectionFail(t);
        }
        return false;
    }

    /**
     *
     * @param world
     * @param pos
     * @return
     */
    public int triggerSiegeNow(@Nonnull World world, @Nonnull BlockPos pos) {
        final VillageSiegeManager manager = (world instanceof WorldServer) ? getManager((WorldServer)world) : null;
        if (manager == null)
            return 0;
        final Village village = world.getVillageCollection().getNearestVillage(pos, 32);
        if ((village == null) || village.isAnnihilated())
            return -1;
        return manager.triggerSiege(village);
    }

    /**
     *
     * @param world
     * @return
     */
    private VillageSiegeManager getManager(@Nonnull WorldServer world) {
        Field f = getSiegeField();
        if (f != null) {
            try {
                VillageSiege siege = (VillageSiege)f.get(world);
                if (siege instanceof VillageSiegeManager)
                    return (VillageSiegeManager)siege;
            } catch (Throwable t) {
                reflectionFail(t);
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    private Field getSiegeField() {
        Field f = this.injectField;
        if (f == null) {
            try {
                this.injectField = f = ReflectionHelper.findField(WorldServer.class, "field_175740_d", "villageSiege");
            } catch (Throwable t) {
                reflectionFail(t);
                return null;
            }
        }
        return f;
    }

    /**
     *
     * @param t
     */
    private void reflectionFail(Throwable t) {
        this.injectField = null;
        if (!exceptionDumped) {
            exceptionDumped = true;
            MicSiegeMod.LOG.error("Minecraft/Forge/other mod compatiblitiy issue?", t);
        }
        MicSiegeMod.LOG.error("*** Failed to set up Zombie Siege mechanics! Enchanced Zombie Siege mechanics will not work! ***");
    }

    /**
     *
     * @param world
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private List<SiegeAIBase> createSiegeHandlers(@Nonnull WorldServer world) throws InstantiationException, IllegalAccessException {
        List<SiegeAIBase> out = new ArrayList<>();
        int i = 0;
        SiegeAIBase b;
        for (Class<? extends SiegeAIBase> c : siegeClasses) {
            b = c.newInstance();
            b.setWorld(world);
            if (b.onInitializeWorld())
                out.add(b);
            b.setWorld(null);
        }
        return out;
    }
}
