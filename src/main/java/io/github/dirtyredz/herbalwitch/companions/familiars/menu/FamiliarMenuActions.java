package io.github.dirtyredz.herbalwitch.companions.familiars.menu;

import io.github.dirtyredz.herbalwitch.companions.familiars.command.*;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarRequest;
import net.minecraft.server.level.ServerPlayer;

/** Called only after the enclosing menu's nonce, owner revision and request budget pass. */
public final class FamiliarMenuActions {
    private FamiliarMenuActions() {}
    public static String apply(ServerPlayer owner, FamiliarRosterMenu menu, FamiliarRequest request) {
        if (request.bond() == null) return null;
        if (menu.selected() != null && !menu.selected().equals(request.bond())) return null;
        if (request.action().equals("recall")) return FamiliarRecall.recall(owner, request.bond());
        if (request.action().equals("command")) {
            try { return FamiliarCommands.set(owner, request.bond(), FamiliarCommand.parse(request.value())); }
            catch (IllegalArgumentException invalid) { return null; }
        }
        return null;
    }
}
