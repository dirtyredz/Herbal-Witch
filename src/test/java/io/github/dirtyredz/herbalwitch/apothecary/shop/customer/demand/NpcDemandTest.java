package io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Villager spending is a bounded source: every budget, window transitions and saturation without eviction. */
class NpcDemandTest {
    static ShopRules rules(int world, int owner, int shop, int villager) {
        ShopRules d = ShopRules.DEFAULTS;
        return new ShopRules(d.shopsPerOwner(), d.shopsPerWorld(), d.proceedsCapacity(), true, 24_000, world, owner, shop, villager,
                d.visitInterval(), d.visitDuration(), d.concurrentVisits(), d.orderLifetimeWindows());
    }

    @Test
    void everyBudgetCapsAndRefusalsChargeNothing() {
        NpcDemand d = new NpcDemand();
        UUID owner = UUID.randomUUID(), shop = UUID.randomUUID();
        ShopRules r = rules(128, 32, 3, 2);
        UUID v1 = UUID.randomUUID();
        assertEquals(NpcDemand.Refusal.NONE, d.debit(UUID.randomUUID(), owner, shop, v1, r));
        assertEquals(NpcDemand.Refusal.NONE, d.debit(UUID.randomUUID(), owner, shop, v1, r));
        assertEquals(NpcDemand.Refusal.VILLAGER, d.debit(UUID.randomUUID(), owner, shop, v1, r));
        assertEquals(NpcDemand.Refusal.NONE, d.debit(UUID.randomUUID(), owner, shop, UUID.randomUUID(), r));
        assertEquals(NpcDemand.Refusal.SHOP, d.debit(UUID.randomUUID(), owner, shop, UUID.randomUUID(), r), "new villagers cannot bypass the shop budget");
        assertEquals(3, d.world());
        assertEquals(3, d.owner(owner));
        // Owner budget aggregates across shops; a replacement shop or register cannot reset it.
        ShopRules ownerCap = rules(128, 4, 16, 4);
        assertEquals(NpcDemand.Refusal.NONE, d.debit(UUID.randomUUID(), owner, UUID.randomUUID(), UUID.randomUUID(), ownerCap));
        assertEquals(NpcDemand.Refusal.OWNER, d.debit(UUID.randomUUID(), owner, UUID.randomUUID(), UUID.randomUUID(), ownerCap));
        ShopRules worldCap = rules(5, 128, 64, 4);
        assertEquals(NpcDemand.Refusal.NONE, d.debit(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), worldCap));
        assertEquals(NpcDemand.Refusal.WORLD, d.debit(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), worldCap));
        assertEquals(NpcDemand.Refusal.DISABLED, d.check(owner, shop, v1, new ShopRules(4, 1024, 1000, false, 24000, 128, 32, 16, 2, 1200, 200, 16, 7)));
    }

    @Test
    void windowsResetOnlyAfterFullRunningTimeAndNeverAccumulate() {
        NpcDemand d = new NpcDemand();
        ShopRules r = rules(1, 1, 1, 1);
        UUID t = UUID.randomUUID();
        assertEquals(NpcDemand.Refusal.NONE, d.debit(t, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), r));
        assertTrue(d.debited(t));
        assertFalse(d.tick(23_999, 24_000));
        assertEquals(NpcDemand.Refusal.WORLD, d.check(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), r));
        assertTrue(d.tick(1, 24_000));
        assertEquals(0, d.world());
        assertFalse(d.debited(t), "debit evidence is per window");
        assertTrue(d.tick(240_000, 24_000), "a long idle stretch starts one new window");
        assertEquals(NpcDemand.Refusal.NONE, d.debit(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), r));
        assertEquals(NpcDemand.Refusal.WORLD, d.check(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), r), "unused windows grant nothing extra");
    }

    @Test
    void saturatedIdentityMapsRefuseInsteadOfEvicting() {
        NpcDemand d = new NpcDemand();
        ShopRules r = rules(1024, 128, 64, 4);
        for (int i = 0; i < ShopRules.MAX_DEMAND_IDENTITIES; i++) d.owners.put(UUID.randomUUID(), 1);
        assertEquals(NpcDemand.Refusal.SATURATED, d.check(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), r));
        UUID known = d.owners.keySet().iterator().next();
        assertEquals(NpcDemand.Refusal.NONE, d.check(known, UUID.randomUUID(), UUID.randomUUID(), r));
    }

    @Test
    void codecRoundTripsAndRefusesMalformedData() throws Exception {
        NpcDemand d = new NpcDemand();
        d.tick(500, 24_000);
        d.debit(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), rules(10, 10, 10, 2));
        NpcDemand back = NpcDemandCodec.decode(NpcDemandCodec.encode(d));
        assertEquals(d.elapsed(), back.elapsed());
        assertEquals(d.world(), back.world());
        assertEquals(d.debits, back.debits);
        assertThrows(java.io.IOException.class, () -> NpcDemandCodec.decode(new byte[] {10, 0, 0, 0}));
    }

    @Test
    void configurationBoundsAreHard() {
        assertThrows(IllegalArgumentException.class, () -> rules(1025, 32, 16, 2));
        assertThrows(IllegalArgumentException.class, () -> rules(128, 129, 16, 2));
        assertThrows(IllegalArgumentException.class, () -> rules(128, 32, 65, 2));
        assertThrows(IllegalArgumentException.class, () -> rules(128, 32, 16, 5));
        ShopRules d = ShopRules.DEFAULTS;
        assertEquals(24_000, d.economyWindow());
        assertEquals(128, d.worldSpending());
        assertEquals(32, d.ownerSpending());
        assertEquals(16, d.shopSpending());
        assertEquals(2, d.villagerPurchases());
        assertEquals(1200, d.visitInterval());
        assertEquals(200, d.visitDuration());
        assertEquals(16, d.concurrentVisits());
    }
}
