package io.github.dirtyredz.herbalwitch.grimoire.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog.ResearchPiece;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The grimoire: both research blocks facing the entrance, a barrel with two portable grimoires and
 * the supplies of one preparation chain (fresh chamomile and yarrow to dry, grind and steep with
 * water over a lit hearth), and four signs on discovery, research, missing counts and teaching.
 * Building it grants nobody anything; taking the supplies is an ordinary acquisition.
 */
public final class GrimoireShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section.grimoire";
    public static final String SUPPLIES = label("supplies");
    /** The explanatory signs, each a heading line and a detail line. */
    public static final List<String> TOPICS = List.of("discovery", "research", "missing", "teaching");
    public static final int BOOKS = 2;

    public static String label(String topic) {
        return "showcase." + HerbalWitch.MOD_ID + ".label.grimoire." + topic;
    }

    @Override
    public String id() {
        return "grimoire";
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        int x = 0;
        for (ResearchPiece piece : GrimoireCatalog.RESEARCH_PIECES) {
            out.add(ShowcasePlacement.label(x, 1, Line.literal(piece.displayName())));
            out.add(ShowcasePlacement.content(x + 1, 1, 1, () -> GrimoireBlocks.block(piece).defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)));
            x += 3;
        }
        out.add(ShowcasePlacement.label(x, 1, Line.translated(SUPPLIES)));
        out.add(ShowcasePlacement.content(x + 1, 1, 1, () -> Blocks.BARREL.defaultBlockState(), entity -> {
            if (entity instanceof Container barrel) {
                List<Supplier<ItemStack>> supplies = supplies();
                for (int i = 0; i < supplies.size(); i++) {
                    barrel.setItem(i, supplies.get(i).get());
                }
            }
        }));
        x += 3;
        for (String topic : TOPICS) {
            out.add(ShowcasePlacement.label(x++, 0, Line.translated(label(topic)), Line.translated(label(topic) + ".detail")));
        }
        return out;
    }

    /** The barrel's contents, in slot order. */
    public static List<Supplier<ItemStack>> supplies() {
        return List.of(
                () -> new ItemStack(GrimoireItems.BOOK.get()),
                () -> new ItemStack(GrimoireItems.BOOK.get()),
                () -> new ItemStack(BotanyItems.fresh(HerbCatalog.CHAMOMILE), 8),
                () -> new ItemStack(BotanyItems.fresh(HerbCatalog.YARROW), 8),
                () -> PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER),
                () -> PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER),
                () -> new ItemStack(Items.COAL, 8));
    }
}
