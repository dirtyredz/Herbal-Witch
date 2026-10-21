package io.github.dirtyredz.herbalwitch.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.block.InfusionPotBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.kettle.block.KettleBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block.MortarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** One block entity type per station, under the station's id. */
public final class StationBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HerbalWitch.MOD_ID);

    @SuppressWarnings("DataFlowIssue") // vanilla's builder takes a null data-fixer type
    public static final RegistryObject<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK = TYPES.register(
            "stations/drying_rack/station", () -> BlockEntityType.Builder.of(DryingRackBlockEntity::new, StationBlocks.DRYING_RACK.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<MortarBlockEntity>> MORTAR = TYPES.register(
            "stations/mortar/station", () -> BlockEntityType.Builder.of(MortarBlockEntity::new, StationBlocks.MORTAR.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<KettleBlockEntity>> KETTLE = TYPES.register(
            "stations/kettle/station", () -> BlockEntityType.Builder.of(KettleBlockEntity::new, StationBlocks.KETTLE.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<HearthBlockEntity>> HEARTH = TYPES.register(
            "stations/hearth/station", () -> BlockEntityType.Builder.of(HearthBlockEntity::new, StationBlocks.HEARTH.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<InfusionPotBlockEntity>> INFUSION_POT = TYPES.register(
            "stations/infusion_pot/station", () -> BlockEntityType.Builder.of(InfusionPotBlockEntity::new, StationBlocks.INFUSION_POT.get()).build(null));

    private StationBlockEntities() {
    }
}
