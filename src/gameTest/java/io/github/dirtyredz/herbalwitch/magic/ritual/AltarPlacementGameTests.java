package io.github.dirtyredz.herbalwitch.magic.ritual;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualItems;
import io.github.dirtyredz.herbalwitch.platform.placement.PlacementPlan;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacementItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * The altar through its real item and the shared checked placement: every facing places exactly the
 * plan the preview draws, obstruction and a missing floor refuse without placing or consuming, any
 * broken cell removes the whole altar with one drop (none in creative), and pistons cannot move it.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AltarPlacementGameTests {
    private static final BlockPos TARGET = new BlockPos(4, 1, 4);

    private AltarPlacementGameTests() {
    }

    private static void clear(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                for (int y = 1; y < 6; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
                }
            }
        }
    }

    /** The plan the client preview would draw for this player aiming at the floor under {@code target}. */
    private static PlacementPlan preview(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos target) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos clicked = helper.absolutePos(target.below());
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(clicked).relative(Direction.UP, 0.5), Direction.UP, clicked, false);
        BlockPlaceContext context = new BlockPlaceContext(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        return ((StructurePlacementItem) stack.getItem()).plan(context).orElseThrow();
    }

    @GameTest(template = TEMPLATE)
    public static void everyFacingPlacesExactlyThePreviewedPlan(GameTestHelper helper) {
        for (Direction facing : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
            clear(helper);
            floor(helper);
            ServerPlayer player = player(helper, "altar_place_" + facing.getName(), GameType.SURVIVAL, facing.getOpposite());
            ItemStack stack = new ItemStack(RitualItems.ALTAR.get(), 2);
            PlacementPlan plan = preview(helper, player, stack.copy(), TARGET);
            InteractionResult result = useItemOn(helper, player, stack, TARGET.below(), Direction.UP);
            check(result.consumesAction(), facing + ": " + result);
            check(stack.getCount() == 1, facing + ": exactly one item used");
            check(plan.cells().size() == 4 && plan.facing() == facing, facing + ": plan " + plan);
            for (PlacementPlan.Cell cell : plan.cells()) {
                BlockState placed = helper.getLevel().getBlockState(cell.pos());
                check(placed.equals(cell.state()), facing + " cell " + cell.index() + ": " + placed + " vs planned " + cell.state());
            }
            check(helper.getLevel().getBlockEntity(plan.origin()) != null, facing + ": the origin anchors the altar");
            check(!plan.originState().getValue(AltarBlock.ACTIVE), facing + ": placed idle");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void obstructionAndAMissingFloorRefuseWithoutPlacing(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "altar_refused", GameType.SURVIVAL, Direction.SOUTH);
        PlacementPlan plan = preview(helper, player, new ItemStack(RitualItems.ALTAR.get()), TARGET);
        BlockPos upper = plan.cells().get(3).pos();
        helper.getLevel().setBlock(upper, Blocks.STONE.defaultBlockState(), 3);
        ItemStack stack = new ItemStack(RitualItems.ALTAR.get());
        check(!useItemOn(helper, player, stack, TARGET.below(), Direction.UP).consumesAction(), "placed into an obstructed cell");
        check(stack.getCount() == 1 && !(helper.getLevel().getBlockState(plan.origin()).getBlock() instanceof AltarBlock), "a refused placement changed something");
        helper.getLevel().setBlock(upper, Blocks.AIR.defaultBlockState(), 3);
        helper.getLevel().setBlock(plan.cells().get(1).pos().below(), Blocks.AIR.defaultBlockState(), 3);
        check(!useItemOn(helper, player, stack, TARGET.below(), Direction.UP).consumesAction(), "placed with half its floor missing");
        check(stack.getCount() == 1, "the item was used by a refused placement");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingAnyCellRemovesTheAltarWithOneDrop(GameTestHelper helper) {
        Level level = helper.getLevel();
        for (int cell = 0; cell < 4; cell++) {
            clear(helper);
            floor(helper);
            clearDrops(helper);
            AltarBlock block = (AltarBlock) RitualBlocks.ALTAR.get();
            BlockPos origin = helper.absolutePos(TARGET);
            block.placeAltar(level, origin, Direction.NORTH, 3);
            BlockPos broken = AltarBlock.LAYOUT.cellPos(origin, Direction.NORTH, cell);
            level.destroyBlock(broken, true);
            for (int i = 0; i < 4; i++) {
                check(!(level.getBlockState(AltarBlock.LAYOUT.cellPos(origin, Direction.NORTH, i)).getBlock() instanceof AltarBlock),
                        "breaking cell " + cell + " left cell " + i);
            }
            check(dropped(helper, RitualItems.ALTAR.get()) == 1, "breaking cell " + cell + " dropped " + dropped(helper, RitualItems.ALTAR.get()));
        }
        clearDrops(helper);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void pistonsCannotMoveTheAltarAndItNeverOccludes(GameTestHelper helper) {
        BlockState state = RitualBlocks.ALTAR.get().defaultBlockState();
        check(state.getPistonPushReaction() == PushReaction.BLOCK, "pistons may push the altar");
        check(!state.canOcclude(), "the altar occludes its neighbours");
        helper.succeed();
    }
}
