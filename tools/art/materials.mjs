// Material synthesis: the surfaces furniture is built from. Tinted materials (wood grain, painted
// wood, cut stone) are neutral greys so the finish's frame colour sets their hue in game; brass,
// glass and tinctures carry their own colour and are never tinted. Every tile is 32 x 32 pixels,
// i.e. two texels per model pixel.
import { rng, clamp, grey, shade, image } from './png.mjs';

export const SIZE = 32;

/** Natural wood frame: long wavy grain along U with a few darker latewood lines and pores. */
export function woodGrain() {
  const rand = rng(1101);
  const lineOffset = Array.from({ length: SIZE }, () => rand() * 0.6);
  return image(SIZE, (x, y) => {
    const wave = Math.sin(x * 0.21 + y * 0.05) * 1.6 + Math.sin(x * 0.07 + 1.3) * 2.2;
    const ring = (((y + wave + lineOffset[y]) % 5) + 5) % 5;
    let v = 226 + Math.sin((y + wave) * 1.25) * 9;
    if (ring < 0.9) v -= 34; // latewood line
    else if (ring < 1.6) v -= 12;
    v += (rand() - 0.5) * 10;
    if (rand() < 0.025) v -= 28; // pore
    return grey(v);
  });
}

/** Painted wood: an even coat with faint grain showing through, brush streaks and pale wear. */
export function paintedWood() {
  const rand = rng(2203);
  return image(SIZE, (x, y) => {
    let v = 236 + Math.sin(y * 1.1 + Math.sin(x * 0.18) * 1.4) * 4;
    v += Math.sin(x * 0.9 + y * 0.15) * 2; // brush streaks
    v += (rand() - 0.5) * 6;
    if (y % 16 === 15) v -= 22; // board seam
    if (rand() < 0.012) v += 16; // worn speck
    return grey(v);
  });
}

/** Cut stone: two courses of ashlar per tile with recessed mortar and chisel noise. */
export function cutStone() {
  const rand = rng(3307);
  const blockTone = [0.98, 1.03, 0.95, 1.01, 0.97, 1.04];
  return image(SIZE, (x, y) => {
    const course = Math.floor(y / 16);
    const offset = course % 2 === 0 ? 0 : 12;
    const col = Math.floor(((x + offset) % SIZE) / 20);
    const mortarY = y % 16 === 15;
    const mortarX = (x + offset) % SIZE === 0 || (x + offset) % SIZE === 20;
    if (mortarY || mortarX) return grey(150 + (rand() - 0.5) * 10);
    let v = 206 * blockTone[(course * 2 + col) % blockTone.length];
    if (y % 16 === 0) v += 10; // lit top edge of a course
    v += (rand() - 0.5) * 22;
    return grey(v);
  });
}

/** Warm brass: diagonal sheen with tarnish speckle. Untinted. */
export function brass() {
  const rand = rng(4409);
  const base = [196, 152, 64];
  return image(SIZE, (x, y) => {
    const sheen = 0.8 + 0.34 * Math.cos(((x + y) / SIZE) * Math.PI) ** 2;
    let f = sheen + (rand() - 0.5) * 0.08;
    if ((x + y) % 13 === 0) f *= 1.12;
    if (rand() < 0.03) f *= 0.82; // tarnish
    return shade(base, f);
  });
}

/**
 * Bottle glass for cutout rendering: an opaque pale rim, a diagonal highlight, and a fully
 * transparent body so whatever is inside the bottle shows through. Alpha is only 0 or 255.
 */
export function glass() {
  const clear = [0, 0, 0, 0];
  return image(SIZE, (x, y) => {
    const rim = x < 2 || y < 2 || x >= SIZE - 2 || y >= SIZE - 2;
    if (rim) return [clamp(204 + ((x + y) % 3) * 6), 226, 222, 255];
    if (x - y === 6 || x - y === 7) return [244, 250, 248, 255]; // highlight streak
    return clear;
  });
}

/**
 * Tinctures seen through bottle glass: three colour bands along U (sage green, amber, plum), each
 * with a lighter meniscus at the top. Models pick a band through their UVs. Untinted.
 */
export function tincture() {
  const rand = rng(5501);
  const bands = [[92, 128, 70], [196, 128, 40], [104, 52, 104]];
  return image(SIZE, (x, y) => {
    const band = bands[Math.min(2, Math.floor(x / 11))];
    let f = 0.9 - (y / SIZE) * 0.25 + (rand() - 0.5) * 0.06;
    if (y < 2) f = 1.35; // meniscus
    return shade(band, f);
  });
}

/** Dark wood for the stations and the lantern: rich brown grain along U with pores. Untinted. */
export function darkWood() {
  const rand = rng(6607);
  const lineOffset = Array.from({ length: SIZE }, () => rand() * 0.7);
  const dark = [59, 36, 22];
  const light = [90, 58, 34];
  return image(SIZE, (x, y) => {
    const wave = Math.sin(x * 0.19 + y * 0.06) * 1.7 + Math.sin(x * 0.06 + 0.7) * 2.4;
    const ring = (((y + wave + lineOffset[y]) % 6) + 6) % 6;
    let t = 0.35 + Math.sin((y + wave) * 1.1) * 0.18;
    if (ring < 0.9) t = 0; // latewood line stays dark
    else if (ring < 1.8) t = 0.2;
    t += (rand() - 0.5) * 0.18;
    t = Math.max(0, Math.min(1, t));
    let c = [0, 1, 2].map((i) => dark[i] + (light[i] - dark[i]) * t);
    if (rand() < 0.03) c = c.map((v) => v * 0.72); // pore
    return [clamp(c[0]), clamp(c[1]), clamp(c[2]), 255];
  });
}

/**
 * Trailing ivy for cutout rendering: leafy stems that wander down from the top edge, deep green
 * leaves with darker veins on a transparent background. Alpha is only 0 or 255.
 */
export function vine() {
  const rand = rng(7703);
  const stem = [47, 90, 44];
  const leafMid = [62, 122, 58];
  const leafLight = [78, 140, 70];
  const vein = [35, 69, 32];
  const grid = Array.from({ length: SIZE * SIZE }, () => null);
  const put = (x, y, c) => {
    x = Math.round(x);
    y = Math.round(y);
    if (x >= 0 && y >= 0 && x < SIZE && y < SIZE) grid[y * SIZE + x] = c;
  };
  const stems = [[3, 0.2], [9, 1.7], [15, 3.1], [21, 4.4], [27, 5.6]];
  for (const [x0, phase] of stems) {
    const len = 22 + Math.floor(rand() * 10);
    let side = rand() < 0.5 ? -1 : 1;
    for (let y = 0; y < len; y++) {
      const sx = x0 + Math.sin(y * 0.33 + phase) * 2.2;
      put(sx, y, stem);
      if (y % 4 === 2 && y > 2) {
        // a leaf: a small ellipse hanging off the stem with a vein along its axis
        const rx = 2.6 + rand() * 0.9;
        const ry = 1.5;
        const cx = sx + side * (rx - 0.2);
        const cy = y + 1;
        for (let yy = -2; yy <= 2; yy++) {
          for (let xx = -4; xx <= 4; xx++) {
            if ((xx / rx) ** 2 + (yy / ry) ** 2 > 1) continue;
            const px = Math.round(cx) + xx;
            const py = Math.round(cy) + yy;
            put(px, py, yy === 0 ? vein : yy < 0 ? leafLight : leafMid);
          }
        }
        side = -side;
      }
    }
  }
  return image(SIZE, (x, y) => {
    const c = grid[y * SIZE + x];
    return c ? [c[0], c[1], c[2], 255] : [0, 0, 0, 0];
  });
}
