package io.github.dirtyredz.herbalwitch.magic.witch.initiation;

import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationConsent.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Consent to become a witch against an injected clock and status lookup: invite rules, exact-id acceptance, expiry, cooldown and cleanup. */
class InitiationConsentTest {
    private static final UUID A = UUID.nameUUIDFromBytes("a".getBytes());
    private static final UUID B = UUID.nameUUIDFromBytes("b".getBytes());
    private static final UUID C = UUID.nameUUIDFromBytes("c".getBytes());
    private static final UUID D = UUID.nameUUIDFromBytes("d".getBytes());
    private static final UUID E = UUID.nameUUIDFromBytes("e".getBytes());
    private static final RitualSite SITE = new RitualSite("minecraft:overworld", 0);

    private final long[] now = {0};
    private final Set<UUID> witches = new HashSet<>();
    private InitiationConsent consent;

    @BeforeEach
    void setUp() {
        now[0] = 0;
        witches.clear();
        witches.add(A);
        witches.add(D);
        consent = new InitiationConsent(() -> now[0], witches::contains, () -> 600, () -> 100);
    }

    private long inviteB() {
        assertEquals(Result.SENT, consent.invite(A, B, SITE));
        return consent.incoming(B).orElseThrow().id();
    }

    @Test
    void sendingCreatesAnInvitationWithAnExpiry() {
        now[0] = 50;
        assertEquals(Result.SENT, consent.invite(A, B, SITE));
        InitiationOffer offer = consent.incoming(B).orElseThrow();
        assertEquals(offer, consent.outgoing(A).orElseThrow());
        assertEquals(A, offer.initiator().orElseThrow());
        assertEquals(SITE, offer.site().orElseThrow());
        assertEquals(650, offer.expiresTick());
        assertFalse(offer.administrative());
        assertEquals(1, consent.pending());
    }

    @Test
    void invitingYourselfIsRefused() {
        assertEquals(Result.SELF, consent.invite(A, A, SITE));
        assertEquals(0, consent.pending());
    }

    @Test
    void onlyAWitchMayInvite() {
        assertEquals(Result.NOT_WITCH, consent.invite(B, C, SITE));
    }

    @Test
    void aWitchCannotBeInvited() {
        assertEquals(Result.ALREADY_WITCH, consent.invite(A, D, SITE));
    }

    @Test
    void repeatingAnInvitationNeverRefreshesItsExpiry() {
        inviteB();
        long expires = consent.incoming(B).orElseThrow().expiresTick();
        now[0] = 300;
        assertEquals(Result.INITIATOR_BUSY, consent.invite(A, B, SITE));
        assertEquals(Result.INITIATOR_BUSY, consent.invite(A, C, SITE));
        assertEquals(expires, consent.incoming(B).orElseThrow().expiresTick());
        assertEquals(expires, consent.outgoing(A).orElseThrow().expiresTick());
        assertEquals(1, consent.pending());
    }

    @Test
    void aRecipientHoldsOneInvitationAtATime() {
        inviteB();
        assertEquals(Result.RECIPIENT_BUSY, consent.invite(D, B, SITE));
        assertEquals(1, consent.pending());
    }

    @Test
    void aWitchWaitsOutTheCooldownAfterWithdrawing() {
        inviteB();
        assertEquals(Result.WITHDRAWN, consent.withdraw(A));
        now[0] = 99;
        assertEquals(Result.COOLDOWN, consent.invite(A, C, SITE));
        now[0] = 100;
        assertEquals(Result.SENT, consent.invite(A, C, SITE));
    }

    @Test
    void designationIsAnAdministrativeInvitationThatStillNeedsAcceptance() {
        assertEquals(Result.DESIGNATED, consent.designate(B));
        InitiationOffer offer = consent.incoming(B).orElseThrow();
        assertTrue(offer.administrative());
        assertTrue(offer.initiator().isEmpty());
        assertTrue(offer.site().isEmpty());
        assertEquals(600, offer.expiresTick());
        assertEquals(Result.READY, consent.check(B, offer.id()));
        assertEquals(1, consent.pending());
        assertEquals(offer, consent.take(B, offer.id()).orElseThrow());
        assertEquals(0, consent.pending());
    }

    @Test
    void designationRefusesWitchesAndBusyRecipients() {
        assertEquals(Result.ALREADY_WITCH, consent.designate(A));
        assertEquals(Result.DESIGNATED, consent.designate(B));
        assertEquals(Result.RECIPIENT_BUSY, consent.designate(B));
        assertEquals(Result.RECIPIENT_BUSY, consent.invite(A, B, SITE));
    }

    @Test
    void checkAndTakeNameTheExactInvitation() {
        long id = inviteB();
        assertEquals(Result.NO_INVITATION, consent.check(B, id + 1000));
        assertEquals(Result.NO_INVITATION, consent.check(C, id), "another recipient holds nothing");
        assertTrue(consent.take(B, id + 1000).isEmpty(), "a spoofed id takes nothing");
        assertTrue(consent.take(C, id).isEmpty());
        assertEquals(1, consent.pending(), "failed attempts consume nothing");
        assertEquals(Result.READY, consent.check(B, id));
        assertEquals(1, consent.pending(), "check consumes nothing");
    }

    @Test
    void takeRemovesTheInvitationSoAReplayFindsNothing() {
        long id = inviteB();
        InitiationOffer offer = consent.take(B, id).orElseThrow();
        assertEquals(id, offer.id());
        assertTrue(consent.take(B, id).isEmpty());
        assertEquals(Result.NO_INVITATION, consent.check(B, id));
        assertEquals(0, consent.pending());
        assertTrue(consent.outgoing(A).isEmpty());
    }

    @Test
    void anInvitationExpiresAtItsExpiryTick() {
        long id = inviteB();
        now[0] = 599;
        assertEquals(Result.READY, consent.check(B, id));
        now[0] = 600;
        assertEquals(Result.EXPIRED, consent.check(B, id));
        assertEquals(0, consent.pending());
        assertTrue(consent.outgoing(A).isEmpty());
        assertEquals(Result.NO_INVITATION, consent.check(B, id), "an expired invitation is removed");
    }

    @Test
    void takeRefusesAnExpiredInvitation() {
        long id = inviteB();
        now[0] = 700;
        assertTrue(consent.take(B, id).isEmpty());
        assertEquals(0, consent.pending());
    }

    @Test
    void declineAndWithdrawEndTheInvitation() {
        long id = inviteB();
        assertEquals(Result.NO_INVITATION, consent.decline(B, id + 1));
        assertEquals(Result.NO_INVITATION, consent.decline(C, id));
        assertEquals(Result.DECLINED, consent.decline(B, id));
        assertEquals(0, consent.pending());
        assertTrue(consent.outgoing(A).isEmpty());

        assertEquals(Result.NO_INVITATION, consent.withdraw(A));
        now[0] = 100;
        long second = inviteB();
        assertEquals(Result.WITHDRAWN, consent.withdraw(A));
        assertEquals(Result.NO_INVITATION, consent.check(B, second));
        assertEquals(Result.NO_INVITATION, consent.withdraw(A));
    }

    @Test
    void sweepRemovesOnlyExpiredInvitations() {
        inviteB();
        now[0] = 300;
        assertEquals(Result.SENT, consent.invite(D, E, SITE));
        now[0] = 599;
        assertTrue(consent.sweep().isEmpty());
        now[0] = 600;
        List<InitiationOffer> swept = consent.sweep();
        assertEquals(1, swept.size());
        assertEquals(B, swept.get(0).recipient());
        assertEquals(1, consent.pending());
        assertTrue(consent.incoming(E).isPresent());
    }

    @Test
    void cancelInvolvingEndsInvitationsOfEitherParty() {
        inviteB();
        assertEquals(Result.SENT, consent.invite(D, E, SITE));

        List<InitiationOffer> byInitiator = consent.cancelInvolving(A);
        assertEquals(1, byInitiator.size());
        assertEquals(B, byInitiator.get(0).recipient());
        assertTrue(consent.incoming(B).isEmpty());
        assertTrue(consent.outgoing(A).isEmpty());

        List<InitiationOffer> byRecipient = consent.cancelInvolving(E);
        assertEquals(1, byRecipient.size());
        assertEquals(D, byRecipient.get(0).initiator().orElseThrow());
        assertEquals(0, consent.pending());
        assertTrue(consent.cancelInvolving(C).isEmpty());
    }

    @Test
    void cancelAllEndsEverythingOldestFirst() {
        inviteB();
        assertEquals(Result.SENT, consent.invite(D, E, SITE));
        assertEquals(Result.DESIGNATED, consent.designate(C));
        List<InitiationOffer> cancelled = consent.cancelAll();
        assertEquals(3, cancelled.size());
        assertTrue(cancelled.get(0).id() < cancelled.get(1).id() && cancelled.get(1).id() < cancelled.get(2).id());
        assertEquals(0, consent.pending());
        assertTrue(consent.outgoing(A).isEmpty());
        assertTrue(consent.outgoing(D).isEmpty());
    }

    @Test
    void aRecipientWhoBecomesAWitchMeanwhileCannotAccept() {
        long id = inviteB();
        witches.add(B);
        assertEquals(Result.ALREADY_WITCH, consent.check(B, id));
        assertEquals(0, consent.pending(), "the stale invitation is removed");
        assertTrue(consent.take(B, id).isEmpty());
    }

    @Test
    void aWitchRecipientMadeBetweenCheckAndTakeIsRefused() {
        long id = inviteB();
        witches.add(B);
        assertTrue(consent.take(B, id).isEmpty());
        assertEquals(0, consent.pending());
    }

    @Test
    void resultMessageKeysAreLowerCaseNames() {
        assertEquals("witch.herbalwitch.initiation.already_witch", Result.ALREADY_WITCH.messageKey());
    }
}
