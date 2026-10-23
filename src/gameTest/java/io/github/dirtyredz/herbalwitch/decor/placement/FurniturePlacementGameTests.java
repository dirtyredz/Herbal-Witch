package io.github.dirtyredz.herbalwitch.decor.placement;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.MountSpec.Support;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SeatingCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.StorageCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.droppedAny;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Checked structure placement through the real item: every delivered piece in every facing with its
 * support, the whole piece or nothing, cancelled placement rolled back, and the shared teardown
 * (any cell, support loss, explosion: one item; creative: none; pistons refused).
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FurniturePlacementGameTests {
    private static final List<Direction> HORIZONTAL = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST);
    private static final BlockPos TARGET = new BlockPos(4, 1, 4);

    private FurniturePlacementGameTests() {
    }

    /** Clears the test volume above the floor. */
    private static void clear(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                for (int y = 1; y < 6; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    /** The relative cells a piece would occupy with its anchor at {@code target}, facing {@code facing}. */
    static List<BlockPos> cells(FurnitureSpec spec, BlockPos target, Direction facing) {
        MultiblockLayout layout = FurnitureLayouts.of(spec);
        if (layout == null) {
            return List.of(target);
        }
        BlockPos origin = layout.origin(target, facing, FurnitureLayouts.anchorCell(spec));
        List<BlockPos> out = new ArrayList<>();
        for (int i = 0; i < layout.size(); i++) {
            out.add(layout.cellPos(origin, facing, i));
        }
        return out;
    }

    /**
     * Places a piece through its item so it faces {@code facing}, building the support it needs
     * first; returns the result.
     */
    static InteractionResult place(GameTestHelper helper, ServerPlayer player, FurnitureSpec spec, BlockPos target, Direction facing,
                                   ItemStack stack) {
        Support support = spec.support();
        List<BlockPos> cells = cells(spec, target, facing);
        MultiblockLayout layout = FurnitureLayouts.of(spec);
        Direction back = facing.getOpposite();
        switch (support) {
            case WALL, CORNER -> {
                for (int i = 0; i < cells.size(); i++) {
                    if (layout == null || layout.rear(i)) {
                        helper.setBlock(cells.get(i).relative(back), Blocks.STONE);
                        if (support == Support.CORNER) {
                            helper.setBlock(cells.get(i).relative(facing.getClockWise()), Blocks.STONE);
                        }
                    }
                }
                return useItemOn(helper, player, stack, target.relative(back), facing);
            }
            case CEILING -> {
                for (int i = 0; i < cells.size(); i++) {
                    if (layout == null || layout.top(i)) {
                        helper.setBlock(cells.get(i).above(), Blocks.STONE);
                    }
                }
                return useItemOn(helper, player, stack, target.above(), Direction.DOWN);
            }
            default -> {
                return useItemOn(helper, player, stack, target.below(), Direction.UP);
            }
        }
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void everyDeliveredPiecePlacesWholeInEveryFacing(GameTestHelper helper) {
        floor(helper);
        // Retained CI worlds now include naturally spawning familiars. This synchronous matrix
        // tests empty-space placement; entity obstruction has its own explicit test below.
        var volume=new net.minecraft.world.phys.AABB(helper.absolutePos(BlockPos.ZERO),helper.absolutePos(new BlockPos(9,8,9)));
        helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Entity.class,volume,
                entity->!(entity instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            Block block = DecorBlocks.block(spec);
            if (!(block instanceof StructurePlacement)) {
                continue; // the vanilla-carpet small rug
            }
            for (Direction facing : HORIZONTAL) {
                clear(helper);
                ServerPlayer player = player(helper, "place_" + spec.id(), GameType.SURVIVAL, facing.getOpposite());
                ItemStack stack = new ItemStack(block.asItem(), 2);
                BlockPos target = spec.support() == Support.CEILING ? new BlockPos(4, 4, 4) : TARGET; // hang below a ceiling at y 5
                InteractionResult result = place(helper, player, spec, target, facing, stack);
                check(result.consumesAction(), spec.id() + " facing " + facing + ": " + result);
                check(stack.getCount() == 1, spec.id() + " facing " + facing + ": used " + (2 - stack.getCount()));
                List<BlockPos> cells = cells(spec, target, facing);
                for (BlockPos cell : cells) {
                    BlockState state = helper.getBlockState(cell);
                    check(state.is(block), spec.id() + " facing " + facing + ": no cell at " + cell + " (" + state + ")");
                    if (block instanceof DecorAssemblyBlock assembly && spec.directional()) {
                        check(assembly.facing(state) == facing, spec.id() + " faces " + assembly.facing(state) + ", expected " + facing);
                    }
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void blockedOrUnsupportedPlacementsChangeNothing(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "refused", GameType.SURVIVAL, Direction.SOUTH);
        FurnitureSpec table = SurfacesCatalog.DINING_TABLE; // 3 x 2
        List<BlockPos> cells = cells(table, TARGET, Direction.NORTH);
        // a stone in the far cell
        helper.setBlock(cells.get(cells.size() - 1), Blocks.STONE);
        ItemStack stack = new ItemStack(DecorItems.item(table));
        check(!place(helper, player, table, TARGET, Direction.NORTH, stack).consumesAction() && stack.getCount() == 1, "placed over stone");
        check(cells.stream().limit(cells.size() - 1).allMatch(c -> helper.getBlockState(c).isAir()), "a partial table was placed");
        // a hole under one grounded cell
        helper.setBlock(cells.get(cells.size() - 1), Blocks.AIR);
        helper.setBlock(cells.get(1).below(), Blocks.AIR);
        check(!place(helper, player, table, TARGET, Direction.NORTH, stack).consumesAction() && stack.getCount() == 1, "placed over a hole");
        check(cells.stream().allMatch(c -> helper.getBlockState(c).isAir()), "a partial table was placed over a hole");
        helper.setBlock(cells.get(1).below(), Blocks.STONE);
        // an entity standing in a cell
        ArmorStand stand = new ArmorStand(EntityType.ARMOR_STAND, helper.getLevel());
        BlockPos at = helper.absolutePos(cells.get(2));
        stand.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
        helper.getLevel().addFreshEntity(stand);
        check(!place(helper, player, table, TARGET, Direction.NORTH, stack).consumesAction() && stack.getCount() == 1, "placed into an entity");
        stand.discard();
        // an adventure-mode player may not build
        ServerPlayer adventurer = player(helper, "adventure", GameType.ADVENTURE, Direction.SOUTH);
        check(!place(helper, adventurer, table, TARGET, Direction.NORTH, stack).consumesAction() && stack.getCount() == 1, "adventure placed");
        check(cells.stream().allMatch(c -> helper.getBlockState(c).isAir()), "something was left behind");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aCancelledPlacementIsRolledBackWithTheItem(GameTestHelper helper) {
        floor(helper);
        FurnitureSpec bench = SeatingCatalog.BENCH;
        List<BlockPos> cells = cells(bench, TARGET, Direction.NORTH);
        cells.forEach(c -> helper.setBlock(c.below(), Blocks.DIRT)); // grass needs soil to stand
        cells.forEach(c -> helper.setBlock(c, Blocks.GRASS)); // replaceable, must come back
        Consumer<BlockEvent.EntityMultiPlaceEvent> cancel = event -> event.setCanceled(true);
        MinecraftForge.EVENT_BUS.addListener(cancel);
        try {
            ServerPlayer player = player(helper, "cancelled", GameType.SURVIVAL, Direction.SOUTH);
            ItemStack stack = new ItemStack(DecorItems.item(bench));
            InteractionResult result = place(helper, player, bench, TARGET, Direction.NORTH, stack);
            check(!result.consumesAction(), "a cancelled placement reported " + result);
            check(stack.getCount() == 1, "the item was used up by a cancelled placement");
            for (BlockPos c : cells) {
                check(helper.getBlockState(c).is(Blocks.GRASS), "cell " + c + " not restored: " + helper.getBlockState(c));
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(cancel);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingAnyCellRemovesThePieceAndDropsItOnce(GameTestHelper helper) {
        floor(helper);
        FurnitureSpec spec = StorageCatalog.CHINA_CABINET; // 2 x 1 x 2
        ServerPlayer player = player(helper, "breaker", GameType.SURVIVAL, Direction.SOUTH);
        List<BlockPos> cells = cells(spec, TARGET, Direction.NORTH);
        for (int i = 0; i < cells.size(); i++) {
            clear(helper);
            check(place(helper, player, spec, TARGET, Direction.NORTH, new ItemStack(DecorItems.item(spec))).consumesAction(), "not placed");
            clearDrops(helper);
            helper.getLevel().destroyBlock(helper.absolutePos(cells.get(i)), true);
            for (BlockPos c : cells) {
                check(helper.getBlockState(c).isAir(), "breaking cell " + i + " left " + c);
            }
            check(dropped(helper, DecorItems.item(spec)) == 1 && droppedAny(helper) == 1,
                    "breaking cell " + i + " dropped " + droppedAny(helper));
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void creativeBreakingDropsNothing(GameTestHelper helper) {
        floor(helper);
        FurnitureSpec spec = SeatingCatalog.BENCH;
        ServerPlayer player = player(helper, "creative_breaker", GameType.CREATIVE, Direction.SOUTH);
        check(place(helper, player, spec, TARGET, Direction.NORTH, new ItemStack(DecorItems.item(spec))).consumesAction(), "not placed");
        List<BlockPos> cells = cells(spec, TARGET, Direction.NORTH);
        clearDrops(helper);
        player.gameMode.destroyBlock(helper.absolutePos(cells.get(1)));
        cells.forEach(c -> check(helper.getBlockState(c).isAir(), "left " + c));
        check(droppedAny(helper) == 0, "creative breaking dropped " + droppedAny(helper));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void losingSupportOrAnExplosionTearsDownOnce(GameTestHelper helper) {
        floor(helper);
        FurnitureSpec spec = SurfacesCatalog.KITCHEN_ISLAND; // 2 x 1
        ServerPlayer player = player(helper, "support", GameType.SURVIVAL, Direction.SOUTH);
        check(place(helper, player, spec, TARGET, Direction.NORTH, new ItemStack(DecorItems.item(spec))).consumesAction(), "not placed");
        List<BlockPos> cells = cells(spec, TARGET, Direction.NORTH);
        clearDrops(helper);
        helper.setBlock(cells.get(1).below(), Blocks.AIR);
        cells.forEach(c -> check(helper.getBlockState(c).isAir(), "unsupported island left " + c));
        check(dropped(helper, DecorItems.item(spec)) == 1, "support loss dropped " + dropped(helper, DecorItems.item(spec)));
        helper.setBlock(cells.get(1).below(), Blocks.STONE);
        check(place(helper, player, spec, TARGET, Direction.NORTH, new ItemStack(DecorItems.item(spec))).consumesAction(), "not placed again");
        clearDrops(helper);
        BlockPos abs = helper.absolutePos(cells.get(0));
        helper.getLevel().explode(null, abs.getX() + 0.5, abs.getY() + 0.5, abs.getZ() + 0.5, 1.5F, Level.ExplosionInteraction.BLOCK);
        cells.forEach(c -> check(helper.getBlockState(c).isAir(), "an explosion left " + c));
        check(dropped(helper, DecorItems.item(spec)) <= 1, "an explosion dropped " + dropped(helper, DecorItems.item(spec)));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void pistonsCannotMoveAssembledFurniture(GameTestHelper helper) {
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            if (spec.multiblock()) {
                check(DecorBlocks.block(spec).defaultBlockState().getPistonPushReaction() == PushReaction.BLOCK, spec.id() + " can be pushed");
            }
        }
        helper.succeed();
    }
}
