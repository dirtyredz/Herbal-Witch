package io.github.dirtyredz.herbalwitch.platform.permission;

import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

/**
 * The one authorization boundary for this mod's custom world changes (herb harvests, garden
 * products, showcase placement). A change is allowed only at a loaded position inside the world
 * border and build height, by a player who may build there (adventure mode, spawn protection and
 * {@code mayUseItemAt}). Placing a new block additionally fires Forge's place event, so claim
 * mods can cancel it.
 */
public final class ActionPermissionService {
    private ActionPermissionService() {
    }

    /** Vanilla position and player checks. */
    public static boolean mayModify(ActionContext context) {
        Level level = context.level();
        return level.isLoaded(context.pos())
                && level.getWorldBorder().isWithinBounds(context.pos())
                && !level.isOutsideBuildHeight(context.pos())
                && context.player().mayBuild()
                && level.mayInteract(context.player(), context.pos())
                && context.player().mayUseItemAt(context.pos(), context.face(), context.stack());
    }

    /**
     * Whether a player may use (not build at) a position: loaded, inside the world border and build
     * height, and not refused by the level's interaction rules (spawn protection). Deliberately not
     * {@link #mayModify}: using an altar or a chair is not building, so adventure players may.
     * Forge's own interaction events still apply on the way in.
     */
    public static boolean mayUse(ActionContext context) {
        Level level = context.level();
        return level.isLoaded(context.pos())
                && level.getWorldBorder().isWithinBounds(context.pos())
                && !level.isOutsideBuildHeight(context.pos())
                && level.mayInteract(context.player(), context.pos());
    }

    /**
     * {@link #mayModify} plus Forge's place event at the position. Call before setting the block;
     * nothing is written here.
     */
    public static boolean mayPlace(ActionContext context) {
        if (!mayModify(context)) {
            return false;
        }
        BlockSnapshot snapshot = BlockSnapshot.create(context.level().dimension(), context.level(), context.pos());
        return !ForgeEventFactory.onBlockPlace(context.player(), snapshot, context.face());
    }
}
