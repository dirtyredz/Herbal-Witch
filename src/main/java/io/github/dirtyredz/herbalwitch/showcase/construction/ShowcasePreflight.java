package io.github.dirtyredz.herbalwitch.showcase.construction;

import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

/**
 * Checks the whole showcase box before a single write: loaded, inside the world border and build
 * height, buildable by the player, a full solid floor, and nothing that must not be destroyed
 * (block entities, unbreakable blocks, entities). The first problem is reported with its position.
 * Never loads chunks or adds tickets.
 */
public final class ShowcasePreflight {
    public static final String KEY = "commands.herbalwitch.showcase.";

    private ShowcasePreflight() {
    }

    /** The box's minimum corner is {@code origin}: the floor layer is {@code origin.y}. */
    public static Optional<Component> check(ShowcasePlan plan, ServerLevel level, ServerPlayer player, BlockPos origin) {
        int top = origin.getY() + ShowcasePlan.HEIGHT - 1;
        if (origin.getY() < level.getMinBuildHeight() || top >= level.getMaxBuildHeight()) {
            return fail("outside_world", pos(origin));
        }
        for (int dx = 0; dx < plan.width(); dx++) {
            for (int dz = 0; dz < plan.depth(); dz++) {
                BlockPos floor = origin.offset(dx, 0, dz);
                if (!level.isLoaded(floor)) {
                    return fail("unloaded", pos(floor));
                }
                if (!level.getWorldBorder().isWithinBounds(floor)) {
                    return fail("outside_world", pos(floor));
                }
                if (!ActionPermissionService.mayModify(ActionContext.of(player, level, floor))) {
                    return fail("denied", pos(floor));
                }
                BlockState ground = level.getBlockState(floor);
                if (ground.isAir() || !ground.getFluidState().isEmpty() || !ground.isCollisionShapeFullBlock(level, floor)) {
                    return fail("not_flat", pos(floor));
                }
                for (int dy = 0; dy < ShowcasePlan.HEIGHT; dy++) {
                    BlockPos cell = floor.above(dy);
                    if (level.getBlockEntity(cell) != null) {
                        return fail("block_entity", pos(cell));
                    }
                    if (level.getBlockState(cell).getDestroySpeed(level, cell) < 0) {
                        return fail("unbreakable", pos(cell));
                    }
                }
            }
        }
        AABB box = new AABB(origin, origin.offset(plan.width(), ShowcasePlan.HEIGHT, plan.depth()));
        List<Entity> entities = level.getEntities((Entity) null, box, entity -> true);
        if (!entities.isEmpty()) {
            Entity first = entities.get(0);
            return fail("entity", first.getDisplayName(), pos(first.blockPosition()));
        }
        return Optional.empty();
    }

    static String pos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static Optional<Component> fail(String reason, Object... args) {
        return Optional.of(Component.translatable(KEY + reason, args));
    }
}
