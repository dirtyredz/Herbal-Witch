package io.github.dirtyredz.herbalwitch.companions.animals.capture;

import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionContext.Action;
import io.github.dirtyredz.herbalwitch.platform.permission.entity.EntityActionPermission;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/** Exactly one aimed adjacent cell. Never searches, loads chunks, or changes a denied destination. */
public final class CaptureReleasePlacement {
    private CaptureReleasePlacement() {}
    public static void position(Mob mob, BlockPos clicked, Direction face) {
        BlockPos feet = clicked.relative(face);
        mob.moveTo(feet.getX() + .5, feet.getY(), feet.getZ() + .5, 0, 0);
        mob.setDeltaMovement(Vec3.ZERO); mob.setTarget(null); mob.getNavigation().stop(); mob.fallDistance = 0;
    }
    public static boolean safe(ServerPlayer player, Mob mob, BlockPos clicked) {
        var level = player.serverLevel(); var box = mob.getBoundingBox();
        if (!player.isAlive() || player.isSpectator() || player.level() != mob.level() || !player.canReach(clicked, 0)
                || !player.hasLineOfSight(mob)
                || !level.getWorldBorder().isWithinBounds(box) || box.minY < level.getMinBuildHeight() || box.maxY >= level.getMaxBuildHeight()
                || box.getXsize() > 8 || box.getYsize() > 8 || box.getZsize() > 8) return false;
        boolean water = mob.getType() == EntityType.AXOLOTL;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY - .01, box.minZ), BlockPos.containing(box.maxX - .001, box.maxY - .001, box.maxZ - .001))) {
            if (!level.hasChunkAt(pos) || !ActionPermissionService.mayUse(ActionContext.of(player, level, pos))) return false;
            var block = level.getBlockState(pos); var fluid = block.getFluidState();
            if (block.is(Blocks.CACTUS) || block.is(Blocks.MAGMA_BLOCK) || block.is(Blocks.FIRE) || block.is(Blocks.SOUL_FIRE)
                    || block.is(Blocks.CAMPFIRE) || block.is(Blocks.SOUL_CAMPFIRE) || block.is(Blocks.WITHER_ROSE)
                    || block.is(Blocks.SWEET_BERRY_BUSH) || block.is(Blocks.POWDER_SNOW) || block.is(Blocks.NETHER_PORTAL)
                    || block.is(Blocks.END_PORTAL) || block.is(Blocks.END_GATEWAY)) return false;
            if (pos.getY() < box.minY) {
                if (!water && !block.isFaceSturdy(level, pos, Direction.UP)) return false;
            } else if (water ? !fluid.is(FluidTags.WATER) || fluid.getAmount() < 8 : !fluid.isEmpty()) return false;
        }
        return level.noCollision(mob, box) && EntityActionPermission.check(Action.ANIMAL_RELEASE, player, mob).allowed();
    }
}
