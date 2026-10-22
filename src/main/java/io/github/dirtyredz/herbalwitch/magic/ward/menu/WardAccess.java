package io.github.dirtyredz.herbalwitch.magic.ward.menu;

import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionContext;
import io.github.dirtyredz.herbalwitch.platform.permission.ActionPermissionService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Who may look at a ward stone's settings, checked on opening and while the menu stays open: a
 * living, non-spectating player in its level, the stone loaded and standing, within
 * {@link WardCatalog#REACH} blocks, and the shared permission boundary's use rule. Changing anything
 * is the owner's alone ({@code WardService}).
 */
public final class WardAccess {
    private WardAccess() {
    }

    public static boolean mayUse(Player player, BlockPos pos) {
        Level level = player.level();
        if (!player.isAlive() || player.isSpectator() || player.isRemoved() || !level.isLoaded(pos)
                || !(level.getBlockState(pos).getBlock() instanceof WardStoneBlock)) {
            return false;
        }
        if (player.position().distanceTo(Vec3.atCenterOf(pos)) > WardCatalog.REACH) {
            return false;
        }
        return level.isClientSide || ActionPermissionService.mayUse(ActionContext.of(player, level, pos));
    }
}
