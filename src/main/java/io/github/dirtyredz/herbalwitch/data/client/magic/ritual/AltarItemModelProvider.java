package io.github.dirtyredz.herbalwitch.data.client.magic.ritual;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * The altar item shows the whole idle altar: its model only names the compiled {@code assembly}
 * parent, whose display transforms come from the Blockbench project (src/art/blockbench/ritual/altar).
 */
public class AltarItemModelProvider extends ItemModelProvider {
    public AltarItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch altar item model";
    }

    @Override
    protected void registerModels() {
        getBuilder("item/" + AltarCatalog.PATH).parent(new ModelFile.UncheckedModelFile(modLoc(AltarModelProvider.DIR + "assembly")));
    }
}
