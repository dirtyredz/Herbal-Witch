package io.github.dirtyredz.herbalwitch.apothecary.shop.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec.Mount;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The four commerce pieces (Milestone 11) and the existing decor counter they build on. Identities are
 * permanent once a world uses them. No currency item, deed, customer entity or profession exists.
 */
public final class ShopPieceCatalog {
    /** The existing furniture piece a register stands on (and that linked counters reuse). */
    public static final String COUNTER_PATH = "decor/surfaces/apothecary_counter";
    public static final String SHOWCASE = "shop";

    public static final ShopPieceSpec REGISTER = new ShopPieceSpec("register", "Shop Register", Mount.COUNTER_TOP, 0,
            ordered("dark_oak_planks", 4, "iron_ingot", 2, "gold_ingot", 1, "paper", 1, "redstone", 1));
    public static final ShopPieceSpec STOCK_SHELF = new ShopPieceSpec("stock_shelf", "Stock Shelf", Mount.WALL, 3,
            ordered("dark_oak_slab", 3, "stick", 2, "gold_nugget", 1));
    public static final ShopPieceSpec STOCK_JAR = new ShopPieceSpec("stock_jar", "Stock Jar", Mount.SURFACE, 1,
            ordered("glass", 3, "dark_oak_slab", 1, "gold_nugget", 1));
    public static final ShopPieceSpec ORDER_BOARD = new ShopPieceSpec("order_board", "Order Board", Mount.WALL, 0,
            ordered("dark_oak_planks", 3, "paper", 2, "ink_sac", 1));

    public static final List<ShopPieceSpec> ALL = List.of(REGISTER, STOCK_SHELF, STOCK_JAR, ORDER_BOARD);
    public static final List<ShopPieceSpec> DISPLAYS = List.of(STOCK_SHELF, STOCK_JAR);

    private ShopPieceCatalog() {
    }

    private static Map<String, Integer> ordered(Object... pairs) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) out.put((String) pairs[i], (Integer) pairs[i + 1]);
        return out;
    }
}
