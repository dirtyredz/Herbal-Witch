package io.github.dirtyredz.herbalwitch.decor.catalog;

import net.minecraft.world.level.material.MapColor;

/**
 * One coordinated furniture finish: a frame material and colour paired with one fabric colour. A
 * finish is cosmetic only; it never changes strength, recipes or behaviour.
 *
 * @param id          stable registry segment, e.g. {@code dark_oak}; never renamed once released
 * @param displayName English name prefix, e.g. {@code Dark Oak}
 * @param palette     natural wood or painted/material appearance; also a generated-path directory
 * @param frame       the neutral texture the frame colour tints
 * @param frameColor  RGB multiplied onto frame faces (tint index 0)
 * @param fabricColor RGB multiplied onto fabric, shade and rug faces (tint index 1)
 * @param mapColor    the colour this finish shows on maps
 */
public record Finish(String id, String displayName, Palette palette, FrameMaterial frame, int frameColor,
                     int fabricColor, MapColor mapColor) {

    /** The two finish groups. Their directory names keep each generated leaf directory under 12 entries. */
    public enum Palette {
        NATURAL("natural"),
        PAINTED("painted");

        private final String directory;

        Palette(String directory) {
            this.directory = directory;
        }

        public String directory() {
            return directory;
        }
    }

    /** Neutral frame textures under {@code textures/block/material/}; the frame colour gives the hue. */
    public enum FrameMaterial {
        WOOD_GRAIN("wood_grain"),
        PAINTED_WOOD("painted_wood"),
        CUT_STONE("cut_stone");

        private final String texture;

        FrameMaterial(String texture) {
            this.texture = texture;
        }

        public String texture() {
            return texture;
        }
    }
}
