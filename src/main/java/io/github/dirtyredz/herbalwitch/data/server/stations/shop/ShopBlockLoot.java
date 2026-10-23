package io.github.dirtyredz.herbalwitch.data.server.stations.shop;

import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/** Each commerce piece drops its plain item: never a shop id, binding, stock or proceeds. */
public final class ShopBlockLoot extends BlockLootSubProvider {
    public ShopBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        ShopBlocks.all().forEach(this::dropSelf);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ShopBlocks.all().stream().<Block>map(b -> b).toList();
    }
}
