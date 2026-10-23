package io.github.dirtyredz.herbalwitch.platform.recipe;

import net.minecraft.resources.ResourceLocation;

/** Read-only live recipe identity and facts, independent of station execution and registration. */
public interface RecipeView {
    ResourceLocation getId();
    ProcessRecipeDescription description();
    String fingerprint();
    /** Live result semantics for recipes that transform an input instead of making a fresh item. */
    default java.util.Optional<net.minecraft.network.chat.Component> resultNote() { return java.util.Optional.empty(); }
}
