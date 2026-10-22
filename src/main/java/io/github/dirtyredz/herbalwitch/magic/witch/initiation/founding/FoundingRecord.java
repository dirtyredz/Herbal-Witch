package io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding;

import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;

import java.util.Optional;
import java.util.UUID;

/**
 * The world's founding state machine, pure. Exactly one founding attempt can hold the world claim,
 * whatever altar or dimension it uses:
 * <ul>
 *   <li>{@link State#OPEN}: survival founding is available;</li>
 *   <li>{@link State#CLAIMED}: one attempt is in progress; cancelling it before commit releases it, and
 *       a restart reopens it (nothing irreversible happened);</li>
 *   <li>{@link State#COMMITTING}: the durable claim, persisted immediately before the irreversible
 *       completion; if the world loads in this state the completion is ambiguous and becomes
 *       {@link State#NEEDS_REVIEW}, never {@link State#OPEN};</li>
 *   <li>{@link State#COMPLETE}: founding is closed for good (the first conversion by any route);</li>
 *   <li>{@link State#NEEDS_REVIEW}: an operator inspects and closes it without replaying anything.</li>
 * </ul>
 * This deliberately favours founder exclusivity after an ambiguous crash; it does not make player,
 * inventory and world files save atomically.
 */
public final class FoundingRecord {
    public enum State {
        OPEN,
        CLAIMED,
        COMMITTING,
        COMPLETE,
        NEEDS_REVIEW
    }

    private State state = State.OPEN;
    private UUID claim;
    private UUID candidate;
    private RitualSite site;
    private UUID founder;

    public State state() {
        return state;
    }

    public Optional<UUID> claim() {
        return Optional.ofNullable(claim);
    }

    public Optional<UUID> candidate() {
        return Optional.ofNullable(candidate);
    }

    public Optional<RitualSite> site() {
        return Optional.ofNullable(site);
    }

    public Optional<UUID> founder() {
        return Optional.ofNullable(founder);
    }

    /** Takes the world claim for an attempt; empty when another attempt holds it or founding is closed. */
    public Optional<UUID> claim(UUID who, RitualSite where, UUID newClaim) {
        if (state != State.OPEN) {
            return Optional.empty();
        }
        state = State.CLAIMED;
        claim = newClaim;
        candidate = who;
        site = where;
        return Optional.of(newClaim);
    }

    /** Ordinary pre-commit cancellation: the claim is released and founding reopens. */
    public boolean release(UUID claimId) {
        if (state != State.CLAIMED || !claimId.equals(claim)) {
            return false;
        }
        clearAttempt();
        state = State.OPEN;
        return true;
    }

    /** The durable claim, immediately before completion (the caller then persists it). */
    public boolean beginCommit(UUID claimId) {
        if (state != State.CLAIMED || !claimId.equals(claim)) {
            return false;
        }
        state = State.COMMITTING;
        return true;
    }

    /** Persisting the durable claim failed: abort before anything is consumed, founding reopens. */
    public boolean abortCommit(UUID claimId) {
        if (state != State.COMMITTING || !claimId.equals(claim)) {
            return false;
        }
        clearAttempt();
        state = State.OPEN;
        return true;
    }

    /** The founding conversion completed. */
    public boolean complete(UUID claimId, UUID founderId) {
        if (state != State.COMMITTING || !claimId.equals(claim)) {
            return false;
        }
        state = State.COMPLETE;
        founder = founderId;
        return true;
    }

    /**
     * A conversion by another route (initiation, designation) closes open eligibility; an attempt in
     * progress loses its claim and fails at completion. A durable commit in flight is left alone.
     */
    public boolean conversionElsewhere() {
        if (state == State.OPEN || state == State.CLAIMED) {
            clearAttempt();
            state = State.COMPLETE;
            return true;
        }
        return false;
    }

    /** After loading: an unfinished durable claim needs review; a transient claim simply lapses. */
    public void loaded() {
        if (state == State.COMMITTING) {
            state = State.NEEDS_REVIEW;
        } else if (state == State.CLAIMED) {
            clearAttempt();
            state = State.OPEN;
        }
    }

    /** An operator closes an interrupted claim without replaying rewards, charges or conversion. */
    public boolean resolveClose(UUID claimId) {
        if (state != State.NEEDS_REVIEW || !claimId.equals(claim)) {
            return false;
        }
        state = State.COMPLETE;
        return true;
    }

    private void clearAttempt() {
        claim = null;
        candidate = null;
        site = null;
    }

    /** Restores every field (codec only). */
    void set(State s, UUID claimId, UUID who, RitualSite where, UUID founderId) {
        state = s;
        claim = claimId;
        candidate = who;
        site = where;
        founder = founderId;
    }
}
