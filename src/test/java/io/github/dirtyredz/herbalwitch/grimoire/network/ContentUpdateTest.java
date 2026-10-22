package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat.Preference;
import io.github.dirtyredz.herbalwitch.apothecary.botany.conditions.HerbConditions;
import io.github.dirtyredz.herbalwitch.platform.network.ChunkAssembly;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Protocol 4 evidence captured before replacing the botany transport dependency. */
class ContentUpdateTest {
    private static final byte[] GOLDEN = HexFormat.of().parseHex(
            "07000101010106783a6865726209783a686162697461743fe00000000000003fd00000000000000400010203");

    private static ContentUpdate sample() {
        return new ContentUpdate(new ChunkAssembly.Header(7, 0, 1, 1), List.of(new ContentUpdate.Herb(
                new HerbConditions.Condition("x:herb", "x:habitat", 0.5, 0.25,
                        List.of(Preference.RAIN, Preference.LOW_SKY, Preference.NIGHT, Preference.FULL_MOON)))));
    }

    @Test
    void protocolFourHerbBytesAndPreferenceCodesAreFixed() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            sample().encode(buf);
            byte[] actual = new byte[buf.readableBytes()];
            buf.getBytes(0, actual);
            assertArrayEquals(GOLDEN, actual);
            assertEquals(sample(), ContentUpdate.decode(buf));
        } finally {
            buf.release();
        }
    }

    @Test
    void goldenPacketDecodesWithoutUsingTheEncoder() {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(GOLDEN));
        try {
            assertEquals(sample(), ContentUpdate.decode(buf));
            assertEquals(0, buf.readableBytes());
        } finally {
            buf.release();
        }
    }

    @Test
    void unknownPreferenceCodeIsRejected() {
        byte[] bad = GOLDEN.clone();
        bad[bad.length - 1] = 4;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bad));
        try {
            assertThrows(DecoderException.class, () -> ContentUpdate.decode(buf));
        } finally {
            buf.release();
        }
    }

    @Test
    void tooManyPreferencesAreRejected() {
        byte[] bad = GOLDEN.clone();
        bad[bad.length - 5] = 5;
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bad));
        try {
            assertThrows(DecoderException.class, () -> ContentUpdate.decode(buf));
        } finally {
            buf.release();
        }
    }

    @Test
    void nonFiniteChanceIsRejected() {
        byte[] bad = GOLDEN.clone();
        // The two length-prefixed identifiers end at offset 23; discovery is the next double.
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(bad));
        try {
            buf.setDouble(23, Double.NaN);
            assertThrows(DecoderException.class, () -> ContentUpdate.decode(buf));
        } finally {
            buf.release();
        }
    }
}
