package io.github.dirtyredz.herbalwitch.data.client.animals;

import io.github.dirtyredz.herbalwitch.companions.animals.catalog.Bait;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/** One-off item art is owned by Blockbench projects; wrappers only name their compiled parent. */
public final class AnimalItemModelProvider extends ItemModelProvider {
    public AnimalItemModelProvider(PackOutput output, ExistingFileHelper helper) { super(output, "herbalwitch", helper); }
    @Override public String getName() { return "Herbal Witch animal item models"; }
    @Override protected void registerModels() {
        for (Bait bait : Bait.values()) withExistingParent("item/" + bait.itemPath(), modLoc("block/" + bait.itemPath() + "/base"));
        withExistingParent("item/animals/care/calming_powder", modLoc("block/animals/care/calming_powder/base"));
        for (String item : new String[] {"charm", "filled_charm"}) withExistingParent("item/animals/capture/" + item, modLoc("block/animals/capture/" + item + "/base"));
    }
}
