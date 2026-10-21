package io.github.dirtyredz.herbalwitch.data.client.preparations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/** Flat 16 px sprites for bundles, powders and products; the oil lantern item shows the unlit lantern. */
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
        ItemModelBuilder lantern = getBuilder("item/" + PreparationBlocks.OIL_LANTERN_PATH)
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/preparations/templates/oil_lantern_off")));
        lantern.transforms().transform(ItemDisplayContext.GUI).rotation(30, 225, 0).scale(0.9F).end()
                .transform(ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.4F).end()
                .transform(ItemDisplayContext.FIXED).scale(0.75F).end()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0).scale(0.5F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).scale(0.6F).end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).scale(0.6F).end();
    }

    /** Ids contain slashes, so the {@code item/} prefix is explicit (docs/GOTCHAS.md). */
    private void sprite(String path) {
        getBuilder("item/" + path).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/" + path));
    }
}
