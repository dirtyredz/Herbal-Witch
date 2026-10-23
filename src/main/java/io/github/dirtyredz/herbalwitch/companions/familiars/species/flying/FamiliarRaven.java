package io.github.dirtyredz.herbalwitch.companions.familiars.species.flying;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;

/** Wild and bound bird. The appearance field contains no ownership or inventory authority. */
public final class FamiliarRaven extends Animal implements FlyingAnimal {
    private static final EntityDataAccessor<Integer> APPEARANCE = SynchedEntityData.defineId(FamiliarRaven.class, EntityDataSerializers.INT);
    public FamiliarRaven(EntityType<? extends Animal> type, Level level) { super(type, level); moveControl = FamiliarFlight.control(this); }
    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(APPEARANCE, 0); }
    public int appearance() { return entityData.get(APPEARANCE); }
    public void appearance(int value) { entityData.set(APPEARANCE, Math.max(0, Math.min(1, value))); }
    @Override public SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty,
            MobSpawnType reason, SpawnGroupData group, CompoundTag tag) {
        appearance(random.nextBoolean() ? 1 : 0); return super.finalizeSpawn(level, difficulty, reason, group, tag);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putInt("Appearance", appearance()); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); appearance(tag.getInt("Appearance")); }
    @Override protected void registerGoals() { goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal(this, .8)); }
    @Override protected PathNavigation createNavigation(Level level) { return FamiliarFlight.navigation(this, level); }
    @Override public boolean isFlying() { return !onGround(); }
    @Override public boolean causeFallDamage(float distance, float multiplier, net.minecraft.world.damagesource.DamageSource source) { return false; }
    @Override protected void checkFallDamage(double distance, boolean grounded, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos) { }
    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) { return null; }
}
