package io.github.dirtyredz.herbalwitch.companions.familiars.home;

import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.showcase.FamiliarHomeShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.*;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class FamiliarHomeRulesTest {
    @Test void homeCompatibilityAndSingleOccupancySurviveUnloadedIncapacitatedBonds() {
        assertTrue(FamiliarHomeKind.BED.accepts("snake")); assertFalse(FamiliarHomeKind.BED.accepts("raven"));
        assertTrue(FamiliarHomeKind.NEST.accepts("owl")); assertFalse(FamiliarHomeKind.NEST.accepts("bat")); assertTrue(FamiliarHomeKind.PERCH.accepts("bat"));
        UUID instance = UUID.randomUUID(); var home = new CompoundTag(); home.putUUID("instance", instance);
        var state = new CompoundTag(); state.put("home", home); state.putString("command", "stay");
        var payload = new CompoundTag(); payload.putString("id", "minecraft:cat");
        var a = new FamiliarRecord(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "cat", "minecraft:cat", 1, FamiliarRecord.Phase.INCAPACITATED, UUID.randomUUID(), 1, state, payload);
        var b = new FamiliarRecord(UUID.randomUUID(), a.owner(), UUID.randomUUID(), "cat", "minecraft:cat", 1, FamiliarRecord.Phase.ACTIVE, UUID.randomUUID(), 1, state, new CompoundTag());
        var index = new FamiliarHomeIndex(); assertTrue(index.changed(a)); assertFalse(index.changed(b)); assertEquals(a.bond(), index.occupant(instance).orElseThrow());
        assertTrue(index.changed(a.change(s -> s.remove("home")))); assertTrue(index.changed(b)); assertEquals(b.bond(), index.occupant(instance).orElseThrow());
    }
    @Test void homeShowcaseUsesWitchcraftAndASeparateSignBesideEachBlock() {
        var section = new FamiliarHomeShowcaseSection(); assertEquals(ShowcaseGroup.WITCHCRAFT, section.group());
        var placements = section.placements(); assertEquals(6, placements.size());
        var blocks = placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.CONTENT).toList(); assertEquals(3, blocks.size());
        for (var block : blocks) assertTrue(placements.stream().anyMatch(p -> p.kind() == ShowcasePlacement.Kind.LABEL && p.x() == block.x() + 2 && p.y() == block.y() && p.z() == block.z()));
    }
}
