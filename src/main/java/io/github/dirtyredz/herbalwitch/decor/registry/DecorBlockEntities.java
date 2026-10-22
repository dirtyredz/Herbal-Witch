package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.FurnitureStorageEntity;
import io.github.dirtyredz.herbalwitch.decor.behavior.storage.StorageFurnitureBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Decor's block entity types: one storage entity type shared by every cabinet and display. */
public final class DecorBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HerbalWitch.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // vanilla's builder takes a null data fixer type
    public static final RegistryObject<BlockEntityType<FurnitureStorageEntity>> STORAGE = TYPES.register("decor/storage",
            () -> BlockEntityType.Builder.of(FurnitureStorageEntity::new, DecorBlocks.all().stream()
                    .filter(b -> b instanceof StorageFurnitureBlock).toArray(Block[]::new)).build(null));

    private DecorBlockEntities() {
    }
}
