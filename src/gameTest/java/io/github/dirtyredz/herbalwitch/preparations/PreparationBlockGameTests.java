package io.github.dirtyredz.herbalwitch.preparations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.lighting.OilLanternBlock;
import io.github.dirtyredz.herbalwitch.brewing.product.lighting.OilLanternBlockEntity;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlocks;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.clickWith;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/** Hung bundles and the oil lantern. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PreparationBlockGameTests {
    private PreparationBlockGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void bundlesHangOnWallsAndCeilingsAndDropThemselves(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "hanger", GameType.SURVIVAL, Direction.NORTH);
        ItemStack bundle = new ItemStack(PreparationItems.bundle(HerbCatalog.LAVENDER), 3);
        check(!useItemOn(helper, player, bundle, new BlockPos(1, 0, 1), Direction.UP).consumesAction(), "hung on a floor");
        BlockPos ceiling = new BlockPos(4, 4, 4);
        helper.setBlock(ceiling, Blocks.OAK_PLANKS);
        check(useItemOn(helper, player, bundle, ceiling, Direction.DOWN).consumesAction(), "not hung from the ceiling");
        check(helper.getBlockState(ceiling.below()).getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.CEILING, "ceiling face");
        BlockPos wall = new BlockPos(6, 2, 6);
        helper.setBlock(wall, Blocks.OAK_PLANKS);
        check(useItemOn(helper, player, bundle, wall, Direction.NORTH).consumesAction(), "not hung on the wall");
        check(helper.getBlockState(wall.north()).getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.WALL, "wall face");
        clearDrops(helper);
        helper.setBlock(ceiling, Blocks.AIR);
        check(dropped(helper, PreparationItems.bundle(HerbCatalog.LAVENDER)) == 1, "bundle did not drop itself when its support went");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theLanternTakesOneBottleBurnsAndGoesOut(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "lamplighter", GameType.SURVIVAL, Direction.NORTH);
        BlockPos at = new BlockPos(4, 1, 4);
        helper.setBlock(at, PreparationBlocks.OIL_LANTERN.get());
        ItemStack oil = new ItemStack(PreparationItems.product(EverydayProductCatalog.LANTERN_OIL), 2);
        InteractionResult fill = clickWith(helper, player, oil, at);
        check(fill.consumesAction(), "lantern refused oil: " + fill);
        check(helper.getBlockState(at).getValue(OilLanternBlock.LIT), "not lit");
        check(player.getMainHandItem().getCount() == 1 && player.getInventory().countItem(Items.GLASS_BOTTLE) == 1, "oil or bottle wrong");
        InteractionResult refill = clickWith(helper, player, player.getMainHandItem(), at);
        check(!refill.consumesAction() && player.getMainHandItem().getCount() == 1, "refilled while burning");
        OilLanternBlockEntity lantern = (OilLanternBlockEntity) helper.getBlockEntity(at);
        CompoundTag saved = lantern.saveWithoutMetadata();
        check(saved.getInt("Fuel") == OilLanternBlockEntity.FUEL_TICKS, "fuel not saved");
        lantern.setFuel(1);
        helper.startSequence()
                .thenWaitUntil(() -> check(!helper.getBlockState(at).getValue(OilLanternBlock.LIT), "lantern never went out"))
                .thenExecute(() -> check(clickWith(helper, player, player.getMainHandItem(), at).consumesAction(), "empty lantern refused oil"))
                .thenSucceed();
    }
}
