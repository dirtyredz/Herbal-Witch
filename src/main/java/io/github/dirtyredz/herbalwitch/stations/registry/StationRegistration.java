package io.github.dirtyredz.herbalwitch.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.block.InfusionPotBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.kettle.block.KettleBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block.MortarBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeIndex;
import io.github.dirtyredz.herbalwitch.stations.showcase.StationShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;

/** Wires the five stations: blocks, items, block entities, menus, recipe types, the tab, reload tracking and showcases. */
public final class StationRegistration {
    /** Showcase recipe line keys, {@code showcase.herbalwitch.recipe.<station>}. */
    public static String recipeKey(String station) {
        return "showcase." + HerbalWitch.MOD_ID + ".recipe." + station;
    }

    private StationRegistration() {
    }

    /** Called once from the mod constructor. */
    public static void register(IEventBus modBus) {
        StationBlocks.BLOCKS.register(modBus);
        StationItems.ITEMS.register(modBus);
        StationBlockEntities.TYPES.register(modBus);
        StationMenus.MENUS.register(modBus);
        StationRecipes.TYPES.register(modBus);
        StationRecipes.SERIALIZERS.register(modBus);
        StationCreativeTab.TABS.register(modBus);
        MinecraftForge.EVENT_BUS.addListener(ProcessRecipeIndex::onTagsUpdated);
        registerShowcases();
    }

    private static void registerShowcases() {
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.DRYING_RACK, false, recipeKey("drying_rack"),
                () -> Map.of(DryingRackBlockEntity.INPUT, new ItemStack(BotanyItems.fresh(HerbCatalog.LAVENDER), 4),
                        DryingRackBlockEntity.OUTPUT, new ItemStack(PreparationItems.bundle(HerbCatalog.LAVENDER)))));
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.MORTAR, false, recipeKey("mortar"),
                () -> Map.of(MortarBlockEntity.INPUT_0, new ItemStack(PreparationItems.bundle(HerbCatalog.LAVENDER), 4),
                        MortarBlockEntity.OUTPUT, new ItemStack(PreparationItems.powder(HerbCatalog.LAVENDER)))));
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.KETTLE, true, recipeKey("kettle"),
                () -> Map.of(KettleBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE), 2),
                        KettleBlockEntity.INPUT_1, new ItemStack(PreparationItems.powder(HerbCatalog.YARROW), 2),
                        KettleBlockEntity.WATER, water(),
                        KettleBlockEntity.OUTPUT, new ItemStack(PreparationItems.product(EverydayProductCatalog.RECOVERY_TEA)))));
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.HEARTH, false, recipeKey("hearth"),
                () -> Map.of(HearthBlockEntity.FUEL, new ItemStack(Items.COAL, StationShowcaseSection.DEMO_FUEL))));
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.INFUSION_POT, true, recipeKey("infusion_pot"),
                () -> Map.of(InfusionPotBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.YARROW), 2),
                        InfusionPotBlockEntity.INPUT_1, new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE), 2),
                        InfusionPotBlockEntity.BASE, new ItemStack(Items.HONEY_BOTTLE, 2),
                        InfusionPotBlockEntity.PACKAGING, new ItemStack(Items.BOWL, 2),
                        InfusionPotBlockEntity.OUTPUT, new ItemStack(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE)))));
    }

    private static ItemStack water() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
    }
}
