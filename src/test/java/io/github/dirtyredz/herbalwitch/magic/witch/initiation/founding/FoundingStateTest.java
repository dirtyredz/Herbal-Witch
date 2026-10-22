package io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding;

import io.github.dirtyredz.herbalwitch.magic.witch.initiation.RitualSite;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding.FoundingRecord.State;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The world's founding state machine and its saved form: one claim, a durable commit, and crash recovery that never reopens an ambiguous claim. */
class FoundingStateTest {
    private static final UUID P = UUID.nameUUIDFromBytes("candidate".getBytes());
    private static final UUID Q = UUID.nameUUIDFromBytes("rival".getBytes());
    private static final UUID C1 = UUID.nameUUIDFromBytes("claim-1".getBytes());
    private static final UUID C2 = UUID.nameUUIDFromBytes("claim-2".getBytes());
    private static final RitualSite SITE = new RitualSite("minecraft:overworld", 123L);
    private static final RitualSite OTHER_SITE = new RitualSite("minecraft:the_nether", 456L);

    private static FoundingRecord claimed() {
        FoundingRecord r = new FoundingRecord();
        assertTrue(r.claim(P, SITE, C1).isPresent());
        return r;
    }

    private static FoundingRecord committing() {
        FoundingRecord r = claimed();
        assertTrue(r.beginCommit(C1));
        return r;
    }

    @Test
    void freshWorldIsOpen() {
        FoundingRecord r = new FoundingRecord();
        assertEquals(State.OPEN, r.state());
        assertTrue(r.claim().isEmpty());
        assertTrue(r.founder().isEmpty());
    }

    @Test
    void claimIsExclusive() {
        FoundingRecord r = claimed();
        assertEquals(State.CLAIMED, r.state());
        assertEquals(C1, r.claim().orElseThrow());
        assertEquals(P, r.candidate().orElseThrow());
        assertEquals(SITE, r.site().orElseThrow());
        assertTrue(r.claim(Q, OTHER_SITE, C2).isEmpty(), "another attempt, even at another altar, cannot claim");
        assertEquals(C1, r.claim().orElseThrow());
        assertEquals(P, r.candidate().orElseThrow());
    }

    @Test
    void releaseReopensOnlyForTheMatchingClaim() {
        FoundingRecord r = claimed();
        assertFalse(r.release(C2));
        assertEquals(State.CLAIMED, r.state());
        assertTrue(r.release(C1));
        assertEquals(State.OPEN, r.state());
        assertTrue(r.claim().isEmpty());
        assertTrue(r.candidate().isEmpty());
        assertTrue(r.site().isEmpty());
        assertFalse(r.release(C1), "a second release finds nothing");
        assertTrue(r.claim(Q, OTHER_SITE, C2).isPresent());
    }

    @Test
    void commitThenCompleteClosesFoundingForGood() {
        FoundingRecord r = claimed();
        assertFalse(r.complete(C1, P), "completion needs the durable claim first");
        assertFalse(r.beginCommit(C2));
        assertTrue(r.beginCommit(C1));
        assertEquals(State.COMMITTING, r.state());
        assertFalse(r.release(C1), "an ordinary cancel no longer works once committing");
        assertFalse(r.complete(C2, P));
        assertTrue(r.complete(C1, P));
        assertEquals(State.COMPLETE, r.state());
        assertEquals(P, r.founder().orElseThrow());
        assertTrue(r.claim(Q, OTHER_SITE, C2).isEmpty(), "closed for good");
        assertFalse(r.complete(C1, P), "completion happens once");
    }

    @Test
    void abortCommitReopens() {
        FoundingRecord r = committing();
        assertFalse(r.abortCommit(C2));
        assertTrue(r.abortCommit(C1));
        assertEquals(State.OPEN, r.state());
        assertTrue(r.claim().isEmpty());
        assertTrue(r.claim(Q, OTHER_SITE, C2).isPresent());
        assertFalse(claimed().abortCommit(C1), "only a committing claim can be aborted");
    }

    @Test
    void conversionElsewhereClosesOpenFounding() {
        FoundingRecord r = new FoundingRecord();
        assertTrue(r.conversionElsewhere());
        assertEquals(State.COMPLETE, r.state());
        assertTrue(r.claim(P, SITE, C1).isEmpty());
        assertFalse(r.conversionElsewhere(), "already closed");
    }

    @Test
    void conversionElsewhereVoidsAClaimedAttempt() {
        FoundingRecord r = claimed();
        assertTrue(r.conversionElsewhere());
        assertEquals(State.COMPLETE, r.state());
        assertTrue(r.claim().isEmpty());
        assertFalse(r.beginCommit(C1), "the voided attempt cannot go on to commit");
        assertFalse(r.complete(C1, P));
    }

    @Test
    void conversionElsewhereLeavesADurableCommitAlone() {
        FoundingRecord r = committing();
        assertFalse(r.conversionElsewhere());
        assertEquals(State.COMMITTING, r.state());
        assertTrue(r.complete(C1, P));
    }

    @Test
    void claimedLoadsAsOpenBecauseTheTransientClaimLapses() {
        FoundingCodec.Loaded loaded = FoundingCodec.read(FoundingCodec.write(claimed()));
        assertEquals(State.OPEN, loaded.record().state());
        assertTrue(loaded.record().claim().isEmpty());
        assertTrue(loaded.record().candidate().isEmpty());
        assertTrue(loaded.preserved().isEmpty());
        assertTrue(loaded.problem().isEmpty());
        assertTrue(loaded.record().claim(Q, OTHER_SITE, C2).isPresent());
    }

    @Test
    void committingLoadsAsNeedsReviewAndNeverOpen() {
        FoundingCodec.Loaded loaded = FoundingCodec.read(FoundingCodec.write(committing()));
        FoundingRecord r = loaded.record();
        assertEquals(State.NEEDS_REVIEW, r.state());
        assertEquals(C1, r.claim().orElseThrow());
        assertEquals(P, r.candidate().orElseThrow());
        assertEquals(SITE, r.site().orElseThrow());
        assertTrue(r.claim(Q, OTHER_SITE, C2).isEmpty());
        assertTrue(loaded.preserved().isEmpty());
    }

    @Test
    void completeRoundTripsWithItsFounder() {
        FoundingRecord done = committing();
        done.complete(C1, P);
        FoundingRecord r = FoundingCodec.read(FoundingCodec.write(done)).record();
        assertEquals(State.COMPLETE, r.state());
        assertEquals(P, r.founder().orElseThrow());
    }

    @Test
    void absentOrEmptyTagIsAnOpenWorld() {
        for (CompoundTag tag : new CompoundTag[] {null, new CompoundTag()}) {
            FoundingCodec.Loaded loaded = FoundingCodec.read(tag);
            assertEquals(State.OPEN, loaded.record().state());
            assertTrue(loaded.preserved().isEmpty());
        }
    }

    @Test
    void resolveCloseNeedsTheMatchingClaim() {
        FoundingRecord r = FoundingCodec.read(FoundingCodec.write(committing())).record();
        assertFalse(r.resolveClose(C2));
        assertEquals(State.NEEDS_REVIEW, r.state());
        assertTrue(r.resolveClose(C1));
        assertEquals(State.COMPLETE, r.state());
        assertFalse(new FoundingRecord().resolveClose(C1), "only an interrupted claim can be closed");
        assertFalse(claimed().resolveClose(C1));
    }

    @Test
    void unsupportedSchemaIsPreservedAndTreatedAsNeedsReview() {
        CompoundTag tag = FoundingCodec.write(committing());
        tag.putInt("schema", 2);
        CompoundTag before = tag.copy();
        FoundingCodec.Loaded loaded = FoundingCodec.read(tag);
        assertEquals(State.NEEDS_REVIEW, loaded.record().state());
        assertTrue(loaded.problem().isPresent());
        assertEquals(before, loaded.preserved().orElseThrow());
        assertEquals(before, FoundingCodec.write(loaded), "an operator can still read what was saved");
        assertTrue(loaded.record().claim(Q, OTHER_SITE, C2).isEmpty(), "never silently reopened");
        assertFalse(loaded.record().resolveClose(C1), "no claim to match, so nothing closes it by accident");
    }

    @Test
    void missingSchemaOrUnknownStateOrMissingClaimIsPreserved() {
        CompoundTag noSchema = FoundingCodec.write(committing());
        noSchema.remove("schema");
        assertTrue(FoundingCodec.read(noSchema).preserved().isPresent());

        CompoundTag badState = FoundingCodec.write(new FoundingRecord());
        badState.putString("state", "banana");
        FoundingCodec.Loaded unknown = FoundingCodec.read(badState);
        assertEquals(State.NEEDS_REVIEW, unknown.record().state());
        assertEquals(badState, unknown.preserved().orElseThrow());

        CompoundTag noClaim = FoundingCodec.write(committing());
        noClaim.remove("claim");
        FoundingCodec.Loaded broken = FoundingCodec.read(noClaim);
        assertEquals(State.NEEDS_REVIEW, broken.record().state());
        assertEquals(noClaim, broken.preserved().orElseThrow());
    }
}
