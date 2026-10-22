package io.github.dirtyredz.herbalwitch.compat;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.grimoire.content.ServerGrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.recipe.StationRecipeEntries;
import io.github.dirtyredz.herbalwitch.platform.compat.CompatibilityRegistration;
import io.github.dirtyredz.herbalwitch.stations.description.ProcessingCategories;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipe;
import io.github.dirtyredz.herbalwitch.platform.compat.thirst.ThirstIntegration;
import io.github.dirtyredz.herbalwitch.stations.heat.HeatSources;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.Map;

import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.place;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Optional integrations, whichever runtime profile is active (-PcompatProfile). Foreign blocks are
 * looked up by id and foreign classes only by name, so these tests load without the mods; with
 * them absent they check that nothing pretends otherwise.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CompatibilityGameTests {
    private static final ResourceLocation FD_STOVE = ResourceLocation.fromNamespaceAndPath("farmersdelight", "stove");

    private CompatibilityGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void farmersDelightStoveHeatsWhenLitAndTheHearthHeatsItsPots(GameTestHelper helper) {
        floor(helper);
        boolean present = ModList.get().isLoaded(CompatibilityRegistration.FARMERS_DELIGHT);
        Block stove = ForgeRegistries.BLOCKS.getValue(FD_STOVE);
        check(present == (stove != null && ForgeRegistries.BLOCKS.containsKey(FD_STOVE)), "stove registration does not match mod presence");
        check(StationBlocks.HEARTH.get().defaultBlockState().is(BlockTags.create(
                ResourceLocation.fromNamespaceAndPath("farmersdelight", "heat_sources"))), "hearth missing from FD heat sources");
        if (present) {
            BlockPos under = new BlockPos(4, 1, 4);
            BlockState lit = stove.defaultBlockState().setValue(BlockStateProperties.LIT, true);
            helper.setBlock(under, lit);
            place(helper, StationBlocks.KETTLE.get(), under.above());
            check(HeatSources.isHeated(helper.getLevel(), helper.absolutePos(under.above())), "a lit stove does not heat the kettle");
            helper.setBlock(under, lit.setValue(BlockStateProperties.LIT, false));
            check(!HeatSources.isHeated(helper.getLevel(), helper.absolutePos(under.above())), "an unlit stove heats the kettle");
        }
        helper.succeed();
    }

    /**
     * The grimoire and the viewers share one category mapping and the recipes' own descriptions:
     * every recipe the viewers would list is a grimoire page in the same category, at the same
     * station, holding the same description object. Viewer rendering itself is client-only.
     */
    @GameTest(template = TEMPLATE)
    public static void grimoireAndViewersAgreeOnCategoriesAndDescriptions(GameTestHelper helper) {
        GrimoireContent content = ServerGrimoireContent.get(helper.getLevel().getServer());
        for (ProcessingCategories.Category category : ProcessingCategories.ALL) {
            check(ProcessingCategories.byStation(category.station()).orElseThrow() == category, category.id() + " station mapping");
            for (ProcessRecipe recipe : helper.getLevel().getRecipeManager().getAllRecipesFor(category.type().get())) {
                check(ProcessingCategories.byType(recipe.getType()).orElseThrow() == category, recipe.getId() + " type mapping");
                GrimoireEntry page = content.get(StationRecipeEntries.key(recipe.getId())).orElseThrow();
                check(page.category().equals(category.id()), recipe.getId() + " is under " + page.category());
                check(page.recipe().orElseThrow().description() == recipe.description(), recipe.getId() + " copied its description");
                check(recipe.description().heated() == category.heated(), recipe.getId() + " heat disagrees with its category");
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void thirstRegistersEveryKettleDrinkOnce(GameTestHelper helper) throws Exception {
        boolean present = ModList.get().isLoaded(CompatibilityRegistration.THIRST);
        check(ThirstIntegration.active() == present, "Thirst adapter state does not match mod presence");
        if (present) {
            Class<?> helperClass = Class.forName("dev.ghen.thirst.api.ThirstHelper");
            Field drinks = helperClass.getField("VALID_DRINKS");
            Map<?, ?> table = (Map<?, ?>) drinks.get(null);
            for (ProductSpec drink : EverydayProductCatalog.DRINKS) {
                Item item = PreparationItems.product(drink);
                Object values = table.get(item);
                check(values instanceof Number[] numbers && numbers[0].intValue() == 6 && numbers[1].intValue() == 3,
                        drink.id() + " hydration " + (values instanceof Number[] n ? java.util.Arrays.toString(n) : values));
            }
        }
        helper.succeed();
    }
}
