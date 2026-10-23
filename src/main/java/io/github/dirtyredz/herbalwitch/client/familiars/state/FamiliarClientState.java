package io.github.dirtyredz.herbalwitch.client.familiars.state;

import io.github.dirtyredz.herbalwitch.companions.familiars.menu.FamiliarRosterMenu;
import io.github.dirtyredz.herbalwitch.companions.familiars.network.FamiliarRosterPage;
import net.minecraft.client.Minecraft;

/** One current menu page, cleared on close/disconnect. It never accumulates an entire roster. */
public final class FamiliarClientState {
    private static FamiliarRosterPage latest;
    private FamiliarClientState() {}
    public static void accept(FamiliarRosterPage page) {
        var player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu instanceof FamiliarRosterMenu menu && page.container() == menu.containerId && page.nonce() == menu.nonce()) latest = page;
    }
    public static FamiliarRosterPage get(FamiliarRosterMenu menu) { return latest != null && latest.container() == menu.containerId && latest.nonce() == menu.nonce() ? latest : null; }
    public static void clear() { latest = null; }
}
