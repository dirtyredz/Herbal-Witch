package io.github.dirtyredz.herbalwitch.companions.animals.capture;

import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.*;
import io.github.dirtyredz.herbalwitch.companions.animals.capture.data.CaptureRecord.Phase;
import io.github.dirtyredz.herbalwitch.companions.animals.config.AnimalRules;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CaptureStateTest {
    static CaptureRecord prepared(int id) {
        CompoundTag payload = new CompoundTag(); payload.putString("retained", "inventory and care");
        return new CaptureRecord(new UUID(0, id), 1, new UUID(1, id), Phase.CAPTURE_PREPARED, new UUID(2, 1), new UUID(3, id),
                "minecraft:donkey", 1, "Donkey", "minecraft:overworld 0, 64, 0", "", payload);
    }
    @Test void legalLifecycleRetainsPayloadUntilVerifiedReleaseAndPermanentCompaction() {
        var prepared = prepared(1); var stored = prepared.phase(Phase.STORED, "");
        assertThrows(IllegalStateException.class, () -> prepared.phase(Phase.RELEASED, ""));
        assertThrows(IllegalStateException.class, stored::compact);
        var released = stored.phase(Phase.RELEASE_PREPARED, "destination").phase(Phase.RELEASED, "destination");
        assertTrue(released.storedPayload()); assertNull(released.token());
        var compact = released.compact(); assertFalse(compact.storedPayload()); assertEquals(1, compact.generation());
        assertThrows(IllegalStateException.class, () -> compact.recover(UUID.randomUUID()));
    }
    @Test void reissueInvalidatesCopiesAndRecoveryAdvancesGenerationWithoutTransfer() {
        var stored = prepared(1).phase(Phase.STORED, ""); var replacement = stored.reissue(new UUID(4, 1));
        assertNotEquals(stored.token(), replacement.token()); assertEquals(stored.generation(), replacement.generation());
        var recovered = stored.phase(Phase.RELEASE_PREPARED, "x").phase(Phase.QUARANTINED, "x").recover(new UUID(5, 1));
        assertEquals(2, recovered.generation()); assertEquals(stored.captor(), recovered.captor()); assertEquals(stored.payload(), recovered.payload());
    }
    @Test void codecPreservesAllRecordsAndRejectsTrailingBytesAndOversize() throws Exception {
        var record = prepared(1); byte[] bytes = CaptureCodec.encode(List.of(record), 32768);
        assertEquals(record, CaptureCodec.decode(bytes).get(record.animal()));
        assertThrows(Exception.class, () -> CaptureCodec.decode(Arrays.copyOf(bytes, bytes.length + 1)));
        assertThrows(Exception.class, () -> CaptureCodec.encode(List.of(record), 10));
        var changed = record.payload(); changed.putString("retained", "external edit"); assertNotEquals(changed, record.payload());
    }
    @Test void storageCountsQuarantineAndNeverEvictsLifetimeTombstones() {
        List<CaptureRecord> records = new ArrayList<>();
        for (int i = 1; i <= 16; i++) records.add(prepared(i).phase(Phase.QUARANTINED, ""));
        assertFalse(CaptureCodec.fits(records, new UUID(0, 99), new UUID(2, 1), AnimalRules.DEFAULTS));
        assertTrue(CaptureCodec.fits(records, new UUID(0, 99), new UUID(2, 2), AnimalRules.DEFAULTS));
        records.clear();
        for (int i = 1; i <= 4096; i++) records.add(prepared(i).phase(Phase.STORED, "").phase(Phase.RELEASE_PREPARED, "").phase(Phase.RELEASED, "").compact());
        assertFalse(CaptureCodec.fits(records, new UUID(0, 5000), new UUID(2, 1), AnimalRules.DEFAULTS));
        assertTrue(CaptureCodec.fits(records, new UUID(0, 1), new UUID(2, 1), AnimalRules.DEFAULTS));
    }
}
