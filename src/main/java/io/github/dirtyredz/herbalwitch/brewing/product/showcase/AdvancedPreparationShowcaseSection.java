package io.github.dirtyredz.herbalwitch.brewing.product.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

/**
 * Milestone 7's preparations: one sample barrel each for the player preparations (drinks, vials, oils,
 * blinding dust, moon essence), the familiar preparations (whose use arrives with familiar bonding) and
 * the cauldron mixtures, with a sign explaining each. Samples only: nothing is applied or thrown.
 */
public final class AdvancedPreparationShowcaseSection implements ShowcaseSection {
    public static final String ID = "advanced_preparations";
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section." + ID;
    /** The barrels, in order: label topic and contents. */
    public static final List<String> BARRELS = List.of("player", "familiar", "mixtures");

    public static String label(String topic) {
        return "showcase." + HerbalWitch.MOD_ID + ".label.advanced_preparations." + topic;
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
        for (int i = 0; i < BARRELS.size(); i++) {
            String topic = BARRELS.get(i);
            int x = i * 2;
            out.add(ShowcasePlacement.label(x, 0, Line.translated(label(topic)), Line.translated(label(topic) + ".detail")));
            out.add(ShowcasePlacement.content(x, 1, 1, () -> Blocks.BARREL.defaultBlockState(), entity -> {
                if (entity instanceof Container barrel) {
                    List<ProductSpec> specs = contents(topic);
                    for (int s = 0; s < specs.size(); s++) {
                        barrel.setItem(s, new ItemStack(AdvancedPreparations.item(specs.get(s)), 2));
                    }
                }
            }));
        }
        return out;
    }

    /** What a barrel holds (each fits one 27-slot barrel). */
    public static List<ProductSpec> contents(String topic) {
        return switch (topic) {
            case "player" -> AdvancedProductCatalog.ALL;
            case "familiar" -> FamiliarPreparationCatalog.ALL;
            default -> MixtureCatalog.specs();
        };
    }
}
