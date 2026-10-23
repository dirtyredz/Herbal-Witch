package io.github.dirtyredz.herbalwitch.magic.broom.flight;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.broom.interaction.BroomAccess;
import io.github.dirtyredz.herbalwitch.magic.broom.network.BroomControlSession;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
/** Only server ticks move brooms. Every swept volume is loaded before collision queries. */
public final class BroomFlightController {
 private BroomFlightController(){}
 public static void tick(BroomEntity broom){
  var level=(ServerLevel)broom.level();var rules=WitchRules.server().broom();var rider=broom.rider();var session=broom.controls();
  int flags=session==null?0:session.flags(level.getGameTime());
  boolean permitted=rider!=null&&rider.isAlive()&&WitchStatusService.isWitch(rider)&&broom.remaining()>0&&rules.permits(level.dimension().location())&&session!=null&&!session.expired(level.getGameTime())&&BroomAccess.allowed(BroomAccess.Action.TRAVEL,rider,level,broom.blockPosition());
  boolean ascend=(flags&BroomControlSession.UP)!=0;
  if((flags&BroomControlSession.LAND)!=0)permitted=false;
  BroomFlightState state=broom.flight().next(permitted,ascend,broom.onGround());
  if(state==BroomFlightState.GROUNDED&&!broom.onGround()&&!supported(broom))state=BroomFlightState.LANDING;
  broom.flight(state);
  if(state==BroomFlightState.FLYING){
   var previous=broom.condition();var cost=BroomFlightCost.next(rules.energyPerSecond(),previous.energyRemainder());
   var next=BroomFlightState.worn(previous,rules.wearInterval(),cost.remainder());
   boolean paid=WitchStatusService.spend(rider,cost.units(),new WitchStatusService.Change(){
    public boolean apply(){broom.condition(next);return true;}
    public void rollback(){broom.condition(previous);}
   });
   if(!paid){broom.flight(BroomFlightState.LANDING);state=BroomFlightState.LANDING;}
  }
  Vec3 velocity;
  if(state==BroomFlightState.FLYING){
   float heading=(float)BroomMotion.turn(broom.getYRot(),rider.getYRot(),rules.turnRate());broom.setYRot(heading);
   int forward=((flags&1)!=0?1:0)-((flags&2)!=0?1:0),side=((flags&8)!=0?1:0)-((flags&4)!=0?1:0);
   var target=BroomMotion.target(forward,side,heading,rules.speed());var current=broom.getDeltaMovement();
   var horizontal=BroomMotion.accelerate(new BroomMotion.Horizontal(current.x,current.z),target,rules.acceleration());
   double vertical=(((flags&16)!=0?1:0)-((flags&32)!=0?1:0))*rules.verticalSpeed()/20;
   double y=current.y+Math.max(-rules.acceleration()/400,Math.min(rules.acceleration()/400,vertical-current.y));
   velocity=new Vec3(horizontal.x(),y,horizontal.z());
  }else velocity=new Vec3(0,state==BroomFlightState.LANDING?-rules.landingSpeed()/20:-.02,0);
  Vec3 allowed=bounded(broom,velocity);
  broom.setDeltaMovement(allowed);broom.move(MoverType.SELF,allowed);
  if(state==BroomFlightState.FLYING&&!broom.onGround()){broom.flew();broom.fallDistance=0;if(rider!=null)rider.fallDistance=0;}
  if(broom.onGround()&&state==BroomFlightState.LANDING){broom.flight(BroomFlightState.GROUNDED);broom.setDeltaMovement(Vec3.ZERO);}
 }
 private static boolean supported(BroomEntity broom){var box=broom.getBoundingBox().move(0,-.03,0);return loaded(broom,box)&&!broom.level().noCollision(broom,box);}
 public static boolean loaded(BroomEntity broom,AABB box){
  if(box.minY<broom.level().getMinBuildHeight()||box.maxY>broom.level().getMaxBuildHeight()||!broom.level().getWorldBorder().isWithinBounds(box))return false;
  for(int x=net.minecraft.util.Mth.floor(box.minX)>>4;x<=net.minecraft.util.Mth.floor(box.maxX)>>4;x++)for(int z=net.minecraft.util.Mth.floor(box.minZ)>>4;z<=net.minecraft.util.Mth.floor(box.maxZ)>>4;z++)if(!broom.level().hasChunk(x,z))return false;
  return true;
 }
 private static Vec3 bounded(BroomEntity broom,Vec3 movement){
  AABB body=broom.getBoundingBox();AABB swept=body.expandTowards(movement).inflate(.001);
  if(!loaded(broom,swept))return Vec3.ZERO;
  var rider=broom.rider();
  if(rider!=null){
   AABB passenger=new AABB(body.minX,broom.getY()+.2,body.minZ,body.maxX,broom.getY()+.2+rider.getBbHeight(),body.maxZ).expandTowards(movement);
   if(!loaded(broom,passenger)||!broom.level().noCollision(broom,passenger))return new Vec3(0,Math.min(0,movement.y),0);
   if(!BroomAccess.allowed(BroomAccess.Action.TRAVEL,rider,(ServerLevel)broom.level(),BlockPos.containing(broom.position().add(movement)))){broom.flight(BroomFlightState.LANDING);return Vec3.ZERO;}
  }
  return movement;
 }
}
