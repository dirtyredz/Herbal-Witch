package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A fixture associated with a shop: a dimension-qualified position, the fixture's instance UUID (a
 * counter has none: it stays ordinary decor) and, for displays, the offer ids it shows. Display
 * assignments select offers; they never move inventory.
 */
public record ShopLink(Kind kind, String dimension, long pos, UUID instance, List<UUID> offers) {
    public enum Kind { COUNTER, SHELF, JAR, BOARD }

    private static final Set<String> FIELDS = Set.of("kind", "dimension", "pos", "instance", "offers");

    public ShopLink {
        if (kind == null || dimension == null || dimension.length() > 256 || (kind == Kind.COUNTER) != (instance == null)) {
            throw new IllegalArgumentException("Invalid link");
        }
        offers = List.copyOf(offers);
        if (offers.size() > slots(kind)) throw new IllegalArgumentException("Too many display offers");
    }

    public static int slots(Kind kind) {
        return switch (kind) {
            case SHELF -> 3;
            case JAR -> 1;
            default -> 0;
        };
    }

    public boolean at(String dimension, long pos) {
        return this.dimension.equals(dimension) && this.pos == pos;
    }

    public ShopLink withOffers(List<UUID> next) {
        return new ShopLink(kind, dimension, pos, instance, next);
    }

    public CompoundTag save() {
        CompoundTag out = new CompoundTag();
        out.putString("kind", kind.name());
        out.putString("dimension", dimension);
        out.putLong("pos", pos);
        if (instance != null) out.putUUID("instance", instance);
        ListTag list = new ListTag();
        for (UUID offer : offers) list.add(NbtUtils.createUUID(offer));
        out.put("offers", list);
        return out;
    }

    public static ShopLink load(CompoundTag in) {
        if (!FIELDS.containsAll(in.getAllKeys())) throw new IllegalArgumentException("Malformed link");
        List<UUID> offers = new ArrayList<>();
        for (Tag tag : in.getList("offers", Tag.TAG_INT_ARRAY)) offers.add(NbtUtils.loadUUID(tag));
        return new ShopLink(Kind.valueOf(in.getString("kind")), in.getString("dimension"), in.getLong("pos"),
                in.hasUUID("instance") ? in.getUUID("instance") : null, offers);
    }
}
