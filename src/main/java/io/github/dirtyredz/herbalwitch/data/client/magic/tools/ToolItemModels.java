package io.github.dirtyredz.herbalwitch.data.client.magic.tools;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.common.data.ExistingFileHelper;
public final class ToolItemModels extends ItemModelProvider {
 public ToolItemModels(PackOutput output, ExistingFileHelper existing) { super(output,"herbalwitch",existing); }
 @Override public String getName() { return "Herbal Witch tools"; }
 @Override protected void registerModels() {
  for(String tool:new String[]{"broom","wand"}) getBuilder("item/magic/"+tool+"/"+tool).parent(new ModelFile.UncheckedModelFile(modLoc("item/magic/"+tool+"/base")));
 }
}
