package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.block.TableLampBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Furniture blockstates over the authored templates ({@code models/block/decor/templates/}), which
 * bind their own coloured textures: with one design there is nothing to rebind, so no wrapper
 * models are generated. Lamps switch between their off and on templates.
 */
public class DecorModelProvider extends BlockStateProvider {
    private static final String TEMPLATES = "block/decor/templates/";

    public DecorModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch decor blockstates";
    }

    /** The model an item of the piece shows: its template (the unlit lamp). */
    public static String itemTemplate(FurnitureSpec spec) {
        return TEMPLATES + spec.template() + (spec.behavior() == FurnitureSpec.Behavior.TABLE_LAMP ? "_off" : "");
    }

    @Override
    protected void registerStatesAndModels() {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            FurnitureSpec spec = entry.spec();
            Block block = entry.block().get();
            switch (spec.behavior()) {
                case FREESTANDING, WALL_MOUNTED -> horizontal(block, template(spec.template()));
                case TABLE_LAMP -> {
                    ModelFile off = template(spec.template() + "_off");
                    ModelFile on = template(spec.template() + "_on");
                    getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                            .modelFile(state.getValue(TableLampBlock.LIT) ? on : off).build());
                }
                case RUG -> simpleBlock(block, template(spec.template()));
            }
        }
    }

    private ModelFile template(String name) {
        return models().getExistingFile(modLoc(TEMPLATES + name));
    }

    /** Templates face north; the blockstate turns them clockwise to the block's facing. */
    private void horizontal(Block block, ModelFile model) {
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            return ConfiguredModel.builder().modelFile(model).rotationY(((int) facing.toYRot() + 180) % 360).build();
        });
    }
}
