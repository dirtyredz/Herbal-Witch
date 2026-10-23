package io.github.dirtyredz.herbalwitch.data.client.stations.shop;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopRegisterBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.registry.ShopBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * Commerce blockstates (every facing; the register also open/closed) over the compiled Blockbench
 * models, and item models that only name their compiled parent (display lives in the project).
 */
public final class ShopModelProvider extends BlockStateProvider {
    public ShopModelProvider(PackOutput output, ExistingFileHelper files) {
        super(output, HerbalWitch.MOD_ID, files);
    }

    @Override
    public String getName() {
        return "Herbal Witch commerce";
    }

    @Override
    protected void registerStatesAndModels() {
        for (ShopPieceSpec spec : ShopPieceCatalog.ALL) {
            String root = "block/" + spec.path() + "/";
            ModelFile item = models().getExistingFile(modLoc(root + (spec == ShopPieceCatalog.REGISTER ? "closed" : "base")));
            getVariantBuilder(ShopBlocks.block(spec)).forAllStates(state -> ConfiguredModel.builder()
                    .modelFile(models().getExistingFile(modLoc(root + model(spec, state)))).rotationY(rotation(state)).build());
            itemModels().getBuilder("item/" + spec.path()).parent(item);
        }
    }

    private static String model(ShopPieceSpec spec, BlockState state) {
        if (spec != ShopPieceCatalog.REGISTER) return "base";
        return state.getValue(ShopRegisterBlock.OPEN) ? "open" : "closed";
    }

    private static int rotation(BlockState state) {
        return ((int) state.getValue(ShopFixtureBlock.FACING).toYRot() + 180) % 360;
    }
}
