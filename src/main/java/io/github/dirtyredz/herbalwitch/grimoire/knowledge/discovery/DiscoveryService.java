package io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.HerbForms;
import io.github.dirtyredz.herbalwitch.grimoire.network.DiscoveryUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.research.Familiarity;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The one way discoveries are granted and synchronised, server side. Grants are idempotent (a
 * duplicate is a no-op) and only new keys are sent, privately, to the player who gained them.
 * {@link #observe} applies the acquisition policy to what the player confirmedly carries:
 *
 * <ul>
 *   <li>any registered form of a herb (seeds, fresh, bundle, powder) makes the herb known;</li>
 *   <li>carrying a recipe's result makes that recipe known;</li>
 *   <li>a recipe whose primary ingredients are all familiar becomes known.</li>
 * </ul>
 *
 * Research and accepted teaching grant through {@link #grant}. Later features grant their own
 * keys the same way. Knowledge never gates crafting, processing, automation or use.
 */
public final class DiscoveryService {
    private static final AtomicInteger SNAPSHOTS = new AtomicInteger();

    private DiscoveryService() {
    }

    public static boolean knows(Player player, EntryKey key) {
        return DiscoveryCapability.of(player).map(c -> c.knows(key)).orElse(false);
    }

    /** Grants one key; returns whether it was new. */
    public static boolean grant(ServerPlayer player, EntryKey key) {
        return !grantAll(player, List.of(key)).isEmpty();
    }

    /** Grants keys, sends only the new ones to their owner, and returns them. */
    public static List<EntryKey> grantAll(ServerPlayer player, Collection<EntryKey> keys) {
        Optional<DiscoveryCapability> capability = DiscoveryCapability.of(player);
        if (capability.isEmpty()) {
            return List.of();
        }
        List<EntryKey> added = new ArrayList<>();
        for (EntryKey key : new LinkedHashSet<>(keys)) {
            if (capability.get().grant(key) == DiscoveryState.Grant.ADDED) {
                added.add(key);
            }
        }
        if (!added.isEmpty()) {
            DiscoveryUpdate.delta(added).forEach(m -> GrimoirePackets.send(player, m));
        }
        return added;
    }

    /** Sends the player's complete knowledge (login, respawn, dimension change). */
    public static void sendSnapshot(ServerPlayer player) {
        DiscoveryCapability.of(player).ifPresent(c -> DiscoveryUpdate.snapshot(SNAPSHOTS.incrementAndGet(), List.copyOf(c.state().known()))
                .forEach(m -> GrimoirePackets.send(player, m)));
    }

    /**
     * Applies the acquisition policy to the main inventory and offhand. Skipped when neither the
     * inventory nor the content revision changed since the last observation.
     */
    public static List<EntryKey> observe(ServerPlayer player) {
        Optional<DiscoveryCapability> capability = DiscoveryCapability.of(player);
        if (capability.isEmpty() || player.getServer() == null) {
            return List.of();
        }
        GrimoireContent content = ServerGrimoireContent.get(player.getServer());
        List<ItemStack> carried = Familiarity.carried(player);
        long fingerprint = Familiarity.fingerprint(carried);
        DiscoveryCapability cap = capability.get();
        if (cap.observedAlready(fingerprint, content.revision())) {
            return List.of();
        }
        cap.markObserved(fingerprint, content.revision());
        List<EntryKey> granted = new ArrayList<>(grantAll(player, herbsIn(carried)));
        Familiarity familiarity = new Familiarity(carried, cap::knows);
        Set<EntryKey> recipes = new LinkedHashSet<>();
        for (GrimoireEntry entry : content.recipes()) {
            if (cap.knows(entry.key())) {
                continue;
            }
            ProcessRecipe recipe = entry.recipe().orElseThrow();
            ItemStack result = recipe.description().result();
            boolean carriesResult = carried.stream().anyMatch(s -> ItemStack.isSameItemSameTags(s, result));
            if (carriesResult || Familiarity.eligibility(recipe.description()).automatic(familiarity)) {
                recipes.add(entry.key());
            }
        }
        granted.addAll(grantAll(player, recipes));
        return granted;
    }

    private static List<EntryKey> herbsIn(List<ItemStack> carried) {
        Set<EntryKey> herbs = new LinkedHashSet<>();
        for (ItemStack stack : carried) {
            HerbForms.herbOf(stack.getItem()).ifPresent(h -> herbs.add(HerbForms.key(h)));
        }
        return List.copyOf(herbs);
    }
}
