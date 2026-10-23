package io.github.dirtyredz.herbalwitch.magic.wand;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import io.github.dirtyredz.herbalwitch.magic.wand.use.WandUseService;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.magic.wand.fixture.WandFixtures.*;
@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class WandPermissionGameTests {
 @GameTest(template=TEMPLATE,batch="herbalwitch_wand_permissions") public static void deniedPlacementRestoresSeedWorldEnergyAndPractice(GameTestHelper h){floor(h);h.setBlock(TARGET.below(),Blocks.FARMLAND);var actor=witch(h,"wand_event",FEET);actor.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.WHEAT_SEEDS,2));int energy=WitchStatusService.energyUnits(actor),practice=io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.practice(actor);var seen=new java.util.concurrent.atomic.AtomicInteger();java.util.function.Consumer<BlockEvent.EntityPlaceEvent> cancel=e->{if(e.getEntity()==actor){seen.incrementAndGet();e.setCanceled(true);}};MinecraftForge.EVENT_BUS.addListener(cancel);try{cast(h,actor,WandMode.PLANT,InteractionHand.MAIN_HAND,TARGET.below());check(seen.get()==1,"native placement event missing");check(h.getBlockState(TARGET).isAir()&&actor.getOffhandItem().getCount()==2&&WitchStatusService.energyUnits(actor)==energy&&io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.practice(actor)==practice,"cancelled placement changed supplies or witch state");}finally{MinecraftForge.EVENT_BUS.unregister(cancel);}h.succeed();}
 @GameTest(template=TEMPLATE,batch="herbalwitch_wand_permissions") public static void adventureNoEnergyAndNoOpWateringNeverSpend(GameTestHelper h){floor(h);h.setBlock(TARGET,Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,0));var actor=witch(h,"wand_adventure",FEET);int before=WitchStatusService.energyUnits(actor);actor.setGameMode(GameType.ADVENTURE);cast(h,actor,WandMode.WATER,InteractionHand.MAIN_HAND,TARGET);check(h.getBlockState(TARGET).getValue(FarmBlock.MOISTURE)==0&&WitchStatusService.energyUnits(actor)==before,"adventure changed farmland");actor.setGameMode(GameType.SURVIVAL);io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.setEnergy(actor,0);cast(h,actor,WandMode.WATER,InteractionHand.MAIN_HAND,TARGET);check(h.getBlockState(TARGET).getValue(FarmBlock.MOISTURE)==0&&WitchStatusService.energyUnits(actor)==0,"unfunded water");var fresh=witch(h,"wand_noop",FEET);h.setBlock(TARGET,Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE,7));before=WitchStatusService.energyUnits(fresh);cast(h,fresh,WandMode.WATER,InteractionHand.MAIN_HAND,TARGET);check(WitchStatusService.energyUnits(fresh)==before,"unchanged moisture charged");h.succeed();}
 @GameTest(template=TEMPLATE,batch="herbalwitch_wand_permissions") public static void aWallBlocksHarvestAndForgedClientHitCannotChooseAnotherCrop(GameTestHelper h){floor(h);h.setBlock(TARGET.below(),Blocks.FARMLAND);h.setBlock(TARGET,((CropBlock)Blocks.WHEAT).getStateForAge(7));var actor=witch(h,"wand_wall",FEET);h.setBlock(TARGET.north(),Blocks.STONE);h.setBlock(TARGET.north().above(),Blocks.STONE);int before=WitchStatusService.energyUnits(actor);cast(h,actor,WandMode.HARVEST,InteractionHand.MAIN_HAND,TARGET);check(((CropBlock)Blocks.WHEAT).isMaxAge(h.getBlockState(TARGET))&&WitchStatusService.energyUnits(actor)==before,"wall or supplied hit bypassed server ray");h.succeed();}
}
