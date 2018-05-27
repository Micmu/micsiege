package net.micmu.mcmods.micsiege;

import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;

/**
 *
 * @author Micmu
 */
@net.minecraftforge.common.config.Config(modid = MicSiegeMod.MODID, name = MicSiegeMod.CONFIG_FILE_NAME)
public class Config {

    @Comment("Chances per night (percentage) for the event to occur in any village.\nSet this to 0 to disable Zombie Siege events altogether.\nValue for vanilla sieges is 10.")
    @RangeInt(min = 0, max = 100)
    public static int chancesPerNight = 10;

    @Comment("If 'true', events will not occur exactly at midnight.\nEvents might occur at any time between midnight and 3 am. Vanilla behavior is false.")
    public static boolean randomizeTime = true;

    @Comment("If 'true', one or more lightning bolts will strike at the spawn location.\nThis lightning doesn't do any damage.")
    public static boolean lightningStrikes = true;

    @Comment("Distance from village center where zombies may spawn; multiplier of village radius.\nValue of 1.4 means the zombies will spawn at 140% of village radius away from center.\nVanilla sieges are using multiplier of 0.9.")
    @RangeDouble(min = 0.2D, max = 1.9D)
    public static double villageDistanceMultiplier = 1.4D;

    @Comment("Minimum number of village doors required for the event to occur.\nValue for vanilla sieges is 10.")
    @RangeInt(min = 1, max = 200)
    public static int villageMinDoors = 10;

    @Comment("Minimum number of villagers required for the event to occur.\nValue for vanilla sieges is 20.")
    @RangeInt(min = 1, max = 200)
    public static int villageMinVillagers = 20;

    @Comment("Maximum number of zombies to spawn when playing on HARD difficulty.\nOn EASY, 50% of that value is used and 75% on NORMAL.")
    @RangeInt(min = 1, max = 300)
    public static int zombieSpawnCount = 20;

    @Comment("If 'true', spawned zombies will not despawn until killed.\nThis option is useful if you are using large 'villageDistanceMultiplier'.")
    public static boolean zombiePreventDespawn = false;

    @Comment("If 'true', spawned zombies will not be able to break doors.\nThis option will prevent siege Zombies breaking wooden doors on Hard difficulty.\nOption has no effect on regular Zombies.")
    public static boolean zombiePreventBreakDoors = false;

    @Comment("Chance for a Zombie Villager to spawn instead of a regular one (percentage).\nValue for vanilla sieges is 0.")
    @RangeInt(min = 0, max = 100)
    public static int zombieVillagerChance = 5;
}
