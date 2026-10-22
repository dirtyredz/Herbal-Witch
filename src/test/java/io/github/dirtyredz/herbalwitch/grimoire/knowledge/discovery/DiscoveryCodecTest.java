package io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The saved form: deterministic schema-1 output, migration of players without grimoire data,
 * retention of unknown keys, and never overwriting data this version cannot read.
 */
class DiscoveryCodecTest {
    private static final EntryKey HERB = EntryKey.of(EntryKey.Kind.HERB, "herbalwitch:botany/wet/mint/plant");
    private static final EntryKey TEA = EntryKey.of(EntryKey.Kind.RECIPE, "herbalwitch:processing/steeping/recovery_tea");

    private static CompoundTag saved(int schema, String... keys) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(DiscoveryCodec.SCHEMA_KEY, schema);
        ListTag list = new ListTag();
        for (String k : keys) {
            list.add(StringTag.valueOf(k));
        }
        tag.put(DiscoveryCodec.KEYS, list);
        return tag;
    }

    @Test
    void writingIsDeterministicAndRoundTrips() {
        DiscoveryState a = new DiscoveryState();
        a.grant(TEA);
        a.grant(HERB);
        DiscoveryState b = new DiscoveryState();
        b.grant(HERB);
        b.grant(TEA);
        assertEquals(DiscoveryCodec.write(a), DiscoveryCodec.write(b), "grant order never changes the save");
        CompoundTag tag = DiscoveryCodec.write(a);
        assertEquals(1, tag.getInt(DiscoveryCodec.SCHEMA_KEY));
        DiscoveryCodec.Loaded back = DiscoveryCodec.read(tag);
        assertEquals(Set.of(HERB, TEA), back.state().known());
        assertTrue(back.preserved().isEmpty());
    }

    @Test
    void aPlayerFromBeforeTheGrimoireMigratesToAnEmptySchemaOneState() {
        for (CompoundTag legacy : new CompoundTag[] {null, new CompoundTag()}) {
            DiscoveryCodec.Loaded loaded = DiscoveryCodec.read(legacy);
            assertEquals(0, loaded.state().size());
            assertFalse(loaded.state().isReadOnly());
            assertEquals(saved(1), DiscoveryCodec.write(loaded));
        }
    }

    @Test
    void unknownKindsAndDuplicatesAreKeptOnceAndWrittenBack() {
        CompoundTag tag = saved(1, TEA.serialized(), "spell|futuremod:moonlight", TEA.serialized());
        DiscoveryCodec.Loaded loaded = DiscoveryCodec.read(tag);
        assertEquals(Set.of(TEA), loaded.state().known());
        assertEquals(Set.of("spell|futuremod:moonlight"), loaded.state().retained());
        ListTag written = DiscoveryCodec.write(loaded).getList(DiscoveryCodec.KEYS, 8);
        assertEquals(List.of(TEA.serialized(), "spell|futuremod:moonlight"),
                List.of(written.getString(0), written.getString(1)), "sorted, unknown key preserved");
    }

    @Test
    void anUnsupportedSchemaIsPreservedUntouchedAndLocksGrants() {
        CompoundTag future = saved(2, TEA.serialized());
        future.putString("extra", "from a newer version");
        DiscoveryCodec.Loaded loaded = DiscoveryCodec.read(future);
        assertTrue(loaded.problem().isPresent());
        assertTrue(loaded.state().isReadOnly());
        assertEquals(DiscoveryState.Grant.READ_ONLY, loaded.state().grant(HERB));
        assertEquals(future, DiscoveryCodec.write(loaded), "written back byte for byte");
    }

    @Test
    void malformedSavesArePreservedRatherThanGuessed() {
        CompoundTag noSchema = new CompoundTag();
        noSchema.put(DiscoveryCodec.KEYS, new ListTag());
        CompoundTag notAList = new CompoundTag();
        notAList.putInt(DiscoveryCodec.SCHEMA_KEY, 1);
        notAList.putString(DiscoveryCodec.KEYS, "recipe|herbalwitch:x");
        CompoundTag numbers = new CompoundTag();
        numbers.putInt(DiscoveryCodec.SCHEMA_KEY, 1);
        ListTag ints = new ListTag();
        ints.add(IntTag.valueOf(3));
        numbers.put(DiscoveryCodec.KEYS, ints);
        for (CompoundTag bad : List.of(noSchema, notAList, numbers)) {
            DiscoveryCodec.Loaded loaded = DiscoveryCodec.read(bad);
            assertTrue(loaded.problem().isPresent(), bad.toString());
            assertEquals(bad, DiscoveryCodec.write(loaded), bad.toString());
        }
    }

    @Test
    void moreKeysThanTheCeilingAreRefusedNotTruncated() {
        String[] keys = new String[DiscoveryState.MAX_DISCOVERIES + 1];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "recipe|test:r" + i;
        }
        CompoundTag tag = saved(1, keys);
        DiscoveryCodec.Loaded loaded = DiscoveryCodec.read(tag);
        assertTrue(loaded.state().isReadOnly());
        assertEquals(tag, DiscoveryCodec.write(loaded));
    }
}
