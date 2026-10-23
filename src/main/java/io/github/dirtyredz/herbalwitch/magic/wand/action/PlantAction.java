package io.github.dirtyredz.herbalwitch.magic.wand.action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Block;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbSeedItem;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.use.*;
/** Native seed placement keeps its survival, event and inventory rules. */
public final class PlantAction {
 private PlantAction(){}
 public static WandCommit prepare(ServerPlayer actor,BlockHitResult hit){
  var seeds=actor.getOffhandItem();if(!(seeds.getItem() instanceof HerbSeedItem||seeds.is(Items.WHEAT_SEEDS)||seeds.is(Items.CARROT)||seeds.is(Items.POTATO)||seeds.is(Items.BEETROOT_SEEDS)))return null;
  var use=new UseOnContext(actor,InteractionHand.OFF_HAND,hit);var context=new BlockPlaceContext(use);var pos=context.getClickedPos();var level=actor.serverLevel();if(!level.hasChunkAt(pos)||!level.getBlockState(pos).isAir()||!WandPermission.allowed(WandMode.PLANT,actor,pos))return null;
  var original=seeds.copy();var before=level.getBlockState(pos);var crop=((BlockItem)seeds.getItem()).getBlock();
  return new WandCommit(){boolean attempted;
   public boolean prepare(){return ItemStack.matches(actor.getOffhandItem(),original)&&level.getBlockState(pos)==before&&WandPermission.allowed(WandMode.PLANT,actor,pos);}
   public boolean apply(){if(!prepare())return false;attempted=true;var result=actor.getOffhandItem().useOn(use);return result.consumesAction()&&level.getBlockState(pos).is(crop);}
   public void rollback(){if(!attempted)return;level.setBlock(pos,before,Block.UPDATE_CLIENTS);actor.setItemInHand(InteractionHand.OFF_HAND,original.copy());}
  };
 }
}
