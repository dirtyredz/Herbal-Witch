package io.github.dirtyredz.herbalwitch.apothecary.shop.order;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;
import java.util.UUID;

/**
 * A nonbinding customer request: no payment, reservation, delivery or promise. The customer later
 * reviews and buys an ordinary offer at its current price; a purchase explicitly associated with the
 * request advances {@code purchased} inside the same shop transaction.
 *
 * @param requirement {@code ANY}: any accepted metadata; {@code DEFAULT}: only the plain product
 * @param offer       associated offer (null unless OFFERED)
 */
public record ShopOrder(UUID id, long revision, UUID requester, String requesterName, String item, Requirement requirement,
                        int quantity, int purchased, int maxPrice, long created, long expires, State state, UUID offer, long offerRevision) {
    public enum State { OPEN, OFFERED, FULFILLED, CANCELLED, DECLINED, EXPIRED }
    public enum Requirement { ANY, DEFAULT }

    public static final int MAX_QUANTITY = 64;
    private static final Set<String> FIELDS = Set.of("id", "revision", "requester", "requesterName", "item", "requirement", "quantity",
            "purchased", "maxPrice", "created", "expires", "state", "offer", "offerRevision");

    public ShopOrder {
        if (id == null || requester == null || item == null || requirement == null || state == null || requesterName == null
                || requesterName.length() > 64 || item.length() > 256 || revision < 1 || quantity < 1 || quantity > MAX_QUANTITY
                || purchased < 0 || purchased > quantity || maxPrice < 1 || maxPrice > 64 || expires < created
                || (state == State.OFFERED) != (offer != null)) {
            throw new IllegalArgumentException("Invalid order");
        }
    }

    public boolean active() {
        return state == State.OPEN || state == State.OFFERED;
    }

    public int remaining() {
        return quantity - purchased;
    }

    ShopOrder with(State next, UUID nextOffer, long nextOfferRevision, int nextPurchased) {
        return new ShopOrder(id, Math.addExact(revision, 1), requester, requesterName, item, requirement, quantity, nextPurchased, maxPrice,
                created, expires, next, nextOffer, nextOffer == null ? 0 : nextOfferRevision);
    }

    public CompoundTag save() {
        CompoundTag out = new CompoundTag();
        out.putUUID("id", id);
        out.putLong("revision", revision);
        out.putUUID("requester", requester);
        out.putString("requesterName", requesterName);
        out.putString("item", item);
        out.putString("requirement", requirement.name());
        out.putInt("quantity", quantity);
        out.putInt("purchased", purchased);
        out.putInt("maxPrice", maxPrice);
        out.putLong("created", created);
        out.putLong("expires", expires);
        out.putString("state", state.name());
        if (offer != null) out.putUUID("offer", offer);
        out.putLong("offerRevision", offerRevision);
        return out;
    }

    public static ShopOrder load(CompoundTag in) {
        if (!FIELDS.containsAll(in.getAllKeys())) throw new IllegalArgumentException("Malformed order");
        return new ShopOrder(in.getUUID("id"), in.getLong("revision"), in.getUUID("requester"), in.getString("requesterName"), in.getString("item"),
                Requirement.valueOf(in.getString("requirement")), in.getInt("quantity"), in.getInt("purchased"), in.getInt("maxPrice"),
                in.getLong("created"), in.getLong("expires"), State.valueOf(in.getString("state")),
                in.hasUUID("offer") ? in.getUUID("offer") : null, in.getLong("offerRevision"));
    }
}
