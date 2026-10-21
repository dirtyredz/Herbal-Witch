// The oil lantern's materials (blackened iron, glowing coals, flame tongues), also the flames of the
// hearth. 32 x 32, untinted; flame has alpha 0 or 255 (cutout). The house-style station materials
// are in house.mjs and stone.mjs.
import { rng, clamp, shade, image } from '../pixels.mjs';

const SIZE = 32;

/** Blackened iron with a soft diagonal sheen, a few rivet-bright specks and pitting. */
export function iron() {
  const rand = rng(8802);
  const base = [44, 44, 52];
  return image(SIZE, (x, y) => {
    let f = 0.85 + 0.4 * Math.cos(((x - y) / SIZE) * Math.PI * 2) ** 2 * 0.6 + (rand() - 0.5) * 0.12;
    if (rand() < 0.02) f *= 1.7; // bright speck
    if (rand() < 0.04) f *= 0.65; // pit
    return shade(base, f);
  });
}

/** Glowing coals: dark red clinker with orange glow cells and a scatter of yellow flecks. */
export function ember() {
  const rand = rng(8804);
  return image(SIZE, (x, y) => {
    const glow = Math.sin(x * 0.55) * Math.sin(y * 0.6 + 1) + Math.sin((x + y) * 0.31 + 2) * 0.5;
    let c = [122, 26, 16];
    if (glow > 0.5) c = [232, 96, 28];
    else if (glow > -0.1) c = [186, 52, 20];
    const n = rand();
    if (n < 0.06) c = [255, 200, 58];
    else if (n < 0.1) c = [70, 18, 14];
    return [c[0], c[1], c[2], 255];
  });
}

/**
 * Flame tongues for cutout planes: three stylised tongues rising from a joined base, orange outside
 * with a yellow core. They live in the bottom half (rows 14-31) so a plane can use v 8..16 only.
 */
export function flame() {
  const tongues = [[8, 17, 5.5], [16, 14, 7], [24, 18, 5]]; // centre x, top row, half-width at the base
  const BASE = 31;
  return image(SIZE, (x, y) => {
    let best = null;
    for (const [cx, top, hw] of tongues) {
      if (y < top || y > BASE) continue;
      const t = (y - top) / (BASE - top); // 0 at the tip, 1 at the base
      const half = hw * Math.pow(t, 0.75) + (t < 0.15 ? 0 : 0.4);
      const d = Math.abs(x - cx - Math.sin(y * 0.5) * 0.8 * (1 - t));
      if (d > half) continue;
      const inner = d < half * 0.5 && t > 0.3;
      const core = d < half * 0.25 && t > 0.55;
      const c = core ? [255, 238, 150] : inner ? [255, 216, 74] : t < 0.35 ? [255, 160, 40] : [240, 124, 28];
      if (!best || (core && !best.core)) best = { c, core };
    }
    if (y >= 30 && x >= 4 && x <= 27) return [226, 96, 24, 255]; // joined base
    return best ? [clamp(best.c[0]), clamp(best.c[1]), clamp(best.c[2]), 255] : [0, 0, 0, 0];
  });
}
