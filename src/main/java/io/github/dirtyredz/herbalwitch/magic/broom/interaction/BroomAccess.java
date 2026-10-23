package io.github.dirtyredz.herbalwitch.magic.broom.interaction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.ProtectionAdapters;
import java.util.concurrent.CopyOnWriteArrayList;
/** Travel is not block building. Unsupported recognized claims fail closed for custom movement. */
public final class BroomAccess {
 public enum Action{DEPLOY,MOUNT,PACK,TRAVEL}
 @FunctionalInterface public interface Policy{boolean allows(Action action,ServerPlayer actor,ServerLevel level,BlockPos at);}
 private static final java.util.List<Policy> POLICIES=new CopyOnWriteArrayList<>();
 private BroomAccess(){}
 public static void register(Policy policy){POLICIES.add(policy);}
 public static boolean allowed(Action action,ServerPlayer actor,ServerLevel level,BlockPos at){
  if(actor==null||!actor.isAlive()||actor.isSpectator()||actor.level()!=level||!level.hasChunkAt(at)||level.isOutsideBuildHeight(at)||!level.getWorldBorder().isWithinBounds(at)||ProtectionAdapters.restricted())return false;
  if(action!=Action.TRAVEL&&!level.mayInteract(actor,at))return false;
  for(var policy:POLICIES)if(!policy.allows(action,actor,level,at))return false;
  return true;
 }
 public static boolean owner(ServerPlayer actor,BroomEntity broom){return !broom.isRemoved()&&broom.writable()&&actor.getUUID().equals(broom.owner())&&actor.distanceToSqr(broom)<=16&&allowed(Action.PACK,actor,(ServerLevel)broom.level(),broom.blockPosition());}
 public static boolean mount(ServerPlayer actor,BroomEntity broom){return io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules.server().broom().permits(broom.level().dimension().location())&&owner(actor,broom)&&!broom.isVehicle()&&!actor.isPassenger()&&WitchStatusService.isWitch(actor)&&broom.remaining()>0&&allowed(Action.MOUNT,actor,(ServerLevel)broom.level(),broom.blockPosition());}
}
