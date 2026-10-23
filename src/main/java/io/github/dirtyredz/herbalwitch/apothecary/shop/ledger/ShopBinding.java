package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;
import java.util.UUID;

/**
 * The active register: dimension, position, the register's random instance UUID and the generation it
 * was bound under. Only the exact match is the shop's register; every older generation is invalid.
 */
public record ShopBinding(String dimension, long pos, UUID instance, long generation) {
    private static final Set<String> FIELDS = Set.of("dimension", "pos", "instance", "generation");

    public ShopBinding {
        if (dimension == null || dimension.length() > 256 || instance == null || generation < 1) throw new IllegalArgumentException("Invalid binding");
    }

    public boolean matches(String dimension, long pos, UUID instance, long generation) {
        return this.dimension.equals(dimension) && this.pos == pos && this.instance.equals(instance) && this.generation == generation;
    }

    public CompoundTag save() {
        CompoundTag out = new CompoundTag();
        out.putString("dimension", dimension);
        out.putLong("pos", pos);
        out.putUUID("instance", instance);
        out.putLong("generation", generation);
        return out;
    }

    public static ShopBinding load(CompoundTag in) {
        if (!in.getAllKeys().equals(FIELDS)) throw new IllegalArgumentException("Malformed binding");
        return new ShopBinding(in.getString("dimension"), in.getLong("pos"), in.getUUID("instance"), in.getLong("generation"));
    }
}
