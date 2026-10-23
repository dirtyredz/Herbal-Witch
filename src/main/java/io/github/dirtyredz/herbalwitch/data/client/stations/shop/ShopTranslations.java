package io.github.dirtyredz.herbalwitch.data.client.stations.shop;

import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec;
import net.minecraftforge.common.data.LanguageProvider;

/** Commerce English: piece names, the screen, every result code, the guide pages and the showcase. */
public final class ShopTranslations {
    private ShopTranslations() {
    }

    private static void all(LanguageProvider out, String prefix, String... pairs) {
        for (int i = 0; i < pairs.length; i += 2) out.add(prefix + pairs[i], pairs[i + 1]);
    }

    public static void addTo(LanguageProvider out) {
        for (ShopPieceSpec spec : ShopPieceCatalog.ALL) out.add("block.herbalwitch.shop." + spec.key(), spec.name());
        out.add("container.herbalwitch.shop", "Shop");
        results(out);
        ui(out);
        guide(out);
        showcase(out);
    }

    private static void results(LanguageProvider out) {
        all(out, "shop.herbalwitch.result.",
                "refreshed", "Up to date.", "created", "Shop established. It starts closed.", "reconnected", "Shop reconnected under a new binding.",
                "retired", "Shop retired. Its identity is kept as a record.", "saved", "Saved.", "purchased", "Bought.",
                "deposited", "Stocked.", "withdrawn", "Taken from stock.", "proceeds_withdrawn", "Emeralds taken from proceeds.",
                "linked", "Fixture linked.", "unlinked", "Fixture unlinked.",
                "link_armed_counter", "Click a counter within 16 blocks to link it.", "link_armed_display", "Click a stock shelf or jar to link it.",
                "link_armed_board", "Click an order board to link it.", "link_armed_unlink", "Click a linked fixture to unlink it.",
                "order_created", "Request posted. Nothing was paid or reserved.", "order_cancelled", "Request cancelled.",
                "order_declined", "Request declined.", "order_offered", "Offer suggested for the request.",
                "unavailable", "This shop is unavailable right now.", "stale", "The shop changed; look again.",
                "recovery", "An interrupted transfer is waiting for recovery. Nothing more can move until it is resolved.",
                "receipt_pending", "Your last purchase is still being settled.", "inventory_changed", "Your inventory changed; try again.",
                "bounds", "That would exceed the shop record's limits.", "actor", "Only a player in survival or adventure with an empty cursor can trade.",
                "own_shop", "You cannot buy from your own shop.", "banned", "This shop does not sell to you.", "closed", "The shop is closed.",
                "stale_offer", "That offer changed. Check the new price before buying.", "order_stale", "That request changed.",
                "not_tradeable", "That cannot be traded.", "none", "Done.", "disabled", "That offer is not for sale.", "sold_out", "Sold out.",
                "payment", "Not enough emeralds in your inventory.", "inventory_full", "Make room in your inventory first.",
                "proceeds_full", "The shop's proceeds are full.", "no_room", "No room in stock.", "amount", "Choose a valid amount.",
                "not_owner", "Only the shop's owner can do that.", "product_empty", "Choose a stack first.",
                "product_not_tradeable", "That item cannot be sold in a shop.", "product_metadata", "That stack carries data a shop cannot keep exactly.",
                "product_capability", "That stack carries attached data a shop cannot keep.", "product_damaged", "Damaged items cannot be sold.",
                "indexing", "Shops are still loading.", "not_witch", "Only an initiated witch can establish a shop.",
                "name", "Use 1 to 32 plain characters.", "already_bound", "This register already serves a shop.",
                "owner_limit", "You already have the most shops allowed.", "world_limit", "This world has the most shops allowed.",
                "not_detached", "That shop is not detached.", "retire_not_empty", "Empty the stock, proceeds and active requests first.",
                "no_register", "The register is missing or not loaded.", "no_stocked_offer", "Add at least one enabled, stocked offer first.",
                "already_open", "Already open.", "not_open", "Not open.", "player_unknown", "No online player has that name.",
                "ban_self", "You cannot ban yourself.", "already_banned", "Already banned.", "ban_limit", "The ban list is full.",
                "not_banned", "Not banned.", "no_sample", "Pick a stock slot with the product.", "lot", "Lot must fit one stack (at most 64).",
                "price", "Price must be 1 to 64 emeralds.", "offer_limit", "A shop holds at most 12 offers.", "stale_link", "That fixture changed.",
                "link_expired", "Linking timed out.", "link_dimension", "Link fixtures in the register's dimension, with the register loaded.",
                "link_range", "That is more than 16 blocks from the register.", "permission", "You may not change that place.",
                "link_ordinary_shelf", "Ordinary shelves keep their own storage and cannot become shop displays.",
                "link_wrong_target", "That is not the kind of fixture you chose.", "link_taken", "That fixture already belongs to a shop.",
                "link_demonstration", "Showcase displays are demonstrations.", "link_limit", "This shop has the most fixtures allowed.",
                "link_missing", "That is not linked to this shop.", "order_quantity", "Request 1 to 64.", "order_price", "The price ceiling must be 1 to 64.",
                "order_customer_limit", "You already have three active requests here.", "order_shop_limit", "This shop has sixteen active requests.",
                "order_cooldown", "Wait a moment before posting another request.", "order_state", "That request is no longer active.",
                "order_owner", "That request is not yours.", "order_incompatible", "That offer does not match the request.",
                "rate", "Slow down.", "stale_request", "That request was out of order and was ignored.", "session", "This menu is no longer valid.",
                "sight", "You need to see the fixture.", "unbound", "Establish or reconnect a shop first.",
                "fixture_unlinked", "This fixture is not linked to a shop.", "break_not_owner", "Only the shop's owner can remove this.");
    }

    private static void ui(LanguageProvider out) {
        all(out, "shop.herbalwitch.ui.",
                "tab.stock", "Stock", "tab.offers", "Offers", "tab.settings", "Shop", "tab.history", "Ledger", "tab.orders", "Requests",
                "tab.browse", "Browse", "tab.create", "Create",
                "create", "Create shop", "reconnect", "Reconnect", "unbound", "This register serves no shop yet.",
                "create_hint", "Name your shop. It starts closed.", "create_witch_only", "Only an initiated witch can establish a shop.",
                "owned", "Your shops: %s of %s", "detached", "Your detached shops:",
                "buy", "Buy", "heading", "%s - kept by %s", "no_offers", "Nothing on offer.", "lot", "%sx %s", "price", "%s emerald(s)",
                "state.open", "Open", "state.closed", "Closed", "state.detached", "No register", "state.quarantined", "Under recovery",
                "state.retired", "Retired", "state.recovery", "Under recovery", "state.unavailable", "Register unloaded",
                "problem.unavailable", "This shop is unavailable.", "problem.unreadable", "This shop's record could not be read and is preserved.",
                "problem.unsupported", "This shop's record is from a newer version and is preserved.",
                "problem.write_failed", "This shop's record could not be saved; it is read-only until restart.",
                "withdraw", "Take", "deposit", "Stock", "take", "Take", "stock_heading", "%s - stock", "inventory", "Your inventory (main and hotbar)",
                "from_stock", "From stock:", "to_stock", "Into stock:", "proceeds", "Proceeds %s/%s",
                "offers_heading", "Offers %s of %s", "offer_detail", "%s emerald(s), %s in stock, %s", "enabled", "Enabled", "disabled", "Disabled",
                "add", "Add", "save", "Save", "remove", "Remove", "offer_editor", "Stock slot, lot, price:",
                "offer_fields", "Stock slot (1-27), items per lot, price in emeralds",
                "open", "Open", "close", "Close", "public_on", "Public: on", "public_off", "Public: off", "villagers_on", "NPC: on",
                "villagers_off", "NPC: off", "rename", "Rename", "ban", "Ban", "unban", "Unban", "retire", "Retire",
                "link_counter", "Counter", "link_display", "Display", "link_board", "Board", "link_unlink", "Unlink",
                "unlink", "Drop", "slot", "#%s", "none", "(none)", "assign", "Show", "settings_heading", "Shop: %s",
                "lifecycle.open", "open", "lifecycle.closed", "closed", "lifecycle.detached", "detached", "lifecycle.quarantined", "under recovery",
                "lifecycle.retired", "retired", "bans", "Bans: %s", "links", "Fixtures: %s",
                "kind.counter", "Counter", "kind.shelf", "Shelf", "kind.jar", "Jar", "kind.board", "Board",
                "history_heading", "Ledger, page %s of %s", "recovery_line", "%s %s %s: %s",
                "history.sale", "%1$s bought %2$sx %3$s for %4$s", "history.npc_sale", "%1$s bought %2$sx %3$s for %4$s",
                "history.deposit", "%1$s stocked %2$sx %3$s", "history.withdraw", "%1$s took %2$sx %3$s", "history.proceeds", "%1$s took %4$s emeralds",
                "history.recovery_finalized", "Recovered transfer settled", "history.recovery_rolled_back", "Interrupted transfer returned to the shop",
                "history.recovery_discarded", "%1$s discarded a reservation (%2$sx %3$s, %4$s emeralds lost)",
                "history.opened", "%1$s opened the shop", "history.closed", "%1$s closed the shop",
                "plain_only", "Plain only", "any_metadata", "Any kind", "request", "Request", "decline", "Decline", "offer", "Offer",
                "cancel", "Cancel", "orders_owner", "Customer requests", "orders_customer", "Your requests",
                "order_fields", "qty / max", "nonbinding", "No payment, reservation or promise.",
                "associate_hint", "Pick an offer, then Offer it to a request.", "no_orders", "No requests.",
                "order_line", "%s/%s, max %s, %s", "order_state.open", "open", "order_state.offered", "offered", "order_state.fulfilled", "fulfilled",
                "order_state.cancelled", "cancelled", "order_state.declined", "declined", "order_state.expired", "expired",
                "transaction", "%s (transaction %s)");
    }

    private static void guide(LanguageProvider out) {
        all(out, "guide.herbalwitch.shop.",
                "setup.title", "Keeping a shop",
                "setup.0", "Set a shop register on an apothecary counter. An initiated witch names the shop from it; the shop starts closed.",
                "setup.limits", "A witch may keep %1$s shops. Up to %2$s more counters and %3$s displays or boards can be linked, all within %4$s blocks of the register.",
                "setup.1", "Removing the register detaches the shop; its stock and proceeds stay safe until you reconnect it from a new register.",
                "stocking.title", "Stocking",
                "stocking.0", "Stock moves only through the register: choose a stack in your main inventory or hotbar and how many to move.",
                "stocking.limits", "A shop holds %s stock slots. Hoppers, pipes and familiars cannot reach it.",
                "stocking.1", "Shop shelves and jars only show offers; ordinary furniture shelves keep their own storage.",
                "pricing.title", "Offers and prices",
                "pricing.0", "An offer sells one lot of an exact product for whole emeralds, taken from a stock sample.",
                "pricing.limits", "Up to %1$s offers, priced %2$s to %3$s emeralds, lots up to %4$s (one stack).",
                "pricing.1", "Changing an offer makes a new version; anyone who saw the old one is asked to look again.",
                "metadata.title", "Exact goods",
                "metadata.0", "What a buyer gets is exactly what was stocked: water purity, bottle labels and a charm's used charge are kept and shown.",
                "metadata.1", "Stacks with other data, nested inventories, creature tokens and four-dose mixtures cannot be sold.",
                "buying.title", "Buying",
                "buying.0", "Anyone may browse an open shop at its register, a linked counter or a display, and buy one lot at a time with emeralds from their inventory.",
                "buying.limits", "Stand within %s blocks and see the fixture. Your inventory must have room; nothing is dropped and no change is given.",
                "buying.1", "If an offer changes while you look, the purchase is refused rather than charged differently.",
                "proceeds.title", "Proceeds and ledger",
                "proceeds.0", "Sales add emeralds to the shop's proceeds; only the owner can take them out.",
                "proceeds.limits", "Proceeds hold up to %1$s emeralds. The ledger keeps the latest %2$s events for the owner alone.",
                "proceeds.1", "The ledger is a record for you, not a recovery journal.",
                "orders.title", "Requests",
                "orders.0", "At an order board, customers request a product, quantity and the most they will pay per lot. Nothing is paid or promised.",
                "orders.limits", "%1$s active requests per customer and %2$s per shop; a request lasts %3$s economy windows; wait %4$s seconds between requests.",
                "orders.1", "The owner can decline a request or suggest an existing offer; the customer still buys at the current price.",
                "villagers.title", "Villager customers",
                "villagers.0", "Owners may let nearby adult villagers buy from an open, loaded shop. They walk to the register; nothing is simulated.",
                "villagers.limits", "Villagers buy a %1$s-item lot priced at %2$s emerald. Each window the world spends at most %3$s, one owner %4$s, one shop %5$s, one villager %6$s purchases; a window lasts %7$s days of running time; customers come from within %8$s blocks.",
                "villagers.enabled", "Villager customers on this server: %s", "on", "on", "off", "off",
                "villagers.product", "Villagers buy: %s",
                "villagers.1", "The preparation is used up as demand. Villagers gain no trades, experience or discounts from it.",
                "offline.title", "Unloaded shops",
                "offline.0", "A shop sells only while its register is loaded. Nothing sells while you are away from an unloaded shop, and nothing catches up later.",
                "offline.1", "Linked displays show sold out while the register is unloaded.",
                "recovery.title", "Interrupted transfers",
                "recovery.0", "Every transfer is saved in steps. If a server stops in the middle, the shop waits for proof before settling it.",
                "recovery.1", "An unresolved transfer shows its identifier. An operator can reconcile it from proof or discard it; nothing is ever granted twice.");
    }

    private static void showcase(LanguageProvider out) {
        out.add("showcase.herbalwitch.section.shop", "Apothecary commerce");
        all(out, "showcase.herbalwitch.label.shop.",
                "register_closed", "Shop register", "register_closed.detail", "Closed",
                "register_open", "Shop register", "register_open.detail", "Open",
                "shelf_stocked", "Stock shelf", "shelf_stocked.detail", "Demonstration",
                "shelf_sold_out", "Stock shelf", "shelf_sold_out.detail", "Sold out (demo)",
                "jar_stocked", "Stock jar", "jar_stocked.detail", "Demonstration",
                "jar_sold_out", "Stock jar", "jar_sold_out.detail", "Sold out (demo)",
                "board", "Order board", "board.detail", "Requests",
                "offers", "Offers", "offers.detail", "Lot for emeralds",
                "history", "Ledger", "history.detail", "Owner only",
                "orders", "Requests", "orders.detail", "Nonbinding",
                "villagers", "Villagers", "villagers.detail", "Inert sample");
    }
}
