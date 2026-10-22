package io.github.dirtyredz.herbalwitch.grimoire.access.menu;

import io.github.dirtyredz.herbalwitch.grimoire.access.block.ResearchAccessBlock;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ReaderService;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import io.github.dirtyredz.herbalwitch.platform.network.PayloadBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import java.security.SecureRandom;

/**
 * How a reader was opened, validated while it stays open: a carried grimoire (the holder must keep
 * one in their inventory or offhand) or a research block (it must still stand, loaded, within
 * {@link #MAX_DISTANCE} blocks, at a position the player may interact with). Vanilla closes a menu
 * whose {@link #stillValid} turns false, so breaking the block, walking away or dropping the book
 * closes the reader. Research and teaching invitations need a research block; reading, missing
 * counts and answering an invitation work from either.
 *
 * @param kind  carried book or research block
 * @param pos   the block (unused for a book)
 * @param opens the page the reader starts on
 */
public record GrimoireAccess(Kind kind, BlockPos pos, GrimoireCatalog.Opens opens) {
    public static final double MAX_DISTANCE = 8.0;
    public static final String TITLE = "container.herbalwitch.grimoire.reader";
    private static final SecureRandom NONCES = new SecureRandom();

    public enum Kind {
        BOOK,
        BLOCK
    }

    public static GrimoireAccess book() {
        return new GrimoireAccess(Kind.BOOK, BlockPos.ZERO, GrimoireCatalog.Opens.INDEX);
    }

    public static GrimoireAccess block(BlockPos pos, GrimoireCatalog.Opens opens) {
        return new GrimoireAccess(Kind.BLOCK, pos.immutable(), opens);
    }

    /** Research and teaching invitations happen at research blocks. */
    public boolean atResearchBlock() {
        return kind == Kind.BLOCK;
    }

    public boolean stillValid(Player player) {
        if (!player.isAlive()) {
            return false;
        }
        if (kind == Kind.BOOK) {
            return player.getInventory().hasAnyMatching(GrimoireItems::isGrimoire);
        }
        Level level = player.level();
        return level.isLoaded(pos)
                && level.getBlockState(pos).getBlock() instanceof ResearchAccessBlock
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= MAX_DISTANCE * MAX_DISTANCE
                && level.mayInteract(player, pos);
    }

    public void write(FriendlyByteBuf buf) {
        PayloadBounds.writeEnum(buf, kind);
        buf.writeBlockPos(pos);
        PayloadBounds.writeEnum(buf, opens);
    }

    public static GrimoireAccess read(FriendlyByteBuf buf) {
        return new GrimoireAccess(PayloadBounds.readEnum(buf, Kind.class), buf.readBlockPos(),
                PayloadBounds.readEnum(buf, GrimoireCatalog.Opens.class));
    }

    /** Opens the player's reader with a fresh session, if the access is valid now. */
    public boolean open(ServerPlayer player) {
        if (!stillValid(player)) {
            return false;
        }
        long nonce = nonce();
        NetworkHooks.openScreen(player, new SimpleMenuProvider((id, inventory, p) -> new GrimoireMenu(id, inventory, this, nonce),
                Component.translatable(TITLE)), buf -> {
            write(buf);
            buf.writeLong(nonce);
        });
        if (player.containerMenu instanceof GrimoireMenu menu && menu.nonce() == nonce) {
            ReaderService.opened(player, menu);
        }
        return true;
    }

    private static long nonce() {
        long n;
        do {
            n = NONCES.nextLong();
        } while (n == 0);
        return n;
    }
}
