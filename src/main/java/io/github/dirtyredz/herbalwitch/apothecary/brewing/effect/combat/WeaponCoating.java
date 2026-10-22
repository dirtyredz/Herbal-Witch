package io.github.dirtyredz.herbalwitch.apothecary.brewing.effect.combat;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * A venom coating on one weapon stack, versioned: {@code HerbalWitchCoating {Version, Kind, Hits, Until}}.
 * It lives on the coated stack itself, so it can never move to an unrelated item, and it is honoured
 * only while that stack is still an eligible weapon ({@link #COATABLE}: swords and axes). It ends after
 * its hits or at its game-time expiry, whichever comes first; an unknown version is ignored and kept.
 */
public final class WeaponCoating {
    public static final String TAG = "HerbalWitchCoating";
    public static final int VERSION = 1;
    public static final String VENOM = "venom";
    /** Successful hits a fresh venom coating gives. */
    public static final int HITS = 8;
    /** Ticks a fresh coating lasts. */
    public static final int DURATION = 60 * 20;
    public static final TagKey<Item> COATABLE = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID,
            "coatable_weapons"));

    /** A readable coating. */
    public record Coating(String kind, int hits, long until) {
    }

    private WeaponCoating() {
    }

    public static boolean eligible(ItemStack weapon) {
        return !weapon.isEmpty() && weapon.is(COATABLE) && weapon.getCount() == 1;
    }

    /** Coats {@code weapon} with venom from now; refreshes (never adds to) an existing coating. */
    public static boolean coat(ItemStack weapon, long now) {
        if (!eligible(weapon)) {
            return false;
        }
        CompoundTag tag = new CompoundTag();
        tag.putInt("Version", VERSION);
        tag.putString("Kind", VENOM);
        tag.putInt("Hits", HITS);
        tag.putLong("Until", now + DURATION);
        weapon.getOrCreateTag().put(TAG, tag);
        return true;
    }

    /** The weapon's live coating, if any (an expired one is removed). */
    public static Optional<Coating> read(ItemStack weapon, long now) {
        if (!eligible(weapon) || !weapon.hasTag() || !weapon.getTag().contains(TAG)) {
            return Optional.empty();
        }
        CompoundTag tag = weapon.getTag().getCompound(TAG);
        if (tag.getInt("Version") != VERSION) {
            return Optional.empty();
        }
        Coating coating = new Coating(tag.getString("Kind"), tag.getInt("Hits"), tag.getLong("Until"));
        if (coating.hits() <= 0 || coating.until() < now) {
            clear(weapon);
            return Optional.empty();
        }
        return Optional.of(coating);
    }

    /** Spends one hit; the coating goes with the last one. */
    public static void spend(ItemStack weapon) {
        CompoundTag tag = weapon.getTag() == null ? null : weapon.getTag().getCompound(TAG);
        if (tag == null) {
            return;
        }
        int hits = tag.getInt("Hits") - 1;
        if (hits <= 0) {
            clear(weapon);
        } else {
            tag.putInt("Hits", hits);
        }
    }

    public static void clear(ItemStack weapon) {
        if (weapon.hasTag()) {
            weapon.getTag().remove(TAG);
            if (weapon.getTag().isEmpty()) {
                weapon.setTag(null);
            }
        }
    }
}
