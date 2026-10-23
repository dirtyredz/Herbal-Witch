package io.github.dirtyredz.herbalwitch.apothecary.shop.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata.PreparationMetadata;
import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.magic.charm.item.ProtectiveCharmItem;
import io.github.dirtyredz.herbalwitch.platform.compat.thirst.ThirstPurity;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

/**
 * The gameplay metadata commerce preserves exactly: Thirst purity, the bottling label and a charm's
 * used-charge compound. Any other tag (a custom name included), a malformed value, more than
 * {@value ShopRules#ITEM_NBT_BYTES} bytes or more than {@value ShopRules#ITEM_NBT_DEPTH} levels is
 * refused rather than stripped, because stripping would change what the buyer receives.
 */
public final class ShopMetadataRules {
    private ShopMetadataRules() {
    }

    public static boolean accepts(Item item, CompoundTag tag) {
        return accepts(item instanceof ProtectiveCharmItem, tag);
    }

    /** Pure form: {@code charm} says whether the item keeps charge data. */
    public static boolean accepts(boolean charm, CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return true;
        if (depth(tag, 0) > ShopRules.ITEM_NBT_DEPTH || bytes(tag) > ShopRules.ITEM_NBT_BYTES) return false;
        for (String key : tag.getAllKeys()) {
            Tag value = tag.get(key);
            boolean ok = switch (key) {
                case ThirstPurity.TAG -> value instanceof IntTag purity && purity.getAsInt() >= 0 && purity.getAsInt() <= 3;
                case PreparationMetadata.LABEL -> value instanceof ByteTag label && label.getAsByte() == 1;
                case ProtectiveCharmItem.TAG -> charm && value instanceof CompoundTag charge && charge(charge);
                default -> false;
            };
            if (!ok) return false;
        }
        return true;
    }

    private static boolean charge(CompoundTag charge) {
        return Set.of("Used").equals(charge.getAllKeys()) && charge.get("Used") instanceof IntTag used && used.getAsInt() >= 0;
    }

    /** Villager demand accepts only default prepared metadata: nothing, or Thirst's pristine purity. */
    public static boolean defaultPrepared(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) return true;
        return Set.of(ThirstPurity.TAG).equals(tag.getAllKeys()) && tag.get(ThirstPurity.TAG) instanceof IntTag purity
                && purity.getAsInt() == ThirstPurity.PRISTINE;
    }

    private static int depth(Tag tag, int depth) {
        int max = depth;
        if (tag instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) max = Math.max(max, depth(compound.get(key), depth + 1));
        } else if (tag instanceof net.minecraft.nbt.CollectionTag<?> list) {
            for (Tag value : list) max = Math.max(max, depth(value, depth + 1));
        }
        return max;
    }

    private static int bytes(CompoundTag tag) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            NbtIo.write(tag, new DataOutputStream(out));
            return out.size();
        } catch (IOException impossible) {
            return Integer.MAX_VALUE;
        }
    }
}
