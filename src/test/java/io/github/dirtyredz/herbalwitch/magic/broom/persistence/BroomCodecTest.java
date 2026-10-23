package io.github.dirtyredz.herbalwitch.magic.broom.persistence;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BroomCodecTest {
    @Test void conditionRoundTripsAndRepairPreservesAccumulators() {
        var data = new BroomItemData(550, 199, 19);
        assertEquals(data, BroomItemData.decode(data.encode()).orElseThrow());
        assertEquals(new BroomItemData(350, 199, 19), data.repair(200));
        assertEquals(50, data.condition());
        assertEquals(new BroomItemData(0, 199, 19), data.repair(600));
        assertEquals(550, data.wear());
    }

    @Test void malformedAndFutureDataNeverBecomesFresh() {
        assertTrue(BroomItemData.decode(new CompoundTag()).isEmpty());
        for (String field : new String[] {"schema", "wear", "wearTicks", "energyRemainder"}) {
            CompoundTag missing = BroomItemData.FRESH.encode();
            missing.remove(field);
            assertTrue(BroomItemData.decode(missing).isEmpty());
            missing.putString(field, "0");
            assertTrue(BroomItemData.decode(missing).isEmpty());
        }
        for (int[] values : new int[][] {{2, 0, 0, 0}, {1, -1, 0, 0}, {1, 601, 0, 0}, {1, 0, 200, 0}, {1, 0, 0, 20}}) {
            CompoundTag tag = new CompoundTag();
            tag.putInt("schema", values[0]); tag.putInt("wear", values[1]);
            tag.putInt("wearTicks", values[2]); tag.putInt("energyRemainder", values[3]);
            CompoundTag before = tag.copy();
            assertTrue(BroomItemData.decode(tag).isEmpty());
            assertEquals(before, tag);
        }
    }
}
