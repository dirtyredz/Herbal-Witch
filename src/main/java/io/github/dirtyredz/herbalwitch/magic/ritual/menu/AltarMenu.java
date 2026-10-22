package io.github.dirtyredz.herbalwitch.magic.ritual.menu;

import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;

import java.security.SecureRandom;

/**
 * The altar's menu, {@code herbalwitch:magic/ritual/altar}: zero slots (no storage, input or output;
 * offerings are taken from the payer's own inventory at completion). It exists so requests can be
 * tied to an open, validated altar: it carries the altar's origin and a server-issued session nonce,
 * and stays open only while {@link AltarAccess} holds.
 */
public class AltarMenu extends AbstractContainerMenu {
    public static final String TITLE = "container.herbalwitch.magic.ritual.altar";
    private static final SecureRandom NONCES = new SecureRandom();
    private final BlockPos origin;
    private final long nonce;

    public AltarMenu(int id, Inventory inventory, BlockPos origin, long nonce) {
        super(RitualMenus.ALTAR.get(), id);
        this.origin = origin.immutable();
        this.nonce = nonce;
    }

    /** Client side, from the opening packet. */
    public AltarMenu(int id, Inventory inventory, FriendlyByteBuf buf) {
        this(id, inventory, buf.readBlockPos(), buf.readLong());
    }

    public BlockPos origin() {
        return origin;
    }

    public long nonce() {
        return nonce;
    }

    @Override
    public boolean stillValid(Player player) {
        return AltarAccess.mayUse(player, origin);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // no slots
    }

    /** Opens the altar's menu with a fresh session nonce, if the player may use it now. */
    public static boolean open(ServerPlayer player, BlockPos origin) {
        if (!AltarAccess.mayUse(player, origin)) {
            return false;
        }
        long nonce;
        do {
            nonce = NONCES.nextLong();
        } while (nonce == 0);
        long issued = nonce;
        NetworkHooks.openScreen(player, new SimpleMenuProvider((id, inventory, p) -> new AltarMenu(id, inventory, origin, issued),
                Component.translatable(TITLE)), buf -> {
            buf.writeBlockPos(origin);
            buf.writeLong(issued);
        });
        return player.containerMenu instanceof AltarMenu menu && menu.nonce() == issued;
    }
}
