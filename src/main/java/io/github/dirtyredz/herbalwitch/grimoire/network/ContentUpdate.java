package io.github.dirtyredz.herbalwitch.grimoire.network;

import io.github.dirtyredz.herbalwitch.grimoire.content.input.HerbSummary;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.reload.GrimoireDefinition;
import io.github.dirtyredz.herbalwitch.platform.network.ChunkAssembly;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Server to client: the inputs a client cannot know by itself, so it builds the same content as the
 * server: the editorial definitions and the server's herb conditions (never a client's local
 * defaults). Recipe bodies are not sent; clients have them through vanilla recipe sync. Sent in
 * bounded chunks of one content revision, assembled atomically.
 *
 * @param header  chunk header (the revision is the server's content revision)
 * @param records definitions and herb conditions
 */
public record ContentUpdate(ChunkAssembly.Header header, List<Record> records) {
    /** One synchronised input. */
    public sealed interface Record permits Definition, Herb {
    }

    public record Definition(GrimoireDefinition definition) implements Record {
    }

    public record Herb(HerbSummary condition) implements Record {
    }

    public ContentUpdate {
        records = List.copyOf(records);
    }

    /** The complete content inputs of one revision, in chunks. */
    public static List<ContentUpdate> of(int revision, Map<EntryKey, GrimoireDefinition> definitions,
                                         Map<String, HerbSummary> herbs) {
        List<Record> all = new ArrayList<>();
        definitions.values().stream().sorted((a, b) -> a.key().compareTo(b.key())).forEach(d -> all.add(new Definition(d)));
        herbs.values().forEach(c -> all.add(new Herb(c)));
        List<List<Record>> chunks = PayloadBounds.chunk(all, ContentUpdate::size);
        List<ContentUpdate> out = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            out.add(new ContentUpdate(new ChunkAssembly.Header(revision, i, chunks.size(), all.size()), chunks.get(i)));
        }
        return out;
    }

    private static int size(Record record) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            write(buf, record);
            return buf.readableBytes();
        } finally {
            buf.release();
        }
    }

    public void encode(FriendlyByteBuf buf) {
        GrimoireCodec.writeHeader(buf, header);
        buf.writeVarInt(records.size());
        records.forEach(r -> write(buf, r));
    }

    public static ContentUpdate decode(FriendlyByteBuf buf) {
        ChunkAssembly.Header header = GrimoireCodec.readHeader(buf);
        int n = PayloadBounds.readCount(buf, PayloadBounds.MAX_CHUNK_RECORDS);
        List<Record> records = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            records.add(read(buf));
        }
        return new ContentUpdate(header, records);
    }

    private static void write(FriendlyByteBuf buf, Record record) {
        if (record instanceof Definition definition) {
            GrimoireDefinition d = definition.definition();
            buf.writeByte(0);
            GrimoireCodec.writeKey(buf, d.key());
            GrimoireCodec.writeOptionalText(buf, d.category());
            GrimoireCodec.writeOptionalText(buf, d.title());
            GrimoireCodec.writeTexts(buf, d.pages());
            buf.writeVarInt(d.links().size());
            d.links().forEach(l -> GrimoireCodec.writeKey(buf, l));
            GrimoireCodec.writeOptionalText(buf, d.hint());
        } else if (record instanceof Herb herb) {
            HerbSummary c = herb.condition();
            buf.writeByte(1);
            buf.writeUtf(c.herb(), PayloadBounds.MAX_ID);
            buf.writeUtf(c.habitat(), PayloadBounds.MAX_ID);
            buf.writeDouble(c.discoveryChance());
            buf.writeDouble(c.harvestBonusChance());
            buf.writeVarInt(c.preferences().size());
            c.preferences().forEach(buf::writeVarInt);
        }
    }

    private static Record read(FriendlyByteBuf buf) {
        byte kind = buf.readByte();
        switch (kind) {
            case 0 -> {
                EntryKey key = GrimoireCodec.readKey(buf);
                var category = GrimoireCodec.readOptionalText(buf);
                var title = GrimoireCodec.readOptionalText(buf);
                List<String> pages = GrimoireCodec.readTexts(buf, GrimoireDefinition.MAX_PAGES);
                int links = PayloadBounds.readCount(buf, GrimoireDefinition.MAX_LINKS);
                List<EntryKey> linkKeys = new ArrayList<>(links);
                for (int i = 0; i < links; i++) {
                    linkKeys.add(GrimoireCodec.readKey(buf));
                }
                return new Definition(new GrimoireDefinition(key, category, title, pages, linkKeys, GrimoireCodec.readOptionalText(buf)));
            }
            case 1 -> {
                String herb = PayloadBounds.readString(buf, PayloadBounds.MAX_ID);
                String habitat = PayloadBounds.readString(buf, PayloadBounds.MAX_ID);
                double discovery = buf.readDouble();
                double bonus = buf.readDouble();
                if (!(discovery >= 0 && discovery <= 1 && bonus >= 0 && bonus <= 1)) {
                    throw new DecoderException("herb chance out of range");
                }
                int n = PayloadBounds.readCount(buf, HerbSummary.PREFERENCE_COUNT);
                List<Integer> preferences = new ArrayList<>(n);
                for (int i = 0; i < n; i++) {
                    int code = buf.readVarInt();
                    if (code < 0 || code >= HerbSummary.PREFERENCE_COUNT) {
                        throw new DecoderException("unknown herb preference " + code);
                    }
                    preferences.add(code);
                }
                return new Herb(new HerbSummary(herb, habitat, discovery, bonus, preferences));
            }
            default -> throw new DecoderException("unknown content record " + kind);
        }
    }
}
