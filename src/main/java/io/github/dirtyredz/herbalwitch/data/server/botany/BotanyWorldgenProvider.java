package io.github.dirtyredz.herbalwitch.data.server.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.HerbWorldgen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Writes the herb configured/placed features and the per-group biome modifiers from {@link HerbWorldgen}. */
public class BotanyWorldgenProvider extends DatapackBuiltinEntriesProvider {
    public static final RegistrySetBuilder ENTRIES = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, HerbWorldgen::bootstrapConfigured)
            .add(Registries.PLACED_FEATURE, HerbWorldgen::bootstrapPlaced)
            .add(ForgeRegistries.Keys.BIOME_MODIFIERS, HerbWorldgen::bootstrapBiomeModifiers);

    public BotanyWorldgenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup, ENTRIES, Set.of(HerbalWitch.MOD_ID));
    }

    @Override
    public String getName() {
        return "Herbal Witch botany worldgen";
    }
}
