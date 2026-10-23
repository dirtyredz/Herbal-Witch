package io.github.dirtyredz.herbalwitch.data.server.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import java.util.Set;

/** Empty home items never carry an instance, owner, assignment or inventory. */
public final class FamiliarHomeLoot extends BlockLootSubProvider {
    public FamiliarHomeLoot() { super(Set.of(), FeatureFlags.REGISTRY.allFlags()); }
    @Override protected void generate() { FamiliarHomes.ALL.forEach(e -> dropSelf(e.block().get())); }
    @Override protected Iterable<Block> getKnownBlocks() { return FamiliarHomes.ALL.stream().<Block>map(e -> e.block().get()).toList(); }
}
