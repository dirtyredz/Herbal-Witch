// Decorative motifs: upholstery fabric, the small rug and the lamp shade. All are neutral greys,
// tinted in game by the finish's fabric colour, so a motif reads as a lighter or darker shade of
// that colour (tone-on-tone). 32 x 32 pixels, tiling seamlessly.
import { rng, grey, image } from './png.mjs';
import { SIZE } from './materials.mjs';

/** Five-pixel flower: centre plus four petals. */
function flowerAt(dx, dy) {
  if (dx === 0 && dy === 0) return 'centre';
  if (Math.abs(dx) + Math.abs(dy) === 1) return 'petal';
  return null;
}

/** Upholstery: a fine weave with small flowers on a staggered 8-pixel lattice and leaf dots. */
export function fabricFloral() {
  const rand = rng(6607);
  return image(SIZE, (x, y) => {
    let v = 204 + ((x + y) % 2 === 0 ? 5 : -5) + (rand() - 0.5) * 6; // weave
    const row = Math.floor(y / 8);
    const offset = row % 2 === 0 ? 3 : 7;
    const cx = offset + 8 * Math.round((x - offset) / 8); // nearest centre, so flowers wrap across tiles
    const cy = row * 8 + 3;
    const part = flowerAt(x - cx, y - cy);
    if (part === 'centre') v = 150;
    else if (part === 'petal') v = 248;
    else if (Math.abs(x - cx) === 2 && y - cy === 2) v = 176; // leaves
    return grey(v);
  });
}

/** Small rug: dark outer border, a light rule, a diamond lattice field and a centre medallion. */
export function rug() {
  const rand = rng(7703);
  const c = (SIZE - 1) / 2;
  return image(SIZE, (x, y) => {
    const edge = Math.min(x, y, SIZE - 1 - x, SIZE - 1 - y);
    if (edge < 3) return grey(118 + ((x + y) % 4 === 0 ? 18 : 0) + (rand() - 0.5) * 6);
    if (edge === 3) return grey(240);
    const dx = Math.abs(x - c);
    const dy = Math.abs(y - c);
    if (dx + dy < 5) return grey(dx + dy < 2 ? 150 : 250); // medallion
    if (dx + dy < 7) return grey(176);
    const lattice = (x + y) % 8 === 0 || (x - y + SIZE) % 8 === 0;
    return grey((lattice ? 226 : 198) + (rand() - 0.5) * 6);
  });
}

/** Lamp shade: vertical pleats, a pale scalloped hem at the bottom and a band at the top. */
export function lampShade() {
  const rand = rng(8803);
  return image(SIZE, (x, y) => {
    if (y < 3) return grey(214);
    if (y >= SIZE - 4) {
      const scallop = Math.abs((x % 6) - 2.5);
      const depth = SIZE - 1 - y;
      return grey(depth > 2 - scallop / 2.5 ? 246 : 168);
    }
    const pleat = x % 4 < 2 ? 236 : 214;
    return grey(pleat + (rand() - 0.5) * 6);
  });
}
