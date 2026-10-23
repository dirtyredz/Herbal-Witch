package io.github.dirtyredz.herbalwitch.client.wand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import io.github.dirtyredz.herbalwitch.magic.wand.registry.WandItems;
public final class WandClientRegistration {
 public static final KeyMapping SELECT=new KeyMapping("key.herbalwitch.wand.select",org.lwjgl.glfw.GLFW.GLFW_KEY_V,"key.categories.herbalwitch");
 private WandClientRegistration(){}
 public static void setup(){MinecraftForge.EVENT_BUS.addListener(WandClientRegistration::tick);}
 public static void keys(RegisterKeyMappingsEvent event){event.register(SELECT);}
 public static void overlays(RegisterGuiOverlaysEvent event){event.registerAboveAll("wand",WandHud.OVERLAY);}
 private static void tick(TickEvent.ClientTickEvent event){if(event.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();while(SELECT.consumeClick()){if(mc.player==null||mc.screen!=null)continue;var hand=mc.player.getMainHandItem().is(WandItems.WAND.get())?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;var stack=mc.player.getItemInHand(hand);if(stack.is(WandItems.WAND.get()))mc.setScreen(new WandModeScreen(hand,hand==InteractionHand.MAIN_HAND?mc.player.getInventory().selected:40,stack));}}
}
