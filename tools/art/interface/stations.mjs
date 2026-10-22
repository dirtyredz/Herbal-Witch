// Station GUI atlases: nine 256 x 256 RGBA sheets in the vanilla container layout. Each has the
// 176 x 166 background panel at (0, 0) (dark wood frame, parchment inside, a vine flourish on the top
// edge and a dark purple corner ornament), the player inventory, the station's slot frames and EMPTY
// progress arrow / flame outlines, plus the FILLED flame at (176, 0) and FILLED arrow at (176, 14)
// that the screens clip to show progress. Deterministic, no dependencies.
import { C, Sheet, hex, panel, slot, ghost, inventory } from './kit.mjs';

const GHOST_BOTTLE = [[1, 7, 8], [2, 7, 8], [3, 7, 8], [4, 6, 9], [5, 5, 10], [6, 4, 11], [7, 4, 11], [8, 4, 11], [9, 4, 11], [10, 4, 11], [11, 4, 11], [12, 5, 10], [13, 6, 9]];
const GHOST_BOWL = [[6, 2, 13], [7, 2, 13], [8, 3, 12], [9, 3, 12], [10, 4, 11], [11, 5, 10], [12, 6, 9]];
const GHOST_COAL = [[5, 6, 9], [6, 4, 11], [7, 3, 12], [8, 3, 12], [9, 3, 12], [10, 4, 11], [11, 5, 10]];

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

export const cauldron = () => atlas((s) => {
  for (const x of [30, 48, 66]) slot(s, x, 17);
  slot(s, 48, 53);
  ghost(s, 48, 53, GHOST_BOTTLE);
  slot(s, 124, 35);
  slot(s, 124, 58);
  emptyArrow(s, 92, 34);
  emptyFlame(s, 50, 36);
});

export const still = () => atlas((s) => {
  slot(s, 30, 17);
  slot(s, 48, 17);
  slot(s, 30, 53);
  ghost(s, 30, 53, GHOST_BOTTLE);
  slot(s, 48, 53);
  ghost(s, 48, 53, GHOST_BOTTLE);
  slot(s, 116, 35);
  slot(s, 116, 58);
  emptyArrow(s, 79, 34);
  emptyFlame(s, 81, 54);
});

export const bottling_bench = () => atlas((s) => {
  for (const x of [30, 48, 66]) slot(s, x, 17);
  slot(s, 48, 53);
  ghost(s, 48, 53, GHOST_BOTTLE);
  slot(s, 124, 35);
  slot(s, 124, 58);
  emptyArrow(s, 92, 34);
});

export const charm_workbench = () => atlas((s) => {
  for (const x of [30, 48, 66]) slot(s, x, 26);
  slot(s, 124, 35);
  slot(s, 124, 58);
  emptyArrow(s, 92, 34);
});
