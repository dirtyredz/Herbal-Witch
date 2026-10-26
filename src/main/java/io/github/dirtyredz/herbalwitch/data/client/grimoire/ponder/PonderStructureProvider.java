package io.github.dirtyredz.herbalwitch.data.client.grimoire.ponder;

import com.google.common.hash.Hashing;
import io.github.dirtyredz.herbalwitch.data.client.grimoire.guide.GuideSceneBlocks.Placement;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationLayout;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;
import net.minecraft.core.Vec3i;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.CRC32;

/**
 * Writes each animated lesson's structure as gzip-compressed vanilla structure NBT at the path Ponder loads
 * ({@code assets/<ns>/ponder/<path>.nbt}). Output is byte-stable on every platform: blocks are ordered by
 * position, the palette by first use, and the gzip stream uses stored (uncompressed) deflate blocks written
 * here, so no platform zlib can change the bytes that hosted CI compares. No Ponder types are involved.
 */
public final class PonderStructureProvider implements DataProvider {
    /** Minecraft 1.20.1's data version. */
    static final int DATA_VERSION = 3465;
    private static final int STORED_BLOCK = 0xFFFF;

    private final PackOutput output;

    public PonderStructureProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public String getName() {
        return "Herbal Witch animated lesson structures";
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        try {
            write(cache, GuideLessons.FIRST_PREPARATION, FirstPreparationLayout.SIZE, FirstPreparationStructure.placements());
            return CompletableFuture.completedFuture(null);
        } catch (IOException | RuntimeException error) {
            return CompletableFuture.failedFuture(error);
        }
    }

    private void write(CachedOutput cache, GuideLessons.Lesson lesson, Vec3i size, List<Placement> placements) throws IOException {
        byte[] bytes = gzip(nbt(structure(size, placements)));
        cache.writeIfNeeded(output.getOutputFolder().resolve(lesson.schematicAsset()), bytes, Hashing.sha1().hashBytes(bytes));
    }

    /** The vanilla structure-template tag: size, palette, blocks (no block-entity data) and no entities. */
    static CompoundTag structure(Vec3i size, List<Placement> placements) {
        List<Placement> ordered = new ArrayList<>(placements);
        ordered.sort(Comparator.comparingInt((Placement p) -> p.pos().getY())
                .thenComparingInt(p -> p.pos().getZ()).thenComparingInt(p -> p.pos().getX()));
        Map<BlockState, Integer> palette = new LinkedHashMap<>();
        ListTag blocks = new ListTag();
        for (Placement placement : ordered) {
            int index = palette.computeIfAbsent(placement.state(), state -> palette.size());
            CompoundTag block = new CompoundTag();
            block.put("pos", ints(placement.pos().getX(), placement.pos().getY(), placement.pos().getZ()));
            block.putInt("state", index);
            blocks.add(block);
        }
        ListTag paletteTag = new ListTag();
        palette.keySet().forEach(state -> paletteTag.add(NbtUtils.writeBlockState(state)));
        CompoundTag tag = new CompoundTag();
        tag.putInt("DataVersion", DATA_VERSION);
        tag.put("size", ints(size.getX(), size.getY(), size.getZ()));
        tag.put("palette", paletteTag);
        tag.put("blocks", blocks);
        tag.put("entities", new ListTag());
        return tag;
    }

    private static ListTag ints(int... values) {
        ListTag list = new ListTag();
        for (int value : values) list.add(IntTag.valueOf(value));
        return list;
    }

    private static byte[] nbt(CompoundTag tag) throws IOException {
        var bytes = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(bytes)) {
            NbtIo.write(tag, out);
        }
        return bytes.toByteArray();
    }

    /** RFC 1952 gzip with RFC 1951 stored blocks: zero mtime, unknown OS, deterministic everywhere. */
    static byte[] gzip(byte[] data) {
        var out = new ByteArrayOutputStream();
        out.writeBytes(new byte[] {0x1f, (byte) 0x8b, 8, 0, 0, 0, 0, 0, 0, (byte) 0xff});
        int offset = 0;
        do {
            int length = Math.min(STORED_BLOCK, data.length - offset);
            boolean last = offset + length == data.length;
            out.write(last ? 1 : 0);
            out.write(length & 0xff);
            out.write(length >>> 8);
            out.write(~length & 0xff);
            out.write((~length >>> 8) & 0xff);
            out.write(data, offset, length);
            offset += length;
        } while (offset < data.length);
        var crc = new CRC32();
        crc.update(data);
        littleEndian(out, (int) crc.getValue());
        littleEndian(out, data.length);
        return out.toByteArray();
    }

    private static void littleEndian(ByteArrayOutputStream out, int value) {
        for (int shift = 0; shift < 32; shift += 8) out.write((value >>> shift) & 0xff);
    }
}
