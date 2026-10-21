// Station GUI atlases: five 256 x 256 RGBA sheets in the vanilla container layout. Each has the
// 176 x 166 background panel at (0, 0) (dark wood frame, parchment inside, a vine flourish on the top
// edge and a dark purple corner ornament), the player inventory, the station's slot frames and EMPTY
// progress arrow / flame outlines, plus the FILLED flame at (176, 0) and FILLED arrow at (176, 14)
// that the screens clip to show progress. Deterministic, no dependencies.
import { rng } from '../png.mjs';

const W = 256;
const hex = (s) => [1, 3, 5].map((i) => parseInt(s.slice(i, i + 2), 16));
const C = {
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

class Sheet {
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

/** The 176 x 166 panel: wood frame with grain, bevelled parchment, vine on top, purple corner studs. */
function panel(s) {
  const rand = rng(9101);
  s.rect(0, 0, 176, 166, C.edge);
  for (let y = 1; y < 165; y++) {
    for (let x = 1; x < 175; x++) {
      const t = rand();
      s.set(x, y, t < 0.18 ? C.woodLo : t > 0.86 ? C.woodHi : C.wood);
    }
  }
  s.rect(3, 3, 170, 160, C.parchLo); // inner bevel: dark top/left edge...
  s.rect(4, 4, 169, 159, C.parchHi); // ...light bottom/right edge...
  s.rect(4, 4, 168, 158, C.parch);
  for (let x = 4; x < 172; x++) s.set(x, 4, C.parchLo);
  for (let y = 4; y < 162; y++) s.set(4, y, C.parchLo);
  // light fibre flecks in the parchment
  for (let i = 0; i < 160; i++) s.set(6 + Math.floor(rand() * 164), 6 + Math.floor(rand() * 154), rand() < 0.5 ? C.parchHi : C.parchLo);
  // frame highlight line just outside the inner bevel
  for (let x = 1; x < 175; x++) s.set(x, 1, C.woodHi);
  for (let y = 1; y < 165; y++) s.set(1, y, C.woodHi);
  // dark purple corner ornaments, 3 x 3 studs with a lit pixel
  for (const [cx, cy] of [[1, 1], [172, 1], [1, 162], [172, 162]]) {
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
function slot(s, ix, iy) {
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
function ghost(s, ix, iy, rows) {
  for (const [dy, a, b] of rows) for (let dx = a; dx <= b; dx++) s.set(ix + dx, iy + dy, C.ghost);
}
const GHOST_BOTTLE = [[1, 7, 8], [2, 7, 8], [3, 7, 8], [4, 6, 9], [5, 5, 10], [6, 4, 11], [7, 4, 11], [8, 4, 11], [9, 4, 11], [10, 4, 11], [11, 4, 11], [12, 5, 10], [13, 6, 9]];
const GHOST_BOWL = [[6, 2, 13], [7, 2, 13], [8, 3, 12], [9, 3, 12], [10, 4, 11], [11, 5, 10], [12, 6, 9]];
const GHOST_COAL = [[5, 6, 9], [6, 4, 11], [7, 3, 12], [8, 3, 12], [9, 3, 12], [10, 4, 11], [11, 5, 10]];

function inventory(s) {
  for (let r = 0; r < 3; r++) for (let c = 0; c < 9; c++) slot(s, 8 + 18 * c, 84 + 18 * r);
  for (let c = 0; c < 9; c++) slot(s, 8 + 18 * c, 142);
}

// --- progress arrow (24 x 17) and flame (14 x 14) masks ---

const arrowIn = (x, y) => (x <= 14 && y >= 5 && y <= 11) || (x >= 15 && x <= 23 && Math.abs(y - 8) <= 8 - (x - 15));
const FLAME = [
  '......#.......',
  '.....##.......',
  '.....###..#...',
  '....####..##..',
  '....#####.##..',
  '...##########.',
  '...###########',
  '..############',
  '..############',
  '..############',
  '..############',
  '...##########.',
  '....########..',
  '.....######...',
];
const flameIn = (x, y) => FLAME[y]?.[x] === '#';

function drawMask(s, x0, y0, w, h, inside, edgeColour, fill, fillAt = null) {
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      if (!inside(x, y)) continue;
      const edge = [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([dx, dy]) => !inside(x + dx, y + dy));
      s.set(x0 + x, y0 + y, edge ? edgeColour : fillAt ? fillAt(x, y) : fill);
    }
  }
}

const emptyArrow = (s, x, y) => drawMask(s, x, y, 24, 17, arrowIn, C.line, C.parchLo);
const emptyFlame = (s, x, y) => drawMask(s, x, y, 14, 14, flameIn, C.purple, C.parchLo);

function filledSprites(s) {
  drawMask(s, 176, 0, 14, 14, flameIn, hex('#8a2a10'), null, (x, y) => (y >= 8 && x >= 5 && x <= 9 ? hex('#ffd84a') : y < 5 ? hex('#ffb02e') : hex('#f08a1c')));
  drawMask(s, 176, 14, 24, 17, arrowIn, C.green, C.pink, (x, y) => (y === 6 || y === 10 ? hex('#ee7aa4') : C.pink));
}

function atlas(setup) {
  const s = new Sheet();
  panel(s);
  inventory(s);
  setup(s);
  filledSprites(s);
  return s.image();
}

export const drying_rack = () => atlas((s) => {
  slot(s, 56, 35);
  slot(s, 116, 35);
  emptyArrow(s, 79, 34);
});

export const mortar = () => atlas((s) => {
  for (const x of [30, 48, 66]) slot(s, x, 35);
  slot(s, 124, 35);
  slot(s, 124, 58);
  emptyArrow(s, 92, 34);
});

export const kettle = () => atlas((s) => {
  slot(s, 30, 26);
  slot(s, 48, 26);
  slot(s, 39, 50);
  ghost(s, 39, 50, GHOST_BOTTLE);
  slot(s, 116, 35);
  slot(s, 116, 58);
  emptyArrow(s, 79, 34);
  emptyFlame(s, 81, 54);
});

export const infusion_pot = () => atlas((s) => {
  slot(s, 30, 17);
  slot(s, 48, 17);
  slot(s, 30, 53);
  ghost(s, 30, 53, GHOST_BOTTLE);
  slot(s, 48, 53);
  ghost(s, 48, 53, GHOST_BOWL);
  slot(s, 116, 35);
  slot(s, 116, 58);
  emptyArrow(s, 79, 34);
  emptyFlame(s, 81, 54);
});

export const hearth = () => atlas((s) => {
  slot(s, 80, 44);
  ghost(s, 80, 44, GHOST_COAL);
  slot(s, 116, 44);
  emptyFlame(s, 81, 26);
});
