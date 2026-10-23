package io.github.dirtyredz.herbalwitch.magic.broom.persistence;

import io.github.dirtyredz.herbalwitch.magic.broom.catalog.BroomSpec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Strict schema; reads never replace unknown or malformed data with a fresh broom. */
public record BroomItemData(int wear, int wearTicks, int energyRemainder) {
    public static final String KEY = "HerbalWitchBroom";
    public static final int SCHEMA = 1;
    public static final BroomItemData FRESH = new BroomItemData(0, 0, 0);

    public BroomItemData {
        if (wear < 0 || wear > BroomSpec.MAX_WEAR || wearTicks < 0 || wearTicks > 23999
                || energyRemainder < 0 || energyRemainder > 19) {
            throw new IllegalArgumentException("Invalid broom condition or accumulator");
        }
    }

    public int condition() { return BroomSpec.MAX_WEAR - wear; }

    public BroomItemData repair(int amount) {
        if (amount < 1 || amount > BroomSpec.MAX_WEAR) { throw new IllegalArgumentException("Invalid repair amount"); }
        return new BroomItemData(Math.max(0, wear - amount), wearTicks, energyRemainder);
    }

    public CompoundTag encode() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", SCHEMA);
        tag.putInt("wear", wear);
        tag.putInt("wearTicks", wearTicks);
        tag.putInt("energyRemainder", energyRemainder);
        return tag;
    }

    public static Optional<BroomItemData> decode(CompoundTag tag) {
        for (String key : new String[] {"schema", "wear", "wearTicks", "energyRemainder"}) {
            if (!tag.contains(key, Tag.TAG_INT)) { return Optional.empty(); }
        }
        if (tag.getInt("schema") != SCHEMA) { return Optional.empty(); }
        try {
            return Optional.of(new BroomItemData(tag.getInt("wear"), tag.getInt("wearTicks"), tag.getInt("energyRemainder")));
        } catch (IllegalArgumentException invalid) {
            return Optional.empty();
        }
    }

    /** Untagged crafted stacks are fresh; a present but invalid field remains unusable. */
    public static Optional<BroomItemData> read(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1) { return Optional.empty(); }
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(KEY)) { return Optional.of(FRESH); }
        return root.contains(KEY, Tag.TAG_COMPOUND) ? decode(root.getCompound(KEY)) : Optional.empty();
    }

    public void write(ItemStack stack) { stack.getOrCreateTag().put(KEY, encode()); }
}
