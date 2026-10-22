package io.github.dirtyredz.herbalwitch.grimoire.content;

import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinition;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * One page of the grimoire, immutable. A recipe entry holds the live {@link ProcessRecipe}, and its
 * page is drawn from that recipe's description, never from a copy: ingredients, counts, result,
 * returns, time and heat are always the current recipe's. Editorial definitions add prose, grouping,
 * links and hints only.
 *
 * @param key      permanent identity
 * @param category grouping id ({@link GrimoireContent#GUIDE}, {@code herbs}, {@code stations} or a
 *                 processing category)
 * @param title    page title
 * @param icon     item shown beside the title
 * @param body     prose and generated facts, in order
 * @param links    related entries (only ones present in the content survive the build)
 * @param hint     how to discover it, shown while it is unknown
 * @param always   public guidance, readable before any discovery
 * @param recipe   the live recipe of a recipe entry
 */
public record GrimoireEntry(EntryKey key, String category, Component title, ItemStack icon, List<Component> body,
                            List<EntryKey> links, Optional<Component> hint, boolean always, Optional<ProcessRecipe> recipe) {
    public GrimoireEntry {
        body = List.copyOf(body);
        links = List.copyOf(new LinkedHashSet<>(links));
        icon = icon.copy();
    }

    @Override
    public ItemStack icon() {
        return icon.copy();
    }

    /** This entry with an editorial definition laid over it: prose first, then the generated facts. */
    public GrimoireEntry withEditorial(GrimoireDefinition definition) {
        List<Component> text = new ArrayList<>();
        definition.pages().forEach(page -> text.add(Component.translatable(page)));
        text.addAll(body);
        List<EntryKey> allLinks = new ArrayList<>(definition.links());
        allLinks.addAll(links);
        return new GrimoireEntry(key, definition.category().orElse(category),
                definition.title().<Component>map(Component::translatable).orElse(title), icon, text, allLinks,
                definition.hint().<Component>map(Component::translatable).or(() -> hint), always, recipe);
    }

    /** This entry keeping only links that pass {@code present}. */
    public GrimoireEntry withLinksIn(java.util.function.Predicate<EntryKey> present) {
        return new GrimoireEntry(key, category, title, icon, body, links.stream().filter(present).toList(), hint, always, recipe);
    }
}
