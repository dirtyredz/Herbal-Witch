package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.apothecary.botany.showcase.HerbShowcaseSection;
import io.github.dirtyredz.herbalwitch.data.botany.BotanyManifest;
import io.github.dirtyredz.herbalwitch.data.decor.DecorManifest;
import io.github.dirtyredz.herbalwitch.data.decor.manifest.DecorPieces;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSection;
import io.github.dirtyredz.herbalwitch.grimoire.showcase.GrimoireShowcaseSection;
import io.github.dirtyredz.herbalwitch.magic.ritual.showcase.AltarShowcaseSection;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlacement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Coverage against the independent manifests: one labelled decor cell per piece, one
 * labelled herb row with every age. The live block identities are checked by the GameTests.
 */
class ShowcaseCoverageTest {
    private static long count(List<ShowcasePlacement> placements, ShowcasePlacement.Kind kind, int y) {
        return placements.stream().filter(p -> p.kind() == kind && p.y() == y).count();
    }

    /** Pieces with an inventory (cabinets and display shelves), independently of the catalogue. */
    private static final Set<String> STORAGE = Set.of("small_wall_shelf", "lower_cabinet", "upper_cabinet", "pantry", "sideboard",
            "china_cabinet", "wine_rack", "ingredient_cabinet", "open_shelves", "potion_shelf", "nightstand", "dresser", "wardrobe",
            "trunk", "rolltop_desk", "filing_cabinet", "shoe_cabinet", "bookshelf", "linen_cabinet", "storage_cabinet",
            "wall_shelf_medium", "wall_shelf_large", "corner_shelf", "free_standing_shelf_short", "free_standing_shelf_medium",
            "free_standing_shelf_large", "ladder_shelf");

    /**
     * One canonical labelled sample per piece (the door also shows its three other states), exactly
     * its declared cells, and exactly one stocked inventory per storage or display sample.
     */
    @Test
    void decorShowsEveryPieceWithItsSignAndExactlyItsCells() {
        List<ShowcasePlacement> placements = new DecorShowcaseSection().placements();
        List<String> names = new ArrayList<>();
        int cells = 0;
        int stores = 0;
        for (DecorPieces.Piece piece : DecorManifest.pieces()) {
            int samples = piece.id().equals("door") ? 4 : 1;
            for (int i = 0; i < samples; i++) {
                names.add(piece.name());
            }
            cells += piece.cells() * samples;
            stores += STORAGE.contains(piece.id()) ? 1 : 0;
        }
        List<ShowcasePlacement> labels = placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.LABEL).toList();
        assertEquals(names, labels.stream().map(p -> String.join(" ", p.lines().stream().filter(ShowcasePlacement.Line::literal)
                .map(ShowcasePlacement.Line::text).toList())).toList());
        assertEquals(cells, placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.CONTENT).count());
        assertEquals(stores, placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.CONTENT && p.init() != null).count());
    }

    @Test
    void theGrimoireShowsBothResearchBlocksSuppliesAndItsFourSigns() {
        List<ShowcasePlacement> placements = new GrimoireShowcaseSection().placements();
        assertEquals(3, placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.CONTENT).count(),
                "stand, bookshelf, supply barrel");
        List<String> labels = placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.LABEL)
                .map(p -> p.lines().get(0).text()).toList();
        assertEquals(List.of("Grimoire Stand", "Witch's Bookshelf", "showcase.herbalwitch.label.grimoire.supplies",
                "showcase.herbalwitch.label.grimoire.discovery", "showcase.herbalwitch.label.grimoire.research",
                "showcase.herbalwitch.label.grimoire.missing", "showcase.herbalwitch.label.grimoire.teaching"), labels);
        assertEquals(7, GrimoireShowcaseSection.supplies().size(), "two grimoires and one preparation chain's supplies");
    }

    /** The whole idle altar (its four cells), one supply barrel, and its sign row in order. */
    @Test
    void theAltarShowsItsFourCellsSuppliesAndSigns() {
        List<ShowcasePlacement> placements = new AltarShowcaseSection().placements();
        List<ShowcasePlacement> content = placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.CONTENT).toList();
        assertEquals(5, content.size(), "four altar cells and a supply barrel");
        assertEquals(1, content.stream().filter(p -> p.init() != null).count(), "one stocked barrel");
        assertEquals(4, content.stream().filter(p -> p.x() <= 1).count(), "the altar stands at x 0..1");
        List<String> labels = placements.stream().filter(p -> p.kind() == ShowcasePlacement.Kind.LABEL)
                .map(p -> p.lines().get(0).text()).toList();
        assertEquals(List.of("Witch's Altar", "showcase.herbalwitch.label.altar.use", "showcase.herbalwitch.label.altar.founding",
                "showcase.herbalwitch.label.altar.supplies", "showcase.herbalwitch.label.altar.initiation",
                "showcase.herbalwitch.label.altar.recovery", "showcase.herbalwitch.label.altar.practice"), labels);
        assertEquals(9, AltarShowcaseSection.supplies().size(), "the founding offering, a second set, a grimoire and the drinks");
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
