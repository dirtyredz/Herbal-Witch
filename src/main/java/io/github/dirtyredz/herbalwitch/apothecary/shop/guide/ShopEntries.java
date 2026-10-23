package io.github.dirtyredz.herbalwitch.apothecary.shop.guide;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.NpcProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopHistoryEntry;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopOffer;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopItems;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Commerce guidance (priority 1300, after the tools): setup, stocking, pricing, metadata, buying,
 * proceeds, requests, villager customers, offline behaviour and recovery. Every number is an argument
 * from the live commerce rules or a catalogue constant; the prose holds none. Reading a page grants
 * nothing: discovery never authorizes commerce.
 */
public final class ShopEntries implements GrimoireContentSource {
    public static final int PRIORITY = 1300;
    public static final String PREFIX = "guide." + HerbalWitch.MOD_ID + ".shop.";
    public static final List<String> TOPICS = List.of("setup", "stocking", "pricing", "metadata", "buying", "proceeds", "orders", "villagers", "offline", "recovery");
    public static final EntryKey COUNTER = EntryKey.of(EntryKey.Kind.FURNITURE, HerbalWitch.MOD_ID + ":" + ShopPieceCatalog.COUNTER_PATH);

    public static EntryKey key(String topic) {
        return EntryKey.of(EntryKey.Kind.GUIDE, HerbalWitch.MOD_ID + ":commerce/" + topic);
    }

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        ShopRules r = ShopServerConfig.current();
        for (String topic : TOPICS) {
            List<Component> body = new ArrayList<>();
            body.add(Component.translatable(PREFIX + topic + ".0"));
            switch (topic) {
                case "setup" -> body.add(Component.translatable(PREFIX + "setup.limits", r.shopsPerOwner(), ShopRecord.MAX_EXTRA_COUNTERS,
                        ShopRecord.MAX_FIXTURES, ShopRules.LINK_RANGE));
                case "stocking" -> body.add(Component.translatable(PREFIX + "stocking.limits", ShopRules.STOCK_SLOTS));
                case "pricing" -> body.add(Component.translatable(PREFIX + "pricing.limits", ShopRules.OFFERS, ShopOffer.MIN_PRICE, ShopOffer.MAX_PRICE, ShopOffer.MAX_LOT));
                case "buying" -> body.add(Component.translatable(PREFIX + "buying.limits", ShopRules.REACH));
                case "proceeds" -> body.add(Component.translatable(PREFIX + "proceeds.limits", r.proceedsCapacity(), ShopHistoryEntry.CAPACITY));
                case "orders" -> body.add(Component.translatable(PREFIX + "orders.limits", ShopRules.ORDERS_PER_CUSTOMER, ShopRules.ORDERS_PER_SHOP,
                        r.orderLifetimeWindows(), ShopRules.ORDER_COOLDOWN / 20));
                case "villagers" -> {
                    body.add(Component.translatable(PREFIX + "villagers.limits", NpcProductCatalog.LOT, NpcProductCatalog.PRICE, r.worldSpending(),
                            r.ownerSpending(), r.shopSpending(), r.villagerPurchases(), r.economyWindow() / 24000, ShopRules.CUSTOMER_RANGE));
                    body.add(Component.translatable(PREFIX + "villagers.enabled", Component.translatable(PREFIX + (r.npcService() ? "on" : "off"))));
                    for (String id : NpcProductCatalog.ITEMS) {
                        var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(id));
                        if (item != null) body.add(Component.translatable(PREFIX + "villagers.product", item.getDescription()));
                    }
                }
                default -> { }
            }
            body.add(Component.translatable(PREFIX + topic + ".1"));
            ItemStack icon = topic.equals("orders") ? ShopItems.item(ShopPieceCatalog.ORDER_BOARD).getDefaultInstance()
                    : topic.equals("stocking") || topic.equals("pricing") ? ShopItems.item(ShopPieceCatalog.STOCK_SHELF).getDefaultInstance()
                    : ShopItems.item(ShopPieceCatalog.REGISTER).getDefaultInstance();
            out.accept(GrimoireEntry.page(key(topic), GrimoireContent.GUIDE, Component.translatable(PREFIX + topic + ".title"), icon, body, links(topic)));
        }
    }

    private static List<EntryKey> links(String topic) {
        List<EntryKey> out = new ArrayList<>();
        switch (topic) {
            case "setup" -> {
                out.add(COUNTER);
                out.add(WitchcraftKeys.key("status"));
            }
            case "metadata" -> {
                out.add(WitchcraftKeys.key("brewing"));
                out.add(WitchcraftKeys.key("familiar"));
                out.add(WitchcraftKeys.key("charms"));
            }
            case "villagers" -> out.add(WitchcraftKeys.key("brewing"));
            default -> { }
        }
        for (String other : TOPICS) if (!other.equals(topic)) out.add(key(other));
        return out;
    }
}
