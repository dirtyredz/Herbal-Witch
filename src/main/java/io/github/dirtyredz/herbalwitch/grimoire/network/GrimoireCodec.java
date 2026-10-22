package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.platform.network.ChunkAssembly;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Bounded field codecs the grimoire messages share: entry keys, chunk headers, optional and list fields. */
public final class GrimoireCodec {
    private GrimoireCodec() {
    }

    public static void writeKey(FriendlyByteBuf buf, EntryKey key) {
        buf.writeUtf(key.serialized(), PayloadBounds.MAX_ID);
    }

    public static EntryKey readKey(FriendlyByteBuf buf) {
        String raw = PayloadBounds.readString(buf, PayloadBounds.MAX_ID);
        return EntryKey.parse(raw).orElseThrow(() -> new DecoderException("bad entry key " + raw));
    }

    public static void writeOptionalKey(FriendlyByteBuf buf, Optional<EntryKey> key) {
        buf.writeBoolean(key.isPresent());
        key.ifPresent(k -> writeKey(buf, k));
    }

    public static Optional<EntryKey> readOptionalKey(FriendlyByteBuf buf) {
        return buf.readBoolean() ? Optional.of(readKey(buf)) : Optional.empty();
    }

    public static void writeHeader(FriendlyByteBuf buf, ChunkAssembly.Header header) {
        buf.writeVarInt(header.revision());
        buf.writeVarInt(header.index());
        buf.writeVarInt(header.chunks());
        buf.writeVarInt(header.totalRecords());
    }

    public static ChunkAssembly.Header readHeader(FriendlyByteBuf buf) {
        int revision = buf.readVarInt();
        int index = buf.readVarInt();
        int chunks = buf.readVarInt();
        int total = buf.readVarInt();
        try {
            return new ChunkAssembly.Header(revision, index, chunks, total);
        } catch (IllegalArgumentException e) {
            throw new DecoderException(e.getMessage());
        }
    }

    public static void writeOptionalText(FriendlyByteBuf buf, Optional<String> text) {
        buf.writeBoolean(text.isPresent());
        text.ifPresent(t -> buf.writeUtf(t, PayloadBounds.MAX_TEXT));
    }

    public static Optional<String> readOptionalText(FriendlyByteBuf buf) {
        return buf.readBoolean() ? Optional.of(PayloadBounds.readString(buf, PayloadBounds.MAX_TEXT)) : Optional.empty();
    }

    public static void writeTexts(FriendlyByteBuf buf, List<String> texts) {
        buf.writeVarInt(texts.size());
        texts.forEach(t -> buf.writeUtf(t, PayloadBounds.MAX_TEXT));
    }

    public static List<String> readTexts(FriendlyByteBuf buf, int max) {
        int n = PayloadBounds.readCount(buf, max);
        List<String> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(PayloadBounds.readString(buf, PayloadBounds.MAX_TEXT));
        }
        return out;
    }
}
