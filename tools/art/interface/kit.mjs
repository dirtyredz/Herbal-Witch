// The house GUI drawing shared by every interface atlas (stations, decor storage): a 256 x 256 RGBA
// sheet in vanilla's container layout, the wood-framed parchment panel (any height), 18 x 18 slot
// frames, faint slot silhouettes and the player inventory. Deterministic, no dependencies.
import { rng } from '../pixels.mjs';

export const W = 256;
export const hex = (s) => [1, 3, 5].map((i) => parseInt(s.slice(i, i + 2), 16));
export const C = {
  edge: hex('#24160d'),
  wood: hex('#5a3a22'),
  woodHi: hex('#7a5230'),
  woodLo: hex('#3b2416'),
  parch: hex('#d9c7a0'),
  parchHi: hex('#e8dab8'),
  parchLo: hex('#b8a47c'),
  slot: hex('#a8946f'),
  slotLo: hex('#4a3622'),
  slotHi: hex('#f2e8cf'),
  ghost: hex('#8e7a56'),
  pink: hex('#d8407a'),
  green: hex('#2f5a2c'),
  greenHi: hex('#4a8040'),
  purple: hex('#4a2a55'),
  line: hex('#4a3622'),
};

export class Sheet {
  constructor() {
    this.px = Array.from({ length: W * W }, () => [0, 0, 0, 0]);
  }
  set(x, y, c) {
    if (x < 0 || y < 0 || x >= W || y >= W) return;
    this.px[y * W + x] = [c[0], c[1], c[2], 255];
  }
  get(x, y) {
    return this.px[y * W + x];
  }
  rect(x0, y0, w, h, c) {
    for (let y = y0; y < y0 + h; y++) for (let x = x0; x < x0 + w; x++) this.set(x, y, c);
  }
  image() {
    return { size: W, px: this.px };
  }
}

/**
 * The 176-wide panel, `h` tall (vanilla's 166 by default): wood frame with grain, bevelled
 * parchment, vine on top, purple corner studs.
 */
export function panel(s, h = 166) {
  const rand = rng(9101);
  s.rect(0, 0, 176, h, C.edge);
  for (let y = 1; y < h - 1; y++) {
    for (let x = 1; x < 175; x++) {
      const t = rand();
      s.set(x, y, t < 0.18 ? C.woodLo : t > 0.86 ? C.woodHi : C.wood);
    }
  }
  s.rect(3, 3, 170, h - 6, C.parchLo); // inner bevel: dark top/left edge...
  s.rect(4, 4, 169, h - 7, C.parchHi); // ...light bottom/right edge...
  s.rect(4, 4, 168, h - 8, C.parch);
  for (let x = 4; x < 172; x++) s.set(x, 4, C.parchLo);
  for (let y = 4; y < h - 4; y++) s.set(4, y, C.parchLo);
  // light fibre flecks in the parchment
  for (let i = 0; i < 160; i++) s.set(6 + Math.floor(rand() * 164), 6 + Math.floor(rand() * (h - 12)), rand() < 0.5 ? C.parchHi : C.parchLo);
  // frame highlight line just outside the inner bevel
  for (let x = 1; x < 175; x++) s.set(x, 1, C.woodHi);
  for (let y = 1; y < h - 1; y++) s.set(1, y, C.woodHi);
  // dark purple corner ornaments, 3 x 3 studs with a lit pixel
  for (const [cx, cy] of [[1, 1], [172, 1], [1, h - 4], [172, h - 4]]) {
    s.rect(cx, cy, 3, 3, C.purple);
    s.set(cx, cy, hex('#6b4a7a'));
  }
  // vine flourish: a stem along the top frame row with leaves dipping into the inner area to the right
  // of the title, so the title area at (8, 6) stays clear
  const vine = [hex('#2f5a2c'), hex('#4a8040'), hex('#1f3f1e')];
  for (let x = 6; x < 170; x++) {
    const y = 2 + (Math.round(Math.sin(x * 0.45)) > 0 ? 1 : 0);
    s.set(x, y, vine[0]);
    if (x % 6 === 2) {
      const leafY = y - 1;
      s.set(x, leafY, vine[1]);
      s.set(x + 1, leafY, vine[1]);
      s.set(x + 1, y + 1, vine[2]);
    }
  }
  for (const x0 of [134, 148, 160]) {
    // pendant tendrils hang over the parchment on the right half only
    const len = x0 === 148 ? 5 : 3;
    for (let i = 0; i < len; i++) s.set(x0, 5 + i, vine[0]);
    s.set(x0 + 1, 5 + len - 1, vine[1]);
    s.set(x0 - 1, 5 + len - 2, vine[1]);
  }
}

/** An 18 x 18 slot frame whose item position is (ix, iy): dark top/left, light bottom/right. */
export function slot(s, ix, iy) {
  const x0 = ix - 1;
  const y0 = iy - 1;
  s.rect(x0, y0, 18, 18, C.slot);
  for (let i = 0; i < 18; i++) {
    s.set(x0 + i, y0, C.slotLo);
    s.set(x0, y0 + i, C.slotLo);
  }
  for (let i = 1; i < 18; i++) {
    s.set(x0 + i, y0 + 17, C.slotHi);
    s.set(x0 + 17, y0 + i, C.slotHi);
  }
  s.set(x0 + 17, y0, C.parch);
  s.set(x0, y0 + 17, C.parch);
}

/** A faint silhouette inside the 16 x 16 item area of a slot: rows of x-ranges relative to (ix, iy). */
export function ghost(s, ix, iy, rows) {
  for (const [dy, a, b] of rows) for (let dx = a; dx <= b; dx++) s.set(ix + dx, iy + dy, C.ghost);
}

/** The player inventory (three rows from `top`) and the hotbar 58 below it. */
export function inventory(s, top = 84) {
  for (let r = 0; r < 3; r++) for (let c = 0; c < 9; c++) slot(s, 8 + 18 * c, top + 18 * r);
  for (let c = 0; c < 9; c++) slot(s, 8 + 18 * c, top + 58);
}
