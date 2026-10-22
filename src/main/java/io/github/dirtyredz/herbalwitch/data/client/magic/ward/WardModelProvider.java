package io.github.dirtyredz.herbalwitch.data.client.magic.ward;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * The ward stone's blockstate over its compiled models ({@code block/ward/ward_stone/{base,active}},
 * from src/art/blockbench) turned to its facing, and its item model, which only names the idle model.
 */
public class WardModelProvider extends BlockStateProvider {
    public static final String DIR = "block/ward/ward_stone/";

    public WardModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch ward stone blockstate";
    }

    @Override
    protected void registerStatesAndModels() {
        ModelFile base = models().getExistingFile(modLoc(DIR + "base"));
        ModelFile active = models().getExistingFile(modLoc(DIR + "active"));
        getVariantBuilder(WardRegistration.STONE.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(WardStoneBlock.ACTIVE) ? active : base)
                .rotationY(((int) state.getValue(WardStoneBlock.FACING).toYRot() + 180) % 360).build());
        itemModels().getBuilder("item/" + WardCatalog.PATH).parent(new ModelFile.UncheckedModelFile(modLoc(DIR + "base")));
    }
}
