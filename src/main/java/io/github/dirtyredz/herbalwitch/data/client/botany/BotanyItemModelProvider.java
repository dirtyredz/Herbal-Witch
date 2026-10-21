package io.github.dirtyredz.herbalwitch.data.client.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Flat 16 px sprites for seeds and fresh herbs; the herb bed item shows an unjoined dry bed (the
 * authored assembly {@code block/botany/garden/herb_bed}) with block-item transforms.
 */
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
        ItemModelBuilder bed = getBuilder("item/" + BotanyBlocks.HERB_BED_PATH)
                .parent(new ModelFile.UncheckedModelFile(modLoc("block/" + BotanyBlocks.HERB_BED_PATH)));
        transform(bed, ItemDisplayContext.GUI, 30, 225, 0, 0.625F);
        transform(bed, ItemDisplayContext.GROUND, 0, 0, 3, 0.25F);
        transform(bed, ItemDisplayContext.FIXED, 0, 0, 0, 0.5F);
        transform(bed, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, 75, 45, 2.5F, 0.375F);
        transform(bed, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, 0, 45, 0, 0.4F);
        transform(bed, ItemDisplayContext.FIRST_PERSON_LEFT_HAND, 0, 225, 0, 0.4F);
    }

    private static void transform(ItemModelBuilder item, ItemDisplayContext context, float rx, float ry, float ty, float scale) {
        item.transforms().transform(context).rotation(rx, ry, 0).translation(0, ty, 0).scale(scale).end().end();
    }

    /** Ids contain slashes, so the {@code item/} prefix is explicit (docs/GOTCHAS.md). */
    private void sprite(String path) {
        getBuilder("item/" + path).parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/" + path));
    }
}
