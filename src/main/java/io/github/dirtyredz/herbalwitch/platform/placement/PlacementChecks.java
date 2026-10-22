package io.github.dirtyredz.herbalwitch.platform.placement;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Locale;
import java.util.Optional;

/**
 * World validation of a resolved plan, every cell: loaded (never loading a chunk), inside the world
 * border and build height, replaceable, clear of entities, supported, and (on the server, with a
 * player) permitted by the shared authorization boundary. The first problem is returned with the
 * cell it concerns; nothing is written. The client preview runs the same checks except permission,
 * which only the server can know.
 */
public final class PlacementChecks {
    public static final String KEY = "message." + HerbalWitch.MOD_ID + ".placement.";

    /** Why a plan was refused. */
    public enum Problem {
        UNLOADED,
        OUTSIDE_WORLD,
        OCCUPIED,
        OBSTRUCTED,
        UNSUPPORTED,
        DENIED;

        public Component message() {
            return Component.translatable(KEY + name().toLowerCase(Locale.ROOT));
        }
    }

    /** A refusal and the cell it concerns. */
    public record Refusal(Problem problem, BlockPos pos) {
    }

    private PlacementChecks() {
    }

    /** Every check including permission (server side, before committing). */
    public static Optional<Refusal> check(BlockPlaceContext context, PlacementPlan plan, StructurePlacement structure) {
        return check(context, plan, structure, true);
    }

    /** Every check but permission (the client preview). */
    public static Optional<Refusal> checkGeometry(BlockPlaceContext context, PlacementPlan plan, StructurePlacement structure) {
        return check(context, plan, structure, false);
    }

    private static Optional<Refusal> check(BlockPlaceContext context, PlacementPlan plan, StructurePlacement structure, boolean permission) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        CollisionContext collision = player == null ? CollisionContext.empty() : CollisionContext.of(player);
        for (PlacementPlan.Cell cell : plan.cells()) {
            BlockPos pos = cell.pos();
            if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
                return refuse(Problem.OUTSIDE_WORLD, pos);
            }
            if (!level.isLoaded(pos)) {
                return refuse(Problem.UNLOADED, pos);
            }
            if (!level.getBlockState(pos).canBeReplaced(BlockPlaceContext.at(context, pos, context.getClickedFace()))) {
                return refuse(Problem.OCCUPIED, pos);
            }
            if (!level.isUnobstructed(cell.state(), pos, collision)) {
                return refuse(Problem.OBSTRUCTED, pos);
            }
        }
        for (PlacementPlan.Cell cell : plan.cells()) {
            if (!structure.supported(level, cell.pos(), cell.state())) {
                return refuse(Problem.UNSUPPORTED, cell.pos());
            }
        }
        if (permission && player != null) {
            for (PlacementPlan.Cell cell : plan.cells()) {
                ActionContext action = ActionContext.using(player, level, cell.pos(), context.getClickedFace(), context.getItemInHand());
                if (!ActionPermissionService.mayModify(action)) {
                    return refuse(Problem.DENIED, cell.pos());
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<Refusal> refuse(Problem problem, BlockPos pos) {
        return Optional.of(new Refusal(problem, pos.immutable()));
    }
}
