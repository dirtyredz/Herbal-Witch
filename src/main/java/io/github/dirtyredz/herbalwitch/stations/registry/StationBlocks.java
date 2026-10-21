package io.github.dirtyredz.herbalwitch.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.block.InfusionPotBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.kettle.block.KettleBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block.MortarBlock;
import io.github.dirtyredz.herbalwitch.stations.everyday.stove.block.StoveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
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
