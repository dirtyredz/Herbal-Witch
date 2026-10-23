package io.github.dirtyredz.herbalwitch.data.server.stations.recipes.tools;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.broom_stand.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeJson;
import io.github.dirtyredz.herbalwitch.data.server.stations.recipes.ProcessingRecipeBuilder;
import io.github.dirtyredz.herbalwitch.platform.recipe.*;
import io.github.dirtyredz.herbalwitch.magic.broom.registry.BroomItems;
import io.github.dirtyredz.herbalwitch.magic.wand.registry.WandItems;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import java.util.*;
import java.util.function.Consumer;
import static io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider.has;
public final class ToolStationRecipes {
 private ToolStationRecipes() { }
 public static void addTo(Consumer<FinishedRecipe> writer) {
  ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS,StationItems.item(StationCatalog.BROOM_STAND))
   .requires(Items.CRAFTING_TABLE).requires(Items.DARK_OAK_PLANKS,2).requires(Items.STICK,2).requires(Items.STRING).requires(Items.IRON_INGOT)
   .unlockedBy("has_string",has(Items.STRING)).save(writer,id("stations/tools/broom_stand/crafting"));
  ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS,StationItems.item(StationCatalog.WAND_BENCH))
   .requires(Items.CRAFTING_TABLE).requires(Items.DARK_OAK_PLANKS,2).requires(Items.FLINT).requires(Items.GOLD_NUGGET)
   .unlockedBy("has_flint",has(Items.FLINT)).save(writer,id("stations/tools/wand_bench/crafting"));
  broom(writer,false);broom(writer,true);
  ProcessingRecipeBuilder.of(StationRecipes.WAND_CARVING_SERIALIZER.get(),100).input(Items.STICK,2).input(Items.AMETHYST_SHARD)
   .input(Items.GOLD_NUGGET).witch(5).result(WandItems.WAND.get()).save(writer,"processing/wand_carving/craft");
 }
 private static ProcessRecipeDescription.Counted counted(Item item,int count) { return new ProcessRecipeDescription.Counted(Ingredient.of(item),count); }
 private static void broom(Consumer<FinishedRecipe> writer,boolean repair) {
  var ingredients=repair?List.of(counted(Items.WHEAT,2),counted(Items.STRING,1)):List.of(counted(Items.STICK,2),counted(Items.WHEAT,3),counted(Items.STRING,2));
  var description=new ProcessRecipeDescription(ingredients,repair?Optional.of(counted(BroomItems.BROOM.get(),1)):Optional.empty(),Optional.empty(),
   new ItemStack(BroomItems.BROOM.get()),List.of(),repair?100:200,false,ProcessRequirements.witch(repair?5:10));
  ResourceLocation id=id("processing/broom_work/"+(repair?"repair":"craft"));
  new BroomWorkRecipe(id,description,repair?200:0);
  writer.accept(new FinishedRecipe() {
   public void serializeRecipeData(JsonObject json) { ProcessRecipeJson.write(description).entrySet().forEach(e->json.add(e.getKey(),e.getValue()));json.addProperty("operation",repair?"repair":"craft");json.addProperty("repair",repair?200:0); }
   public ResourceLocation getId() { return id; }
   public RecipeSerializer<?> getType() { return StationRecipes.BROOM_WORK_SERIALIZER.get(); }
   public JsonObject serializeAdvancement() { return null; }
   public ResourceLocation getAdvancementId() { return null; }
  });
 }
 private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath("herbalwitch",path); }
}
