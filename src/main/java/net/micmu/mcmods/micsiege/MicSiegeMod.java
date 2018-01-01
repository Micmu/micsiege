package net.micmu.mcmods.micsiege;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import net.micmu.mcmods.micsiege.core.SiegeCore;

/**
 *
 * @author Micmu
 */
@EventBusSubscriber
@Mod(modid = MicSiegeMod.MODID, name = MicSiegeMod.NAME, version = MicSiegeMod.VERSION, acceptedMinecraftVersions = MicSiegeMod.ACCEPTED_MINECRAFT_VERSIONS, acceptableRemoteVersions = "*")
public class MicSiegeMod {
    public static final String MODID = "micsiege";
    public static final String NAME = "Brutal Zombie Siege";
    public static final String VERSION = "1.0.0";
    public static final String ACCEPTED_MINECRAFT_VERSIONS = "[1.12,1.13)";
    public static final String CONFIG_FILE_NAME = "BrutalZombieSiege";

    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance(MODID)
    public static MicSiegeMod INSTANCE;

    /**
     *
     * @param event
     */
    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        final World world = event.getWorld();
        if (!world.isRemote && (world instanceof WorldServer))
            SiegeCore.getInstance().setupWorld((WorldServer)world);
    }

    /**
     *
     * @param event
     */
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (MODID.equals(event.getModID()))
            ConfigManager.load(MODID, Config.Type.INSTANCE);
    }
}
