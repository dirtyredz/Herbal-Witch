package io.github.dirtyredz.herbalwitch.data.client.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Herb plants: one untinted cutout cross model per age ({@code block/botany/<group>/<herb>/stage_<n>})
 * selected by the {@code age} property. The herb bed: the authored raised-bed model (dry soil) and a
 * generated moist variant that rebinds only the soil texture.
 */
public class BotanyModelProvider extends BlockStateProvider {
    public BotanyModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch botany blockstates and models";
    }

    @Override
    protected void registerStatesAndModels() {
        for (HerbSpec herb : HerbCatalog.ALL) {
            ModelFile[] stages = new ModelFile[HerbSpec.MAX_AGE + 1];
            for (int age = 0; age <= HerbSpec.MAX_AGE; age++) {
                String path = herb.directory() + "/stage_" + age;
                stages[age] = models().withExistingParent("block/" + path, mcLoc("block/cross"))
                        .texture("cross", modLoc("block/" + path))
                        .renderType("minecraft:cutout");
            }
            getVariantBuilder(BotanyBlocks.plant(herb)).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(stages[state.getValue(HerbCropBlock.AGE)]).build());
        }
        ModelFile dry = models().getExistingFile(modLoc("block/" + BotanyBlocks.HERB_BED_PATH));
        ModelFile moist = models().withExistingParent("block/" + BotanyBlocks.HERB_BED_PATH + "_moist",
                        modLoc("block/" + BotanyBlocks.HERB_BED_PATH))
                .texture("soil", soil("soil_moist"));
        getVariantBuilder(BotanyBlocks.herbBed()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(HerbBedBlock.isMoist(state) ? moist : dry).build());
    }

    private static ResourceLocation soil(String name) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "block/botany/garden/" + name);
    }
}
