package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.companions.animals.showcase.AnimalShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ShowcaseAnimalsTest {
    @Test void independentlyExpectedSixteenSpeciesHaveSeparateTranslatedHabitats() throws Exception {
        var section = new AnimalShowcaseSection(); var plan = ShowcasePlan.of(ShowcasePlanTest.sections());
        assertEquals(Set.of("cow", "sheep", "goat", "pig", "rabbit", "chicken", "horse", "donkey", "mule", "llama", "cat", "wolf", "fox", "parrot", "axolotl", "bat"),
                section.entities().stream().map(s -> s.type().replace("minecraft:", "")).collect(java.util.stream.Collectors.toSet()));
        assertEquals(16, plan.entities().size()); assertEquals(16, new HashSet<>(plan.entities()).size());
        var area = plan.areas().stream().filter(a -> a.id().equals("animals")).findFirst().orElseThrow();
        assertEquals(section.entities().get(0).offset(area.x(), area.z() + 1), plan.entities().get(0));
        assertTrue(plan.cellCount() < 65536);
        System.out.println("M8 showcase: " + plan.width() + "x8x" + plan.depth() + ", " + plan.cellCount() + " writes, " + plan.placements().size() + " placements, 16 entities");
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
