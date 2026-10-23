package io.github.dirtyredz.herbalwitch.client.broom;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
public final class BroomHud {
 private BroomHud(){}
 public static final IGuiOverlay OVERLAY=(gui,g,partial,width,height)->{
  var mc=Minecraft.getInstance();if(mc.options.hideGui||mc.player==null||!(mc.player.getVehicle() instanceof BroomEntity broom))return;
  g.drawCenteredString(mc.font,Component.translatable("hud.herbalwitch.broom.condition",broom.remaining(),Component.translatable("broom.herbalwitch.state."+broom.flight().name().toLowerCase(java.util.Locale.ROOT))),width/2,height-72,0xF2E8CF);
  g.drawCenteredString(mc.font,Component.translatable("hud.herbalwitch.broom.controls",mc.options.keyJump.getTranslatedKeyMessage(),BroomControls.DESCEND.getTranslatedKeyMessage(),BroomControls.LAND.getTranslatedKeyMessage(),mc.options.keyShift.getTranslatedKeyMessage()),width/2,height-61,0xF2E8CF);
 };
}
