package io.github.dirtyredz.herbalwitch.grimoire.access.menu;

import io.github.dirtyredz.herbalwitch.grimoire.reading.ReaderSession;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * The grimoire reader: a menu with no slots at all (no storage, input, output or player inventory)
 * that exists so the server can tie requests to an open, validated reader. It carries its access
 * context and a server-issued session nonce; the server side also owns the {@link ReaderSession}
 * (selected entry, last availability sent). Every request names the menu id and nonce.
 */
public class GrimoireMenu extends AbstractContainerMenu {
    private final GrimoireAccess access;
    private final long nonce;
    private final ReaderSession session;

    /** Server side. */
    public GrimoireMenu(int id, Inventory inventory, GrimoireAccess access, long nonce) {
        super(GrimoireMenus.READER.get(), id);
        this.access = access;
        this.nonce = nonce;
        this.session = new ReaderSession(nonce);
    }

    /** Client side, from the opening packet. */
    public GrimoireMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        super(GrimoireMenus.READER.get(), id);
        this.access = GrimoireAccess.read(buf);
        this.nonce = buf.readLong();
        this.session = null;
    }

    public GrimoireAccess access() {
        return access;
    }

    public long nonce() {
        return nonce;
    }

    /** The server's reading session; null on the client. */
    public ReaderSession session() {
        return session;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // no slots
    }
    @Override public boolean clickMenuButton(Player player, int button) {
        return player instanceof net.minecraft.server.level.ServerPlayer server && GrimoireMenuActions.open(server, this, button);
    }
}
