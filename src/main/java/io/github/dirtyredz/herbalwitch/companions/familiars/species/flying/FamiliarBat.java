package io.github.dirtyredz.herbalwitch.companions.familiars.species.flying;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

public final class FamiliarBat extends Bat {
    public FamiliarBat(EntityType<? extends Bat> type, Level level) { super(type, level); moveControl = FamiliarFlight.control(this); setResting(false); setPersistenceRequired(); }
    @Override protected void customServerAiStep() { setResting(false); }
    @Override protected PathNavigation createNavigation(Level level) { return FamiliarFlight.navigation(this, level); }
    @Override public boolean causeFallDamage(float distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) { return false; }
}
