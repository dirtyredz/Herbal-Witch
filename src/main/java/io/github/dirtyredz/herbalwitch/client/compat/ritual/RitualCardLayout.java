package io.github.dirtyredz.herbalwitch.client.compat.ritual;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WorkingRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * The one ritual card EMI and JEI share (category {@code herbalwitch:ritual}), read from the altar's
 * own action catalogue and the active rules, the same sources as the grimoire's ritual pages: the
 * offering on the top row, then what it needs (the altar, or the altar inside a complete circle), its
 * duration and energy, and its result in words. No item outputs: a ritual's results are not items.
 */
public final class RitualCardLayout {
    public static final ResourceLocation CATEGORY = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "ritual");
    public static final String TITLE = "category." + HerbalWitch.MOD_ID + ".ritual";
    public static final int WIDTH = 150;
    public static final int HEIGHT = 62;
    public static final int SLOT = 18;
    public static final int TEXT_Y = 22;
    private static final String KEY = "category." + HerbalWitch.MOD_ID + ".ritual.";

    private RitualCardLayout() {
    }

    /** The actions shown (every one has a card). */
    public static List<AltarActionSpec> actions() {
        return List.of(AltarActionSpec.values());
    }

    public static int slotX(int index) {
        return index * SLOT;
    }

    /** The offering's stacks, in order. */
    public static List<ItemStack> offering(AltarActionSpec action) {
        List<ItemStack> out = new ArrayList<>();
        for (AltarActionSpec.Offering o : action.offering()) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(o.item()));
            if (item != null) {
                out.add(new ItemStack(item, o.count()));
            }
        }
        return out;
    }

    /** The card's text lines: name, needs, terms, result. */
    public static List<Component> lines(AltarActionSpec action) {
        WitchRules rules = WitchRules.current();
        WorkingRules w = rules.workings();
        EnergyBalance e = rules.energy();
        Component terms = switch (action) {
            case FOUNDING -> Component.translatable(KEY + "time", seconds(rules.foundingTicks()));
            case INITIATION -> Component.translatable(KEY + "terms", seconds(rules.initiationTicks()), e.initiationCost());
            case MEDITATION -> Component.translatable(KEY + "open");
            case ATTUNEMENT -> Component.translatable(KEY + "terms", seconds(rules.attunementTicks()), e.attunementCost());
            case GARDEN_BLESSING -> Component.translatable(KEY + "terms", seconds(w.gardenBlessingTicks()), w.gardenBlessingCost());
            case HOME_PROTECTION -> Component.translatable(KEY + "terms", seconds(w.homeProtectionTicks()), w.homeProtectionCost());
        };
        return List.of(Component.translatable("grimoire." + HerbalWitch.MOD_ID + ".ritual." + action.key() + ".title"),
                Component.translatable(KEY + (action.circle() ? "needs_circle" : "needs_altar")), terms,
                Component.translatable(KEY + "result." + action.key()));
    }

    private static int seconds(int ticks) {
        return (ticks + 19) / 20;
    }
}
