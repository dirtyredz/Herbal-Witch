package io.github.dirtyredz.herbalwitch.grimoire;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.grimoire.access.menu.GrimoireMenu;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderRequest;
import io.github.dirtyredz.herbalwitch.grimoire.network.ReaderUpdate;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ReaderService;
import io.github.dirtyredz.herbalwitch.grimoire.reading.ingredients.IngredientRequirements;
import io.github.dirtyredz.herbalwitch.stations.process.ProcessRecipeDescription;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.capture;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.carry;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.content;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.openBook;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.recipe;
import static io.github.dirtyredz.herbalwitch.fixture.GrimoireFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.StationFixtures.water;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * Missing-ingredient counts with Forge's real ingredient matching (a water bottle is not any
 * potion), split stacks, the base competing with ingredients, and the reader's server-computed
 * update, none of which ever touch the inventory.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GrimoireIngredientGameTests {
    private static final EntryKey TEA = recipe("processing/steeping/recovery_tea");
    private static final EntryKey SALVE = recipe("processing/infusion/healing_salve");

    private GrimoireIngredientGameTests() {
    }

    private static ProcessRecipeDescription description(GameTestHelper helper, EntryKey key) {
        return content(helper).recipe(key).orElseThrow().description();
    }

    private static List<Integer> available(List<IngredientRequirements.Count> counts) {
        return counts.stream().map(IngredientRequirements.Count::available).toList();
    }

    @GameTest(template = TEMPLATE)
    public static void onlyAWaterBottleIsTheTeasWater(GameTestHelper helper) {
        ProcessRecipeDescription tea = description(helper, TEA);
        ItemStack awkward = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD);
        List<ItemStack> carried = List.of(new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE), 2),
                new ItemStack(PreparationItems.powder(HerbCatalog.YARROW), 2), awkward);
        List<IngredientRequirements.Count> counts = IngredientRequirements.count(tea, carried);
        check(counts.size() == 3, "tea requirements " + counts);
        check(available(counts).equals(List.of(counts.get(0).required(), counts.get(1).required(), 0)),
                "an awkward potion counted as water: " + counts);
        List<ItemStack> withWater = List.of(carried.get(0), carried.get(1), awkward, water());
        check(available(IngredientRequirements.count(tea, withWater)).get(2) == 1, "the water bottle was not counted");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void splitStacksAddUpAndReturnedBottlesNeverCount(GameTestHelper helper) {
        ProcessRecipeDescription salve = description(helper, SALVE);
        List<ItemStack> carried = List.of(new ItemStack(PreparationItems.powder(HerbCatalog.YARROW), 1),
                new ItemStack(PreparationItems.powder(HerbCatalog.YARROW), 1), new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE), 2),
                new ItemStack(Items.GLASS_BOTTLE, 8));
        List<IngredientRequirements.Count> counts = IngredientRequirements.count(salve, carried);
        check(counts.get(0).available() == 1 && counts.get(1).available() == 1, "the powders were not counted: " + counts);
        check(counts.get(2).available() == 0, "the returned glass bottle stood in for the honey base: " + counts);
        ProcessRecipeDescription twoOfOne = new ProcessRecipeDescription(List.of(new ProcessRecipeDescription.Counted(
                Ingredient.of(PreparationItems.powder(HerbCatalog.YARROW)), 2)), Optional.empty(), Optional.empty(),
                new ItemStack(Items.STICK), List.of(), 20, false);
        check(IngredientRequirements.count(twoOfOne, carried).get(0).available() == 2, "two split stacks of one did not add up to two");
        check(counts.get(3).available() == 0, "glass bottles counted as the bowl: " + counts);
        check(carried.get(0).getCount() == 1 && carried.get(3).getCount() == 8, "counting changed the stacks");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theReaderSendsCountsWithoutTouchingTheInventory(GameTestHelper helper) {
        ServerPlayer player = standing(helper, "ingr_reader", new BlockPos(1, 1, 1));
        GrimoireMenu menu = openBook(player);
        carry(player, new ItemStack(PreparationItems.powder(HerbCatalog.CHAMOMILE), 3), water());
        int before = player.getInventory().countItem(PreparationItems.powder(HerbCatalog.CHAMOMILE));
        var sent = capture(() -> {
            ReaderService.handle(player, new ReaderRequest(menu.containerId, menu.nonce(), content(helper).revision(),
                    ReaderRequest.Op.SELECT, Optional.of(TEA)));
            ReaderService.tick(player);
        });
        ReaderUpdate update = sent.stream().map(s -> s.message()).filter(m -> m instanceof ReaderUpdate)
                .map(m -> (ReaderUpdate) m).findFirst().orElse(null);
        check(update != null && update.kind() == ReaderUpdate.Kind.AVAILABILITY && update.key().equals(Optional.of(TEA)),
                "no counts were sent: " + sent);
        check(update.containerId() == menu.containerId && update.session() == menu.nonce(), "counts for another reader");
        check(update.lines().get(0).missing() == 0 && update.lines().get(1).available() == 0 && update.lines().get(2).available() == 1,
                "wrong counts " + update.lines());
        check(player.getInventory().countItem(PreparationItems.powder(HerbCatalog.CHAMOMILE)) == before, "the inventory changed");
        var again = capture(() -> ReaderService.tick(player));
        check(again.isEmpty(), "unchanged counts were sent again");
        player.closeContainer();
        helper.succeed();
    }
}
