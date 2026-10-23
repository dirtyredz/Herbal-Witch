package io.github.dirtyredz.herbalwitch.magic.broom.lifecycle;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
public final class BroomLifecycle {
 private BroomLifecycle(){}
 private static void end(Entity actor){if(actor.getVehicle() instanceof BroomEntity broom){actor.stopRiding();broom.endFlight();}if(actor instanceof ServerPlayer player)BroomDismount.clear(player);}
 public static void logout(PlayerEvent.PlayerLoggedOutEvent event){end(event.getEntity());}
 public static void death(LivingDeathEvent event){end(event.getEntity());}
 public static void dimension(EntityTravelToDimensionEvent event){if(event.getEntity() instanceof BroomEntity){event.setCanceled(true);return;}end(event.getEntity());}
 public static void stopped(ServerStoppedEvent event){BroomDismount.clear();}
}
