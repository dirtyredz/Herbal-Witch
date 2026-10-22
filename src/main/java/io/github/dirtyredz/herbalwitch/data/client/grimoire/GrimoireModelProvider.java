package io.github.dirtyredz.herbalwitch.data.client.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog.ResearchPiece;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Research block states: the piece's authored model ({@code models/block/grimoire/<piece>/base},
 * compiled from its Blockbench project) turned to the block's facing.
 */
public class GrimoireModelProvider extends BlockStateProvider {
    public GrimoireModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch grimoire blockstates";
    }

    public static String model(ResearchPiece piece) {
        return "block/" + piece.path() + "/base";
    }

    @Override
    protected void registerStatesAndModels() {
        for (ResearchPiece piece : GrimoireCatalog.RESEARCH_PIECES) {
            ModelFile base = models().getExistingFile(modLoc(model(piece)));
            getVariantBuilder(GrimoireBlocks.block(piece)).forAllStates(state -> ConfiguredModel.builder().modelFile(base)
                    .rotationY(rotation(state.getValue(HorizontalDirectionalBlock.FACING))).build());
        }
    }

    /** Models face north; the blockstate turns them clockwise to the facing. */
    static int rotation(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }
}
