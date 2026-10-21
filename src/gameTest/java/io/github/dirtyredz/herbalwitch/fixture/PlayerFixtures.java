package io.github.dirtyredz.herbalwitch.fixture;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Deterministic fake players and interaction through the real item and block paths, so Forge's
 * hooks run. Adapted from Roundwood's DoorFixtures (same author, MIT).
 */
public final class PlayerFixtures {
    private PlayerFixtures() {
    }

    /** A fake player in {@code mode} looking horizontally towards {@code facing}, with an empty inventory. */
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
        return clickWith(helper, player, ItemStack.EMPTY, relative);
    }

    /** Clicks a block holding {@code stack} through the real server interaction path (block use first, then item). */
    public static InteractionResult clickWith(GameTestHelper helper, ServerPlayer player, ItemStack stack, BlockPos relative) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos abs = helper.absolutePos(relative);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        return player.gameMode.useItemOn(player, helper.getLevel(), stack, InteractionHand.MAIN_HAND, hit);
    }
}
