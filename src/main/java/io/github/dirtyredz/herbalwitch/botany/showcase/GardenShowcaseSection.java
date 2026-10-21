package io.github.dirtyredz.herbalwitch.botany.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Where herbs grow: a dry herb bed, a herb bed kept moist by water, and a mature herb on vanilla
 * farmland (seeds plant on either). Each with its sign.
 */
public final class GardenShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.garden";
    public static final String BED_DRY = "showcase." + HerbalWitch.MOD_ID + ".label.bed_dry";
    public static final String BED_MOIST = "showcase." + HerbalWitch.MOD_ID + ".label.bed_moist";
    public static final String FARMLAND = "showcase." + HerbalWitch.MOD_ID + ".label.farmland";

    @Override
    public String id() {
        return "garden";
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>(
                ShowcasePlacement.floorArea(0, 0, 6, 3, () -> Blocks.GRASS_BLOCK.defaultBlockState()));
        out.addAll(List.of(
                ShowcasePlacement.label(0, 0, Line.translated(BED_DRY)),
                ShowcasePlacement.content(0, 1, 1, () -> BotanyBlocks.herbBed().defaultBlockState()),
                ShowcasePlacement.label(2, 0, Line.translated(BED_MOIST)),
                ShowcasePlacement.content(2, 1, 1, HerbShowcaseSection::moistBed),
                // A water source walled in on three sides, the bed being the fourth.
                ShowcasePlacement.support(3, 1, 0, () -> Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                ShowcasePlacement.support(4, 1, 1, () -> Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                ShowcasePlacement.support(3, 1, 2, () -> Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                ShowcasePlacement.content(3, 1, 1, () -> Blocks.WATER.defaultBlockState()),
                ShowcasePlacement.label(5, 0, Line.translated(FARMLAND)),
                ShowcasePlacement.content(5, 1, 1, () -> Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7)),
                ShowcasePlacement.content(5, 2, 1,
                        () -> BotanyBlocks.plant(HerbCatalog.CHAMOMILE).getStateForAge(HerbSpec.MAX_AGE))));
        return out;
    }
}
