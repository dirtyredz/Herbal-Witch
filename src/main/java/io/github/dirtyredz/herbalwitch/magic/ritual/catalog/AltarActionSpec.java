package io.github.dirtyredz.herbalwitch.magic.ritual.catalog;

import io.github.dirtyredz.herbalwitch.HerbalWitch;

import java.util.List;
import java.util.Locale;

/**
 * The altar's actions, plain data (Milestone 6's four and the circle's two of Milestone 7): each one's permanent action identity
 * ({@code ritual/<name>}; not a recipe type) and the offering it takes from the payer's own main
 * inventory and offhand at completion. Durations and energy come from the server's rules.
 */
public final class AltarActionSpec {
    public enum Kind { FOUNDING, INITIATION, MEDITATION, ATTUNEMENT, GARDEN_BLESSING, HOME_PROTECTION, CONTRIBUTED }
    private static final java.util.Map<String, AltarActionSpec> ALL = new java.util.LinkedHashMap<>();
    /** A non-witch founds the world's first lineage: offering only, no energy. */
    public static final AltarActionSpec FOUNDING = builtin(Kind.FOUNDING, List.of(offering("botany/temperate/lavender/fresh"), offering("botany/temperate/sage/fresh"),
            offering("botany/wet/mint/fresh"), offering("botany/temperate/chamomile/fresh"),
            new Offering("minecraft:amethyst_shard", 1), new Offering("minecraft:written_book", 1)));
    /** A witch initiates a consenting non-witch: the witch supplies the offering and pays the energy. */
    public static final AltarActionSpec INITIATION = builtin(Kind.INITIATION, List.of(offering("botany/temperate/sage/fresh"), offering("botany/temperate/lavender/fresh"),
            new Offering("minecraft:amethyst_shard", 1)));
    /** A witch meditates at an idle altar to recover energy. */
    public static final AltarActionSpec MEDITATION = builtin(Kind.MEDITATION, List.of());
    /** A witch's optional practice: a fresh sage and a little energy, for capacity progression. */
    public static final AltarActionSpec ATTUNEMENT = builtin(Kind.ATTUNEMENT, List.of(offering("botany/temperate/sage/fresh")));
    /** With a complete circle: enrich the existing herb beds of a chosen 5 x 5 area (Milestone 7). */
    public static final AltarActionSpec GARDEN_BLESSING = builtin(Kind.GARDEN_BLESSING, List.of(offering("preparations/herbs/wet/nettle/powder"), new Offering("minecraft:bone_meal", 1),
            offering("preparations/garden/soil_tonic")));
    /** With a complete circle: charge an empty ward stone the witch may bind (Milestone 7). */
    public static final AltarActionSpec HOME_PROTECTION = builtin(Kind.HOME_PROTECTION, List.of(offering("preparations/herbs/temperate/sage/powder"), new Offering("minecraft:amethyst_shard", 1)));

    /** One offered item: a registry id and how many. A written book must be a normally valid signed book. */
    public record Offering(String item, int count) {
    }

    private final List<Offering> offering;
    private final String identity;
    private final Kind kind;
    private final int ticks, energy;
    private AltarActionSpec(String identity, Kind kind, List<Offering> offering, int ticks, int energy) {
        this.identity = identity; this.kind = kind; this.offering = List.copyOf(offering); this.ticks = ticks; this.energy = energy;
    }
    private static AltarActionSpec builtin(Kind kind, List<Offering> offering) {
        var action = new AltarActionSpec("ritual/" + kind.name().toLowerCase(Locale.ROOT), kind, offering, 0, 0);
        ALL.put(action.identity(), action); return action;
    }
    public static AltarActionSpec register(String identity, List<Offering> offering, int ticks, int energy) {
        if (!identity.matches("ritual/[a-z0-9_/]{1,64}") || offering.size() > 8 || ticks < 1 || energy < 0 || ALL.size() >= 32)
            throw new IllegalArgumentException("Invalid contributed altar action");
        var action = new AltarActionSpec(identity, Kind.CONTRIBUTED, offering, ticks, energy);
        if (ALL.putIfAbsent(identity, action) != null) throw new IllegalArgumentException("Duplicate altar action " + identity);
        return action;
    }
    public static AltarActionSpec[] values() { return ALL.values().toArray(AltarActionSpec[]::new); }
    public static java.util.List<AltarActionSpec> builtins() { return ALL.values().stream().filter(a -> a.kind != Kind.CONTRIBUTED).toList(); }
    public static AltarActionSpec byIdentity(String identity) {
        var action = ALL.get(identity); if (action == null) throw new IllegalArgumentException("Unknown altar action"); return action;
    }
    private static Offering offering(String path) { return new Offering(HerbalWitch.MOD_ID + ":" + path, 1); }
    public List<Offering> offering() { return offering; }
    public String identity() { return identity; }
    public String key() { return identity.substring(7).replace('/', '.'); }
    public Kind kind() { return kind; }
    public int ticks() { return ticks; }
    public int energy() { return energy; }
    public boolean circle() { return this == GARDEN_BLESSING || this == HOME_PROTECTION || kind == Kind.CONTRIBUTED; }
}
