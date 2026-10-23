package io.github.dirtyredz.herbalwitch.magic.wand.action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import io.github.dirtyredz.herbalwitch.apothecary.botany.harvest.*;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.use.*;
/** One mature crop, one loot roll, and a seed reserved from that roll before replanting. */
public final class HarvestAction {
 private HarvestAction(){}
 public static WandCommit prepare(ServerPlayer actor,BlockPos pos){
  var level=actor.serverLevel();var state=level.getBlockState(pos);
  if(!WandPermission.allowed(WandMode.HARVEST,actor,pos))return null;
  if(state.getBlock() instanceof HerbCropBlock){var plan=HerbHarvestPlan.prepare(level,pos).orElse(null);if(plan==null)return null;var change=new HerbHarvestCommit(level,actor,plan);
   return new WandCommit(){public boolean prepare(){return WandPermission.allowed(WandMode.HARVEST,actor,pos)&&(plan.soil()==plan.nextSoil()||WandPermission.allowed(WandMode.HARVEST,actor,pos.below()))&&change.ready();}public boolean apply(){return change.apply();}public void rollback(){change.rollback();}public void finish(){change.emit();}};
  }
  Item seed=state.is(Blocks.WHEAT)?Items.WHEAT_SEEDS:state.is(Blocks.CARROTS)?Items.CARROT:state.is(Blocks.POTATOES)?Items.POTATO:state.is(Blocks.BEETROOTS)?Items.BEETROOT_SEEDS:null;
  if(seed==null||!(state.getBlock() instanceof CropBlock crop)||!crop.isMaxAge(state))return null;
  var loot=Block.getDrops(state,level,pos,null,actor,ItemStack.EMPTY).stream().map(ItemStack::copy).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));boolean funded=false;
  for(var stack:loot)if(stack.is(seed)&&!stack.isEmpty()){stack.shrink(1);funded=true;break;}if(!funded)return null;
  return new WandCommit(){boolean changed;
   public boolean prepare(){return level.getBlockState(pos)==state&&WandPermission.allowed(WandMode.HARVEST,actor,pos);}
   public boolean apply(){if(!prepare())return false;changed=level.setBlock(pos,crop.getStateForAge(0),Block.UPDATE_CLIENTS);return changed;}
   public void rollback(){if(changed)level.setBlock(pos,state,Block.UPDATE_CLIENTS);}
   public void finish(){for(var stack:loot)if(!stack.isEmpty())Block.popResource(level,pos,stack);level.gameEvent(actor,net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE,pos);}
  };
 }
}
