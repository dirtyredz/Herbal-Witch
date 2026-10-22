package io.github.dirtyredz.herbalwitch.data.client.magic.ritual;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.circle.RitualCircleBlock;
import io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * The altar's multipart blockstate over its compiled models ({@code models/block/ritual/altar/}): each
 * cell shows its permanent {@code part_<n>}, plus its {@code active_<n>} overlay (glowing runes,
 * candle flames) while a ritual runs, where the overlay reaches that cell; turned to the facing.
 */
public class AltarModelProvider extends BlockStateProvider {
    public static final String DIR = "block/ritual/altar/";
    public static final String CIRCLE_DIR = "block/ritual/circle/";
    private final ExistingFileHelper existing;

    public AltarModelProvider(PackOutput output, ExistingFileHelper existingFiles) {
        super(output, HerbalWitch.MOD_ID, existingFiles);
        this.existing = existingFiles;
    }

    @Override
    public String getName() {
        return "Herbal Witch altar blockstates";
    }

    @Override
    protected void registerStatesAndModels() {
        MultiPartBlockStateBuilder states = getMultipartBuilder(RitualBlocks.ALTAR.get());
        for (int part = 0; part < AltarBlock.LAYOUT.size(); part++) {
            ModelFile base = model("part_" + part);
            ModelFile overlay = exists("active_" + part) ? model("active_" + part) : null;
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                int rotation = ((int) facing.toYRot() + 180) % 360;
                states.part().modelFile(base).rotationY(rotation).addModel()
                        .condition(AltarBlock.FACING, facing).condition(AltarBlock.LAYOUT.part(), part).end();
                if (overlay != null) {
                    states.part().modelFile(overlay).rotationY(rotation).addModel()
                            .condition(AltarBlock.FACING, facing).condition(AltarBlock.LAYOUT.part(), part).condition(AltarBlock.ACTIVE, true).end();
                }
            }
        }
        circle();
    }

    /** The ritual circle: every cell's permanent relief and, while a circle ritual runs, its glowing overlay. */
    private void circle() {
        MultiPartBlockStateBuilder states = getMultipartBuilder(RitualBlocks.CIRCLE.get());
        for (int part = 0; part < RitualCircleBlock.LAYOUT.size(); part++) {
            ModelFile base = models().getExistingFile(modLoc(CIRCLE_DIR + "part_" + part));
            ResourceLocation glow = modLoc(CIRCLE_DIR + "active_" + part);
            ModelFile overlay = existing.exists(glow, PackType.CLIENT_RESOURCES, ".json", "models") ? models().getExistingFile(glow) : null;
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                int rotation = ((int) facing.toYRot() + 180) % 360;
                states.part().modelFile(base).rotationY(rotation).addModel()
                        .condition(RitualCircleBlock.FACING, facing).condition(RitualCircleBlock.LAYOUT.part(), part).end();
                if (overlay != null) {
                    states.part().modelFile(overlay).rotationY(rotation).addModel().condition(RitualCircleBlock.FACING, facing)
                            .condition(RitualCircleBlock.LAYOUT.part(), part).condition(RitualCircleBlock.ACTIVE, true).end();
                }
            }
        }
    }

    private ResourceLocation location(String name) {
        return modLoc(DIR + name);
    }

    private boolean exists(String name) {
        return existing.exists(location(name), PackType.CLIENT_RESOURCES, ".json", "models");
    }

    private ModelFile model(String name) {
        return models().getExistingFile(location(name));
    }
}
