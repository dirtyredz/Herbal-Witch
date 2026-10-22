package io.github.dirtyredz.herbalwitch.grimoire.knowledge.teaching;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.LongSupplier;

/**
 * Consenting player-to-player teaching of one recipe. The rules live here, against an injected
 * clock and {@link World} (so they are unit-tested without a server):
 *
 * <ul>
 *   <li>the teacher must know the live recipe, hold a valid reader, and be within {@link #RANGE}
 *       blocks of the recipient, in the same dimension, both alive and not spectating;</li>
 *   <li>one outgoing invitation per teacher, one incoming per recipient, {@link #EXPIRY} ticks to
 *       answer, {@link #COOLDOWN} ticks between a teacher's invitations; repeating a request never
 *       refreshes an invitation;</li>
 *   <li>acceptance names the teacher and recipe it accepts, needs the recipient's own valid reader,
 *       and revalidates range, the teacher's session and knowledge, and the recipe's fingerprint;</li>
 *   <li>acceptance grants that one recipe only (no ingredients, no herb discoveries) and consumes
 *       the invitation, so a replay finds nothing.</li>
 * </ul>
 */
public final class TeachingService {
    public static final double RANGE = 8.0;
    public static final int EXPIRY = 600;
    public static final int COOLDOWN = 100;

    /** Live checks against the world. */
    public interface World {
        /** Both online, same dimension, within range, alive and not spectating. */
        boolean inRange(UUID teacher, UUID recipient);

        /** The player's open, valid reader session, or 0. */
        long readerSession(UUID player);

        boolean knows(UUID player, EntryKey recipe);

        /** The live recipe's content fingerprint, empty when it is not a live recipe. */
        Optional<String> fingerprint(EntryKey recipe);

        /** Records the discovery for the recipient; false if it could not be recorded. */
        boolean grant(UUID recipient, EntryKey recipe);
    }

    /** The outcome, also the message key {@code grimoire.herbalwitch.teaching.<id>}. */
    public enum Result {
        SENT,
        ACCEPTED,
        DECLINED,
        CANCELLED,
        SELF,
        NO_READER,
        OUT_OF_RANGE,
        NOT_LIVE_RECIPE,
        NOT_KNOWN,
        ALREADY_KNOWN,
        TEACHER_BUSY,
        RECIPIENT_BUSY,
        COOLDOWN,
        NO_INVITATION,
        EXPIRED,
        TEACHER_LEFT,
        RECIPE_CHANGED,
        NOT_RECORDED;

        public String messageKey() {
            return "grimoire." + HerbalWitch.MOD_ID + ".teaching." + name().toLowerCase(Locale.ROOT);
        }
    }

    private final LongSupplier clock;
    private final World world;
    private final Map<UUID, TeachingInvitation> byTeacher = new HashMap<>();
    private final Map<UUID, TeachingInvitation> byRecipient = new HashMap<>();
    private final Map<UUID, Long> lastInvite = new HashMap<>();

    public TeachingService(LongSupplier clock, World world) {
        this.clock = clock;
        this.world = world;
    }

    public Result invite(UUID teacher, UUID recipient, EntryKey recipe) {
        long now = clock.getAsLong();
        if (teacher.equals(recipient)) {
            return Result.SELF;
        }
        long session = world.readerSession(teacher);
        if (session == 0) {
            return Result.NO_READER;
        }
        if (byTeacher.containsKey(teacher)) {
            return Result.TEACHER_BUSY; // never refreshes the pending invitation
        }
        Long last = lastInvite.get(teacher);
        if (last != null && now - last < COOLDOWN) {
            return Result.COOLDOWN;
        }
        if (!world.inRange(teacher, recipient)) {
            return Result.OUT_OF_RANGE;
        }
        Optional<String> fingerprint = world.fingerprint(recipe);
        if (fingerprint.isEmpty() || recipe.kind() != EntryKey.Kind.RECIPE) {
            return Result.NOT_LIVE_RECIPE;
        }
        if (!world.knows(teacher, recipe)) {
            return Result.NOT_KNOWN;
        }
        if (world.knows(recipient, recipe)) {
            return Result.ALREADY_KNOWN;
        }
        if (byRecipient.containsKey(recipient)) {
            return Result.RECIPIENT_BUSY;
        }
        TeachingInvitation invitation = new TeachingInvitation(teacher, recipient, recipe, fingerprint.get(), now, now + EXPIRY, session);
        byTeacher.put(teacher, invitation);
        byRecipient.put(recipient, invitation);
        lastInvite.put(teacher, now);
        return Result.SENT;
    }

    /** The recipient accepts the invitation from {@code teacher} for {@code recipe}, exactly. */
    public Result accept(UUID recipient, UUID teacher, EntryKey recipe) {
        long now = clock.getAsLong();
        TeachingInvitation invitation = byRecipient.get(recipient);
        if (invitation == null || !invitation.teacher().equals(teacher) || !invitation.recipe().equals(recipe)) {
            return Result.NO_INVITATION;
        }
        if (invitation.expired(now)) {
            remove(invitation);
            return Result.EXPIRED;
        }
        if (world.readerSession(teacher) != invitation.teacherSession()) {
            remove(invitation);
            return Result.TEACHER_LEFT;
        }
        if (world.readerSession(recipient) == 0) {
            return Result.NO_READER;
        }
        if (!world.inRange(teacher, recipient)) {
            return Result.OUT_OF_RANGE;
        }
        if (!world.fingerprint(recipe).map(invitation.fingerprint()::equals).orElse(false)) {
            remove(invitation);
            return Result.RECIPE_CHANGED;
        }
        if (!world.knows(teacher, recipe)) {
            remove(invitation);
            return Result.NOT_KNOWN;
        }
        remove(invitation);
        if (world.knows(recipient, recipe)) {
            return Result.ALREADY_KNOWN;
        }
        return world.grant(recipient, recipe) ? Result.ACCEPTED : Result.NOT_RECORDED;
    }

    public Result decline(UUID recipient, UUID teacher) {
        TeachingInvitation invitation = byRecipient.get(recipient);
        if (invitation == null || !invitation.teacher().equals(teacher)) {
            return Result.NO_INVITATION;
        }
        remove(invitation);
        return Result.DECLINED;
    }

    /** The teacher withdraws their invitation. */
    public Result cancel(UUID teacher) {
        TeachingInvitation invitation = byTeacher.get(teacher);
        if (invitation == null) {
            return Result.NO_INVITATION;
        }
        remove(invitation);
        return Result.CANCELLED;
    }

    /** Removes and returns invitations that expired or whose teacher closed the reader they offered from. */
    public List<TeachingInvitation> sweep() {
        long now = clock.getAsLong();
        return removeIf(i -> i.expired(now) || world.readerSession(i.teacher()) != i.teacherSession());
    }

    /** Logout, death or a dimension change of either player. */
    public List<TeachingInvitation> cancelInvolving(UUID player) {
        return removeIf(i -> i.involves(player));
    }

    /** A content reload: offers whose recipe vanished or changed are void. */
    public List<TeachingInvitation> invalidate(Function<EntryKey, Optional<String>> fingerprints) {
        return removeIf(i -> !fingerprints.apply(i.recipe()).map(i.fingerprint()::equals).orElse(false));
    }

    public Optional<TeachingInvitation> incoming(UUID recipient) {
        return Optional.ofNullable(byRecipient.get(recipient));
    }

    public Optional<TeachingInvitation> outgoing(UUID teacher) {
        return Optional.ofNullable(byTeacher.get(teacher));
    }

    public int pending() {
        return byTeacher.size();
    }

    private List<TeachingInvitation> removeIf(java.util.function.Predicate<TeachingInvitation> test) {
        List<TeachingInvitation> removed = new ArrayList<>();
        for (TeachingInvitation invitation : new ArrayList<>(byTeacher.values())) {
            if (test.test(invitation)) {
                remove(invitation);
                removed.add(invitation);
            }
        }
        removed.sort(java.util.Comparator.comparing(i -> i.teacher().toString()));
        return removed;
    }

    private void remove(TeachingInvitation invitation) {
        byTeacher.remove(invitation.teacher(), invitation);
        byRecipient.remove(invitation.recipient(), invitation);
    }
}
