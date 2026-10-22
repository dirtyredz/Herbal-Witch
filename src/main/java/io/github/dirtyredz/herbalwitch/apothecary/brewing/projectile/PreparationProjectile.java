package io.github.dirtyredz.herbalwitch.apothecary.brewing.projectile;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.advanced.ThrowableApplications.Burst;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.item.ThrowablePreparationItem;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.BrewingEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * One thrown preparation in flight ({@code herbalwitch:brewing/preparation_projectile}). Its burst
 * comes from the registered item it carries, so a reloaded projectile still knows what it is. It
 * expires after {@link #LIFETIME} loaded ticks, never flies into an unloaded chunk (it is discarded at
 * the edge instead, loading nothing), and on impact applies its burst once, server side
 * ({@link PreparationImpact}). It counts against {@link ProjectileLimits} while it is in a level.
 */
public class PreparationProjectile extends ThrowableItemProjectile {
    public static final int LIFETIME = 100;
    private int age;
    private java.util.UUID counted;

    public PreparationProjectile(EntityType<? extends PreparationProjectile> type, Level level) {
        super(type, level);
    }

    public PreparationProjectile(Level level, LivingEntity thrower, Burst burst) {
        super(BrewingEntities.PREPARATION_PROJECTILE.get(), thrower, level);
    }

    @Override
    protected Item getDefaultItem() {
        return AdvancedPreparations.item(AdvancedProductCatalog.POISON);
    }

    /** The burst of the item it carries. */
    public Burst burst() {
        return getItem().getItem() instanceof ThrowablePreparationItem t ? t.burst() : Burst.POISON;
    }

    @Override
    protected float getGravity() {
        return 0.05F;
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            age++;
            Vec3 next = position().add(getDeltaMovement());
            if (age > LIFETIME || !level().hasChunkAt(BlockPos.containing(next))) {
                discard();
                return;
            }
        }
        super.tick();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (level() instanceof ServerLevel server && !isRemoved()) {
            PreparationImpact.burst(server, result.getLocation(), burst(), getOwner(), this);
            discard();
        }
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!level().isClientSide) {
            counted = getOwner() == null ? null : getOwner().getUUID();
            ProjectileLimits.joined(level(), getUUID(), counted);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level().isClientSide) {
            ProjectileLimits.left(level(), getUUID(), counted);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Age", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        age = tag.getInt("Age");
    }
}
