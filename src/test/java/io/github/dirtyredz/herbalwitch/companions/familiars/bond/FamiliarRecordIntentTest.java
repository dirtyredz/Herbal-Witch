package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** M12: the copy-free reads and the intent comparison the store uses every tick. */
class FamiliarRecordIntentTest {
    private static FamiliarRecord record() {
        CompoundTag state = new CompoundTag();
        state.putString("command", "follow");
        state.putLong("anchor", 7);
        CompoundTag buffs = new CompoundTag(); buffs.putInt("swiftpaw", 40); state.put("buffs", buffs);
        return new FamiliarRecord(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "wolf", "minecraft:wolf", 1,
                FamiliarRecord.Phase.ACTIVE, UUID.randomUUID(), 1, state, new CompoundTag());
    }

    @Test
    void countersAndBuffTimersDoNotChangeIntent() {
        var r = record();
        var ticked = r.change(s -> {
            var p = s.getCompound("progress"); p.putLong("clock", 99); s.put("progress", p);
            s.putInt("homeRest", 3); s.putInt("charmTicks", 5);
            s.getCompound("buffs").putInt("swiftpaw", 39);
        });
        assertTrue(r.sameIntent(ticked));
    }

    @Test
    void commandsAnchorsAndNewBuffsDo() {
        var r = record();
        assertFalse(r.sameIntent(r.change(s -> s.putString("command", "stay"))));
        assertFalse(r.sameIntent(r.change(s -> s.putLong("anchor", 8))));
        assertFalse(r.sameIntent(r.change(s -> s.getCompound("buffs").putInt("moonveil", 10))));
        assertFalse(r.sameIntent(r.change(s -> s.remove("buffs"))));
    }

    @Test
    void readsNeverExposeTheStoredState() {
        var r = record();
        r.compound("buffs").putInt("swiftpaw", 0);
        assertEquals(40, r.compound("buffs").getInt("swiftpaw"));
        assertTrue(r.has("anchor") && r.longValue("anchor") == 7 && r.stringValue("command").equals("follow"));
    }
}
