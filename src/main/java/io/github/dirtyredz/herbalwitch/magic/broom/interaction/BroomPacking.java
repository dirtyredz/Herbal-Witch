package io.github.dirtyredz.herbalwitch.magic.broom.interaction;
import net.minecraft.server.level.ServerPlayer;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.flight.BroomFlightState;
/** Transfer happens synchronously; inventory insertion must succeed before retiring the entity. */
public final class BroomPacking {
 private BroomPacking(){}
 public static boolean pack(ServerPlayer actor,BroomEntity broom){
  if(!BroomAccess.owner(actor,broom)||broom.isVehicle()||broom.flight()!=BroomFlightState.GROUNDED||!broom.onGround()||actor.getInventory().getFreeSlot()<0)return false;
  var stack=broom.packed();
  if(!actor.getInventory().add(stack))return false;
  broom.discard();actor.containerMenu.broadcastChanges();return true;
 }
}
