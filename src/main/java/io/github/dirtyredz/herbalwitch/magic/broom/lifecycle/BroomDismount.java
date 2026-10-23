package io.github.dirtyredz.herbalwitch.magic.broom.lifecycle;
import java.util.HashMap;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
/** Bounded ordinary-fall grace following real airborne flight; never general damage immunity. */
public final class BroomDismount {
 private record Grace(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,long until){}
 private static final java.util.Map<UUID,Grace> GRACE=new HashMap<>();
 private BroomDismount(){}
 public static void protect(ServerPlayer actor){int ticks=WitchRules.server().broom().fallProtection();if(ticks>0)GRACE.put(actor.getUUID(),new Grace(actor.level().dimension(),actor.level().getGameTime()+ticks));}
 public static void clear(ServerPlayer actor){GRACE.remove(actor.getUUID());}
 public static void clear(){GRACE.clear();}
 public static void fall(LivingFallEvent event){
  if(!(event.getEntity() instanceof ServerPlayer actor))return;
  Grace grace=GRACE.remove(actor.getUUID());
  if(grace!=null && grace.dimension.equals(actor.level().dimension()) && actor.level().getGameTime()<=grace.until)event.setCanceled(true);
 }
 public static Vec3 location(BroomEntity broom,LivingEntity rider){
  for(int[] offset:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){
   Vec3 at=broom.position().add(offset[0],.1,offset[1]);BlockPos pos=BlockPos.containing(at);
   if(broom.level().hasChunkAt(pos)&&broom.level().getWorldBorder().isWithinBounds(pos)&&broom.level().noCollision(rider,rider.getDimensions(rider.getPose()).makeBoundingBox(at))&&!broom.level().getBlockState(pos.below()).getCollisionShape(broom.level(),pos.below()).isEmpty())return at;
  }
  return broom.position().add(0,.4,0);
 }
}
