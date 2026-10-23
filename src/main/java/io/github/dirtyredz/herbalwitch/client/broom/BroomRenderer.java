package io.github.dirtyredz.herbalwitch.client.broom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
/** The reload-aware item renderer resolves the one Blockbench model on every render. */
public final class BroomRenderer extends EntityRenderer<BroomEntity> {
 public BroomRenderer(EntityRendererProvider.Context context){super(context);shadowRadius=.4F;}
 @Override public void render(BroomEntity broom,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){
  pose.pushPose();pose.translate(0,io.github.dirtyredz.herbalwitch.magic.broom.catalog.BroomSpec.MODEL_HEIGHT,0);pose.mulPose(Axis.YP.rotationDegrees(180-yaw));
  Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(BroomItems.BROOM.get()),ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,pose,buffers,broom.level(),broom.getId());
  pose.popPose();super.render(broom,yaw,partial,pose,buffers,light);
 }
 @Override public ResourceLocation getTextureLocation(BroomEntity broom){return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;}
}
