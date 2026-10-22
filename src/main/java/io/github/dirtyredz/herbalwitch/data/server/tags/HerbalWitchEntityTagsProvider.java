package io.github.dirtyredz.herbalwitch.data.server.tags;

import io.github.dirtyredz.herbalwitch.data.server.animals.AnimalTagContributions;
import io.github.dirtyredz.herbalwitch.data.server.preparations.PreparationEntityTagContributions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import java.util.concurrent.CompletableFuture;

/** The sole entity-tag writer. Features contribute through a neutral sink. */
public final class HerbalWitchEntityTagsProvider extends EntityTypeTagsProvider {
    public interface Sink { IntrinsicHolderTagsProvider.IntrinsicTagAppender<EntityType<?>> tag(TagKey<EntityType<?>> key); }

    public HerbalWitchEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, "herbalwitch", files);
    }
    @Override public String getName() { return "Herbal Witch entity tags"; }
    @Override protected void addTags(HolderLookup.Provider lookup) {
        PreparationEntityTagContributions.addTo(this::tag);
        AnimalTagContributions.entities(this::tag);
    }
}
