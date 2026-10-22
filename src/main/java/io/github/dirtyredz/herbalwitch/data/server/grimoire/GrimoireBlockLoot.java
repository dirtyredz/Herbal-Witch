package io.github.dirtyredz.herbalwitch.data.server.grimoire;

import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/** Each research block drops itself once; it holds nothing else. */
public class GrimoireBlockLoot extends BlockLootSubProvider {
    public GrimoireBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        GrimoireBlocks.all().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return GrimoireBlocks.all();
    }
}
