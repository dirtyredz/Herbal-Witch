// The set's texture density (owner ruling 2026-10-21, 0.2.0-dev.9): two texels per model unit, twice
// vanilla's. Model geometry and UVs stay in Minecraft's 16-unit space; only the bitmaps are finer.
// Every block and item texture is therefore 32 x 32 (a 16-unit face or sprite spans 32 texels) and
// every GUI atlas is 512 x 512 (a 256-unit vanilla container sheet at two texels per GUI pixel).
// The producers, the Blockbench compiler and the tests all check sizes against this one rule, so a
// 16 x 16 texture cannot come back next to 32 x 32 ones.
import { dimensions } from '../image/png-encode.mjs';

/** Texels per model unit (per GUI pixel for the interface atlases). */
export const TEXELS_PER_UNIT = 2;
/** Minecraft's UV space: a face, sprite or project texture spans 16 units. */
export const MODEL_UNITS = 16;
/** The size of every block and item texture. */
export const TEXTURE_SIZE = MODEL_UNITS * TEXELS_PER_UNIT;
/** The logical size of a GUI atlas (what blit's u/v are measured in) and its file size. */
export const GUI_UNITS = 256;
export const GUI_SIZE = GUI_UNITS * TEXELS_PER_UNIT;

/** The square size a texture at `rel` (relative to assets/<ns>/textures/) must have. */
export const expectedSize = (rel) => (rel.startsWith('gui/') ? GUI_SIZE : /^entity\/familiars\/(raven|owl|snake)\//.test(rel) ? 64 : TEXTURE_SIZE);

/** Throws unless the image (any {size} or {width, height}) has the size the density rule requires at `rel`. */
export function checkDensity(rel, img) {
  const { width, height } = dimensions(img);
  const want = expectedSize(rel);
  if (width !== want || height !== want) {
    throw new Error(`${rel}: ${width} x ${height}, but the set paints at ${TEXELS_PER_UNIT} texels per model unit (${want} x ${want}; docs/ART.md)`);
  }
}
