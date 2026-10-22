package io.github.dirtyredz.herbalwitch.grimoire.content.reference;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinition;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.network.chat.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The public guidance pages: every editorial definition of a guide becomes a page readable before
 * any discovery. The mod's own guides come first in reading order ({@link #ORDER}); datapack guides
 * follow by id.
 */
public final class GuideEntries implements GrimoireContentSource {
    /** The mod's guides, in reading order. */
    public static final List<String> ORDER = List.of("getting_started", "acquisition", "preparation", "stations", "heat",
            "products", "missing_ingredients", "research", "teaching");

    public static EntryKey key(String name) {
        return EntryKey.of(EntryKey.Kind.GUIDE, HerbalWitch.MOD_ID + ":" + name);
    }

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        inputs.definitions().values().stream()
                .filter(d -> d.key().kind() == EntryKey.Kind.GUIDE)
                .sorted(Comparator.comparingInt(GuideEntries::rank).thenComparing(d -> d.key().id()))
                .forEach(d -> out.accept(entry(d)));
    }

    private static int rank(GrimoireDefinition definition) {
        int i = ORDER.indexOf(definition.key().id().substring(HerbalWitch.MOD_ID.length() + 1));
        return definition.key().id().startsWith(HerbalWitch.MOD_ID + ":") && i >= 0 ? i : ORDER.size();
    }

    private static GrimoireEntry entry(GrimoireDefinition d) {
        List<Component> pages = d.pages().stream().<Component>map(Component::translatable).toList();
        return new GrimoireEntry(d.key(), d.category().orElse(GrimoireContent.GUIDE),
                Component.translatable(d.title().orElseThrow()), GrimoireItems.BOOK.get().getDefaultInstance(), pages,
                d.links(), d.hint().<Component>map(Component::translatable), true, Optional.empty());
    }
}
