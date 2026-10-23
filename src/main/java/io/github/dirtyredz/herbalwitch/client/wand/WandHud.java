package io.github.dirtyredz.herbalwitch.client.wand;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseService;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
public final class WandHud {
 private WandHud(){}
 public static final IGuiOverlay OVERLAY=(gui,g,partial,width,height)->{var mc=Minecraft.getInstance();if(mc.options.hideGui||mc.player==null||mc.player.getVehicle() instanceof BroomEntity)return;var mode=WandUseService.mode(mc.player.getMainHandItem());if(mode==null)mode=WandUseService.mode(mc.player.getOffhandItem());if(mode!=null)g.drawCenteredString(mc.font,Component.translatable("hud.herbalwitch.wand",Component.translatable("wand.herbalwitch.mode."+mode.id()),WandClientRegistration.SELECT.getTranslatedKeyMessage()),width/2,height-61,0xF2E8CF);};
}
