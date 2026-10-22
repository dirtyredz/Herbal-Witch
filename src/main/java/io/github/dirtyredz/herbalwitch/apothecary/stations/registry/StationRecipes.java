package io.github.dirtyredz.herbalwitch.apothecary.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench.BottlingRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.CauldronRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.charm_workbench.CharmRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.still.DistillationRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.DryingRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot.InfusionRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.SteepingRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.mortar.GrindingRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

/** The processing recipe types and their serializers, ids {@code processing/<type>}. */
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

    public static final RegistryObject<RecipeType<CauldronRecipe>> CAULDRON = type("cauldron");
    public static final RegistryObject<RecipeType<DistillationRecipe>> DISTILLATION = type("distillation");
    public static final RegistryObject<RecipeType<BottlingRecipe>> BOTTLING = type("bottling");
    public static final RegistryObject<RecipeType<CharmRecipe>> CHARM_WORK = type("charm_work");

    public static final RegistryObject<ProcessRecipeSerializer<CauldronRecipe>> CAULDRON_SERIALIZER = SERIALIZERS.register(
            "processing/cauldron", () -> new ProcessRecipeSerializer<>(CauldronRecipe.BOUNDS, CauldronRecipe::new));
    public static final RegistryObject<ProcessRecipeSerializer<DistillationRecipe>> DISTILLATION_SERIALIZER = SERIALIZERS.register(
            "processing/distillation", () -> new ProcessRecipeSerializer<>(DistillationRecipe.BOUNDS, DistillationRecipe::new));
    public static final RegistryObject<ProcessRecipeSerializer<BottlingRecipe>> BOTTLING_SERIALIZER = SERIALIZERS.register(
            "processing/bottling", () -> new ProcessRecipeSerializer<>(BottlingRecipe.BOUNDS, BottlingRecipe::new));
    public static final RegistryObject<ProcessRecipeSerializer<CharmRecipe>> CHARM_WORK_SERIALIZER = SERIALIZERS.register(
            "processing/charm_work", () -> new ProcessRecipeSerializer<>(CharmRecipe.BOUNDS, CharmRecipe::new));

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

    /** Every processing type, in station order, for viewers. */
    public static List<RegistryObject<? extends RecipeType<? extends ProcessRecipe>>> all() {
        return List.of(DRYING, GRINDING, STEEPING, INFUSION, CAULDRON, DISTILLATION, BOTTLING, CHARM_WORK);
    }
}
