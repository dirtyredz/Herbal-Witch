package io.github.dirtyredz.herbalwitch.fixture;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.GardenSoil;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;

/** Garden setup for botany GameTests: farmland, beds and plants at a given age, and a crafting grid. */
public final class BotanyFixtures {
    private BotanyFixtures() {
    }

    /** Moist farmland at {@code soil} and the herb's plant at the given age above it. */
    public static BlockPos plantOnFarmland(GameTestHelper helper, HerbSpec herb, BlockPos soil, int age) {
        helper.setBlock(soil, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
        helper.setBlock(soil.above(), BotanyBlocks.plant(herb).getStateForAge(age));
        return soil.above();
    }

    public static void bed(GameTestHelper helper, BlockPos pos, boolean moist) {
        helper.setBlock(pos, BotanyBlocks.herbBed().defaultBlockState()
                .setValue(HerbBedBlock.MOISTURE, moist ? GardenSoil.MAX_MOISTURE : 0));
    }

    /** A 3 x 3 crafting grid filled row by row. */
    public static TransientCraftingContainer grid(ItemStack... stacks) {
        TransientCraftingContainer grid = new TransientCraftingContainer(new AbstractContainerMenu(null, -1) {
            @Override
            public ItemStack quickMoveStack(Player player, int index) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 3, 3);
        for (int i = 0; i < stacks.length; i++) {
            grid.setItem(i, stacks[i].copy());
        }
        return grid;
    }
}
