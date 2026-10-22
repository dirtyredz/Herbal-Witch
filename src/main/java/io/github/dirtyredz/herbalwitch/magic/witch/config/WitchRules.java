package io.github.dirtyredz.herbalwitch.magic.witch.config;

import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * The active, validated rules of initiation and energy: the server's (from its configuration) and,
 * on a client, the copy that server sent. Changes are all-or-nothing: an invalid set is refused and
 * the previous one stays. Listeners hear every accepted server change (to cancel pending consent and
 * rituals and to clamp energy without refilling it).
 *
 * @param foundingEnabled   whether a survival player may found the first lineage
 * @param initiationEnabled whether witches may initiate others
 * @param foundingTicks     loaded ticks a founding ritual lasts
 * @param initiationTicks   loaded ticks an initiation lasts
 * @param attunementTicks   loaded ticks an attunement lasts
 * @param inviteExpiry      ticks an initiation invitation stays open
 * @param inviteCooldown    ticks between one witch's invitations
 * @param energy            the energy economy
 * @param workings          what magical processing, circle rituals, wards and charms cost or last (Milestone 7)
 */
public record WitchRules(boolean foundingEnabled, boolean initiationEnabled, int foundingTicks, int initiationTicks,
                         int attunementTicks, int inviteExpiry, int inviteCooldown, EnergyBalance energy, WorkingRules workings) {
    public static final WitchRules DEFAULTS = new WitchRules(true, true, 200, 200, 100, 600, 100, EnergyBalance.DEFAULTS);

    /** Rules with the default workings (Milestone 6 callers). */
    public WitchRules(boolean foundingEnabled, boolean initiationEnabled, int foundingTicks, int initiationTicks, int attunementTicks,
                      int inviteExpiry, int inviteCooldown, EnergyBalance energy) {
        this(foundingEnabled, initiationEnabled, foundingTicks, initiationTicks, attunementTicks, inviteExpiry, inviteCooldown, energy,
                WorkingRules.DEFAULTS);
    }

    private static volatile WitchRules server = DEFAULTS;
    private static volatile WitchRules client = DEFAULTS;
    private static final List<Consumer<WitchRules>> LISTENERS = new CopyOnWriteArrayList<>();

    /** Why these rules are unusable; empty when valid. */
    public List<String> problems() {
        List<String> out = new ArrayList<>(energy.problems());
        out.addAll(workings.problems());
        for (int[] t : new int[][] {{foundingTicks}, {initiationTicks}, {attunementTicks}, {inviteExpiry}}) {
            if (t[0] < 1 || t[0] > EnergyBalance.MAX_TICKS) {
                out.add("durations must be 1.." + EnergyBalance.MAX_TICKS);
                break;
            }
        }
        if (inviteCooldown < 0 || inviteCooldown > EnergyBalance.MAX_TICKS) {
            out.add("inviteCooldown outside 0.." + EnergyBalance.MAX_TICKS);
        }
        return out;
    }

    /**
     * A stable fingerprint of every value (the same on client and server, unlike a record's hash),
     * so a request made against stale rules is refused.
     */
    public int fingerprint() {
        EnergyBalance e = energy;
        int h = 17;
        for (int v : new int[] {foundingEnabled ? 1 : 0, initiationEnabled ? 1 : 0, foundingTicks, initiationTicks, attunementTicks,
                inviteExpiry, inviteCooldown, e.initialCapacity(), e.capacityPerThreshold(), e.initiationCost(), e.attunementCost(),
                e.restInterval(), e.restAmount(), e.meditationSettle(), e.meditationInterval(), e.meditationAmount(), e.sleepAmount(),
                e.teaAmount(), e.teaCooldown(), e.restfulBonus(), e.restfulDuration(), e.natureBonus(), e.moonBonus(), e.bonusCap()}) {
            h = h * 31 + v;
        }
        for (int t : e.thresholds()) {
            h = h * 31 + t;
        }
        for (int v : workings.numbers()) {
            h = h * 31 + v;
        }
        return h;
    }

    /** The server's rules. */
    public static WitchRules server() {
        return server;
    }

    /** The rules of the side running this code (a client reads its server's copy). */
    public static WitchRules current() {
        return EffectiveSide.get().isServer() ? server : client;
    }

    /**
     * Installs new server rules if they are valid; returns the problems otherwise (the previous rules
     * stay). Listeners run only for an accepted change that differs from the current rules.
     */
    public static List<String> applyServer(WitchRules rules) {
        List<String> problems = rules.problems();
        if (!problems.isEmpty()) {
            return problems;
        }
        boolean changed = !rules.equals(server);
        server = rules;
        if (changed) {
            LISTENERS.forEach(l -> l.accept(rules));
        }
        return List.of();
    }

    /** Server start or stop: back to the defaults without notifying anyone. */
    public static void resetServer() {
        server = DEFAULTS;
    }

    /** A client installs what its server sent (already validated there). */
    public static void applyClient(WitchRules rules) {
        client = rules.problems().isEmpty() ? rules : client;
    }

    /** Disconnect: forget the last server's rules. */
    public static void resetClient() {
        client = DEFAULTS;
    }

    /** Hears every accepted change of the server's rules (registered at bootstrap). */
    public static void listen(Consumer<WitchRules> listener) {
        LISTENERS.add(listener);
    }
}
