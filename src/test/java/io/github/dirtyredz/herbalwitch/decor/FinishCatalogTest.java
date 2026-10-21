package io.github.dirtyredz.herbalwitch.decor;

import io.github.dirtyredz.herbalwitch.decor.catalog.ArtSpikeCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.Finish;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The 18 finishes and the five spike pieces, checked against expectations written out here
 * independently of the catalogue. Pure JUnit: no Minecraft bootstrap.
 */
class FinishCatalogTest {
    private static final List<String> NATURAL = List.of("dark_oak", "walnut", "cherry", "rosewood", "pale_oak",
            "spruce", "birch");
    private static final List<String> PAINTED = List.of("black", "stone", "moss", "sage_green", "forest_green",
            "plum", "burgundy", "dusty_pink", "blush", "cream", "lilac");
    private static final Path MATERIALS = Path.of("src/main/resources/assets/herbalwitch/textures/block/material");

    @Test
    void eighteenStableFinishIdsInTabOrder() {
        assertEquals(NATURAL, FinishCatalog.NATURAL.stream().map(Finish::id).toList());
        assertEquals(PAINTED, FinishCatalog.PAINTED.stream().map(Finish::id).toList());
        assertEquals(18, FinishCatalog.ALL.size());
        assertEquals(18, FinishCatalog.ALL.stream().map(Finish::id).distinct().count());
        assertEquals("dark_oak", FinishCatalog.BASE.id());
    }

    @Test
    void palettesSplitSevenNaturalAndElevenPaintedSoLeafDirectoriesStayUnderTwelve() {
        FinishCatalog.NATURAL.forEach(f -> assertEquals(Finish.Palette.NATURAL, f.palette(), f.id()));
        FinishCatalog.PAINTED.forEach(f -> assertEquals(Finish.Palette.PAINTED, f.palette(), f.id()));
        assertTrue(FinishCatalog.NATURAL.size() < 12 && FinishCatalog.PAINTED.size() < 12);
    }

    @Test
    void naturalFinishesAreWoodGrainAndOnlyStoneIsCutStone() {
        FinishCatalog.NATURAL.forEach(f -> assertEquals(Finish.FrameMaterial.WOOD_GRAIN, f.frame(), f.id()));
        for (Finish f : FinishCatalog.PAINTED) {
            Finish.FrameMaterial expected = f.id().equals("stone") ? Finish.FrameMaterial.CUT_STONE : Finish.FrameMaterial.PAINTED_WOOD;
            assertEquals(expected, f.frame(), f.id());
        }
    }

    @Test
    void frameMaterialsReferenceGeneratedTextures() {
        for (Finish.FrameMaterial material : Finish.FrameMaterial.values()) {
            assertTrue(Files.isRegularFile(MATERIALS.resolve(material.texture() + ".png")), material.texture());
        }
    }

    @Test
    void coloursAreOpaqueRgbAndEveryFinishHasItsOwnCoordinatedPair() {
        Set<List<Integer>> pairs = new HashSet<>();
        for (Finish f : FinishCatalog.ALL) {
            assertTrue(f.frameColor() >= 0 && f.frameColor() <= 0xFFFFFF, f.id() + " frame colour");
            assertTrue(f.fabricColor() >= 0 && f.fabricColor() <= 0xFFFFFF, f.id() + " fabric colour");
            assertTrue(f.frameColor() != f.fabricColor(), f.id() + ": fabric must contrast with the frame");
            pairs.add(List.of(f.frameColor(), f.fabricColor()));
            assertTrue(!f.displayName().isBlank(), f.id());
        }
        // One fabric per finish: 18 coordinated pairs, not a wood x upholstery cross-product.
        assertEquals(18, pairs.size());
    }

    @Test
    void fiveSpikePiecesWithStablePaths() {
        assertEquals(List.of("armchair", "apothecary_counter", "small_wall_shelf", "table_lamp", "small_rug"),
                ArtSpikeCatalog.ALL.stream().map(FurnitureSpec::id).toList());
        Finish darkOak = FinishCatalog.BASE;
        Finish plum = FinishCatalog.PAINTED.get(5);
        assertEquals("decor/seating/armchair/natural/dark_oak", ArtSpikeCatalog.ARMCHAIR.path(darkOak));
        assertEquals("decor/surfaces/apothecary_counter/painted/plum", ArtSpikeCatalog.APOTHECARY_COUNTER.path(plum));
        assertEquals("decor/storage/small_wall_shelf/natural/dark_oak", ArtSpikeCatalog.SMALL_WALL_SHELF.path(darkOak));
        assertEquals("decor/lighting/table_lamp/painted/plum", ArtSpikeCatalog.TABLE_LAMP.path(plum));
        assertEquals("decor/textiles/small_rug/natural/dark_oak", ArtSpikeCatalog.SMALL_RUG.path(darkOak));
        assertEquals("Plum Apothecary Counter", ArtSpikeCatalog.APOTHECARY_COUNTER.displayName(plum));
    }
}
