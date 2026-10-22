package io.github.dirtyredz.herbalwitch.magic.ward.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.grimoire.WardEntries;
import io.github.dirtyredz.herbalwitch.magic.ward.menu.WardMenu;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.magic.ward.showcase.WardShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ward.targeting.WardProtection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationCreativeTab;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Wires home wards (Milestone 7): the ward stone block, its plain item (a fresh stone is always
 * uncharged), its record-reference entity and zero-slot menu, target suppression, the index's server
 * lifecycle, the grimoire pages, the Apothecary tab entry and the showcase section.
 */
public final class WardRegistration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, HerbalWitch.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, HerbalWitch.MOD_ID);

    public static final RegistryObject<Block> STONE = BLOCKS.register(WardCatalog.PATH, () -> new WardStoneBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.STONE).strength(2.5F, 6.0F).sound(SoundType.STONE).noOcclusion().requiresCorrectToolForDrops()
            .pushReaction(PushReaction.BLOCK).lightLevel(WardStoneBlock::light)));
    public static final RegistryObject<Item> STONE_ITEM = ITEMS.register(WardCatalog.PATH, () -> new BlockItem(STONE.get(), new Item.Properties()));
    @SuppressWarnings("DataFlowIssue") // vanilla's builder takes a null data-fixer type
    public static final RegistryObject<BlockEntityType<WardStoneBlockEntity>> STONE_ENTITY = TYPES.register(WardCatalog.PATH,
            () -> BlockEntityType.Builder.of(WardStoneBlockEntity::new, STONE.get()).build(null));
    public static final RegistryObject<MenuType<WardMenu>> MENU = MENUS.register(WardCatalog.PATH, () -> IForgeMenuType.create(WardMenu::new));

    private WardRegistration() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        TYPES.register(modBus);
        MENUS.register(modBus);
        modBus.addListener(WardRegistration::creativeTab);
        IEventBus forge = MinecraftForge.EVENT_BUS;
        forge.addListener(WardService::serverStarted);
        forge.addListener(WardService::serverStopped);
        forge.addListener(WardProtection::onChangeTarget);
        GrimoireContent.register(GrimoireContent.Order.WARD, new WardEntries());
        ShowcaseRegistry.register(new WardShowcaseSection());
    }

    private static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(StationCreativeTab.APOTHECARY.getKey())) {
            event.accept(STONE_ITEM.get());
        }
    }
}
