package io.github.dirtyredz.herbalwitch.magic.ritual.grimoire;

import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;

import io.github.dirtyredz.herbalwitch.platform.time.TickTime;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.GardenBlessingAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.GardenBlessingPlan;
import io.github.dirtyredz.herbalwitch.magic.ritual.action.HomeProtectionAction;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualItems;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * The altar's public pages (Milestone 6): the altar itself (how it is made and used, that it holds
 * nothing and takes offerings from the payer's own inventory) and one page per action with its
 * exact offering, payer, cost and duration, read from the catalogue and the active rules. Everything
 * a non-witch needs to found the first lineage is here, readable before any discovery.
 */
public final class AltarEntries implements GrimoireContentSource {
    public static final String PREFIX = "grimoire." + HerbalWitch.MOD_ID + ".ritual.";
    public static final EntryKey ALTAR = EntryKey.of(EntryKey.Kind.STATION, AltarCatalog.id());
    public static final EntryKey CIRCLE = EntryKey.of(EntryKey.Kind.STATION, CircleCatalog.id());

    /** An action's page key, {@code guide|herbalwitch:ritual/<action>}. */
    public static EntryKey key(AltarActionSpec action) {
        return EntryKey.of(EntryKey.Kind.GUIDE, HerbalWitch.MOD_ID + ":" + action.identity());
    }

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        WitchRules rules = WitchRules.current();
        List<EntryKey> actions = new ArrayList<>();
        for (AltarActionSpec action : AltarActionSpec.values()) {
            actions.add(key(action));
        }
        List<Component> altar = List.of(Component.translatable(PREFIX + "altar.0"), Component.translatable(PREFIX + "altar.size"),
                Component.translatable(PREFIX + "altar.scope"), Component.translatable(PREFIX + "altar.crafted"));
        out.accept(GrimoireEntry.page(ALTAR, GrimoireContent.WITCHCRAFT, Component.translatable("block." + HerbalWitch.MOD_ID + "."
                + AltarCatalog.PATH.replace('/', '.')), RitualItems.ALTAR.get().getDefaultInstance(), altar, actions));
        List<Component> circle = List.of(Component.translatable(PREFIX + "circle.0"), Component.translatable(PREFIX + "circle.size"),
                Component.translatable(PREFIX + "circle.altar"), Component.translatable(PREFIX + "circle.crafted"));
        out.accept(GrimoireEntry.page(CIRCLE, GrimoireContent.WITCHCRAFT, Component.translatable("block." + HerbalWitch.MOD_ID + "."
                + CircleCatalog.PATH.replace('/', '.')), RitualItems.CIRCLE.get().getDefaultInstance(), circle,
                List.of(ALTAR, key(AltarActionSpec.GARDEN_BLESSING), key(AltarActionSpec.HOME_PROTECTION))));
        for (AltarActionSpec action : AltarActionSpec.values()) {
            List<Component> body = new ArrayList<>();
            body.add(Component.translatable(PREFIX + action.key() + ".0"));
            if (!action.offering().isEmpty()) {
                body.add(Component.translatable(PREFIX + "offering", offering(action)));
            }
            switch (action) {
                case FOUNDING -> {
                    body.add(Component.translatable(PREFIX + "founding.terms", TickTime.ceilingSeconds(rules.foundingTicks())));
                    body.add(Component.translatable(PREFIX + (rules.foundingEnabled() ? "founding.once" : "founding.disabled")));
                }
                case INITIATION -> {
                    body.add(Component.translatable(PREFIX + "initiation.terms", rules.energy().initiationCost(),
                            TickTime.ceilingSeconds(rules.initiationTicks()), TickTime.ceilingSeconds(rules.inviteExpiry())));
                    body.add(Component.translatable(PREFIX + "initiation.consent"));
                    body.add(Component.translatable(PREFIX + "initiation.designation"));
                }
                case MEDITATION -> body.add(Component.translatable(PREFIX + "meditation.terms",
                        TickTime.ceilingSeconds(rules.energy().meditationSettle()), rules.energy().meditationAmount(),
                        TickTime.ceilingSeconds(rules.energy().meditationInterval())));
                case ATTUNEMENT -> {
                    body.add(Component.translatable(PREFIX + "attunement.terms", rules.energy().attunementCost(),
                            TickTime.ceilingSeconds(rules.attunementTicks())));
                    body.add(Component.translatable(PREFIX + "attunement.capacity"));
                }
                case GARDEN_BLESSING -> {
                    body.add(Component.translatable(PREFIX + "garden_blessing.terms", rules.workings().gardenBlessingCost(),
                            TickTime.ceilingSeconds(rules.workings().gardenBlessingTicks())));
                    body.add(Component.translatable(PREFIX + "garden_blessing.area", GardenBlessingPlan.SIZE, (int) GardenBlessingAction.REACH));
                    body.add(Component.translatable(PREFIX + "circle.needed"));
                }
                case HOME_PROTECTION -> {
                    body.add(Component.translatable(PREFIX + "home_protection.terms", rules.workings().homeProtectionCost(),
                            TickTime.ceilingSeconds(rules.workings().homeProtectionTicks())));
                    body.add(Component.translatable(PREFIX + "home_protection.ward", (int) HomeProtectionAction.REACH));
                    body.add(Component.translatable(PREFIX + "circle.needed"));
                }
            }
            List<EntryKey> links = new ArrayList<>(action.circle() ? List.of(ALTAR, CIRCLE) : List.of(ALTAR));
            WitchcraftKeys.CONVERSION_PAGES.forEach(p -> links.add(WitchcraftKeys.key(p)));
            out.accept(GrimoireEntry.page(key(action), GrimoireContent.WITCHCRAFT, Component.translatable(PREFIX + action.key() + ".title"),
                    RitualItems.ALTAR.get().getDefaultInstance(), body, links));
        }
    }

    /** "1 Fresh Lavender, 1 Fresh Sage, ..." from the item registry. */
    private static Component offering(AltarActionSpec action) {
        MutableComponent text = Component.empty();
        boolean first = true;
        for (AltarActionSpec.Offering o : action.offering()) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.parse(o.item()));
            if (!first) {
                text.append(", ");
            }
            first = false;
            text.append(o.count() + " ").append(item == null ? Component.literal(o.item()) : item.getDescription());
        }
        return text;
    }
}
