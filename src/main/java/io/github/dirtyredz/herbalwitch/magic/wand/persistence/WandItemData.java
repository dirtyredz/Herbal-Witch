package io.github.dirtyredz.herbalwitch.magic.wand.persistence;

import io.github.dirtyredz.herbalwitch.magic.wand.catalog.WandMode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

/** A mode preference only; no familiar identity or action authorization is persisted here. */
public final class WandItemData {
    public static final String KEY = "HerbalWitchWand";
    private WandItemData() { }

    public static Optional<WandMode> decode(CompoundTag tag) {
        if (!tag.contains("schema", Tag.TAG_INT) || tag.getInt("schema") != 1
                || !tag.contains("mode", Tag.TAG_STRING)) { return Optional.empty(); }
        return WandMode.parse(tag.getString("mode"));
    }

    public static CompoundTag encode(WandMode mode) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("schema", 1);
        tag.putString("mode", mode.id());
        return tag;
    }

    public static Optional<WandMode> read(ItemStack stack) {
        if (stack.isEmpty() || stack.getCount() != 1) { return Optional.empty(); }
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(KEY)) { return Optional.of(WandMode.HARVEST); }
        return root.contains(KEY, Tag.TAG_COMPOUND) ? decode(root.getCompound(KEY)) : Optional.empty();
    }

    public static void write(ItemStack stack, WandMode mode) { stack.getOrCreateTag().put(KEY, encode(mode)); }
}
