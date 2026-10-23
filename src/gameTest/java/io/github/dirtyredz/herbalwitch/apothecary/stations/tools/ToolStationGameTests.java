package io.github.dirtyredz.herbalwitch.apothecary.stations.tools;
import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.ProcessStationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization.StationOperations.Operation;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
import io.github.dirtyredz.herbalwitch.magic.wand.registry.WandItems;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
@GameTestHolder(HerbalWitch.MOD_ID) @PrefixGameTestTemplate(false)
public final class ToolStationGameTests {
 private static final BlockPos AT=new BlockPos(4,1,4), NEAR=new BlockPos(4,1,2);
 @GameTest(template=TEMPLATE) public static void toolsRequireExplicitWitchStartAndChargeOnce(GameTestHelper h) {
  floor(h);place(h,StationBlocks.WAND_BENCH.get(),AT);var inv=inventory(h,AT);
  inv.setStackInSlot(0,new ItemStack(Items.STICK,2));inv.setStackInSlot(1,new ItemStack(Items.AMETHYST_SHARD));inv.setStackInSlot(2,new ItemStack(Items.GOLD_NUGGET));
  var novice=player(h,"m10_tool_novice",NEAR);tick(h,AT,200);
  check(inv.getStackInSlot(3).isEmpty() && inv.reserved(0)==0,"automation started a batch");
  check(operate(h,novice,AT,Operation.START).equals("not_witch"),"non-witch started tools");
  var witch=witch(h,"m10_tool_carver",NEAR);int before=energy(witch);start(h,witch,AT);tick(h,AT,100);
  check(inv.getStackInSlot(3).is(WandItems.WAND.get()),"wand missing");check(energy(witch)==before-500,"wrong debit");
  tick(h,AT,200);check(energy(witch)==before-500,"repeat debit");h.succeed();
 }
 @GameTest(template=TEMPLATE) public static void repairPreservesMetadataAndBlockedOutputConservesEverything(GameTestHelper h) {
  floor(h);place(h,StationBlocks.BROOM_STAND.get(),AT);var inv=inventory(h,AT);
  inv.setStackInSlot(0,new ItemStack(Items.WHEAT,2));inv.setStackInSlot(1,new ItemStack(Items.STRING));
  ItemStack broom=new ItemStack(BroomItems.BROOM.get());new BroomItemData(450,23999,19).write(broom);broom.setHoverName(Component.literal("Old Hazel"));
  inv.setStackInSlot(4,broom);inv.setStackInSlot(3,new ItemStack(Items.DIRT,64));
  var witch=witch(h,"m10_tool_repairer",NEAR);int before=energy(witch);start(h,witch,AT);tick(h,AT,200);
  check(energy(witch)==before && ItemStack.matches(broom,inv.getStackInSlot(4)),"blocked repair changed input or energy");
  check(inv.extractItem(4,1,false).isEmpty(),"automation extracted reserved broom");
  inv.setStackInSlot(3,ItemStack.EMPTY);tick(h,AT,100);
  ItemStack output=inv.getStackInSlot(3);check(output.getHoverName().getString().equals("Old Hazel"),"lost custom name");
  check(BroomItemData.read(output).orElseThrow().equals(new BroomItemData(250,23999,19)),"repair replaced condition/remainders");
  check(inv.getStackInSlot(4).isEmpty() && inv.getStackInSlot(0).isEmpty() && energy(witch)==before-500,"repair did not settle once");h.succeed();
 }
 @GameTest(template=TEMPLATE) public static void pristineOrUnknownBroomsNeverMatchRepair(GameTestHelper h) {
  floor(h);place(h,StationBlocks.BROOM_STAND.get(),AT);var inv=inventory(h,AT);
  inv.setStackInSlot(0,new ItemStack(Items.WHEAT,2));inv.setStackInSlot(1,new ItemStack(Items.STRING));
  ItemStack broom=new ItemStack(BroomItems.BROOM.get());inv.setStackInSlot(4,broom);tick(h,AT,1);
  var station=(ProcessStationEntity)h.getBlockEntity(AT);check(station.station().job().recipe()==null,"pristine matched");
  new BroomItemData(600,0,0).write(broom);broom.getTag().getCompound(BroomItemData.KEY).putInt("schema",99);inv.setStackInSlot(4,broom.copy());tick(h,AT,1);
  check(station.station().job().recipe()==null,"unknown schema matched");h.succeed();
 }

 @GameTest(template=TEMPLATE) public static void bothStationsPlaceThroughTheCheckedStructureItem(GameTestHelper h) {
  floor(h);
  for(var spec:java.util.List.of(io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog.BROOM_STAND,io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog.WAND_BENCH)) {
   for(int x=1;x<8;x++)for(int z=1;z<8;z++)h.setBlock(new BlockPos(x,1,z),net.minecraft.world.level.block.Blocks.AIR);
   var actor=io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player(h,"m10_place_"+spec.directory().replace('/','_'),net.minecraft.world.level.GameType.SURVIVAL,net.minecraft.core.Direction.SOUTH);
   var stack=new ItemStack(io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationItems.item(spec),2);
   check(stack.getItem() instanceof io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacementItem,"not checked placement");
   var result=io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn(h,actor,stack,AT.below(),net.minecraft.core.Direction.UP);
   check(result.consumesAction() && stack.getCount()==1,"placement did not consume exactly one");
   check(h.getBlockEntity(AT) instanceof ProcessStationEntity,"missing origin");
  }
  h.succeed();
 }
 @GameTest(template=TEMPLATE) public static void interruptedRepairCanCancelWithoutLosingTheNamedBroom(GameTestHelper h) {
  floor(h);place(h,StationBlocks.BROOM_STAND.get(),AT);var inv=inventory(h,AT);
  inv.setStackInSlot(0,new ItemStack(Items.WHEAT,2));inv.setStackInSlot(1,new ItemStack(Items.STRING));
  var broom=new ItemStack(BroomItems.BROOM.get());new BroomItemData(350,199,7).write(broom);broom.setHoverName(Component.literal("Hazel"));inv.setStackInSlot(4,broom);
  var actor=witch(h,"m10_repair_cancel",NEAR);int before=energy(actor);start(h,actor,AT);tick(h,AT,40);
  var station=(ProcessStationEntity)h.getBlockEntity(AT);var saved=station.saveWithoutMetadata();station.load(saved);tick(h,AT,1);
  check(inv.getStackInSlot(3).isEmpty(),"reload completed repair");
  check(operate(h,actor,AT,Operation.CANCEL).equals("cancelled"),"cannot cancel restored repair");
  check(ItemStack.matches(broom,inv.getStackInSlot(4)) && energy(actor)==before,"cancel lost broom or energy");
  check(inv.extractItem(4,1,false).getCount()==1,"cancel retained reservation");h.succeed();
 }
}
