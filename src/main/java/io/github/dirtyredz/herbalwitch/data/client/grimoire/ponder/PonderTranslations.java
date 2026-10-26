package io.github.dirtyredz.herbalwitch.data.client.grimoire.ponder;

import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationText;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.GuideLessons;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * The animated lessons' English: the guide's Watch label, each scene title and its named shared captions.
 * Contributed to the sole language writer; the Ponder plugin registers the same defaults from the same catalogue.
 */
public final class PonderTranslations {
    private PonderTranslations() {
    }

    public static void addTo(LanguageProvider lang) {
        lang.add(GuideLessons.WATCH_LABEL_KEY, "Watch the animated lesson");
        lang.add(FirstPreparationText.titleKey(), FirstPreparationText.TITLE);
        for (FirstPreparationText.Caption caption : FirstPreparationText.ALL) {
            lang.add(caption.key(), caption.english());
        }
    }
}
