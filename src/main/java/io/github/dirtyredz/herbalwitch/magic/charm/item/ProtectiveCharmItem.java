package io.github.dirtyredz.herbalwitch.magic.charm.item;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmSpec;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A protective charm: one per stack, a finite charge of equipped seconds (the rules' charm charge,
 * {@code charmChargeSeconds}) counted as seconds used on the stack itself ({@code HerbalWitchCharm.Used}),
 * so copying, storing or re-equipping can never refill it. Only the one active charm drains, while its
 * wearer is alive and loaded; a spent charm breaks. Shown with a durability bar. Witch energy is never
 * involved in wearing one.
 */
public class ProtectiveCharmItem extends Item {
    public static final String TAG = "HerbalWitchCharm";
    public static final String WEAR = "item." + HerbalWitch.MOD_ID + ".magic.charms.wear";

    private final CharmSpec spec;

    public ProtectiveCharmItem(Properties properties, CharmSpec spec) {
        super(properties.stacksTo(1));
        this.spec = spec;
    }

    public CharmSpec spec() {
        return spec;
    }

    /** Equipped seconds a fresh charm lasts under the active rules (a client reads its server's copy). */
    public static int capacity() {
        return WitchRules.current().workings().charmChargeSeconds();
    }

    public static int used(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, tag.getCompound(TAG).getInt("Used"));
    }

    public static int remaining(ItemStack stack) {
        return Math.max(0, capacity() - used(stack));
    }

    /** Records one more equipped second; returns whether the charm is now spent. */
    public static boolean spendSecond(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTagElement(TAG);
        tag.putInt("Used", used(stack) + 1);
        return remaining(stack) <= 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return used(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * remaining(stack) / Math.max(1, capacity()));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = (float) remaining(stack) / Math.max(1, capacity());
        return Mth.hsvToRgb(0.85F - 0.15F * (1 - f), 0.5F, 0.9F);
    }

    /** The bar ticking down must not replay the equip animation. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(WEAR, remaining(stack), capacity()).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
