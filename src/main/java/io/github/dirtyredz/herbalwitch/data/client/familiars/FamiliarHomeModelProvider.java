package io.github.dirtyredz.herbalwitch.data.client.familiars;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeBlock;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class FamiliarHomeModelProvider extends BlockStateProvider {
    public FamiliarHomeModelProvider(PackOutput output, ExistingFileHelper files) { super(output, HerbalWitch.MOD_ID, files); }
    @Override public String getName() { return "Herbal Witch familiar homes"; }
    @Override protected void registerStatesAndModels() {
        for (var entry : FamiliarHomes.ALL) {
            var base = models().getExistingFile(modLoc("block/" + entry.kind().path() + "/base"));
            getVariantBuilder(entry.block().get()).forAllStates(s -> ConfiguredModel.builder().modelFile(base)
                    .rotationY(((int) s.getValue(FamiliarHomeBlock.FACING).toYRot() + 180) % 360).build());
            itemModels().getBuilder("item/" + entry.kind().path()).parent(base);
        }
    }
}
