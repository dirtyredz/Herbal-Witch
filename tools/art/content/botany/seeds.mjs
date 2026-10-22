// Botanical seed layouts stay with botany; only their drawing primitives are shared.
import { rng, set, sprite } from '../../drawing/sprites.mjs';

const LAYOUTS = [
  [[6, 7], [9, 6], [8, 10], [5, 10], [11, 9]],
  [[7, 5], [5, 8], [9, 8], [7, 11], [11, 11]],
  [[4, 8], [7, 7], [10, 8], [6, 11], [9, 11], [12, 6]],
  [[8, 6], [6, 9], [10, 10], [4, 11], [8, 12]],
];

/** Seed cluster: layout picked by id, colours from ramp r, shape 'oval' | 'round' | 'speck' | 'long'. */
export function seeds(id, r, shape, extra = null) {
  return sprite((cv) => {
    const rand = rng(900 + id * 17);
    for (const [x, y] of LAYOUTS[id % LAYOUTS.length]) {
      const jx = Math.floor(rand() * 2);
      const jy = Math.floor(rand() * 2);
      const sx = x + jx;
      const sy = y + jy;
      if (shape === 'oval') {
        set(cv, sx, sy, r[1]);
        set(cv, sx + 1, sy, r[0]);
        set(cv, sx, sy - 1, r[2]);
      } else if (shape === 'round') {
        set(cv, sx, sy, r[2]);
        set(cv, sx + 1, sy, r[1]);
        set(cv, sx, sy + 1, r[1]);
        set(cv, sx + 1, sy + 1, r[0]);
      } else if (shape === 'long') {
        set(cv, sx, sy, r[1]);
        set(cv, sx, sy + 1, r[0]);
        set(cv, sx, sy - 1, r[2]);
      } else {
        set(cv, sx, sy, r[1]);
        set(cv, sx + 1, sy + 1, r[0]);
      }
    }
    if (extra) extra(cv);
  });
}
