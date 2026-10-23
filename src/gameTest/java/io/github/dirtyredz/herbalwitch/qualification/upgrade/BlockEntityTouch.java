package io.github.dirtyredz.herbalwitch.qualification.upgrade;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Opens the block entity of every Herbal Witch block that should have one, as a player's first use
 * would. A block saved before its block entity existed (the pre-0.4.0 small wall shelf) gets an
 * empty one: its decorative bottles must never become inventory.
 */
final class BlockEntityTouch {
    private BlockEntityTouch() {
    }

    static void touch(ServerLevel level, List<LevelChunk> chunks, UpgradeReport report) {
        int existing = 0;
        for (LevelChunk chunk : chunks) {
            LevelChunkSection[] sections = chunk.getSections();
            for (int index = 0; index < sections.length; index++) {
                LevelChunkSection section = sections[index];
                if (section.hasOnlyAir() || !section.maybeHas(BlockEntityTouch::ours)) continue;
                int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
                for (int y = 0; y < 16; y++) for (int z = 0; z < 16; z++) for (int x = 0; x < 16; x++) {
                    BlockState state = section.getBlockState(x, y, z);
                    if (!ours(state) || !state.hasBlockEntity()) continue;
                    BlockPos pos = new BlockPos(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z);
                    boolean saved = chunk.getBlockEntities().containsKey(pos);
                    BlockEntity entity = level.getBlockEntity(pos);
                    if (saved) {
                        existing++;
                    } else if (entity != null) {
                        created(entity, pos, report);
                    }
                }
            }
        }
        report.put("existingBlockEntities", existing);
    }

    private static void created(BlockEntity entity, BlockPos pos, UpgradeReport report) {
        int stored = stored(entity);
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("pos", pos.getX() + " " + pos.getY() + " " + pos.getZ());
        entry.put("type", String.valueOf(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType())));
        entry.put("storedItems", stored);
        report.created(entry);
        if (stored != 0) report.fail("created block entity at " + entry.get("pos") + " materialized " + stored + " items");
    }

    private static int stored(BlockEntity entity) {
        int[] count = {0};
        entity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for (int slot = 0; slot < handler.getSlots(); slot++) count[0] += handler.getStackInSlot(slot).getCount();
        });
        if (entity instanceof Container container) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) count[0] += container.getItem(slot).getCount();
        }
        return count[0];
    }

    private static boolean ours(BlockState state) {
        return HerbalWitch.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace());
    }
}
