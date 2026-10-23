package io.github.dirtyredz.herbalwitch.data.client.familiars;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarEquipmentCatalog;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public final class FamiliarEquipmentModelProvider extends ItemModelProvider {
    public FamiliarEquipmentModelProvider(PackOutput output, ExistingFileHelper files) { super(output, HerbalWitch.MOD_ID, files); }
    @Override protected void registerModels() {
        for (var gear : FamiliarEquipmentCatalog.ALL) withExistingParent("item/" + gear.path(), modLoc("block/" + gear.path() + "/base"));
        for (String species : java.util.List.of("raven", "owl", "snake"))
            withExistingParent("item/familiars/spawn_egg/" + species, modLoc("block/familiars/spawn_egg/" + species + "/base"));
    }
}
