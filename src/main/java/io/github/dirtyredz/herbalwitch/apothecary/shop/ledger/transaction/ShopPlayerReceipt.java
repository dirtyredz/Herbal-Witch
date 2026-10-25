package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger.transaction;

import io.github.dirtyredz.herbalwitch.platform.persistence.PlayerFileCheckpoint;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Optional;
import java.util.UUID;

/**
 * The commerce receipt carried in a player's persistent data: a monotonic sequence and the last
 * transaction applied to this player's inventory. It is written in the same server-thread step as the
 * inventory change, so any saved player file that contains transaction T also contains T's inventory
 * result. It is evidence only: never money, stock or a shop capability. While {@code pending}, the
 * player can start no other commerce transaction.
 */
public final class ShopPlayerReceipt {
    public static final String KEY = "herbalwitch_shop_receipt";

    /** The marker's contents; sequence 0 and no transaction when the player never traded. */
    public record Marker(long sequence, UUID transaction, UUID shop, boolean pending) {
        public static final Marker NONE = new Marker(0, null, null, false);

        public CompoundTag save() {
            CompoundTag out = new CompoundTag();
            out.putInt("schema", 1);
            out.putLong("sequence", sequence);
            if (transaction != null) out.putUUID("transaction", transaction);
            if (shop != null) out.putUUID("shop", shop);
            out.putBoolean("pending", pending);
            return out;
        }

        /** Empty for an absent marker; null (unusable) for an unsupported or malformed one. */
        public static Marker load(CompoundTag tag) {
            if (tag.isEmpty()) return NONE;
            if (tag.getInt("schema") != 1 || !tag.contains("sequence", CompoundTag.TAG_LONG) || tag.getLong("sequence") < 0) return null;
            return new Marker(tag.getLong("sequence"), tag.hasUUID("transaction") ? tag.getUUID("transaction") : null,
                    tag.hasUUID("shop") ? tag.getUUID("shop") : null, tag.getBoolean("pending"));
        }
    }

    private ShopPlayerReceipt() {
    }

    /** The player's marker, or empty when it is unreadable (which blocks commerce). */
    public static Optional<Marker> of(Player player) {
        return Optional.ofNullable(Marker.load(player.getPersistentData().getCompound(KEY)));
    }

    /** Whether the player may start a new transaction: a readable marker that is not pending. */
    public static boolean free(Player player) {
        return of(player).map(m -> !m.pending()).orElse(false);
    }

    static void set(Player player, Marker marker) {
        player.getPersistentData().put(KEY, marker.save());
    }

    /** Settles the marker in memory after the shop committed (the next ordinary save persists it). */
    static void settle(Player player, UUID transaction) {
        of(player).filter(m -> transaction.equals(m.transaction())).ifPresent(m -> set(player, new Marker(m.sequence(), m.transaction(), m.shop(), false)));
    }

    /** Death and End return copy the marker so a pending receipt keeps blocking. */
    public static void clonePlayer(PlayerEvent.Clone event) {
        PlayerFileCheckpoint.copy(event.getOriginal(), event.getEntity(), KEY);
    }
}
