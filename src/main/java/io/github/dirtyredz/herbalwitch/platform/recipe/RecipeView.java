package io.github.dirtyredz.herbalwitch.platform.recipe;

import net.minecraft.resources.ResourceLocation;

/** Read-only live recipe identity and facts, independent of station execution and registration. */
public interface RecipeView {
    ResourceLocation getId();
    ProcessRecipeDescription description();
    String fingerprint();
}
