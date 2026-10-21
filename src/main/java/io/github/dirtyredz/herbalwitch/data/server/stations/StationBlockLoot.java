package io.github.dirtyredz.herbalwitch.data.server.stations;

import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/** Every station drops itself; its stored items drop separately when it is removed. */
public class StationBlockLoot extends BlockLootSubProvider {
    public StationBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        StationBlocks.all().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return StationBlocks.all();
    }
}
