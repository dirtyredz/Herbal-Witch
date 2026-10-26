package io.github.dirtyredz.herbalwitch.client.grimoire.guide.animation;

import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons.Lesson;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

/**
 * The guide's Ponder-free bridge to an optional animation player. Unavailable by default: only the guarded
 * Ponder integration installs a player, so without Ponder the guide's Watch links are simply not emitted.
 * Lesson ids are validated either way, so a broken Watch tag fails the page even when nothing could play it.
 */
public final class GuideAnimations {
    /** An installed player of animated lessons. */
    public interface Player {
        /** Whether exactly one animation is registered for the lesson. */
        boolean has(Lesson lesson);

        /** Rechecks the lesson and the client's state, then opens it; returns whether it opened. */
        boolean play(Lesson lesson);
    }

    private static volatile Player player;

    private GuideAnimations() {
    }

    /** Installs the player; once per client (the integration's setup). */
    public static void install(Player installed) {
        Objects.requireNonNull(installed, "player");
        if (player != null && player != installed) throw new IllegalStateException("An animation player is already installed");
        player = installed;
    }

    /** Test-only reset to the unavailable default. */
    static void uninstall() {
        player = null;
    }

    /**
     * Why a Watch tag's {@code lesson} attribute is invalid, if it is: missing, malformed or not a known lesson.
     * Checked regardless of whether a player is installed.
     */
    public static Optional<String> problem(String lessonId) {
        if (lessonId == null || lessonId.isBlank()) return Optional.of("Watch needs a lesson attribute");
        if (ResourceLocation.tryParse(lessonId) == null || !lessonId.contains(":")) {
            return Optional.of("Malformed lesson id: " + lessonId);
        }
        if (GuideLessons.find(lessonId).isEmpty()) return Optional.of("Unknown lesson: " + lessonId);
        return Optional.empty();
    }

    /** Whether a Watch link should be shown for the lesson: a player is installed and has its animation. */
    public static boolean available(Lesson lesson) {
        Player current = player;
        return current != null && current.has(lesson);
    }

    /** Opens the lesson if it is still available at click time; returns whether it opened. */
    public static boolean open(String lessonId) {
        Optional<Lesson> lesson = GuideLessons.find(lessonId);
        Player current = player;
        if (lesson.isEmpty() || current == null || !current.has(lesson.get())) return false;
        return current.play(lesson.get());
    }
}
