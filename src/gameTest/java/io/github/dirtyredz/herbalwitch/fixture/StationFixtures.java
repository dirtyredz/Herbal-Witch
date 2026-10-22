package io.github.dirtyredz.herbalwitch.fixture;

import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Station setup for GameTests: placing a station, stocking its inventory and running its server ticks. */
public final class StationFixtures {
    private StationFixtures() {
    }

    /**
     * Places {@code station} facing north at {@code pos} (relative) and returns its block entity. A
     * multiblock station is placed whole, its other cells to the west (the viewer's right) and above.
     */
    public static StationEntity place(GameTestHelper helper, Block station, BlockPos pos) {
        BlockState state = station.defaultBlockState().setValue(StationBlock.FACING, Direction.NORTH);
        if (station instanceof StationBlock block && block.layout() != null) {
            block.placeStation(helper.getLevel(), helper.absolutePos(pos), state, Block.UPDATE_ALL);
        } else {
            helper.setBlock(pos, state);
        }
        return (StationEntity) helper.getBlockEntity(pos);
    }

    public static ProcessInventory inventory(GameTestHelper helper, BlockPos pos) {
        return ((StationEntity) helper.getBlockEntity(pos)).inventory();
    }

    /** Runs {@code ticks} loaded server ticks of the station at {@code pos}. */
    public static void tick(GameTestHelper helper, BlockPos pos, int ticks) {
        StationEntity station = (StationEntity) helper.getBlockEntity(pos);
        for (int i = 0; i < ticks; i++) {
            station.serverTick();
        }
    }

    /** A lit campfire at {@code pos}: the simplest heat source. */
    public static void campfire(GameTestHelper helper, BlockPos pos, boolean lit) {
        helper.setBlock(pos, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, lit));
    }

    public static ItemStack water() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
    }
}
