package io.github.dirtyredz.herbalwitch.apothecary.botany.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Where herbs grow: a dry herb bed, a herb bed kept moist by water, and a mature herb on vanilla
 * farmland (seeds plant on either), each with its sign; then beds joining into planters: a 2 x 2
 * square planted with four mature herbs, a line, an L and a ring around an empty centre.
 */
public final class GardenShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.garden";
    public static final String BED_DRY = "showcase." + HerbalWitch.MOD_ID + ".label.bed_dry";
    public static final String BED_MOIST = "showcase." + HerbalWitch.MOD_ID + ".label.bed_moist";
    public static final String FARMLAND = "showcase." + HerbalWitch.MOD_ID + ".label.farmland";
    public static final String JOINED = "showcase." + HerbalWitch.MOD_ID + ".label.beds_joined";

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
                ShowcasePlacement.floorArea(0, 0, 10, 9, () -> Blocks.GRASS_BLOCK.defaultBlockState()));
        // each bed has its sign one block west of it; the moist bed's water sits on its far side
        out.addAll(List.of(
                ShowcasePlacement.label(0, 1, Line.translated(BED_DRY)),
                ShowcasePlacement.content(1, 1, 1, () -> BotanyBlocks.herbBed().defaultBlockState()),
                ShowcasePlacement.label(3, 1, Line.translated(BED_MOIST)),
                ShowcasePlacement.content(4, 1, 1, HerbShowcaseSection::moistBed),
                // A water source walled in on three sides, the bed being the fourth.
                ShowcasePlacement.support(5, 1, 0, () -> Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                ShowcasePlacement.support(6, 1, 1, () -> Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                ShowcasePlacement.support(5, 1, 2, () -> Blocks.MOSSY_COBBLESTONE.defaultBlockState()),
                ShowcasePlacement.content(5, 1, 1, () -> Blocks.WATER.defaultBlockState()),
                ShowcasePlacement.label(8, 1, Line.translated(FARMLAND)),
                ShowcasePlacement.content(9, 1, 1, () -> Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7)),
                ShowcasePlacement.content(9, 2, 1,
                        () -> BotanyBlocks.plant(HerbCatalog.CHAMOMILE).getStateForAge(HerbSpec.MAX_AGE))));
        // joined beds (they connect as they are placed): a planted square, a line, an L, a ring
        out.add(ShowcasePlacement.label(6, 3, Line.translated(JOINED)));
        HerbSpec[] square = {HerbCatalog.CHAMOMILE, HerbCatalog.LAVENDER, HerbCatalog.MANDRAKE, HerbCatalog.THYME};
        for (int i = 0; i < 4; i++) {
            int x = i % 2;
            int z = 3 + i / 2;
            HerbSpec herb = square[i];
            out.add(bed(x, z));
            out.add(ShowcasePlacement.content(x, 2, z, () -> BotanyBlocks.plant(herb).getStateForAge(HerbSpec.MAX_AGE)));
        }
        for (int x = 3; x <= 5; x++) {
            out.add(bed(x, 3));
        }
        out.addAll(List.of(bed(7, 3), bed(8, 3), bed(7, 4)));
        for (int x = 0; x <= 2; x++) {
            for (int z = 6; z <= 8; z++) {
                if (x != 1 || z != 7) {
                    out.add(bed(x, z));
                }
            }
        }
        return out;
    }

    private static ShowcasePlacement bed(int x, int z) {
        return ShowcasePlacement.content(x, 1, z, () -> BotanyBlocks.herbBed().defaultBlockState());
    }
}
