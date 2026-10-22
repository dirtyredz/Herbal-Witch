package io.github.dirtyredz.herbalwitch.companions.animals.capture;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

/** Non-stackable handheld transport, separate from M7's equipped protective charms. */
public final class CaptureCharmItem extends Item {
    private final boolean filled;
    public CaptureCharmItem(boolean filled) { super(new Properties().stacksTo(1)); this.filled = filled; }
    public boolean filled() { return filled; }
    @Override public InteractionResult useOn(UseOnContext context) {
        if (!filled) return InteractionResult.PASS;
        if (context.getPlayer() instanceof ServerPlayer player) {
            if (!io.github.dirtyredz.herbalwitch.companions.animals.care.interaction.AnimalInteractionEvents.allowRequest(player)) return InteractionResult.SUCCESS;
            message(player, CaptureService.release(player, context.getHand(), context.getClickedPos(), context.getClickedFace()));
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
    public static void message(ServerPlayer player, String reason) {
        player.displayClientMessage(Component.translatable("animal.herbalwitch.capture." + (reason == null ? "success" : reason)), true);
    }
}
