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
        if (request.action().equals("power") || request.action().equals("auto") || request.action().equals("charge")) {
            if (!(menu instanceof FamiliarDetailMenu) || !menu.stillValid(owner)) return "nearby_only";
            return switch (request.action()) {
                case "auto" -> io.github.dirtyredz.herbalwitch.companions.familiars.ability.FamiliarAbilities.toggle(owner, request.bond());
                case "charge" -> io.github.dirtyredz.herbalwitch.companions.familiars.ability.FamiliarReserve.charge(owner, request.bond());
                default -> io.github.dirtyredz.herbalwitch.companions.familiars.ability.FamiliarAbilities.request(owner, request.bond(), request.value(), request.cursor());
            };
        }
        if (request.action().equals("home")) {
            var home = menu.home(owner);
            return home == null ? "unavailable" : io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeService.assign(owner, home, request.bond());
        }
        if (request.action().equals("cargo")) return menu instanceof FamiliarDetailMenu detail ? detail.collectCargo(owner) : "nearby_only";
        if (request.action().equals("recall")) return FamiliarRecall.recall(owner, request.bond());
        if (request.action().equals("command")) {
            try { return FamiliarCommands.set(owner, request.bond(), FamiliarCommand.parse(request.value())); }
            catch (IllegalArgumentException invalid) { return null; }
        }
        return null;
    }
}
