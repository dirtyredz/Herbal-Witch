package io.github.dirtyredz.herbalwitch.data.server.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * Every furniture block is in {@code herbalwitch:furniture}; everything with a frame is axe-mineable
 * (the stone finish too: finishes are appearance only). Rugs break instantly like carpet.
 */
public class DecorBlockTagsProvider extends BlockTagsProvider {
    public DecorBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                  ExistingFileHelper existingFiles) {
        super(output, lookup, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            tag(DecorTags.FURNITURE).add(entry.block().get());
            if (entry.spec().behavior() != FurnitureSpec.Behavior.RUG) {
                tag(BlockTags.MINEABLE_WITH_AXE).add(entry.block().get());
            }
        }
    }
}
