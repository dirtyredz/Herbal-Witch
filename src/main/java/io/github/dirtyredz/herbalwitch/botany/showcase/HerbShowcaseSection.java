package io.github.dirtyredz.herbalwitch.botany.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.garden.GardenSoil;
import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Every herb at every age (14 x 4 = 56 live plants), each on a moist herb bed, with a sign naming the
 * herb and a barrel of its seeds and fresh herbs. The plants keep growing after placement; the
 * signs describe the ages as placed.
 */
public final class HerbShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.herbs";
    public static final String AGES = "showcase." + HerbalWitch.MOD_ID + ".label.ages";
    public static final String SAMPLES = "showcase." + HerbalWitch.MOD_ID + ".label.samples";
    /** Sample stack size of seeds and fresh herbs in each barrel. */
    public static final int SAMPLE = 16;

    @Override
    public String id() {
        return "herbs";
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        for (int row = 0; row < HerbCatalog.ALL.size(); row++) {
            HerbSpec herb = HerbCatalog.ALL.get(row);
            int z = row * 2;
            out.add(ShowcasePlacement.label(0, z, Line.literal(herb.displayName()), Line.translated(AGES),
                    Line.translated(SAMPLES)));
            for (int age = 0; age <= HerbSpec.MAX_AGE; age++) {
                int x = 1 + age;
                int placedAge = age;
                out.add(ShowcasePlacement.content(x, 1, z, HerbShowcaseSection::moistBed));
                out.add(ShowcasePlacement.content(x, 2, z, () -> BotanyBlocks.plant(herb).getStateForAge(placedAge)));
            }
            out.add(ShowcasePlacement.content(5, 1, z, () -> Blocks.BARREL.defaultBlockState(), entity -> {
                if (entity instanceof Container barrel) {
                    barrel.setItem(0, new ItemStack(BotanyItems.seeds(herb), SAMPLE));
                    barrel.setItem(1, new ItemStack(BotanyItems.fresh(herb), SAMPLE));
                }
            }));
        }
        return out;
    }

    static BlockState moistBed() {
        return BotanyBlocks.herbBed().defaultBlockState().setValue(HerbBedBlock.MOISTURE, GardenSoil.MAX_MOISTURE);
    }
}
