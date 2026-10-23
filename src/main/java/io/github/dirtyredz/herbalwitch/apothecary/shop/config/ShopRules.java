package io.github.dirtyredz.herbalwitch.apothecary.shop.config;

/**
 * One validated, complete commerce configuration. Fixed limits (27 stock slots, 12 offers, 32 bans,
 * 128 history entries, 4 KiB item NBT, a 512 KiB shard, one outstanding transaction per shop/player,
 * four-block reach, 200-tick link sessions) are constants, not settings. Lowering a limit never
 * deletes stock, balances or records; it only blocks additions.
 */
public record ShopRules(int shopsPerOwner, int shopsPerWorld, long proceedsCapacity, boolean npcService, int economyWindow,
                        int worldSpending, int ownerSpending, int shopSpending, int villagerPurchases, int visitInterval,
                        int visitDuration, int concurrentVisits, int orderLifetimeWindows) {
    public static final int STOCK_SLOTS = 27;
    public static final int OFFERS = 12;
    public static final int REACH = 4;
    public static final int LINK_RANGE = 16;
    public static final int LINK_SESSION_TICKS = 200;
    public static final int ITEM_NBT_BYTES = 4096;
    public static final int ITEM_NBT_DEPTH = 8;
    public static final int ORDERS_PER_CUSTOMER = 3;
    public static final int ORDERS_PER_SHOP = 16;
    public static final int ORDER_COOLDOWN = 200;
    public static final int CUSTOMER_RANGE = 16;
    public static final int ARRIVAL_DISTANCE = 2;
    public static final int VISITS_PER_SHOP = 1;
    public static final int EVALUATIONS_PER_TICK = 4;
    public static final int PATHS_PER_TICK = 1;
    public static final int CANDIDATES_PER_EVALUATION = 16;
    public static final int MAX_DEMAND_IDENTITIES = 1024;
    public static final int MAX_DEMAND_VILLAGERS = 4096;

    public static final ShopRules DEFAULTS = new ShopRules(4, 1024, 1_000_000L, true, 24_000, 128, 32, 16, 2, 1200, 200, 16, 7);

    public ShopRules {
        bound(shopsPerOwner, 1, 16);
        bound(shopsPerWorld, 1, 4096);
        if (proceedsCapacity < 64 || proceedsCapacity > 1_000_000L) throw new IllegalArgumentException("proceedsCapacity outside 64..1000000");
        bound(economyWindow, 24_000, 168_000);
        bound(worldSpending, 0, 1024);
        bound(ownerSpending, 0, 128);
        bound(shopSpending, 0, 64);
        bound(villagerPurchases, 0, 4);
        bound(visitInterval, 200, 24_000);
        bound(visitDuration, 40, 600);
        bound(concurrentVisits, 1, 32);
        bound(orderLifetimeWindows, 1, 28);
    }

    private static void bound(int value, int min, int max) {
        if (value < min || value > max) throw new IllegalArgumentException("Commerce rule " + value + " outside " + min + ".." + max);
    }

    /** Order lifetime in running server ticks. */
    public long orderLifetime() {
        return (long) orderLifetimeWindows * economyWindow;
    }
}
