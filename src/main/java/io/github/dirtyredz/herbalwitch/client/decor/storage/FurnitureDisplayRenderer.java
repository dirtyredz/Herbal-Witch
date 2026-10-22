package io.github.dirtyredz.herbalwitch.client.decor.storage;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureStorageEntity;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.StorageFurnitureBlock;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.display.DisplaySpec;
import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Draws what a display shelf actually holds: each stored item standing at its slot's authored anchor
 * ({@link DisplaySpec}), turned with the piece, facing out. Nothing is drawn for an empty slot, and
 * only the origin cell's entity draws (it holds the inventory).
 */
public class FurnitureDisplayRenderer implements BlockEntityRenderer<FurnitureStorageEntity> {
    /** Items stand four pixels across, the anchors' spacing. */
    private static final float SCALE = 0.25F;

    public FurnitureDisplayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FurnitureStorageEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        BlockState state = entity.getBlockState();
        if (!(state.getBlock() instanceof StorageFurnitureBlock block) || !entity.isOrigin() || !entity.storage().display()
                || !DisplaySpec.authored(block.furniture())) {
            return;
        }
        Direction facing = block.facing(state);
        List<double[]> anchors = DisplaySpec.anchors(block.furniture());
        List<ItemStack> shown = entity.shown();
        for (int i = 0; i < Math.min(anchors.size(), shown.size()); i++) {
            ItemStack stack = shown.get(i);
            if (stack.isEmpty()) {
                continue;
            }
            double[] a = anchors.get(i);
            double[] turned = ShapeRotation.rotate(new double[] {a[0], a[1], a[2], a[0], a[1], a[2]}, ShapeRotation.quarterTurns(facing));
            pose.pushPose();
            pose.translate(turned[0] / 16.0, a[1] / 16.0 + SCALE / 2, turned[2] / 16.0);
            pose.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
            pose.scale(SCALE, SCALE, SCALE);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, pose, buffers,
                    entity.getLevel(), (int) entity.getBlockPos().asLong() + i);
            pose.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(FurnitureStorageEntity entity) {
        return true; // a tall shelf's upper items stand outside the origin block
    }
}
