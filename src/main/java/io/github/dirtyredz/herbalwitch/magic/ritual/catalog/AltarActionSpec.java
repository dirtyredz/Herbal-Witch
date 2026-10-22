package io.github.dirtyredz.herbalwitch.magic.ritual.catalog;

import io.github.dirtyredz.herbalwitch.HerbalWitch;

import java.util.List;
import java.util.Locale;

/**
 * The altar's four implemented actions, plain data: each one's permanent action identity
 * ({@code ritual/<name>}; not a recipe type) and the offering it takes from the payer's own main
 * inventory and offhand at completion. Durations and energy come from the server's rules.
 */
public enum AltarActionSpec {
    /** A non-witch founds the world's first lineage: offering only, no energy. */
    FOUNDING(List.of(offering("botany/temperate/lavender/fresh"), offering("botany/temperate/sage/fresh"),
            offering("botany/wet/mint/fresh"), offering("botany/temperate/chamomile/fresh"),
            new Offering("minecraft:amethyst_shard", 1), new Offering("minecraft:written_book", 1))),
    /** A witch initiates a consenting non-witch: the witch supplies the offering and pays the energy. */
    INITIATION(List.of(offering("botany/temperate/sage/fresh"), offering("botany/temperate/lavender/fresh"),
            new Offering("minecraft:amethyst_shard", 1))),
    /** A witch meditates at an idle altar to recover energy. */
    MEDITATION(List.of()),
    /** A witch's optional practice: a fresh sage and a little energy, for capacity progression. */
    ATTUNEMENT(List.of(offering("botany/temperate/sage/fresh")));

    /** One offered item: a registry id and how many. A written book must be a normally valid signed book. */
    public record Offering(String item, int count) {
    }

    private final List<Offering> offering;

    AltarActionSpec(List<Offering> offering) {
        this.offering = List.copyOf(offering);
    }

    private static Offering offering(String path) {
        return new Offering(HerbalWitch.MOD_ID + ":" + path, 1);
    }

    public List<Offering> offering() {
        return offering;
    }

    /** {@code ritual/<name>}. */
    public String identity() {
        return "ritual/" + name().toLowerCase(Locale.ROOT);
    }

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }
}
