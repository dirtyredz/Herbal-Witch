package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.metadata;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The only data a Milestone 7 preparation carries, both bounded: the water metadata that rides from
 * the cauldron's water into its mixture and from the mixture into every bottle it fills (through
 * {@link DrinkMetadata}, so only an installed carrier such as Thirst's purity copies anything, and
 * never improved on the way), and the bottling bench's label: one boolean that makes the tooltip name
 * the preparation's effect and delivery. No free text, nothing executable, nothing a client writes.
 */
public final class PreparationMetadata {
    /** The label flag (a byte), set by the labelled bottling recipes' results. */
    public static final String LABEL = "HerbalWitchLabel";
    public static final String LABEL_KEY = "item." + HerbalWitch.MOD_ID + ".preparations.label";

    private PreparationMetadata() {
    }

    /** {@code product} with the water metadata of {@code source} (a water bottle or a mixture). */
    public static ItemStack carry(ItemStack source, ItemStack product) {
        return DrinkMetadata.carry(source, product);
    }

    /** A labelled copy of {@code product}. */
    public static ItemStack labelled(ItemStack product) {
        ItemStack out = product.copy();
        out.getOrCreateTag().putBoolean(LABEL, true);
        return out;
    }

    public static boolean isLabelled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(LABEL);
    }

    /** The generated label line: "Labelled: <name> (<delivery>)". */
    public static void appendLabel(ItemStack stack, List<Component> lines) {
        if (isLabelled(stack)) {
            lines.add(Component.translatable(LABEL_KEY, stack.getItem().getDescription()).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
