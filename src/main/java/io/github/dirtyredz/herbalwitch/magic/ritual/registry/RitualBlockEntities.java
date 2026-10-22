package io.github.dirtyredz.herbalwitch.magic.ritual.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ritual.altar.AltarBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ritual.catalog.AltarCatalog;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The altar's origin anchor, {@code magic/ritual/altar} (only cell 0 has one). */
public final class RitualBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HerbalWitch.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // vanilla's builder takes a null data-fixer type
    public static final RegistryObject<BlockEntityType<AltarBlockEntity>> ALTAR = TYPES.register(AltarCatalog.PATH,
            () -> BlockEntityType.Builder.of(AltarBlockEntity::new, RitualBlocks.ALTAR.get()).build(null));

    private RitualBlockEntities() {
    }
}
