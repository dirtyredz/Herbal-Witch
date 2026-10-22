package io.github.dirtyredz.herbalwitch.fixture;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.menu.AltarMenu;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSession;
import io.github.dirtyredz.herbalwitch.magic.ritual.session.AltarSessionService;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.AltarShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.witch.initiation.InitiationEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Witch and altar setup for GameTests: an altar on the stone floor, fresh fake players (unique names,
 * since fake players are cached), players made witches through the real service, altar menus opened
 * through the real access path, offerings, and synchronous ritual ticks (so a test runs its whole
 * ritual inside its own tick and no other test can interleave with the world's founding record).
 */
public final class WitchFixtures {
    /** The altar's origin in the empty template: the altar faces north, so its front is at lower z. */
    public static final BlockPos ALTAR = new BlockPos(4, 1, 4);
    /** Where a participant stands: in front of the altar, within reach. */
    public static final BlockPos FRONT = new BlockPos(4, 1, 2);
    private static final List<ServerPlayer> REGISTERED = new ArrayList<>();

    private WitchFixtures() {
    }

    /** A complete altar facing north with its origin at {@link #ALTAR} (relative), on a stone floor. */
    public static void altar(GameTestHelper helper) {
        WorldFixtures.floor(helper);
        ((AltarBlock) RitualBlocks.ALTAR.get()).placeAltar(helper.getLevel(), helper.absolutePos(ALTAR), Direction.NORTH, Block.UPDATE_ALL);
    }

    /** A survival fake player standing at {@code feet}, known to the consent and session lookups. */
    public static ServerPlayer player(GameTestHelper helper, String name, BlockPos feet) {
        ServerPlayer player = GrimoireFixtures.standing(helper, name, feet);
        REGISTERED.removeIf(p -> p.getUUID().equals(player.getUUID()) || p.isRemoved());
        REGISTERED.add(player);
        InitiationEvents.setTestPlayers(REGISTERED);
        return player;
    }

    /** A witch at {@code feet} with full initial energy (closes open founding, as any conversion does). */
    public static ServerPlayer witch(GameTestHelper helper, String name, BlockPos feet) {
        ServerPlayer player = player(helper, name, feet);
        WorldFixtures.check(WitchStatusService.convert(player, UUID.randomUUID()), name + " was not converted");
        return player;
    }

    /** Opens the altar's menu for {@code player}; returns it, or null when access was refused. */
    public static AltarMenu open(GameTestHelper helper, ServerPlayer player) {
        return AltarMenu.open(player, helper.absolutePos(ALTAR)) ? (AltarMenu) player.containerMenu : null;
    }

    /** The founding offering, in the main inventory. */
    public static void foundingOffering(ServerPlayer player) {
        GrimoireFixtures.carry(player, new ItemStack(BotanyItems.fresh(HerbCatalog.LAVENDER)), new ItemStack(BotanyItems.fresh(HerbCatalog.SAGE)),
                new ItemStack(BotanyItems.fresh(HerbCatalog.MINT)), new ItemStack(BotanyItems.fresh(HerbCatalog.CHAMOMILE)),
                new ItemStack(Items.AMETHYST_SHARD), AltarShowcaseSection.sampleBook());
    }

    /** The initiation offering, in the main inventory. */
    public static void initiationOffering(ServerPlayer player) {
        GrimoireFixtures.carry(player, new ItemStack(BotanyItems.fresh(HerbCatalog.SAGE)), new ItemStack(BotanyItems.fresh(HerbCatalog.LAVENDER)),
                new ItemStack(Items.AMETHYST_SHARD));
    }

    /** Runs {@code ticks} ticks of one session, synchronously. */
    public static void advance(AltarSession session, int ticks) {
        for (int i = 0; i < ticks && session.running(); i++) {
            AltarSessionService.tick(session);
        }
    }

    /** The player's running session (fails the test if there is none). */
    public static AltarSession session(ServerPlayer player) {
        return AltarSessionService.of(player.getUUID()).orElseThrow(() -> new net.minecraft.gametest.framework.GameTestAssertException(
                player.getGameProfile().getName() + " has no running ritual"));
    }

    /** Fills every main-inventory slot with dirt (a full inventory). */
    public static void fill(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            if (player.getInventory().items.get(i).isEmpty()) {
                player.getInventory().items.set(i, new ItemStack(Items.DIRT, 64));
            }
        }
    }
}
