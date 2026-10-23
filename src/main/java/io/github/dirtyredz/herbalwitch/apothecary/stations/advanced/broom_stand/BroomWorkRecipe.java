package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.broom_stand;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationRecipes;
import io.github.dirtyredz.herbalwitch.platform.recipe.*;
import io.github.dirtyredz.herbalwitch.magic.broom.persistence.BroomItemData;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
/** Recipe-owned pure transform: never mutate the reserved broom during preview. */
public final class BroomWorkRecipe extends ProcessRecipe implements ProcessJob.ResultDecorator {
 private final int repair;
 public static ProcessRecipeJson.Bounds bounds(boolean repair) { return new ProcessRecipeJson.Bounds(3, repair, false, false, ProcessRequirements.Policy.MAGICAL); }
 public BroomWorkRecipe(ResourceLocation id, ProcessRecipeDescription description, int repair) {
  super(id,description);
  if(repair < 0 || repair > 600 || description.base().isPresent() != (repair > 0)
    || !description.result().is(BroomItems.BROOM.get()) || description.result().getCount()!=1 || !description.returns().isEmpty()) throw new IllegalArgumentException("Invalid broom operation");
  ProcessRecipeJson.validate(description,bounds(repair>0)); this.repair=repair;
 }
 public int repair() { return repair; }
 @Override public java.util.Optional<net.minecraft.network.chat.Component> resultNote() { return repair==0 ? java.util.Optional.empty() : java.util.Optional.of(net.minecraft.network.chat.Component.translatable("recipe.herbalwitch.broom.repair",repair)); }
 @Override public String fingerprint() { return super.fingerprint()+"/broom/"+repair; }
 @Override public int[] reserve(ProcessInventory inventory, ProcessLayout layout) {
  int[] reserved=super.reserve(inventory,layout);
  if(reserved==null || repair==0) return reserved;
  ItemStack base=inventory.getStackInSlot(layout.base());
  return base.is(BroomItems.BROOM.get()) && base.getCount()==1 && description().base().orElseThrow().count()==1
    && BroomItemData.read(base).filter(d->d.wear()>0).isPresent() ? reserved : null;
 }
 @Override public ItemStack decorate(ItemStack result, ProcessInventory inventory, int[] reservation) {
  if(repair==0) return result;
  int base=ProcessLayout.of(inventory).base();
  ItemStack copy=inventory.getStackInSlot(base).copy();
  BroomItemData.read(copy).orElseThrow().repair(repair).write(copy);
  return copy;
 }
 @Override public RecipeSerializer<?> getSerializer() { return StationRecipes.BROOM_WORK_SERIALIZER.get(); }
 @Override public RecipeType<?> getType() { return StationRecipes.BROOM_WORK.get(); }
}
