package io.github.dirtyredz.herbalwitch.data.client.decor.state;

import io.github.dirtyredz.herbalwitch.data.client.decor.DecorModelProvider;
import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.decor.behavior.DecorLightBlock;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;

/**
 * Lights switch models with {@code lit}: a one-cell lamp between {@code off} and {@code on}, a
 * multiblock between {@code part_<n>} and {@code lit_<n>} (the same geometry with its glowing groups
 * emissive). The 0.1.0 table lamp has no facing.
 */
public final class LightStates {
    private LightStates() {
    }

    public static void add(DecorModelProvider provider, FurnitureSpec spec, Block block) {
        MultiblockLayout layout = FurnitureLayouts.of(spec);
        provider.getVariantBuilder(block).forAllStates(state -> {
            boolean lit = state.getValue(DecorLightBlock.LIT);
            String model = layout == null ? DecorModelProvider.cellModel(spec, lit ? "on" : "off")
                    : DecorModelProvider.partModel(spec, lit ? "lit" : "part", state.getValue(layout.part()));
            ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(provider.existing(model));
            if (spec.directional()) {
                builder.rotationY(DecorModelProvider.rotationY(state.getValue(DecorAssemblyBlock.FACING)));
            }
            return builder.build();
        });
    }
}
