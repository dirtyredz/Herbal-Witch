package io.github.dirtyredz.herbalwitch.companions.familiars.species.ground;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.level.Level;

/** Native appearance and NBT, with movement and targeting owned by familiar goals. */
public final class FamiliarRabbit extends Rabbit {
    public FamiliarRabbit(EntityType<? extends Rabbit> type, Level level) { super(type, level); setPersistenceRequired(); }
    @Override protected void registerGoals() {}
}

