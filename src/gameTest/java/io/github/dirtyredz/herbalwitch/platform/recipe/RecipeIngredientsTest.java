package io.github.dirtyredz.herbalwitch.platform.recipe;

import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeIngredients;

import io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire.StationRecipeEntries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Optional;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/** The projection's ingredient queries, characterized before moving them into this package. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RecipeIngredientsTest {

    @GameTest(template = TEMPLATE)
    public static void primaryBaseAndPackageKeepTheirOrderAndIdentity(GameTestHelper helper) {
        Ingredient primary = Ingredient.of(Items.PAPER);
        Ingredient base = Ingredient.of(Items.POTION);
        Ingredient packaging = Ingredient.of(Items.GLASS_BOTTLE);
        ProcessRecipeDescription d = new ProcessRecipeDescription(List.of(new ProcessRecipeDescription.Counted(primary, 2)),
                Optional.of(new ProcessRecipeDescription.Counted(base, 1)), Optional.of(new ProcessRecipeDescription.Counted(packaging, 4)),
                new ItemStack(Items.STICK), List.of(new ItemStack(Items.BUCKET)), 50, false);
        assertEquals(List.of(primary), RecipeIngredients.primaries(d));
        assertEquals(List.of(base, packaging), RecipeIngredients.secondaries(d));
        assertEquals(List.of(primary, base, packaging), RecipeIngredients.allIngredients(d));
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void absentRolesAndReturnedContainersDoNotBecomeInputs(GameTestHelper helper) {
        ProcessRecipeDescription d = new ProcessRecipeDescription(List.of(), Optional.empty(), Optional.empty(),
                new ItemStack(Items.PAPER), List.of(new ItemStack(Items.BUCKET)), 50, false);
        assertTrue(RecipeIngredients.allIngredients(d).isEmpty());
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void countedIngredientsRequireTheRightItemAndMinimumCount(GameTestHelper helper) {
        ProcessRecipeDescription.Counted counted = new ProcessRecipeDescription.Counted(Ingredient.of(Items.PAPER), 2);
        assertFalse(counted.test(ItemStack.EMPTY));
        assertFalse(counted.test(new ItemStack(Items.PAPER)));
        assertFalse(counted.test(new ItemStack(Items.STICK, 3)));
        assertTrue(counted.test(new ItemStack(Items.PAPER, 2)));
        assertTrue(counted.test(new ItemStack(Items.PAPER, 3)));
        helper.succeed();
    }

    private static void assertEquals(Object expected, Object actual) {
        check(java.util.Objects.equals(expected, actual), "expected " + expected + ", got " + actual);
    }

    private static void assertTrue(boolean actual) { check(actual, "expected true"); }
    private static void assertFalse(boolean actual) { check(!actual, "expected false"); }
}
