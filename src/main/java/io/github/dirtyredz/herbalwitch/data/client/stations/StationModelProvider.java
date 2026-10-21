package io.github.dirtyredz.herbalwitch.data.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.platform.multiblock.MultiblockLayout;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlock;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Station blockstates over the authored models ({@code models/block/stations/<station>/}), turned
 * to the block's facing. A one-block station selects {@code base}. A multiblock station is
 * multipart: each cell shows its permanent {@code part_<n>} and, while lit or active, its overlay
 * ({@code fire_<n>} for the hearth and stove, {@code bundles_<n>} for the rack) where one exists.
 */
public class StationModelProvider extends BlockStateProvider {
    private final ExistingFileHelper existing;

    public StationModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
        this.existing = existingFiles;
    }

    @Override
    public String getName() {
        return "Herbal Witch station blockstates";
    }

    @Override
    protected void registerStatesAndModels() {
        for (StationSpec spec : StationCatalog.ALL) {
            Block block = StationBlocks.block(spec);
            if (block instanceof StationBlock station && station.layout() != null) {
                multiblock(spec, block, station.layout());
            } else {
                ModelFile base = model(spec, "base");
                getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                        .modelFile(base).rotationY(rotation(state.getValue(StationBlock.FACING))).build());
            }
        }
    }

    private void multiblock(StationSpec spec, Block block, MultiblockLayout layout) {
        BooleanProperty shown = block.defaultBlockState().hasProperty(DryingRackBlock.ACTIVE) ? DryingRackBlock.ACTIVE : HearthBlock.LIT;
        String overlay = shown == DryingRackBlock.ACTIVE ? "bundles_" : "fire_";
        MultiPartBlockStateBuilder states = getMultipartBuilder(block);
        for (int part = 0; part < layout.size(); part++) {
            ModelFile base = model(spec, "part_" + part);
            ModelFile extra = exists(spec, overlay + part) ? model(spec, overlay + part) : null;
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                states.part().modelFile(base).rotationY(rotation(facing)).addModel()
                        .condition(StationBlock.FACING, facing).condition(layout.part(), part).end();
                if (extra != null) {
                    states.part().modelFile(extra).rotationY(rotation(facing)).addModel()
                            .condition(StationBlock.FACING, facing).condition(layout.part(), part).condition(shown, true).end();
                }
            }
        }
    }

    private ResourceLocation location(StationSpec spec, String name) {
        return modLoc("block/stations/" + spec.directory() + "/" + name);
    }

    private boolean exists(StationSpec spec, String name) {
        return existing.exists(location(spec, name), PackType.CLIENT_RESOURCES, ".json", "models");
    }

    private ModelFile model(StationSpec spec, String name) {
        return models().getExistingFile(location(spec, name));
    }

    /** Models face north; the blockstate turns them clockwise to the facing. */
    static int rotation(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }
}
