// Furniture textiles and painted surfaces in the cottage design (docs/ART.md): forest-green velvet
// with tufting, deep green painted drawer fronts and the draped runner (a decal). The small rug and
// the lamp shade are painted in their Blockbench projects (src/art/blockbench/decor/). Plain fabric
// for upholstery (no floral cushions); small embroidered moons, stars and flowers belong on runners
// and rugs. 32 x 32; the runner has alpha 0/255 (its pointed hem).
import { rng, shade, mix, image } from './pixels.mjs';
import { SIZE } from './materials.mjs';
import { grid, moon, star, flower, overlay, put, smoothNoise, GOLD, PINK, MAGENTA } from './motifs.mjs';

const FOREST = [28, 56, 38];
const FOREST_LIGHT = [40, 74, 50];
const MAGENTA_C = [196, 48, 128];
const PLUM = [74, 36, 82];

/** Forest-green velvet with subtle tufting: small magenta diamond studs with gold centres, every 16 texels. */
export function velvet() {
  const n = smoothNoise(rng(8901), SIZE, 4);
  const rand = rng(8902);
  const base = image(SIZE, (x, y) => shade(mix(FOREST, FOREST_LIGHT, n(x, y) * 0.8), 0.95 + rand() * 0.07));
  const g = grid();
  for (const [cx, cy] of [[8, 8], [24, 24], [24, 8], [8, 24]]) {
    for (const [dx, dy] of [[0, -1], [1, 0], [0, 1], [-1, 0]]) put(g, cx + dx, cy + dy, MAGENTA[1]);
    put(g, cx, cy, GOLD[1]);
    put(g, cx - 1, cy - 2, shade(FOREST, 0.75));
    put(g, cx + 1, cy + 2, shade(FOREST, 0.75));
  }
  return overlay(base, g);
}

/** Deep green paint over wood: drawer fronts and panels, with faint brushed grain. */
export function paintGreen() {
  const rand = rng(8903);
  return image(SIZE, (x, y) => shade(Math.sin(y * 0.9 + Math.sin(x * 0.2)) > 0.6 ? [40, 82, 46] : [46, 92, 52], 0.95 + rand() * 0.07));
}

/**
 * A draped runner, stretched over one face: a green field (the armchair's) or a magenta field with
 * a gold crescent (the apothecary chest's), magenta-and-plum or gold borders, a pointed hem with an
 * embroidered flower and a gold tassel tip. Transparent outside the hem.
 */
export function runner(magenta = false) {
  const rand = rng(8904);
  const g = grid();
  flower(g, 16, 22, magenta ? [[150, 34, 98], [238, 150, 196], [255, 200, 226]] : PINK, 5);
  if (magenta) moon(g, 16, 11, 4.5);
  else star(g, 16, 13, 1);
  const FIELD = magenta ? [176, 40, 110] : FOREST;
  const FIELD_LIGHT = magenta ? [196, 48, 128] : FOREST_LIGHT;
  const BORDER = magenta ? GOLD[1] : MAGENTA_C;
  return overlay(image(SIZE, (x, y) => {
    const tip = 24 + (16 - Math.abs(x - 15.5)) * 0.45; // the pointed hem
    if (y > tip) return Math.abs(x - 15.5) < 1 && y < tip + 3 ? [GOLD[1][0], GOLD[1][1], GOLD[1][2], 255] : [0, 0, 0, 0];
    const edge = Math.min(x, SIZE - 1 - x);
    if (edge < 2 || y > tip - 2) return shade(BORDER, 0.95 + rand() * 0.08);
    if (edge < 3 || y > tip - 3) return shade(PLUM, 1);
    return shade((x + y) % 2 ? FIELD : FIELD_LIGHT, 0.95 + rand() * 0.06);
  }), g);
}
