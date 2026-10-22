package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.botany.showcase.GardenShowcaseSection;
import io.github.dirtyredz.herbalwitch.botany.showcase.HerbShowcaseSection;
import io.github.dirtyredz.herbalwitch.brewing.product.showcase.AdvancedPreparationShowcaseSection;
import io.github.dirtyredz.herbalwitch.brewing.product.showcase.PreparationShowcaseSection;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSection;
import io.github.dirtyredz.herbalwitch.grimoire.showcase.GrimoireShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.charm.showcase.CharmShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.AltarShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.CircleShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ward.showcase.WardShowcaseSection;
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
        // the stations that stand on a lit witch's stove, as StationRegistration registers them
        List<StationSpec> heated = List.of(StationCatalog.KETTLE, StationCatalog.INFUSION_POT, StationCatalog.CAULDRON, StationCatalog.STILL);
        for (StationSpec station : StationCatalog.ALL) {
            all.add(new StationShowcaseSection(station, heated.contains(station), "recipe", Map::of));
        }
        all.add(new PreparationShowcaseSection());
        all.add(new AdvancedPreparationShowcaseSection());
        all.add(new GrimoireShowcaseSection());
        all.add(new AltarShowcaseSection());
        all.add(new CircleShowcaseSection());
        all.add(new WardShowcaseSection());
        all.add(new CharmShowcaseSection());
        return all;
    }

    /**
     * Sections follow one another along x with a GAP aisle; a section that would pass ROW_WIDTH starts
     * a new row at x = 0, one GAP behind the deepest section of the row before.
     */
    @Test
    void sectionsStandSideBySideInRegistrationOrder() throws Exception {
        ShowcasePlan plan = ShowcasePlan.of(sections());
        List<ShowcasePlan.SectionArea> areas = plan.areas();
        assertEquals(List.of("decor", "herbs", "garden", "drying_rack", "mortar", "kettle", "hearth", "stove", "infusion_pot", "cauldron",
                "still", "bottling_bench", "charm_workbench", "preparations", "advanced_preparations", "grimoire", "altar", "ritual_circle",
                "ward_stone", "charms"), areas.stream().map(ShowcasePlan.SectionArea::id).toList());
        assertEquals(0, areas.get(0).x());
        assertEquals(0, areas.get(0).z());
        int rowZ = 0;
        int rowDepth = areas.get(0).depth();
        List<String> rowStarts = new ArrayList<>(List.of(areas.get(0).id()));
        for (int i = 1; i < areas.size(); i++) {
            ShowcasePlan.SectionArea previous = areas.get(i - 1);
            ShowcasePlan.SectionArea area = areas.get(i);
            if (area.x() == 0) {
                rowZ += rowDepth + ShowcasePlan.GAP;
                rowDepth = area.depth();
                rowStarts.add(area.id());
            } else {
                assertEquals(previous.x() + previous.width() + ShowcasePlan.GAP, area.x(), area.id() + " follows " + previous.id());
                rowDepth = Math.max(rowDepth, area.depth());
            }
            assertEquals(rowZ, area.z(), area.id() + " row");
            assertTrue(area.x() + area.width() <= ShowcasePlan.ROW_WIDTH, area.id() + " passes the row width");
        }
        // the first row fills up to the preparations; the Milestone 7 witch sections begin the second
        assertEquals(List.of("decor", "advanced_preparations"), rowStarts);
        // the herbs (27 rows of beds, 28 deep with their signs) are the deepest section of the first row
        assertEquals(28, areas.get(1).depth());
        assertEquals(28 + ShowcasePlan.GAP, areas.get(14).z());
    }

    @Test
    void theFullLayoutStaysInsideItsBudget() throws Exception {
        ShowcasePlan plan = ShowcasePlan.of(sections());
        assertTrue(plan.width() <= ShowcasePlan.MAX_WIDTH && plan.depth() <= ShowcasePlan.MAX_DEPTH);
        assertTrue(plan.cellCount() <= ShowcasePlan.MAX_CELLS, "full layout uses " + plan.cellCount() + " cells");
        for (ShowcasePlacement p : plan.placements()) {
            assertTrue(p.x() >= 0 && p.x() < plan.width() && p.z() >= 0 && p.z() < plan.depth()
                    && p.y() >= 0 && p.y() < ShowcasePlan.HEIGHT, "outside the box: " + p);
        }
    }

    /** The GameTest template (tools/generate-test-structures.mjs) is 128 x 10 x 64 and the box starts at (2, 0, 1). */
    @Test
    void theFullLayoutFitsTheGameTestTemplate() throws Exception {
        ShowcasePlan plan = ShowcasePlan.of(sections());
        assertTrue(plan.width() + 2 <= 128 && plan.depth() + 1 <= 64,
                "plan " + plan.width() + " x " + plan.depth() + " outgrows the 128 x 64 showcase template");
        // two rows: the first ends with the preparations at x = 121 (the herbs, 28 deep, set its depth); the second, 8 deep from z = 30
        // (the ritual circle), holds the witch sections: 30 + 8 = 38
        assertEquals(List.of(121, 38), List.of(plan.width(), plan.depth()), "the README documents the full layout as 121 x 8 x 38");
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
