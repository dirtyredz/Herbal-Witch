package io.github.dirtyredz.herbalwitch.data.client.magic.charm;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmCatalog;
import io.github.dirtyredz.herbalwitch.magic.charm.catalog.CharmSpec;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Each charm's flat item model over its generated sprite ({@code item/magic/charms/<charm>}). */
public class CharmItemModelProvider extends ItemModelProvider {
    public CharmItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch charm item models";
    }

    @Override
    protected void registerModels() {
        for (CharmSpec charm : CharmCatalog.ALL) {
            getBuilder("item/" + charm.path()).parent(new ModelFile.UncheckedModelFile("item/generated"))
                    .texture("layer0", modLoc("item/" + charm.path()));
        }
    }
}
