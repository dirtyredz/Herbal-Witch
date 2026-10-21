package io.github.dirtyredz.herbalwitch.data.client.botany.garden;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;

import java.util.HashMap;
import java.util.Map;

/**
 * The bed's part models: authored wood and dry soil parts (written by tools/generate-models.mjs),
 * and generated moist soil parts ({@code parts/moist/<name>}) that only rebind {@code #soil}.
 */
final class HerbBedModels {
    private final BlockStateProvider states;
    private final Map<String, ModelFile> moist = new HashMap<>();

    HerbBedModels(BlockStateProvider states) {
        this.states = states;
    }

    ModelFile wood(String name) {
        return states.models().getExistingFile(states.modLoc(HerbBedBlockstates.PARTS + "wood/" + name));
    }

    ModelFile dry(String name) {
        return states.models().getExistingFile(states.modLoc(HerbBedBlockstates.PARTS + "soil/" + name));
    }

    ModelFile moist(String name) {
        return moist.computeIfAbsent(name, n -> states.models()
                .withExistingParent(HerbBedBlockstates.PARTS + "moist/" + n, states.modLoc(HerbBedBlockstates.PARTS + "soil/" + n))
                .texture("soil", ResourceLocation.fromNamespaceAndPath(HerbalWitch.MOD_ID, "block/botany/garden/soil_moist")));
    }
}
