package io.github.dirtyredz.herbalwitch.client.grimoire.ponder;

import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.ui.PonderUI;

import java.util.List;

/**
 * Ponder's native lesson screen for an exact scene list. {@code PonderUI}'s list constructor is protected and its
 * public factories only accept a component, so this adapter exposes the constructor and nothing else: rendering,
 * controls, replay and back navigation stay Ponder's. Depends on the pinned Ponder 1.0.91 constructor.
 */
final class PonderLessonScreen extends PonderUI {
    PonderLessonScreen(List<PonderScene> scenes) {
        super(scenes);
    }
}
