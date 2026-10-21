package io.github.dirtyredz.herbalwitch.platform.permission;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Who wants to change which block, with what: the input to {@link ActionPermissionService}.
 *
 * @param player the acting player
 * @param level  the level of the target
 * @param pos    the block that would change
 * @param face   the clicked face (UP when not from a click)
 * @param stack  the item used, or empty
 */
public record ActionContext(Player player, Level level, BlockPos pos, Direction face, ItemStack stack) {
    public static ActionContext of(Player player, Level level, BlockPos pos) {
        return new ActionContext(player, level, pos.immutable(), Direction.UP, ItemStack.EMPTY);
    }

    public static ActionContext using(Player player, Level level, BlockPos pos, Direction face, ItemStack stack) {
        return new ActionContext(player, level, pos.immutable(), face, stack);
    }

    public ActionContext at(BlockPos other) {
        return new ActionContext(player, level, other.immutable(), face, stack);
    }
}
