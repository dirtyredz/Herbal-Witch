package io.github.dirtyredz.herbalwitch.companions.animals.capture;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureRecord;
import io.github.dirtyredz.herbalwitch.companions.animals.registry.AnimalItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import java.util.Set;
import java.util.UUID;

/** Presentation is never authority. Only this opaque token is resolved against the server ledger. */
public final class CaptureToken {
    private CaptureToken() {}
    public static ItemStack item(CaptureRecord record) {
        ItemStack stack = new ItemStack(AnimalItems.FILLED_CHARM.get());
        CompoundTag tag = new CompoundTag(); tag.putInt("version", 1); tag.putUUID("token", record.token()); tag.putString("summary", record.summary());
        stack.addTagElement("Capture", tag); return stack;
    }
    public static UUID read(ItemStack stack) {
        if (!stack.is(AnimalItems.FILLED_CHARM.get()) || stack.getCount() != 1 || !stack.hasTag() || !stack.getTag().contains("Capture", Tag.TAG_COMPOUND)) return null;
        CompoundTag tag = stack.getTag().getCompound("Capture");
        if (!tag.getAllKeys().equals(Set.of("version", "token", "summary")) || !tag.contains("version", Tag.TAG_INT) || tag.getInt("version") != 1
                || !tag.hasUUID("token") || !tag.contains("summary", Tag.TAG_STRING) || tag.getString("summary").length() > 128) return null;
        return tag.getUUID("token");
    }
}
