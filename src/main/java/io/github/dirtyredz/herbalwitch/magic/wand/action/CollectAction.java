package io.github.dirtyredz.herbalwitch.magic.wand.action;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.util.AbortableIterationConsumer.Continuation;
import io.github.dirtyredz.herbalwitch.magic.wand.use.*;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.ItemPickupPermission;
/** Bounded enumeration, partial inventory acceptance and deferred pickup notifications. */
public final class CollectAction {
 private record Pickup(ItemEntity entity,ItemStack before,ItemStack remaining,int taken){}
 private CollectAction(){}
 public static WandCommit prepare(ServerPlayer actor,Vec3 center){
  var rules=WitchRules.server().wand();double radius=rules.radius();var candidates=new ArrayList<ItemEntity>();int[] examined={0};
  actor.serverLevel().getEntities().get(EntityTypeTest.forClass(ItemEntity.class),new AABB(center,center).inflate(radius),item->{
   examined[0]++;if(item.position().distanceToSqr(center)<=radius*radius&&eligible(actor,item))candidates.add(item);
   return examined[0]>=rules.scanned()?Continuation.ABORT:Continuation.CONTINUE;
  });
  var before=actor.getInventory().items.stream().map(ItemStack::copy).toList();var after=new ArrayList<>(before.stream().map(ItemStack::copy).toList());var pickups=new ArrayList<Pickup>();
  for(var entity:candidates){if(pickups.size()>=rules.stacks())break;var stack=entity.getItem().copy();
   if(!ItemPickupPermission.authorize(actor,entity)||!eligible(actor,entity)||!ItemStack.matches(stack,entity.getItem()))continue;
   var rest=stack.copy();insert(after,rest);int taken=stack.getCount()-rest.getCount();if(taken>0)pickups.add(new Pickup(entity,stack,rest,taken));
  }
  if(pickups.isEmpty())return null;
  return new WandCommit(){boolean changed;
   public boolean prepare(){for(int i=0;i<before.size();i++)if(!ItemStack.matches(actor.getInventory().items.get(i),before.get(i)))return false;for(var p:pickups)if(!eligible(actor,p.entity)||!ItemStack.matches(p.entity.getItem(),p.before))return false;return true;}
   public boolean apply(){if(!prepare())return false;changed=true;for(int i=0;i<after.size();i++)actor.getInventory().items.set(i,after.get(i).copy());for(var p:pickups)p.entity.setItem(p.remaining.copy());actor.getInventory().setChanged();return true;}
   public void rollback(){if(!changed)return;for(int i=0;i<before.size();i++)actor.getInventory().items.set(i,before.get(i).copy());for(var p:pickups)p.entity.setItem(p.before.copy());actor.getInventory().setChanged();}
   public void finish(){for(var p:pickups){var taken=p.before.copyWithCount(p.taken);net.minecraftforge.event.ForgeEventFactory.firePlayerItemPickupEvent(actor,p.entity,taken);actor.take(p.entity,p.taken);actor.awardStat(net.minecraft.stats.Stats.ITEM_PICKED_UP.get(taken.getItem()),p.taken);if(p.remaining.isEmpty())p.entity.discard();}actor.containerMenu.broadcastChanges();}
  };
 }
 private static boolean eligible(ServerPlayer actor,ItemEntity item){return ItemPickupPermission.allowed(actor,item)&&!item.getItem().isEmpty()&&WandTargeting.visible(actor,item.getBoundingBox().getCenter());}
 /** Same vanilla stack limits; unlike creative Inventory.add, never erases an unaccepted remainder. */
 private static void insert(List<ItemStack> slots,ItemStack remainder){
  for(int pass=0;pass<2&&!remainder.isEmpty();pass++)for(int i=0;i<slots.size()&&!remainder.isEmpty();i++){
   var current=slots.get(i);if(pass==0&&(current.isEmpty()||!ItemStack.isSameItemSameTags(current,remainder))||pass==1&&!current.isEmpty())continue;
   int count=Math.min(remainder.getCount(),Math.min(64,remainder.getMaxStackSize())-current.getCount());if(count<=0)continue;
   slots.set(i,remainder.copyWithCount(current.getCount()+count));remainder.shrink(count);
  }
 }
}
