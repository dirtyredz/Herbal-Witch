package io.github.dirtyredz.herbalwitch.magic.ward;

import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardCodec;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ward records: round trips, an unsupported file kept byte for byte, malformed records preserved and the record cap. */
class WardCodecTest {
    private static final UUID OWNER = UUID.nameUUIDFromBytes("owner".getBytes());

    private static WardCodec.Loaded loaded(WardRecord... records) {
        Map<Long, WardRecord> map = new LinkedHashMap<>();
        for (WardRecord r : records) {
            map.put(r.id(), r);
        }
        return new WardCodec.Loaded(map, 10, new ArrayList<>(), Optional.empty());
    }

    @Test
    void recordsRoundTrip() {
        WardRecord bound = new WardRecord(3, "minecraft:overworld", 12345L, OWNER, 6, WardCatalog.Profile.UNDEAD, 11_000);
        WardRecord free = new WardRecord(4, "minecraft:the_nether", -9L, null, 8, WardCatalog.Profile.BOTH, 0);
        WardCodec.Loaded back = WardCodec.read(WardCodec.write(loaded(bound, free)));
        assertEquals(2, back.records().size());
        WardRecord r = back.records().get(3L);
        assertEquals(OWNER, r.owner().orElseThrow());
        assertEquals(6, r.radius());
        assertEquals(WardCatalog.Profile.UNDEAD, r.profile());
        assertEquals(11_000, r.charge());
        assertTrue(back.records().get(4L).owner().isEmpty());
        assertEquals(10, back.nextId());
    }

    @Test
    void anUnsupportedSchemaIsKeptAndDisablesEverything() {
        CompoundTag future = new CompoundTag();
        future.putInt("Schema", 99);
        future.putString("Whatever", "kept");
        WardCodec.Loaded read = WardCodec.read(future);
        assertTrue(read.unsupported().isPresent() && read.records().isEmpty());
        assertEquals(future, WardCodec.write(read), "written back unchanged");
    }

    @Test
    void malformedRecordsArePreservedNotUsed() {
        CompoundTag tag = WardCodec.write(loaded(new WardRecord(1, "minecraft:overworld", 1L, null, 8, WardCatalog.Profile.BOTH, 5)));
        CompoundTag broken = new CompoundTag();
        broken.putLong("Id", 2);
        broken.putString("Dim", "minecraft:overworld");
        tag.getList("Records", 10).add(broken);
        WardCodec.Loaded read = WardCodec.read(tag);
        assertEquals(1, read.records().size());
        assertEquals(1, read.preserved().size());
        CompoundTag again = WardCodec.write(read);
        assertEquals(1, again.getList("Preserved", 10).size(), "preserved records survive another save");
    }

    @Test
    void theRecordCapPreservesTheOverflow() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Schema", WardCodec.SCHEMA);
        ListTag list = new ListTag();
        for (int i = 1; i <= WardCatalog.MAX_RECORDS + 3; i++) {
            CompoundTag r = new CompoundTag();
            r.putLong("Id", i);
            r.putString("Dim", "minecraft:overworld");
            r.putLong("Pos", i);
            r.putInt("Radius", 8);
            r.putInt("Profile", 2);
            r.putInt("Charge", 0);
            list.add(r);
        }
        tag.put("Records", list);
        WardCodec.Loaded read = WardCodec.read(tag);
        assertEquals(WardCatalog.MAX_RECORDS, read.records().size());
        assertEquals(3, read.preserved().size());
    }
}
