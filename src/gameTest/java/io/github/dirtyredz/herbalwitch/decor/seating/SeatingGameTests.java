package io.github.dirtyredz.herbalwitch.decor.seating;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.seating.SeatEntity;
import io.github.dirtyredz.herbalwitch.decor.behavior.seating.SeatLayout;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SeatingCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Sitting: the aimed seat (never another), independent sitters on a multi-seat piece, a taken seat
 * refused without ejecting anyone, no headroom refused, and the seat gone when its sitter stands,
 * the furniture breaks, or the sitter dies.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class SeatingGameTests {
    private static final BlockPos ORIGIN = new BlockPos(4, 1, 4);

    private SeatingGameTests() {
    }

    /** Places a piece facing north with its origin at ORIGIN (cell by cell, origin first). */
    static void put(GameTestHelper helper, FurnitureSpec spec) {
        var block = DecorBlocks.block(spec);
        var layout = FurnitureLayouts.of(spec);
        BlockState origin = block.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH);
        if (layout == null) {
            helper.setBlock(ORIGIN, origin);
            return;
        }
        for (int i = 0; i < layout.size(); i++) {
            helper.setBlock(ORIGIN.offset(layout.offset(Direction.NORTH, i)), origin.setValue(layout.part(), i));
        }
    }

    /** Uses the piece aiming at a world point (absolute). */
    static InteractionResult sitAt(GameTestHelper helper, ServerPlayer player, Vec3 aim) {
        BlockPos block = BlockPos.containing(aim.x, aim.y - 0.01, aim.z);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                new BlockHitResult(aim, Direction.UP, block, false));
    }

    static List<Vec3> seats(GameTestHelper helper, FurnitureSpec spec) {
        return SeatLayout.world(spec, helper.absolutePos(ORIGIN), Direction.NORTH);
    }

    static long seatEntities(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(SeatEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO),
                helper.absolutePos(new BlockPos(9, 6, 9)))).stream().filter(SeatEntity::isAlive).count();
    }

    @GameTest(template = TEMPLATE)
    public static void sittingRidesAnInvisibleUnsavedSeat(GameTestHelper helper) {
        floor(helper);
        put(helper, SeatingCatalog.ARMCHAIR);
        ServerPlayer player = player(helper, "sitter", GameType.SURVIVAL, Direction.SOUTH);
        Vec3 seat = seats(helper, SeatingCatalog.ARMCHAIR).get(0);
        check(sitAt(helper, player, seat).consumesAction(), "could not sit");
        check(player.getVehicle() instanceof SeatEntity s && !s.shouldBeSaved() && s.isInvisible(), "not riding a seat: " + player.getVehicle());
        player.stopRiding();
        helper.startSequence().thenWaitUntil(() -> check(seatEntities(helper) == 0, "an empty seat stayed")).thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void aBenchSeatsTwoAndNeverEjectsAnyone(GameTestHelper helper) {
        floor(helper);
        FurnitureSpec bench = SeatingCatalog.BENCH;
        put(helper, bench);
        List<Vec3> seats = seats(helper, bench);
        check(seats.size() == 2, "a bench has two seats, not " + seats.size());
        ServerPlayer a = player(helper, "bench_a", GameType.SURVIVAL, Direction.SOUTH);
        ServerPlayer b = player(helper, "bench_b", GameType.SURVIVAL, Direction.SOUTH);
        ServerPlayer c = player(helper, "bench_c", GameType.SURVIVAL, Direction.SOUTH);
        check(sitAt(helper, a, seats.get(0)).consumesAction(), "a could not sit");
        check(sitAt(helper, b, seats.get(1)).consumesAction(), "b could not sit beside a");
        check(a.getVehicle() != b.getVehicle(), "two sitters share a seat");
        check(!sitAt(helper, c, seats.get(0)).consumesAction(), "c took a's seat");
        check(a.getVehicle() instanceof SeatEntity && c.getVehicle() == null, "a was ejected or c moved to another seat");
        a.stopRiding();
        b.stopRiding();
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void noHeadroomNoSeat(GameTestHelper helper) {
        floor(helper);
        put(helper, SeatingCatalog.DINING_CHAIR);
        helper.setBlock(ORIGIN.above(), Blocks.STONE);
        ServerPlayer player = player(helper, "cramped", GameType.SURVIVAL, Direction.SOUTH);
        check(!sitAt(helper, player, seats(helper, SeatingCatalog.DINING_CHAIR).get(0)).consumesAction(), "sat under a stone");
        check(player.getVehicle() == null && seatEntities(helper) == 0, "a seat was made");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingTheChairStandsTheSitterUp(GameTestHelper helper) {
        floor(helper);
        put(helper, SeatingCatalog.DINING_CHAIR);
        ServerPlayer player = player(helper, "unseated", GameType.SURVIVAL, Direction.SOUTH);
        check(sitAt(helper, player, seats(helper, SeatingCatalog.DINING_CHAIR).get(0)).consumesAction(), "could not sit");
        helper.getLevel().destroyBlock(helper.absolutePos(ORIGIN), false);
        helper.startSequence()
                .thenWaitUntil(() -> check(player.getVehicle() == null && seatEntities(helper) == 0, "still seated on a broken chair"))
                .thenSucceed();
    }

    @GameTest(template = TEMPLATE)
    public static void aSitterWhoDiesLeavesNoSeat(GameTestHelper helper) {
        floor(helper);
        put(helper, SeatingCatalog.ARMCHAIR);
        ServerPlayer player = player(helper, "mortal", GameType.SURVIVAL, Direction.SOUTH);
        check(sitAt(helper, player, seats(helper, SeatingCatalog.ARMCHAIR).get(0)).consumesAction(), "could not sit");
        MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(player, helper.getLevel().damageSources().genericKill()));
        helper.startSequence()
                .thenWaitUntil(() -> check(seatEntities(helper) == 0, "a dead sitter's seat stayed"))
                .thenSucceed();
    }
}
