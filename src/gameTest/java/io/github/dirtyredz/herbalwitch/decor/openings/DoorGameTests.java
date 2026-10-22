package io.github.dirtyredz.herbalwitch.decor.openings;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.openings.DecorDoorBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.OpeningsCatalog;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.click;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.useItemOn;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.droppedAny;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * The cottage door behaves as vanilla's: both halves placed together, opened by hand and by
 * redstone on both halves, vanilla's collision for each hinge and state, one drop from either half.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DoorGameTests {
    private static final BlockPos AT = new BlockPos(4, 1, 4);

    private DoorGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void theDoorOpensByHandAndRedstoneOnBothHalves(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "doorkeeper", GameType.SURVIVAL, Direction.SOUTH);
        ItemStack stack = new ItemStack(DecorItems.item(OpeningsCatalog.DOOR));
        check(useItemOn(helper, player, stack, AT.below(), Direction.UP).consumesAction() && stack.isEmpty(), "door not placed");
        BlockState lower = helper.getBlockState(AT);
        BlockState upper = helper.getBlockState(AT.above());
        check(lower.getBlock() instanceof DecorDoorBlock && lower.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER, "lower " + lower);
        check(upper.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER && upper.getValue(DoorBlock.HINGE) == lower.getValue(DoorBlock.HINGE),
                "upper " + upper);
        var closed = lower.getCollisionShape(helper.getLevel(), helper.absolutePos(AT), CollisionContext.empty()).bounds();
        check(click(helper, player, AT.above()).consumesAction(), "the upper half did not open the door");
        check(helper.getBlockState(AT).getValue(DoorBlock.OPEN) && helper.getBlockState(AT.above()).getValue(DoorBlock.OPEN), "not open");
        var open = helper.getBlockState(AT).getCollisionShape(helper.getLevel(), helper.absolutePos(AT), CollisionContext.empty()).bounds();
        check(!closed.equals(open), "collision did not swing with the door");
        check(click(helper, player, AT).consumesAction() && !helper.getBlockState(AT.above()).getValue(DoorBlock.OPEN), "not closed again");
        helper.setBlock(AT.east(), Blocks.REDSTONE_BLOCK);
        check(helper.getBlockState(AT).getValue(DoorBlock.OPEN) && helper.getBlockState(AT).getValue(DoorBlock.POWERED), "redstone did not open it");
        helper.setBlock(AT.east(), Blocks.AIR);
        check(!helper.getBlockState(AT.above()).getValue(DoorBlock.OPEN), "redstone did not close it");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingEitherHalfDropsOneDoor(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "door_breaker", GameType.SURVIVAL, Direction.SOUTH);
        for (BlockPos half : new BlockPos[] {AT, AT.above()}) {
            check(useItemOn(helper, player, new ItemStack(DecorItems.item(OpeningsCatalog.DOOR)), AT.below(), Direction.UP).consumesAction(),
                    "door not placed");
            clearDrops(helper);
            helper.getLevel().destroyBlock(helper.absolutePos(half), true);
            check(helper.getBlockState(AT).isAir() && helper.getBlockState(AT.above()).isAir(), "half a door left");
            check(dropped(helper, DecorItems.item(OpeningsCatalog.DOOR)) == 1 && droppedAny(helper) == 1, "dropped " + droppedAny(helper));
        }
        helper.succeed();
    }
}
