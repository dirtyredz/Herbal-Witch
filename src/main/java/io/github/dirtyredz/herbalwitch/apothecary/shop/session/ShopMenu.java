package io.github.dirtyredz.herbalwitch.apothecary.shop.session;

import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * The commerce menu ({@code herbalwitch:shop/menu}): no slots at all. Stock and offers are read-only
 * projections delivered by {@link ShopUpdate}; every change is a bounded {@link ShopRequest} the server
 * validates. Vanilla closes the menu as soon as the session is no longer valid (death, logout,
 * dimension change, reach or permission lost, rebind, configuration change, a new recovery state).
 */
public final class ShopMenu extends AbstractContainerMenu {
    public static final String TITLE = "container.herbalwitch.shop";
    private final ShopSession session;
    private final ShopSession.View view;
    private final UUID nonce;

    /** Server side. */
    public ShopMenu(int id, ShopSession session) {
        super(ShopMenus.MENU.get(), id);
        this.session = session;
        this.view = session.view();
        this.nonce = session.nonce();
    }

    /** Client side, from the opening packet: only the view and the nonce. */
    public ShopMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        super(ShopMenus.MENU.get(), id);
        this.session = null;
        this.view = buf.readEnum(ShopSession.View.class);
        this.nonce = buf.readUUID();
    }

    public ShopSession.View view() {
        return view;
    }

    public UUID nonce() {
        return nonce;
    }

    public ShopSession session() {
        return session;
    }

    @Override
    public boolean stillValid(Player player) {
        return session == null || player instanceof ServerPlayer server && ShopSessionService.valid(server, session);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (session != null && player instanceof ServerPlayer server) ShopSessionService.closed(server, session);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
