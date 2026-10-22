package io.github.dirtyredz.herbalwitch.data.server.magic.ward;

import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

/** A broken ward stone drops itself, empty: its charge and binding stay with the retired record, never with the item. */
public class WardBlockLoot extends BlockLootSubProvider {
    public WardBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(WardRegistration.STONE.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return List.of(WardRegistration.STONE.get());
    }
}
