package io.github.dirtyredz.herbalwitch.client.recipe;

import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;

import io.github.dirtyredz.herbalwitch.client.recipe.RecipeText;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.client.compat.ViewerLayout;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRequirements;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Optional;

import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class RecipeTextGameTests {
    @GameTest(template = TEMPLATE)
    public static void countedDisplaysCopyStacksAndKeepStrictNbt(GameTestHelper helper) {
        var source = new ItemStack(Items.POTION);
        source.getOrCreateTag().putString("Potion", "minecraft:water");
        var ingredient = StrictNBTIngredient.of(source);
        var counted = new ProcessRecipeDescription.Counted(ingredient, 3);
        var shown = RecipeText.stacks(counted);
        check(shown.size() == 1 && shown.get(0).getCount() == 3, "display count");
        check(ingredient.test(shown.get(0)), "display lost NBT");
        shown.get(0).getOrCreateTag().putString("Potion", "minecraft:awkward");
        check(source.getCount() == 1 && source.getTag().getString("Potion").equals("minecraft:water"), "display mutated source");
        check(ingredient.getItems()[0].getTag().getString("Potion").equals("minecraft:water"), "display mutated accepted stack");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void theFooterKeepsFractionalSecondsAndOrdinaryRecipesHaveNoEnergyLine(GameTestHelper helper) {
        var description = new ProcessRecipeDescription(List.of(new ProcessRecipeDescription.Counted(Ingredient.of(Items.STICK), 1)),
                Optional.empty(), Optional.empty(), new ItemStack(Items.BOWL), List.of(), 50, false);
        var footer = (TranslatableContents) RecipeText.footer(description.duration(), description.heated()).getContents();
        check(footer.getArgs().length == 1 && footer.getArgs()[0].equals("2.5"), "fractional footer/type");
        check(RecipeText.requirements(description.requirements(), WitchRules.current().workings().processingPercent()).isEmpty(), "ordinary energy line");
        helper.succeed();
    }
}
