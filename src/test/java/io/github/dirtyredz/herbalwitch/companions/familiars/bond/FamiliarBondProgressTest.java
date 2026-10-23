package io.github.dirtyredz.herbalwitch.companions.familiars.bond;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarRecord;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarBondProgressTest {
    private FamiliarRecord record() {
        var payload = new CompoundTag(); payload.putFloat("Health", 10);
        return FamiliarRecord.binding(UUID.randomUUID(), UUID.randomUUID(), "cat", 1, payload);
    }
    @Test void thresholdsAndDailyCapPersistWithoutRitualPenalty() {
        assertEquals(1, FamiliarBondProgress.level(39)); assertEquals(2, FamiliarBondProgress.level(40));
        assertEquals(3, FamiliarBondProgress.level(100)); assertEquals(4, FamiliarBondProgress.level(200)); assertEquals(5, FamiliarBondProgress.level(350));
        var r = record(); for (int i = 0; i < 20; i++) r = FamiliarBondProgress.award(r, 2, false);
        assertEquals(10, r.state().getInt("xp"));
        r = FamiliarBondProgress.award(r, 10, true); assertEquals(20, r.state().getInt("xp"));
        r = r.change(s -> { var p = s.getCompound("progress"); p.putLong("clock", 24000); s.put("progress", p); });
        r = FamiliarBondProgress.award(r, 2, false); assertEquals(22, r.state().getInt("xp"));
        assertEquals(2, r.state().getCompound("progress").getInt("earned"));
    }
    @Test void spiritbondAddsWithinCapAndMaximumLevelDoesNotOverflow() {
        var r = record().change(s -> { var buffs = new CompoundTag(); buffs.putInt("spiritbond", 3600); s.put("buffs", buffs); });
        for (int i = 0; i < 10; i++) r = FamiliarBondProgress.award(r, 2, false);
        assertEquals(10, r.state().getInt("xp"));
        r = r.change(s -> s.putInt("xp", 349)); r = FamiliarBondProgress.award(r, 10, true);
        assertEquals(350, r.state().getInt("xp")); assertSame(r, FamiliarBondProgress.award(r, 10, true));
    }
}
