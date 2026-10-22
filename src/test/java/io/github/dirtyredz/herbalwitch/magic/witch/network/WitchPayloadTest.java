package io.github.dirtyredz.herbalwitch.magic.witch.network;

import io.github.dirtyredz.herbalwitch.magic.ritual.network.AltarRequest;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import io.github.dirtyredz.herbalwitch.platform.network.RateLimit;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The witch channel's messages: bounded, validated decoding, round trips, snapshot ordering, rules fingerprints and the request rate limit. */
class WitchPayloadTest {
    private static final UUID PLAYER = UUID.nameUUIDFromBytes("player".getBytes());
    private static final UUID OTHER = UUID.nameUUIDFromBytes("other".getBytes());

    private static FriendlyByteBuf buf() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    private static WitchSnapshot snapshot(long epoch, int revision) {
        return new WitchSnapshot(epoch, revision, true, 5_000, 10_000, 1_200, true, WitchSnapshot.Recovering.MEDITATION, false);
    }

    private static WitchRules rulesWith(EnergyBalance energy) {
        return new WitchRules(true, true, 200, 200, 100, 600, 100, energy);
    }

    private static EnergyBalance balance(List<Integer> thresholds, int attunementCost) {
        return new EnergyBalance(100, thresholds, 10, 50, attunementCost, 100, 1, 40, 20, 2, 30, 25, 1200, 30, 24_000, 10, 15, 25);
    }

    // ----- WitchSnapshot -----

    @Test
    void snapshotRoundTrips() {
        WitchSnapshot original = new WitchSnapshot(1L << 40, 17, true, 5_000, 10_000, 1_200, true, WitchSnapshot.Recovering.REST, true);
        FriendlyByteBuf b = buf();
        original.encode(b);
        assertEquals(original, WitchSnapshot.decode(b));
        assertEquals(0, b.readableBytes(), "the whole message is consumed");
    }

    @Test
    void snapshotOrderingHigherEpochWins() {
        assertTrue(snapshot(5, 0).newerThan(4, 999));
        assertFalse(snapshot(4, 999).newerThan(5, 0));
    }

    @Test
    void snapshotWithinAnEpochNeedsAHigherRevision() {
        assertTrue(snapshot(5, 3).newerThan(5, 2));
        assertFalse(snapshot(5, 3).newerThan(5, 3), "an identical revision is not newer");
        assertFalse(snapshot(5, 2).newerThan(5, 3), "stale revisions are ignored");
    }

    @Test
    void snapshotWithNegativeEnergyFailsToDecode() {
        FriendlyByteBuf b = buf();
        b.writeLong(1L);
        b.writeVarInt(1);
        b.writeBoolean(true);
        b.writeVarInt(-1);
        b.writeVarInt(100);
        b.writeVarInt(0);
        b.writeBoolean(false);
        PayloadBounds.writeEnum(b, WitchSnapshot.Recovering.NONE);
        b.writeBoolean(false);
        assertThrows(DecoderException.class, () -> WitchSnapshot.decode(b));
    }

    @Test
    void snapshotWithUnknownRecoveringFailsToDecode() {
        FriendlyByteBuf b = buf();
        b.writeLong(1L);
        b.writeVarInt(1);
        b.writeBoolean(true);
        b.writeVarInt(1);
        b.writeVarInt(100);
        b.writeVarInt(0);
        b.writeBoolean(false);
        b.writeVarInt(99);
        b.writeBoolean(false);
        assertThrows(DecoderException.class, () -> WitchSnapshot.decode(b));
    }

    // ----- WitchRulesUpdate and fingerprint -----

    @Test
    void defaultRulesRoundTrip() {
        FriendlyByteBuf b = buf();
        new WitchRulesUpdate(WitchRules.DEFAULTS).encode(b);
        WitchRulesUpdate decoded = WitchRulesUpdate.decode(b);
        assertEquals(WitchRules.DEFAULTS, decoded.rules());
        assertEquals(0, b.readableBytes());
    }

    @Test
    void customRulesRoundTrip() {
        WitchRules custom = new WitchRules(false, true, 40, 80, 20, 300, 0, balance(List.of(10, 20, 30), 5));
        FriendlyByteBuf b = buf();
        new WitchRulesUpdate(custom).encode(b);
        assertEquals(custom, WitchRulesUpdate.decode(b).rules());
    }

    @Test
    void invalidRulesAreRefusedOnDecode() {
        FriendlyByteBuf b = buf();
        new WitchRulesUpdate(rulesWith(balance(List.of(50, 100), -1))).encode(b);
        assertThrows(DecoderException.class, () -> WitchRulesUpdate.decode(b));

        FriendlyByteBuf decreasing = buf();
        new WitchRulesUpdate(rulesWith(balance(List.of(100, 50), 10))).encode(decreasing);
        assertThrows(DecoderException.class, () -> WitchRulesUpdate.decode(decreasing));

        FriendlyByteBuf badDuration = buf();
        new WitchRulesUpdate(new WitchRules(true, true, 0, 200, 100, 600, 100, EnergyBalance.DEFAULTS)).encode(badDuration);
        assertThrows(DecoderException.class, () -> WitchRulesUpdate.decode(badDuration));
    }

    @Test
    void aThresholdCountAboveThirtyTwoIsRefused() {
        FriendlyByteBuf b = buf();
        b.writeBoolean(true);
        b.writeBoolean(true);
        for (int i = 0; i < 5; i++) {
            b.writeVarInt(100);
        }
        b.writeVarInt(33);
        for (int i = 1; i <= 33; i++) {
            b.writeVarInt(i);
        }
        assertThrows(DecoderException.class, () -> WitchRulesUpdate.decode(b));
    }

    @Test
    void fingerprintIsStableForEqualRulesAndChangesWithAnyValue() {
        int base = WitchRules.DEFAULTS.fingerprint();
        WitchRules same = new WitchRules(true, true, 200, 200, 100, 600, 100, balance(List.of(50, 100, 150, 200, 250, 300, 350, 400, 450, 500), 10));
        assertEquals(WitchRules.DEFAULTS, same);
        assertEquals(base, same.fingerprint());
        assertEquals(base, WitchRules.DEFAULTS.fingerprint());

        assertNotEquals(base, new WitchRules(false, true, 200, 200, 100, 600, 100, EnergyBalance.DEFAULTS).fingerprint());
        assertNotEquals(base, new WitchRules(true, true, 201, 200, 100, 600, 100, EnergyBalance.DEFAULTS).fingerprint());
        assertNotEquals(base, new WitchRules(true, true, 200, 200, 100, 600, 101, EnergyBalance.DEFAULTS).fingerprint());
        assertNotEquals(base, rulesWith(balance(List.of(50, 100, 150, 200, 250, 300, 350, 400, 450, 500), 11)).fingerprint());
        assertNotEquals(base, rulesWith(balance(List.of(50, 100, 150, 200, 250, 300, 350, 400, 450, 501), 10)).fingerprint());
    }

    // ----- AltarRequest -----

    @Test
    void altarRequestRoundTrips() {
        AltarRequest invite = new AltarRequest(7, 123456789L, WitchRules.DEFAULTS.fingerprint(), AltarRequest.Op.INVITE, Optional.of(PLAYER), 0L);
        FriendlyByteBuf b = buf();
        invite.encode(b);
        assertEquals(invite, AltarRequest.decode(b));

        AltarRequest accept = new AltarRequest(8, 5L, -42, AltarRequest.Op.ACCEPT, Optional.empty(), 99L);
        FriendlyByteBuf b2 = buf();
        accept.encode(b2);
        assertEquals(accept, AltarRequest.decode(b2));
    }

    @Test
    void everyAltarOperationRoundTrips() {
        for (AltarRequest.Op op : AltarRequest.Op.values()) {
            AltarRequest request = new AltarRequest(1, 2L, 3, op, Optional.of(OTHER), 4L);
            FriendlyByteBuf b = buf();
            request.encode(b);
            assertEquals(request, AltarRequest.decode(b));
        }
    }

    @Test
    void anUnknownAltarOperationFailsToDecode() {
        FriendlyByteBuf b = buf();
        b.writeVarInt(1);
        b.writeLong(2L);
        b.writeInt(3);
        b.writeVarInt(AltarRequest.Op.values().length);
        b.writeBoolean(false);
        b.writeLong(0L);
        assertThrows(DecoderException.class, () -> AltarRequest.decode(b));
    }

    @Test
    void anOversizedAltarRequestFailsBeforeDecoding() {
        AltarRequest request = new AltarRequest(1, 2L, 3, AltarRequest.Op.STOP, Optional.empty(), 0L);
        FriendlyByteBuf over = buf();
        request.encode(over);
        over.writeBytes(new byte[PayloadBounds.MAX_REQUEST_BYTES]);
        assertTrue(over.readableBytes() > PayloadBounds.MAX_REQUEST_BYTES);
        assertThrows(DecoderException.class, () -> AltarRequest.decode(over));

        FriendlyByteBuf atLimit = buf();
        request.encode(atLimit);
        atLimit.writeBytes(new byte[PayloadBounds.MAX_REQUEST_BYTES - atLimit.readableBytes()]);
        assertEquals(PayloadBounds.MAX_REQUEST_BYTES, atLimit.readableBytes());
        assertEquals(request, AltarRequest.decode(atLimit));
    }

    // ----- RateLimit -----

    @Test
    void rateLimitAllowsFivePerTwentyTicksAndRefusesTheSixth() {
        RateLimit limit = new RateLimit(5, 20);
        for (int i = 0; i < 5; i++) {
            assertTrue(limit.allow(PLAYER, i), "request " + (i + 1));
        }
        assertFalse(limit.allow(PLAYER, 5));
        assertFalse(limit.allow(PLAYER, 19));
    }

    @Test
    void rateLimitAllowsAgainOnceTheWindowPasses() {
        RateLimit limit = new RateLimit(5, 20);
        for (int i = 0; i < 5; i++) {
            limit.allow(PLAYER, 0);
        }
        assertFalse(limit.allow(PLAYER, 19));
        assertTrue(limit.allow(PLAYER, 20), "the oldest requests have left the window");
        assertTrue(limit.allow(PLAYER, 20));
    }

    @Test
    void refusedRequestsAreNotRecorded() {
        RateLimit limit = new RateLimit(5, 20);
        for (int i = 0; i < 5; i++) {
            limit.allow(PLAYER, 0);
        }
        for (int t = 1; t < 20; t++) {
            assertFalse(limit.allow(PLAYER, t));
        }
        assertTrue(limit.allow(PLAYER, 20), "spamming while limited does not extend the limit");
    }

    @Test
    void rateLimitPlayersAreIndependent() {
        RateLimit limit = new RateLimit(5, 20);
        for (int i = 0; i < 5; i++) {
            limit.allow(PLAYER, 0);
        }
        assertFalse(limit.allow(PLAYER, 1));
        assertTrue(limit.allow(OTHER, 1));
    }

    @Test
    void forgetClearsAPlayersHistory() {
        RateLimit limit = new RateLimit(5, 20);
        for (int i = 0; i < 5; i++) {
            limit.allow(PLAYER, 0);
        }
        assertFalse(limit.allow(PLAYER, 1));
        limit.forget(PLAYER);
        assertTrue(limit.allow(PLAYER, 1));
    }
}
