package io.github.dirtyredz.herbalwitch.companions.familiars.preparation;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarBuffRulesTest {
    @Test void allTenTimersAdvanceOnlyWhenCalledAndCostsNeverGenerateEnergy() {
        var state = new CompoundTag(); var buffs = new CompoundTag();
        for (var key : FamiliarBuffRules.PRODUCTS) buffs.putInt(key, 3600);
        assertEquals(10, buffs.getAllKeys().size()); state.put("buffs", buffs);
        assertEquals(240, FamiliarBuffRules.cost(state, 3)); assertEquals(100, FamiliarBuffRules.cost(state, 1));
        var next = FamiliarBuffRules.advance(buffs); assertEquals(3600, buffs.getInt("guardian")); assertEquals(3599, next.getInt("guardian"));
        for (int i = 1; i < 3600; i++) next = FamiliarBuffRules.advance(next);
        assertTrue(next.isEmpty()); state.put("buffs", next); assertEquals(300, FamiliarBuffRules.cost(state, 3));
    }
}
