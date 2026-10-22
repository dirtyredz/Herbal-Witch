package io.github.dirtyredz.herbalwitch.magic.witch.initiation;

import io.github.dirtyredz.herbalwitch.HerbalWitch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

/**
 * Explicit consent to become a witch, as pure rules over an injected clock and status lookup (so
 * they are unit-tested without a server):
 * <ul>
 *   <li>a witch invites one non-witch at a time; a recipient holds one invitation at a time;</li>
 *   <li>invitations expire, a witch waits a cooldown between invitations, and repeating a request
 *       never refreshes or extends one;</li>
 *   <li>an operator's designation is an invitation too: the recipient still has to accept;</li>
 *   <li>acceptance names the exact invitation id; {@link #take} removes it, so a replay finds nothing.</li>
 * </ul>
 * Whether the two players, the altar, the offering and the energy are still valid is checked by the
 * altar's initiation action immediately before it takes the invitation.
 */
public final class InitiationConsent {
    /** The outcome, also the message key {@code witch.herbalwitch.initiation.<id>}. */
    public enum Result {
        SENT,
        DESIGNATED,
        DECLINED,
        WITHDRAWN,
        SELF,
        NOT_WITCH,
        ALREADY_WITCH,
        DISABLED,
        INITIATOR_BUSY,
        RECIPIENT_BUSY,
        COOLDOWN,
        NO_INVITATION,
        EXPIRED,
        READY;

        public String messageKey() {
            return "witch." + HerbalWitch.MOD_ID + ".initiation." + name().toLowerCase(Locale.ROOT);
        }
    }

    private final LongSupplier clock;
    private final Predicate<UUID> isWitch;
    private final IntSupplier expiry;
    private final IntSupplier cooldown;
    private final Map<UUID, InitiationOffer> byInitiator = new HashMap<>();
    private final Map<UUID, InitiationOffer> byRecipient = new HashMap<>();
    private final Map<UUID, Long> lastInvite = new HashMap<>();
    private long nextId = 1;

    public InitiationConsent(LongSupplier clock, Predicate<UUID> isWitch, IntSupplier expiry, IntSupplier cooldown) {
        this.clock = clock;
        this.isWitch = isWitch;
        this.expiry = expiry;
        this.cooldown = cooldown;
    }

    /** A witch invites {@code recipient} at {@code site}. */
    public Result invite(UUID initiator, UUID recipient, RitualSite site) {
        long now = clock.getAsLong();
        if (initiator.equals(recipient)) {
            return Result.SELF;
        }
        if (!isWitch.test(initiator)) {
            return Result.NOT_WITCH;
        }
        if (byInitiator.containsKey(initiator)) {
            return Result.INITIATOR_BUSY; // never refreshes the pending invitation
        }
        Long last = lastInvite.get(initiator);
        if (last != null && now - last < cooldown.getAsInt()) {
            return Result.COOLDOWN;
        }
        if (isWitch.test(recipient)) {
            return Result.ALREADY_WITCH;
        }
        if (byRecipient.containsKey(recipient)) {
            return Result.RECIPIENT_BUSY;
        }
        InitiationOffer offer = new InitiationOffer(nextId++, Optional.of(initiator), recipient, Optional.of(site), now,
                now + expiry.getAsInt());
        byInitiator.put(initiator, offer);
        byRecipient.put(recipient, offer);
        lastInvite.put(initiator, now);
        return Result.SENT;
    }

    /** An operator designates {@code recipient}: an invitation accepted at any valid altar. */
    public Result designate(UUID recipient) {
        long now = clock.getAsLong();
        if (isWitch.test(recipient)) {
            return Result.ALREADY_WITCH;
        }
        if (byRecipient.containsKey(recipient)) {
            return Result.RECIPIENT_BUSY;
        }
        InitiationOffer offer = new InitiationOffer(nextId++, Optional.empty(), recipient, Optional.empty(), now, now + expiry.getAsInt());
        byRecipient.put(recipient, offer);
        return Result.DESIGNATED;
    }

    public Optional<InitiationOffer> incoming(UUID recipient) {
        return Optional.ofNullable(byRecipient.get(recipient));
    }

    public Optional<InitiationOffer> outgoing(UUID initiator) {
        return Optional.ofNullable(byInitiator.get(initiator));
    }

    /** Whether {@code recipient} holds exactly invitation {@code id}, unexpired; consumes nothing. */
    public Result check(UUID recipient, long id) {
        InitiationOffer offer = byRecipient.get(recipient);
        if (offer == null || offer.id() != id) {
            return Result.NO_INVITATION;
        }
        if (offer.expired(clock.getAsLong())) {
            remove(offer);
            return Result.EXPIRED;
        }
        if (isWitch.test(recipient)) {
            remove(offer);
            return Result.ALREADY_WITCH;
        }
        return Result.READY;
    }

    /** Accepts and removes exactly invitation {@code id} of {@code recipient}, if it is still valid. */
    public Optional<InitiationOffer> take(UUID recipient, long id) {
        if (check(recipient, id) != Result.READY) {
            return Optional.empty();
        }
        InitiationOffer offer = byRecipient.get(recipient);
        remove(offer);
        return Optional.of(offer);
    }

    public Result decline(UUID recipient, long id) {
        InitiationOffer offer = byRecipient.get(recipient);
        if (offer == null || offer.id() != id) {
            return Result.NO_INVITATION;
        }
        remove(offer);
        return Result.DECLINED;
    }

    /** The initiator withdraws their invitation. */
    public Result withdraw(UUID initiator) {
        InitiationOffer offer = byInitiator.get(initiator);
        if (offer == null) {
            return Result.NO_INVITATION;
        }
        remove(offer);
        return Result.WITHDRAWN;
    }

    /** Removes and returns expired invitations. */
    public List<InitiationOffer> sweep() {
        long now = clock.getAsLong();
        return removeIf(o -> o.expired(now));
    }

    /** Logout, death or a dimension change of either player. */
    public List<InitiationOffer> cancelInvolving(UUID player) {
        return removeIf(o -> o.involves(player));
    }

    /** A rules change or shutdown: every pending invitation ends. */
    public List<InitiationOffer> cancelAll() {
        return removeIf(o -> true);
    }

    public int pending() {
        return byRecipient.size();
    }

    private List<InitiationOffer> removeIf(Predicate<InitiationOffer> test) {
        List<InitiationOffer> removed = new ArrayList<>();
        for (InitiationOffer offer : new ArrayList<>(byRecipient.values())) {
            if (test.test(offer)) {
                remove(offer);
                removed.add(offer);
            }
        }
        removed.sort(Comparator.comparingLong(InitiationOffer::id));
        return removed;
    }

    private void remove(InitiationOffer offer) {
        byRecipient.remove(offer.recipient(), offer);
        offer.initiator().ifPresent(i -> byInitiator.remove(i, offer));
    }
}
