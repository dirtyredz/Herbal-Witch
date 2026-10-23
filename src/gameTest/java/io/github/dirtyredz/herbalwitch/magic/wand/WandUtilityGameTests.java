package io.github.dirtyredz.herbalwitch.magic.wand;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.action.*;
import io.github.dirtyredz.herbalwitch.magic.wand.network.*;
import io.github.dirtyredz.herbalwitch.magic.wand.use.*;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.magic.wand.fixture.WandFixtures.*;
@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class WandUtilityGameTests {
 @GameTest(template=TEMPLATE) public static void partialCollectionConservesRemainderAndChargesOnce(GameTestHelper h){
  floor(h);var actor=witch(h,"wand_collect_partial",FEET);actor.setItemInHand(InteractionHand.MAIN_HAND,wand(WandMode.COLLECT));for(int i=1;i<36;i++)actor.getInventory().items.set(i,new ItemStack(Items.DIRT,64));actor.getInventory().items.set(1,new ItemStack(Items.WHEAT,63));
  var at=Vec3.atCenterOf(h.absolutePos(TARGET));var entity=new ItemEntity(h.getLevel(),at.x,at.y,at.z,new ItemStack(Items.WHEAT,5));entity.setNoPickUpDelay();h.getLevel().addFreshEntity(entity);
  actor.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,at);h.setBlock(TARGET.below(),Blocks.STONE);int before=WitchStatusService.energyUnits(actor);String result=WandUseService.use(actor,InteractionHand.MAIN_HAND);
  check(result.equals("success"),"collection refused: "+result);check(entity.isAlive()&&entity.getItem().getCount()==4&&actor.getInventory().items.get(1).getCount()==64,"collection lost remainder");check(WitchStatusService.energyUnits(actor)==before-100,"collection debit");h.succeed();
 }
 @GameTest(template=TEMPLATE) public static void collectionRejectsOwnershipDelayAndChangedInventory(GameTestHelper h){
  floor(h);var actor=witch(h,"wand_collect_guards",FEET);var at=Vec3.atCenterOf(h.absolutePos(TARGET));var item=new ItemEntity(h.getLevel(),at.x,at.y,at.z,new ItemStack(Items.APPLE,3));item.setNoPickUpDelay();item.setTarget(java.util.UUID.randomUUID());h.getLevel().addFreshEntity(item);check(CollectAction.prepare(actor,at)==null,"foreign pickup accepted");item.setTarget(null);item.setDefaultPickUpDelay();check(CollectAction.prepare(actor,at)==null,"pickup delay bypassed");item.setNoPickUpDelay();var change=CollectAction.prepare(actor,at);check(change!=null,"eligible pickup rejected");actor.getInventory().items.set(1,new ItemStack(Items.STONE));check(!change.prepare()&&!change.apply()&&item.getItem().getCount()==3,"stale pickup committed");h.succeed();
 }
 @GameTest(template=TEMPLATE) public static void lightingUsesVanillaStatesAndRealFuelWithNoEmptyFuelCharge(GameTestHelper h){
  floor(h);int index=0;for(var block:java.util.List.of(Blocks.CANDLE,Blocks.CAMPFIRE,Blocks.SOUL_CAMPFIRE)){h.setBlock(TARGET,block.defaultBlockState().setValue(BlockStateProperties.LIT,false));var actor=witch(h,"wand_light_"+index++,FEET);int before=WitchStatusService.energyUnits(actor);cast(h,actor,WandMode.LIGHT,InteractionHand.MAIN_HAND,TARGET);check(h.getBlockState(TARGET).getValue(BlockStateProperties.LIT),"vanilla light failed");check(WitchStatusService.energyUnits(actor)==before-100,"light debit");}
  for(var block:java.util.List.of(StationBlocks.HEARTH.get(),StationBlocks.STOVE.get())){h.setBlock(TARGET,Blocks.AIR);((StationBlock)block).placeStation(h.getLevel(),h.absolutePos(TARGET),block.defaultBlockState(),Block.UPDATE_ALL);var hearth=(HearthBlockEntity)h.getLevel().getBlockEntity(h.absolutePos(TARGET));var actor=witch(h,"wand_fuel_"+index++,FEET);check(LightAction.prepare(actor,h.absolutePos(TARGET))==null,"empty hearth lit");hearth.inventory().setStackInSlot(HearthBlockEntity.FUEL,new ItemStack(Items.LAVA_BUCKET));var change=LightAction.prepare(actor,h.absolutePos(TARGET));check(change!=null&&WitchStatusService.spend(actor,100,change),"fuel ignition refused");change.finish();check(hearth.burn()>0&&hearth.inventory().getStackInSlot(HearthBlockEntity.FUEL).isEmpty()&&hearth.inventory().getStackInSlot(HearthBlockEntity.RETURN).is(Items.BUCKET),"fuel/container accounting");}
  h.succeed();
 }
 @GameTest(template=TEMPLATE) public static void selectorRejectsStaleHandStackAndNeverCasts(GameTestHelper h){
  floor(h);var actor=witch(h,"wand_select",FEET);var stack=wand(WandMode.HARVEST);actor.setItemInHand(InteractionHand.MAIN_HAND,stack);int before=WitchStatusService.energyUnits(actor);var request=new WandModeRequest(InteractionHand.MAIN_HAND,actor.getInventory().selected,stack.copy(),WandMode.WATER);WandPackets.handle(actor,request);check(WandUseService.mode(stack)==WandMode.WATER,"selection refused");WandPackets.handle(actor,new WandModeRequest(request.hand(),request.slot(),request.observed(),WandMode.LIGHT));check(WandUseService.mode(stack)==WandMode.WATER&&WitchStatusService.energyUnits(actor)==before,"stale selector changed state or energy");h.succeed();
 }
}
