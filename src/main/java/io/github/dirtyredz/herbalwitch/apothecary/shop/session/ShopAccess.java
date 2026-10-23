package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.apothecary.shop.config.ShopRules;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.link.ShopBindingService;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Who may use a commerce menu right now. Using a shop is interaction, not building: adventure players
 * may buy when the level lets them interact there. Every request rechecks presence, reach (four blocks
 * from the eye to the fixture), line of sight and use permission; opening a menu earlier is never
 * continuing authorization. Operators get no silent ownership bypass.
 */
public final class ShopAccess {
    private ShopAccess() {
    }

    public static boolean actor(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && !player.isRemoved();
    }

    /** Eye within {@link ShopRules#REACH} blocks of the fixture's cell. */
    public static boolean near(ServerPlayer player, Level level, BlockPos pos) {
        if (player.level() != level) return false;
        Vec3 eye = player.getEyePosition();
        double x = Math.max(pos.getX(), Math.min(eye.x, pos.getX() + 1.0));
        double y = Math.max(pos.getY(), Math.min(eye.y, pos.getY() + 1.0));
        double z = Math.max(pos.getZ(), Math.min(eye.z, pos.getZ() + 1.0));
        return eye.distanceToSqr(x, y, z) <= ShopRules.REACH * (double) ShopRules.REACH;
    }

    /** Nothing solid between the eye and the fixture. */
    public static boolean sight(ServerPlayer player, Level level, BlockPos pos) {
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(eye, Vec3.atCenterOf(pos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(pos);
    }

    public static boolean mayUse(ServerPlayer player, Level level, BlockPos pos) {
        return level.isLoaded(pos) && ActionPermissionService.mayUse(ActionContext.of(player, level, pos));
    }

    /** The full per-request check against a session's origin. */
    public static boolean present(ServerPlayer player, ShopSession session) {
        if (!actor(player) || !ShopBindingService.dimension(player.level()).equals(session.dimension())) return false;
        BlockPos pos = BlockPos.of(session.pos());
        return near(player, player.level(), pos) && mayUse(player, player.level(), pos);
    }
}
