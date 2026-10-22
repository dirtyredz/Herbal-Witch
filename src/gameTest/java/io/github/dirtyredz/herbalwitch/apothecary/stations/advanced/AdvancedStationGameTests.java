package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench.block.BottlingBenchBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.block.CauldronBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.charm_workbench.block.CharmWorkbenchBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.still.block.StillBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization.StationOperations.Operation;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.core.Direction;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.start;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.campfire;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.inventory;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.tick;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.water;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * The four production stations, each with a representative recipe through its own inventory and
 * ticks: the cauldron and still over heat for a witch, the bottling bench by itself (plain, labelled
 * and vials), the charm workbench; and a two-cell bench placed whole through its item and broken whole.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AdvancedStationGameTests {
    private static final BlockPos AT = new BlockPos(4, 2, 4);
    private static final BlockPos NEAR = new BlockPos(4, 1, 2);

    private AdvancedStationGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void theCauldronBrewsAFourDoseMixtureOverHeat(GameTestHelper helper) {
        floor(helper);
        campfire(helper, AT.below(), true);
        place(helper, StationBlocks.CAULDRON.get(), AT);
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(CauldronBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.MUGWORT)));
        inv.setStackInSlot(CauldronBlockEntity.INPUT_1, new ItemStack(Items.CARROT));
        inv.setStackInSlot(CauldronBlockEntity.BASE, water());
        ServerPlayer w = witch(helper, "m7_brewer", NEAR);
        start(helper, w, AT);
        tick(helper, AT, 300);
        check(helper.getBlockState(AT).getValue(AdvancedStationBlock.ACTIVE), "a working cauldron shows it");
        tick(helper, AT, 300);
        ItemStack out = inv.getStackInSlot(CauldronBlockEntity.OUTPUT);
        check(out.is(AdvancedPreparations.item(MixtureCatalog.of(AdvancedProductCatalog.NIGHT_VISION).orElseThrow().spec())),
                "made " + out);
        check(inv.getStackInSlot(CauldronBlockEntity.RETURN).is(Items.GLASS_BOTTLE), "the water bottle did not come back");
        tick(helper, AT, 1);
        check(!helper.getBlockState(AT).getValue(AdvancedStationBlock.ACTIVE), "an idle cauldron still shows working");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theStillDistilsMoonEssenceOverHeat(GameTestHelper helper) {
        floor(helper);
        campfire(helper, AT.below(), true);
        place(helper, StationBlocks.STILL.get(), AT);
        check(helper.getBlockState(AT.above()).getBlock() == StationBlocks.STILL.get(), "the still's upper cell is missing");
        ProcessInventory inv = inventory(helper, AT);
        inv.setStackInSlot(StillBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.MUGWORT)));
        inv.setStackInSlot(StillBlockEntity.INPUT_1, new ItemStack(Items.AMETHYST_SHARD));
        inv.setStackInSlot(StillBlockEntity.BASE, water());
        inv.setStackInSlot(StillBlockEntity.PACKAGING, new ItemStack(Items.GLASS_BOTTLE));
        ServerPlayer w = witch(helper, "m7_distiller", NEAR);
        start(helper, w, AT);
        tick(helper, AT, 800);
        check(inv.getStackInSlot(StillBlockEntity.OUTPUT).is(AdvancedPreparations.item(AdvancedProductCatalog.MOON_ESSENCE)),
                "made " + inv.getStackInSlot(StillBlockEntity.OUTPUT));
        check(inv.getStackInSlot(StillBlockEntity.RETURN).is(Items.GLASS_BOTTLE), "no bottle back");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theBottlingBenchPortionsWholeMixturesByItself(GameTestHelper helper) {
        floor(helper);
        place(helper, StationBlocks.BOTTLING_BENCH.get(), AT.below());
        BlockPos bench = AT.below();
        ProcessInventory inv = inventory(helper, bench);
        MixtureCatalog.Mixture drink = MixtureCatalog.of(AdvancedProductCatalog.NIGHT_VISION).orElseThrow();
        inv.setStackInSlot(BottlingBenchBlockEntity.INPUT_0, new ItemStack(AdvancedPreparations.item(drink.spec())));
        inv.setStackInSlot(BottlingBenchBlockEntity.PACKAGING, new ItemStack(Items.GLASS_BOTTLE, 4));
        tick(helper, bench, 100);
        ItemStack out = inv.getStackInSlot(BottlingBenchBlockEntity.OUTPUT);
        check(out.is(AdvancedPreparations.item(AdvancedProductCatalog.NIGHT_VISION)) && out.getCount() == 4 && !PreparationMetadata.isLabelled(out),
                "plain bottling made " + out);
        check(inv.getStackInSlot(BottlingBenchBlockEntity.INPUT_0).isEmpty() && inv.getStackInSlot(BottlingBenchBlockEntity.PACKAGING).isEmpty(),
                "the whole mixture and four bottles are used");
        inv.setStackInSlot(BottlingBenchBlockEntity.OUTPUT, ItemStack.EMPTY);
        MixtureCatalog.Mixture vial = MixtureCatalog.of(AdvancedProductCatalog.POISON).orElseThrow();
        inv.setStackInSlot(BottlingBenchBlockEntity.INPUT_0, new ItemStack(AdvancedPreparations.item(vial.spec())));
        inv.setStackInSlot(BottlingBenchBlockEntity.INPUT_1, new ItemStack(Items.GUNPOWDER));
        inv.setStackInSlot(BottlingBenchBlockEntity.INPUT_2, new ItemStack(Items.PAPER));
        inv.setStackInSlot(BottlingBenchBlockEntity.PACKAGING, new ItemStack(Items.GLASS_BOTTLE, 4));
        tick(helper, bench, 100);
        out = inv.getStackInSlot(BottlingBenchBlockEntity.OUTPUT);
        check(out.is(AdvancedPreparations.item(AdvancedProductCatalog.POISON)) && out.getCount() == 4 && PreparationMetadata.isLabelled(out),
                "labelled vials made " + out);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theCharmWorkbenchMakesASachetForAWitch(GameTestHelper helper) {
        floor(helper);
        BlockPos bench = AT.below();
        place(helper, StationBlocks.CHARM_WORKBENCH.get(), bench);
        ProcessInventory inv = inventory(helper, bench);
        inv.setStackInSlot(CharmWorkbenchBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.SAGE)));
        inv.setStackInSlot(CharmWorkbenchBlockEntity.INPUT_1, new ItemStack(PreparationItems.powder(HerbCatalog.LAVENDER)));
        inv.setStackInSlot(CharmWorkbenchBlockEntity.INPUT_2, new ItemStack(Items.STRING, 2));
        ServerPlayer w = witch(helper, "m7_charmer", NEAR);
        start(helper, w, bench);
        tick(helper, bench, 200);
        check(inv.getStackInSlot(CharmWorkbenchBlockEntity.OUTPUT).is(CharmItems.item(CharmCatalog.PROTECTIVE_SACHET)),
                "made " + inv.getStackInSlot(CharmWorkbenchBlockEntity.OUTPUT));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBenchPlacesWholeAndBreaksWhole(GameTestHelper helper) {
        floor(helper);
        clearDrops(helper);
        ServerPlayer builder = io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player(helper, "m7_builder", GameType.SURVIVAL,
                Direction.SOUTH);
        io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.moveTo(helper, builder, new BlockPos(4, 1, 1));
        builder.setYRot(Direction.SOUTH.toYRot());
        io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.placeOnFloor(helper, builder,
                StationItems.item(io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog.BOTTLING_BENCH), new BlockPos(4, 1, 4));
        int cells = 0;
        for (int x = 0; x < 9; x++) {
            if (helper.getBlockState(new BlockPos(x, 1, 4)).getBlock() == StationBlocks.BOTTLING_BENCH.get()) {
                cells++;
            }
        }
        check(cells == 2, "the bench stands on " + cells + " cells");
        helper.getLevel().destroyBlock(helper.absolutePos(new BlockPos(4, 1, 4)), true);
        int left = 0;
        for (int x = 0; x < 9; x++) {
            if (helper.getBlockState(new BlockPos(x, 1, 4)).getBlock() == StationBlocks.BOTTLING_BENCH.get()) {
                left++;
            }
        }
        check(left == 0, left + " bench cells survived");
        check(dropped(helper, StationItems.item(io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog.BOTTLING_BENCH)) == 1,
                "exactly one bench drops");
        helper.succeed();
    }
}
