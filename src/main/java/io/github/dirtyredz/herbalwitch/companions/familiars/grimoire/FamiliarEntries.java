package io.github.dirtyredz.herbalwitch.companions.familiars.grimoire;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.FamiliarPower;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarServerConfig;
import io.github.dirtyredz.herbalwitch.companions.familiars.preparation.FamiliarBuffRules;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.*;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.FamiliarPreparationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.WitchcraftKeys;
import io.github.dirtyredz.herbalwitch.grimoire.content.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import java.util.*;
import java.util.function.Consumer;

/** The familiar feature owns its guide, with live catalogue/configuration facts and existing recipe links. */
public final class FamiliarEntries implements GrimoireContentSource {
    public static final int PRIORITY = 301;
    public static final List<String> PAGES = List.of("species", "commands", "homes", "equipment", "abilities", "preparations", "progression", "recovery");
    public static EntryKey key(String page) { return EntryKey.of(EntryKey.Kind.GUIDE, "herbalwitch:familiars/" + page); }
    private static Component text(String key, Object... args) { return Component.translatable("familiar.herbalwitch.guide." + key, args); }
    private static Component species(String key) { return Component.translatable("entity.herbalwitch.familiars.entity." + key); }
    @Override public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        var rules = FamiliarServerConfig.current(); var links = new ArrayList<EntryKey>();
        links.add(WitchcraftKeys.key("brewing")); links.add(WitchcraftKeys.key("preparations")); PAGES.forEach(p -> links.add(key(p)));
        out.accept(GrimoireEntry.page(WitchcraftKeys.key("familiar"), GrimoireContent.WITCHCRAFT,
                text("title"), FamiliarItems.gear("cat_bell").getDefaultInstance(),
                List.of(text("overview"), text("limits", rules.mobile(), rules.offlineGuards(), rules.guardRadius(), rules.senseRadius())), links));
        var body = new ArrayList<Component>(); body.add(text("species.intro"));
        for (var s : FamiliarCatalog.ALL) body.add(text("species.row", species(s.key()), Component.translatable("item.herbalwitch.animals.bait." + s.bait()),
                s.trust(), s.feeds(), text(rules.binding() && rules.species().contains(s.key()) ? "enabled" : "disabled")));
        for (var wild : io.github.dirtyredz.herbalwitch.companions.familiars.acquisition.FamiliarWildSpawns.ALL)
            body.add(text("species.wild", species(wild.species()), wild.weight(), wild.biomes().location().toString()));
        page(out, "species", FamiliarItems.egg("raven"), body, List.of(io.github.dirtyredz.herbalwitch.companions.animals.grimoire.AnimalEntries.KEY));
        body = new ArrayList<>();
        for (var command : io.github.dirtyredz.herbalwitch.companions.familiars.command.FamiliarCommand.values()) body.add(text("command." + command.key()));
        body.add(text("commands.limits", rules.mobile(), rules.guardRadius(), rules.senseRadius()));
        page(out, "commands", Items.COMPASS, body, List.of(key("homes"), key("abilities")));
        body = new ArrayList<>(); body.add(text("homes.rules"));
        for (var home : FamiliarHomeKind.values()) body.add(text("homes.row", Component.translatable("block.herbalwitch." + home.path().replace('/', '.')),
                String.join(", ", FamiliarCatalog.ALL.stream().filter(s -> home.accepts(s.key())).map(FamiliarSpeciesSpec::key).toList())));
        page(out, "homes", FamiliarHomes.get(FamiliarHomeKind.BED).item().get(), body, List.of(key("recovery")));
        body = new ArrayList<>(); body.add(text("equipment.rules")); var recipes = new ArrayList<EntryKey>();
        for (var gear : FamiliarEquipmentCatalog.ALL) {
            body.add(text("equipment.row", FamiliarItems.gear(gear.key()).getDescription(), species(gear.species()), text("gear." + gear.key())));
            recipes.add(EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:processing/charm_work/" + gear.path()));
        }
        page(out, "equipment", FamiliarItems.gear("raven_satchel"), body, recipes);
        body = new ArrayList<>(); body.add(text("abilities.rules", rules.scans(), rules.paths()));
        for (var power : FamiliarPower.values()) body.add(text("abilities.row", Component.translatable("familiar.herbalwitch.power." + power.key()), power.energy(), power.ticks() / 20.0));
        body.add(text("abilities.effects")); body.add(text("abilities.reserve")); body.add(text("abilities.retrieval", rules.publicRetrieval() ? text("enabled") : text("disabled")));
        page(out, "abilities", Items.AMETHYST_SHARD, body, List.of(key("equipment"), key("preparations")));
        body = new ArrayList<>(); body.add(text("preparations.rules", FamiliarBuffRules.DURATION / 20, FamiliarBuffRules.APPLICATION_GATE / 20));
        for (var preparation : FamiliarPreparationCatalog.ALL)
            body.add(text("preparations.row", AdvancedPreparations.item(preparation).getDescription(), text("preparation." + preparation.id())));
        body.add(text("preparations.shared"));
        page(out, "preparations", AdvancedPreparations.item(FamiliarPreparationCatalog.GUARDIAN), body, List.of(WitchcraftKeys.key("preparations")));
        body = new ArrayList<>(); body.add(text("progression.rules"));
        for (var action : FamiliarActionCatalog.ALL) body.add(text("ritual.row", Component.translatable("grimoire.herbalwitch.ritual." + action.key() + ".title"),
                action.energy(), action.ticks() / 20.0, Component.translatable("grimoire.herbalwitch.ritual." + action.key() + ".offering")));
        page(out, "progression", Items.GOLD_INGOT, body, List.of(key("recovery"), key("species")));
        page(out, "recovery", Items.TOTEM_OF_UNDYING, List.of(text("recovery.rules"), text("recovery.revival"), text("recovery.admin")), List.of(key("progression"), key("homes")));
    }
    private static void page(Consumer<GrimoireEntry> out, String id, Item icon, List<Component> body, List<EntryKey> links) {
        out.accept(GrimoireEntry.page(key(id), GrimoireContent.WITCHCRAFT, text(id + ".title"), icon.getDefaultInstance(), body, links));
    }
}
