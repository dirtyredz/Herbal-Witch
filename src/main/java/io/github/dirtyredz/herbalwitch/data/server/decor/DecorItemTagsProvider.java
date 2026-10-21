package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/** {@code herbalwitch:furniture} copied from the block tag, plus one {@code herbalwitch:decor/<piece>} tag per piece. */
public class DecorItemTagsProvider extends ItemTagsProvider {
    public DecorItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                 CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper existingFiles) {
        super(output, lookup, blockTags, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(DecorTags.FURNITURE, DecorTags.FURNITURE_ITEMS);
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            tag(DecorTags.pieceItems(entry.spec())).add(entry.block().get().asItem());
        }
    }
}
