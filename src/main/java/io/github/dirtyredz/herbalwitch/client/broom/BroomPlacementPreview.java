package io.github.dirtyredz.herbalwitch.client.broom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderHighlightEvent;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
/** Advisory placement volume; the server independently checks owner, permissions and insertion. */
public final class BroomPlacementPreview {
 private BroomPlacementPreview(){}
 public static void render(RenderHighlightEvent.Block event){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.player.isPassenger()||!mc.player.getMainHandItem().is(BroomItems.BROOM.get()))return;
  var hit=event.getTarget();var at=hit.getBlockPos().relative(hit.getDirection());var box=new AABB(at.getX()+.1,at.getY(),at.getZ()+.1,at.getX()+.9,at.getY()+.4,at.getZ()+.9);
  boolean clear=mc.level.hasChunkAt(at)&&mc.level.getWorldBorder().isWithinBounds(box)&&mc.level.noCollision(mc.player,box);
  var camera=event.getCamera().getPosition();var pose=event.getPoseStack();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);
  LevelRenderer.renderLineBox(pose,event.getMultiBufferSource().getBuffer(RenderType.lines()),box,clear?.8F:1F,clear?.7F:.2F,.2F,.8F);pose.popPose();
 }
}
