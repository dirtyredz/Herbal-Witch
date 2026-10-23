package io.github.dirtyredz.herbalwitch.data.stations.shop;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Independent inventory of the Milestone 11 commerce resources: generated files and language keys,
 * kept apart from the providers on purpose so a catalogue or provider slip shows up as a diff.
 */
public final class ShopManifest {
    public static final List<String> PIECES = List.of("register", "stock_shelf", "stock_jar", "order_board");

    private ShopManifest() {
    }

    public static Set<String> files() {
        Set<String> out = new TreeSet<>();
        for (String piece : PIECES) {
            String path = "shop/" + piece + ".json";
            out.add("assets/herbalwitch/blockstates/" + path);
            out.add("assets/herbalwitch/models/item/" + path);
            out.add("data/herbalwitch/loot_tables/blocks/" + path);
            out.add("data/herbalwitch/recipes/" + path);
            out.add("data/herbalwitch/advancements/recipes/decorations/" + path);
        }
        return out;
    }

    private static void keys(Map<String, String> out, String prefix, String... suffixes) {
        for (String suffix : suffixes) out.put(prefix + suffix, null);
    }

    /** Every commerce key; values are checked for presence only. */
    public static Map<String, String> lang() {
        Map<String, String> out = new LinkedHashMap<>();
        out.put("container.herbalwitch.shop", "Shop");
        keys(out, "block.herbalwitch.shop.", "order_board", "register", "stock_jar", "stock_shelf");
        keys(out, "guide.herbalwitch.shop.", "buying.0", "buying.1", "buying.limits", "buying.title", "metadata.0", "metadata.1", "metadata.title",
                "off", "offline.0", "offline.1", "offline.title", "on", "orders.0", "orders.1", "orders.limits", "orders.title", "pricing.0",
                "pricing.1", "pricing.limits", "pricing.title", "proceeds.0", "proceeds.1", "proceeds.limits", "proceeds.title", "recovery.0",
                "recovery.1", "recovery.title", "setup.0", "setup.1", "setup.limits", "setup.title", "stocking.0", "stocking.1", "stocking.limits",
                "stocking.title", "villagers.0", "villagers.1", "villagers.enabled", "villagers.limits", "villagers.product", "villagers.title");
        keys(out, "shop.herbalwitch.result.", "actor", "already_banned", "already_bound", "already_open", "amount", "ban_limit", "ban_self", "banned",
                "bounds", "break_not_owner", "closed", "created", "deposited", "disabled", "fixture_unlinked", "indexing", "inventory_changed",
                "inventory_full", "link_armed_board", "link_armed_counter", "link_armed_display", "link_armed_unlink", "link_demonstration",
                "link_dimension", "link_expired", "link_limit", "link_missing", "link_ordinary_shelf", "link_range", "link_taken", "link_wrong_target",
                "linked", "lot", "name", "no_register", "no_room", "no_sample", "no_stocked_offer", "none", "not_banned", "not_detached", "not_open",
                "not_owner", "not_tradeable", "not_witch", "offer_limit", "order_cancelled", "order_cooldown", "order_created", "order_customer_limit",
                "order_declined", "order_incompatible", "order_offered", "order_owner", "order_price", "order_quantity", "order_shop_limit",
                "order_stale", "order_state", "own_shop", "owner_limit", "payment", "permission", "player_unknown", "price", "proceeds_full",
                "proceeds_withdrawn", "product_capability", "product_damaged", "product_empty", "product_metadata", "product_not_tradeable",
                "purchased", "rate", "receipt_pending", "reconnected", "recovery", "refreshed", "retire_not_empty", "retired", "saved", "session",
                "sight", "sold_out", "stale", "stale_link", "stale_offer", "stale_request", "unavailable", "unbound", "unlinked", "withdrawn",
                "world_limit");
        keys(out, "shop.herbalwitch.ui.", "add", "any_metadata", "assign", "associate_hint", "ban", "bans", "buy", "cancel", "close", "create",
                "create_hint", "create_witch_only", "decline", "deposit", "detached", "disabled", "enabled", "from_stock", "heading", "history.closed",
                "history.deposit", "history.npc_sale", "history.opened", "history.proceeds", "history.recovery_discarded",
                "history.recovery_finalized", "history.recovery_rolled_back", "history.sale", "history.withdraw", "history_heading", "inventory",
                "kind.board", "kind.counter", "kind.jar", "kind.shelf", "lifecycle.closed", "lifecycle.detached", "lifecycle.open",
                "lifecycle.quarantined", "lifecycle.retired", "link_board", "link_counter", "link_display", "link_unlink", "links", "lot", "no_offers",
                "no_orders", "nonbinding", "none", "offer", "offer_detail", "offer_editor", "offer_fields", "offers_heading", "open", "order_fields",
                "order_line", "order_state.cancelled", "order_state.declined", "order_state.expired", "order_state.fulfilled", "order_state.offered",
                "order_state.open", "orders_customer", "orders_owner", "owned", "plain_only", "price", "problem.unavailable", "problem.unreadable",
                "problem.unsupported", "problem.write_failed", "proceeds", "public_off", "public_on", "reconnect", "recovery_line", "remove", "rename",
                "request", "retire", "save", "settings_heading", "slot", "state.closed", "state.detached", "state.open", "state.quarantined",
                "state.recovery", "state.retired", "state.unavailable", "stock_heading", "tab.browse", "tab.create", "tab.history", "tab.offers",
                "tab.orders", "tab.settings", "tab.stock", "take", "to_stock", "transaction", "unban", "unbound", "unlink", "villagers_off",
                "villagers_on", "withdraw");
        keys(out, "showcase.herbalwitch.label.", "shop.board", "shop.board.detail", "shop.history", "shop.history.detail", "shop.jar_sold_out",
                "shop.jar_sold_out.detail", "shop.jar_stocked", "shop.jar_stocked.detail", "shop.offers", "shop.offers.detail", "shop.orders",
                "shop.orders.detail", "shop.register_closed", "shop.register_closed.detail", "shop.register_open", "shop.register_open.detail",
                "shop.shelf_sold_out", "shop.shelf_sold_out.detail", "shop.shelf_stocked", "shop.shelf_stocked.detail", "shop.villagers",
                "shop.villagers.detail");
        keys(out, "showcase.herbalwitch.section.", "shop");
        return out;
    }
}
