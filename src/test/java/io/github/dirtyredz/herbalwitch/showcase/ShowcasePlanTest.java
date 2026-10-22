package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.apothecary.botany.showcase.GardenShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.botany.showcase.HerbShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.showcase.AdvancedPreparationShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.showcase.PreparationShowcaseSection;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSection;
import io.github.dirtyredz.herbalwitch.grimoire.showcase.GrimoireShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.charm.showcase.CharmShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.AltarShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.CircleShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ward.showcase.WardShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseSection;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.apothecary.stations.showcase.StationShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.command.ShowcaseCommand;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseGroup;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    static List<ShowcaseSection> group(ShowcaseGroup group) {
        return sections().stream().filter(s -> s.group() == group).toList();
    }

    private static List<String> ids(ShowcaseGroup group) {
        return group(group).stream().map(ShowcaseSection::id).toList();
    }

    /** Furniture on its own plot; everything else (herbs, stations, products, magic) on the other. */
    @Test
    void theFurnitureAndWitchcraftShowcasesPartitionTheSections() {
        assertEquals(List.of("decor"), ids(ShowcaseGroup.FURNITURE));
        List<String> witchcraft = ids(ShowcaseGroup.WITCHCRAFT);
        assertEquals(sections().size() - 1, witchcraft.size());
        assertFalse(witchcraft.contains("decor"));
        for (ShowcaseSection section : sections()) {
            assertTrue(ShowcaseRegistry.group(section.id()).isEmpty(), section.id() + " shares a group's command name");
        }
        assertEquals(Optional.of(ShowcaseGroup.FURNITURE), ShowcaseRegistry.group("furniture"));
        assertEquals(Optional.of(ShowcaseGroup.WITCHCRAFT), ShowcaseRegistry.group("witchcraft"));
        assertEquals(Optional.empty(), ShowcaseRegistry.group("decor"));
    }

    /**
     * Sections follow one another along x with a GAP aisle; a section that would pass ROW_WIDTH starts
     * a new row at x = 0, one GAP behind the deepest section of the row before.
     */
    @Test
    void witchcraftSectionsStandSideBySideInRegistrationOrder() throws Exception {
        ShowcasePlan plan = ShowcasePlan.of(group(ShowcaseGroup.WITCHCRAFT));
        List<ShowcasePlan.SectionArea> areas = plan.areas();
        assertEquals(List.of("herbs", "garden", "drying_rack", "mortar", "kettle", "hearth", "stove", "infusion_pot", "cauldron",
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
        assertEquals(List.of("herbs", "advanced_preparations"), rowStarts);
    }

    @Test
    void bothShowcasesStayInsideTheirBudget() throws Exception {
        for (ShowcaseGroup group : ShowcaseGroup.values()) {
            ShowcasePlan plan = ShowcasePlan.of(group(group));
            assertTrue(plan.width() <= ShowcasePlan.MAX_WIDTH && plan.depth() <= ShowcasePlan.MAX_DEPTH);
            assertTrue(plan.cellCount() <= ShowcasePlan.MAX_CELLS, group + " uses " + plan.cellCount() + " cells");
            for (ShowcasePlacement p : plan.placements()) {
                assertTrue(p.x() >= 0 && p.x() < plan.width() && p.z() >= 0 && p.z() < plan.depth()
                        && p.y() >= 0 && p.y() < ShowcasePlan.HEIGHT, group + " outside the box: " + p);
            }
        }
    }

    /** The GameTest template (tools/generate-test-structures.mjs) is 128 x 10 x 80 and the first box starts at (2, 0, 1). */
    @Test
    void eachShowcaseFitsTheGameTestTemplateAndMatchesTheDocumentedBounds() throws Exception {
        for (ShowcaseGroup group : ShowcaseGroup.values()) {
            ShowcasePlan plan = ShowcasePlan.of(group(group));
            assertTrue(plan.width() + 2 <= 128 && plan.depth() + 1 <= 80,
                    group + " " + plan.width() + " x " + plan.depth() + " outgrows the 128 x 80 showcase template");
        }
        // the bare command stacks the furniture showcase and the witchcraft showcase one separation apart
        assertTrue(28 + ShowcaseCommand.SEPARATION + 40 + 1 <= 80, "both showcases outgrow the template depth");
        assertEquals(List.of(41, 28), bounds(ShowcaseGroup.FURNITURE), "the README documents the furniture showcase as 41 x 8 x 28");
        assertEquals(List.of(119, 40), bounds(ShowcaseGroup.WITCHCRAFT), "the README documents the witchcraft showcase as 119 x 8 x 40");
    }

    private static List<Integer> bounds(ShowcaseGroup group) throws Exception {
        ShowcasePlan plan = ShowcasePlan.of(group(group));
        return List.of(plan.width(), plan.depth());
    }

    /**
     * The sign rule: no sign stands directly in front of (north of) a block or its support, at the
     * block's height or a block above, so a sign never hides what it names. The headings, the notes and
     * the block signs all follow it.
     */
    @Test
    void noSignStandsInFrontOfABlock() throws Exception {
        for (ShowcaseGroup group : ShowcaseGroup.values()) {
            ShowcasePlan plan = ShowcasePlan.of(group(group));
            Set<Long> blocks = new HashSet<>();
            for (ShowcasePlacement p : plan.placements()) {
                if (p.kind() == ShowcasePlacement.Kind.CONTENT || p.kind() == ShowcasePlacement.Kind.SUPPORT) {
                    blocks.add(p.cell());
                }
            }
            for (ShowcasePlacement p : plan.placements()) {
                if (p.kind() == ShowcasePlacement.Kind.LABEL) {
                    for (int dy = 0; dy <= 1; dy++) {
                        long behind = ((long) p.x() * 64 + p.y() + dy) * 65_536L + p.z() + 1;
                        assertFalse(blocks.contains(behind), group + " sign " + p.lines().get(0).text() + " at "
                                + p.x() + "," + p.y() + "," + p.z() + " stands in front of a block");
                    }
                }
            }
        }
    }

    /** Every sign for a block stands beside it: the cell to its east (the sign's viewer-right neighbour) is the block's, never open air with a block two away. */
    @Test
    void decorSignsStandBesideTheirPiece() {
        List<ShowcasePlacement> placements = new DecorShowcaseSection().placements();
        for (ShowcasePlacement label : placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.LABEL).toList()) {
            boolean beside = placements.stream().anyMatch(p -> p.kind() == ShowcasePlacement.Kind.CONTENT
                    && p.z() == label.z() && p.x() == label.x() + 1);
            assertTrue(beside, "no piece beside the sign at " + label.x() + "," + label.z());
        }
    }

    @Test
    void planningIsDeterministicAndOrderedByPhase() throws Exception {
        ShowcasePlan a = ShowcasePlan.of(group(ShowcaseGroup.WITCHCRAFT));
        ShowcasePlan b = ShowcasePlan.of(group(ShowcaseGroup.WITCHCRAFT));
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
