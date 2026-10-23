package io.github.dirtyredz.herbalwitch.client.broom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.event.TickEvent;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.network.*;
import io.github.dirtyredz.herbalwitch.platform.network.Messages;
import org.lwjgl.glfw.GLFW;
/** No predicted movement: send remappable bounded intent while the authenticated mount is current. */
public final class BroomControls {
 public static final KeyMapping DESCEND=new KeyMapping("key.herbalwitch.broom.descend",GLFW.GLFW_KEY_LEFT_CONTROL,"key.categories.herbalwitch");
 public static final KeyMapping LAND=new KeyMapping("key.herbalwitch.broom.land",GLFW.GLFW_KEY_R,"key.categories.herbalwitch");
 private static BroomPackets.Lease lease;
 private static Object world;
 private static long sequence;
 private static int waiting;
 private BroomControls(){}
 public static void accept(BroomPackets.Lease value){lease=value;sequence=0;waiting=20;world=Minecraft.getInstance().level;}
 public static void clear(){lease=null;world=null;sequence=0;waiting=0;}
 public static void tick(TickEvent.ClientTickEvent event){
  if(event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
  if(mc.player==null||mc.level==null||world!=mc.level){clear();return;}
  if(lease==null)return;
  if(!(mc.player.getVehicle() instanceof BroomEntity b)||b.getId()!=lease.entity()){if(--waiting<=0)clear();return;}waiting=0;
  int flags=0;
  if(mc.screen==null&&mc.isWindowActive()){
   if(mc.options.keyUp.isDown())flags|=1;if(mc.options.keyDown.isDown())flags|=2;if(mc.options.keyLeft.isDown())flags|=4;if(mc.options.keyRight.isDown())flags|=8;
   if(mc.options.keyJump.isDown())flags|=16;if(DESCEND.isDown())flags|=32;if(LAND.isDown())flags|=64;
  }
  Messages.sendToServer(new BroomInput(lease.nonce(),sequence++,flags));
 }
}
