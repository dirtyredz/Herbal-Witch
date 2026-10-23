package io.github.dirtyredz.herbalwitch.data.server.familiars;

import io.github.dirtyredz.herbalwitch.companions.familiars.acquisition.FamiliarWildSpawns;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import java.util.concurrent.CompletableFuture;

public final class FamiliarBiomeTagsProvider extends BiomeTagsProvider {
    public FamiliarBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) { super(output, lookup, "herbalwitch", files); }
    @Override protected void addTags(HolderLookup.Provider lookup) {
        tag(FamiliarWildSpawns.WOODED).add(Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.OLD_GROWTH_BIRCH_FOREST,
                Biomes.DARK_FOREST, Biomes.TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        tag(FamiliarWildSpawns.WARM_DRY).add(Biomes.DESERT, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA, Biomes.BADLANDS, Biomes.WOODED_BADLANDS);
    }
    @Override public String getName() { return "Herbal Witch familiar acquisition biomes"; }
}
