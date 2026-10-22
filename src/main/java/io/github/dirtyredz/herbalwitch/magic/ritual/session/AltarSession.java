package io.github.dirtyredz.herbalwitch.magic.ritual.session;

import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarActionSpec;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * One operation at one altar, transient (never saved, never resumed): what it is, who takes part
 * (each with the altar menu session they must keep open), how far it has run, and what it captured
 * when it began (the offering's exact stacks, a founding claim, an accepted invitation). Its state
 * moves RUNNING to COMPLETED or CANCELLED exactly once ({@link AltarSessions}).
 */
public final class AltarSession {
    public enum State {
        RUNNING,
        COMPLETED,
        CANCELLED
    }

    private final long id;
    private final AltarActionSpec action;
    private final RitualSite site;
    private final Optional<UUID> payer;
    private final UUID subject;
    private final int duration;
    private final Map<UUID, Long> menus = new LinkedHashMap<>();
    private State state = State.RUNNING;
    private int elapsed;
    private Object payload;

    /**
     * @param payer    who supplies the offering and the energy (empty for an administrative designation)
     * @param subject  who is converted (founding, initiation) or who acts (meditation, attunement)
     * @param duration ticks until completion; 0 for an open-ended meditation
     * @param menus    each participant's altar menu nonce
     */
    public AltarSession(long id, AltarActionSpec action, RitualSite site, Optional<UUID> payer, UUID subject, int duration,
                        Map<UUID, Long> menus) {
        this.id = id;
        this.action = action;
        this.site = site;
        this.payer = payer;
        this.subject = subject;
        this.duration = duration;
        this.menus.putAll(menus);
    }

    public long id() {
        return id;
    }

    public AltarActionSpec action() {
        return action;
    }

    public RitualSite site() {
        return site;
    }

    public Optional<UUID> payer() {
        return payer;
    }

    public UUID subject() {
        return subject;
    }

    public int duration() {
        return duration;
    }

    /** Every participant and the menu nonce they keep open. */
    public Map<UUID, Long> menus() {
        return Map.copyOf(menus);
    }

    public List<UUID> participants() {
        return List.copyOf(menus.keySet());
    }

    public boolean involves(UUID player) {
        return menus.containsKey(player);
    }

    public State state() {
        return state;
    }

    public boolean running() {
        return state == State.RUNNING;
    }

    public int elapsed() {
        return elapsed;
    }

    /** One more loaded tick of progress; returns the new elapsed count. */
    public int advance() {
        return ++elapsed;
    }

    /** Whether a timed operation has run its full duration. */
    public boolean due() {
        return duration > 0 && elapsed >= duration;
    }

    /** What the action captured when it began (offering, claim, invitation); its own type. */
    @SuppressWarnings("unchecked")
    public <T> T payload() {
        return (T) payload;
    }

    public void setPayload(Object value) {
        payload = value;
    }

    /** RUNNING to {@code next}, once; false when it already ended. */
    boolean end(State next) {
        if (state != State.RUNNING) {
            return false;
        }
        state = next;
        return true;
    }
}
