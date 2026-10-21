package io.github.dirtyredz.herbalwitch.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.stove.block.StoveBlock;
import io.github.dirtyredz.herbalwitch.stations.heat.HeatSources;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.stations.registry.StationItems;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.placeOnFloor;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.inventory;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.tick;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * The multiblock stations (the 2 x 2 drying rack and hearth, the 2-wide stove): a player places the
 * whole station from one item or not at all; breaking any cell removes every cell and drops the
 * item once (never in creative) and the contents once; shared states (active, lit) show on every
 * cell; both stove burners heat a vessel.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class StationMultiblockGameTests {
    private StationMultiblockGameTests() {
    }

    /** Every cell of the station whose origin is {@code origin} (relative), facing {@code facing}. */
    private static boolean whole(GameTestHelper helper, StationBlock block, BlockPos origin, Direction facing) {
        MultiblockLayout layout = block.layout();
        BlockPos abs = helper.absolutePos(origin);
        for (int i = 0; i < layout.size(); i++) {
            BlockState cell = helper.getLevel().getBlockState(layout.cellPos(abs, facing, i));
            if (!cell.is(block) || cell.getValue(StationBlock.FACING) != facing || cell.getValue(layout.part()) != i) {
                return false;
            }
        }
        return true;
    }

    private static boolean gone(GameTestHelper helper, StationBlock block, BlockPos origin, Direction facing) {
        MultiblockLayout layout = block.layout();
        BlockPos abs = helper.absolutePos(origin);
        for (int i = 0; i < layout.size(); i++) {
            if (helper.getLevel().getBlockState(layout.cellPos(abs, facing, i)).is(block)) {
                return false;
            }
        }
        return true;
    }

    @GameTest(template = TEMPLATE)
    public static void theRackPlacesWholeAndBreaksAsOneDroppingItselfAndItsHerbsOnce(GameTestHelper helper) {
        floor(helper);
        StationBlock rack = (StationBlock) StationBlocks.DRYING_RACK.get();
        ServerPlayer player = player(helper, "rack_builder", GameType.SURVIVAL, Direction.NORTH);
        BlockPos origin = new BlockPos(2, 1, 4);
        check(placeOnFloor(helper, player, StationItems.item(StationCatalog.DRYING_RACK), origin).consumesAction(), "rack not placed");
        Direction facing = helper.getBlockState(origin).getValue(StationBlock.FACING);
        check(whole(helper, rack, origin, facing), "the rack is not two wide and two tall");
        ProcessInventory inv = inventory(helper, origin);
        inv.setStackInSlot(DryingRackBlockEntity.INPUT, new ItemStack(BotanyItems.fresh(HerbCatalog.MINT), 3));
        tick(helper, origin, 1);
        MultiblockLayout layout = rack.layout();
        BlockPos abs = helper.absolutePos(origin);
        for (int i = 0; i < layout.size(); i++) {
            check(helper.getLevel().getBlockState(layout.cellPos(abs, facing, i)).getValue(DryingRackBlock.ACTIVE), "cell " + i + " shows no bundles");
        }
        clearDrops(helper);
        // break a top cell: the whole rack goes, one rack and the three herbs drop
        BlockPos top = layout.cellPos(abs, facing, 3);
        player.gameMode.destroyBlock(top);
        check(gone(helper, rack, origin, facing), "cells left standing");
        helper.runAfterDelay(1, () -> {
            check(dropped(helper, StationItems.item(StationCatalog.DRYING_RACK)) == 1, "rack dropped "
                    + dropped(helper, StationItems.item(StationCatalog.DRYING_RACK)) + " times");
            check(dropped(helper, BotanyItems.fresh(HerbCatalog.MINT)) == 3, "herbs dropped " + dropped(helper, BotanyItems.fresh(HerbCatalog.MINT)));
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void aCreativeBreakDropsNoStationAndTheRackNeedsRoom(GameTestHelper helper) {
        floor(helper);
        StationBlock rack = (StationBlock) StationBlocks.DRYING_RACK.get();
        ServerPlayer creative = player(helper, "rack_creative", GameType.CREATIVE, Direction.NORTH);
        BlockPos origin = new BlockPos(2, 1, 4);
        place(helper, rack, origin);
        check(whole(helper, rack, origin, Direction.NORTH), "fixture did not place the whole rack");
        clearDrops(helper);
        creative.gameMode.destroyBlock(helper.absolutePos(origin.west()));
        check(gone(helper, rack, origin, Direction.NORTH), "cells left standing after a creative break");
        // no room: a block where the upper cells would stand refuses the placement
        ServerPlayer player = player(helper, "rack_cramped", GameType.SURVIVAL, Direction.NORTH);
        BlockPos cramped = new BlockPos(6, 1, 4);
        helper.setBlock(cramped.above(), Blocks.STONE);
        check(!placeOnFloor(helper, player, StationItems.item(StationCatalog.DRYING_RACK), cramped).consumesAction()
                || !helper.getBlockState(cramped).is(rack), "a rack squeezed under a block");
        helper.runAfterDelay(1, () -> {
            check(dropped(helper, StationItems.item(StationCatalog.DRYING_RACK)) == 0, "creative break dropped a rack");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void bothStoveBurnersHeatAVesselWhileLit(GameTestHelper helper) {
        floor(helper);
        StationBlock stove = (StationBlock) StationBlocks.STOVE.get();
        BlockPos origin = new BlockPos(5, 1, 4);
        place(helper, stove, origin);
        check(whole(helper, stove, origin, Direction.NORTH), "the stove is not two wide");
        inventory(helper, origin).setStackInSlot(HearthBlockEntity.FUEL, new ItemStack(Items.COAL));
        tick(helper, origin, 1);
        BlockPos second = origin.west();
        check(helper.getBlockState(origin).getValue(StoveBlock.LIT) && helper.getBlockState(second).getValue(StoveBlock.LIT), "not lit on both burners");
        place(helper, StationBlocks.KETTLE.get(), origin.above());
        place(helper, StationBlocks.INFUSION_POT.get(), second.above());
        check(HeatSources.isHeated(helper.getLevel(), helper.absolutePos(origin.above())), "the first burner does not heat");
        check(HeatSources.isHeated(helper.getLevel(), helper.absolutePos(second.above())), "the second burner does not heat");
        check(helper.getBlockEntity(second) == null, "a non-origin cell holds a block entity");
        helper.succeed();
    }
}
