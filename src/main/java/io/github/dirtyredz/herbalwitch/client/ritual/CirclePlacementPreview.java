package io.github.dirtyredz.herbalwitch.client.ritual;

import io.github.dirtyredz.herbalwitch.client.placement.PlacementPreviewRenderer;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.CircleLayout;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualItems;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementPlan;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementResolver;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacementItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.IEventBus;

/**
 * While the player holds a ritual circle, the shared structure preview draws the ring; this adds, as a
 * distinct plum guide, the altar the circle needs: where its origin must stand and which way it must
 * face (the same {@link CircleLayout} arithmetic the server validates with). Advisory only.
 */
public final class CirclePlacementPreview {
    private CirclePlacementPreview() {
    }

    public static void register(IEventBus forge) {
        forge.addListener(CirclePlacementPreview::render);
    }

    private static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = mc.player.getItemInHand(hand);
            if (stack.is(RitualItems.CIRCLE.get()) && stack.getItem() instanceof StructurePlacementItem item) {
                BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(mc.player, hand, hit));
                if (!context.canPlace()) {
                    return;
                }
                item.plan(context).ifPresent(circle -> PlacementPreviewRenderer.draw(event, mc, altar(circle), 0.75F, 0.35F, 0.9F));
                return;
            }
        }
    }

    /** The altar plan a circle plan requires. */
    public static PlacementPlan altar(PlacementPlan circle) {
        AltarBlock altar = (AltarBlock) RitualBlocks.ALTAR.get();
        BlockState state = altar.defaultBlockState().setValue(AltarBlock.FACING, circle.facing());
        return PlacementResolver.resolve(CircleLayout.altarOrigin(circle.origin(), circle.facing()), state, altar);
    }
}
