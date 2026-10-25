package io.github.dirtyredz.herbalwitch.client.grimoire.guide.content;

import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LiveRecipesTagTest {
    @Test
    void reloadReplacementRemovalAndForeignRecipeNeverReuseOldFacts() {
        var id = new ResourceLocation("herbalwitch", "processing/steeping/restful_infusion");
        Map<ResourceLocation, Object> live = new HashMap<>();
        RecipeView before = recipe(id, "before"), after = recipe(id, "changed amounts and duration");
        live.put(id, before);
        assertSame(before, LiveRecipesTag.find(key -> Optional.ofNullable(live.get(key)), id).orElseThrow());
        live.put(id, after);
        assertSame(after, LiveRecipesTag.find(key -> Optional.ofNullable(live.get(key)), id).orElseThrow());
        live.remove(id);
        assertTrue(LiveRecipesTag.find(key -> Optional.ofNullable(live.get(key)), id).isEmpty());
        live.put(id, new Object());
        assertTrue(LiveRecipesTag.find(key -> Optional.ofNullable(live.get(key)), id).isEmpty());
        assertTrue(LiveRecipesTag.find(key -> Optional.empty(), id).isEmpty());
    }

    private static RecipeView recipe(ResourceLocation id, String fingerprint) {
        return new RecipeView() {
            public ResourceLocation getId() { return id; }
            public String fingerprint() { return fingerprint; }
            public ProcessRecipeDescription description() { throw new AssertionError("Lookup must not snapshot or alter facts"); }
        };
    }
}
