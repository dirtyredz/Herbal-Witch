package io.github.dirtyredz.herbalwitch.magic.ward.persistence;

import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;

import java.util.Optional;
import java.util.UUID;

/**
 * One ward's authoritative state, owned only by {@link WardSavedData}: who it is bound to (nobody
 * until its first activation), where it stands (dimension and position), its settings, and the
 * loaded ticks of protection left. The stone's block entity only refers to it by {@link #id}.
 * Mutations go through the saved data, which marks itself dirty.
 */
public final class WardRecord {
    private final long id;
    private final String dimension;
    private final long pos;
    private UUID owner;
    private int radius;
    private WardCatalog.Profile profile;
    private int charge;
    private boolean focused;

    public WardRecord(long id, String dimension, long pos, UUID owner, int radius, WardCatalog.Profile profile, int charge) {
        this.id = id;
        this.dimension = dimension;
        this.pos = pos;
        this.owner = owner;
        this.radius = radius;
        this.profile = profile;
        this.charge = Math.max(0, charge);
    }

    public long id() {
        return id;
    }

    public String dimension() {
        return dimension;
    }

    public long pos() {
        return pos;
    }

    public Optional<UUID> owner() {
        return Optional.ofNullable(owner);
    }

    public boolean ownedBy(UUID player) {
        return owner != null && owner.equals(player);
    }

    void setOwner(UUID owner) {
        this.owner = owner;
    }

    /** The radius the owner chose (the effective one may be smaller without a focus). */
    public int radius() {
        return radius;
    }

    void setRadius(int radius) {
        this.radius = radius;
    }

    public WardCatalog.Profile profile() {
        return profile;
    }

    void setProfile(WardCatalog.Profile profile) {
        this.profile = profile;
    }

    /** Loaded ticks of protection left; 0 is an empty (inactive) ward. */
    public int charge() {
        return charge;
    }

    void setCharge(int charge) {
        this.charge = Math.max(0, charge);
    }

    public boolean charged() {
        return charge > 0;
    }

    /** Whether a hearth or stove focus was found at the last check (transient, rechecked each sweep). */
    public boolean focused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    /** The radius in force: the chosen one, capped by what the focus allows now. */
    public int effectiveRadius(int base, int withFocus) {
        return Math.min(radius, focused ? withFocus : base);
    }
}
