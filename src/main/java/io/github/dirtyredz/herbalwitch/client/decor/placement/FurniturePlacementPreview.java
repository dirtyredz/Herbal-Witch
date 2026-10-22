package io.github.dirtyredz.herbalwitch.client.decor.placement;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.dirtyredz.herbalwitch.decor.structure.FurniturePlacementItem;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementChecks;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementPlan;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.List;
import java.util.Optional;

/**
 * Hammer-style placement preview: while the player holds a furniture item and aims at a block, the
 * very plan the server would place ({@link FurniturePlacementItem#plan}, the shared resolver) is
 * drawn as see-through models of every cell in its real states (a door's hinge, a bed's direction)
 * with a green outline, or red when the client can already tell the place will not do (occupied,
 * unsupported, outside the world). Permission is the server's to judge: the preview never claims it.
 * No packets, no client-only positioning, no alternate anchors.
 */
public final class FurniturePlacementPreview {
    private static final float ALPHA = 0.55F;

    private FurniturePlacementPreview() {
    }

    public static void register(IEventBus forgeBus) {
        forgeBus.addListener(FurniturePlacementPreview::render);
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
            if (stack.getItem() instanceof FurniturePlacementItem item && item.getBlock() instanceof StructurePlacement structure) {
                BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(mc.player, hand, hit));
                if (!context.canPlace()) {
                    return;
                }
                Optional<PlacementPlan> plan = item.plan(context);
                plan.ifPresent(p -> draw(event, mc, p, PlacementChecks.checkGeometry(context, p, structure).isEmpty()));
                return;
            }
        }
    }

    private static void draw(RenderLevelStageEvent event, Minecraft mc, PlacementPlan plan, boolean placeable) {
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher blocks = mc.getBlockRenderer();
        List<PlacementPreviewGeometry.Cell> cells = PlacementPreviewGeometry.cells(plan, camera);
        VertexConsumer ghost = new TranslucentConsumer(buffers.getBuffer(RenderType.translucent()), ALPHA);
        for (PlacementPreviewGeometry.Cell cell : cells) {
            pose.pushPose();
            pose.translate(cell.offset().x, cell.offset().y, cell.offset().z);
            blocks.getModelRenderer().renderModel(pose.last(), ghost, cell.cell().state(), blocks.getBlockModel(cell.cell().state()),
                    1.0F, 1.0F, 1.0F, 0xF000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.translucent());
            pose.popPose();
        }
        buffers.endBatch(RenderType.translucent());
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());
        float r = placeable ? 0.3F : 0.9F;
        float g = placeable ? 0.9F : 0.25F;
        for (PlacementPreviewGeometry.Cell cell : cells) {
            LevelRenderer.renderLineBox(pose, lines, cell.outline(), r, g, 0.3F, 0.8F);
        }
        buffers.endBatch(RenderType.lines());
    }

    /** Passes vertices through with their alpha scaled, so the ghost models are see-through. */
    private record TranslucentConsumer(VertexConsumer delegate, float alpha) implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            delegate.vertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int a) {
            delegate.color(red, green, blue, Math.round(a * alpha));
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            delegate.uv(u, v);
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            delegate.overlayCoords(u, v);
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            delegate.uv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            delegate.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            delegate.endVertex();
        }

        @Override
        public void defaultColor(int red, int green, int blue, int a) {
            delegate.defaultColor(red, green, blue, Math.round(a * alpha));
        }

        @Override
        public void unsetDefaultColor() {
            delegate.unsetDefaultColor();
        }
    }
}
