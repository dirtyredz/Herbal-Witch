package io.github.dirtyredz.herbalwitch.platform.network;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Decoding limits and bounded, atomic snapshot assembly; nothing is ever silently truncated. */
class PayloadBoundsTest {
    private enum Op {
        A,
        B
    }

    private static FriendlyByteBuf buf() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    @Test
    void oversizedCountsStringsAndUnknownDiscriminatorsAreRefused() {
        FriendlyByteBuf count = buf();
        count.writeVarInt(PayloadBounds.MAX_CHUNK_RECORDS + 1);
        assertThrows(DecoderException.class, () -> PayloadBounds.readCount(count, PayloadBounds.MAX_CHUNK_RECORDS));
        FriendlyByteBuf negative = buf();
        negative.writeVarInt(-1);
        assertThrows(DecoderException.class, () -> PayloadBounds.readCount(negative, 10));
        FriendlyByteBuf text = buf();
        text.writeUtf("x".repeat(PayloadBounds.MAX_ID + 1), 32767);
        assertThrows(DecoderException.class, () -> PayloadBounds.readString(text, PayloadBounds.MAX_ID));
        FriendlyByteBuf discriminator = buf();
        discriminator.writeVarInt(2);
        assertThrows(DecoderException.class, () -> PayloadBounds.readEnum(discriminator, Op.class));
        FriendlyByteBuf ok = buf();
        PayloadBounds.writeEnum(ok, Op.B);
        assertEquals(Op.B, PayloadBounds.readEnum(ok, Op.class));
    }

    @Test
    void chunksRespectRecordAndByteLimitsAndRefuseExcess() {
        List<Integer> records = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            records.add(i);
        }
        List<List<Integer>> byCount = PayloadBounds.chunk(records, r -> 10);
        assertEquals(4, byCount.size());
        assertTrue(byCount.stream().allMatch(c -> c.size() <= PayloadBounds.MAX_CHUNK_RECORDS));
        List<List<Integer>> byBytes = PayloadBounds.chunk(records.subList(0, 10), r -> 5000);
        assertTrue(byBytes.stream().allMatch(c -> c.size() * 5000 <= PayloadBounds.MAX_CHUNK_BYTES));
        assertEquals(List.of(List.of()), PayloadBounds.chunk(List.of(), r -> 1), "an empty snapshot is one empty chunk");
        List<Integer> tooMany = new ArrayList<>();
        for (int i = 0; i <= PayloadBounds.MAX_TOTAL_RECORDS; i++) {
            tooMany.add(i);
        }
        assertThrows(IllegalArgumentException.class, () -> PayloadBounds.chunk(tooMany, r -> 1));
        assertThrows(IllegalArgumentException.class, () -> PayloadBounds.chunk(List.of(1), r -> PayloadBounds.MAX_CHUNK_BYTES + 1));
    }

    @Test
    void aSnapshotAssemblesOnlyWhenCompleteAndConsistent() {
        ChunkAssembly<String> assembly = new ChunkAssembly<>();
        assertEquals(Optional.empty(), assembly.offer(new ChunkAssembly.Header(7, 0, 2, 3), List.of("a", "b")));
        assertEquals(Optional.of(List.of("a", "b", "c")), assembly.offer(new ChunkAssembly.Header(7, 1, 2, 3), List.of("c")));
    }

    @Test
    void staleMismatchedOrOutOfOrderChunksAreRejected() {
        ChunkAssembly<String> assembly = new ChunkAssembly<>();
        assembly.offer(new ChunkAssembly.Header(7, 0, 2, 3), List.of("a", "b"));
        assertThrows(IllegalStateException.class, () -> assembly.offer(new ChunkAssembly.Header(8, 1, 2, 3), List.of("c")),
                "another revision");
        assertThrows(IllegalStateException.class, () -> assembly.offer(new ChunkAssembly.Header(7, 1, 2, 3), List.of("c")),
                "the assembly was reset, so a lone second chunk does not belong");
        assembly.offer(new ChunkAssembly.Header(7, 0, 2, 3), List.of("a", "b"));
        assertThrows(IllegalStateException.class, () -> assembly.offer(new ChunkAssembly.Header(7, 1, 2, 4), List.of("c")),
                "a different declared total");
        assembly.offer(new ChunkAssembly.Header(7, 0, 2, 3), List.of("a", "b"));
        assertThrows(IllegalStateException.class, () -> assembly.offer(new ChunkAssembly.Header(7, 1, 2, 3), List.of("c", "d")),
                "more records than declared");
        assertThrows(IllegalStateException.class, () -> assembly.offer(new ChunkAssembly.Header(1, 0, 1, 2), List.of("x")),
                "fewer records than declared");
        assertThrows(IllegalArgumentException.class, () -> new ChunkAssembly.Header(1, 2, 2, 0), "index past the count");
        assertThrows(IllegalArgumentException.class, () -> new ChunkAssembly.Header(1, 0, 1, PayloadBounds.MAX_TOTAL_RECORDS + 1));
        assertEquals(Optional.of(List.of("z")), assembly.offer(new ChunkAssembly.Header(2, 0, 1, 1), List.of("z")),
                "a fresh snapshot still assembles after rejections");
    }
}
