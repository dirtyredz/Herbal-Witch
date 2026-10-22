package io.github.dirtyredz.herbalwitch.platform.network;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Reusable limits for every message on the channel: bounded strings, counts and enum
 * discriminators, and splitting record lists into bounded chunks. A decoder that meets anything
 * outside its bound throws {@link DecoderException}, so the connection drops the message before any
 * handler sees it. Nothing is ever truncated silently.
 */
public final class PayloadBounds {
    /** Records per snapshot chunk. */
    public static final int MAX_CHUNK_RECORDS = 64;
    /** Encoded bytes of the records in one chunk. */
    public static final int MAX_CHUNK_BYTES = 24 * 1024;
    /** Records in one complete snapshot (content entries or discoveries). */
    public static final int MAX_TOTAL_RECORDS = 4096;
    /** Characters of an id (namespace:path with a kind prefix). */
    public static final int MAX_ID = 256;
    /** Characters of one line of text or a translation key. */
    public static final int MAX_TEXT = 512;

    /** Encoded bytes of one feature request (client to server). */
    public static final int MAX_REQUEST_BYTES = 4 * 1024;

    private PayloadBounds() {
    }

    /** Refuses a request buffer larger than {@code max} bytes before anything is decoded. */
    public static void requireAtMost(FriendlyByteBuf buf, int max) {
        if (buf.readableBytes() > max) {
            throw new DecoderException("payload of " + buf.readableBytes() + " bytes exceeds " + max);
        }
    }

    /** A UTF-8 string of at most {@code max} characters. */
    public static String readString(FriendlyByteBuf buf, int max) {
        return buf.readUtf(max);
    }

    /** A count in {@code 0..max}. */
    public static int readCount(FriendlyByteBuf buf, int max) {
        int n = buf.readVarInt();
        if (n < 0 || n > max) {
            throw new DecoderException("count " + n + " outside 0.." + max);
        }
        return n;
    }

    /** An enum constant by ordinal, refusing unknown discriminators. */
    public static <E extends Enum<E>> E readEnum(FriendlyByteBuf buf, Class<E> type) {
        int ordinal = buf.readVarInt();
        E[] values = type.getEnumConstants();
        if (ordinal < 0 || ordinal >= values.length) {
            throw new DecoderException("unknown " + type.getSimpleName() + " " + ordinal);
        }
        return values[ordinal];
    }

    public static <E extends Enum<E>> void writeEnum(FriendlyByteBuf buf, E value) {
        buf.writeVarInt(value.ordinal());
    }

    /** Encoded size of a string as written by {@link FriendlyByteBuf#writeUtf}: length prefix plus UTF-8 bytes. */
    public static int stringBytes(String s) {
        return 5 + s.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Splits records into chunks of at most {@link #MAX_CHUNK_RECORDS} records and
     * {@link #MAX_CHUNK_BYTES} encoded bytes, in order. Refuses (never truncates) more than
     * {@link #MAX_TOTAL_RECORDS} records or a single record too large for any chunk.
     */
    public static <T> List<List<T>> chunk(List<T> records, ToIntFunction<T> encodedBytes) {
        if (records.size() > MAX_TOTAL_RECORDS) {
            throw new IllegalArgumentException(records.size() + " records exceed the " + MAX_TOTAL_RECORDS + " ceiling");
        }
        List<List<T>> chunks = new ArrayList<>();
        List<T> current = new ArrayList<>();
        int bytes = 0;
        for (T record : records) {
            int size = encodedBytes.applyAsInt(record);
            if (size > MAX_CHUNK_BYTES) {
                throw new IllegalArgumentException("a record of " + size + " bytes exceeds one chunk");
            }
            if (current.size() == MAX_CHUNK_RECORDS || bytes + size > MAX_CHUNK_BYTES) {
                chunks.add(List.copyOf(current));
                current.clear();
                bytes = 0;
            }
            current.add(record);
            bytes += size;
        }
        if (!current.isEmpty() || chunks.isEmpty()) {
            chunks.add(List.copyOf(current));
        }
        return chunks;
    }
}
