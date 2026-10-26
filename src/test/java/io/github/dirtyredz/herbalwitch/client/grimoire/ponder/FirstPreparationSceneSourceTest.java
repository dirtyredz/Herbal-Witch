package io.github.dirtyredz.herbalwitch.client.grimoire.ponder;

import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationText;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationText.Caption;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Source-level guards for the storyboard. Ponder is compile-only, so plain JUnit cannot load the scene; these
 * checks keep its captions, title, registration and replay contract tied to the shared lesson catalogues.
 */
class FirstPreparationSceneSourceTest {
    private static final Path PONDER = Path.of("src/main/java/io/github/dirtyredz/herbalwitch/client/grimoire/ponder");
    private static final Path SCENE = PONDER.resolve("scenes/FirstPreparationScene.java");

    @Test
    void everyCaptionIsShownOnceInStoryboardOrderThroughSharedText() throws Exception {
        String source = Files.readString(SCENE);
        int last = -1;
        for (String name : captionFieldNames()) {
            var use = Pattern.compile("\\bFirstPreparationText\\." + name + "\\b").matcher(source);
            assertTrue(use.find(), "Caption not shown: " + name);
            assertTrue(use.start() > last, "Caption out of storyboard order: " + name);
            last = use.start();
            assertFalse(use.find(), "Caption shown twice: " + name);
        }
        assertTrue(source.contains(".sharedText(caption.id())"), "Captions must use named shared text");
        assertFalse(Pattern.compile("\\.text\\(").matcher(source).find(), "Positional .text(...) keys are forbidden");
    }

    @Test
    void sceneHasItsTitleAFinishAndNoStaticMutableState() throws Exception {
        String source = Files.readString(SCENE);
        assertTrue(source.contains("scene.title(FirstPreparationText.SCENE, FirstPreparationText.TITLE)"));
        assertTrue(source.contains("scene.markAsFinished()"));
        assertFalse(Pattern.compile("(?m)^\\s*(?:private |public |protected )?static (?!final)[\\w<>\\[\\], ]+ \\w+\\s*[;=]")
                .matcher(source).find(), "No static mutable scene state; native restore handles replay");
    }

    @Test
    void everyLessonHasARegisteredStoryboardAndSharedTextRegistration() throws Exception {
        String registry = Files.readString(PONDER.resolve("PonderSceneRegistry.java"));
        assertEquals(1, GuideLessons.ALL.size(), "A new lesson needs its storyboard entry in PonderSceneRegistry.BOARDS");
        assertTrue(registry.contains("GuideLessons.FIRST_PREPARATION.id(), FirstPreparationScene::program"));
        assertTrue(registry.contains("addStoryBoard(new ResourceLocation(lesson.schematic()), board)"),
                "Storyboards must load the lesson's own schematic");
        String plugin = Files.readString(PONDER.resolve("HerbalWitchPonderPlugin.java"));
        assertTrue(plugin.contains("FirstPreparationText.ALL"), "Shared-text defaults must come from the caption catalogue");
    }

    private static List<String> captionFieldNames() throws IllegalAccessException {
        List<String> ordered = new ArrayList<>();
        for (Caption caption : FirstPreparationText.ALL) {
            for (Field field : FirstPreparationText.class.getFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getType() == Caption.class && field.get(null) == caption) {
                    ordered.add(field.getName());
                }
            }
        }
        assertEquals(FirstPreparationText.ALL.size(), ordered.size(), "Every caption must be a named constant");
        return ordered;
    }
}
