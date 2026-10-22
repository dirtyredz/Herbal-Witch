package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;
import io.github.dirtyredz.herbalwitch.decor.grimoire.DecorEntries;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.Locale;
import java.util.Map;

/**
 * The English of the furniture reference pages (grimoire/content/reference/DecorEntries): one
 * editorial line per piece and the shared fact lines. Mechanical facts (size, seats, slots) are
 * filled in from the catalogue at reading time; only the prose lives here.
 */
public final class DecorReferenceTranslations {
    private static final Map<String, String> BLURBS = Map.ofEntries(
            Map.entry("armchair", "A deep velvet armchair under trailing ivy, for long evenings with a book."),
            Map.entry("dining_chair", "A tall-backed chair for the cottage table, cushioned in forest green."),
            Map.entry("bench", "A cushioned bench for two at the table or the door."),
            Map.entry("sofa", "Three can sit together on this rolled-arm sofa."),
            Map.entry("loveseat", "A two-seat sofa for a cosy corner."),
            Map.entry("chaise_lounge", "A long reclining seat for one, its back raised at the far end."),
            Map.entry("vanity_chair", "A small stool with a curved back for the vanity table."),
            Map.entry("desk_chair", "A sturdy study chair on a pedestal; it does not turn."),
            Map.entry("reading_chair", "A high wingback chair that keeps the draughts off a reader."),
            Map.entry("entry_bench", "A hall bench with hooks above and a shelf for boots below."),
            Map.entry("garden_bench", "Slatted dark wood on iron scrolls, for sitting among the herbs."),
            Map.entry("outdoor_chair", "The garden bench's single companion."),
            Map.entry("garden_swing", "A swinging seat for two under its own frame. It hangs still."),
            Map.entry("apothecary_counter", "The apothecary's chest, with a runner over the top. Selling from it comes later."),
            Map.entry("kitchen_counter", "A stone-topped worktop over drawers and doors."),
            Map.entry("corner_counter", "Turns a run of counters round a corner."),
            Map.entry("kitchen_island", "A butcher-block work table with a rack below and pans at hand."),
            Map.entry("dining_table", "A long planked table for six with a runner down its middle."),
            Map.entry("serving_cart", "A two-tier tea trolley."),
            Map.entry("work_table", "A heavy bench with a vice and drying herbs: a place to work, but not a station."),
            Map.entry("coffee_table", "A low table for teacups and books before the sofa."),
            Map.entry("side_table", "A small pedestal table beside a chair."),
            Map.entry("console_table", "A narrow hall table to stand against a wall."),
            Map.entry("tv_stand", "A low cabinet of glazed curios. Its drawers are for show."),
            Map.entry("vanity_table", "A dressing table with a framed mirror and little bottles. Its drawers are for show."),
            Map.entry("writing_desk", "A desk with ink, quill and paper. Its drawers are for show."),
            Map.entry("map_table", "A heavy chart table with a map laid out and instruments to read it."),
            Map.entry("garden_table", "A small slatted table on an iron foot."),
            Map.entry("small_wall_shelf", "A little wall shelf that shows the three things you set on it."),
            Map.entry("lower_cabinet", "A kitchen base cabinet; its doors hide a chest's worth of things."),
            Map.entry("upper_cabinet", "A glazed cupboard hung on the kitchen wall."),
            Map.entry("pantry", "A tall larder cupboard."),
            Map.entry("sideboard", "A long buffet of drawers and doors, holding two chests' worth."),
            Map.entry("china_cabinet", "A glazed hutch over a cupboard, holding two chests' worth."),
            Map.entry("wine_rack", "A lattice of bottle cubbies over a drawer; it keeps anything, not only bottles."),
            Map.entry("ingredient_cabinet", "An apothecary cabinet of many small labelled drawers."),
            Map.entry("open_shelves", "Three open shelves that show what you put on them."),
            Map.entry("potion_shelf", "A railed shelf to show off bottles and jars, one to a place."),
            Map.entry("nightstand", "A bedside table with a drawer and a door."),
            Map.entry("dresser", "A chest of drawers holding two chests' worth."),
            Map.entry("wardrobe", "A tall double-doored wardrobe holding two chests' worth."),
            Map.entry("trunk", "An iron-banded travelling trunk holding two chests' worth."),
            Map.entry("rolltop_desk", "A desk whose tambour hides two chests' worth of papers."),
            Map.entry("filing_cabinet", "Four drawers of labelled files."),
            Map.entry("shoe_cabinet", "A low cabinet with tilt-out doors for boots."),
            Map.entry("bookshelf", "An open bookcase that shows the books you shelve. It is not the Witch's Bookshelf."),
            Map.entry("linen_cabinet", "A tall linen press."),
            Map.entry("storage_cabinet", "A big double-doored cupboard holding two chests' worth."),
            Map.entry("wall_shelf_medium", "A long wall shelf that shows six things."),
            Map.entry("wall_shelf_large", "A tall run of wall shelves."),
            Map.entry("corner_shelf", "Shelves that fit into a corner."),
            Map.entry("free_standing_shelf_short", "A low open shelf."),
            Map.entry("free_standing_shelf_medium", "An open shelf of three boards."),
            Map.entry("free_standing_shelf_large", "A tall open shelf."),
            Map.entry("ladder_shelf", "Shelves on a leaning ladder."),
            Map.entry("table_lamp", "A shaded lamp to stand on a table."),
            Map.entry("wall_sconce", "A candle in a glass chimney on a carved plate."),
            Map.entry("hanging_lantern", "A lantern on a chain."),
            Map.entry("chandelier", "A wrought-iron ring of candles."),
            Map.entry("ceiling_light", "A brass dome set against the ceiling."),
            Map.entry("floor_lamp", "A tall lamp with a pleated shade."),
            Map.entry("post_lantern", "A lantern on a post, for the garden path."),
            Map.entry("pendant_light", "A glass globe on a long chain."),
            Map.entry("small_rug", "A little rug."),
            Map.entry("medium_rug", "A rug for a sitting area."),
            Map.entry("large_rug", "A rug for a whole room."),
            Map.entry("curtain", "Drapes on a rod, tied back."),
            Map.entry("room_divider", "A folding screen of three panels."),
            Map.entry("hanging_divider", "A fabric panel hung from a rod to part a room."),
            Map.entry("kitchen_sink", "A basin set in a cabinet, with a pump. It holds no water."),
            Map.entry("stove_oven", "A small iron range. It cooks nothing: the Witch's Stove is the working one."),
            Map.entry("fridge", "A wooden icebox. It keeps nothing cold."),
            Map.entry("drying_rack_decor", "Herbs drying on an upright frame, for looks: the Herb Drying Rack does the drying."),
            Map.entry("altar_table", "An altar of candles, crystals and a moon-marked drape. Its rites are still to come."),
            Map.entry("bed", "A carved cottage bed: sleep in it and it sets where you wake."),
            Map.entry("canopy_bed", "A wide bed under a draped canopy. One sleeps in it."),
            Map.entry("fireplace", "A cosy hearth with a mantel. It is cold decor: the Hearth burns and heats."),
            Map.entry("firepit", "A ring of stones round a fire you can light and put out. It does not heat."),
            Map.entry("mirror", "A tall framed mirror. It shows no reflection."),
            Map.entry("globe_stand", "A globe on a gilded meridian."),
            Map.entry("coat_rack", "A hall tree with a cloak and a witch's hat."),
            Map.entry("key_hooks", "A board of hooks with a couple of keys."),
            Map.entry("umbrella_stand", "A stand for an umbrella and a walking stick."),
            Map.entry("clawfoot_tub", "A clawfoot tub. It holds no water."),
            Map.entry("shower_stall", "A corner shower. It holds no water."),
            Map.entry("toilet", "An old-fashioned commode."),
            Map.entry("sink_vanity", "A washstand with a basin and jug."),
            Map.entry("towel_rack", "A brass rail with towels."),
            Map.entry("planter_box", "A planter of leaves and flowers. It grows nothing you can harvest."),
            Map.entry("trellis", "A lattice for climbing ivy."),
            Map.entry("decorative_well", "A roofed wishing well. It draws no water."),
            Map.entry("door", "A cottage door with an arched window; it opens like any door."),
            Map.entry("window", "A shuttered window panel to hang on a wall."),
            Map.entry("archway", "A stone arch to walk through."));

    private DecorReferenceTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            String blurb = BLURBS.get(spec.id());
            if (blurb == null) {
                throw new IllegalStateException("no reference line for " + spec.path());
            }
            lang.add(DecorEntries.blurb(spec), blurb);
        }
        lang.add(DecorEntries.SIZE, "Takes %s x %s x %s blocks (wide, deep, high).");
        lang.add(DecorEntries.CRAFTED, "Crafted at a crafting table.");
        lang.add(DecorEntries.LOOKALIKE, "It is furniture, not a working station.");
        lang.add(DecorEntries.FUNCTIONAL_ALTAR, "Only the Witch's Altar performs rituals; this table is decoration.");
        Map<Support, String> mounts = Map.of(Support.FREE, "Stands anywhere.", Support.FLOOR, "Stands on a solid floor.",
                Support.WALL, "Hangs on a solid wall, facing away from it.", Support.CEILING, "Hangs from a solid ceiling.",
                Support.CORNER, "Fits into a corner: a solid wall behind it and another on its left.",
                Support.CARPET, "Lies on any floor, like carpet.");
        mounts.forEach((support, text) -> lang.add("grimoire.herbalwitch.decor.mount." + support.name().toLowerCase(Locale.ROOT), text));
        Map<FurnitureBehavior, String> uses = Map.of(FurnitureBehavior.STATIC, "Decorative.",
                FurnitureBehavior.SEATING, "Use it to sit down: seats %s.",
                FurnitureBehavior.STORAGE, "Use it to open its %s slots; hoppers reach them too.",
                FurnitureBehavior.DISPLAY, "Use it to set out up to %s things, one to a place, and see them on show.",
                FurnitureBehavior.LIGHT, "Use it to light it or put it out.",
                FurnitureBehavior.BED, "Sleep in it at night; it sets where you wake.",
                FurnitureBehavior.DOOR, "Opens by hand or by redstone.",
                FurnitureBehavior.RUG, "Decorative.");
        uses.forEach((use, text) -> lang.add("grimoire.herbalwitch.decor.use." + use.name().toLowerCase(Locale.ROOT), text));
    }
}
