package io.github.dirtyredz.herbalwitch.data.decor.manifest;

import java.util.List;

/**
 * The canonical decor inventory, written independently of the mod's catalogue: the five 0.1.0
 * pieces and Milestone 5's 89 additions with their waves and footprints (width x depth x height in
 * cells) as the M5 plan lists them, plus their English names and art groups. Catalogue order.
 */
public final class DecorPieces {
    /** One piece. {@code group} is null for the five 0.1.0 pieces. */
    public record Piece(String wave, String family, String group, String id, String name, int width, int depth, int height) {
        public String path() {
            return "decor/" + family + "/" + id;
        }

        public String recipePath() {
            return group == null ? path() : "decor/" + family + "/" + group + "/" + id;
        }

        public int cells() {
            return id.equals("archway") ? 7 : width * depth * height;
        }
    }

    public static final List<Piece> ALL = List.of(
            piece("M1", "seating", null, "armchair", "Armchair", 1, 1, 1),
            piece("A", "seating", "chairs", "dining_chair", "Dining Chair", 1, 1, 1),
            piece("A", "seating", "benches", "bench", "Bench", 2, 1, 1),
            piece("B", "seating", "lounges", "sofa", "Sofa", 3, 1, 1),
            piece("B", "seating", "lounges", "loveseat", "Loveseat", 2, 1, 1),
            piece("B", "seating", "lounges", "chaise_lounge", "Chaise Lounge", 1, 2, 1),
            piece("B", "seating", "chairs", "vanity_chair", "Vanity Stool", 1, 1, 1),
            piece("B", "seating", "chairs", "desk_chair", "Desk Chair", 1, 1, 1),
            piece("B", "seating", "chairs", "reading_chair", "Reading Chair", 1, 1, 1),
            piece("B", "seating", "benches", "entry_bench", "Entry Bench", 2, 1, 1),
            piece("C", "seating", "benches", "garden_bench", "Garden Bench", 2, 1, 1),
            piece("C", "seating", "chairs", "outdoor_chair", "Garden Chair", 1, 1, 1),
            piece("C", "seating", "benches", "garden_swing", "Garden Swing", 3, 2, 3),
            piece("M1", "surfaces", null, "apothecary_counter", "Apothecary Counter", 1, 1, 1),
            piece("A", "surfaces", "kitchen", "kitchen_counter", "Kitchen Counter", 1, 1, 1),
            piece("A", "surfaces", "kitchen", "corner_counter", "Corner Counter", 1, 1, 1),
            piece("A", "surfaces", "kitchen", "kitchen_island", "Kitchen Island", 2, 1, 1),
            piece("A", "surfaces", "tables", "dining_table", "Dining Table", 3, 2, 1),
            piece("A", "surfaces", "kitchen", "serving_cart", "Serving Cart", 1, 1, 1),
            piece("A", "surfaces", "desks", "work_table", "Work Table", 2, 1, 1),
            piece("B", "surfaces", "tables", "coffee_table", "Coffee Table", 2, 1, 1),
            piece("B", "surfaces", "tables", "side_table", "Side Table", 1, 1, 1),
            piece("B", "surfaces", "tables", "console_table", "Console Table", 2, 1, 1),
            piece("B", "surfaces", "desks", "tv_stand", "Curio Cabinet", 2, 1, 1),
            piece("B", "surfaces", "desks", "vanity_table", "Vanity Table", 2, 1, 2),
            piece("B", "surfaces", "desks", "writing_desk", "Writing Desk", 2, 1, 1),
            piece("B", "surfaces", "tables", "map_table", "Map Table", 2, 2, 1),
            piece("C", "surfaces", "tables", "garden_table", "Garden Table", 1, 1, 1),
            piece("M1", "storage", null, "small_wall_shelf", "Small Wall Shelf", 1, 1, 1),
            piece("A", "storage", "cabinets", "lower_cabinet", "Lower Cabinet", 1, 1, 1),
            piece("A", "storage", "cabinets", "upper_cabinet", "Upper Cabinet", 1, 1, 1),
            piece("A", "storage", "cabinets", "pantry", "Pantry", 1, 1, 2),
            piece("A", "storage", "cabinets", "sideboard", "Sideboard", 2, 1, 1),
            piece("A", "storage", "cabinets", "china_cabinet", "China Cabinet", 2, 1, 2),
            piece("A", "storage", "cabinets", "wine_rack", "Wine Rack", 1, 1, 2),
            piece("A", "storage", "cabinets", "ingredient_cabinet", "Ingredient Cabinet", 1, 1, 2),
            piece("A", "storage", "shelves", "open_shelves", "Open Shelves", 1, 1, 2),
            piece("A", "storage", "shelves", "potion_shelf", "Potion Shelf", 1, 1, 2),
            piece("B", "storage", "bedroom", "nightstand", "Nightstand", 1, 1, 1),
            piece("B", "storage", "bedroom", "dresser", "Dresser", 2, 1, 1),
            piece("B", "storage", "bedroom", "wardrobe", "Wardrobe", 2, 1, 2),
            piece("B", "storage", "bedroom", "trunk", "Trunk", 2, 1, 1),
            piece("B", "storage", "study", "rolltop_desk", "Rolltop Desk", 2, 1, 2),
            piece("B", "storage", "study", "filing_cabinet", "Filing Cabinet", 1, 1, 2),
            piece("B", "storage", "bedroom", "shoe_cabinet", "Shoe Cabinet", 1, 1, 1),
            piece("B", "storage", "study", "bookshelf", "Bookshelf", 1, 1, 2),
            piece("C", "storage", "bedroom", "linen_cabinet", "Linen Cabinet", 1, 1, 2),
            piece("C", "storage", "study", "storage_cabinet", "Storage Cabinet", 2, 1, 2),
            piece("C", "storage", "shelves", "wall_shelf_medium", "Medium Wall Shelf", 2, 1, 1),
            piece("C", "storage", "shelves", "wall_shelf_large", "Large Wall Shelf", 2, 1, 2),
            piece("C", "storage", "shelves", "corner_shelf", "Corner Shelf", 1, 1, 2),
            piece("C", "storage", "shelves", "free_standing_shelf_short", "Short Standing Shelf", 1, 1, 1),
            piece("C", "storage", "shelves", "free_standing_shelf_medium", "Standing Shelf", 1, 1, 2),
            piece("C", "storage", "shelves", "free_standing_shelf_large", "Tall Standing Shelf", 1, 1, 3),
            piece("C", "storage", "shelves", "ladder_shelf", "Ladder Shelf", 1, 1, 2),
            piece("M1", "lighting", null, "table_lamp", "Table Lamp", 1, 1, 1),
            piece("C", "lighting", "lamps", "wall_sconce", "Wall Sconce", 1, 1, 1),
            piece("C", "lighting", "lamps", "hanging_lantern", "Hanging Lantern", 1, 1, 1),
            piece("C", "lighting", "lamps", "chandelier", "Chandelier", 2, 2, 1),
            piece("C", "lighting", "lamps", "ceiling_light", "Ceiling Light", 1, 1, 1),
            piece("C", "lighting", "lamps", "floor_lamp", "Floor Lamp", 1, 1, 2),
            piece("C", "lighting", "lamps", "post_lantern", "Post Lantern", 1, 1, 3),
            piece("C", "lighting", "lamps", "pendant_light", "Pendant Light", 1, 1, 2),
            piece("M1", "textiles", null, "small_rug", "Small Rug", 1, 1, 1),
            piece("C", "textiles", "rugs", "medium_rug", "Medium Rug", 2, 2, 1),
            piece("C", "textiles", "rugs", "large_rug", "Large Rug", 3, 3, 1),
            piece("C", "textiles", "drapes", "curtain", "Curtain", 2, 1, 2),
            piece("C", "textiles", "drapes", "room_divider", "Folding Screen", 3, 1, 2),
            piece("C", "textiles", "drapes", "hanging_divider", "Hanging Divider", 3, 1, 2),
            piece("A", "fixtures", "kitchen", "kitchen_sink", "Kitchen Sink", 1, 1, 1),
            piece("A", "fixtures", "kitchen", "stove_oven", "Kitchen Range", 1, 1, 1),
            piece("A", "fixtures", "kitchen", "fridge", "Icebox", 1, 1, 2),
            piece("A", "fixtures", "apothecary", "drying_rack_decor", "Decorative Drying Rack", 2, 1, 2),
            piece("A", "fixtures", "apothecary", "altar_table", "Altar Table", 2, 1, 1),
            piece("B", "fixtures", "bedroom", "bed", "Cottage Bed", 1, 2, 1),
            piece("B", "fixtures", "bedroom", "canopy_bed", "Canopy Bed", 2, 2, 3),
            piece("B", "fixtures", "hearth", "fireplace", "Fireplace", 2, 1, 2),
            piece("B", "fixtures", "hall", "mirror", "Mirror", 1, 1, 2),
            piece("B", "fixtures", "hall", "globe_stand", "Globe Stand", 1, 1, 1),
            piece("B", "fixtures", "hall", "coat_rack", "Coat Rack", 1, 1, 2),
            piece("B", "fixtures", "hall", "key_hooks", "Key Hooks", 1, 1, 1),
            piece("B", "fixtures", "hall", "umbrella_stand", "Umbrella Stand", 1, 1, 1),
            piece("C", "fixtures", "bathroom", "clawfoot_tub", "Clawfoot Tub", 1, 2, 1),
            piece("C", "fixtures", "bathroom", "shower_stall", "Shower Stall", 1, 1, 2),
            piece("C", "fixtures", "bathroom", "toilet", "Commode", 1, 1, 1),
            piece("C", "fixtures", "bathroom", "sink_vanity", "Washstand", 1, 1, 1),
            piece("C", "fixtures", "bathroom", "towel_rack", "Towel Rack", 1, 1, 1),
            piece("C", "fixtures", "garden", "planter_box", "Planter Box", 2, 1, 1),
            piece("C", "fixtures", "garden", "trellis", "Trellis", 1, 1, 2),
            piece("C", "fixtures", "garden", "decorative_well", "Wishing Well", 2, 2, 3),
            piece("C", "fixtures", "hearth", "firepit", "Firepit", 2, 2, 1),
            piece("C", "openings", "frames", "door", "Cottage Door", 1, 1, 2),
            piece("C", "openings", "frames", "window", "Cottage Window", 1, 1, 2),
            piece("C", "openings", "frames", "archway", "Archway", 3, 1, 3));

    private DecorPieces() {
    }

    private static Piece piece(String wave, String family, String group, String id, String name, int width, int depth, int height) {
        return new Piece(wave, family, group, id, name, width, depth, height);
    }
}
