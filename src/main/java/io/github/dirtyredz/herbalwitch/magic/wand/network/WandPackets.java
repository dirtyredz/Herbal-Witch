package io.github.dirtyredz.herbalwitch.magic.wand.network;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import io.github.dirtyredz.herbalwitch.platform.network.*;
import io.github.dirtyredz.herbalwitch.magic.wand.persistence.WandItemData;
import io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseService;
public final class WandPackets {
 private static RateLimit selections=new RateLimit(10,20);
 private WandPackets(){}
 public static void register(){Messages.toServer(WandModeRequest.class,WandModeRequest::encode,WandModeRequest::decode,WandPackets::handle);}
 public static void handle(ServerPlayer actor,WandModeRequest request){
  if(!actor.isAlive()||actor.isSpectator()||!selections.allow(actor.getUUID(),actor.server.getTickCount()))return;
  int slot=request.hand()==InteractionHand.MAIN_HAND?actor.getInventory().selected:40;var held=actor.getItemInHand(request.hand());
  if(slot!=request.slot()||WandUseService.mode(held)==null||!ItemStack.matches(held,request.observed()))return;
  WandItemData.write(held,request.mode());actor.getInventory().setChanged();actor.inventoryMenu.broadcastChanges();actor.containerMenu.broadcastChanges();
 }
 public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){selections.forget(e.getEntity().getUUID());}
 public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent e){selections=new RateLimit(10,20);}
}
