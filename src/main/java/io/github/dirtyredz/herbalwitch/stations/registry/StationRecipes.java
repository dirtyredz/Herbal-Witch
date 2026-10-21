package io.github.dirtyredz.herbalwitch.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.recipe.DryingRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.recipe.InfusionRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.kettle.recipe.SteepingRecipe;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.recipe.GrindingRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/** The four processing recipe types and their serializers, ids {@code processing/<type>}. */
public final class StationRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, HerbalWitch.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, HerbalWitch.MOD_ID);

    public static final RegistryObject<RecipeType<DryingRecipe>> DRYING = type("drying");
    public static final RegistryObject<RecipeType<GrindingRecipe>> GRINDING = type("grinding");
    public static final RegistryObject<RecipeType<SteepingRecipe>> STEEPING = type("steeping");
    public static final RegistryObject<RecipeType<InfusionRecipe>> INFUSION = type("infusion");

    public static final RegistryObject<ProcessRecipeSerializer<DryingRecipe>> DRYING_SERIALIZER = SERIALIZERS.register(
            "processing/drying", () -> new ProcessRecipeSerializer<>(DryingRecipe.BOUNDS, DryingRecipe::new));
    public static final RegistryObject<ProcessRecipeSerializer<GrindingRecipe>> GRINDING_SERIALIZER = SERIALIZERS.register(
            "processing/grinding", () -> new ProcessRecipeSerializer<>(GrindingRecipe.BOUNDS, GrindingRecipe::new));
    public static final RegistryObject<ProcessRecipeSerializer<SteepingRecipe>> STEEPING_SERIALIZER = SERIALIZERS.register(
            "processing/steeping", () -> new ProcessRecipeSerializer<>(SteepingRecipe.BOUNDS, SteepingRecipe::new));
    public static final RegistryObject<ProcessRecipeSerializer<InfusionRecipe>> INFUSION_SERIALIZER = SERIALIZERS.register(
            "processing/infusion", () -> new ProcessRecipeSerializer<>(InfusionRecipe.BOUNDS, InfusionRecipe::new));

    private StationRecipes() {
    }

    private static <R extends ProcessRecipe> RegistryObject<RecipeType<R>> type(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "processing/" + name);
        return TYPES.register("processing/" + name, () -> new RecipeType<>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    /** All four types, in station order, for viewers. */
    public static List<RegistryObject<? extends RecipeType<? extends ProcessRecipe>>> all() {
        return List.of(DRYING, GRINDING, STEEPING, INFUSION);
    }
}
