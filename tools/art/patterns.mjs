// Decorative motifs: the small rug and the lamp shade. Both carry their own colour (deep green,
// hot pink, a thread of dark purple, cream); nothing is tinted in game. 32 x 32 pixels, tiling
// seamlessly.
import { rng, shade, image } from './pixels.mjs';
import { SIZE } from './materials.mjs';

const GREEN = [47, 90, 44];
const GREEN_LIGHT = [54, 100, 58];
const PINK = [216, 64, 122];
const PINK_DEEP = [201, 58, 112];
const PURPLE = [62, 31, 71];
const CREAM = [238, 226, 196];

/** Small rug: plain woven deep-green field, a hot-pink border band and a thin dark-purple rule inside it. */
export function rug() {
  const rand = rng(7703);
  return image(SIZE, (x, y) => {
    const edge = Math.min(x, y, SIZE - 1 - x, SIZE - 1 - y);
    const weave = (x + y) % 2 === 0;
    const noise = 1 + (rand() - 0.5) * 0.05;
    if (edge < 4) return shade(weave ? PINK : PINK_DEEP, noise);
    if (edge === 4) return shade(PURPLE, noise);
    return shade(weave ? GREEN : GREEN_LIGHT, noise);
  });
}

/** Lamp shade: hot-pink pleats, a thin dark-purple band at the top and a cream scalloped hem. */
export function lampShade() {
  const rand = rng(8803);
  return image(SIZE, (x, y) => {
    const noise = 1 + (rand() - 0.5) * 0.04;
    if (y < 3) return shade(PURPLE, noise);
    if (y >= SIZE - 4) {
      const scallop = Math.abs((x % 6) - 2.5);
      const depth = SIZE - 1 - y;
      return depth > 2 - scallop / 2.5 ? shade(CREAM, noise) : shade(PINK_DEEP, noise);
    }
    return shade(x % 4 < 2 ? PINK : PINK_DEEP, noise);
  });
}
