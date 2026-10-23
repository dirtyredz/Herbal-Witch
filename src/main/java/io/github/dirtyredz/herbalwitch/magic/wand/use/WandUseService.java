package io.github.dirtyredz.herbalwitch.magic.wand.use;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import io.github.dirtyredz.herbalwitch.magic.broom.entity.BroomEntity;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.persistence.WandItemData;
import io.github.dirtyredz.herbalwitch.magic.wand.registry.WandItems;
import io.github.dirtyredz.herbalwitch.magic.wand.action.*;
import io.github.dirtyredz.herbalwitch.magic.witch.status.*;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
/** One actual held-item path, with actor-wide attempt/cast limits and synchronous energy participation. */
public final class WandUseService {
 private static final WandUseLimits LIMITS=new WandUseLimits();
 private WandUseService(){}
 public static boolean focus(ServerPlayer actor){return WitchRules.server().wand().enabled()&&WitchStatusService.isWitch(actor)&&!(actor.getVehicle() instanceof BroomEntity)&&java.util.Arrays.stream(InteractionHand.values()).anyMatch(h->mode(actor.getItemInHand(h))==WandMode.FAMILIAR);}
 public static WandMode mode(ItemStack stack){return stack.is(WandItems.WAND.get())?WandItemData.read(stack).orElse(null):null;}
 public static String use(ServerPlayer actor,InteractionHand hand){return use(actor,hand,null);}
 public static String use(ServerPlayer actor,InteractionHand hand,Mob entity){
  long tick=actor.server.getTickCount();if(!LIMITS.attempt(actor.getUUID(),tick))return "rate";
  if(!LIMITS.begin(actor.getUUID()))return "busy";
  try{
   var stack=actor.getItemInHand(hand);var selected=mode(stack);var rules=WitchRules.server();
   if(selected==null)return "invalid";if(!rules.wand().enabled())return "disabled";
   if(!actor.isAlive()||actor.isSpectator()||!WitchStatusService.isWitch(actor))return "witch";
   if(actor.getVehicle() instanceof BroomEntity)return "riding";
   if(entity!=null)return selected==WandMode.FAMILIAR&&actor.canReach(entity,0)&&actor.hasLineOfSight(entity)&&FamiliarAction.detail(actor,entity)?"success":"target";
   var hit=WandTargeting.ray(actor);
   if(selected==WandMode.FAMILIAR)return FamiliarAction.roster(actor)?"success":"target";
   if(hit==null)return "target";
   if(selected==WandMode.FOCUS)return hit.getType()==HitResult.Type.BLOCK&&FocusAction.open(actor,hand,hit)?"success":"target";
   if(!LIMITS.ready(actor.getUUID(),tick))return "cooldown";
   int cost=rules.wand().costs().get(selected.ordinal());
   if(WitchStatusService.check(actor,cost)!=EnergyService.Result.OK)return "energy";
   if(selected!=WandMode.COLLECT&&hit.getType()!=HitResult.Type.BLOCK)return "target";
   ItemStack observed=stack.copy();int fingerprint=rules.fingerprint();
   WandCommit change=switch(selected){case HARVEST->HarvestAction.prepare(actor,hit.getBlockPos());case PLANT->PlantAction.prepare(actor,hit);case WATER->WaterAction.prepare(actor,hit.getBlockPos());case COLLECT->CollectAction.prepare(actor,hit.getLocation());case LIGHT->LightAction.prepare(actor,hit.getBlockPos());default->null;};
   if(change==null)return "target";
   boolean success=WitchStatusService.spend(actor,cost,new WitchStatusService.Change(){
    private boolean unchanged(){return ItemStack.matches(observed,actor.getItemInHand(hand))&&WitchRules.server().fingerprint()==fingerprint&&!(actor.getVehicle() instanceof BroomEntity);}
    public boolean prepare(){return unchanged()&&change.prepare();}
    public boolean apply(){return unchanged()&&change.apply();}
    public void rollback(){change.rollback();}
   });
   if(!success)return "changed";LIMITS.committed(actor.getUUID(),tick,rules.wand().cooldown());change.finish();return "success";
  }finally{LIMITS.end(actor.getUUID());}
 }
 public static void message(ServerPlayer actor,String result){actor.displayClientMessage(Component.translatable("wand.herbalwitch.result."+result),true);}
 public static void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event){LIMITS.forget(event.getEntity().getUUID());}
 public static void stopped(net.minecraftforge.event.server.ServerStoppedEvent event){LIMITS.clear();}
}
