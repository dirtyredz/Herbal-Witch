package io.github.dirtyredz.herbalwitch.data.server.tags;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.data.server.botany.BotanyTagContributions;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorItemTags;
import io.github.dirtyredz.herbalwitch.data.server.magic.charm.CharmTagContributions;
import io.github.dirtyredz.herbalwitch.data.server.preparations.PreparationTagContributions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** The only writer of item tags; features contribute through {@link Sink}. */
public class HerbalWitchItemTagsProvider extends ItemTagsProvider {
    /** Open an item tag builder, or copy a block tag's members into an item tag. */
    public interface Sink {
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tag(TagKey<Item> key);

        void copy(TagKey<Block> blockTag, TagKey<Item> itemTag);
    }

    public HerbalWitchItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                       CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFiles) {
        super(output, lookup, blockTags, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch item tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        Sink sink = new Sink() {
            @Override
            public IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tag(TagKey<Item> key) {
                return HerbalWitchItemTagsProvider.this.tag(key);
            }

            @Override
            public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
                HerbalWitchItemTagsProvider.this.copy(blockTag, itemTag);
            }
        };
        DecorItemTags.addTo(sink);
        CharmTagContributions.items(sink);
        BotanyTagContributions.items(sink);
        PreparationTagContributions.items(sink);
        io.github.dirtyredz.herbalwitch.data.server.animals.AnimalTagContributions.items(sink);
    }
}
