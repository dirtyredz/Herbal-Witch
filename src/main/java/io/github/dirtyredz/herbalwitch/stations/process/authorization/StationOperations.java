package io.github.dirtyredz.herbalwitch.stations.process.authorization;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.platform.network.RateLimit;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessStation;
import io.github.dirtyredz.herbalwitch.stations.process.execution.ProcessExecutionPolicy;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * What a station screen's Start, Resume and Cancel buttons do, server side. They arrive as vanilla
 * menu-button clicks, so vanilla has already checked the open menu's id and validity; this adds a
 * per-player rate limit and the policy's own rules, and tells the player the outcome on the action
 * bar ({@code status.herbalwitch.station.<outcome>}). No custom packet, no client-supplied cost.
 * Hoppers and other automation can never reach it: they have no menu.
 */
public final class StationOperations {
    /** The buttons, by vanilla button id (their ordinal). */
    public enum Operation {
        START,
        RESUME,
        CANCEL;

        public int id() {
            return ordinal();
        }

        public static Operation byId(int id) {
            Operation[] values = values();
            return id >= 0 && id < values.length ? values[id] : null;
        }
    }

    public static final String KEY = "status." + HerbalWitch.MOD_ID + ".station.";
    private static final RateLimit LIMIT = new RateLimit(5, 20);

    private StationOperations() {
    }

    /**
     * Handles one button click on a station menu; returns whether it was one of ours. {@code station}
     * and {@code policy} belong to the station whose origin is {@code pos}.
     */
    public static boolean handle(ServerPlayer player, int button, ProcessStation station, MagicalProcessPolicy policy, Level level,
                                 BlockPos pos) {
        Operation op = Operation.byId(button);
        if (op == null || player.getServer() == null) {
            return false;
        }
        if (!LIMIT.allow(player.getUUID(), player.getServer().getTickCount())) {
            return true;
        }
        String outcome = operate(player, op, station, policy, new ProcessExecutionPolicy.Site(level, pos));
        player.displayClientMessage(Component.translatable(KEY + outcome), true);
        return true;
    }

    /** The operation's effect; returns the outcome suffix. */
    public static String operate(ServerPlayer player, Operation op, ProcessStation station, MagicalProcessPolicy policy,
                                 ProcessExecutionPolicy.Site site) {
        ProcessRecipe recipe = station.job().recipe();
        return switch (op) {
            case START -> policy.start(player, recipe, site);
            case RESUME -> policy.resume(player, recipe, site);
            case CANCEL -> {
                if (policy.operator().isEmpty()) {
                    yield "nothing";
                }
                if (!policy.mayCancel(player)) {
                    yield "not_yours";
                }
                station.job().release(station.inventory());
                yield "cancelled";
            }
        };
    }

    public static void loggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LIMIT.forget(event.getEntity().getUUID());
    }
}
