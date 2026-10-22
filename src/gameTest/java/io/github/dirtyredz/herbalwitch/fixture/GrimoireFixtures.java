package io.github.dirtyredz.herbalwitch.fixture;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireAccess;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.network.GrimoirePackets;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Grimoire setup for GameTests: fresh fake players (a unique name per test, since fake players are
 * cached and their knowledge would otherwise carry over), readers opened through the real access
 * paths, recipe keys, and capturing what the server sends to whom.
 */
public final class GrimoireFixtures {
    /** A message the server sent, and to whom. */
    public record Sent(ServerPlayer to, Object message) {
    }

    private GrimoireFixtures() {
    }

    public static EntryKey recipe(String path) {
        return EntryKey.of(EntryKey.Kind.RECIPE, HerbalWitch.MOD_ID + ":" + path);
    }

    public static GrimoireContent content(GameTestHelper helper) {
        return ServerGrimoireContent.get(helper.getLevel().getServer());
    }

    /** A survival fake player standing at {@code feet} (relative), empty-handed. */
    public static ServerPlayer standing(GameTestHelper helper, String name, BlockPos feet) {
        ServerPlayer player = PlayerFixtures.player(helper, name, GameType.SURVIVAL, Direction.NORTH);
        moveTo(helper, player, feet);
        return player;
    }

    public static void moveTo(GameTestHelper helper, ServerPlayer player, BlockPos feet) {
        BlockPos abs = helper.absolutePos(feet);
        player.moveTo(abs.getX() + 0.5, abs.getY(), abs.getZ() + 0.5, Direction.NORTH.toYRot(), 0);
    }

    /** Puts stacks in the main inventory, in order. */
    public static void carry(ServerPlayer player, ItemStack... stacks) {
        for (ItemStack stack : stacks) {
            player.getInventory().add(stack);
        }
    }

    /** Gives the player a grimoire and opens it; returns the open reader. */
    public static GrimoireMenu openBook(ServerPlayer player) {
        carry(player, new ItemStack(GrimoireItems.BOOK.get()));
        WorldFixtures.check(GrimoireAccess.book().open(player), "the book did not open");
        return (GrimoireMenu) player.containerMenu;
    }

    /** Places a research block facing north at {@code pos} (relative) on whatever stands below. */
    public static void place(GameTestHelper helper, GrimoireCatalog.ResearchPiece piece, BlockPos pos) {
        helper.setBlock(pos, GrimoireBlocks.block(piece).defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
    }

    /** Opens the reader of the research block at {@code pos} (relative); returns it, or null if refused. */
    public static GrimoireMenu openBlock(GameTestHelper helper, ServerPlayer player, GrimoireCatalog.ResearchPiece piece, BlockPos pos) {
        boolean opened = GrimoireAccess.block(helper.absolutePos(pos), piece.opens()).open(player);
        return opened ? (GrimoireMenu) player.containerMenu : null;
    }

    /** Runs {@code action} while recording every grimoire message the server sends. */
    public static List<Sent> capture(Runnable action) {
        List<Sent> sent = new ArrayList<>();
        GrimoirePackets.observe((to, message) -> sent.add(new Sent(to, message)));
        try {
            action.run();
        } finally {
            GrimoirePackets.observe(null);
        }
        return sent;
    }

    public static <T> T capture(Supplier<T> action, List<Sent> into) {
        GrimoirePackets.observe((to, message) -> into.add(new Sent(to, message)));
        try {
            return action.get();
        } finally {
            GrimoirePackets.observe(null);
        }
    }
}
