package io.github.dirtyredz.herbalwitch.grimoire.access.menu;

import net.minecraft.server.level.ServerPlayer;
import java.util.*;
import java.util.function.Consumer;

/** Bootstrap contributions to the portable reader, with ordinary validated menu-button intents. */
public final class GrimoireMenuActions {
    public record Action(String label, Consumer<ServerPlayer> open) {}
    private static final List<Action> ACTIONS = new ArrayList<>();
    private GrimoireMenuActions() {}
    public static void register(String label, Consumer<ServerPlayer> open) {
        if (ACTIONS.size() >= 4 || ACTIONS.stream().anyMatch(a -> a.label().equals(label))) throw new IllegalArgumentException("Duplicate or excessive reader action");
        ACTIONS.add(new Action(label, open));
    }
    public static List<Action> actions() { return List.copyOf(ACTIONS); }
    public static boolean open(ServerPlayer player, GrimoireMenu menu, int button) {
        if (button < 0 || button >= ACTIONS.size() || !menu.stillValid(player)) return false;
        ACTIONS.get(button).open().accept(player); return true;
    }
}
