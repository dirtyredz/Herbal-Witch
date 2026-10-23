package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;
import java.util.UUID;

/**
 * A published offer: one lot of an exact product for a whole number of emeralds. Offers name exact
 * keys, never slots or bare item ids. Changing the product, lot, price or enabled state makes a new
 * revision, so a buyer who saw the old one cannot buy the new one by accident; withdrawing stock only
 * makes an offer sold out.
 */
public record ShopOffer(UUID id, long revision, ShopStackKey key, int lot, int price, boolean enabled) {
    public static final int MAX_LOT = 64;
    public static final int MIN_PRICE = 1;
    public static final int MAX_PRICE = 64;
    private static final Set<String> FIELDS = Set.of("id", "revision", "key", "lot", "price", "enabled");

    public ShopOffer {
        if (id == null || key == null || revision < 1 || lot < 1 || lot > MAX_LOT || price < MIN_PRICE || price > MAX_PRICE) {
            throw new IllegalArgumentException("Invalid offer");
        }
    }

    /** A changed offer; any change bumps the revision, an identical edit returns this offer. */
    public ShopOffer edit(ShopStackKey key, int lot, int price, boolean enabled) {
        boolean same = this.key.equals(key) && this.lot == lot && this.price == price && this.enabled == enabled;
        return same ? this : new ShopOffer(id, Math.addExact(revision, 1), key, lot, price, enabled);
    }

    public CompoundTag save() {
        CompoundTag out = new CompoundTag();
        out.putUUID("id", id);
        out.putLong("revision", revision);
        out.put("key", key.save());
        out.putInt("lot", lot);
        out.putInt("price", price);
        out.putBoolean("enabled", enabled);
        return out;
    }

    public static ShopOffer load(CompoundTag in) {
        if (!in.getAllKeys().equals(FIELDS) || !in.hasUUID("id")) throw new IllegalArgumentException("Malformed offer");
        return new ShopOffer(in.getUUID("id"), in.getLong("revision"), ShopStackKey.load(in.getCompound("key")),
                in.getInt("lot"), in.getInt("price"), in.getBoolean("enabled"));
    }
}
