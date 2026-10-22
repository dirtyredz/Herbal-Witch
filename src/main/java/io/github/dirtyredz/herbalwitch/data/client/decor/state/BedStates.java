package io.github.dirtyredz.herbalwitch.data.client.decor.state;

import io.github.dirtyredz.herbalwitch.data.client.decor.DecorModelProvider;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.FurnitureLayouts;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;

/**
 * Beds: every cell shows its compiled {@code part_<cell>} (static art, no vanilla bed renderer). A
 * bed's {@code facing} is vanilla's (foot to head); the models are drawn with the foot at the front
 * (z 0) and the head toward the back, so a bed facing south needs no turn.
 */
public final class BedStates {
    private BedStates() {
    }

    public static void add(DecorModelProvider provider, FurnitureSpec spec, Block block) {
        MultiblockLayout layout = FurnitureLayouts.of(spec);
        provider.getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(provider.existing(DecorModelProvider.partModel(spec, "part", state.getValue(layout.part()))))
                .rotationY(DecorModelProvider.rotationY(state.getValue(BedBlock.FACING).getOpposite())).build());
    }
}
