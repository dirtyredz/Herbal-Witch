package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/** Every furniture block drops itself in its own finish (explosion rules apply). */
public class DecorBlockLoot extends BlockLootSubProvider {
    public DecorBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        DecorBlocks.all().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return DecorBlocks.all();
    }
}
