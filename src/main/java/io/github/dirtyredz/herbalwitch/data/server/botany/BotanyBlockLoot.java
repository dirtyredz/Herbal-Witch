package io.github.dirtyredz.herbalwitch.data.server.botany;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbHarvest;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Breaking a herb: always one seed; a mature plant also gives the fresh herbs of a harvest
 * ({@link HerbHarvest#BASE_FRESH}). The bed drops itself.
 */
public class BotanyBlockLoot extends BlockLootSubProvider {
    public BotanyBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(BotanyBlocks.herbBed());
        for (HerbSpec herb : HerbCatalog.ALL) {
            HerbCropBlock plant = BotanyBlocks.plant(herb);
            LootItemBlockStatePropertyCondition.Builder mature = LootItemBlockStatePropertyCondition.hasBlockStateProperties(plant)
                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(HerbCropBlock.AGE, HerbSpec.MAX_AGE));
            add(plant, applyExplosionDecay(plant, LootTable.lootTable()
                    .withPool(LootPool.lootPool().add(LootItem.lootTableItem(BotanyItems.seeds(herb))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(HerbHarvest.BASE_SEEDS)))))
                    .withPool(LootPool.lootPool().when(mature).add(LootItem.lootTableItem(BotanyItems.fresh(herb))
                            .apply(SetItemCountFunction.setCount(ConstantValue.exactly(HerbHarvest.BASE_FRESH)))))));
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> blocks = new ArrayList<>(BotanyBlocks.plants());
        blocks.add(BotanyBlocks.herbBed());
        return blocks;
    }
}
