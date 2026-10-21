package io.github.dirtyredz.herbalwitch.data.client.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Flat 16 px sprites for seeds and fresh herbs; the herb bed item shows the placed (dry) bed. */
public class BotanyItemModelProvider extends ItemModelProvider {
    public BotanyItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch botany item models";
    }

    @Override
    protected void registerModels() {
        for (HerbSpec herb : HerbCatalog.ALL) {
            sprite(herb.seedsPath());
            sprite(herb.freshPath());
        }
        getBuilder("item/" + BotanyBlocks.HERB_BED_PATH)
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + BotanyBlocks.HERB_BED_PATH)));
    }

    /** Ids contain slashes, so the {@code item/} prefix is explicit (docs/GOTCHAS.md). */
    private void sprite(String path) {
        getBuilder("item/" + path).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/" + path));
    }
}
