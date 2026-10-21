package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Inventory models: every item is its piece's template (the unlit lamp, the north-facing piece), so
 * item, placed block and dropped item look alike. The item model only names its parent: display
 * transforms belong to the piece's Blockbench project and are compiled into the template
 * (src/art/blockbench/decor/), so a Display edit in Blockbench needs no datagen.
 */
public class DecorItemModelProvider extends ItemModelProvider {
    public DecorItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch decor item models";
    }

    @Override
    protected void registerModels() {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            FurnitureSpec spec = entry.spec();
            getBuilder("item/" + spec.path()).parent(new ModelFile.UncheckedModelFile(modLoc(DecorModelProvider.itemTemplate(spec))));
        }
    }
}
