package io.github.dirtyredz.herbalwitch.apothecary.shop.ledger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;
import java.util.Optional;

/**
 * The exact identity of a stocked product: its registered item and every accepted gameplay-relevant
 * tag (purity, label, charm charge). Two stacks trade as the same product only when both match; no
 * tag is ever stripped. Pure apart from the registry adapters at the bottom.
 */
public record ShopStackKey(String item, CompoundTag tag) {
    public ShopStackKey {
        Objects.requireNonNull(item);
        if (!item.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") || item.length() > 256) throw new IllegalArgumentException("Invalid item id " + item);
        tag = tag == null ? new CompoundTag() : tag.copy();
    }

    public static ShopStackKey of(String item) {
        return new ShopStackKey(item, new CompoundTag());
    }

    @Override
    public CompoundTag tag() {
        return tag.copy();
    }

    public boolean plain() {
        return tag.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ShopStackKey key && key.item.equals(item) && key.tag.equals(tag);
    }

    @Override
    public int hashCode() {
        return item.hashCode() * 31 + tag.hashCode();
    }

    public CompoundTag save() {
        CompoundTag out = new CompoundTag();
        out.putString("item", item);
        if (!tag.isEmpty()) out.put("tag", tag.copy());
        return out;
    }

    public static ShopStackKey load(CompoundTag in) {
        if (!in.contains("item", CompoundTag.TAG_STRING) || !java.util.Set.of("item", "tag").containsAll(in.getAllKeys())
                || in.contains("tag") && !in.contains("tag", CompoundTag.TAG_COMPOUND)) throw new IllegalArgumentException("Malformed stack key");
        return new ShopStackKey(in.getString("item"), in.getCompound("tag"));
    }

    // Registry adapters (server/runtime only).

    /** The key of a non-empty stack. */
    public static ShopStackKey of(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return new ShopStackKey(Objects.requireNonNull(id).toString(), stack.getTag());
    }

    public Optional<Item> resolve() {
        ResourceLocation id = ResourceLocation.tryParse(item);
        if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) return Optional.empty();
        return Optional.ofNullable(ForgeRegistries.ITEMS.getValue(id));
    }

    /** A stack of this product, or empty when the item no longer exists. */
    public ItemStack stack(int count) {
        return resolve().map(item -> {
            ItemStack out = new ItemStack(item, count);
            if (!tag.isEmpty()) out.setTag(tag.copy());
            return out;
        }).orElse(ItemStack.EMPTY);
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && equals(of(stack));
    }
}
