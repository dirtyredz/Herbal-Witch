// Shared painting helpers for the procedural textures: the house gold ramp and seeded value noise.

export const GOLD = [[154, 104, 30], [212, 160, 58], [242, 201, 90]]; // shadow, mid, light

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
