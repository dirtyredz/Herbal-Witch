package io.github.dirtyredz.herbalwitch.apothecary.shop.trade;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopRecord;
import io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopStackKey;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

import static io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.ShopFixtures.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** Owner stock transfers through the real path: exact counts, stack limits, cursor rule and refused goods. */
@GameTestHolder("herbalwitch")
@PrefixGameTestTemplate(false)
public final class ShopStockGameTests {
    private static final BlockPos COUNTER = new BlockPos(4, 1, 4);

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_stock", timeoutTicks = 60)
    public static void depositsAndWithdrawalsConserveEveryItemAndRespectStackLimits(GameTestHelper h) {
        floor(h);
        ServerPlayer owner = witch(h, "stock_owner");
        Shop shop = shop(h, owner, COUNTER);
        ShopStackKey teaKey = ShopStackKey.of(tea(1));
        owner.getInventory().setItem(3, tea(16));
        owner.getInventory().setItem(4, tea(9));
        check(ShopTrades.deposit(owner, store(h), shop.id(), 3, 10).success(), "partial deposit");
        check(owner.getInventory().items.get(3).getCount() == 6, "partial deposit left the wrong amount");
        check(ShopTrades.deposit(owner, store(h), shop.id(), 4, 9).success(), "second deposit");
        ShopRecord r = record(h, shop);
        check(r.stock.count(teaKey) == 19 && r.stock.slot(0).count() == 16 && r.stock.slot(1).count() == 3, "stock exceeded the item stack limit");
        check(ShopTrades.deposit(owner, store(h), shop.id(), 3, 7).code().equals("amount"), "over-count deposit accepted");
        check(ShopTrades.withdraw(owner, store(h), shop.id(), 1, 2).success(), "partial withdrawal");
        int held = count(owner, tea(1).getItem());
        check(held + record(h, shop).stock.count(teaKey) == 25, "teas not conserved: " + held);
        owner.containerMenu.setCarried(new ItemStack(Items.STICK));
        var before = snapshot(owner);
        check(ShopTrades.deposit(owner, store(h), shop.id(), 3, 1).code().equals("actor"), "a held cursor did not block the transfer");
        check(same(before, owner) && record(h, shop).stock.count(teaKey) == 17, "cursor refusal moved value");
        owner.containerMenu.setCarried(ItemStack.EMPTY);
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_stock", timeoutTicks = 60)
    public static void untradeableNamedAndIntermediateGoodsAreRefusedUntouched(GameTestHelper h) {
        floor(h);
        ServerPlayer owner = witch(h, "refuse_owner");
        Shop shop = shop(h, owner, COUNTER);
        ItemStack named = tea(2);
        named.setHoverName(Component.literal("Special"));
        owner.getInventory().setItem(0, new ItemStack(Items.DIRT, 4));
        owner.getInventory().setItem(1, named);
        owner.getInventory().setItem(2, new ItemStack(AdvancedPreparations.item(MixtureCatalog.specs().get(0)), 1));
        owner.getInventory().setItem(5, new ItemStack(Items.EMERALD, 3));
        var before = snapshot(owner);
        check(ShopTrades.deposit(owner, store(h), shop.id(), 0, 1).code().equals("product_not_tradeable"), "dirt");
        check(ShopTrades.deposit(owner, store(h), shop.id(), 1, 1).code().equals("product_metadata"), "custom name stripped or accepted");
        check(ShopTrades.deposit(owner, store(h), shop.id(), 2, 1).code().equals("product_not_tradeable"), "four-dose mixture");
        check(ShopTrades.deposit(owner, store(h), shop.id(), 5, 1).code().equals("product_not_tradeable"), "currency stocked");
        check(same(before, owner) && record(h, shop).stock.empty(), "a refusal moved items");
        h.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_shop_stock", timeoutTicks = 60)
    public static void proceedsWithdrawalPaysExactEmeraldsOnce(GameTestHelper h) {
        floor(h);
        Shop shop = shop(h, witch(h, "proceeds_owner"), COUNTER);
        UUID offer = selling(h, shop, 3, 5);
        ServerPlayer buyer = player(h, "proceeds_buyer");
        emeralds(buyer, 15);
        long revision = record(h, shop).offer(offer).orElseThrow().revision();
        for (int i = 0; i < 3; i++) check(ShopTrades.purchase(buyer, store(h), shop.id(), offer, revision, null).success(), "purchase " + i);
        check(record(h, shop).proceeds == 15, "proceeds");
        check(ShopTrades.proceeds(shop.owner(), store(h), shop.id(), 16).code().equals("amount"), "overdraw");
        check(ShopTrades.proceeds(buyer, store(h), shop.id(), 1).code().equals("not_owner"), "customer withdrew proceeds");
        check(ShopTrades.proceeds(shop.owner(), store(h), shop.id(), 15).success(), "withdrawal");
        check(count(shop.owner(), Items.EMERALD) == 15 && record(h, shop).proceeds == 0, "emeralds not conserved");
        check(record(h, shop).history.stream().anyMatch(e -> e.type() == io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.ShopHistoryEntry.Type.PROCEEDS), "history");
        h.succeed();
    }
}
