package io.github.dirtyredz.herbalwitch.decor.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseLayout.Slot;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSamples.Sample;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseGroup;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Every delivered furniture piece in a footprint-aware grid ({@link DecorShowcaseLayout}): a sign beside
 * each (to its west), the piece facing the entrance (north) with the walls and ceilings it hangs from,
 * lights lit, storage stocked from {@link #stock}, and the door's alternate states beside it
 * ({@link DecorShowcaseSamples}).
 */
public final class DecorShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.decor";

    private final Consumer<BlockEntity> stock;

    /** The section with its storage samples stocked by {@code stock} (run once per storage origin after placement). */
    public DecorShowcaseSection(Consumer<BlockEntity> stock) {
        this.stock = stock;
    }

    /** The section without stock (planning tests). */
    public DecorShowcaseSection() {
        this(entity -> {
        });
    }

    @Override
    public String id() {
        return "decor";
    }

    @Override
    public ShowcaseGroup group() {
        return ShowcaseGroup.FURNITURE;
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<Sample> samples = DecorShowcaseSamples.samples();
        List<Slot> slots = DecorShowcaseLayout.pack(samples.stream().map(Sample::footprint).toList());
        List<ShowcasePlacement> out = new ArrayList<>();
        for (int i = 0; i < samples.size(); i++) {
            out.addAll(DecorShowcaseSamples.place(samples.get(i), slots.get(i), stock));
        }
        return out;
    }
}
