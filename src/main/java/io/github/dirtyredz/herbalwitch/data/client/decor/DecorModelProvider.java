package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.block.TableLampBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.Finish;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Finish bindings and blockstates. Each piece's geometry is one authored template
 * ({@code models/block/decor/templates/}); each finish gets a small wrapper model at
 * {@code block/<id>} that binds only the finish's frame material (colours come from the tint
 * handlers). Lamps also get a lit wrapper at {@code decor/<family>/<piece>/lit/<palette>/<finish>},
 * a sibling of the palette directories so each leaf directory stays under 12 entries.
 */
public class DecorModelProvider extends BlockStateProvider {
    private static final String TEMPLATES = "block/decor/templates/";
    private static final String FRAME = "frame";

    public DecorModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch decor blockstates and finish models";
    }

    /** Model path of the lit lamp in a finish. */
    private static String litPath(FurnitureSpec spec, Finish finish) {
        return spec.pieceDirectory() + "/lit/" + finish.palette().directory() + "/" + finish.id();
    }

    @Override
    protected void registerStatesAndModels() {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            FurnitureSpec spec = entry.spec();
            Finish finish = entry.finish();
            Block block = entry.block().get();
            switch (spec.behavior()) {
                case FREESTANDING, WALL_MOUNTED -> horizontal(block, wrapper(spec.path(finish), spec.template(), finish));
                case TABLE_LAMP -> {
                    ModelFile off = wrapper(spec.path(finish), spec.template() + "_off", finish);
                    ModelFile on = wrapper(litPath(spec, finish), spec.template() + "_on", finish);
                    getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(state.getValue(TableLampBlock.LIT) ? on : off).build());
                }
                case RUG -> simpleBlock(block, models().withExistingParent("block/" + spec.path(finish),
                        modLoc(TEMPLATES + spec.template())));
            }
        }
    }

    private ModelFile wrapper(String path, String template, Finish finish) {
        return models().withExistingParent("block/" + path, modLoc(TEMPLATES + template))
                .texture(FRAME, frameTexture(finish));
    }

    private static ResourceLocation frameTexture(Finish finish) {
        return ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "block/material/" + finish.frame().texture());
    }

    /** Templates face north; the blockstate turns them clockwise to the block's facing. */
    private void horizontal(Block block, ModelFile model) {
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            return ConfiguredModel.builder().modelFile(model).rotationY(((int) facing.toYRot() + 180) % 360).build();
        });
    }
}
