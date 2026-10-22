package io.github.dirtyredz.herbalwitch.data.server.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbHabitat;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** The default habitat biome tags ({@code herbalwitch:botany/habitat/<group>}). Packs may extend them. */
public class BotanyBiomeTagsProvider extends BiomeTagsProvider {
    public static final Map<HerbHabitat, List<ResourceKey<Biome>>> HABITATS = Map.of(
            HerbHabitat.TEMPERATE, List.of(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.FOREST, Biomes.FLOWER_FOREST,
                    Biomes.BIRCH_FOREST),
            HerbHabitat.DRY, List.of(Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA),
            HerbHabitat.WET, List.of(Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.RIVER),
            HerbHabitat.SHADE, List.of(Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
                    Biomes.DARK_FOREST),
            HerbHabitat.WOODLAND, List.of(Biomes.DARK_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA));

    public BotanyBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                   ExistingFileHelper existingFiles) {
        super(output, lookup, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch habitat biome tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (HerbHabitat habitat : HerbHabitat.values()) {
            HABITATS.get(habitat).forEach(biome -> tag(BotanyTags.habitat(habitat)).add(biome));
        }
    }
}
