package io.github.dirtyredz.herbalwitch.data.server.magic.ritual;

import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Set;

/** The altar and the circle each drop one item, from their origin cell only ({@code part=0}), surviving explosions like dropSelf. */
public class AltarBlockLoot extends BlockLootSubProvider {
    public AltarBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        Block altar = RitualBlocks.ALTAR.get();
        add(altar, LootTable.lootTable().withPool(applyExplosionCondition(altar, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(altar).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(altar)
                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(AltarBlock.LAYOUT.part(), 0)))))));
        Block circle = RitualBlocks.CIRCLE.get();
        add(circle, LootTable.lootTable().withPool(applyExplosionCondition(circle, LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(circle).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(circle)
                        .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(RitualCircleBlock.LAYOUT.part(), 0)))))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(RitualBlocks.ALTAR.get(), RitualBlocks.CIRCLE.get());
    }
}
