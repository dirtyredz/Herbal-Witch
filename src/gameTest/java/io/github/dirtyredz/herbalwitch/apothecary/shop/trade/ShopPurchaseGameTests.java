package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopServerConfig;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import io.github.dirtyredz.herbalwitch.magic.charm.registry.CharmItems;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Real purchases through the production transfer path: contention, refusals that move nothing, and exact goods. */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopPurchaseGameTests {
    private static final BlockPos COUNTER = new BlockPos(4, 1, 4);

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_trade", timeoutTicks = 60)
    public static void twoBuyersContendForTheFinalLotAndExactlyOneSucceeds(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        UUID offer = selling(h, shop, 1, 2);
        ServerPlayer a = player(h, "buyer_a"), b = player(h, "buyer_b");
        emeralds(a, 5);
        emeralds(b, 5);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        ShopResult first = ShopTrades.purchase(a, store(h), shop.id(), offer, revision, null);
        ShopResult second = ShopTrades.purchase(b, store(h), shop.id(), offer, revision, null);
        check(first.success() && first.transaction() != null, "first buyer refused: " + first.code());
        check(!second.success() && second.code().equals("sold_out"), "second buyer got " + second.code());
        ShopRecord r = record(h, shop);
        check(count(a, tea(1).getItem()) == 1 && count(a, Items.EMERALD) == 3, "winner inventory");
        check(count(b, tea(1).getItem()) == 0 && count(b, Items.EMERALD) == 5, "loser inventory changed");
        check(r.stock.empty() && r.proceeds == 2 && !r.blocked(), "shop state after sale");
        check(count(a, Items.EMERALD) + count(b, Items.EMERALD) + r.proceeds == 10, "emeralds not conserved");
        check(r.settled(first.transaction()), "receipt missing");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_trade", timeoutTicks = 60)
    public static void stalePriceInsufficientPaymentFullInventoryAndMetadataMismatchMoveNothing(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        UUID offer = selling(h, shop, 4, 2);
        ServerPlayer buyer = player(h, "buyer");
        emeralds(buyer, 1);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        var before = snapshot(buyer);
        check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, revision, null).code().equals("payment"), "insufficient payment");
        check(same(before, buyer) && record(h, shop).stock.count(ShopStackKey.of(tea(1))) == 4 && record(h, shop).proceeds == 0, "payment refusal moved value");
        emeralds(buyer, 10);
        check(ShopManagement.saveOffer(shop.owner(), store(h), shop.id(), offer, 0, 1, 3, true).success(), "price edit");
        before = snapshot(buyer);
        check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, revision, null).code().equals("stale_offer"), "stale price was charged");
        check(same(before, buyer) && record(h, shop).proceeds == 0, "stale refusal moved value");
        for (int i = 0; i < 36; i++) if (i != 8) give(buyer, i, new ItemStack(Items.DIRT, 64));
        before = snapshot(buyer);
        long current = record(h, shop).offer(offer).orElseThrow().revision();
        check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, current, null).code().equals("inventory_full"), "full inventory");
        check(same(before, buyer) && record(h, shop).stock.count(ShopStackKey.of(tea(1))) == 4, "full refusal moved value");
        buyer.getInventory().clearContent();
        emeralds(buyer, 10);
        // A purity-tagged product is a different product: the plain offer is sold out against tagged stock.
        ItemStack tagged = tea(2);
        tagged.getOrCreateTag().putInt("Purity", 1);
        check(ShopTrades.withdraw(shop.owner(), store(h), shop.id(), 0, 4).success(), "owner withdrawal");
        stock(h, shop, tagged);
        before = snapshot(buyer);
        check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, current, null).code().equals("sold_out"), "metadata mismatch sold");
        check(same(before, buyer) && record(h, shop).proceeds == 0, "mismatch refusal moved value");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_proceeds_cap", timeoutTicks = 60)
    public static void fullProceedsRefuseTheSale(GameTestHelper h) {
        floor(h);
        ShopRules old = ShopServerConfig.current();
        try {
            Shop shop = shop(h, witch(h, "owner"), COUNTER);
            UUID offer = selling(h, shop, 2, 2);
            ShopServerConfig.install(new ShopRules(old.shopsPerOwner(), old.shopsPerWorld(), 64, old.npcService(), old.economyWindow(), old.worldSpending(),
                    old.ownerSpending(), old.shopSpending(), old.villagerPurchases(), old.visitInterval(), old.visitDuration(), old.concurrentVisits(), old.orderLifetimeWindows()));
            ShopRecord full = record(h, shop).next();
            full.proceeds = 63;
            check(store(h).update(full, true), "proceeds setup");
            ServerPlayer buyer = player(h, "buyer");
            emeralds(buyer, 4);
            var before = snapshot(buyer);
            check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, record(h, shop).offer(offer).orElseThrow().revision(), null).code().equals("proceeds_full"), "proceeds cap");
            check(same(before, buyer) && record(h, shop).proceeds == 63, "cap refusal moved value");
            h.succeed();
        } finally {
            ShopServerConfig.install(old);
        }
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_trade", timeoutTicks = 60)
    public static void exactPreparationLabelsAndCharmChargeSurviveTrading(GameTestHelper h) {
        floor(h);
        ServerPlayer owner = witch(h, "owner");
        Shop shop = shop(h, owner, COUNTER);
        int energy = WitchStatusService.energyUnits(owner);
        ItemStack labelled = PreparationMetadata.labelled(new ItemStack(AdvancedPreparations.item(AdvancedProductCatalog.HEALING), 1));
        ItemStack charm = new ItemStack(CharmItems.item(CharmCatalog.ALL.get(0)));
        for (int i = 0; i < 7; i++) ProtectiveCharmItem.spendSecond(charm);
        stock(h, shop, labelled);
        owner.getInventory().setItem(0, charm.copy());
        check(ShopTrades.deposit(owner, store(h), shop.id(), 0, 1).success(), "charm deposit");
        UUID labelOffer = offer(h, shop, 0, 1, 2);
        UUID charmOffer = offer(h, shop, 1, 1, 3);
        open(h, shop);
        ServerPlayer buyer = player(h, "buyer");
        emeralds(buyer, 10);
        ShopRecord r = record(h, shop);
        check(ShopTrades.purchase(buyer, store(h), shop.id(), labelOffer, r.offer(labelOffer).orElseThrow().revision(), null).success(), "label purchase");
        check(ShopTrades.purchase(buyer, store(h), shop.id(), charmOffer, r.offer(charmOffer).orElseThrow().revision(), null).success(), "charm purchase");
        boolean label = false, charged = false;
        for (ItemStack stack : buyer.getInventory().items) {
            label |= ItemStack.matches(stack, labelled);
            charged |= ItemStack.isSameItemSameTags(stack, charm) && ProtectiveCharmItem.used(stack) == 7;
        }
        check(label, "label lost in trade");
        check(charged, "charm charge changed in trade");
        check(WitchStatusService.energyUnits(owner) == energy, "commerce changed witch energy");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_trade", timeoutTicks = 60)
    public static void aNonWitchBuysAndUsesAnOrdinaryPreparationWhileTheOwnerIsAway(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "owner"), COUNTER);
        UUID offer = selling(h, shop, 3, 1);
        shop.owner().discard(); // the owner is gone; a loaded shop keeps selling
        ServerPlayer buyer = player(h, "plain_buyer");
        check(!WitchStatusService.isWitch(buyer), "buyer should not be a witch");
        emeralds(buyer, 1);
        check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, record(h, shop).offer(offer).orElseThrow().revision(), null).success(), "purchase");
        int slot = -1;
        for (int i = 0; i < 36; i++) if (buyer.getInventory().items.get(i).is(tea(1).getItem())) slot = i;
        check(slot >= 0, "tea not received");
        ItemStack bought = buyer.getInventory().items.get(slot);
        ItemStack left = bought.finishUsingItem(h.getLevel(), buyer);
        check(left.is(Items.GLASS_BOTTLE) || left.isEmpty() || left.getCount() < 1 || !left.is(tea(1).getItem()), "the tea was not drunk normally");
        h.succeed();
    }
}
