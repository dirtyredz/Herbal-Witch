package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;
import java.util.UUID;

/**
 * One owner-visible economic event. History is bounded presentation, not recovery evidence: evicting
 * an entry never removes a transaction or receipt. A villager customer is recorded by UUID only.
 *
 * @param transaction transaction identity (a fresh id for events outside a transaction)
 * @param time        server game time
 * @param actor       acting player or villager UUID
 * @param actorName   bounded plain display name at the time ("Villager" for a villager)
 * @param type        event type
 * @param product     item id summary, or empty
 * @param quantity    items moved
 * @param emeralds    emeralds moved
 */
public record ShopHistoryEntry(UUID transaction, long time, UUID actor, String actorName, Type type, String product, int quantity, long emeralds) {
    public static final int CAPACITY = 128;
    private static final Set<String> FIELDS = Set.of("transaction", "time", "actor", "actorName", "type", "product", "quantity", "emeralds");

    public enum Type { SALE, NPC_SALE, DEPOSIT, WITHDRAW, PROCEEDS, RECOVERY_FINALIZED, RECOVERY_ROLLED_BACK, RECOVERY_DISCARDED, OPENED, CLOSED }

    public ShopHistoryEntry {
        if (transaction == null || actor == null || type == null || actorName == null || product == null
                || actorName.length() > 64 || product.length() > 256 || quantity < 0 || emeralds < 0) {
            throw new IllegalArgumentException("Invalid history entry");
        }
    }

    public CompoundTag save() {
        CompoundTag out = new CompoundTag();
        out.putUUID("transaction", transaction);
        out.putLong("time", time);
        out.putUUID("actor", actor);
        out.putString("actorName", actorName);
        out.putString("type", type.name());
        out.putString("product", product);
        out.putInt("quantity", quantity);
        out.putLong("emeralds", emeralds);
        return out;
    }

    public static ShopHistoryEntry load(CompoundTag in) {
        if (!in.getAllKeys().equals(FIELDS)) throw new IllegalArgumentException("Malformed history entry");
        return new ShopHistoryEntry(in.getUUID("transaction"), in.getLong("time"), in.getUUID("actor"), in.getString("actorName"),
                Type.valueOf(in.getString("type")), in.getString("product"), in.getInt("quantity"), in.getLong("emeralds"));
    }
}
