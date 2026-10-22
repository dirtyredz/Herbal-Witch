package io.github.dirtyredz.herbalwitch.decor.behavior.seating;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.platform.geometry.ShapeRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Where a seating piece's seats are: in the structure's north-facing pixels (cell 0 at x 0..16, a
 * step to the viewer's right at x - 16, back at z + 16, as the Blockbench projects are drawn), each
 * seat's centre on the cushion. By default {@link FurnitureSpec#seats()} seats are spread evenly
 * along the piece's width at the cushion height {@link #SEAT_Y}; a piece whose cushion is elsewhere
 * says so here. The sitter faces the piece's front. Pure geometry: unit-tested without a world.
 */
public final class SeatLayout {
    /** Cushion top of the set's chairs, benches and sofas, in pixels. */
    public static final double SEAT_Y = 8;

    /** One seat: centre on the cushion, in north-facing structure pixels. */
    public record Seat(double x, double y, double z) {
    }

    /** Pieces whose seats are not the default row. */
    private static final Map<String, List<Seat>> OVERRIDES = Map.of(
            "armchair", List.of(new Seat(8, 7, 7)),
            "chaise_lounge", List.of(new Seat(8, 8, 10)),
            "garden_swing", List.of(new Seat(-2, 7, 16), new Seat(-14, 7, 16)));

    private SeatLayout() {
    }

    /** The seats of a piece in seat order: seat 0 is the one over the origin cell. */
    public static List<Seat> seats(FurnitureSpec spec) {
        List<Seat> override = OVERRIDES.get(spec.id());
        if (override != null) {
            return override;
        }
        int width = spec.mount().size()[0];
        int count = spec.seats();
        List<Seat> out = new ArrayList<>();
        double span = 16.0 * width;
        for (int i = 0; i < count; i++) {
            double x = 16 - span * (i + 0.5) / count; // from cell 0 toward the viewer's right
            out.add(new Seat(x, SEAT_Y, 7));
        }
        return out;
    }

    /** A seat's world position for a structure with its origin at {@code origin}, facing {@code facing}. */
    public static Vec3 world(Seat seat, BlockPos origin, Direction facing) {
        double[] turned = ShapeRotation.rotate(new double[] {seat.x(), seat.y(), seat.z(), seat.x(), seat.y(), seat.z()},
                ShapeRotation.quarterTurns(facing));
        return new Vec3(origin.getX() + turned[0] / 16.0, origin.getY() + seat.y() / 16.0, origin.getZ() + turned[2] / 16.0);
    }

    /** Every seat of a structure in world space, in seat order. */
    public static List<Vec3> world(FurnitureSpec spec, BlockPos origin, Direction facing) {
        return seats(spec).stream().map(s -> world(s, origin, facing)).toList();
    }

    /** The seat nearest an aimed point, or -1 when the piece has none. */
    public static int nearest(List<Vec3> seats, Vec3 aimed) {
        int best = -1;
        double bestDistance = Double.MAX_VALUE;
        for (int i = 0; i < seats.size(); i++) {
            double dx = seats.get(i).x - aimed.x;
            double dz = seats.get(i).z - aimed.z;
            double d = dx * dx + dz * dz;
            if (d < bestDistance - 1e-9) {
                bestDistance = d;
                best = i;
            }
        }
        return best;
    }
}
