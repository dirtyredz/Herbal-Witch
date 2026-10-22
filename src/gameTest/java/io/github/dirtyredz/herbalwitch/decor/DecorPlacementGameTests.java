package io.github.dirtyredz.herbalwitch.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.behavior.DecorLightBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SeatingCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.StorageCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.LightingCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.TextilesCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Map;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.droppedAny;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.placeOnFloor;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;

/** Placement through the real item, facing, support rules, collision, neighbours and drops. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DecorPlacementGameTests {
    private static final List<Direction> HORIZONTAL = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);

    private DecorPlacementGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void freestandingPiecesFaceThePlacerInEveryOrientation(GameTestHelper helper) {
        floor(helper);
        int x = 1;
        for (FurnitureSpec spec : List.of(SeatingCatalog.ARMCHAIR, SurfacesCatalog.APOTHECARY_COUNTER)) {
            Block block = DecorBlocks.block(spec);
            int z = 1;
            for (Direction looking : HORIZONTAL) {
                ServerPlayer player = player(helper, spec.id() + "_" + looking, GameType.SURVIVAL, looking);
                ItemStack stack = new ItemStack(block.asItem(), 2);
                BlockPos target = new BlockPos(x, 1, z);
                InteractionResult result = useItemOn(helper, player, stack, target.below(), Direction.UP);
                check(result.consumesAction(), spec.id() + " looking " + looking + ": " + result);
                check(stack.getCount() == 1, spec.id() + ": one item consumed, have " + stack.getCount());
                BlockState state = helper.getBlockState(target);
                check(state.is(block), spec.id() + " not placed at " + target + ": " + state);
                check(state.getValue(HorizontalDirectionalBlock.FACING) == looking.getOpposite(),
                        spec.id() + " looking " + looking + " faces " + state.getValue(HorizontalDirectionalBlock.FACING));
                z += 2;
            }
            x += 3;
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void collisionTurnsWithTheFacing(GameTestHelper helper) {
        // The armchair's back sits opposite its front: north-facing has its back at the south edge.
        Map<Direction, double[]> back = Map.of(Direction.NORTH, new double[] {8, 14, 13.5}, Direction.EAST, new double[] {2.5, 14, 8},
                Direction.SOUTH, new double[] {8, 14, 2.5}, Direction.WEST, new double[] {13.5, 14, 8});
        Map<Direction, double[]> front = Map.of(Direction.NORTH, new double[] {8, 14, 2.5}, Direction.EAST, new double[] {13.5, 14, 8},
                Direction.SOUTH, new double[] {8, 14, 13.5}, Direction.WEST, new double[] {2.5, 14, 8});
        Block chair = DecorBlocks.block(SeatingCatalog.ARMCHAIR);
        BlockPos pos = new BlockPos(4, 1, 4);
        for (Direction facing : HORIZONTAL) {
            BlockState state = chair.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
            helper.setBlock(pos, state);
            BlockPos abs = helper.absolutePos(pos);
            VoxelShape collision = state.getCollisionShape(helper.getLevel(), abs, CollisionContext.empty());
            VoxelShape selection = state.getShape(helper.getLevel(), abs, CollisionContext.empty());
            check(contains(collision, back.get(facing)), facing + ": no back where expected");
            check(!contains(collision, front.get(facing)), facing + ": collision above the seat front");
            check(collision.toAabbs().equals(selection.toAabbs()), facing + ": selection differs from collision");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void wallShelfNeedsASturdyWallAndDropsWhenItGoes(GameTestHelper helper) {
        floor(helper);
        Item shelf = DecorItems.item(StorageCatalog.SMALL_WALL_SHELF);
        ServerPlayer player = player(helper, "shelf", GameType.SURVIVAL, Direction.SOUTH);
        // Clicking the floor in open space finds no wall: nothing placed, item kept.
        ItemStack stack = new ItemStack(shelf);
        InteractionResult open = useItemOn(helper, player, stack, new BlockPos(1, 0, 1), Direction.UP);
        check(!open.consumesAction() && stack.getCount() == 1, "placed without a wall: " + open);
        check(helper.getBlockState(new BlockPos(1, 1, 1)).isAir(), "shelf in open space");
        // Clicking the north face of a wall block hangs it facing north, away from the wall.
        BlockPos wall = new BlockPos(4, 2, 5);
        helper.setBlock(wall, Blocks.STONE);
        InteractionResult hung = useItemOn(helper, player, new ItemStack(shelf), wall, Direction.NORTH);
        BlockPos at = wall.north();
        check(hung.consumesAction(), "not hung on the wall: " + hung);
        check(helper.getBlockState(at).is(DecorBlocks.block(StorageCatalog.SMALL_WALL_SHELF)), "shelf missing");
        check(helper.getBlockState(at).getValue(HorizontalDirectionalBlock.FACING) == Direction.NORTH, "shelf faces the wall");
        // A wall without a sturdy face (a thin glass pane) does not hold it.
        helper.setBlock(new BlockPos(7, 2, 5), Blocks.GLASS_PANE);
        InteractionResult pane = useItemOn(helper, player, new ItemStack(shelf), new BlockPos(7, 2, 5), Direction.NORTH);
        check(!pane.consumesAction() && helper.getBlockState(new BlockPos(7, 2, 4)).isAir(), "hung on a glass pane");
        // Removing the wall breaks the shelf and drops it.
        clearDrops(helper);
        helper.setBlock(wall, Blocks.AIR);
        check(helper.getBlockState(at).isAir(), "shelf floats after its wall was removed");
        check(dropped(helper, shelf) == 1, "expected the shelf to drop once, dropped " + dropped(helper, shelf));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void lampNeedsASurfaceAndStandsOnTheCounter(GameTestHelper helper) {
        floor(helper);
        Item lamp = DecorItems.item(LightingCatalog.TABLE_LAMP);
        ServerPlayer player = player(helper, "lamp", GameType.SURVIVAL, Direction.NORTH);
        check(placeOnFloor(helper, player, lamp, new BlockPos(1, 1, 1)).consumesAction(), "lamp not placed on the floor");
        check(!helper.getBlockState(new BlockPos(1, 1, 1)).getValue(DecorLightBlock.LIT), "a new lamp is off");
        // On top of an apothecary counter.
        helper.setBlock(new BlockPos(4, 1, 4), DecorBlocks.block(SurfacesCatalog.APOTHECARY_COUNTER));
        check(placeOnFloor(helper, player, lamp, new BlockPos(4, 2, 4)).consumesAction(), "lamp not placed on the counter");
        check(helper.getBlockState(new BlockPos(4, 2, 4)).getBlock() instanceof DecorLightBlock, "lamp missing on the counter");
        // Against the side of a floating block, with air below: refused, item kept.
        helper.setBlock(new BlockPos(7, 3, 7), Blocks.STONE);
        ItemStack stack = new ItemStack(lamp);
        InteractionResult side = useItemOn(helper, player, stack, new BlockPos(7, 3, 7), Direction.WEST);
        check(!side.consumesAction() && stack.getCount() == 1 && helper.getBlockState(new BlockPos(6, 3, 7)).isAir(),
                "lamp placed over air: " + side);
        // Losing the floor breaks it and drops it.
        clearDrops(helper);
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.AIR);
        check(helper.getBlockState(new BlockPos(1, 1, 1)).isAir(), "lamp floats");
        check(dropped(helper, lamp) == 1, "lamp drops " + dropped(helper, lamp));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void rugNeedsAFloorAndDropsWhenItGoes(GameTestHelper helper) {
        floor(helper);
        Item rug = DecorItems.item(TextilesCatalog.SMALL_RUG);
        ServerPlayer player = player(helper, "rug", GameType.SURVIVAL, Direction.NORTH);
        check(placeOnFloor(helper, player, rug, new BlockPos(2, 1, 2)).consumesAction(), "rug not placed");
        check(helper.getBlockState(new BlockPos(2, 1, 2)).is(DecorBlocks.block(TextilesCatalog.SMALL_RUG)), "rug missing");
        clearDrops(helper);
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.AIR);
        check(helper.getBlockState(new BlockPos(2, 1, 2)).isAir(), "rug floats");
        check(dropped(helper, rug) == 1, "rug drops " + dropped(helper, rug));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void neighboursDoNotDisturbEachOther(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "neighbours", GameType.SURVIVAL, Direction.NORTH);
        List<BlockPos> row = List.of(new BlockPos(2, 1, 4), new BlockPos(3, 1, 4), new BlockPos(4, 1, 4), new BlockPos(5, 1, 4));
        List<Item> items = List.of(DecorItems.item(SeatingCatalog.ARMCHAIR),
                DecorItems.item(SeatingCatalog.ARMCHAIR),
                DecorItems.item(SurfacesCatalog.APOTHECARY_COUNTER),
                DecorItems.item(TextilesCatalog.SMALL_RUG));
        for (int i = 0; i < row.size(); i++) {
            check(placeOnFloor(helper, player, items.get(i), row.get(i)).consumesAction(), "placement " + i + " failed");
        }
        // A shelf on a wall behind the rug, beside the lamp on the counter.
        helper.setBlock(new BlockPos(5, 2, 5), Blocks.STONE);
        check(useItemOn(helper, player, new ItemStack(DecorItems.item(StorageCatalog.SMALL_WALL_SHELF)),
                new BlockPos(5, 2, 5), Direction.NORTH).consumesAction(), "shelf not hung");
        check(placeOnFloor(helper, player, DecorItems.item(LightingCatalog.TABLE_LAMP), new BlockPos(4, 2, 4)).consumesAction(),
                "lamp not placed on the counter");
        for (int i = 0; i < row.size(); i++) {
            check(helper.getBlockState(row.get(i)).is(Block.byItem(items.get(i))), "piece " + i + " disturbed");
        }
        check(droppedAny(helper) == 0, "something dropped");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingEveryPieceDropsItself(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(new BlockPos(4, 2, 8), Blocks.STONE);
        for (FurnitureSpec spec : FurnitureCatalog.withWave(FurnitureSpec.Wave.M1)) {
                Block block = DecorBlocks.block(spec);
                BlockPos pos = spec == StorageCatalog.SMALL_WALL_SHELF ? new BlockPos(4, 2, 7) : new BlockPos(2, 1, 2);
                helper.setBlock(pos, block);
                clearDrops(helper);
                helper.getLevel().destroyBlock(helper.absolutePos(pos), true); // GameTestHelper.destroyBlock drops nothing
                check(helper.getBlockState(pos).isAir(), spec.id() + " not broken");
                check(dropped(helper, block.asItem()) == 1 && droppedAny(helper) == 1,
                        spec.id() + " dropped " + dropped(helper, block.asItem()) + " own, " + droppedAny(helper) + " total");
            }
        helper.succeed();
    }

    private static boolean contains(VoxelShape shape, double[] pixel) {
        return shape.toAabbs().stream().anyMatch(box -> box.contains(pixel[0] / 16, pixel[1] / 16, pixel[2] / 16));
    }
}
