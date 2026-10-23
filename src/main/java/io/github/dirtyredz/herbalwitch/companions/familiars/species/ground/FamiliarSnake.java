package io.github.dirtyredz.herbalwitch.companions.familiars.species.ground;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

public final class FamiliarSnake extends Animal {
    public FamiliarSnake(EntityType<? extends Animal> type, Level level) { super(type, level); }
    @Override protected void registerGoals() { goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal(this, .8)); }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return null; }
}
