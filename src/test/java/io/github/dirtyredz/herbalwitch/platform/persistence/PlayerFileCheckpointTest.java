package io.github.dirtyredz.herbalwitch.platform.persistence;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerFileCheckpointTest {
    @TempDir Path world;

    @Test void forcedHostReadRequiresIdentityAndMarkerFromLevelPlayer() throws Exception {
        UUID owner = UUID.randomUUID();
        Path level = world.resolve("level.dat");
        for (String key : new String[]{"herbalwitch_familiar_receipt", "herbalwitch_shop_receipt"}) {
            CompoundTag expected = marker(5);
            writeLevel(player(owner, key, marker(4)));
            assertFalse(PlayerFileCheckpoint.verify(level, owner, saved -> expected.equals(saved.getCompound("ForgeData").getCompound(key))));
            writeLevel(player(UUID.randomUUID(), key, expected));
            assertFalse(PlayerFileCheckpoint.verify(level, owner, saved -> true));
            writeLevel(player(owner, key, expected));
            assertTrue(PlayerFileCheckpoint.verify(level, owner, saved -> expected.equals(saved.getCompound("ForgeData").getCompound(key))));
        }
    }

    @Test void dedicatedAndGuestReadTheirOwnFileWhileHostAlwaysUsesLevel() throws Exception {
        UUID owner = UUID.randomUUID(), guest = UUID.randomUUID();
        String key = "receipt";
        CompoundTag newer = player(owner, key, marker(5));
        Files.createDirectories(world.resolve("playerdata"));
        Path file = world.resolve("playerdata").resolve(owner + ".dat");
        NbtIo.writeCompressed(newer, file.toFile());
        assertTrue(PlayerFileCheckpoint.verify(file, null, newer::equals));
        assertEquals(newer, PlayerFileCheckpoint.saved(world, owner));
        writeLevel(player(owner, key, marker(4)));
        assertEquals(4, PlayerFileCheckpoint.saved(world, owner).getCompound("ForgeData").getCompound(key).getLong("sequence"));
        CompoundTag guestData = player(guest, key, marker(7));
        NbtIo.writeCompressed(guestData, world.resolve("playerdata").resolve(guest + ".dat").toFile());
        assertEquals(guestData, PlayerFileCheckpoint.saved(world, guest));
        Files.delete(file);
        assertEquals(4, PlayerFileCheckpoint.saved(world, owner).getCompound("ForgeData").getCompound(key).getLong("sequence"));
    }

    @Test void missingMalformedAndOversizedFilesNeverProveAReceipt() throws Exception {
        UUID owner = UUID.randomUUID();
        Path level = world.resolve("level.dat");
        assertThrows(IOException.class, () -> PlayerFileCheckpoint.verify(level, owner, saved -> true));
        Files.writeString(level, "not compressed NBT");
        assertThrows(IOException.class, () -> PlayerFileCheckpoint.saved(world, owner));
        Files.write(level, new byte[4 * 1024 * 1024 + 1]);
        assertThrows(IOException.class, () -> PlayerFileCheckpoint.verify(level, owner, saved -> true));
        writeLevel(new CompoundTag());
        assertFalse(PlayerFileCheckpoint.verify(level, owner, saved -> true));
        assertThrows(IOException.class, () -> PlayerFileCheckpoint.saved(world, owner));
    }

    @Test void cloneCopiesOnlyTheRequestedMarkerWithoutAliasing() {
        var original = new CompoundTag(); original.put("receipt", marker(5)); original.putString("unrelated", "old");
        var replacement = new CompoundTag(); replacement.putString("unrelated", "new");
        PlayerFileCheckpoint.copy(original, replacement, "receipt");
        original.getCompound("receipt").putLong("sequence", 6);
        assertEquals(5, replacement.getCompound("receipt").getLong("sequence"));
        assertEquals("new", replacement.getString("unrelated"));
        PlayerFileCheckpoint.copy(original, replacement, "absent");
        assertFalse(replacement.contains("absent"));
    }

    private void writeLevel(CompoundTag player) throws IOException {
        var data = new CompoundTag(); data.put("Player", player);
        var level = new CompoundTag(); level.put("Data", data);
        NbtIo.writeCompressed(level, world.resolve("level.dat").toFile());
    }

    private static CompoundTag marker(long sequence) {
        var tag = new CompoundTag(); tag.putInt("schema", 1); tag.putLong("sequence", sequence); return tag;
    }

    private static CompoundTag player(UUID owner, String key, CompoundTag marker) {
        var tag = new CompoundTag(); tag.putUUID("UUID", owner);
        var forge = new CompoundTag(); forge.put(key, marker); tag.put("ForgeData", forge); return tag;
    }
}
