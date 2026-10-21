package io.github.dirtyredz.herbalwitch.data;

import io.github.dirtyredz.herbalwitch.data.client.decor.DecorItemModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.decor.DecorLanguageProvider;
import io.github.dirtyredz.herbalwitch.data.client.decor.DecorModelProvider;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorBlockLoot;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorItemTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Registers the data providers for {@code runData}. Provider registration only; nothing here runs in a game. */
public final class HerbalWitchDataProviders {
    private HerbalWitchDataProviders() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFiles = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new DecorModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new DecorItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new DecorLanguageProvider(output));

        generator.addProvider(event.includeServer(), new DecorRecipeProvider(output));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(DecorBlockLoot::new, LootContextParamSets.BLOCK))));
        DecorBlockTagsProvider blockTags = generator.addProvider(event.includeServer(),
                new DecorBlockTagsProvider(output, lookup, existingFiles));
        generator.addProvider(event.includeServer(),
                new DecorItemTagsProvider(output, lookup, blockTags.contentsGetter(), existingFiles));
    }
}
