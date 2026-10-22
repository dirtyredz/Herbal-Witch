package io.github.dirtyredz.herbalwitch.decor.behavior.seating;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureAccess;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Sitting down, server side: the aimed seat of the structure (never another one), refused when it is
 * taken or has no headroom, never ejecting anyone; and the seat's lifecycle checks (the furniture is
 * still there, a safe place to stand up).
 */
public final class SeatingService {
    public static final String OCCUPIED = "message." + HerbalWitch.MOD_ID + ".seat.occupied";
    public static final String NO_ROOM = "message." + HerbalWitch.MOD_ID + ".seat.no_room";
    /** How close a seat entity must be to a seat to occupy it. */
    private static final double TAKEN = 0.25;

    private SeatingService() {
    }

    /** A player uses a seating block. */
    public static InteractionResult sit(SeatingBlock block, BlockState state, Level level, BlockPos pos, Player player, Vec3 aimed) {
        if (player.isPassenger() || player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos origin = block.origin(state, pos);
        if (!FurnitureAccess.mayUse(player, level, origin)) {
            return InteractionResult.FAIL;
        }
        FurnitureSpec spec = block.furniture();
        Direction facing = block.facing(state);
        List<Vec3> seats = SeatLayout.world(spec, origin, facing);
        int index = SeatLayout.nearest(seats, aimed);
        if (index < 0) {
            return InteractionResult.PASS;
        }
        Vec3 seat = seats.get(index);
        if (occupant(level, seat) != null) {
            player.displayClientMessage(Component.translatable(OCCUPIED), true);
            return InteractionResult.FAIL;
        }
        if (!headroom(level, seat, block, state, pos)) {
            player.displayClientMessage(Component.translatable(NO_ROOM), true);
            return InteractionResult.FAIL;
        }
        SeatEntity entity = SeatEntity.at(level, seat, origin, index);
        level.addFreshEntity(entity);
        if (!player.startRiding(entity, true)) {
            entity.discard();
            return InteractionResult.FAIL;
        }
        player.setYRot(facing.toYRot());
        player.setYHeadRot(facing.toYRot());
        return InteractionResult.CONSUME;
    }

    /** The seat entity at a seat position, if someone sits there. */
    public static SeatEntity occupant(Level level, Vec3 seat) {
        List<SeatEntity> found = level.getEntitiesOfClass(SeatEntity.class, new AABB(seat, seat).inflate(TAKEN), e -> e.isAlive() && !e.getPassengers().isEmpty());
        return found.isEmpty() ? null : found.get(0);
    }

    /** The block above the seat's own block must be open (or part of the same piece, a swing's frame). */
    private static boolean headroom(Level level, Vec3 seat, SeatingBlock block, BlockState state, BlockPos pos) {
        BlockPos seatBlock = BlockPos.containing(seat.x, seat.y - 0.01, seat.z);
        BlockPos above = seatBlock.above();
        if (!level.isLoaded(above)) {
            return false;
        }
        BlockState over = level.getBlockState(above);
        return over.is(block) && block.origin(over, above).equals(block.origin(state, pos)) || over.getCollisionShape(level, above).isEmpty();
    }

    /** Whether a seat entity still sits on its furniture (re-checked every tick on the server). */
    static boolean stillSeated(SeatEntity seat) {
        Level level = seat.level();
        BlockPos origin = seat.origin();
        if (!level.isLoaded(origin)) {
            return false;
        }
        BlockState state = level.getBlockState(origin);
        return state.getBlock() instanceof SeatingBlock block && block.isOrigin(state)
                && seat.seat() < block.furniture().seats();
    }

    /** A safe place to stand up: in front of the seat, then its sides, then behind; null when none. */
    static Vec3 exit(SeatEntity seat, LivingEntity passenger) {
        Level level = seat.level();
        BlockState state = level.isLoaded(seat.origin()) ? level.getBlockState(seat.origin()) : null;
        Direction front = state != null && state.getBlock() instanceof SeatingBlock block ? block.facing(state) : Direction.NORTH;
        BlockPos at = BlockPos.containing(seat.getX(), seat.getY() - 0.01, seat.getZ());
        for (Direction d : List.of(front, front.getClockWise(), front.getCounterClockWise(), front.getOpposite())) {
            Vec3 exit = DismountHelper.findSafeDismountLocation(passenger.getType(), level, at.relative(d), true);
            if (exit != null) {
                return exit;
            }
        }
        return null;
    }
}
