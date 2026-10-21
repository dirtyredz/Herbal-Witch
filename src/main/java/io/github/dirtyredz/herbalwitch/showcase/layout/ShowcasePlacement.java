package io.github.dirtyredz.herbalwitch.showcase.layout;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * One planned write of a showcase section, in section-local coordinates: {@code y = 0} is the floor
 * layer, content stands at {@code y >= 1}. A placement only describes the world change; the builder
 * performs it. States are resolved lazily so a layout can be planned (and unit-tested) without
 * touching registries.
 *
 * @param kind  write phase: floor, then supports, then content, then labels
 * @param state the block to place (a sign's state for labels)
 * @param init  optional block entity setup after placement (sample inventories, lit fuel), or null
 * @param lines sign text for labels; empty otherwise
 */
public record ShowcasePlacement(Kind kind, int x, int y, int z, Supplier<BlockState> state,
                               Consumer<BlockEntity> init, List<Line> lines) {
    /** Write phases, in order. */
    public enum Kind {
        FLOOR,
        SUPPORT,
        CONTENT,
        LABEL
    }

    /** One sign line: a translation key with arguments, or literal text when {@code literal}. */
    public record Line(String text, List<Object> args, boolean literal) {
        public Line {
            args = List.copyOf(args);
        }

        public static Line translated(String key, Object... args) {
            return new Line(key, List.of(args), false);
        }

        public static Line literal(String text) {
            return new Line(text, List.of(), true);
        }
    }

    public ShowcasePlacement {
        lines = List.copyOf(lines);
        if (kind == Kind.FLOOR ? y != 0 : y < 1) {
            throw new IllegalArgumentException(kind + " placement at y " + y);
        }
    }

    public static ShowcasePlacement floor(int x, int z, Supplier<BlockState> state) {
        return new ShowcasePlacement(Kind.FLOOR, x, 0, z, state, null, List.of());
    }

    /** A rectangle of floor cells: {@code width} along x and {@code depth} along z from (x, z). */
    public static List<ShowcasePlacement> floorArea(int x, int z, int width, int depth, Supplier<BlockState> state) {
        List<ShowcasePlacement> cells = new ArrayList<>();
        for (int dx = 0; dx < width; dx++) {
            for (int dz = 0; dz < depth; dz++) {
                cells.add(floor(x + dx, z + dz, state));
            }
        }
        return cells;
    }

    public static ShowcasePlacement support(int x, int y, int z, Supplier<BlockState> state) {
        return new ShowcasePlacement(Kind.SUPPORT, x, y, z, state, null, List.of());
    }

    public static ShowcasePlacement content(int x, int y, int z, Supplier<BlockState> state) {
        return new ShowcasePlacement(Kind.CONTENT, x, y, z, state, null, List.of());
    }

    public static ShowcasePlacement content(int x, int y, int z, Supplier<BlockState> state, Consumer<BlockEntity> init) {
        return new ShowcasePlacement(Kind.CONTENT, x, y, z, state, init, List.of());
    }

    /** A standing sign at floor level, readable from the north (the showcase entrance side). */
    public static ShowcasePlacement label(int x, int z, Line... lines) {
        if (lines.length == 0 || lines.length > 4) {
            throw new IllegalArgumentException("a sign holds 1 to 4 lines");
        }
        return new ShowcasePlacement(Kind.LABEL, x, 1, z, null, null, List.of(lines));
    }

    /** The same placement moved by an offset. */
    public ShowcasePlacement offset(int dx, int dz) {
        return new ShowcasePlacement(kind, x + dx, y, z + dz, state, init, lines);
    }

    /** Cell identity, for overlap checks (unique for coordinates within the plan's caps). */
    public long cell() {
        return ((long) x * 64 + y) * 65_536L + z;
    }
}
