package io.github.dirtyredz.herbalwitch.client.stations.shop;

import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopMenu;
import io.github.dirtyredz.herbalwitch.apothecary.shop.session.ShopUpdate;
import net.minecraft.client.Minecraft;

/**
 * The latest projection for the open commerce menu only. An update for another menu or nonce is
 * ignored; nothing is kept after the menu closes or the connection ends. A revision counter tells the
 * screen when to rebuild.
 */
public final class ShopClientState {
    private static ShopUpdate latest;
    private static int version;

    private ShopClientState() {
    }

    public static void accept(ShopUpdate update) {
        var player = Minecraft.getInstance().player;
        if (player == null || !(player.containerMenu instanceof ShopMenu menu) || menu.containerId != update.container()
                || !menu.nonce().equals(update.nonce())) return;
        latest = update;
        version++;
    }

    public static ShopUpdate latest(ShopMenu menu) {
        return latest != null && latest.container() == menu.containerId && latest.nonce().equals(menu.nonce()) ? latest : null;
    }

    public static int version() {
        return version;
    }

    public static void clear() {
        latest = null;
        version++;
    }
}
