package io.github.dirtyredz.herbalwitch.data.client.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog.ResearchPiece;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * The grimoire is a flat sprite ({@code item/grimoire/book}); each research block's item names its
 * compiled block model as parent, whose display transforms come from the Blockbench project.
 */
public class GrimoireItemModelProvider extends ItemModelProvider {
    public GrimoireItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch grimoire item models";
    }

    @Override
    protected void registerModels() {
        getBuilder("item/" + GrimoireCatalog.BOOK_PATH).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/" + GrimoireCatalog.BOOK_PATH));
        for (ResearchPiece piece : GrimoireCatalog.RESEARCH_PIECES) {
            getBuilder("item/" + piece.path()).parent(new ModelFile.UncheckedModelFile(modLoc(GrimoireModelProvider.model(piece))));
        }
    }
}
