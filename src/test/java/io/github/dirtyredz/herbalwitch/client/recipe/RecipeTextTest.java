package io.github.dirtyredz.herbalwitch.client.recipe;

import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRequirements;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTextTest {
    @Test
    void requirementsUseTheSuppliedMultiplierAndKeepAnIntegerArgument() {
        var half = (TranslatableContents) RecipeText.requirements(ProcessRequirements.witch(7), 50).orElseThrow().getContents();
        assertEquals("category.herbalwitch.processing.witch", half.getKey());
        assertArrayEquals(new Object[]{4}, half.getArgs());
        var doubled = (TranslatableContents) RecipeText.requirements(ProcessRequirements.witch(7), 200).orElseThrow().getContents();
        assertArrayEquals(new Object[]{14}, doubled.getArgs());
    }

    @Test
    void ordinaryRequirementsAreAbsentAndZeroCostWitchRequirementsRemainVisible() {
        assertTrue(RecipeText.requirements(ProcessRequirements.NONE, 200).isEmpty());
        var free = (TranslatableContents) RecipeText.requirements(ProcessRequirements.witch(0), 100).orElseThrow().getContents();
        assertArrayEquals(new Object[]{0}, free.getArgs());
    }

    @Test
    void durationTextUsesTheOriginalKeysAndFractionalStringArgument() {
        var plain = (TranslatableContents) RecipeText.footer(50, false).getContents();
        var heated = (TranslatableContents) RecipeText.footer(50, true).getContents();
        assertEquals("category.herbalwitch.processing.time", plain.getKey());
        assertEquals("category.herbalwitch.processing.heated", heated.getKey());
        assertArrayEquals(new Object[]{"2.5"}, plain.getArgs());
        assertArrayEquals(plain.getArgs(), heated.getArgs());
    }
}
