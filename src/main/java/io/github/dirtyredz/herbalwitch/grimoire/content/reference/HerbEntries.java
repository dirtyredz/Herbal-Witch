package io.github.dirtyredz.herbalwitch.grimoire.content.reference;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbHabitat;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbTrait;
import io.github.dirtyredz.herbalwitch.botany.conditions.HerbConditions;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbTooltips;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.content.recipe.StationRecipeEntries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The fourteen herb pages: catalogue traits and growth form, and where and when the herb is found
 * from the server's live herb conditions (habitat, discovery chance, liked conditions, harvest
 * bonus), never a client's local defaults. An unknown herb still shows how to find it.
 */
public final class HerbEntries implements GrimoireContentSource {
    private static final String PREFIX = "grimoire." + HerbalWitch.MOD_ID + ".herb.";
    public static final String FORM_CROP = PREFIX + "form.crop";
    public static final String FORM_BUSH = PREFIX + "form.bush";
    public static final String HABITAT = PREFIX + "habitat";
    public static final String DISCOVERY = PREFIX + "discovery";
    public static final String DISCOVERY_NONE = PREFIX + "discovery.none";
    public static final String LIKES = PREFIX + "likes";
    public static final String BONUS = PREFIX + "bonus";
    public static final String NO_CONDITIONS = PREFIX + "no_conditions";

    public static String titleKey(HerbSpec herb) {
        return PREFIX + herb.id();
    }

    public static String preferenceKey(HerbHabitat.Preference preference) {
        return "grimoire." + HerbalWitch.MOD_ID + ".preference." + preference.id();
    }

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            Optional<HerbConditions.Condition> condition = Optional.ofNullable(inputs.herbs().get(HerbConditions.plantId(herb)));
            Component acquisition = Component.translatable(herb.starter() ? HerbTooltips.STARTER : HerbTooltips.WILD,
                    habitat(herb, condition));
            List<Component> body = new ArrayList<>();
            body.add(Component.translatable(HerbTooltips.TRAITS, traits(herb)));
            body.add(Component.translatable(herb.form() == HerbSpec.Form.CROP ? FORM_CROP : FORM_BUSH));
            body.add(acquisition);
            condition.ifPresentOrElse(c -> conditionLines(c, body), () -> body.add(Component.translatable(NO_CONDITIONS)));
            out.accept(new GrimoireEntry(HerbForms.key(herb), GrimoireContent.HERBS, Component.translatable(titleKey(herb)),
                    BotanyItems.fresh(herb).getDefaultInstance(), body,
                    StationRecipeEntries.usingAny(inputs.recipes(), HerbForms.formStacks(herb)),
                    Optional.of(acquisition), false, Optional.empty()));
        }
    }

    private static Component traits(HerbSpec herb) {
        MutableComponent traits = Component.empty();
        List<HerbTrait> list = herb.traits();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                traits.append(", ");
            }
            traits.append(Component.translatable(HerbTooltips.traitKey(list.get(i))));
        }
        return traits;
    }

    /** The habitat's name when the server keeps the default tag, else the tag id it uses instead. */
    private static Component habitat(HerbSpec herb, Optional<HerbConditions.Condition> condition) {
        String tag = condition.map(HerbConditions.Condition::habitat).orElse(HerbConditions.habitatTagId(herb));
        return tag.equals(HerbConditions.habitatTagId(herb))
                ? Component.translatable(HerbTooltips.habitatKey(herb.habitat())) : Component.literal("#" + tag);
    }

    private static void conditionLines(HerbConditions.Condition c, List<Component> body) {
        body.add(c.discoveryChance() <= 0 ? Component.translatable(DISCOVERY_NONE)
                : Component.translatable(DISCOVERY, oneIn(c.discoveryChance())));
        if (!c.preferences().isEmpty()) {
            MutableComponent likes = Component.empty();
            for (int i = 0; i < c.preferences().size(); i++) {
                if (i > 0) {
                    likes.append(", ");
                }
                likes.append(Component.translatable(preferenceKey(c.preferences().get(i))));
            }
            body.add(Component.translatable(LIKES, likes));
        }
        body.add(Component.translatable(BONUS, percent(c.harvestBonusChance())));
    }

    /** {@code 32} for 1/32, rounded. */
    static String oneIn(double chance) {
        return String.valueOf(Math.max(1, Math.round(1 / chance)));
    }

    static String percent(double chance) {
        return String.format(Locale.ROOT, "%d", Math.round(chance * 100));
    }
}
