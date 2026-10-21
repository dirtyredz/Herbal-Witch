package io.github.dirtyredz.herbalwitch.stations.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import io.github.dirtyredz.herbalwitch.stations.block.StationEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * One station's showcase: the working station facing the entrance with a sign naming it and its
 * recipe, stocked with real sample inputs (and a sample product) through its ordinary inventory.
 * Heated stations stand on a lit, fuelled hearth. One parameterised class serves all five stations.
 */
public final class StationShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.stations";
    /** Hearth fuel for heated demonstrations: enough coal to keep them running a while. */
    public static final int DEMO_FUEL = 16;

    private final StationSpec spec;
    private final boolean onHearth;
    private final String recipeKey;
    private final Supplier<Map<Integer, ItemStack>> samples;

    /**
     * @param recipeKey translation key of the sign's recipe line
     * @param samples   slot -> stack stocked into the station's inventory
     */
    public StationShowcaseSection(StationSpec spec, boolean onHearth, String recipeKey, Supplier<Map<Integer, ItemStack>> samples) {
        this.spec = spec;
        this.onHearth = onHearth;
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
        if (onHearth) {
            out.add(ShowcasePlacement.content(0, 1, 1, () -> StationBlocks.HEARTH.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH), StationShowcaseSection::fuel));
            y = 2;
        }
        out.add(ShowcasePlacement.content(0, y, 1, () -> StationBlocks.block(spec).defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH), this::stock));
        return out;
    }

    private void stock(BlockEntity entity) {
        if (entity instanceof StationEntity station) {
            samples.get().forEach((slot, stack) -> station.inventory().setStackInSlot(slot, stack.copy()));
        }
    }

    private static void fuel(BlockEntity entity) {
        if (entity instanceof HearthBlockEntity hearth) {
            hearth.inventory().setStackInSlot(HearthBlockEntity.FUEL, new ItemStack(Items.COAL, DEMO_FUEL));
        }
    }
}
