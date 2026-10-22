package io.github.dirtyredz.herbalwitch.data.client.preparations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Flat 16 px sprites for bundles, powders and products; the oil lantern item shows the unlit lantern,
 * whose display transforms come from its Blockbench project (src/art/blockbench/preparations/).
 */
public class PreparationItemModelProvider extends ItemModelProvider {
    public PreparationItemModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch preparation item models";
    }

    @Override
    protected void registerModels() {
        for (HerbSpec herb : HerbCatalog.ALL) {
            sprite(HerbalMaterialCatalog.bundlePath(herb));
            sprite(HerbalMaterialCatalog.powderPath(herb));
        }
        for (ProductSpec product : EverydayProductCatalog.ALL) {
            sprite(product.path());
        }
        for (ProductSpec product : AdvancedPreparations.specs()) {
            sprite(product.path());
        }
        getBuilder("item/" + PreparationBlocks.OIL_LANTERN_PATH)
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/preparations/templates/oil_lantern_off")));
    }

    /** Ids contain slashes, so the {@code item/} prefix is explicit (docs/GOTCHAS.md). */
    private void sprite(String path) {
        getBuilder("item/" + path).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/" + path));
    }
}
