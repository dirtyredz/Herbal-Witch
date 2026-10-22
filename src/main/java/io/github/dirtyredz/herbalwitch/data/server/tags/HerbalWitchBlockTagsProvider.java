package io.github.dirtyredz.herbalwitch.data.server.tags;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.data.server.botany.BotanyTagContributions;
import io.github.dirtyredz.herbalwitch.data.server.compat.CompatibilityTagContributions;
import io.github.dirtyredz.herbalwitch.data.server.decor.DecorBlockTags;
import io.github.dirtyredz.herbalwitch.data.server.grimoire.GrimoireTagContributions;
import io.github.dirtyredz.herbalwitch.data.server.preparations.PreparationTagContributions;
import io.github.dirtyredz.herbalwitch.data.server.stations.StationTagContributions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

/**
 * The only writer of block tags. Features share tag files (vanilla {@code mineable/axe}, crops), and
 * two providers writing one path would overwrite each other, so every feature contributes here.
 */
public class HerbalWitchBlockTagsProvider extends BlockTagsProvider {
    /** What a feature's contribution may do: open (or reopen) a tag builder. */
    @FunctionalInterface
    public interface Sink {
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tag(TagKey<Block> key);
    }

    public HerbalWitchBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                        ExistingFileHelper existingFiles) {
        super(output, lookup, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch block tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        Sink sink = this::tag;
        DecorBlockTags.addTo(sink);
        BotanyTagContributions.blocks(sink);
        StationTagContributions.blocks(sink);
        PreparationTagContributions.blocks(sink);
        CompatibilityTagContributions.blocks(sink);
        GrimoireTagContributions.blocks(sink);
    }
}
