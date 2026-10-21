package io.github.dirtyredz.herbalwitch.data.server.preparations;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** The pests the repellent keeps off: silverfish and endermites. (The mod's only entity tag writer.) */
public class PreparationEntityTagsProvider extends EntityTypeTagsProvider {
    public PreparationEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                         ExistingFileHelper existingFiles) {
        super(output, lookup, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch entity tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(PreparationTags.REPELLED_PESTS).add(EntityType.SILVERFISH, EntityType.ENDERMITE);
    }
}
