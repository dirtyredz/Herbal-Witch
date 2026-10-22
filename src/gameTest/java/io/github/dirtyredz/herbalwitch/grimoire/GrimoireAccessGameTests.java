package io.github.dirtyredz.herbalwitch.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireAccess;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryService;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderRequest;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ReaderService;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.carry;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.content;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.moveTo;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.openBlock;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.openBook;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.recipe;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.click;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Opening readers through the real paths and keeping them honest: the book only while carried, the
 * research blocks placed facing the player on a supported surface, dropped once, closing readers
 * when broken or left; stale sessions and research away from a research block refused.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrimoireAccessGameTests {
    private GrimoireAccessGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void theBookOpensItsHoldersReaderOnlyWhileCarried(GameTestHelper helper) {
        ServerPlayer player = standing(helper, "access_book", new BlockPos(1, 1, 1));
        check(!GrimoireAccess.book().open(player), "opened without a grimoire");
        GrimoireMenu menu = openBook(player);
        check(menu.slots.isEmpty(), "the reader has slots");
        check(menu.stillValid(player), "the reader is not valid while the book is carried");
        player.getInventory().clearContent();
        check(!menu.stillValid(player), "the reader stayed valid after the book was dropped");
        player.getInventory().offhand.set(0, new ItemStack(GrimoireItems.BOOK.get()));
        check(menu.stillValid(player), "a grimoire in the offhand counts");
        player.closeContainer();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void researchBlocksFaceThePlacerAndNeedSupport(GameTestHelper helper) {
        floor(helper);
        int x = 1;
        for (GrimoireCatalog.ResearchPiece piece : GrimoireCatalog.RESEARCH_PIECES) {
            Block block = GrimoireBlocks.block(piece);
            int z = 1;
            for (Direction looking : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                ServerPlayer player = player(helper, "access_" + piece.directory() + "_" + looking, GameType.SURVIVAL, looking);
                BlockPos target = new BlockPos(x, 1, z);
                InteractionResult result = useItemOn(helper, player, new ItemStack(block.asItem()), target.below(), Direction.UP);
                check(result.consumesAction(), piece.directory() + " not placed: " + result);
                check(helper.getBlockState(target).getValue(HorizontalDirectionalBlock.FACING) == looking.getOpposite(),
                        piece.directory() + " looking " + looking + " faces " + helper.getBlockState(target));
                z += 2;
            }
            ServerPlayer midair = player(helper, "access_air_" + piece.directory(), GameType.SURVIVAL, Direction.NORTH);
            BlockPos high = new BlockPos(x, 4, 1);
            helper.setBlock(high, Blocks.GLASS);
            useItemOn(helper, midair, new ItemStack(block.asItem()), high, Direction.DOWN);
            check(!helper.getBlockState(high.below()).is(block), piece.directory() + " hung under glass with nothing below it");
            x += 3;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void losingSupportOrBreakingDropsTheBlockOnce(GameTestHelper helper) {
        floor(helper);
        for (GrimoireCatalog.ResearchPiece piece : GrimoireCatalog.RESEARCH_PIECES) {
            clearDrops(helper);
            BlockPos support = new BlockPos(4, 1, 4);
            helper.setBlock(support, Blocks.OAK_PLANKS);
            place(helper, piece, support.above());
            helper.setBlock(support, Blocks.AIR);
            check(helper.getBlockState(support.above()).isAir(), piece.directory() + " survived losing its support");
            check(dropped(helper, GrimoireBlocks.block(piece).asItem()) == 1, piece.directory() + " dropped "
                    + dropped(helper, GrimoireBlocks.block(piece).asItem()));
            clearDrops(helper);
            place(helper, piece, new BlockPos(2, 1, 2));
            helper.getLevel().destroyBlock(helper.absolutePos(new BlockPos(2, 1, 2)), true);
            check(dropped(helper, GrimoireBlocks.block(piece).asItem()) == 1, piece.directory() + " broke into "
                    + dropped(helper, GrimoireBlocks.block(piece).asItem()));
        }
        clearDrops(helper);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void blockReadersCloseWhenBrokenOrLeft(GameTestHelper helper) {
        floor(helper);
        BlockPos stand = new BlockPos(4, 1, 4);
        place(helper, GrimoireCatalog.STAND, stand);
        ServerPlayer reader = standing(helper, "access_block", new BlockPos(4, 1, 2));
        GrimoireMenu menu = openBlock(helper, reader, GrimoireCatalog.STAND, stand);
        check(menu != null && menu.access().atResearchBlock(), "the stand did not open a research reader");
        check(menu.access().opens() == GrimoireCatalog.Opens.RESEARCH, "the stand opens on research");
        moveTo(helper, reader, new BlockPos(4, 1, 30));
        check(!menu.stillValid(reader), "the reader stayed valid out of range");
        moveTo(helper, reader, new BlockPos(4, 1, 2));
        check(menu.stillValid(reader), "the reader was not valid back in range");
        helper.setBlock(stand, Blocks.AIR);
        check(!menu.stillValid(reader), "the reader stayed valid after the stand was removed");
        reader.closeContainer();
        ServerPlayer clicker = standing(helper, "access_click", new BlockPos(2, 1, 2));
        place(helper, GrimoireCatalog.BOOKSHELF, new BlockPos(2, 1, 4));
        click(helper, clicker, new BlockPos(2, 1, 4));
        check(clicker.containerMenu instanceof GrimoireMenu shelf && shelf.access().opens() == GrimoireCatalog.Opens.INDEX,
                "clicking the bookshelf did not open its index");
        clicker.closeContainer();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void staleSessionsAndResearchAwayFromABlockAreRefused(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = standing(helper, "access_session", new BlockPos(4, 1, 2));
        carry(player, new ItemStack(PreparationItems.bundle(HerbCatalog.LAVENDER)));
        GrimoireMenu book = openBook(player);
        int revision = content(helper).revision();
        check(ReaderService.validMenu(player, book.containerId, book.nonce() + 1, revision).isEmpty(), "a wrong nonce was accepted");
        check(ReaderService.validMenu(player, book.containerId + 1, book.nonce(), revision).isEmpty(), "a wrong menu id was accepted");
        check(ReaderService.validMenu(player, book.containerId, book.nonce(), revision - 1).isEmpty(), "a stale revision was accepted");
        ReaderService.handle(player, new ReaderRequest(book.containerId, book.nonce(), revision, ReaderRequest.Op.RESEARCH,
                Optional.of(recipe("processing/steeping/recovery_tea"))));
        check(!DiscoveryService.knows(player, recipe("processing/steeping/recovery_tea")), "researched from a carried book");
        player.closeContainer();

        BlockPos shelf = new BlockPos(4, 1, 4);
        place(helper, GrimoireCatalog.BOOKSHELF, shelf);
        GrimoireMenu block = openBlock(helper, player, GrimoireCatalog.BOOKSHELF, shelf);
        check(block != null, "the bookshelf did not open");
        ReaderService.handle(player, new ReaderRequest(block.containerId, block.nonce() ^ 1, revision, ReaderRequest.Op.RESEARCH,
                Optional.of(recipe("processing/steeping/recovery_tea"))));
        check(!DiscoveryService.knows(player, recipe("processing/steeping/recovery_tea")), "researched with a forged session");
        EntryKey tea = recipe("processing/steeping/recovery_tea");
        check(ReaderService.research(player, block, content(helper), tea) != null && !DiscoveryService.knows(player, tea),
                "researched with no familiar ingredient");
        carry(player, new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE)));
        ReaderService.handle(player, new ReaderRequest(block.containerId, block.nonce(), revision, ReaderRequest.Op.RESEARCH,
                Optional.of(tea)));
        check(DiscoveryService.knows(player, tea), "one familiar ingredient at a bookshelf did not allow research");
        check(player.getInventory().countItem(PreparationItems.powder(HerbCatalog.CHAMOMILE)) == 1, "research consumed something");
        player.closeContainer();
        helper.succeed();
    }
}
