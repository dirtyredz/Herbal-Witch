package io.github.dirtyredz.herbalwitch.data.client.decor.state;

import io.github.dirtyredz.herbalwitch.data.client.decor.DecorModelProvider;
import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;

/** Multiblock pieces: each cell shows its compiled {@code part_<n>}, turned to the structure's facing. */
public final class AssemblyStates {
    private AssemblyStates() {
    }

    public static void add(DecorModelProvider provider, FurnitureSpec spec, Block block) {
        MultiblockLayout layout = FurnitureLayouts.of(spec);
        provider.getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(provider.existing(DecorModelProvider.partModel(spec, "part", state.getValue(layout.part()))))
                .rotationY(DecorModelProvider.rotationY(state.getValue(DecorAssemblyBlock.FACING))).build());
    }
}
