package io.github.dirtyredz.herbalwitch.data.fixture;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The set's texture density (owner ruling, 0.2.0-dev.9): two texels per model unit, twice vanilla's.
 * Models and UVs stay in the 16-unit space; every block and item texture is 32 x 32 and every GUI
 * atlas is 512 x 512 (vanilla's 256-unit sheet at two texels per GUI pixel). Mirrors
 * {@code tools/art/density.mjs}; one size per kind, so a 16 x 16 texture cannot sit next to 32 x 32 ones.
 */
public final class TextureDensity {
    public static final int TEXELS_PER_UNIT = 2;
    public static final int TEXTURE_SIZE = 16 * TEXELS_PER_UNIT;
    public static final int GUI_SIZE = 256 * TEXELS_PER_UNIT;

    private TextureDensity() {
    }

    /** The size a texture at {@code relative} (to {@code textures/}, slash-separated) must have. */
    public static int expectedSize(String relative) {
        return relative.startsWith("gui/") ? GUI_SIZE : TEXTURE_SIZE;
    }

    /** Fails unless the block or item texture PNG is exactly 32 x 32. */
    public static void assertTextureSize(Path png, String what) {
        int[] size = ResourceFiles.pngSize(png);
        assertTrue(size[0] == TEXTURE_SIZE && size[1] == TEXTURE_SIZE,
                what + ": " + png.getFileName() + " is " + size[0] + " x " + size[1] + ", not " + TEXTURE_SIZE + " x " + TEXTURE_SIZE
                        + " (" + TEXELS_PER_UNIT + " texels per model unit)");
    }
}
