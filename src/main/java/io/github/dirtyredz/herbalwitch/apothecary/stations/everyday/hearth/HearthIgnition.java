package io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationBlock;
import net.minecraftforge.common.ForgeHooks;
/** The ticker and wand share actual furnace fuel and returned-container accounting. */
public final class HearthIgnition {
 private HearthIgnition(){}
 public static boolean ready(HearthBlockEntity hearth){var inventory=hearth.inventory();var fuel=inventory.getStackInSlot(HearthBlockEntity.FUEL);return hearth.burn()==0&&ForgeHooks.getBurnTime(fuel,null)>0&&ProcessCompletion.fits(inventory.amount(HearthBlockEntity.RETURN),ProcessInventory.amountOf(fuel.getCraftingRemainingItem()));}
 public static boolean ignite(HearthBlockEntity hearth){
  if(hearth.burn()>0)return false;var inventory=hearth.inventory();var fuel=inventory.getStackInSlot(HearthBlockEntity.FUEL);int time=ForgeHooks.getBurnTime(fuel,null);
  if(time<=0){hearth.ignition(0);return false;}if(!ready(hearth))return false;var container=fuel.getCraftingRemainingItem();inventory.consume(HearthBlockEntity.FUEL,1);inventory.insertInternal(HearthBlockEntity.RETURN,container);hearth.ignition(time);return true;
 }
 public static void synchronize(HearthBlockEntity hearth){var state=hearth.getBlockState();if(state.getBlock() instanceof StationBlock block)block.setOnStation(hearth.getLevel(),hearth.getBlockPos(),HearthBlock.LIT,hearth.burn()>0);}
}
