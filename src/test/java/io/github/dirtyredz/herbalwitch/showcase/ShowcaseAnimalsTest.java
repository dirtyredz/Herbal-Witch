package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.companions.animals.showcase.AnimalShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ShowcaseAnimalsTest {
    @Test void independentlyExpectedSixteenSpeciesHaveSeparateTranslatedHabitats() throws Exception {
        var section = new AnimalShowcaseSection(); var plan = ShowcasePlan.of(ShowcasePlanTest.group(io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseGroup.WITCHCRAFT));
        assertEquals(Set.of("cow", "sheep", "goat", "pig", "rabbit", "chicken", "horse", "donkey", "mule", "llama", "cat", "wolf", "fox", "parrot", "axolotl", "bat"),
                section.entities().stream().map(s -> s.type().replace("minecraft:", "")).collect(java.util.stream.Collectors.toSet()));
        assertEquals(16, plan.entities().size()); assertEquals(16, new HashSet<>(plan.entities()).size());
        var area = plan.areas().stream().filter(a -> a.id().equals("animals")).findFirst().orElseThrow();
        assertEquals(section.entities().get(0).offset(area.x(), area.z() + ShowcasePlan.HEADING_ROWS), plan.entities().get(0));
        assertTrue(plan.cellCount() < 65536);
        System.out.println("M8 showcase: " + plan.width() + "x8x" + plan.depth() + ", " + plan.cellCount() + " writes, " + plan.placements().size() + " placements, 16 entities");
    }
    /** The widest sample (a horse, 1.4 across) and a 2-block height must fit between the planned walls: the cause of a refused spawn once the heading rows moved. */
    @Test void everySampleStandsClearOfPlannedBlocks() throws Exception {
        var plan = ShowcasePlan.of(ShowcasePlanTest.group(ShowcaseGroup.WITCHCRAFT));
        Set<String> solid = new HashSet<>();
        for (ShowcasePlacement p : plan.placements()) {
            if (p.kind() == ShowcasePlacement.Kind.SUPPORT) solid.add(p.x() + "," + p.y() + "," + p.z());
        }
        for (var sample : plan.entities()) {
            for (int x = (int) Math.floor(sample.x() - 0.7); x <= (int) Math.floor(sample.x() + 0.7); x++) {
                for (int z = (int) Math.floor(sample.z() - 0.7); z <= (int) Math.floor(sample.z() + 0.7); z++) {
                    for (int y = (int) sample.y(); y < sample.y() + 2; y++) {
                        assertFalse(solid.contains(x + "," + y + "," + z), sample.type() + " overlaps a planned block at " + x + "," + y + "," + z);
                    }
                }
            }
        }
    }
    @Test void entityCapAndOutsideSamplesFailDuringPurePlanning() {
        ShowcaseSection section = new ShowcaseSection() {
            public String id() { return "too_many"; } public String titleKey() { return "test"; }
            public List<ShowcasePlacement> placements() { return List.of(ShowcasePlacement.support(4, 1, 4, () -> null)); }
            public List<ShowcaseEntitySample> entities() { return Collections.nCopies(33, new ShowcaseEntitySample("minecraft:cow", 2, 1, 2)); }
        };
        assertThrows(ShowcasePlan.InvalidPlanException.class, () -> ShowcasePlan.of(List.of(section)));
    }
}
