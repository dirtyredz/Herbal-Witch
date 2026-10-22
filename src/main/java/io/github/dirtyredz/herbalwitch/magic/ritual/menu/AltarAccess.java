package io.github.dirtyredz.herbalwitch.magic.ritual.menu;

import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Who may use an altar, checked when its menu opens and continuously while it stays open (vanilla
 * closes a menu whose validity fails): a living, non-spectating player in the altar's dimension; the
 * whole altar standing and loaded (never loading a chunk); the player within
 * {@link AltarCatalog#ACCESS_DISTANCE} blocks of the occupied footprint with a clear line of sight to
 * it; and the shared permission boundary's use rule (not the build rule, so adventure players may
 * use an altar). Forge's interaction events still apply on the way in.
 */
public final class AltarAccess {
    private AltarAccess() {
    }

    /** Whether {@code player} may use the altar whose origin is {@code origin} in their own level now. */
    public static boolean mayUse(Player player, BlockPos origin) {
        Level level = player.level();
        if (!player.isAlive() || player.isSpectator() || player.isRemoved()) {
            return false;
        }
        if (!complete(level, origin)) {
            return false;
        }
        AABB footprint = footprint(level, origin);
        Vec3 eye = player.getEyePosition();
        if (distance(footprint, eye) > AltarCatalog.ACCESS_DISTANCE) {
            return false;
        }
        if (!level.isClientSide && !ActionPermissionService.mayUse(ActionContext.of(player, level, origin))) {
            return false;
        }
        return level.isClientSide || sees(player, footprint, eye);
    }

    /** Every cell of the altar is loaded and present, facing one way, in order. */
    public static boolean complete(Level level, BlockPos origin) {
        if (!level.isLoaded(origin)) {
            return false;
        }
        BlockState originState = level.getBlockState(origin);
        if (!(originState.getBlock() instanceof AltarBlock) || AltarBlock.cell(originState) != 0) {
            return false;
        }
        Direction facing = originState.getValue(AltarBlock.FACING);
        for (int i = 1; i < AltarBlock.LAYOUT.size(); i++) {
            BlockPos pos = AltarBlock.LAYOUT.cellPos(origin, facing, i);
            if (!level.isLoaded(pos)) {
                return false;
            }
            BlockState cell = level.getBlockState(pos);
            if (!(cell.getBlock() instanceof AltarBlock) || AltarBlock.cell(cell) != i || cell.getValue(AltarBlock.FACING) != facing) {
                return false;
            }
        }
        return true;
    }

    /** The box around every cell of a standing altar. */
    public static AABB footprint(Level level, BlockPos origin) {
        Direction facing = level.getBlockState(origin).getValue(AltarBlock.FACING);
        AABB box = new AABB(origin);
        for (int i = 1; i < AltarBlock.LAYOUT.size(); i++) {
            box = box.minmax(new AABB(AltarBlock.LAYOUT.cellPos(origin, facing, i)));
        }
        return box;
    }

    /** Distance from a point to a box (0 inside it). */
    public static double distance(AABB box, Vec3 point) {
        double dx = Math.max(0, Math.max(box.minX - point.x, point.x - box.maxX));
        double dy = Math.max(0, Math.max(box.minY - point.y, point.y - box.maxY));
        double dz = Math.max(0, Math.max(box.minZ - point.z, point.z - box.maxZ));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /** A clear line from the eyes to the nearest point of the footprint (the altar itself may be hit). */
    private static boolean sees(Player player, AABB footprint, Vec3 eye) {
        AABB inner = footprint.deflate(0.05);
        Vec3 target = new Vec3(clamp(eye.x, inner.minX, inner.maxX), clamp(eye.y, inner.minY, inner.maxY), clamp(eye.z, inner.minZ, inner.maxZ));
        if (eye.distanceToSqr(target) < 1.0e-6) {
            return true;
        }
        BlockHitResult hit = player.level().clip(new ClipContext(eye, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || footprint.inflate(0.01).contains(Vec3.atCenterOf(hit.getBlockPos()));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}
