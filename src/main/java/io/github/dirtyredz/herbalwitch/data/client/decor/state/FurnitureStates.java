package io.github.dirtyredz.herbalwitch.data.client.decor.state;

import io.github.dirtyredz.herbalwitch.data.client.decor.DecorModelProvider;
import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

/** One-cell pieces (any behaviour but light, bed and door): one model, turned to the facing; the small rug has none. */
public final class FurnitureStates {
    private FurnitureStates() {
    }

    public static void add(DecorModelProvider provider, FurnitureSpec spec, Block block) {
        ModelFile model = provider.existing(DecorModelProvider.cellModel(spec, "base"));
        if (!spec.directional()) {
            provider.simpleBlock(block, model);
            return;
        }
        provider.getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(model)
                .rotationY(DecorModelProvider.rotationY(state.getValue(DecorAssemblyBlock.FACING))).build());
    }
}
