package io.github.dirtyredz.herbalwitch.companions.familiars.species.ground;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.level.Level;

/** Native appearance and NBT, with movement and targeting owned by familiar goals. */
public final class FamiliarFox extends Fox {
    public FamiliarFox(EntityType<? extends Fox> type, Level level) { super(type, level); setPersistenceRequired(); }
    @Override protected void registerGoals() {}

    @Override public boolean isSleeping() { return false; }
    @Override public boolean isFaceplanted() { return false; }
    @Override protected void customServerAiStep() { setSitting(false); setIsCrouching(false); setIsInterested(false); }
}
