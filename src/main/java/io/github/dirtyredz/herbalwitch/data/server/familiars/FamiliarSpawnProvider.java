package io.github.dirtyredz.herbalwitch.data.server.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.acquisition.FamiliarWildSpawns;
import io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarEntities;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.*;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class FamiliarSpawnProvider extends DatapackBuiltinEntriesProvider {
    public FamiliarSpawnProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, new RegistrySetBuilder().add(ForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
            var biomes = context.lookup(Registries.BIOME);
            for (var wild : FamiliarWildSpawns.ALL) context.register(ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                    ResourceLocation.fromNamespaceAndPath("herbalwitch", "familiars/spawns/" + wild.species())),
                    new ForgeBiomeModifiers.AddSpawnsBiomeModifier(biomes.getOrThrow(wild.biomes()),
                            List.of(new MobSpawnSettings.SpawnerData(FamiliarEntities.type(wild.species()), wild.weight(), 1, 2))));
        }), Set.of("herbalwitch"));
    }
    @Override public String getName() { return "Herbal Witch familiar wild spawns"; }
}
