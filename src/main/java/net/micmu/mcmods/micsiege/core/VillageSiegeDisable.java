package net.micmu.mcmods.micsiege.core;

import net.minecraft.village.VillageSiege;

/**
 *
 * @author Micmu
 */
final class VillageSiegeDisable extends VillageSiege {

    /**
     *
     */
    VillageSiegeDisable() {
        super(null);
    }

    /**
     *
     */
    @Override
    public void tick() {
        // Sieges are disabled. Do nothing.
    }
}
