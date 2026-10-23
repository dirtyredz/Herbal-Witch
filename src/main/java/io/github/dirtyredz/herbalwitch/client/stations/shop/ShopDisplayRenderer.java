package io.github.dirtyredz.herbalwitch.client.stations.shop;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.List;

/**
 * Draws a display's public projection at the anchors authored in its Blockbench draft: three spots on
 * the shelf plank, one inside the jar. A sold-out sample is drawn unlit. It is a picture: nothing here
 * is an item, a slot or inventory.
 */
public final class ShopDisplayRenderer implements BlockEntityRenderer<ShopDisplayBlockEntity> {
    private static final List<double[]> SHELF = List.of(new double[] {3, 7.5, 12.25}, new double[] {8, 7.5, 12.25}, new double[] {13, 7.5, 12.25});
    private static final List<double[]> JAR = List.of(new double[] {8, 1.4, 8});
    private static final float SCALE = 0.3F;

    public ShopDisplayRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ShopDisplayBlockEntity entity, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        if (!(entity.getBlockState().getBlock() instanceof ShopDisplayBlock block)) return;
        Direction facing = entity.getBlockState().getValue(ShopFixtureBlock.FACING);
        List<double[]> anchors = block.spec() == ShopPieceCatalog.STOCK_SHELF ? SHELF : JAR;
        var samples = entity.samples();
        for (int i = 0; i < Math.min(anchors.size(), samples.size()); i++) {
            double[] a = anchors.get(i);
            double[] turned = ShapeRotation.rotate(new double[] {a[0], a[1], a[2], a[0], a[1], a[2]}, ShapeRotation.quarterTurns(facing));
            pose.pushPose();
            pose.translate(turned[0] / 16.0, a[1] / 16.0 + SCALE / 2, turned[2] / 16.0);
            pose.mulPose(Axis.YP.rotationDegrees(180 - facing.toYRot()));
            pose.scale(SCALE, SCALE, SCALE);
            int shown = samples.get(i).available() ? light : LightTexture.pack(1, 1);
            Minecraft.getInstance().getItemRenderer().renderStatic(samples.get(i).stack(), ItemDisplayContext.FIXED, shown, overlay, pose, buffers,
                    entity.getLevel(), (int) entity.getBlockPos().asLong() + i);
            pose.popPose();
        }
    }
}
