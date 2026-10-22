package io.github.dirtyredz.herbalwitch.data.server.decor.recipes;

import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.FixturesCatalog;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipes.grid;

/**
 * Fixture recipes: iron, glass, stone and copper fittings in dark oak; quartz for porcelain; beds
 * from a vanilla bed; the firepit from a campfire. None of them makes a working station.
 */
public final class FixturesRecipes {
    private static final Object P = Items.DARK_OAK_PLANKS;
    private static final Object S = Items.DARK_OAK_SLAB;
    private static final Object T = Items.STICK;

    private FixturesRecipes() {
    }

    public static void addTo(Consumer<FinishedRecipe> writer) {
        grid(writer, FixturesCatalog.KITCHEN_SINK, "CBC/PPP/PPP", 'C', Items.COPPER_INGOT, 'B', Items.BUCKET, 'P', P);
        grid(writer, FixturesCatalog.STOVE_OVEN, "NIN/IFI/III", 'N', Items.GOLD_NUGGET, 'I', Items.IRON_INGOT, 'F', Items.FURNACE);
        grid(writer, FixturesCatalog.FRIDGE, "PIP/PKP/PIP", 'P', P, 'I', Items.IRON_INGOT, 'K', Items.PACKED_ICE);
        grid(writer, FixturesCatalog.DRYING_RACK_DECOR, "TRT/TRT/S S", 'T', T, 'R', Items.STRING, 'S', S);
        grid(writer, FixturesCatalog.ALTAR_TABLE, "CGC/SSS/P P", 'C', Items.CANDLE, 'G', Items.GOLD_INGOT, 'S', S, 'P', P);
        grid(writer, FixturesCatalog.BED, "T T/PBP", 'T', T, 'P', P, 'B', ItemTags.BEDS);
        grid(writer, FixturesCatalog.CANOPY_BED, "TWT/T T/TBT", 'T', T, 'W', ItemTags.WOOL, 'B', ItemTags.BEDS);
        grid(writer, FixturesCatalog.FIREPLACE, "BBB/B B/BLB", 'B', Items.BRICKS, 'L', Items.DARK_OAK_LOG);
        grid(writer, FixturesCatalog.MIRROR, "PGP/PIP/PGP", 'P', P, 'G', Items.GLASS, 'I', Items.IRON_INGOT);
        grid(writer, FixturesCatalog.GLOBE_STAND, "GCG/ T /SSS", 'G', Items.GOLD_NUGGET, 'C', Items.COMPASS, 'T', T, 'S', S);
        grid(writer, FixturesCatalog.COAT_RACK, "TTT/ T / S ", 'T', T, 'S', S);
        grid(writer, FixturesCatalog.KEY_HOOKS, "NNN/SSS", 'N', Items.IRON_NUGGET, 'S', S);
        grid(writer, FixturesCatalog.UMBRELLA_STAND, "C C/C C/CCC", 'C', Items.COPPER_INGOT);
        grid(writer, FixturesCatalog.CLAWFOOT_TUB, "Q Q/QQQ/G G", 'Q', Items.QUARTZ, 'G', Items.GOLD_NUGGET);
        grid(writer, FixturesCatalog.SHOWER_STALL, "IBI/G G/QQQ", 'I', Items.IRON_INGOT, 'B', Items.BUCKET, 'G', Items.GLASS_PANE, 'Q', Items.QUARTZ);
        grid(writer, FixturesCatalog.TOILET, "P  /QQQ/ Q ", 'P', P, 'Q', Items.QUARTZ);
        grid(writer, FixturesCatalog.SINK_VANITY, "QBQ/PPP/P P", 'Q', Items.QUARTZ, 'B', Items.BUCKET, 'P', P);
        grid(writer, FixturesCatalog.TOWEL_RACK, "NWN", 'N', Items.IRON_NUGGET, 'W', ItemTags.WOOL);
        grid(writer, FixturesCatalog.PLANTER_BOX, "PDP/PPP", 'P', P, 'D', Items.DIRT);
        grid(writer, FixturesCatalog.TRELLIS, "T T/ T /T T", 'T', T);
        grid(writer, FixturesCatalog.DECORATIVE_WELL, "SSS/T T/XBX", 'S', S, 'T', T, 'X', Items.COBBLESTONE, 'B', Items.BUCKET);
        grid(writer, FixturesCatalog.FIREPIT, " C /XXX", 'C', Items.CAMPFIRE, 'X', Items.COBBLESTONE);
    }
}
