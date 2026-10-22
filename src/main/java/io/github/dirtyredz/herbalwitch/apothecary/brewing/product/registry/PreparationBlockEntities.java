package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.lighting.OilLanternBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The oil lantern's fuel holder. */
public final class PreparationBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HerbalWitch.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // vanilla's builder takes a null data-fixer type
    public static final RegistryObject<BlockEntityType<OilLanternBlockEntity>> OIL_LANTERN = TYPES.register(
            PreparationBlocks.OIL_LANTERN_PATH,
            () -> BlockEntityType.Builder.of(OilLanternBlockEntity::new, PreparationBlocks.OIL_LANTERN.get()).build(null));

    private PreparationBlockEntities() {
    }
}
