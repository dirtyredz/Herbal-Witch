package io.github.dirtyredz.herbalwitch.magic.ward.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Home wards: an uncharged ward stone (building it binds nobody, charges nothing and protects nothing)
 * and signs on activating one, its boundary and focus, and what it does and does not do.
 */
public final class WardShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section." + WardCatalog.SHOWCASE;
    public static final List<String> TOPICS = List.of("activate", "boundary", "protection");

    public static String label(String topic) {
        return "showcase." + HerbalWitch.MOD_ID + ".label.ward." + topic;
    }

    @Override
    public String id() {
        return WardCatalog.SHOWCASE;
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        out.add(ShowcasePlacement.content(0, 1, 1, () -> WardRegistration.STONE.get().defaultBlockState()
                .setValue(WardStoneBlock.FACING, Direction.NORTH)));
        for (int i = 0; i < TOPICS.size(); i++) {
            String topic = TOPICS.get(i);
            out.add(ShowcasePlacement.label(2 + i, 0, Line.translated(label(topic)), Line.translated(label(topic) + ".detail")));
        }
        return out;
    }
}
