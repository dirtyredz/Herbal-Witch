package io.github.dirtyredz.herbalwitch.compat;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.catalog.ProductSpec;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.platform.compat.CompatibilityRegistration;
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
