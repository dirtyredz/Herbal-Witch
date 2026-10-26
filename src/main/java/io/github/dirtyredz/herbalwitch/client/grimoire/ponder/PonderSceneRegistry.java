package io.github.dirtyredz.herbalwitch.client.grimoire.ponder;

import io.github.dirtyredz.herbalwitch.client.grimoire.ponder.scenes.FirstPreparationScene;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons.Lesson;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.StoryBoardEntry;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Each lesson's storyboard, registered against every item the lesson is about (so Ponder's W-hover offers it),
 * and the exact lookup the guide's Watch link uses: the one entry with the lesson's schematic under its
 * canonical component. Missing or ambiguous matches resolve to nothing, never to an unrelated scene.
 */
final class PonderSceneRegistry {
    /** Lesson id to its storyboard; every lesson in {@link GuideLessons#ALL} must have one. */
    private static final Map<String, PonderStoryBoard> BOARDS = Map.of(
            GuideLessons.FIRST_PREPARATION.id(), FirstPreparationScene::program);

    private PonderSceneRegistry() {
    }

    static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        for (Lesson lesson : GuideLessons.ALL) {
            PonderStoryBoard board = BOARDS.get(lesson.id());
            if (board == null) throw new IllegalStateException("No storyboard for lesson " + lesson.id());
            List<ResourceLocation> components = lesson.items().stream().map(ResourceLocation::new).toList();
            helper.forComponents(components).addStoryBoard(new ResourceLocation(lesson.schematic()), board);
        }
    }

    /** The lesson's single registered storyboard, if exactly one exists. */
    static Optional<StoryBoardEntry> entry(Lesson lesson) {
        ResourceLocation component = new ResourceLocation(lesson.canonicalItem());
        ResourceLocation schematic = new ResourceLocation(lesson.schematic());
        List<StoryBoardEntry> matches = PonderIndex.getSceneAccess().getRegisteredEntries().stream()
                .filter(registered -> registered.getKey().equals(component))
                .map(Map.Entry::getValue)
                .filter(candidate -> component.equals(candidate.getComponent())
                        && schematic.equals(candidate.getSchematicLocation()))
                .toList();
        return matches.size() == 1 ? Optional.of(matches.get(0)) : Optional.empty();
    }
}
