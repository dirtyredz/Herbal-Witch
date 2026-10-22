package io.github.dirtyredz.herbalwitch.apothecary.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench.BottlingBenchBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.CauldronBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.charm_workbench.CharmWorkbenchBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.still.StillBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.DryingRackBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot.InfusionPotBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.KettleBlockEntity;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.mortar.MortarBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** One block entity type per station, under the station's id; the stove shares the hearth's (same fuel behaviour). */
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
            "stations/hearth/station", () -> BlockEntityType.Builder.of(HearthBlockEntity::new, StationBlocks.HEARTH.get(), StationBlocks.STOVE.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<InfusionPotBlockEntity>> INFUSION_POT = TYPES.register(
            "stations/infusion_pot/station", () -> BlockEntityType.Builder.of(InfusionPotBlockEntity::new, StationBlocks.INFUSION_POT.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<CauldronBlockEntity>> CAULDRON = TYPES.register(
            "stations/cauldron/station", () -> BlockEntityType.Builder.of(CauldronBlockEntity::new, StationBlocks.CAULDRON.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<StillBlockEntity>> STILL = TYPES.register(
            "stations/still/station", () -> BlockEntityType.Builder.of(StillBlockEntity::new, StationBlocks.STILL.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<BottlingBenchBlockEntity>> BOTTLING_BENCH = TYPES.register(
            "stations/bottling_bench/station", () -> BlockEntityType.Builder.of(BottlingBenchBlockEntity::new, StationBlocks.BOTTLING_BENCH.get())
                    .build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistryObject<BlockEntityType<CharmWorkbenchBlockEntity>> CHARM_WORKBENCH = TYPES.register(
            "stations/charm_workbench/station", () -> BlockEntityType.Builder.of(CharmWorkbenchBlockEntity::new,
                    StationBlocks.CHARM_WORKBENCH.get()).build(null));

    private StationBlockEntities() {
    }
}
