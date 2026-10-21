package io.github.dirtyredz.herbalwitch.fixture;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Shared GameTest setup: a stone floor, deterministic fake players, and placement through the real
 * item path so Forge's placement hooks run. Adapted from Roundwood's DoorFixtures (same author, MIT).
 */
public final class DecorFixtures {
    /** 9 x 6 x 9 of air (src/gameTest/resources/data/herbalwitch/structures/empty.nbt). */
    public static final String TEMPLATE = "empty";

    private DecorFixtures() {
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    /** Stone at relative y = 0 across the template; everything is built from y = 1. */
    public static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    /** A fake survival/creative player looking horizontally towards {@code facing}, with an empty inventory. */
    public static ServerPlayer player(GameTestHelper helper, String name, GameType mode, Direction facing) {
        ServerLevel level = helper.getLevel();
        UUID id = UUID.nameUUIDFromBytes(("herbalwitch:" + name).getBytes(StandardCharsets.UTF_8));
        ServerPlayer player = FakePlayerFactory.get(level, new GameProfile(id, "hw_" + name));
        player.getInventory().clearContent();
        player.setGameMode(mode);
        BlockPos stand = helper.absolutePos(new BlockPos(0, 4, 0));
        player.moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, facing.toYRot(), 0);
        player.setYRot(facing.toYRot());
        player.setYHeadRot(facing.toYRot());
        player.setXRot(0);
        return player;
    }

    /** Uses {@code stack} on a face of the block at {@code clicked} (relative) through {@link ItemStack#useOn}. */
    public static InteractionResult useItemOn(GameTestHelper helper, ServerPlayer player, ItemStack stack,
                                              BlockPos clicked, Direction face) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos abs = helper.absolutePos(clicked);
        Vec3 hit = Vec3.atCenterOf(abs).relative(face, 0.5);
        return stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, new BlockHitResult(hit, face, abs, false)));
    }

    /** Places one {@code item} on top of the block below {@code target} (relative). */
    public static InteractionResult placeOnFloor(GameTestHelper helper, ServerPlayer player, Item item, BlockPos target) {
        return useItemOn(helper, player, new ItemStack(item), target.below(), Direction.UP);
    }

    /** Clicks a block with an empty hand through the real server interaction path. */
    public static InteractionResult click(GameTestHelper helper, ServerPlayer player, BlockPos relative) {
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        BlockPos abs = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        return player.gameMode.useItemOn(player, helper.getLevel(), ItemStack.EMPTY, InteractionHand.MAIN_HAND, hit);
    }

    /** Total count of dropped {@code item} within the test area. */
    public static int dropped(GameTestHelper helper, Item item) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, area(helper), entity -> entity.getItem().is(item))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    /** Total count of every dropped item within the test area. */
    public static int droppedAny(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, area(helper))
                .stream().mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    /** Removes every dropped item in the area, so a test can count from zero. */
    public static void clearDrops(GameTestHelper helper) {
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, area(helper)).forEach(ItemEntity::discard);
    }

    private static AABB area(GameTestHelper helper) {
        return new AABB(helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(9, 6, 9))).inflate(1);
    }
}
