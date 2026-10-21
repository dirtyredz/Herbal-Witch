package io.github.dirtyredz.herbalwitch.data.client.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.data.client.botany.garden.HerbBedBlockstates;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Botany blockstates and models: the herb plants ({@link HerbPlantModels}) and the joining herb
 * bed ({@link HerbBedBlockstates}). This provider only delegates.
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
        HerbPlantModels.register(this);
        HerbBedBlockstates.register(this);
    }
}
