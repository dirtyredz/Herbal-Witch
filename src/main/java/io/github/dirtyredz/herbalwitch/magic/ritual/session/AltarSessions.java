package io.github.dirtyredz.herbalwitch.magic.ritual.session;

import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The running operations of one server, pure: one operation per altar and one per player.
 * Completion and cancellation each happen exactly once (a second completion of the same session,
 * a duplicate request or a late cancellation finds it already ended), and an ended session leaves
 * the registry at once. Invitations do not lock an altar; only a started operation does.
 */
public final class AltarSessions {
    /** Why an operation could not start. */
    public enum Refusal {
        ALTAR_BUSY,
        PLAYER_BUSY
    }

    private final Map<RitualSite, AltarSession> byAltar = new HashMap<>();
    private final Map<UUID, AltarSession> byPlayer = new HashMap<>();
    private long nextId = 1;

    public long nextId() {
        return nextId++;
    }

    /** Registers a new running session, or says why it cannot run. */
    public Optional<Refusal> start(AltarSession session) {
        if (byAltar.containsKey(session.site())) {
            return Optional.of(Refusal.ALTAR_BUSY);
        }
        for (UUID p : session.participants()) {
            if (byPlayer.containsKey(p)) {
                return Optional.of(Refusal.PLAYER_BUSY);
            }
        }
        byAltar.put(session.site(), session);
        session.participants().forEach(p -> byPlayer.put(p, session));
        return Optional.empty();
    }

    public Optional<AltarSession> at(RitualSite site) {
        return Optional.ofNullable(byAltar.get(site));
    }

    public Optional<AltarSession> of(UUID player) {
        return Optional.ofNullable(byPlayer.get(player));
    }

    /** Marks the session completed, once; true only for the first call while it runs. */
    public boolean complete(AltarSession session) {
        return finish(session, AltarSession.State.COMPLETED);
    }

    /** Cancels the session, once; true only for the first call while it runs. */
    public boolean cancel(AltarSession session) {
        return finish(session, AltarSession.State.CANCELLED);
    }

    private boolean finish(AltarSession session, AltarSession.State next) {
        if (!session.end(next)) {
            return false;
        }
        byAltar.remove(session.site(), session);
        session.participants().forEach(p -> byPlayer.remove(p, session));
        return true;
    }

    /** Running sessions, oldest first. */
    public List<AltarSession> running() {
        List<AltarSession> out = new ArrayList<>(byAltar.values());
        out.sort(Comparator.comparingLong(AltarSession::id));
        return out;
    }

    /** Cancels and returns every running session matching {@code test}. */
    public List<AltarSession> cancelIf(Predicate<AltarSession> test) {
        List<AltarSession> out = new ArrayList<>();
        for (AltarSession s : running()) {
            if (test.test(s) && cancel(s)) {
                out.add(s);
            }
        }
        return out;
    }

    public int size() {
        return byAltar.size();
    }
}
