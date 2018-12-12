package net.micmu.mcmods.micsiege.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Nonnull;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.world.World;

/**
 *
 * @author Micmu
 */
final class SpawnableEntity {
    private final Class<? extends EntityCreature> entityClass;
    private final Constructor<? extends EntityCreature> constructor;
    private final int weight;

    /**
     *
     * @param entityClass
     * @param weight
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    SpawnableEntity(@Nonnull Class<? extends EntityCreature> entityClass, int weight) throws NoSuchMethodException, SecurityException {
        this.constructor = entityClass.getDeclaredConstructor(World.class);
        if (!this.constructor.isAccessible())
            this.constructor.setAccessible(true);
        this.entityClass = entityClass;
        this.weight = weight;
    }

    /**
     *
     * @return
     */
    int getWeight() {
        return weight;
    }

    /**
     *
     * @return
     */
    boolean isDesertOnly() {
        return EntityHusk.class.isAssignableFrom(this.entityClass);
    }

    /**
     *
     * @param world
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    @Nonnull
    EntityCreature newInstance(@Nonnull World world) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return this.constructor.newInstance(world);
    }
}
