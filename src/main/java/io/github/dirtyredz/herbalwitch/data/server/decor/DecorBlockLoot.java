package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
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
 * Every furniture block drops itself once (explosion rules apply): a one-cell piece from its block,
 * a multiblock only from its origin cell (cell 0), the door from its lower half, so breaking any
 * part of a piece yields exactly one item.
 */
public class DecorBlockLoot extends BlockLootSubProvider {
    public DecorBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            FurnitureSpec spec = entry.spec();
            Block block = entry.block().get();
            if (spec.behavior() == FurnitureBehavior.DOOR) {
                add(block, createDoorTable(block));
            } else if (spec.multiblock()) {
                add(block, originOnly(block, FurnitureLayouts.of(spec).part()));
            } else {
                dropSelf(block);
            }
        }
    }

    /** The block itself, only from cell 0 (explosion rules apply). */
    private LootTable.Builder originOnly(Block block, IntegerProperty cell) {
        return LootTable.lootTable().withPool(applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(cell, 0))))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return DecorBlocks.all();
    }
}
