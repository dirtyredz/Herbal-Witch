package io.github.dirtyredz.herbalwitch.magic.ritual.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The functional altar: a complete idle altar facing the entrance, a supply barrel with everything a
 * visitor needs to try it (the founding offering including a valid signed book, a second set for an
 * initiation, sage for attunement, a grimoire, energy tea and restful infusions), and signs on using
 * the altar, founding, initiation, recovery and practice. Building it initiates nobody, touches no
 * founding record, grants no energy and starts no ritual; taking supplies and using the altar follow
 * the ordinary rules.
 */
public final class AltarShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section." + AltarCatalog.SHOWCASE;
    /** The explanatory signs after the altar's own two, each a heading line and a detail line. */
    public static final List<String> TOPICS = List.of("founding", "initiation", "recovery", "practice");
    public static final String USE = label("use");
    public static final String SUPPLIES = label("supplies");

    public static String label(String topic) {
        return "showcase." + HerbalWitch.MOD_ID + ".label.altar." + topic;
    }

    @Override
    public String id() {
        return AltarCatalog.SHOWCASE;
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        // the name sign stands beside the altar, which faces north; its other column is to the viewer's right
        // (west), so the origin stands at x = 2 and the sign at x = 0; the notes about it stand in the front row
        // beyond the barrel, with nothing behind them
        out.add(ShowcasePlacement.label(0, 1, Line.literal(AltarCatalog.NAME)));
        out.add(ShowcasePlacement.label(7, 0, Line.translated(USE), Line.translated(USE + ".detail")));
        for (int i = 0; i < AltarCatalog.LAYOUT.size(); i++) {
            int part = i;
            BlockPos at = AltarCatalog.LAYOUT.offset(Direction.NORTH, i);
            out.add(ShowcasePlacement.content(2 + at.getX(), 1 + at.getY(), 1 + at.getZ(), () -> RitualBlocks.ALTAR.get().defaultBlockState()
                    .setValue(AltarBlock.FACING, Direction.NORTH).setValue(AltarBlock.LAYOUT.part(), part)));
        }
        out.add(ShowcasePlacement.label(8, 0, Line.translated(label("founding")), Line.translated(label("founding") + ".detail")));
        out.add(ShowcasePlacement.label(4, 1, Line.translated(SUPPLIES)));
        out.add(ShowcasePlacement.content(5, 1, 1, () -> Blocks.BARREL.defaultBlockState(), entity -> {
            if (entity instanceof Container barrel) {
                List<Supplier<ItemStack>> supplies = supplies();
                for (int i = 0; i < supplies.size(); i++) {
                    barrel.setItem(i, supplies.get(i).get());
                }
            }
        }));
        int x = 9;
        for (String topic : TOPICS.subList(1, TOPICS.size())) {
            out.add(ShowcasePlacement.label(x++, 0, Line.translated(label(topic)), Line.translated(label(topic) + ".detail")));
        }
        return out;
    }

    /** The supply barrel's contents, in slot order. */
    public static List<Supplier<ItemStack>> supplies() {
        return List.of(
                () -> new ItemStack(BotanyItems.fresh(HerbCatalog.LAVENDER), 2),
                () -> new ItemStack(BotanyItems.fresh(HerbCatalog.SAGE), 4),
                () -> new ItemStack(BotanyItems.fresh(HerbCatalog.MINT), 1),
                () -> new ItemStack(BotanyItems.fresh(HerbCatalog.CHAMOMILE), 1),
                () -> new ItemStack(Items.AMETHYST_SHARD, 2),
                AltarShowcaseSection::sampleBook,
                () -> new ItemStack(GrimoireItems.BOOK.get()),
                () -> new ItemStack(PreparationItems.product(EverydayProductCatalog.ENERGY_TEA), 2),
                () -> new ItemStack(PreparationItems.product(EverydayProductCatalog.RESTFUL_INFUSION), 2));
    }

    /** A normally valid signed book for the founding offering (its words are never read). */
    public static ItemStack sampleBook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", "A Lineage Begins");
        tag.putString("author", "The Herbal Witch");
        ListTag pages = new ListTag();
        pages.add(StringTag.valueOf("{\"text\":\"Here the first witch of this world wrote her name.\"}"));
        tag.put("pages", pages);
        tag.putBoolean("resolved", true);
        return book;
    }
}
