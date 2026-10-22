// The grimoire reader GUI sheet: one 256 x 256 RGBA atlas in vanilla GUI units (the file is enlarged
// 2x by the catalogue). Java screens blit these regions by 256-unit coordinates; everything else is
// transparent.
//
//   (  0,   0) 252 x 180  open book spread: leather cover rim 5px wide all round (x/y 0..5 and the
//                         mirrored far edges), gold 3x3 corner studs at (1,1) (248,1) (1,176) (248,176),
//                         left page x 6..123, right page x 128..245, both y 6..173 (plain parchment
//                         with flecks and gutter shading, safe for text), gutter strip x 124..127.
//                         Ivy flourishes sit on the top rim only (y 0..5).
//   (  0, 184)  18 x 18   slot frame; the item sits at (1, 185)
//   ( 20, 184)   9 x 9    "known" marker: green check leaf
//   ( 30, 184)   9 x 9    "unknown" marker: purple question mark on dark parchment
//   ( 40, 184)   9 x 9    heat marker: small flame
//   ( 50, 184)   9 x 9    time marker: small hourglass
//   ( 60, 184)   9 x 9    "missing" marker: hot pink cross
//   ( 72, 184)  22 x 15   filled right-pointing arrow, green edge, pink fill
import { rng } from '../pixels.mjs';

const W = 256;
const hex = (s) => [1, 3, 5].map((i) => parseInt(s.slice(i, i + 2), 16));
const C = {
  edge: hex('#1c0f24'),
  plum: hex('#4a2a55'),
  plumHi: hex('#5c3668'),
  plumLo: hex('#3a1f44'),
  parch: hex('#d9c7a0'),
  parchHi: hex('#e8dab8'),
  parchLo: hex('#b8a47c'),
  parchDeep: hex('#a08c66'),
  slot: hex('#a8946f'),
  slotLo: hex('#4a3622'),
  slotHi: hex('#f2e8cf'),
  gold: hex('#e0b040'),
  goldHi: hex('#f6d86a'),
  goldLo: hex('#8a5e18'),
  pink: hex('#d8407a'),
  pinkHi: hex('#ee7aa4'),
  pinkLo: hex('#a82a5c'),
  green: hex('#2f5a2c'),
  greenHi: hex('#4a8040'),
  greenLo: hex('#1f3f1e'),
  purple: hex('#4a2a55'),
};

class Sheet {
  constructor() {
    this.px = Array.from({ length: W * W }, () => [0, 0, 0, 0]);
  }
  set(x, y, c) {
    if (x < 0 || y < 0 || x >= W || y >= W) return;
    this.px[y * W + x] = [c[0], c[1], c[2], 255];
  }
  rect(x0, y0, w, h, c) {
    for (let y = y0; y < y0 + h; y++) for (let x = x0; x < x0 + w; x++) this.set(x, y, c);
  }
  image() {
    return { size: W, px: this.px };
  }
}

const SPREAD_W = 252;
const SPREAD_H = 180;

/** Leather cover with grain and a dark outer edge; pages and gutter are laid over it. */
function cover(s) {
  const rand = rng(4417);
  s.rect(0, 0, SPREAD_W, SPREAD_H, C.edge);
  for (let y = 1; y < SPREAD_H - 1; y++) {
    for (let x = 1; x < SPREAD_W - 1; x++) {
      const t = rand();
      s.set(x, y, t < 0.2 ? C.plumLo : t > 0.88 ? C.plumHi : C.plum);
    }
  }
}

/** One parchment page; `dir` is +1 when the gutter is on its right. Shades darker toward the gutter. */
function page(s, x0, x1, rand, dir) {
  const y0 = 6;
  const y1 = 173;
  for (let y = y0; y <= y1; y++) {
    for (let x = x0; x <= x1; x++) {
      const d = dir > 0 ? x1 - x : x - x0; // distance from the gutter edge
      let c = C.parch;
      if (d < 3) c = C.parchDeep;
      else if (d < 6) c = (x + y) % 2 === 0 ? C.parchDeep : C.parchLo;
      else if (d < 12) c = (x + y) % 2 === 0 ? C.parchLo : C.parch;
      s.set(x, y, c);
    }
  }
  // outer page edge: one lit line against the cover (top and outside)
  for (let y = y0; y <= y1; y++) s.set(dir > 0 ? x0 : x1, y, C.parchHi);
  for (let x = x0; x <= x1; x++) s.set(x, y0, C.parchHi);
  // fibre flecks over the open part of the page
  const lo = dir > 0 ? x0 + 3 : x0 + 14;
  const hi = dir > 0 ? x1 - 14 : x1 - 3;
  for (let i = 0; i < 330; i++) {
    const x = lo + Math.floor(rand() * (hi - lo));
    const y = y0 + 2 + Math.floor(rand() * (y1 - y0 - 3));
    s.set(x, y, rand() < 0.5 ? C.parchHi : C.parchLo);
  }
  for (let x = x0; x <= x1; x++) s.set(x, y1, C.parchLo); // lower edge shadow
}

function gutter(s) {
  s.rect(124, 6, 4, 168, C.plumLo);
  for (let y = 6; y <= 173; y++) {
    s.set(125, y, C.plum);
    s.set(126, y, y % 4 === 0 ? C.plumHi : C.plum);
  }
}

function studs(s) {
  for (const [x, y] of [[1, 1], [248, 1], [1, 176], [248, 176]]) {
    s.rect(x, y, 3, 3, C.gold);
    s.set(x, y, C.goldHi);
    s.set(x + 2, y + 2, C.goldLo);
  }
}

/** A vine along the top rim (rows 1..4 only), starting at x0 and running dir (+1/-1) for len columns. */
function ivy(s, x0, dir, len) {
  for (let i = 0; i < len; i++) {
    const x = x0 + dir * i;
    const y = 2 + (Math.round(Math.sin(i * 0.5)) > 0 ? 1 : 0);
    s.set(x, y, C.green);
    if (i % 4 === 2) {
      s.set(x, y - 1, C.greenHi);
      s.set(x + dir, y - 1, C.greenHi);
      s.set(x + dir, y + 1, C.greenLo);
    }
  }
  const e = x0 + dir * len;
  s.set(e, 3, C.green);
  s.set(e + dir, 4, C.greenHi);
}

function spread(s) {
  const rand = rng(4418);
  cover(s);
  page(s, 6, 123, rand, 1);
  page(s, 128, 245, rand, -1);
  gutter(s);
  studs(s);
  ivy(s, 8, 1, 56);
  ivy(s, 243, -1, 56);
}

/** An 18 x 18 slot frame at (x0, y0): dark top/left, light bottom/right (the station slot style). */
function slot(s, x0, y0) {
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

/** Blits a character pattern; characters missing from the key are transparent. */
function stamp(s, x0, y0, rows, key) {
  rows.forEach((row, y) => [...row].forEach((ch, x) => key[ch] && s.set(x0 + x, y0 + y, key[ch])));
}

const KNOWN = [
  '.......oo',
  '......ogh',
  '.....ogh.',
  'o...ogh..',
  'ogo.ogh..',
  '.ogogh...',
  '..ogh....',
  '...o.....',
  '.........',
];
const UNKNOWN = [
  '.bbbbbbb.',
  'bdddddddb',
  'bddPPPddb',
  'bdPPdPPdb',
  'bddddPPdb',
  'bdddPPddb',
  'bdddddddb',
  'bdddPdddb',
  '.bbbbbbb.',
];
const FLAME = [
  '....o....',
  '...ofo...',
  '..offfo..',
  '..offfo..',
  '.offyffo.',
  '.offyyfo.',
  '.offyyfo.',
  '..offfo..',
  '...ooo...',
];
const HOURGLASS = [
  '.ggggggg.',
  '..ossso..',
  '...oso...',
  '....s....',
  '....o....',
  '...oao...',
  '..oaaao..',
  '.oaaaaao.',
  '.ggggggg.',
];

function markers(s) {
  stamp(s, 20, 184, KNOWN, { o: C.greenLo, g: C.green, h: C.greenHi });
  stamp(s, 30, 184, UNKNOWN, { b: C.parchDeep, d: C.parchLo, P: C.purple });
  stamp(s, 40, 184, FLAME, { o: hex('#8a2a10'), f: hex('#f08a1c'), y: hex('#ffd84a') });
  stamp(s, 50, 184, HOURGLASS, { g: C.gold, o: C.goldLo, s: hex('#f2e8cf'), a: hex('#d88a22') });
  // missing: a hot pink X with a dark edge, built from its own mask
  const inX = (x, y) => x >= 1 && x <= 7 && y >= 1 && y <= 7 && (Math.abs(x - y) <= 1 || Math.abs(x + y - 8) <= 1);
  for (let y = 0; y < 9; y++) {
    for (let x = 0; x < 9; x++) {
      if (inX(x, y)) s.set(60 + x, 184 + y, x === y || x + y === 8 ? C.pinkHi : C.pink);
      else if ([[1, 0], [-1, 0], [0, 1], [0, -1]].some(([dx, dy]) => inX(x + dx, y + dy))) s.set(60 + x, 184 + y, C.pinkLo);
    }
  }
}

/** The filled 22 x 15 right-pointing arrow at (72, 184): green edge, pink fill. */
function arrow(s) {
  const inside = (x, y) => (x <= 10 && y >= 4 && y <= 10) || (x >= 11 && x <= 21 && Math.abs(y - 7) <= Math.round(7 - ((x - 11) * 7) / 10));
  for (let y = 0; y < 15; y++) {
    for (let x = 0; x < 22; x++) {
      if (!inside(x, y)) continue;
      const edge = [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([dx, dy]) => !inside(x + dx, y + dy));
      s.set(72 + x, 184 + y, edge ? C.green : y === 5 || y === 9 ? C.pinkHi : C.pink);
    }
  }
}

/** The grimoire reader atlas, 256 x 256. */
export const reader = () => {
  const s = new Sheet();
  spread(s);
  slot(s, 0, 184);
  markers(s);
  arrow(s);
  return s.image();
};
