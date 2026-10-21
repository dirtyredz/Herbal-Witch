package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.botany.showcase.GardenShowcaseSection;
import io.github.dirtyredz.herbalwitch.botany.showcase.HerbShowcaseSection;
import io.github.dirtyredz.herbalwitch.brewing.product.showcase.PreparationShowcaseSection;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.showcase.StationShowcaseSection;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plans the real sections without touching registries (states are resolved lazily): deterministic
 * order, disjoint cells, the documented size and write budgets, and refusal of oversize layouts.
 */
class ShowcasePlanTest {
    static List<ShowcaseSection> sections() {
        List<ShowcaseSection> all = new ArrayList<>(List.of(new DecorShowcaseSection(), new HerbShowcaseSection(), new GardenShowcaseSection()));
        for (StationSpec station : StationCatalog.ALL) {
            boolean heated = station == StationCatalog.KETTLE || station == StationCatalog.INFUSION_POT;
            all.add(new StationShowcaseSection(station, heated, "recipe", Map::of));
        }
        all.add(new PreparationShowcaseSection());
        return all;
    }

    @Test
    void sectionsStandSideBySideInRegistrationOrder() throws Exception {
        ShowcasePlan plan = ShowcasePlan.of(sections());
        List<ShowcasePlan.SectionArea> areas = plan.areas();
        assertEquals(List.of("decor", "herbs", "garden", "drying_rack", "mortar", "kettle", "hearth", "infusion_pot", "preparations"), areas.stream().map(ShowcasePlan.SectionArea::id).toList());
        for (int i = 1; i < areas.size(); i++) {
            ShowcasePlan.SectionArea previous = areas.get(i - 1);
            assertEquals(previous.x() + previous.width() + ShowcasePlan.GAP, areas.get(i).x());
        }
    }

    @Test
    void theFullLayoutStaysInsideItsBudget() throws Exception {
        ShowcasePlan plan = ShowcasePlan.of(sections());
        assertTrue(plan.width() <= ShowcasePlan.MAX_WIDTH && plan.depth() <= ShowcasePlan.MAX_DEPTH);
        assertTrue(plan.cellCount() < 32_768, "full layout uses " + plan.cellCount() + " cells");
        for (ShowcasePlacement p : plan.placements()) {
            assertTrue(p.x() >= 0 && p.x() < plan.width() && p.z() >= 0 && p.z() < plan.depth()
                    && p.y() >= 0 && p.y() < ShowcasePlan.HEIGHT, "outside the box: " + p);
        }
    }

    @Test
    void planningIsDeterministicAndOrderedByPhase() throws Exception {
        ShowcasePlan a = ShowcasePlan.of(sections());
        ShowcasePlan b = ShowcasePlan.of(sections());
        assertEquals(a.placements().size(), b.placements().size());
        for (int i = 0; i < a.placements().size(); i++) {
            ShowcasePlacement x = a.placements().get(i);
            ShowcasePlacement y = b.placements().get(i);
            assertEquals(List.of(x.kind(), x.x(), x.y(), x.z(), x.lines()), List.of(y.kind(), y.x(), y.y(), y.z(), y.lines()));
        }
        List<ShowcasePlacement> ps = a.placements();
        IntStream.range(1, ps.size()).forEach(i -> assertTrue(ps.get(i - 1).kind().compareTo(ps.get(i).kind()) <= 0));
    }

    @Test
    void oversizeAndOverlappingLayoutsAreRefusedBeforeAnyWrite() {
        ShowcaseSection wide = section("wide", List.of(ShowcasePlacement.content(ShowcasePlan.MAX_WIDTH, 1, 0, () -> null)));
        assertThrows(ShowcasePlan.InvalidPlanException.class, () -> ShowcasePlan.of(List.of(wide)));
        ShowcaseSection overlapping = section("overlap", List.of(ShowcasePlacement.content(1, 1, 1, () -> null),
                ShowcasePlacement.support(1, 1, 1, () -> null)));
        assertThrows(ShowcasePlan.InvalidPlanException.class, () -> ShowcasePlan.of(List.of(overlapping)));
        assertThrows(IllegalArgumentException.class, () -> ShowcasePlacement.content(0, 0, 0, () -> null));
    }

    static ShowcaseSection section(String id, List<ShowcasePlacement> placements) {
        return new ShowcaseSection() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public String titleKey() {
                return "x";
            }

            @Override
            public List<ShowcasePlacement> placements() {
                return placements;
            }
        };
    }
}
