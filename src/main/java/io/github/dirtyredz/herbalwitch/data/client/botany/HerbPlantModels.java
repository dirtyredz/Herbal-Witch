package io.github.dirtyredz.herbalwitch.data.client.botany;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

/**
 * Herb plants, one model per age ({@code block/botany/<group>/<herb>/stage_<n>}) selected by
 * {@code age}. Every age is a 3D cube model written by tools/generate-models.mjs and rooted two
 * pixels below the block so the plant sits in bed or farmland soil: ages 0-2 the herb's young forms
 * ({@code young_<n>}), age 3 its mature form ({@code mature}).
 */
final class HerbPlantModels {
    private HerbPlantModels() {
    }

    static void register(BlockStateProvider states) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            ModelFile[] stages = new ModelFile[HerbSpec.MAX_AGE + 1];
            for (int age = 0; age <= HerbSpec.MAX_AGE; age++) {
                String form = age == HerbSpec.MAX_AGE ? "mature" : "young_" + age;
                stages[age] = states.models().withExistingParent("block/" + herb.directory() + "/stage_" + age,
                        states.modLoc("block/" + herb.directory() + "/" + form));
            }
            states.getVariantBuilder(BotanyBlocks.plant(herb)).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(stages[state.getValue(HerbCropBlock.AGE)]).build());
        }
    }
}
