package io.github.dirtyredz.herbalwitch.showcase.layout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The complete, immutable layout of a showcase run, planned before any write. Sections stand side
 * by side along +x with a {@link #GAP}-block aisle, packed into rows no wider than {@link #ROW_WIDTH}
 * (a section that would not fit starts a new row behind the deepest section of the one before, a
 * {@link #GAP}-block aisle between rows); each gets a heading sign in its first row and its own
 * placements from the row after. The plan covers the whole box: one floor layer plus
 * {@link #HEIGHT}{@code - 1} layers cleared above it, and the placements. Bounded by
 * {@link #MAX_WIDTH} x {@link #HEIGHT} x {@link #MAX_DEPTH} and {@link #MAX_CELLS} writes; a plan
 * that would exceed them is refused before anything is written.
 */
public final class ShowcasePlan {
    public static final int HEIGHT = 8;
    public static final int MAX_WIDTH = 128;
    public static final int MAX_DEPTH = 128;
    public static final int MAX_CELLS = 65_536;
    public static final int MAX_ENTITIES = 32;
    public static final int GAP = 2;
    /** The widest a row may grow: the full width less the entrance aisle the showcase template keeps. */
    public static final int ROW_WIDTH = MAX_WIDTH - GAP;

    /** Where a section landed in the plan. */
    public record SectionArea(String id, int x, int z, int width, int depth, int placements) {
    }

    /** Thrown when a plan breaks a cap or two placements share a cell. */
    public static final class InvalidPlanException extends Exception {
        public InvalidPlanException(String message) {
            super(message);
        }
    }

    private final List<ShowcasePlacement> placements;
    private final List<ShowcaseEntitySample> entities;
    private final List<SectionArea> areas;
    private final int width;
    private final int depth;

    private ShowcasePlan(List<ShowcasePlacement> placements, List<ShowcaseEntitySample> entities, List<SectionArea> areas, int width, int depth) {
        this.placements = List.copyOf(placements);
        this.entities = List.copyOf(entities);
        this.areas = List.copyOf(areas);
        this.width = width;
        this.depth = depth;
    }

    public static ShowcasePlan of(List<ShowcaseSection> sections) throws InvalidPlanException {
        if (sections.isEmpty()) {
            throw new InvalidPlanException("no sections");
        }
        List<ShowcasePlacement> all = new ArrayList<>();
        List<ShowcaseEntitySample> entities = new ArrayList<>();
        List<SectionArea> areas = new ArrayList<>();
        int x = 0;
        int rowZ = 0;
        int rowDepth = 1;
        int width = 0;
        for (ShowcaseSection section : sections) {
            List<ShowcasePlacement> own = new ArrayList<>();
            own.add(ShowcasePlacement.label(0, 0, ShowcasePlacement.Line.translated(section.titleKey())));
            int sectionWidth = 1;
            int sectionDepth = 1;
            for (ShowcasePlacement p : section.placements()) {
                if (p.x() < 0 || p.z() < 0 || p.y() >= HEIGHT) {
                    throw new InvalidPlanException(section.id() + " places outside its area at " + p.x() + "," + p.y() + "," + p.z());
                }
                own.add(p.offset(0, 1));
                sectionWidth = Math.max(sectionWidth, p.x() + 1);
                sectionDepth = Math.max(sectionDepth, p.z() + 2);
            }
            var samples = section.entities();
            if (entities.size() + samples.size() > MAX_ENTITIES) throw new InvalidPlanException("more than 32 entity samples");
            for (var sample : samples) {
                if (sample.x() < 0 || sample.z() < 0 || sample.y() < 1 || sample.y() >= HEIGHT
                        || sample.x() >= sectionWidth || sample.z() + 1 >= sectionDepth) throw new InvalidPlanException("entity sample outside section");
            }
            if (x > 0 && x + sectionWidth > ROW_WIDTH) {
                rowZ += rowDepth + GAP;
                rowDepth = 1;
                x = 0;
            }
            for (ShowcasePlacement p : own) {
                all.add(p.offset(x, rowZ));
            }
            for (var sample : samples) entities.add(sample.offset(x, rowZ + 1));
            areas.add(new SectionArea(section.id(), x, rowZ, sectionWidth, sectionDepth, own.size()));
            rowDepth = Math.max(rowDepth, sectionDepth);
            width = Math.max(width, x + sectionWidth);
            x += sectionWidth + GAP;
        }
        ShowcasePlan plan = new ShowcasePlan(sort(all), entities, areas, width, rowZ + rowDepth);
        plan.validate();
        return plan;
    }

    /** Floor, supports, content, labels; within a phase bottom-up, so supports exist before what they hold. */
    private static List<ShowcasePlacement> sort(List<ShowcasePlacement> placements) {
        List<ShowcasePlacement> sorted = new ArrayList<>(placements);
        sorted.sort(Comparator.comparing(ShowcasePlacement::kind).thenComparingInt(ShowcasePlacement::y));
        return sorted;
    }

    private void validate() throws InvalidPlanException {
        if (width > MAX_WIDTH || depth > MAX_DEPTH) {
            throw new InvalidPlanException("layout " + width + "x" + HEIGHT + "x" + depth + " exceeds "
                    + MAX_WIDTH + "x" + HEIGHT + "x" + MAX_DEPTH);
        }
        if (cellCount() > MAX_CELLS) {
            throw new InvalidPlanException("layout needs " + cellCount() + " writes, more than " + MAX_CELLS);
        }
        Set<Long> used = new HashSet<>();
        for (ShowcasePlacement p : placements) {
            if (!used.add(p.cell())) {
                throw new InvalidPlanException("two placements share " + p.x() + "," + p.y() + "," + p.z());
            }
        }
    }

    /** Every placement, in write order. */
    public List<ShowcasePlacement> placements() {
        return placements;
    }

    public List<SectionArea> areas() {
        return areas;
    }
    public List<ShowcaseEntitySample> entities() { return entities; }

    public int width() {
        return width;
    }

    public int depth() {
        return depth;
    }

    /** Floor and cleared cells of the whole box, plus every placement. */
    public int cellCount() {
        return width * depth * HEIGHT + placements.size();
    }
}
