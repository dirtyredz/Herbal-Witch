package io.github.dirtyredz.herbalwitch.data.client.decor;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.data.client.decor.state.AssemblyStates;
import io.github.dirtyredz.herbalwitch.data.client.decor.state.BedStates;
import io.github.dirtyredz.herbalwitch.data.client.decor.state.DoorStates;
import io.github.dirtyredz.herbalwitch.data.client.decor.state.FurnitureStates;
import io.github.dirtyredz.herbalwitch.data.client.decor.state.LightStates;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Furniture blockstates over the compiled Blockbench models, which bind their own textures: with one
 * design there is nothing to rebind, so no wrapper models are generated. Each behaviour's state
 * mapping lives in {@code state/}; this class names the compiled models and dispatches.
 *
 * <p>Model names (docs/BLOCKBENCH.md): the five 0.1.0 pieces are templates under
 * {@code block/decor/templates/} (the lamp adds {@code _off}/{@code _on}); every other piece has a
 * directory under {@code block/decor/pieces/<family>/<group>/<piece>/} holding {@code base} (one
 * cell), {@code off}/{@code on} (a one-cell light), or {@code part_<n>}, {@code lit_<n>},
 * {@code mirrored_<n>} and {@code assembly} (a multiblock; the assembly is the item).
 */
public class DecorModelProvider extends BlockStateProvider {
    public DecorModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
    }

    @Override
    public String getName() {
        return "Herbal Witch decor blockstates";
    }

    /** The model an item of the piece shows: the whole piece, unlit. */
    public static String itemModel(FurnitureSpec spec) {
        boolean light = spec.behavior() == FurnitureBehavior.LIGHT;
        if (spec.template() != null) {
            return spec.modelRoot() + (light ? "_off" : "");
        }
        return spec.modelRoot() + "/" + (spec.multiblock() ? "assembly" : light ? "off" : "base");
    }

    /** A one-cell model by name ({@code base}, {@code off}, {@code on}; a template ignores the name unless lit). */
    public static String cellModel(FurnitureSpec spec, String name) {
        if (spec.template() != null) {
            return spec.modelRoot() + (spec.behavior() == FurnitureBehavior.LIGHT ? "_" + name : "");
        }
        return spec.modelRoot() + "/" + name;
    }

    /** A multiblock cell model: {@code <kind>_<index>} with kind part, lit or mirrored. */
    public static String partModel(FurnitureSpec spec, String kind, int index) {
        return spec.modelRoot() + "/" + kind + "_" + index;
    }

    /** Templates face north; the blockstate turns them clockwise to the block's facing. */
    public static int rotationY(Direction facing) {
        return ((int) facing.toYRot() + 180) % 360;
    }

    public ModelFile existing(String path) {
        return models().getExistingFile(modLoc(path));
    }

    @Override
    protected void registerStatesAndModels() {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            FurnitureSpec spec = entry.spec();
            Block block = entry.block().get();
            switch (spec.behavior()) {
                case LIGHT -> LightStates.add(this, spec, block);
                case BED -> BedStates.add(this, spec, block);
                case DOOR -> DoorStates.add(this, spec, block);
                default -> {
                    if (spec.multiblock()) {
                        AssemblyStates.add(this, spec, block);
                    } else {
                        FurnitureStates.add(this, spec, block);
                    }
                }
            }
        }
    }
}
