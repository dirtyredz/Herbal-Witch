package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.botany.showcase.HerbShowcaseSection;
import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;
import io.github.dirtyredz.herbalwitch.data.decor.DecorManifest;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Coverage against the independent manifests: one labelled decor cell per piece and finish, one
 * labelled herb row with every age. The live block identities are checked by the GameTests.
 */
class ShowcaseCoverageTest {
    private static long count(List<ShowcasePlacement> placements, ShowcasePlacement.Kind kind, int y) {
        return placements.stream().filter(p -> p.kind() == kind && p.y() == y).count();
    }

    @Test
    void decorShowsEveryPieceInEveryFinishWithItsSign() {
        List<ShowcasePlacement> placements = new DecorShowcaseSection().placements();
        int cells = DecorManifest.ids().size();
        assertEquals(cells, count(placements, ShowcasePlacement.Kind.LABEL, 1));
        long content = placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.CONTENT).count();
        assertEquals(cells, content);
        List<String> labels = placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.LABEL)
                .map(p -> p.lines().get(0).text() + " " + p.lines().get(1).text()).toList();
        assertEquals("Dark Oak Armchair", labels.get(0));
        assertEquals("Lilac Small Rug", labels.get(labels.size() - 1));
    }

    @Test
    void herbsShowEveryAgeOfEveryHerb() {
        List<ShowcasePlacement> placements = new HerbShowcaseSection().placements();
        int herbs = BotanyManifest.HERBS.size();
        assertEquals(herbs, count(placements, ShowcasePlacement.Kind.LABEL, 1));
        assertEquals(herbs * 4, count(placements, ShowcasePlacement.Kind.CONTENT, 2), "56 live plants");
        assertEquals(herbs * 4 + herbs, count(placements, ShowcasePlacement.Kind.CONTENT, 1), "beds and barrels");
        List<String> names = placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.LABEL)
                .map(p -> p.lines().get(0).text()).toList();
        assertEquals(BotanyManifest.HERBS.stream().map(BotanyManifest.Herb::name).toList(), names);
    }
}
