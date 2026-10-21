package io.github.dirtyredz.herbalwitch.data.art;

import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TextureDensity;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every texture the mod ships, whichever producer wrote it (procedural, Blockbench piece), is at the
 * set's one density: 32 x 32 for blocks and items, 512 x 512 for GUI atlases. Mixed densities fail.
 */
class TextureDensityTest {
    private static final Path TEXTURES = ResourceFiles.MAIN_ASSETS.resolve("textures");

    @Test
    void everyTextureIsAtTwoTexelsPerModelUnit() throws IOException {
        List<Path> pngs;
        try (Stream<Path> walk = Files.walk(TEXTURES)) {
            pngs = walk.filter(p -> p.toString().endsWith(".png")).sorted().toList();
        }
        assertTrue(pngs.size() > 150, "expected the whole texture set, found " + pngs.size());
        for (Path png : pngs) {
            String relative = TEXTURES.relativize(png).toString().replace('\\', '/');
            int want = TextureDensity.expectedSize(relative);
            int[] size = ResourceFiles.pngSize(png);
            assertEquals(want + " x " + want, size[0] + " x " + size[1], relative + " (" + TextureDensity.TEXELS_PER_UNIT + " texels per model unit)");
        }
    }
}
