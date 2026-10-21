package io.github.dirtyredz.herbalwitch.data.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Station items show the idle station: a one-block station its {@code base}, a multiblock station
 * its whole {@code assembly}. The item model only names its parent; display transforms come from the
 * station's Blockbench project (src/art/blockbench/stations/), compiled into that parent.
 */
public class StationItemModelProvider extends ItemModelProvider {
    public StationItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch station item models";
    }

    @Override
    protected void registerModels() {
        for (StationSpec spec : StationCatalog.ALL) {
            boolean multiblock = StationBlocks.block(spec) instanceof StationBlock station && station.layout() != null;
            getBuilder("item/" + spec.path()).parent(new ModelFile.UncheckedModelFile(
                    modLoc("block/stations/" + spec.directory() + (multiblock ? "/assembly" : "/base"))));
        }
    }
}
