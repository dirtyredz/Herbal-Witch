package io.github.dirtyredz.herbalwitch.data.client.stations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Station items show the idle station with vanilla block-item transforms. A multiblock station's
 * item shows its whole authored assembly ({@code assembly}), which carries its own smaller display
 * transforms so it fits a slot.
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
            ItemModelBuilder item = getBuilder("item/" + spec.path()).parent(new ModelFile.UncheckedModelFile(
                    modLoc("block/stations/" + spec.directory() + (multiblock ? "/assembly" : "/base"))));
            if (multiblock) {
                continue;
            }
            transform(item, ItemDisplayContext.GUI, 30, 225, 0, 0.625F);
            transform(item, ItemDisplayContext.GROUND, 0, 0, 3, 0.25F);
            transform(item, ItemDisplayContext.FIXED, 0, 0, 0, 0.5F);
            transform(item, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 75, 45, 2.5F, 0.375F);
            transform(item, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0, 45, 0, 0.4F);
            transform(item, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0, 225, 0, 0.4F);
        }
    }

    static void transform(ItemModelBuilder item, ItemDisplayContext context, float rx, float ry, float ty, float scale) {
        item.transforms().transform(context).rotation(rx, ry, 0).translation(0, ty, 0).scale(scale).end().end();
    }
}
