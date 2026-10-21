package io.github.dirtyredz.herbalwitch.data.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlock;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Station blockstates over the authored models ({@code models/block/stations/<station>/base}, and
 * {@code active} for the rack holding herbs and the burning hearth), turned to the block's facing.
 */
public class StationModelProvider extends BlockStateProvider {
    public StationModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch station blockstates";
    }

    @Override
    protected void registerStatesAndModels() {
        for (StationSpec spec : StationCatalog.ALL) {
            ModelFile base = models().getExistingFile(modLoc("block/stations/" + spec.directory() + "/base"));
            ModelFile active = spec.activeModel() ? models().getExistingFile(modLoc("block/stations/" + spec.directory() + "/active")) : base;
            getVariantBuilder(StationBlocks.block(spec)).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(isActive(state) ? active : base)
                    .rotationY(rotation(state.getValue(StationBlock.FACING))).build());
        }
    }

    static boolean isActive(BlockState state) {
        return (state.hasProperty(DryingRackBlock.ACTIVE) && state.getValue(DryingRackBlock.ACTIVE))
                || (state.hasProperty(HearthBlock.LIT) && state.getValue(HearthBlock.LIT));
    }

    /** Models face north; the blockstate turns them clockwise to the facing. */
    static int rotation(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }
}
