package io.github.dirtyredz.herbalwitch.data.server.magic.witch;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbHabitat;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyTags;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.RecoveryConditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The nature biomes that improve a witch's rest and meditation, {@code herbalwitch:witch/energy/nature}:
 * by default the temperate, wet, shade and woodland habitats (the dry savannas are not). Packs may
 * extend it; its own file, so the habitat tags keep their single writer.
 */
public class EnergyBiomeTagsProvider extends BiomeTagsProvider {
    public static final List<HerbHabitat> NATURE = List.of(HerbHabitat.TEMPERATE, HerbHabitat.WET, HerbHabitat.SHADE, HerbHabitat.WOODLAND);

    public EnergyBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper existingFiles) {
        super(output, lookup, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch energy biome tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var tag = tag(RecoveryConditions.NATURE);
        NATURE.forEach(habitat -> tag.addTag(BotanyTags.habitat(habitat)));
    }
}
