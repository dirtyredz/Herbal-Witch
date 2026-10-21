package io.github.dirtyredz.herbalwitch.data.server.preparations;

import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** A hung bundle drops the same bundle item the recipes use; the lantern drops itself (its oil is lost). */
public class PreparationBlockLoot extends BlockLootSubProvider {
    public PreparationBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        getKnownBlocks().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        List<Block> blocks = new ArrayList<>(PreparationBlocks.bundles());
        blocks.add(PreparationBlocks.OIL_LANTERN.get());
        return blocks;
    }
}
