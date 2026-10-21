package io.github.dirtyredz.herbalwitch.decor.catalog;

import io.github.dirtyredz.herbalwitch.decor.catalog.Finish.FrameMaterial;
import io.github.dirtyredz.herbalwitch.decor.catalog.Finish.Palette;
import net.minecraft.world.level.material.MapColor;

import java.util.List;
import java.util.stream.Stream;

/**
 * The authoritative list of the 18 coordinated finishes, in creative-tab order, from the concept
 * sheet's "Color & Wood Options". Registration, models, colours, names and recipes all iterate this
 * list; nothing else enumerates finishes. Each finish has exactly one fabric colour: this is not a
 * wood x upholstery cross-product.
 */
public final class FinishCatalog {
    public static final List<Finish> NATURAL = List.of(
            natural("dark_oak", "Dark Oak", 0x5A3A22, 0x6B3B6E, MapColor.COLOR_BROWN),
            natural("walnut", "Walnut", 0x6E4A30, 0x3F5A36, MapColor.DIRT),
            natural("cherry", "Cherry", 0x9A4F3F, 0xE8DCC0, MapColor.TERRACOTTA_WHITE),
            natural("rosewood", "Rosewood", 0x7A3430, 0xC48A8E, MapColor.NETHER),
            natural("pale_oak", "Pale Oak", 0xE0D2B4, 0x9AAE8C, MapColor.QUARTZ),
            natural("spruce", "Spruce", 0x7A5634, 0x7A2E3A, MapColor.PODZOL),
            natural("birch", "Birch", 0xE2CC94, 0xE3B1B1, MapColor.SAND));

    public static final List<Finish> PAINTED = List.of(
            painted("black", "Black", FrameMaterial.PAINTED_WOOD, 0x34302F, 0xC98A97, MapColor.COLOR_BLACK),
            painted("stone", "Stone", FrameMaterial.CUT_STONE, 0xB4AFA6, 0x5F7A45, MapColor.STONE),
            painted("moss", "Moss", FrameMaterial.PAINTED_WOOD, 0x667A46, 0xE6DBC0, MapColor.COLOR_GREEN),
            painted("sage_green", "Sage Green", FrameMaterial.PAINTED_WOOD, 0x98AE92, 0xE8CFC4, MapColor.TERRACOTTA_LIGHT_GREEN),
            painted("forest_green", "Forest Green", FrameMaterial.PAINTED_WOOD, 0x36553A, 0xB86E7A, MapColor.TERRACOTTA_GREEN),
            painted("plum", "Plum", FrameMaterial.PAINTED_WOOD, 0x633566, 0xE2A9B4, MapColor.COLOR_PURPLE),
            painted("burgundy", "Burgundy", FrameMaterial.PAINTED_WOOD, 0x76283A, 0xE7D9BC, MapColor.TERRACOTTA_RED),
            painted("dusty_pink", "Dusty Pink", FrameMaterial.PAINTED_WOOD, 0xCC949C, 0xA1B394, MapColor.TERRACOTTA_PINK),
            painted("blush", "Blush", FrameMaterial.PAINTED_WOOD, 0xEDC4C0, 0x6F3C6A, MapColor.COLOR_PINK),
            painted("cream", "Cream", FrameMaterial.PAINTED_WOOD, 0xEFE6D0, 0x46613E, MapColor.TERRACOTTA_WHITE),
            painted("lilac", "Lilac", FrameMaterial.PAINTED_WOOD, 0xB09ABE, 0xEEE4D0, MapColor.COLOR_MAGENTA));

    public static final List<Finish> ALL = Stream.concat(NATURAL.stream(), PAINTED.stream()).toList();

    /** The finish every base crafting recipe produces; the stonecutter restyles from there. */
    public static final Finish BASE = NATURAL.get(0);

    private FinishCatalog() {
    }

    private static Finish natural(String id, String name, int frame, int fabric, MapColor map) {
        return new Finish(id, name, Palette.NATURAL, FrameMaterial.WOOD_GRAIN, frame, fabric, map);
    }

    private static Finish painted(String id, String name, FrameMaterial material, int frame, int fabric, MapColor map) {
        return new Finish(id, name, Palette.PAINTED, material, frame, fabric, map);
    }
}
