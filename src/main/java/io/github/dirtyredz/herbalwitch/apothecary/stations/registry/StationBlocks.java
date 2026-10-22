package io.github.dirtyredz.herbalwitch.apothecary.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.AdvancedStationBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.bottling_bench.BottlingBenchShapes;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.cauldron.CauldronShapes;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.charm_workbench.CharmWorkbenchShapes;
import io.github.dirtyredz.herbalwitch.apothecary.stations.advanced.still.StillShapes;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.DryingRackBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.HearthBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.infusion_pot.InfusionPotBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.kettle.KettleBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.mortar.MortarBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.stove.StoveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The station blocks, ids {@code stations/<station>/station}. */
public final class StationBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);
    private static final Map<StationSpec, RegistryObject<Block>> BY_SPEC = new LinkedHashMap<>();

    public static final RegistryObject<Block> DRYING_RACK = register(StationCatalog.DRYING_RACK, () -> new DryingRackBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(1.5F)
                    .sound(SoundType.WOOD).noOcclusion().ignitedByLava()));
    public static final RegistryObject<Block> MORTAR = register(StationCatalog.MORTAR, () -> new MortarBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5F, 6.0F).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> KETTLE = register(StationCatalog.KETTLE, () -> new KettleBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));
    public static final RegistryObject<Block> HEARTH = register(StationCatalog.HEARTH, () -> new HearthBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.5F, 6.0F).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> STOVE = register(StationCatalog.STOVE, () -> new StoveBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));
    public static final RegistryObject<Block> INFUSION_POT = register(StationCatalog.INFUSION_POT, () -> new InfusionPotBlock(
            BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_WHITE).strength(1.5F, 6.0F).sound(SoundType.STONE)
                    .noOcclusion()));
    public static final RegistryObject<Block> CAULDRON = register(StationCatalog.CAULDRON, () -> AdvancedStationBlock.create(
            working(MapColor.COLOR_BLACK, SoundType.METAL, 2.0F), null, CauldronShapes.CELLS, true, StationBlockEntities.CAULDRON));
    public static final RegistryObject<Block> STILL = register(StationCatalog.STILL, () -> AdvancedStationBlock.create(
            working(MapColor.COLOR_ORANGE, SoundType.COPPER, 2.0F), StationCatalog.STILL.layout(), StillShapes.CELLS, true,
            StationBlockEntities.STILL));
    public static final RegistryObject<Block> BOTTLING_BENCH = register(StationCatalog.BOTTLING_BENCH, () -> AdvancedStationBlock.create(
            working(MapColor.WOOD, SoundType.WOOD, 2.0F).ignitedByLava(), StationCatalog.BOTTLING_BENCH.layout(), BottlingBenchShapes.CELLS,
            false, StationBlockEntities.BOTTLING_BENCH));
    public static final RegistryObject<Block> CHARM_WORKBENCH = register(StationCatalog.CHARM_WORKBENCH, () -> AdvancedStationBlock.create(
            working(MapColor.WOOD, SoundType.WOOD, 2.0F).ignitedByLava(), StationCatalog.CHARM_WORKBENCH.layout(),
            CharmWorkbenchShapes.CELLS, false, StationBlockEntities.CHARM_WORKBENCH));

    /** A production station's properties: a low light while it works. */
    private static BlockBehaviour.Properties working(MapColor color, SoundType sound, float strength) {
        return BlockBehaviour.Properties.of().mapColor(color).strength(strength, 6.0F).sound(sound).noOcclusion()
                .pushReaction(PushReaction.BLOCK).lightLevel(state -> state.getValue(AdvancedStationBlock.ACTIVE) ? ACTIVE_LIGHT : 0);
    }

    /** Light a production station gives while it works. */
    public static final int ACTIVE_LIGHT = 6;

    private StationBlocks() {
    }

    private static RegistryObject<Block> register(StationSpec spec, java.util.function.Supplier<Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(spec.path(), factory);
        BY_SPEC.put(spec, block);
        return block;
    }

    public static Block block(StationSpec spec) {
        return BY_SPEC.get(spec).get();
    }

    /** Catalogue order. */
    public static List<Block> all() {
        return StationCatalog.ALL.stream().map(StationBlocks::block).toList();
    }
}
