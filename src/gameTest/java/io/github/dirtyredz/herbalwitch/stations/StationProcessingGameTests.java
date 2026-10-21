package io.github.dirtyredz.herbalwitch.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.block.InfusionPotBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.kettle.block.KettleBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block.MortarBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStatus;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.campfire;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.inventory;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.tick;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.water;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Each station running real recipes through its own inventory and ticks: durations, heat, outputs,
 * returned containers, water metadata and the hearth's fuel.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StationProcessingGameTests {
    private static final BlockPos AT = new BlockPos(4, 2, 4);

    private StationProcessingGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void theRackDriesAFreshHerbInTwelveHundredTicks(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.DRYING_RACK.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(DryingRackBlockEntity.INPUT, new ItemStack(BotanyItems.fresh(HerbCatalog.SAGE), 2));
        tick(helper, AT, 1_199);
        check(inv.getStackInSlot(DryingRackBlockEntity.OUTPUT).isEmpty(), "dried early");
        check(helper.getBlockState(AT).getValue(DryingRackBlock.ACTIVE), "a stocked rack shows its herbs");
        tick(helper, AT, 1);
        check(inv.getStackInSlot(DryingRackBlockEntity.OUTPUT).is(PreparationItems.bundle(HerbCatalog.SAGE)), "no bundle");
        check(inv.getStackInSlot(DryingRackBlockEntity.INPUT).getCount() == 1, "consumed more than one herb");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theMortarGrindsByItselfAndMakesExactRecipesOnly(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.MORTAR.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(MortarBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE)));
        inv.setStackInSlot(MortarBlockEntity.INPUT_1, new ItemStack(Items.DANDELION));
        inv.setStackInSlot(MortarBlockEntity.INPUT_2, new ItemStack(Items.BONE_MEAL));
        tick(helper, AT, 100);
        ItemStack out = inv.getStackInSlot(MortarBlockEntity.OUTPUT);
        check(out.is(PreparationItems.product(EverydayProductCatalog.FLOWER_DUST)) && out.getCount() == 2,
                "three-slot recipe made " + out + " (not the one-slot yellow dye)");
        // An unrelated item in a slot blocks any recipe.
        inv.setStackInSlot(MortarBlockEntity.INPUT_0, new ItemStack(Items.BONE));
        inv.setStackInSlot(MortarBlockEntity.INPUT_1, new ItemStack(Items.DIRT));
        tick(helper, AT, 200);
        check(inv.getStackInSlot(MortarBlockEntity.INPUT_0).is(Items.BONE), "bone ground beside dirt");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theKettleNeedsHeatAndTheBottleBecomesTheDrink(GameTestHelper helper) {
        floor(helper);
        campfire(helper, AT.below(), false);
        place(helper, StationBlocks.KETTLE.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        ItemStack purified = water();
        purified.getOrCreateTag().putInt("Purity", 2);
        inv.setStackInSlot(KettleBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.MINT)));
        inv.setStackInSlot(KettleBlockEntity.INPUT_1, new ItemStack(Items.SUGAR));
        inv.setStackInSlot(KettleBlockEntity.WATER, purified);
        tick(helper, AT, 300);
        KettleBlockEntity kettle = (KettleBlockEntity) helper.getBlockEntity(AT);
        check(kettle.station().job().status() == ProcessStatus.NEEDS_HEAT, "unlit campfire heated: " + kettle.station().job().status());
        check(inv.getStackInSlot(KettleBlockEntity.OUTPUT).isEmpty(), "steeped without heat");
        campfire(helper, AT.below(), true);
        tick(helper, AT, 200);
        ItemStack tea = inv.getStackInSlot(KettleBlockEntity.OUTPUT);
        check(tea.is(PreparationItems.product(EverydayProductCatalog.ENERGY_TEA)), "no energy tea: " + tea);
        check(inv.getStackInSlot(KettleBlockEntity.WATER).isEmpty(), "water bottle not used");
        check(inv.getStackInSlot(KettleBlockEntity.RETURN).isEmpty(), "the kettle must not also return a bottle");
        // Water metadata rides along only through an installed carrier (Thirst Was Taken's purity).
        boolean thirst = io.github.dirtyredz.herbalwitch.platform.compat.thirst.ThirstIntegration.active();
        check(thirst == (tea.hasTag() && tea.getTag().getInt("Purity") == 2), "water purity carried=" + tea.getTag() + " with Thirst=" + thirst);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theInfusionPotPacksASalveAndReturnsTheHoneyBottle(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.MAGMA_BLOCK);
        place(helper, StationBlocks.INFUSION_POT.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(InfusionPotBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.YARROW)));
        inv.setStackInSlot(InfusionPotBlockEntity.INPUT_1, new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE)));
        inv.setStackInSlot(InfusionPotBlockEntity.BASE, new ItemStack(Items.HONEY_BOTTLE));
        inv.setStackInSlot(InfusionPotBlockEntity.PACKAGING, new ItemStack(Items.BOWL));
        tick(helper, AT, 599);
        check(inv.getStackInSlot(InfusionPotBlockEntity.OUTPUT).isEmpty(), "infused early");
        tick(helper, AT, 1);
        check(inv.getStackInSlot(InfusionPotBlockEntity.OUTPUT).is(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE)), "no salve");
        check(inv.getStackInSlot(InfusionPotBlockEntity.RETURN).is(Items.GLASS_BOTTLE)
                && inv.getStackInSlot(InfusionPotBlockEntity.RETURN).getCount() == 1, "honey bottle not returned once");
        check(inv.getStackInSlot(InfusionPotBlockEntity.BASE).isEmpty() && inv.getStackInSlot(InfusionPotBlockEntity.PACKAGING).isEmpty(),
                "base or package left over");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aFullOutputPausesWithoutConsuming(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(AT.below(), Blocks.STONE);
        place(helper, StationBlocks.MORTAR.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(MortarBlockEntity.INPUT_0, new ItemStack(Items.BONE, 4));
        inv.setStackInSlot(MortarBlockEntity.OUTPUT, new ItemStack(Items.BONE_MEAL, 63));
        tick(helper, AT, 300);
        MortarBlockEntity mortar = (MortarBlockEntity) helper.getBlockEntity(AT);
        check(mortar.station().job().status() == ProcessStatus.OUTPUT_BLOCKED, "status " + mortar.station().job().status());
        check(inv.getStackInSlot(MortarBlockEntity.INPUT_0).getCount() == 4, "consumed while blocked");
        inv.setStackInSlot(MortarBlockEntity.OUTPUT, ItemStack.EMPTY);
        tick(helper, AT, 100);
        check(inv.getStackInSlot(MortarBlockEntity.OUTPUT).getCount() == 3 && inv.getStackInSlot(MortarBlockEntity.INPUT_0).getCount() == 3,
                "did not resume once");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theHearthBurnsFuelHeatsAboveAndReturnsBuckets(GameTestHelper helper) {
        floor(helper);
        BlockPos hearth = new BlockPos(4, 1, 4);
        place(helper, StationBlocks.HEARTH.get(), hearth);
        ProcessInventory inv = inventory(helper, hearth);
        check(!helper.getBlockState(hearth).getValue(HearthBlock.LIT), "a new hearth is cold");
        inv.setStackInSlot(HearthBlockEntity.FUEL, new ItemStack(Items.LAVA_BUCKET));
        tick(helper, hearth, 1);
        check(helper.getBlockState(hearth).getValue(HearthBlock.LIT), "lava did not light it");
        check(inv.getStackInSlot(HearthBlockEntity.RETURN).is(Items.BUCKET), "bucket not returned");
        check(helper.getBlockState(hearth).getLightEmission() == HearthBlock.LIGHT, "no light");
        place(helper, StationBlocks.KETTLE.get(), hearth.above());
        KettleBlockEntity kettle = (KettleBlockEntity) helper.getBlockEntity(hearth.above());
        check(io.github.dirtyredz.herbalwitch.stations.heat.HeatSources.isHeated(helper.getLevel(), helper.absolutePos(hearth.above())),
                "a lit hearth does not heat the kettle");
        check(kettle != null, "kettle on hearth");
        HearthBlockEntity entity = (HearthBlockEntity) helper.getBlockEntity(hearth);
        int left = entity.burn();
        tick(helper, hearth, left);
        check(!helper.getBlockState(hearth).getValue(HearthBlock.LIT), "still lit after the fuel ran out");
        check(!io.github.dirtyredz.herbalwitch.stations.heat.HeatSources.isHeated(helper.getLevel(), helper.absolutePos(hearth.above())),
                "a cold hearth still heats");
        helper.succeed();
    }
}
