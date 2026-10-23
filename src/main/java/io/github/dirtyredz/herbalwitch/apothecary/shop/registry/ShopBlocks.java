package io.github.dirtyredz.herbalwitch.apothecary.shop.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.shop.catalog.ShopPieceSpec;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopOrderBoardBlock;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopRegisterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The four commerce blocks, in catalogue order. None can be pushed by pistons. */
public final class ShopBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);
    private static final Map<ShopPieceSpec, RegistryObject<ShopFixtureBlock>> ALL = new LinkedHashMap<>();
    public static final RegistryObject<ShopFixtureBlock> REGISTER = add(ShopPieceCatalog.REGISTER);
    public static final RegistryObject<ShopFixtureBlock> STOCK_SHELF = add(ShopPieceCatalog.STOCK_SHELF);
    public static final RegistryObject<ShopFixtureBlock> STOCK_JAR = add(ShopPieceCatalog.STOCK_JAR);
    public static final RegistryObject<ShopFixtureBlock> ORDER_BOARD = add(ShopPieceCatalog.ORDER_BOARD);

    private ShopBlocks() {
    }

    private static RegistryObject<ShopFixtureBlock> add(ShopPieceSpec spec) {
        RegistryObject<ShopFixtureBlock> block = BLOCKS.register(spec.path(), () -> create(spec));
        ALL.put(spec, block);
        return block;
    }

    private static ShopFixtureBlock create(ShopPieceSpec spec) {
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F)
                .sound(spec == ShopPieceCatalog.STOCK_JAR ? SoundType.GLASS : SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK);
        if (spec == ShopPieceCatalog.REGISTER) return new ShopRegisterBlock(props);
        if (spec == ShopPieceCatalog.ORDER_BOARD) return new ShopOrderBoardBlock(props);
        return new ShopDisplayBlock(spec, props);
    }

    public static ShopFixtureBlock block(ShopPieceSpec spec) {
        return ALL.get(spec).get();
    }

    public static List<ShopFixtureBlock> all() {
        return ALL.values().stream().map(RegistryObject::get).toList();
    }

    public static List<ShopFixtureBlock> displays() {
        return List.of(STOCK_SHELF.get(), STOCK_JAR.get());
    }
}
