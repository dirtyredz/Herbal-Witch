package io.github.dirtyredz.herbalwitch.client.grimoire.guide.animation;

import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons.Lesson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The Ponder-free Watch contract: unavailable by default, validated ids, exact dispatch, rechecked at click. */
class GuideAnimationsTest {
    private static final Lesson LESSON = GuideLessons.FIRST_PREPARATION;

    @AfterEach
    void reset() {
        GuideAnimations.uninstall();
    }

    @Test
    void unavailableByDefault() {
        assertFalse(GuideAnimations.available(LESSON));
        assertFalse(GuideAnimations.open(LESSON.id()));
    }

    @Test
    void lessonIdsAreValidatedWithOrWithoutAPlayer() {
        assertTrue(GuideAnimations.problem(LESSON.id()).isEmpty());
        for (String bad : new String[] {null, "", "  ", "Not An Id", "first_preparation", "herbalwitch:unknown"}) {
            assertTrue(GuideAnimations.problem(bad).isPresent(), String.valueOf(bad));
        }
        GuideAnimations.install(new FakePlayer(true, true));
        assertTrue(GuideAnimations.problem("herbalwitch:unknown").isPresent());
    }

    @Test
    void installedPlayerDispatchesTheExactLessonOnce() {
        var player = new FakePlayer(true, true);
        GuideAnimations.install(player);
        assertTrue(GuideAnimations.available(LESSON));
        assertTrue(GuideAnimations.open(LESSON.id()));
        assertEquals(List.of(LESSON), player.played);
    }

    @Test
    void unknownLessonsNeverReachThePlayer() {
        var player = new FakePlayer(true, true);
        GuideAnimations.install(player);
        assertFalse(GuideAnimations.open("herbalwitch:unknown"));
        assertTrue(player.played.isEmpty());
    }

    @Test
    void unavailableAtClickTimeOpensNothing() {
        var missingStoryboard = new FakePlayer(false, true);
        GuideAnimations.install(missingStoryboard);
        assertFalse(GuideAnimations.available(LESSON), "A player without the storyboard hides the link");
        assertFalse(GuideAnimations.open(LESSON.id()));
        assertTrue(missingStoryboard.played.isEmpty());
        GuideAnimations.uninstall();
        var noWorld = new FakePlayer(true, false);
        GuideAnimations.install(noWorld);
        assertFalse(GuideAnimations.open(LESSON.id()), "The player's own recheck (e.g. no client level) is honoured");
        assertEquals(List.of(LESSON), noWorld.played);
    }

    @Test
    void aSecondDifferentPlayerIsRejected() {
        var first = new FakePlayer(true, true);
        GuideAnimations.install(first);
        GuideAnimations.install(first);
        assertThrows(IllegalStateException.class, () -> GuideAnimations.install(new FakePlayer(true, true)));
    }

    private static final class FakePlayer implements GuideAnimations.Player {
        private final boolean has;
        private final boolean opens;
        final List<Lesson> played = new ArrayList<>();

        FakePlayer(boolean has, boolean opens) {
            this.has = has;
            this.opens = opens;
        }

        @Override
        public boolean has(Lesson lesson) {
            return has;
        }

        @Override
        public boolean play(Lesson lesson) {
            played.add(lesson);
            return opens;
        }
    }
}
