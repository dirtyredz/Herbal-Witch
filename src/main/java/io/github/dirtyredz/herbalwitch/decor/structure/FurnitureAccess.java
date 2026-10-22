package io.github.dirtyredz.herbalwitch.decor.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Who may use furniture (sit, open a cabinet, switch a lamp): any living, non-spectating player
 * within reach of a loaded piece, in any game mode that can interact, adventure included. This is
 * deliberately not the build permission ({@code ActionPermissionService.mayModify}), which refuses
 * adventure players: using a chair is not building. Forge's own interaction events still apply.
 */
public final class FurnitureAccess {
    /** Vanilla's block reach, squared, measured to the piece's origin cell centre (with a cell of slack for large pieces). */
    private static final double REACH = 8.0 * 8.0;

    private FurnitureAccess() {
    }

    public static boolean mayUse(Player player, Level level, BlockPos origin) {
        return level.isLoaded(origin) && player.isAlive() && !player.isSpectator()
                && player.distanceToSqr(origin.getX() + 0.5, origin.getY() + 0.5, origin.getZ() + 0.5) <= REACH;
    }
}
