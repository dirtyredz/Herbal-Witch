package io.github.dirtyredz.herbalwitch.magic.wand;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraftforge.gametest.*;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.*;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.use.WandPermission;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.magic.wand.fixture.WandFixtures.*;
@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class WandGardenGameTests {
 @GameTest(template=TEMPLATE,batch="herbalwitch_wand_garden") public static void everyHerbSharesTheHandHarvestResetAndConsumesEnrichmentOnce(GameTestHelper h){
  floor(h);int i=0;for(var herb:HerbCatalog.ALL){h.setBlock(TARGET.below(),BotanyBlocks.HERB_BED.get().defaultBlockState().setValue(HerbBedBlock.ENRICHED,true));var crop=(HerbCropBlock)BotanyBlocks.plant(herb);h.setBlock(TARGET,crop.getStateForAge(crop.getMaxAge()));var actor=witch(h,"wand_herb_"+i++,FEET);int energy=WitchStatusService.energyUnits(actor);cast(h,actor,WandMode.HARVEST,InteractionHand.MAIN_HAND,TARGET);check(h.getBlockState(TARGET).equals(crop.getStateForAge(crop.harvestResetAge())),"herb reset "+herb);check(!h.getBlockState(TARGET.below()).getValue(HerbBedBlock.ENRICHED),"enrichment retained");check(WitchStatusService.energyUnits(actor)==energy-100,"herb charge");clearDrops(h);}
  h.succeed();
 }
 @GameTest(template=TEMPLATE,batch="herbalwitch_wand_garden") public static void vanillaHarvestFundsReplantingFromOneLootRoll(GameTestHelper h){
  floor(h);int i=0;for(Block block:java.util.List.of(Blocks.WHEAT,Blocks.CARROTS,Blocks.POTATOES,Blocks.BEETROOTS)){h.setBlock(TARGET.below(),Blocks.FARMLAND);var crop=(CropBlock)block;h.setBlock(TARGET,crop.getStateForAge(crop.getMaxAge()));var actor=witch(h,"wand_crop_"+i++,FEET);int before=WitchStatusService.energyUnits(actor);cast(h,actor,WandMode.HARVEST,InteractionHand.MAIN_HAND,TARGET);check(h.getBlockState(TARGET).equals(crop.getStateForAge(0)),"crop not replanted");check(WitchStatusService.energyUnits(actor)==before-100,"crop debit");clearDrops(h);}h.succeed();
 }
 @GameTest(template=TEMPLATE,batch="herbalwitch_wand_garden") public static void wateringPreservesBedStateAndSharesCooldownAcrossHandsAndCopies(GameTestHelper h){
  floor(h);h.setBlock(TARGET,BotanyBlocks.HERB_BED.get().defaultBlockState().setValue(HerbBedBlock.ENRICHED,true));var before=h.getBlockState(TARGET);var actor=witch(h,"wand_water",FEET);int energy=WitchStatusService.energyUnits(actor);cast(h,actor,WandMode.WATER,InteractionHand.OFF_HAND,TARGET);check(h.getBlockState(TARGET).equals(before.setValue(HerbBedBlock.MOISTURE,7)),"water changed other properties");check(WitchStatusService.energyUnits(actor)==energy-100,"water debit");h.setBlock(TARGET,before);cast(h,actor,WandMode.WATER,InteractionHand.MAIN_HAND,TARGET);check(h.getBlockState(TARGET)==before&&WitchStatusService.energyUnits(actor)==energy-100,"alternate wand bypassed cooldown");h.succeed();
 }
 @GameTest(template=TEMPLATE,batch="herbalwitch_wand_garden") public static void realOffhandPlacementConsumesOneSeedAndDenialsConsumeNothing(GameTestHelper h){
  floor(h);openSky(h,TARGET);h.setBlock(TARGET.below(),Blocks.FARMLAND);var actor=witch(h,"wand_plant",FEET);actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.WHEAT_SEEDS,2));int energy=WitchStatusService.energyUnits(actor);cast(h,actor,WandMode.PLANT,InteractionHand.MAIN_HAND,TARGET.below());check(h.getBlockState(TARGET).is(Blocks.WHEAT)&&actor.getOffhandItem().getCount()==1&&WitchStatusService.energyUnits(actor)==energy-100,"native seed placement failed: state="+h.getBlockState(TARGET)+", seed="+actor.getOffhandItem()+", energy="+WitchStatusService.energyUnits(actor)+", ray="+diagnose(actor));
  var denied=witch(h,"wand_denied",FEET);var active=new java.util.concurrent.atomic.AtomicBoolean(true);WandPermission.register((mode,p,pos)->!active.get()||p!=denied);h.setBlock(TARGET,Blocks.AIR);denied.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.WHEAT_SEEDS,2));int reserve=WitchStatusService.energyUnits(denied);try{cast(h,denied,WandMode.PLANT,InteractionHand.MAIN_HAND,TARGET.below());check(h.getBlockState(TARGET).isAir()&&denied.getOffhandItem().getCount()==2&&WitchStatusService.energyUnits(denied)==reserve,"denial changed world or inventory");}finally{active.set(false);}h.succeed();
 }
 private static String diagnose(net.minecraft.server.level.ServerPlayer actor){var ray=io.github.dirtyredz.herbalwitch.magic.wand.use.WandTargeting.ray(actor);var plan=ray==null?null:io.github.dirtyredz.herbalwitch.magic.wand.action.PlantAction.prepare(actor,ray);return "hit="+(ray==null?"none":ray.getType()+" "+ray.getBlockPos()+" "+ray.getDirection()+" "+actor.level().getBlockState(ray.getBlockPos()))+", plan="+(plan==null?"none":plan.prepare())+", sky="+actor.level().canSeeSky(ray.getBlockPos().above())+", light="+actor.level().getRawBrightness(ray.getBlockPos().above(),0)+", survives="+net.minecraft.world.level.block.Blocks.WHEAT.defaultBlockState().canSurvive(actor.level(),ray.getBlockPos().above())+", retry="+io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseService.use(actor,InteractionHand.MAIN_HAND);}
}
