package io.github.dirtyredz.herbalwitch.grimoire.knowledge.research;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Automatic discovery needs every primary ingredient familiar, research any one; quantities never count. */
class ResearchEligibilityTest {
    @Test
    void allPrimaryIngredientsFamiliarDiscoversAutomatically() {
        ResearchEligibility<String> tea = new ResearchEligibility<>(List.of("chamomile powder", "yarrow powder"), List.of("water"));
        assertTrue(tea.automatic(Set.of("chamomile powder", "yarrow powder")::contains));
        assertFalse(tea.automatic(Set.of("chamomile powder", "water")::contains), "the base never stands in for a primary");
        assertTrue(tea.researchable(Set.of("chamomile powder")::contains));
        assertFalse(tea.researchable(Set.of("water")::contains), "a familiar base alone does not allow research");
        assertFalse(tea.researchable(Set.of()::contains));
    }

    @Test
    void quantitiesAreNotPartOfFamiliarity() {
        // the same ingredient twice (two counted inputs) is familiar once it is familiar at all
        ResearchEligibility<String> recipe = new ResearchEligibility<>(List.of("lavender bundle", "lavender bundle"), List.of());
        assertTrue(recipe.automatic(Set.of("lavender bundle")::contains));
    }

    @Test
    void aRecipeWithoutPrimariesUsesItsBaseAndPackage() {
        ResearchEligibility<String> bare = new ResearchEligibility<>(List.of(), List.of("honey", "bowl"));
        assertTrue(bare.automatic(Set.of("honey", "bowl")::contains));
        assertFalse(bare.automatic(Set.of("honey")::contains));
        assertTrue(bare.researchable(Set.of("bowl")::contains));
        ResearchEligibility<String> empty = new ResearchEligibility<>(List.of(), List.of());
        assertFalse(empty.automatic(x -> true), "a recipe with no requirements is never discovered for free");
        assertFalse(empty.researchable(x -> true));
    }
}
