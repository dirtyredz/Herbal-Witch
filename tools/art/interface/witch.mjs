// Witchcraft GUI atlases (Milestone 6): one 256 x 256 sheet each, in vanilla GUI units (the
// catalogue enlarges them 2x). Java blits these regions by 256-unit coordinates; everything else is
// transparent. Deterministic.
//
// energy (EnergyOverlay):
//   (  0,  0) 82 x 10  bar frame: dark wood rim, gold end studs, a dark plum track inside (x 2..79, y 2..7)
//   (  0, 12) 78 x  6  the fill: violet to pink, lit top row
//   (  0, 20) 78 x  6  the empty state: a dark track with a jagged red crack (shown only at zero)
//   ( 84,  0)  9 x  9  rest indicator: a green leaf
//   ( 94,  0)  9 x  9  meditation indicator: a pale crescent moon
//
// altar (AltarScreen):
//   (  0,  0) 252 x 200  the panel: wood frame, parchment, a vine along the top, a faint divider at x 124..125
//   (  0, 204) 120 x 8   progress frame (dark wood, plum track)
//   (  0, 214) 116 x 4   progress fill (violet to pink)
//
// ward (WardScreen):
//   (  0,   0) 200 x 150  the panel: wood frame, parchment, a vine along the top, a faint divider at y 74..75
//   (  0, 154) 120 x   8  charge bar frame (dark wood, plum track inside x 2..117, y 2..5)
//   (  0, 164) 116 x   4  charge fill (violet to pink, lit top row)
import { C, Sheet, hex } from './kit.mjs';
import { rng } from '../pixels.mjs';

const VIOLET = [hex('#5c3a8a'), hex('#7a4aa8'), hex('#b0509a'), hex('#d8407a')];
const GOLD = hex('#e0b040');
const GOLD_LO = hex('#8a5e18');
const PLUM_DARK = hex('#2a1630');
const PLUM = hex('#4a2a55');

/** A horizontal violet-to-pink gradient strip with a lit top row. */
function gradient(s, x0, y0, w, h) {
  for (let x = 0; x < w; x++) {
    const c = VIOLET[Math.min(VIOLET.length - 1, Math.floor((x / w) * VIOLET.length))];
    for (let y = 0; y < h; y++) s.set(x0 + x, y0 + y, y === 0 ? hex('#f2b6d8') : c);
  }
}

/** A wood-rimmed frame with a plum track and gold end studs. */
function frame(s, x0, y0, w, h) {
  const rand = rng(7301 + w);
  s.rect(x0, y0, w, h, C.edge);
  for (let y = y0 + 1; y < y0 + h - 1; y++) {
    for (let x = x0 + 1; x < x0 + w - 1; x++) {
      const t = rand();
      s.set(x, y, t < 0.2 ? C.woodLo : t > 0.85 ? C.woodHi : C.wood);
    }
  }
  s.rect(x0 + 2, y0 + 2, w - 4, h - 4, PLUM_DARK);
  for (let x = x0 + 2; x < x0 + w - 2; x++) s.set(x, y0 + 2, PLUM);
  for (const x of [x0 + 1, x0 + w - 2]) {
    s.set(x, y0 + 1, GOLD);
    s.set(x, y0 + h - 2, GOLD_LO);
  }
}

const LEAF = [
  '....gg...',
  '...gGGg..',
  '..gGGGg..',
  '.gGGhGg..',
  '.gGhGGg..',
  '.gGhGg...',
  '..ghg....',
  '.s.......',
  's........',
];
const MOON = [
  '...mmm...',
  '..mMMm...',
  '.mMMm....',
  '.mMm.....',
  '.mMm.....',
  '.mMMm....',
  '..mMMm...',
  '...mmm...',
  '.........',
];

function stamp(s, x0, y0, rows, key) {
  rows.forEach((row, y) => [...row].forEach((ch, x) => key[ch] && s.set(x0 + x, y0 + y, key[ch])));
}

/** The energy HUD atlas. */
export const energy = () => {
  const s = new Sheet();
  frame(s, 0, 0, 82, 10);
  gradient(s, 0, 12, 78, 6);
  // empty: the dark track with a jagged crack
  s.rect(0, 20, 78, 6, PLUM_DARK);
  let y = 2;
  const rand = rng(7302);
  for (let x = 0; x < 78; x++) {
    y = Math.max(0, Math.min(5, y + Math.round((rand() - 0.5) * 2)));
    s.set(x, 20 + y, hex('#a82a3c'));
    if (x % 9 === 4) s.set(x, 20 + Math.max(0, y - 1), hex('#e06a8c'));
  }
  stamp(s, 84, 0, LEAF, { g: C.green, G: hex('#4a8040'), h: hex('#7ab060'), s: hex('#3b2416') });
  stamp(s, 94, 0, MOON, { m: hex('#8e86a8'), M: hex('#ece8f4') });
  return s.image();
};

/**
 * A W x H witchcraft screen panel at the sheet origin: wood frame, bevelled parchment with `specks`
 * fibre flecks, gold corner studs and a vine along the top frame. `rand` is the panel's seeded source.
 */
function screenPanel(s, W, H, rand, specks) {
  s.rect(0, 0, W, H, C.edge);
  for (let yy = 1; yy < H - 1; yy++) {
    for (let x = 1; x < W - 1; x++) {
      const t = rand();
      s.set(x, yy, t < 0.18 ? C.woodLo : t > 0.86 ? C.woodHi : C.wood);
    }
  }
  s.rect(3, 3, W - 6, H - 6, C.parchLo);
  s.rect(4, 4, W - 7, H - 7, C.parchHi);
  s.rect(4, 4, W - 8, H - 8, C.parch);
  for (let i = 0; i < specks; i++) s.set(6 + Math.floor(rand() * (W - 12)), 6 + Math.floor(rand() * (H - 12)), rand() < 0.5 ? C.parchHi : C.parchLo);
}

/** Gold corner studs and a vine along the top frame of a W x H panel. */
function ornament(s, W, H) {
  for (const [cx, cy] of [[1, 1], [W - 4, 1], [1, H - 4], [W - 4, H - 4]]) {
    s.rect(cx, cy, 3, 3, GOLD);
    s.set(cx + 2, cy + 2, GOLD_LO);
  }
  const vine = [hex('#2f5a2c'), hex('#4a8040'), hex('#1f3f1e')];
  for (let x = 6; x < W - 6; x++) {
    const vy = 2 + (Math.round(Math.sin(x * 0.45)) > 0 ? 1 : 0);
    s.set(x, vy, vine[0]);
    if (x % 6 === 2) {
      s.set(x, vy - 1, vine[1]);
      s.set(x + 1, vy + 1, vine[2]);
    }
  }
}

/** The altar screen atlas. */
export const altar = () => {
  const s = new Sheet();
  const W = 252;
  const H = 200;
  screenPanel(s, W, H, rng(7303), 260);
  // a faint ruled divider between the two columns, and a rule above the notice line
  for (let yy = 16; yy < H - 24; yy += 2) s.set(124, yy, C.parchLo);
  for (let x = 8; x < W - 8; x += 2) s.set(x, H - 22, C.parchLo);
  ornament(s, W, H);
  frame(s, 0, 204, 120, 8);
  gradient(s, 0, 214, 116, 4);
  return s.image();
};

/** The ward screen atlas: status above a faint horizontal divider, settings below. */
export const ward = () => {
  const s = new Sheet();
  const W = 200;
  const H = 150;
  screenPanel(s, W, H, rng(7304), 155);
  for (let x = 8; x < W - 8; x++) s.set(x, 74 + (x % 2), C.parchLo);
  ornament(s, W, H);
  frame(s, 0, 154, 120, 8);
  gradient(s, 0, 164, 116, 4);
  return s.image();
};
