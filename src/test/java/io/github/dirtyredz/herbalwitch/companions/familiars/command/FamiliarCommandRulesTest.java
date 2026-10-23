package io.github.dirtyredz.herbalwitch.companions.familiars.command;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.config.FamiliarRules;
import io.github.dirtyredz.herbalwitch.companions.familiars.work.FamiliarWorkQueue;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarCommandRulesTest {
    private static FamiliarRecord mobile(UUID owner, long order) {
        var state = new CompoundTag(); state.putString("command", "follow"); state.putLong("mobileOrder", order);
        return new FamiliarRecord(UUID.randomUUID(), owner, UUID.randomUUID(), "cat", "minecraft:cat", 1, FamiliarRecord.Phase.ACTIVE,
                UUID.randomUUID(), 1, state, new CompoundTag());
    }
    @Test void reservationsCountUnloadedBondsAndRetainOldestAcrossCommands() {
        var index = new FamiliarMobileIndex(); UUID owner = UUID.randomUUID();
        var first = mobile(owner, 1); var second = mobile(owner, 2); var third = mobile(owner, 3);
        index.changed(third); index.changed(first); index.changed(second);
        assertEquals(List.of(first.bond(), second.bond(), third.bond()), index.assignments(owner));
        index.changed(first.change(s -> s.putString("command", "scout")));
        assertEquals(3, index.count(owner)); assertEquals(first.bond(), index.assignments(owner).get(0));
        index.changed(second.change(s -> { s.putString("command", "stay"); s.remove("mobileOrder"); }));
        assertEquals(List.of(first.bond(), third.bond()), index.assignments(owner)); assertEquals(4, index.nextOrder());
        index.changed(first.quarantine()); assertEquals(1, index.count(owner));
    }
    @Test void workBudgetIsFairAndDuplicateRequestsCannotIncreaseItsShare() {
        var queue = new FamiliarWorkQueue<Integer>(); for (int i = 0; i < 100; i++) { queue.offer(i); queue.offer(i); }
        List<Integer> served = new ArrayList<>();
        for (int tick = 0; tick < 25; tick++) assertEquals(4, queue.run(4, value -> { served.add(value); queue.offer(value); }));
        assertEquals(100, new HashSet<>(served).size()); assertEquals(100, queue.size());
        queue.remove(0); assertEquals(99, queue.size());
    }
    @Test void commandsUsePermanentNamesAndOneSharedMobileClass() {
        assertEquals(Set.of("follow", "stay", "guard_witch", "protect_home", "scout", "passive", "return_home"),
                Arrays.stream(FamiliarCommand.values()).map(FamiliarCommand::key).collect(java.util.stream.Collectors.toSet()));
        assertEquals(Set.of(FamiliarCommand.FOLLOW, FamiliarCommand.GUARD_WITCH, FamiliarCommand.SCOUT),
                Arrays.stream(FamiliarCommand.values()).filter(FamiliarCommand::mobile).collect(java.util.stream.Collectors.toSet()));
        assertThrows(IllegalArgumentException.class, () -> FamiliarCommand.parse("FOLLOW"));
    }
    @Test void configurationCannotRaiseHardCapsOrNameUnknownSpecies() {
        var d = FamiliarRules.DEFAULTS; assertEquals(3, d.mobile()); assertEquals(4, d.paths()); assertEquals(8, d.scans());
        assertThrows(IllegalArgumentException.class, () -> new FamiliarRules(true, d.species(), 9, d.recordBytes(), 5, 8, 4, 16, 32, 8, false));
        assertThrows(IllegalArgumentException.class, () -> new FamiliarRules(true, d.species(), 3, 131073, 5, 8, 4, 16, 32, 8, false));
        assertThrows(IllegalArgumentException.class, () -> new FamiliarRules(true, Set.of("dragon"), 3, d.recordBytes(), 5, 8, 4, 16, 32, 8, false));
        assertThrows(IllegalArgumentException.class, () -> new FamiliarRules(true, d.species(), 3, d.recordBytes(), 4, 8, 4, 16, 32, 8, false));
    }
}
