package io.github.dirtyredz.herbalwitch.apothecary.stations.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One station's showcase: the working station facing the entrance with a sign naming it and its
 * recipe, stocked with real sample inputs (and a sample product) through its ordinary inventory.
 * Heated stations stand on a lit, fuelled witch's stove. A multiblock station is placed cell by
 * cell, origin first, so it stands complete. One parameterised class serves every station.
 */
public final class StationShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.stations";
    /** Stove and hearth fuel for heated demonstrations: enough coal to keep them running a while. */
    public static final int DEMO_FUEL = 16;

    private final StationSpec spec;
    private final boolean onStove;
    private final String recipeKey;
    private final Supplier<Map<Integer, ItemStack>> samples;

    /**
     * @param recipeKey translation key of the sign's recipe line
     * @param samples   slot -> stack stocked into the station's inventory
     */
    public StationShowcaseSection(StationSpec spec, boolean onStove, String recipeKey, Supplier<Map<Integer, ItemStack>> samples) {
        this.spec = spec;
        this.onStove = onStove;
        this.recipeKey = recipeKey;
        this.samples = samples;
    }

    @Override
    public String id() {
        return spec.directory();
    }

    @Override
    public String titleKey() {
        return "block." + HerbalWitch.MOD_ID + "." + spec.path().replace('/', '.');
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        out.add(ShowcasePlacement.label(1, 0, Line.translated(recipeKey)));
        int y = 1;
        if (onStove) {
            station(out, () -> StationBlocks.STOVE.get(), StationCatalog.STOVE.layout(), 0, 1, StationShowcaseSection::fuel);
            y = 2;
        }
        station(out, () -> StationBlocks.block(spec), spec.layout(), onStove ? 1 : 0, y, this::stock);
        return out;
    }

    /**
     * A station facing north at height {@code y}: one block at {@code x}, or a multiblock with its
     * origin at local x = x + width - 1 (its other cells extend to the viewer's right, west, toward
     * x). Layouts come from the catalogue, so planning never touches the registries.
     */
    private static void station(List<ShowcasePlacement> out, Supplier<Block> block, MultiblockLayout layout, int x, int y,
                                Consumer<BlockEntity> init) {
        if (layout == null) {
            out.add(ShowcasePlacement.content(x, y, 1, () -> facingNorth(block.get()), init));
            return;
        }
        int width = 0;
        for (int i = 0; i < layout.size(); i++) {
            width = Math.max(width, layout.cell(i).right() + 1);
        }
        for (int i = 0; i < layout.size(); i++) {
            int part = i;
            BlockPos at = layout.offset(Direction.NORTH, i);
            out.add(ShowcasePlacement.content(x + width - 1 + at.getX(), y + at.getY(), 1 + at.getZ(),
                    () -> facingNorth(block.get()).setValue(layout.part(), part), i == 0 ? init : null));
        }
    }

    private static BlockState facingNorth(Block block) {
        return block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
    }

    private void stock(BlockEntity entity) {
        if (entity instanceof StationEntity station) {
            samples.get().forEach((slot, stack) -> station.inventory().setStackInSlot(slot, stack.copy()));
        }
    }

    private static void fuel(BlockEntity entity) {
        if (entity instanceof HearthBlockEntity hearth) { // the hearth's and the stove's fuel
            hearth.inventory().setStackInSlot(HearthBlockEntity.FUEL, new ItemStack(Items.COAL, DEMO_FUEL));
        }
    }
}
