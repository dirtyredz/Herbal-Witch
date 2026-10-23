package io.github.dirtyredz.herbalwitch.companions.familiars.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationCreativeTab;
import io.github.dirtyredz.herbalwitch.companions.familiars.catalog.FamiliarHomeKind;
import io.github.dirtyredz.herbalwitch.companions.familiars.home.*;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacementItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import java.util.*;

public final class FamiliarHomes {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HerbalWitch.MOD_ID);
    public record Entry(FamiliarHomeKind kind, RegistryObject<FamiliarHomeBlock> block, RegistryObject<Item> item) {}
    public static final List<Entry> ALL = Arrays.stream(FamiliarHomeKind.values()).map(kind -> {
        var block = BLOCKS.register(kind.path(), () -> new FamiliarHomeBlock(kind, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.BLOCK)));
        RegistryObject<Item> item = ITEMS.register(kind.path(), () -> new StructurePlacementItem(block.get(), new Item.Properties()));
        return new Entry(kind, block, item);
    }).toList();
    public static final RegistryObject<BlockEntityType<FamiliarHomeBlockEntity>> TYPE = TYPES.register("familiars/home",
            () -> BlockEntityType.Builder.of(FamiliarHomeBlockEntity::new, ALL.stream().map(e -> e.block().get()).toArray(Block[]::new)).build(null));
    private FamiliarHomes() {}
    public static Entry get(FamiliarHomeKind kind) { return ALL.get(kind.ordinal()); }
    public static void register(IEventBus bus) {
        BLOCKS.register(bus); ITEMS.register(bus); TYPES.register(bus);
        bus.addListener((net.minecraftforge.event.BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey().equals(StationCreativeTab.APOTHECARY.getKey())) ALL.forEach(e -> event.accept(e.item().get()));
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(FamiliarHomeRest::tick);
        io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry.register(new io.github.dirtyredz.herbalwitch.companions.familiars.showcase.FamiliarHomeShowcaseSection());
    }
}
