package io.github.dirtyredz.herbalwitch.fixture;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * Shared GameTest world setup and assertions: the empty template, a stone floor, and counting
 * dropped items. Adapted from Roundwood's DoorFixtures (same author, MIT).
 */
public final class WorldFixtures {
    /** 9 x 6 x 9 of air (src/gameTest/resources/data/herbalwitch/structures/empty.nbt). */
    public static final String TEMPLATE = "empty";

    private WorldFixtures() {
    }

    public static void check(boolean condition, String message) {
        if (!condition) {
            throw new GameTestAssertException(message);
        }
    }

    /** Stone at relative y = 0 across the 9 x 9 template; everything is built from y = 1. */
    public static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
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

    /**
     * Removes every non-player entity in this test's own area except {@code keep}. Never sweep a wider
     * radius: batches run side by side, and a wide sweep deletes a neighbouring test's mobs and drops.
     */
    public static void clearEntities(GameTestHelper helper, net.minecraft.world.entity.Entity keep) {
        helper.getLevel().getEntities(keep, area(helper), e -> !(e instanceof net.minecraft.world.entity.player.Player))
                .forEach(net.minecraft.world.entity.Entity::discard);
    }

    /**
     * Runs {@code body} once every chunk under the test plot is entity-ticking (bounded). A batch's plot
     * is force-loaded when it is placed, but the ticket takes effect a tick later; an entity added
     * before that sits in a hidden section, which {@code getEntity} and {@code getEntitiesOfClass}
     * never see (a harvest drop "vanishes", a spawned cow is "gone"). Tests that add or look up
     * entities on their first tick start through this.
     */
    public static void whenEntitiesLive(GameTestHelper helper, Runnable body) {
        whenEntitiesLive(helper, body, 40);
    }

    private static void whenEntitiesLive(GameTestHelper helper, Runnable body, int left) {
        if (entitiesLive(helper)) {
            body.run();
        } else {
            check(left > 0, "test plot never became entity-ticking");
            helper.runAfterDelay(1, () -> whenEntitiesLive(helper, body, left - 1));
        }
    }

    /** Whether entities anywhere in the test area are visible and ticking. */
    public static boolean entitiesLive(GameTestHelper helper) {
        AABB box = area(helper);
        for (int x = (int) Math.floor(box.minX) >> 4; x <= (int) Math.floor(box.maxX) >> 4; x++)
            for (int z = (int) Math.floor(box.minZ) >> 4; z <= (int) Math.floor(box.maxZ) >> 4; z++)
                if (!helper.getLevel().isPositionEntityTicking(new BlockPos(x << 4, (int) box.minY, z << 4))) return false;
        return true;
    }

    private static AABB area(GameTestHelper helper) {
        return new AABB(helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(9, 6, 9))).inflate(1);
    }
}
