package io.github.dirtyredz.herbalwitch.data.client.botany;

import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

/**
 * Herb plants, one model per age ({@code block/botany/<group>/<herb>/stage_<n>}) selected by
 * {@code age}. Ages 0-2 are the shared planted cross ({@code block/botany/templates/planted_cross},
 * rooted two pixels below the block so the plant sits in bed or farmland soil) over the herb's stage
 * sprite; age 3 is the herb's authored 3D mature model ({@code block/botany/<group>/<herb>/mature}).
 */
final class HerbPlantModels {
    static final String CROSS = "block/botany/templates/planted_cross";

    private HerbPlantModels() {
    }

    static void register(BlockStateProvider states) {
        for (HerbSpec herb : HerbCatalog.ALL) {
            ModelFile[] stages = new ModelFile[HerbSpec.MAX_AGE + 1];
            for (int age = 0; age < HerbSpec.MAX_AGE; age++) {
                String path = herb.directory() + "/stage_" + age;
                stages[age] = states.models().withExistingParent("block/" + path, states.modLoc(CROSS))
                        .texture("cross", states.modLoc("block/" + path));
            }
            stages[HerbSpec.MAX_AGE] = states.models().withExistingParent("block/" + herb.directory() + "/stage_" + HerbSpec.MAX_AGE,
                    states.modLoc("block/" + herb.directory() + "/mature"));
            states.getVariantBuilder(BotanyBlocks.plant(herb)).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(stages[state.getValue(HerbCropBlock.AGE)]).build());
        }
    }
}
