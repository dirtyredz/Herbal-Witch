package io.github.dirtyredz.herbalwitch.magic.witch.initiation.founding;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * The Overworld's founding record ({@code data/herbalwitch_founding.dat}), schema-versioned and
 * marked dirty on every mutation. Before an irreversible completion the durable claim is also
 * written straight to its file ({@link #persistNow}): if that write fails, the completion is aborted
 * before anything is consumed.
 */
public final class FoundingSavedData extends SavedData {
    public static final String NAME = "herbalwitch_founding";
    private static final Logger LOGGER = LogUtils.getLogger();

    private FoundingCodec.Loaded loaded;

    private FoundingSavedData(FoundingCodec.Loaded loaded) {
        this.loaded = loaded;
        loaded.problem().ifPresent(p -> LOGGER.error("The founding record could not be read ({}); it is kept unchanged and founding "
                + "is unavailable until an operator inspects it", p));
    }

    public static FoundingSavedData create() {
        return new FoundingSavedData(FoundingCodec.read(null));
    }

    public static FoundingSavedData load(CompoundTag tag) {
        return new FoundingSavedData(FoundingCodec.read(tag));
    }

    public FoundingRecord record() {
        return loaded.record();
    }

    /** Whether the saved record was readable (an unreadable one is preserved and never changed). */
    public boolean writable() {
        return loaded.preserved().isEmpty();
    }

    public java.util.Optional<String> problem() {
        return loaded.problem();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return FoundingCodec.write(loaded);
    }

    /**
     * Writes this record to its file now, the way the data storage would (atomically, through a
     * temporary file). Returns false, having logged why, when it could not be written.
     */
    public boolean persistNow(MinecraftServer server) {
        Path dir = server.getWorldPath(LevelResource.ROOT).resolve("data");
        Path file = dir.resolve(NAME + ".dat");
        try {
            Files.createDirectories(dir);
            CompoundTag root = new CompoundTag();
            root.put("data", save(new CompoundTag()));
            NbtUtils.addCurrentDataVersion(root);
            Path temp = Files.createTempFile(dir, NAME, ".tmp");
            NbtIo.writeCompressed(root, temp.toFile());
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Could not persist the founding claim to {}", file, e);
            return false;
        }
    }
}
