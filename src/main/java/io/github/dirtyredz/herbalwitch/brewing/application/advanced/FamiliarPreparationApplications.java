package io.github.dirtyredz.herbalwitch.brewing.application.advanced;

import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * The seam where familiar bonding (Milestone 9) will give the familiar preparations their effect,
 * keyed by the permanent product, without changing a recipe and without a second bond store here.
 * Until a consumer is installed every use is refused as {@link Result#UNAVAILABLE} and nothing is
 * consumed; the item says "Familiar application becomes available with familiar bonding".
 */
public final class FamiliarPreparationApplications {
    /** What a familiar application did. */
    public enum Result {
        /** Applied; the item consumes one and returns its container. */
        APPLIED,
        /** Refused, nothing consumed (no consumer yet, not a bonded familiar, ...). */
        UNAVAILABLE
    }

    /** Applies a familiar preparation from {@code player}'s hand to {@code target}. */
    @FunctionalInterface
    public interface Consumer {
        Result apply(ServerPlayer player, ProductSpec product, ItemStack stack, LivingEntity target, InteractionHand hand);
    }

    private static final Consumer UNAVAILABLE = (player, product, stack, target, hand) -> Result.UNAVAILABLE;
    private static volatile Consumer consumer = UNAVAILABLE;

    private FamiliarPreparationApplications() {
    }

    /** Installed once by familiar bonding (Milestone 9). */
    public static void install(Consumer installed) {
        consumer = installed;
    }

    public static boolean available() {
        return consumer != UNAVAILABLE;
    }

    public static Result apply(ServerPlayer player, ProductSpec product, ItemStack stack, LivingEntity target, InteractionHand hand) {
        return consumer.apply(player, product, stack, target, hand);
    }
}
