package io.github.dirtyredz.herbalwitch.apothecary.shop.customer.demand;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Villager spending as an explicitly bounded emerald source, plus the commerce clock. The clock counts
 * running server ticks (time commands, sleep and reconnects never move it); a window resets only once
 * fully elapsed, so a restart may delay a window but never grants a fresh one, and unused allowance
 * never accumulates. Identity maps are bounded and refuse new identities when full instead of evicting
 * (eviction would reset an allowance). Pure.
 */
public final class NpcDemand {
    public enum Refusal { NONE, DISABLED, WORLD, OWNER, SHOP, VILLAGER, SATURATED }

    long elapsed;
    long windowStart;
    int world;
    final Map<UUID, Integer> owners = new LinkedHashMap<>();
    final Map<UUID, Integer> shops = new LinkedHashMap<>();
    final Map<UUID, Integer> villagers = new LinkedHashMap<>();
    final Set<UUID> debits = new LinkedHashSet<>();

    public long elapsed() {
        return elapsed;
    }

    public long windowStart() {
        return windowStart;
    }

    public int world() {
        return world;
    }

    public int owner(UUID owner) {
        return owners.getOrDefault(owner, 0);
    }

    public int shop(UUID shop) {
        return shops.getOrDefault(shop, 0);
    }

    public int villager(UUID villager) {
        return villagers.getOrDefault(villager, 0);
    }

    public boolean debited(UUID transaction) {
        return debits.contains(transaction);
    }

    /** Advances the clock by running ticks; starts a new window once the current one has fully elapsed. */
    public boolean tick(long ticks, int window) {
        elapsed = Math.addExact(elapsed, ticks);
        if (elapsed - windowStart < window) return false;
        windowStart = elapsed;
        world = 0;
        owners.clear();
        shops.clear();
        villagers.clear();
        debits.clear();
        return true;
    }

    public Refusal check(UUID owner, UUID shop, UUID villager, ShopRules rules) {
        if (!rules.npcService()) return Refusal.DISABLED;
        if (world + 1 > rules.worldSpending()) return Refusal.WORLD;
        if (owner(owner) + 1 > rules.ownerSpending()) return Refusal.OWNER;
        if (shop(shop) + 1 > rules.shopSpending()) return Refusal.SHOP;
        if (villager(villager) + 1 > rules.villagerPurchases()) return Refusal.VILLAGER;
        if (!owners.containsKey(owner) && owners.size() >= ShopRules.MAX_DEMAND_IDENTITIES
                || !shops.containsKey(shop) && shops.size() >= ShopRules.MAX_DEMAND_IDENTITIES
                || !villagers.containsKey(villager) && villagers.size() >= ShopRules.MAX_DEMAND_VILLAGERS
                || debits.size() >= 1024) return Refusal.SATURATED;
        return Refusal.NONE;
    }

    /** Charges one emerald to every budget for {@code transaction}; nothing when refused. */
    public Refusal debit(UUID transaction, UUID owner, UUID shop, UUID villager, ShopRules rules) {
        Refusal refusal = check(owner, shop, villager, rules);
        if (refusal != Refusal.NONE) return refusal;
        if (!debits.add(transaction)) return Refusal.SATURATED;
        world++;
        owners.merge(owner, 1, Integer::sum);
        shops.merge(shop, 1, Integer::sum);
        villagers.merge(villager, 1, Integer::sum);
        return Refusal.NONE;
    }

    public NpcDemand copy() {
        NpcDemand out = new NpcDemand();
        out.elapsed = elapsed;
        out.windowStart = windowStart;
        out.world = world;
        out.owners.putAll(owners);
        out.shops.putAll(shops);
        out.villagers.putAll(villagers);
        out.debits.addAll(debits);
        return out;
    }
}
