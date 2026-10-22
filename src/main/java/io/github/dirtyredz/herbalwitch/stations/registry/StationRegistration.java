package io.github.dirtyredz.herbalwitch.stations.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.AdvancedProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.MixtureCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.AdvancedPreparations;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.stations.advanced.bottling_bench.block.BottlingBenchBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.advanced.cauldron.block.CauldronBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.advanced.charm_workbench.block.CharmWorkbenchBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.advanced.still.block.StillBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.stations.everyday.drying_rack.block.DryingRackBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.hearth.block.HearthBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.infusion_pot.block.InfusionPotBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.kettle.block.KettleBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.everyday.mortar.block.MortarBlockEntity;
import io.github.dirtyredz.herbalwitch.stations.process.authorization.StationOperations;
import io.github.dirtyredz.herbalwitch.stations.process.recipe.ProcessRecipeIndex;
import io.github.dirtyredz.herbalwitch.stations.showcase.StationShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Map;

/** Wires the stations: blocks, items, block entities, menus, recipe types, the tab, reload tracking and showcases. */
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
        MinecraftForge.EVENT_BUS.addListener(StationOperations::loggedOut);
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
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.STOVE, false, recipeKey("stove"),
                () -> Map.of(HearthBlockEntity.FUEL, new ItemStack(Items.COAL, StationShowcaseSection.DEMO_FUEL))));
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.INFUSION_POT, true, recipeKey("infusion_pot"),
                () -> Map.of(InfusionPotBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.YARROW), 2),
                        InfusionPotBlockEntity.INPUT_1, new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE), 2),
                        InfusionPotBlockEntity.BASE, new ItemStack(Items.HONEY_BOTTLE, 2),
                        InfusionPotBlockEntity.PACKAGING, new ItemStack(Items.BOWL, 2),
                        InfusionPotBlockEntity.OUTPUT, new ItemStack(PreparationItems.product(EverydayProductCatalog.HEALING_SALVE)))));
        registerAdvancedShowcases();
    }

    /** The production stations (Milestone 7), stocked but never started: a witch must start each batch herself. */
    private static void registerAdvancedShowcases() {
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.CAULDRON, true, recipeKey("cauldron"),
                () -> Map.of(CauldronBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.MUGWORT), 2),
                        CauldronBlockEntity.INPUT_1, new ItemStack(Items.CARROT, 2),
                        CauldronBlockEntity.BASE, water(),
                        CauldronBlockEntity.OUTPUT, new ItemStack(AdvancedPreparations.item(
                                MixtureCatalog.of(AdvancedProductCatalog.NIGHT_VISION).orElseThrow().spec())))));
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.STILL, true, recipeKey("still"),
                () -> Map.of(StillBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.MUGWORT), 2),
                        StillBlockEntity.INPUT_1, new ItemStack(Items.AMETHYST_SHARD, 2),
                        StillBlockEntity.BASE, water(),
                        StillBlockEntity.PACKAGING, new ItemStack(Items.GLASS_BOTTLE, 2),
                        StillBlockEntity.OUTPUT, new ItemStack(AdvancedPreparations.item(AdvancedProductCatalog.MOON_ESSENCE)))));
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.BOTTLING_BENCH, false, recipeKey("bottling_bench"),
                () -> Map.of(BottlingBenchBlockEntity.INPUT_0, new ItemStack(AdvancedPreparations.item(
                                MixtureCatalog.of(AdvancedProductCatalog.NIGHT_VISION).orElseThrow().spec())),
                        BottlingBenchBlockEntity.PACKAGING, new ItemStack(Items.GLASS_BOTTLE, 8),
                        BottlingBenchBlockEntity.OUTPUT, new ItemStack(AdvancedPreparations.item(AdvancedProductCatalog.NIGHT_VISION), 4))));
        ShowcaseRegistry.register(new StationShowcaseSection(StationCatalog.CHARM_WORKBENCH, false, recipeKey("charm_workbench"),
                () -> Map.of(CharmWorkbenchBlockEntity.INPUT_0, new ItemStack(PreparationItems.powder(HerbCatalog.SAGE)),
                        CharmWorkbenchBlockEntity.INPUT_1, new ItemStack(PreparationItems.powder(HerbCatalog.LAVENDER)),
                        CharmWorkbenchBlockEntity.INPUT_2, new ItemStack(Items.STRING, 2))));
    }

    private static ItemStack water() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
    }
}
