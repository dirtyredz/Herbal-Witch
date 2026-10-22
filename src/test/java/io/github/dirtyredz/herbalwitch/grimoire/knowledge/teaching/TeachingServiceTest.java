package io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching.TeachingService.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Consenting teaching against an injected clock and world: consent, expiry, cooldown, replay, spoofing, cleanup. */
class TeachingServiceTest {
    private static final UUID TEACHER = UUID.nameUUIDFromBytes("teacher".getBytes());
    private static final UUID PUPIL = UUID.nameUUIDFromBytes("pupil".getBytes());
    private static final UUID OTHER = UUID.nameUUIDFromBytes("other".getBytes());
    private static final EntryKey TEA = EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:processing/steeping/recovery_tea");
    private static final EntryKey SALVE = EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:processing/infusion/healing_salve");

    /** A fake world: everyone online and close unless moved away; readers open with fixed sessions. */
    private static final class World implements TeachingService.World {
        final Set<UUID> far = new HashSet<>();
        final Map<UUID, Long> sessions = new HashMap<>(Map.of(TEACHER, 11L, PUPIL, 22L, OTHER, 33L));
        final Map<UUID, Set<EntryKey>> known = new HashMap<>();
        final Map<EntryKey, String> fingerprints = new HashMap<>(Map.of(TEA, "tea-v1", SALVE, "salve-v1"));

        @Override
        public boolean inRange(UUID teacher, UUID recipient) {
            return !far.contains(teacher) && !far.contains(recipient);
        }

        @Override
        public long readerSession(UUID player) {
            return sessions.getOrDefault(player, 0L);
        }

        @Override
        public boolean knows(UUID player, EntryKey recipe) {
            return known.getOrDefault(player, Set.of()).contains(recipe);
        }

        @Override
        public Optional<String> fingerprint(EntryKey recipe) {
            return Optional.ofNullable(fingerprints.get(recipe));
        }

        @Override
        public boolean grant(UUID recipient, EntryKey recipe) {
            return known.computeIfAbsent(recipient, k -> new HashSet<>()).add(recipe);
        }
    }

    private long now;
    private World world;
    private TeachingService service;

    @BeforeEach
    void setUp() {
        now = 1000;
        world = new World();
        world.known.put(TEACHER, new HashSet<>(Set.of(TEA, SALVE)));
        service = new TeachingService(() -> now, world);
    }

    @Test
    void nothingIsLearnedUntilTheRecipientAccepts() {
        assertEquals(Result.SENT, service.invite(TEACHER, PUPIL, TEA));
        assertFalse(world.knows(PUPIL, TEA));
        assertEquals(Result.ACCEPTED, service.accept(PUPIL, TEACHER, TEA));
        assertTrue(world.knows(PUPIL, TEA));
        assertFalse(world.knows(PUPIL, SALVE), "only the offered recipe");
        assertEquals(0, service.pending());
    }

    @Test
    void decliningEndsTheOfferWithoutLearning() {
        service.invite(TEACHER, PUPIL, TEA);
        assertEquals(Result.DECLINED, service.decline(PUPIL, TEACHER));
        assertFalse(world.knows(PUPIL, TEA));
        assertEquals(Result.NO_INVITATION, service.accept(PUPIL, TEACHER, TEA));
    }

    @Test
    void anOfferExpiresAtExactlyItsDeadline() {
        service.invite(TEACHER, PUPIL, TEA);
        now += TeachingService.EXPIRY - 1;
        assertTrue(service.sweep().isEmpty(), "one tick before the deadline it stands");
        now += 1;
        assertEquals(1, service.sweep().size());
        assertEquals(Result.NO_INVITATION, service.accept(PUPIL, TEACHER, TEA));
        service.invite(TEACHER, OTHER, SALVE);
        now += TeachingService.EXPIRY;
        assertEquals(Result.EXPIRED, service.accept(OTHER, TEACHER, SALVE), "accepting at the deadline is too late");
    }

    @Test
    void repeatsNeitherRefreshNorStackAndACooldownFollows() {
        assertEquals(Result.SENT, service.invite(TEACHER, PUPIL, TEA));
        long expires = service.outgoing(TEACHER).orElseThrow().expiresTick();
        now += 50;
        assertEquals(Result.TEACHER_BUSY, service.invite(TEACHER, PUPIL, TEA));
        assertEquals(expires, service.outgoing(TEACHER).orElseThrow().expiresTick(), "a duplicate never refreshes expiry");
        assertEquals(Result.CANCELLED, service.cancel(TEACHER));
        assertEquals(Result.COOLDOWN, service.invite(TEACHER, OTHER, SALVE));
        now += TeachingService.COOLDOWN;
        assertEquals(Result.SENT, service.invite(TEACHER, OTHER, SALVE));
    }

    @Test
    void oneIncomingOfferPerRecipient() {
        world.known.put(OTHER, new HashSet<>(Set.of(TEA)));
        assertEquals(Result.SENT, service.invite(TEACHER, PUPIL, TEA));
        assertEquals(Result.RECIPIENT_BUSY, service.invite(OTHER, PUPIL, TEA));
    }

    @Test
    void replayedAndSpoofedAcceptancesFindNothing() {
        service.invite(TEACHER, PUPIL, TEA);
        assertEquals(Result.NO_INVITATION, service.accept(OTHER, TEACHER, TEA), "someone else cannot accept it");
        assertEquals(Result.NO_INVITATION, service.accept(PUPIL, OTHER, TEA), "naming another teacher");
        assertEquals(Result.NO_INVITATION, service.accept(PUPIL, TEACHER, SALVE), "naming another recipe");
        assertEquals(Result.ACCEPTED, service.accept(PUPIL, TEACHER, TEA));
        assertEquals(Result.NO_INVITATION, service.accept(PUPIL, TEACHER, TEA), "a replay finds the offer consumed");
    }

    @Test
    void invalidOffersAreRefusedUpFront() {
        assertEquals(Result.SELF, service.invite(TEACHER, TEACHER, TEA));
        EntryKey unknown = EntryKey.of(EntryKey.Kind.RECIPE, "othermod:processing/drying/thing");
        assertEquals(Result.NOT_LIVE_RECIPE, service.invite(TEACHER, PUPIL, unknown));
        world.known.get(TEACHER).remove(SALVE);
        assertEquals(Result.NOT_KNOWN, service.invite(TEACHER, PUPIL, SALVE));
        world.known.put(PUPIL, new HashSet<>(Set.of(TEA)));
        assertEquals(Result.ALREADY_KNOWN, service.invite(TEACHER, PUPIL, TEA));
        world.far.add(OTHER);
        assertEquals(Result.OUT_OF_RANGE, service.invite(TEACHER, OTHER, TEA));
        world.sessions.remove(TEACHER);
        assertEquals(Result.NO_READER, service.invite(TEACHER, OTHER, TEA));
    }

    @Test
    void acceptanceRevalidatesEverything() {
        service.invite(TEACHER, PUPIL, TEA);
        world.sessions.remove(PUPIL);
        assertEquals(Result.NO_READER, service.accept(PUPIL, TEACHER, TEA), "the recipient needs an open reader");
        world.sessions.put(PUPIL, 22L);
        world.far.add(PUPIL);
        assertEquals(Result.OUT_OF_RANGE, service.accept(PUPIL, TEACHER, TEA));
        world.far.clear();
        world.fingerprints.put(TEA, "tea-v2");
        assertEquals(Result.RECIPE_CHANGED, service.accept(PUPIL, TEACHER, TEA));
        assertFalse(world.knows(PUPIL, TEA));
    }

    @Test
    void closingTheTeachersReaderVoidsTheOffer() {
        service.invite(TEACHER, PUPIL, TEA);
        world.sessions.put(TEACHER, 99L); // reopened: a new session
        assertEquals(Result.TEACHER_LEFT, service.accept(PUPIL, TEACHER, TEA));
        now += TeachingService.COOLDOWN;
        assertEquals(Result.SENT, service.invite(TEACHER, OTHER, SALVE));
        world.sessions.remove(TEACHER);
        assertEquals(1, service.sweep().size());
    }

    @Test
    void cleanupCancelsOffersInvolvingAPlayerAndReloadsVoidChangedRecipes() {
        world.known.put(OTHER, new HashSet<>(Set.of(SALVE)));
        service.invite(TEACHER, PUPIL, TEA);
        service.invite(OTHER, UUID.nameUUIDFromBytes("fourth".getBytes()), SALVE);
        List<TeachingInvitation> gone = service.cancelInvolving(PUPIL);
        assertEquals(1, gone.size());
        assertEquals(TEACHER, gone.get(0).teacher());
        assertEquals(1, service.pending());
        Map<EntryKey, String> reloaded = Map.of(TEA, "tea-v1");
        assertEquals(1, service.invalidate(k -> Optional.ofNullable(reloaded.get(k))).size(), "the salve was removed");
        assertEquals(0, service.pending());
    }
}
