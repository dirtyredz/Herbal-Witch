package io.github.dirtyredz.herbalwitch.magic.ritual.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.CircleLayout;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement.Line;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The ritual circle: a complete circle facing the entrance with an altar standing correctly inside it,
 * three herb beds beside it to bless, a supply barrel with both circle offerings, and signs on the
 * arrangement and the two circle rituals. Building it starts nothing and spends nothing.
 */
public final class CircleShowcaseSection implements ShowcaseSection {
    public static final String TITLE = "showcase." + HerbalWitch.MOD_ID + ".section." + CircleCatalog.SHOWCASE;
    public static final List<String> TOPICS = List.of("arrangement", "garden_blessing", "home_protection");
    /** The circle's origin (front-left cell) in section coordinates; it faces north, so it extends west and back. */
    public static final BlockPos ORIGIN = new BlockPos(4, 1, 2);
    public static final int BEDS = 3;

    public static String label(String topic) {
        return "showcase." + HerbalWitch.MOD_ID + ".label.circle." + topic;
    }

    @Override
    public String id() {
        return CircleCatalog.SHOWCASE;
    }

    @Override
    public String titleKey() {
        return TITLE;
    }

    @Override
    public List<ShowcasePlacement> placements() {
        List<ShowcasePlacement> out = new ArrayList<>();
        for (int i = 0; i < TOPICS.size(); i++) {
            String topic = TOPICS.get(i);
            out.add(ShowcasePlacement.label(i, 0, Line.translated(label(topic)), Line.translated(label(topic) + ".detail")));
        }
        for (int i = 0; i < CircleCatalog.LAYOUT.size(); i++) {
            int part = i;
            BlockPos at = ORIGIN.offset(CircleCatalog.LAYOUT.offset(Direction.NORTH, i));
            out.add(ShowcasePlacement.content(at.getX(), at.getY(), at.getZ(), () -> RitualBlocks.CIRCLE.get().defaultBlockState()
                    .setValue(RitualCircleBlock.FACING, Direction.NORTH).setValue(RitualCircleBlock.LAYOUT.part(), part)));
        }
        BlockPos altar = CircleLayout.altarOrigin(ORIGIN, Direction.NORTH);
        for (int i = 0; i < AltarCatalog.LAYOUT.size(); i++) {
            int part = i;
            BlockPos at = altar.offset(AltarCatalog.LAYOUT.offset(Direction.NORTH, i));
            out.add(ShowcasePlacement.content(at.getX(), at.getY(), at.getZ(), () -> RitualBlocks.ALTAR.get().defaultBlockState()
                    .setValue(AltarBlock.FACING, Direction.NORTH).setValue(AltarBlock.LAYOUT.part(), part)));
        }
        for (int i = 0; i < BEDS; i++) {
            out.add(ShowcasePlacement.content(6, 1, 2 + i, () -> BotanyBlocks.HERB_BED.get().defaultBlockState()));
        }
        out.add(ShowcasePlacement.label(TOPICS.size(), 0, Line.translated(label("supplies"))));
        out.add(ShowcasePlacement.content(6, 1, 6, () -> Blocks.BARREL.defaultBlockState(), entity -> {
            if (entity instanceof Container barrel) {
                List<Supplier<ItemStack>> supplies = supplies();
                for (int i = 0; i < supplies.size(); i++) {
                    barrel.setItem(i, supplies.get(i).get());
                }
            }
        }));
        return out;
    }

    /** Both circle offerings, twice. */
    public static List<Supplier<ItemStack>> supplies() {
        return List.of(
                () -> new ItemStack(PreparationItems.powder(HerbCatalog.NETTLE), 2),
                () -> new ItemStack(Items.BONE_MEAL, 2),
                () -> new ItemStack(PreparationItems.product(EverydayProductCatalog.SOIL_TONIC), 2),
                () -> new ItemStack(PreparationItems.powder(HerbCatalog.SAGE), 2),
                () -> new ItemStack(Items.AMETHYST_SHARD, 2));
    }
}
