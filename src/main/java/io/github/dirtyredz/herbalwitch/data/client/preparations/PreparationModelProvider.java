package io.github.dirtyredz.herbalwitch.data.client.preparations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.HerbalMaterialCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.lighting.OilLanternBlock;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Hung bundles: per herb a ceiling and a wall wrapper over the authored bundle templates, binding the
 * herb's bundle sprite; the wall form turns with the facing. The oil lantern: off and on templates.
 */
public class PreparationModelProvider extends BlockStateProvider {
    private static final String TEMPLATES = "block/preparations/templates/";

    public PreparationModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch preparation blockstates and models";
    }

    @Override
    protected void registerStatesAndModels() {
        for (HerbSpec herb : HerbCatalog.ALL) {
            String path = HerbalMaterialCatalog.bundlePath(herb);
            ModelFile ceiling = models().withExistingParent("block/" + path, modLoc(TEMPLATES + "dried_bundle"))
                    .texture("bundle", modLoc("item/" + path));
            ModelFile wall = models().withExistingParent("block/" + path + "_wall", modLoc(TEMPLATES + "dried_bundle_wall"))
                    .texture("bundle", modLoc("item/" + path));
            getVariantBuilder(PreparationBlocks.bundle(herb)).forAllStates(state -> {
                boolean onWall = state.getValue(FaceAttachedHorizontalDirectionalBlock.FACE) == AttachFace.WALL;
                int y = ((int) state.getValue(FaceAttachedHorizontalDirectionalBlock.FACING).toYRot() + 180) % 360;
                return ConfiguredModel.builder().modelFile(onWall ? wall : ceiling).rotationY(onWall ? y : 0).build();
            });
        }
        ModelFile off = models().getExistingFile(modLoc(TEMPLATES + "oil_lantern_off"));
        ModelFile on = models().getExistingFile(modLoc(TEMPLATES + "oil_lantern_on"));
        getVariantBuilder(PreparationBlocks.OIL_LANTERN.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(OilLanternBlock.LIT) ? on : off).build());
    }
}
