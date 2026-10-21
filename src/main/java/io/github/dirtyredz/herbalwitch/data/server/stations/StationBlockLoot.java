package io.github.dirtyredz.herbalwitch.data.server.stations;

import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;

/**
 * Every station drops itself; its stored items drop separately when it is removed. A multiblock
 * station drops once, from its origin cell ({@code part=0}).
 */
public class StationBlockLoot extends BlockLootSubProvider {
    public StationBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        for (Block block : StationBlocks.all()) {
            if (block instanceof StationBlock station && station.layout() != null) {
                add(block, originOnly(block, station.layout().part()));
            } else {
                dropSelf(block);
            }
        }
    }

    /** Drops the block only from cell 0 (like a door's lower half), surviving explosions as dropSelf does. */
    private LootTable.Builder originOnly(Block block, IntegerProperty part) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(part, 0))))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return StationBlocks.all();
    }
}
