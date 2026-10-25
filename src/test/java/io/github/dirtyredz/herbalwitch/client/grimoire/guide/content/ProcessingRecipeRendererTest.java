package io.github.dirtyredz.herbalwitch.client.grimoire.guide.content;

import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessingRecipeRendererTest {
    @Test void rolesRetainTheExistingTranslationKeysUntilPresentation() {
        for (String role : List.of("ingredient", "base", "packaging")) {
            var text = ProcessingRecipeRenderer.role(role);
            var translated = assertInstanceOf(TranslatableContents.class, text.getContents());
            assertEquals("grimoire.herbalwitch.ui.role." + role, translated.getKey());
        }
    }
}
