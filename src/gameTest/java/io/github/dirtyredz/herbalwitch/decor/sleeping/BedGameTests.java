package io.github.dirtyredz.herbalwitch.decor.sleeping;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.sleeping.BedLayout;
import io.github.dirtyredz.herbalwitch.decor.behavior.sleeping.DecorBedBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.FixturesCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.droppedAny;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Beds through vanilla's sleeping: placed whole with vanilla head/foot identity, actual sleep and
 * respawn at night, one sleeper per bed (an occupied bed refuses a second), the canopy's frame
 * cells routing to the pair, and breaking any cell waking the sleeper and dropping the bed once.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BedGameTests {
    private static final BlockPos FOOT = new BlockPos(4, 1, 2);

    private BedGameTests() {
    }

    /** Places a bed through its item with the head toward the south (the player looks south). */
    static ServerPlayer placeBed(GameTestHelper helper, FurnitureSpec spec, String name) {
        ServerPlayer player = player(helper, name, GameType.SURVIVAL, Direction.SOUTH);
        ItemStack stack = new ItemStack(DecorItems.item(spec));
        InteractionResult placed = useItemOn(helper, player, stack, FOOT.below(), Direction.UP);
        check(placed.consumesAction(), spec.id() + " not placed: " + placed);
        return player;
    }

    static List<BlockPos> cells(FurnitureSpec spec) {
        MultiblockLayout layout = FurnitureLayouts.of(spec);
        List<BlockPos> out = new ArrayList<>();
        for (int i = 0; i < layout.size(); i++) {
            out.add(layout.cellPos(FOOT, Direction.NORTH, i));
        }
        return out;
    }

    static InteractionResult use(GameTestHelper helper, ServerPlayer player, BlockPos relative) {
        BlockPos abs = helper.absolutePos(relative);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
    }

    @GameTest(template = TEMPLATE)
    public static void bedsArePlacedWithVanillaHeadAndFoot(GameTestHelper helper) {
        floor(helper);
        for (FurnitureSpec spec : List.of(FixturesCatalog.BED, FixturesCatalog.CANOPY_BED)) {
            for (BlockPos c : cells(spec)) {
                for (int y = 0; y < 3; y++) {
                    helper.setBlock(c.above(y), net.minecraft.world.level.block.Blocks.AIR);
                }
            }
            placeBed(helper, spec, "bed_" + spec.id());
            MultiblockLayout layout = FurnitureLayouts.of(spec);
            BlockState foot = helper.getBlockState(FOOT);
            BlockState head = helper.getBlockState(FOOT.south());
            check(foot.getBlock() instanceof DecorBedBlock && foot.getValue(BedBlock.PART) == net.minecraft.world.level.block.state.properties.BedPart.FOOT,
                    spec.id() + " foot " + foot);
            check(head.getValue(BedBlock.PART) == net.minecraft.world.level.block.state.properties.BedPart.HEAD && head.getValue(BedBlock.FACING) == Direction.SOUTH,
                    spec.id() + " head " + head);
            for (int i = 0; i < layout.size(); i++) {
                BlockState cell = helper.getBlockState(cells(spec).get(i));
                check(cell.is(DecorBlocks.block(spec)), spec.id() + " cell " + i + " missing");
                check(cell.isBed(helper.getLevel(), helper.absolutePos(cells(spec).get(i)), null) == BedLayout.sleepingPair(layout, i),
                        spec.id() + " cell " + i + " bed identity");
            }
            helper.getLevel().destroyBlock(helper.absolutePos(FOOT), false);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void sleepingSetsTheSpawnAndOneSleeperOnly(GameTestHelper helper) {
        floor(helper);
        long time = helper.getLevel().getDayTime();
        ServerPlayer sleeper = placeBed(helper, FixturesCatalog.CANOPY_BED, "sleeper");
        ServerPlayer second = player(helper, "second_sleeper", GameType.SURVIVAL, Direction.SOUTH);
        BlockPos beside = helper.absolutePos(new BlockPos(6, 1, 3)); // within vanilla's bed reach
        sleeper.moveTo(beside.getX() + 0.5, beside.getY(), beside.getZ() + 0.5);
        second.moveTo(beside.getX() + 0.5, beside.getY(), beside.getZ() + 1.5);
        helper.getLevel().setDayTime(18000);
        helper.startSequence()
                .thenIdle(2) // the sky darkens on the next tick
                .thenExecute(() -> {
                    // aim at a frame cell: it routes to the pair
                    BlockPos frame = cells(FixturesCatalog.CANOPY_BED).get(1);
                    check(use(helper, sleeper, frame).consumesAction(), "the frame did not route to the bed");
                    check(sleeper.isSleeping(), "not asleep");
                    check(helper.absolutePos(FOOT.south()).equals(sleeper.getRespawnPosition()), "respawn " + sleeper.getRespawnPosition());
                    check(helper.getBlockState(FOOT).getValue(BedBlock.OCCUPIED), "the foot does not show the bed occupied");
                    use(helper, second, FOOT);
                    check(!second.isSleeping(), "two sleepers in one bed");
                    // breaking any cell wakes the sleeper and drops the bed once
                    clearDrops(helper);
                    helper.getLevel().destroyBlock(helper.absolutePos(cells(FixturesCatalog.CANOPY_BED).get(11)), true);
                    cells(FixturesCatalog.CANOPY_BED).forEach(c -> check(helper.getBlockState(c).isAir(), "the canopy left " + c));
                    check(dropped(helper, DecorItems.item(FixturesCatalog.CANOPY_BED)) == 1 && droppedAny(helper) == 1,
                            "dropped " + droppedAny(helper));
                })
                // fake players do not tick, so check what vanilla's living tick checks to wake a sleeper
                .thenExecute(() -> sleeper.getSleepingPos().ifPresent(p -> check(!helper.getLevel().getBlockState(p)
                        .isBed(helper.getLevel(), p, sleeper), "the sleeping place is still a bed")))
                .thenExecute(() -> helper.getLevel().setDayTime(time))
                .thenSucceed();
    }
}
