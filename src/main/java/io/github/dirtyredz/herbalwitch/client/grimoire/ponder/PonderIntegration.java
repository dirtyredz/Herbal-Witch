package io.github.dirtyredz.herbalwitch.client.grimoire.ponder;

import io.github.dirtyredz.herbalwitch.client.grimoire.guide.animation.GuideAnimations;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons.Lesson;
import net.createmod.catnip.gui.ScreenOpener;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * The optional Ponder integration's only entry point. {@code HerbalWitchClient} calls {@link #setup()} solely
 * when the {@code ponder} mod is loaded, so no Ponder, Catnip or Flywheel class is ever resolved without it.
 * Registers the plugin (Ponder runs plugin registration at load-complete) and installs the guide's Watch player.
 */
public final class PonderIntegration {
    private PonderIntegration() {
    }

    public static void setup() {
        PonderIndex.addPlugin(new HerbalWitchPonderPlugin());
        GuideAnimations.install(new LessonPlayer());
    }

    /**
     * Opens exactly the lesson's storyboard. Ponder's own {@code PonderUI.of(id)} opens a component's whole scene
     * list, so the lesson's entry is compiled alone and shown in the native screen (controls, replay, back).
     */
    private static final class LessonPlayer implements GuideAnimations.Player {
        @Override
        public boolean has(Lesson lesson) {
            return PonderSceneRegistry.entry(lesson).isPresent();
        }

        @Override
        public boolean play(Lesson lesson) {
            if (Minecraft.getInstance().level == null) return false;
            var entry = PonderSceneRegistry.entry(lesson);
            if (entry.isEmpty()) return false;
            var scenes = PonderIndex.getSceneAccess().compile(List.of(entry.get()));
            if (scenes.size() != 1) return false;
            ScreenOpener.transitionTo(new PonderLessonScreen(scenes));
            return true;
        }
    }
}
