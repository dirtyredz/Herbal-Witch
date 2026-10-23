package io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.broom_stand;
import com.google.gson.*;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeJson;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
public final class BroomWorkSerializer implements RecipeSerializer<BroomWorkRecipe> {
 @Override public BroomWorkRecipe fromJson(ResourceLocation id, JsonObject json) {
  String operation=GsonHelper.getAsString(json,"operation");
  int repair=GsonHelper.getAsInt(json,"repair",0);
  if(!(operation.equals("craft") && repair==0 || operation.equals("repair") && repair>=1 && repair<=600)) throw new JsonParseException("Invalid broom operation/repair");
  return new BroomWorkRecipe(id,ProcessRecipeJson.read(json,BroomWorkRecipe.bounds(repair>0)),repair);
 }
 @Override public BroomWorkRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buf) {
  int repair=buf.readVarInt();
  if(repair<0 || repair>600) throw new IllegalArgumentException("Invalid broom repair");
  return new BroomWorkRecipe(id,ProcessRecipeJson.fromNetwork(buf,BroomWorkRecipe.bounds(repair>0)),repair);
 }
 @Override public void toNetwork(FriendlyByteBuf buf,BroomWorkRecipe recipe) { buf.writeVarInt(recipe.repair());ProcessRecipeJson.toNetwork(buf,recipe.description()); }
}
