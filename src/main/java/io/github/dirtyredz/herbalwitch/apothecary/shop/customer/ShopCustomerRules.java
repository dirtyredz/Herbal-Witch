package io.github.dirtyredz.herbalwitch.apothecary.shop.customer;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Which real villager may become a customer, and when it has arrived. Only alive, awake, adult,
 * unridden, AI-driven vanilla villagers that are not trading, hurt, panicking, hiding or raiding
 * qualify; an employed villager at work is left alone (vanilla safety and work keep priority).
 * Commerce never replaces a native behaviour: it only offers a walk target the villager's own brain
 * follows.
 */
public final class ShopCustomerRules {
    private ShopCustomerRules() {
    }

    public static boolean eligible(Villager villager) {
        if (!villager.isAlive() || villager.isRemoved() || villager.isBaby() || villager.isSleeping() || villager.isNoAi()
                || villager.isPassenger() || villager.isVehicle() || villager.getTradingPlayer() != null) return false;
        Brain<Villager> brain = villager.getBrain();
        if (brain.hasMemoryValue(MemoryModuleType.HURT_BY) || brain.hasMemoryValue(MemoryModuleType.HURT_BY_ENTITY)) return false;
        if (brain.isActive(Activity.PANIC) || brain.isActive(Activity.HIDE) || brain.isActive(Activity.RAID)
                || brain.isActive(Activity.PRE_RAID)) return false;
        return !(brain.isActive(Activity.WORK) && brain.hasMemoryValue(MemoryModuleType.JOB_SITE));
    }

    /** Within two blocks of the register (feet to the register cell centre, half-block tolerance) with line of sight. */
    public static boolean arrived(Villager villager, BlockPos register) {
        Vec3 centre = Vec3.atCenterOf(register);
        if (villager.position().distanceTo(centre) > ShopRules.ARRIVAL_DISTANCE + 0.5) return false;
        BlockHitResult hit = villager.level().clip(new ClipContext(villager.getEyePosition(), centre, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, villager));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(register);
    }
}
