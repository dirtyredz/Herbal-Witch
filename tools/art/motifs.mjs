// The house motifs, painted onto 32 x 32 texture grids: gold crescent moons, gold four-point
// stars, small pink/magenta flowers on olive vine sprigs. A Grid is a size x size array of
// [r, g, b, a] (or null for "leave the base"); positions are in texels.
import { clamp } from './pixels.mjs';

export const GOLD = [[154, 104, 30], [212, 160, 58], [242, 201, 90]]; // shadow, mid, light
export const PINK = [[176, 40, 104], [224, 74, 140], [246, 140, 186]];
export const MAGENTA = [[140, 30, 96], [196, 48, 128], [232, 96, 168]];
export const OLIVE = [[84, 96, 34], [116, 128, 46], [150, 160, 66]];
export const LEAF = [[52, 92, 38], [78, 126, 50], [116, 162, 70]];

export function grid(size = 32) {
  return { size, px: new Array(size * size).fill(null) };
}

export function put(g, x, y, c) {
  x = Math.round(x);
  y = Math.round(y);
  if (x < 0 || y < 0 || x >= g.size || y >= g.size) return;
  g.px[y * g.size + x] = c.length === 4 ? c : [c[0], c[1], c[2], 255];
}

/** A crescent moon of radius r opening to the right (flip = true opens left), lit from the top-left. */
export function moon(g, cx, cy, r, flip = false) {
  const off = r * 0.48 * (flip ? -1 : 1);
  for (let y = Math.floor(cy - r); y <= Math.ceil(cy + r); y++) {
    for (let x = Math.floor(cx - r); x <= Math.ceil(cx + r); x++) {
      const inOuter = (x - cx) ** 2 + (y - cy) ** 2 <= r * r;
      const inInner = (x - cx - off) ** 2 + (y - cy + r * 0.1) ** 2 <= (r * 0.7) ** 2;
      if (!inOuter || inInner) continue;
      const edge = (x - cx) ** 2 + (y - cy) ** 2 > (r - 1.1) ** 2;
      const lit = y - cy < -r * 0.3 || (flip ? x > cx + r * 0.2 : x < cx - r * 0.2);
      put(g, x, y, edge && !lit ? GOLD[0] : lit ? GOLD[2] : GOLD[1]);
    }
  }
}

/** A four-point star (a plus with a bright centre); size 1 = 3 x 3, size 2 = 5 x 5. */
export function star(g, cx, cy, size = 1) {
  for (let i = -size; i <= size; i++) {
    put(g, cx + i, cy, GOLD[1]);
    put(g, cx, cy + i, GOLD[1]);
  }
  put(g, cx, cy, GOLD[2]);
  if (size > 1) {
    put(g, cx - size, cy, GOLD[0]);
    put(g, cx, cy + size, GOLD[0]);
  }
}

/** A small flower: four (or five, with petals = 5) petals around a gold or deep centre. */
export function flower(g, cx, cy, ramp = PINK, petals = 4) {
  const ring = petals === 5
    ? [[0, -1], [1, 0], [1, 1], [-1, 1], [-1, 0]]
    : [[0, -1], [1, 0], [0, 1], [-1, 0]];
  for (const [dx, dy] of ring) put(g, cx + dx, cy + dy, dy < 0 || dx < 0 ? ramp[2] : ramp[1]);
  put(g, cx, cy, petals === 5 ? GOLD[1] : ramp[0]);
}

/** An olive vine sprig rising from (x, y) with small leaves, length len, leaning by lean per texel. */
export function sprig(g, x, y, len, lean = 0.25, ramp = OLIVE) {
  let side = 1;
  for (let i = 0; i < len; i++) {
    const sx = x + Math.sin(i * 0.7) * 0.8 + i * lean;
    put(g, sx, y - i, ramp[0]);
    if (i > 0 && i % 2 === 0) {
      put(g, sx + side, y - i, ramp[1]);
      put(g, sx + side * 2, y - i - 1, ramp[2]);
      side = -side;
    }
  }
}

/** Paints a grid over a base image ({size, px}); null cells keep the base. */
export function overlay(base, g) {
  return { size: base.size, px: base.px.map((p, i) => g.px[i] ?? p) };
}

/** Soft value noise in [0,1) from a seeded random source, smoothed over `cell` texels (tiles at size). */
export function smoothNoise(rand, size, cell) {
  const n = Math.ceil(size / cell);
  const pts = Array.from({ length: n * n }, () => rand());
  const at = (i, j) => pts[((j % n) + n) % n * n + (((i % n) + n) % n)];
  return (x, y) => {
    const fx = x / cell;
    const fy = y / cell;
    const i = Math.floor(fx);
    const j = Math.floor(fy);
    const tx = fx - i;
    const ty = fy - j;
    const s = (t) => t * t * (3 - 2 * t);
    const a = at(i, j) + (at(i + 1, j) - at(i, j)) * s(tx);
    const b = at(i, j + 1) + (at(i + 1, j + 1) - at(i, j + 1)) * s(tx);
    return a + (b - a) * s(ty);
  };
}

export const rgb = (c, f = 1) => [clamp(c[0] * f), clamp(c[1] * f), clamp(c[2] * f), 255];
