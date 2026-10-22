package io.github.dirtyredz.herbalwitch.magic.charm.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Protective charms: a barrel of fresh charms and signs on wearing one (off hand, or a Curios slot
 * when Curios is present; one active charm, a finite charge) and on what they soften.
 */
public final class CharmShowcaseSection implements ShowcaseSection {
    public static final String ID = "charms";
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section." + ID;
    public static final List<String> TOPICS = List.of("wear", "soften");

    public static String label(String topic) {
        return "showcase." + HerbalWitch.MOD_ID + ".label.charms." + topic;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        out.add(ShowcasePlacement.label(0, 0, Line.translated(label("samples"))));
        out.add(ShowcasePlacement.content(0, 1, 1, () -> Blocks.BARREL.defaultBlockState(), entity -> {
            if (entity instanceof Container barrel) {
                for (int i = 0; i < CharmCatalog.ALL.size(); i++) {
                    barrel.setItem(i, new ItemStack(CharmItems.item(CharmCatalog.ALL.get(i))));
                }
            }
        }));
        for (int i = 0; i < TOPICS.size(); i++) {
            String topic = TOPICS.get(i);
            out.add(ShowcasePlacement.label(1 + i, 0, Line.translated(label(topic)), Line.translated(label(topic) + ".detail")));
        }
        return out;
    }
}
