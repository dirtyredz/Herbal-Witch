package io.github.dirtyredz.herbalwitch.data;

import io.github.dirtyredz.herbalwitch.data.client.botany.BotanyItemModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.botany.BotanyModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.decor.DecorItemModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.decor.DecorModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.grimoire.GrimoireItemModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.grimoire.GrimoireModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.language.HerbalWitchLanguageProvider;
import io.github.dirtyredz.herbalwitch.data.client.magic.charm.CharmItemModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.magic.ritual.AltarItemModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.magic.ward.WardModelProvider;
import io.github.dirtyredz.herbalwitch.data.server.magic.ward.WardBlockLoot;
import io.github.dirtyredz.herbalwitch.data.client.magic.ritual.AltarModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.preparations.PreparationItemModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.preparations.PreparationModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.stations.StationItemModelProvider;
import io.github.dirtyredz.herbalwitch.data.client.stations.StationModelProvider;
import io.github.dirtyredz.herbalwitch.data.server.botany.BotanyBiomeTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.botany.BotanyBlockLoot;
import io.github.dirtyredz.herbalwitch.data.server.botany.BotanyConditionProvider;
import io.github.dirtyredz.herbalwitch.data.server.botany.BotanyWorldgenProvider;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorBlockLoot;
import io.github.dirtyredz.herbalwitch.data.server.grimoire.GrimoireBlockLoot;
import io.github.dirtyredz.herbalwitch.data.server.grimoire.GrimoireContentProvider;
import io.github.dirtyredz.herbalwitch.data.server.magic.ritual.AltarBlockLoot;
import io.github.dirtyredz.herbalwitch.data.server.magic.witch.EnergyBiomeTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.recipes.HerbalWitchRecipeProvider;
import io.github.dirtyredz.herbalwitch.data.server.loot.HerbalWitchLootModifierProvider;
import io.github.dirtyredz.herbalwitch.data.server.preparations.PreparationBlockLoot;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchEntityTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.stations.StationBlockLoot;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchBlockTagsProvider;
import io.github.dirtyredz.herbalwitch.data.server.tags.HerbalWitchItemTagsProvider;
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

/**
 * Registers the data providers for {@code runData}. Provider registration only; nothing here runs in
 * a game. Shared outputs (language, block/item tags, loot tables, global loot modifiers) each have
 * one provider that features contribute to.
 */
public final class HerbalWitchDataProviders {
    private HerbalWitchDataProviders() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        generator.addProvider(event.includeClient(), new io.github.dirtyredz.herbalwitch.data.client.grimoire.guide.GuidePageProvider(output));
        ExistingFileHelper existingFiles = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new DecorModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new DecorItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new BotanyModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new BotanyItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new StationModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new StationItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new PreparationModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new PreparationItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new GrimoireModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new GrimoireItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new AltarModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new AltarItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new WardModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new CharmItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new io.github.dirtyredz.herbalwitch.data.client.animals.AnimalItemModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new io.github.dirtyredz.herbalwitch.data.client.familiars.FamiliarHomeModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new io.github.dirtyredz.herbalwitch.data.client.familiars.FamiliarEquipmentModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new io.github.dirtyredz.herbalwitch.data.client.magic.tools.ToolItemModels(output, existingFiles));
        generator.addProvider(event.includeClient(), new io.github.dirtyredz.herbalwitch.data.client.stations.shop.ShopModelProvider(output, existingFiles));
        generator.addProvider(event.includeClient(), new HerbalWitchLanguageProvider(output));

        generator.addProvider(event.includeServer(), new HerbalWitchRecipeProvider(output));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(DecorBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(BotanyBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(StationBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(PreparationBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(GrimoireBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(AltarBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(WardBlockLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(io.github.dirtyredz.herbalwitch.data.server.familiars.FamiliarHomeLoot::new, LootContextParamSets.BLOCK),
                new LootTableProvider.SubProviderEntry(io.github.dirtyredz.herbalwitch.data.server.stations.shop.ShopBlockLoot::new, LootContextParamSets.BLOCK))));
        HerbalWitchBlockTagsProvider blockTags = generator.addProvider(event.includeServer(),
                new HerbalWitchBlockTagsProvider(output, lookup, existingFiles));
        generator.addProvider(event.includeServer(),
                new HerbalWitchItemTagsProvider(output, lookup, blockTags.contentsGetter(), existingFiles));
        generator.addProvider(event.includeServer(), new BotanyBiomeTagsProvider(output, lookup, existingFiles));
        generator.addProvider(event.includeServer(), new EnergyBiomeTagsProvider(output, lookup, existingFiles));
        generator.addProvider(event.includeServer(), new HerbalWitchEntityTagsProvider(output, lookup, existingFiles));
        generator.addProvider(event.includeServer(), new BotanyWorldgenProvider(output, lookup));
        generator.addProvider(event.includeServer(), new io.github.dirtyredz.herbalwitch.data.server.familiars.FamiliarSpawnProvider(output, lookup));
        generator.addProvider(event.includeServer(), new io.github.dirtyredz.herbalwitch.data.server.familiars.FamiliarBiomeTagsProvider(output, lookup, existingFiles));
        generator.addProvider(event.includeServer(), new BotanyConditionProvider(output));
        generator.addProvider(event.includeServer(), new HerbalWitchLootModifierProvider(output));
        generator.addProvider(event.includeServer(), new GrimoireContentProvider(output));
    }
}
