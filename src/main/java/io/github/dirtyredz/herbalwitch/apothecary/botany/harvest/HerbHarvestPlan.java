package io.github.dirtyredz.herbalwitch.apothecary.botany.harvest;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbHarvest;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditionEvaluator;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
/** One prepared yield shared by hand harvest and the wand; no world mutation during preparation. */
public record HerbHarvestPlan(BlockPos pos,BlockState before,BlockState after,BlockState soil,BlockState nextSoil,List<ItemStack> drops){
 public HerbHarvestPlan{pos=pos.immutable();drops=drops.stream().map(ItemStack::copy).toList();}
 public static Optional<HerbHarvestPlan> prepare(ServerLevel level,BlockPos pos){
  if(!level.hasChunkAt(pos))return Optional.empty();var state=level.getBlockState(pos);
  if(!(state.getBlock() instanceof HerbCropBlock crop)||!crop.isMaxAge(state))return Optional.empty();
  var soil=level.getBlockState(pos.below());boolean enriched=soil.getBlock() instanceof HerbBedBlock&&soil.getValue(HerbBedBlock.ENRICHED);
  int fresh=HerbHarvest.BASE_FRESH+HerbConditionEvaluator.harvestBonus(level,pos,crop.herb(),level.random)+(enriched?1:0);
  return Optional.of(new HerbHarvestPlan(pos,state,crop.getStateForAge(crop.harvestResetAge()),soil,enriched?soil.setValue(HerbBedBlock.ENRICHED,false):soil,List.of(new ItemStack(BotanyItems.fresh(crop.herb()),fresh),new ItemStack(BotanyItems.seeds(crop.herb()),HerbHarvest.BASE_SEEDS))));
 }
}
