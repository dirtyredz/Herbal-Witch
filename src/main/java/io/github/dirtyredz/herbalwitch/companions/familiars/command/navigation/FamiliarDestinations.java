package io.github.dirtyredz.herbalwitch.companions.familiars.command.navigation;

import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarCatalog;
import io.github.dirtyredz.herbalwitch.platform.permission.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import java.util.Optional;

/** Bounded loaded-only geometry, shared by navigation, catch-up, recall and emergency steps. */
public final class FamiliarDestinations {
    private FamiliarDestinations() {}
    public static boolean safe(ServerLevel level, Mob mob, Vec3 feet, ServerPlayer owner) {
        AABB box = mob.getDimensions(mob.getPose()).makeBoundingBox(feet);
        if (!level.getWorldBorder().isWithinBounds(box) || box.minY < level.getMinBuildHeight() || box.maxY >= level.getMaxBuildHeight()
                || box.getXsize() > 4 || box.getYsize() > 4 || box.getZsize() > 4) return false;
        for (var pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX - .001, box.maxY - .001, box.maxZ - .001))) {
            if (!level.hasChunkAt(pos) || owner != null && !ActionPermissionService.mayUse(ActionContext.of(owner, level, pos))) return false;
            if (owner == null) {
                var record = io.github.dirtyredz.herbalwitch.companions.familiars.bond.entity.FamiliarReconciliation.record(mob).orElse(null);
                if (record == null || !PrincipalPermission.mayUse(level, record.owner(), pos).allowed()) return false;
            }
            var state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty() || dangerous(level, pos)) return false;
        }
        boolean flying = FamiliarCatalog.type(EntityType.getKey(mob.getType()).toString()).map(s -> s.flying()).orElse(false);
        var support = BlockPos.containing(feet.x, feet.y - .01, feet.z);
        if (!level.hasChunkAt(support) || dangerous(level, support)
                || !flying && !level.getBlockState(support).isFaceSturdy(level, support, Direction.UP)) return false;
        return level.noCollision(mob, box);
    }
    private static boolean dangerous(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.CACTUS) || state.is(Blocks.MAGMA_BLOCK) || state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE) || state.is(Blocks.WITHER_ROSE)
                || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.NETHER_PORTAL)
                || state.is(Blocks.END_PORTAL) || state.is(Blocks.END_GATEWAY);
    }
    public static Optional<Vec3> near(ServerLevel level, Mob mob, Vec3 centre, ServerPlayer owner, int radius) {
        int bounded = Math.max(1, Math.min(8, radius)); var origin = BlockPos.containing(centre);
        // At most 24 concrete candidates. No chunk search or ticket.
        for (int r = 1; r <= bounded; r++) for (int i = 0; i < 8; i++) {
            int x = new int[]{1, 1, 0, -1, -1, -1, 0, 1}[i] * r;
            int z = new int[]{0, 1, 1, 1, 0, -1, -1, -1}[i] * r;
            var point = Vec3.atBottomCenterOf(origin.offset(x, 0, z));
            if (safe(level, mob, point, owner)) return Optional.of(point);
            if ((r - 1) * 8 + i >= 23) return Optional.empty();
        }
        return Optional.empty();
    }
    public static boolean routeLoaded(Mob mob, Vec3 destination) {
        if (!(mob.level() instanceof ServerLevel level) || mob.position().distanceToSqr(destination) > 64 * 64) return false;
        double range = mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        if (!Double.isFinite(range) || range > 64) return false;
        // PathNavigation's search region includes its follow-range plus eight-block margin.
        int margin = (int) Math.ceil(range) + 8;
        var a = mob.blockPosition(); var b = BlockPos.containing(destination);
        for (int cx = (Math.min(a.getX(), b.getX()) - margin) >> 4; cx <= (Math.max(a.getX(), b.getX()) + margin) >> 4; cx++)
            for (int cz = (Math.min(a.getZ(), b.getZ()) - margin) >> 4; cz <= (Math.max(a.getZ(), b.getZ()) + margin) >> 4; cz++)
                if (!level.hasChunk(cx, cz)) return false;
        return true;
    }
}
