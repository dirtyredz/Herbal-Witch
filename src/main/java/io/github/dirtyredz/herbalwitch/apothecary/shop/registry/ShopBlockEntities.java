package io.github.dirtyredz.herbalwitch.apothecary.shop.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopDisplayBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.shop.fixture.block.ShopFixtureBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Identity-only block entities: register, both displays (one type) and the order board. */
public final class ShopBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HerbalWitch.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // vanilla's builder takes a null data-fixer type
    public static final RegistryObject<BlockEntityType<ShopFixtureBlockEntity>> REGISTER = TYPES.register("shop/register",
            () -> BlockEntityType.Builder.<ShopFixtureBlockEntity>of((pos, state) -> new ShopFixtureBlockEntity(ShopBlockEntities.REGISTER.get(), pos, state),
                    ShopBlocks.REGISTER.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<ShopDisplayBlockEntity>> DISPLAY = TYPES.register("shop/display",
            () -> BlockEntityType.Builder.<ShopDisplayBlockEntity>of((pos, state) -> new ShopDisplayBlockEntity(ShopBlockEntities.DISPLAY.get(), pos, state),
                    ShopBlocks.STOCK_SHELF.get(), ShopBlocks.STOCK_JAR.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<ShopFixtureBlockEntity>> ORDER_BOARD = TYPES.register("shop/order_board",
            () -> BlockEntityType.Builder.<ShopFixtureBlockEntity>of((pos, state) -> new ShopFixtureBlockEntity(ShopBlockEntities.ORDER_BOARD.get(), pos, state),
                    ShopBlocks.ORDER_BOARD.get()).build(null));

    private ShopBlockEntities() {
    }
}
