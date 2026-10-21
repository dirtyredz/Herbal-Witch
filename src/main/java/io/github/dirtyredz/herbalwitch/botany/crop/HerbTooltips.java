package io.github.dirtyredz.herbalwitch.botany.crop;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbHabitat;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbTrait;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/** Tooltip lines shared by a herb's seeds and fresh herb: traits, and where to find it. */
public final class HerbTooltips {
    public static final String TRAITS = "tooltip." + HerbalWitch.MOD_ID + ".herb.traits";
    public static final String STARTER = "tooltip." + HerbalWitch.MOD_ID + ".herb.starter";
    public static final String WILD = "tooltip." + HerbalWitch.MOD_ID + ".herb.wild";

    private HerbTooltips() {
    }

    public static String traitKey(HerbTrait trait) {
        return "trait." + HerbalWitch.MOD_ID + "." + trait.id();
    }

    public static String habitatKey(HerbHabitat habitat) {
        return "habitat." + HerbalWitch.MOD_ID + "." + habitat.directory();
    }

    public static void append(HerbSpec herb, List<Component> lines) {
        MutableComponent traits = Component.empty();
        for (int i = 0; i < herb.traits().size(); i++) {
            if (i > 0) {
                traits.append(", ");
            }
            traits.append(Component.translatable(traitKey(herb.traits().get(i))));
        }
        lines.add(Component.translatable(TRAITS, traits).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable(herb.starter() ? STARTER : WILD,
                Component.translatable(habitatKey(herb.habitat()))).withStyle(ChatFormatting.DARK_GREEN));
    }
}
