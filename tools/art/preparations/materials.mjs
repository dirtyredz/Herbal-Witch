// Preparation material sprites: for each of the 14 herbs a dried bundle (stems up, tied with a hot
// pink band, heads hanging down: it dries upside down) and a ground powder in a container that
// varies by group (bowl, paper twist, glass vial, cloth pouch, stoppered jar). 16 x 16, alpha 0/255.
// Also exports the small drawing kit the product sprites share (preparations/products.mjs).
import { image, rng } from '../pixels.mjs';
import { R, hex, N, canvas, set, line, leaf, blob, bloom, berry, bell } from '../botany/sprites.mjs';

const ramp = (a, b, c) => [hex(a), hex(b), hex(c)];

/** Shared palette: the owner's accents (hot pink, deep green, dark purple) plus container materials. */
export const P = {
  pink: ramp('#a82a5c', '#d8407a', '#ee7aa4'),
  green: ramp('#1f3f1e', '#2f5a2c', '#4a8040'),
  purple: ramp('#2f1a3a', '#4a2a55', '#6b4a7a'),
  paper: ramp('#b9a47c', '#d8c89c', '#efe4c6'),
  kraft: ramp('#8a6a44', '#b08d5c', '#d2b27c'),
  glass: ramp('#6f969c', '#a9ccd0', '#e0f0ee'),
  cork: ramp('#7a5530', '#a47a46', '#c79a5e'),
  burlap: ramp('#8f7440', '#b99a5c', '#d5ba7c'),
  amber: ramp('#a8601a', '#d88a22', '#f2b650'),
  cream: R.cream,
  wood: R.wood,
  twine: R.twine,
};

export function rect(cv, x0, y0, x1, y1, c, shaded = true) {
  for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) set(cv, x, y, c, shaded);
}

/** Fills rows given as [y, x0, x1, colour] spans. */
export function spans(cv, rows, shaded = true) {
  for (const [y, x0, x1, c] of rows) for (let x = x0; x <= x1; x++) set(cv, x, y, c, shaded);
}

/** Like the herb sprites' finish(), but darkens every open edge of a shaded shape, all four sides. */
export function finishOutlined(cv, edge = 0.6) {
  const at = (x, y) => (x < 0 || y < 0 || x >= N || y >= N ? null : cv.px[y * N + x]);
  return image(N, (x, y) => {
    const c = cv.px[y * N + x];
    if (!c) return [0, 0, 0, 0];
    let [r, g, b] = c;
    if (cv.shaded[y * N + x] && [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([dx, dy]) => at(x + dx, y + dy) === null)) {
      [r, g, b] = [r * edge, g * edge, b * edge].map(Math.round);
    }
    return [r, g, b, 255];
  });
}

/** A 16 x 16 outlined sprite from a draw function. */
export const item = (draw) => () => {
  const cv = canvas();
  draw(cv);
  return finishOutlined(cv);
};

// ---------------------------------------------------------------- bundles

/** Stems gathered at the top. */
function gather(cv, stem) {
  for (let y = 0; y <= 4; y++) {
    set(cv, 7, y, stem[y % 2 ? 0 : 1]);
    set(cv, 8, y, stem[y % 2 ? 1 : 2]);
  }
  set(cv, 6, 1, stem[0]);
  set(cv, 9, 2, stem[0]);
}
/** The hot pink tie, with two short loose ends. */
function tie(cv) {
  rect(cv, 6, 5, 9, 5, P.pink[2], false);
  rect(cv, 6, 6, 9, 6, P.pink[1], false);
  set(cv, 5, 6, P.pink[0]);
  set(cv, 10, 6, P.pink[0]);
  set(cv, 5, 7, P.pink[0]);
  set(cv, 10, 7, P.pink[0]);
}
const fan = (cv, tips, c) => tips.forEach(([x, y]) => line(cv, x < 8 ? 7 : 8, 7, x, y, c));

const bundle = (stem, draw) => item((cv) => {
  gather(cv, stem);
  draw(cv);
  tie(cv);
});

/** Violet buds along the last n pixels of the hanging stem from (x1, y1) to (x2, y2). */
const buds = (cv, x1, y1, x2, y2, n = 6) => {
  const len = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
  for (let i = 0; i < n; i++) {
    const t = (len - i) / len;
    const px = Math.round(x1 + (x2 - x1) * t);
    const py = Math.round(y1 + (y2 - y1) * t);
    set(cv, px, py, i % 2 ? R.violet[1] : R.violet[2]);
    if (i % 2 === 0) set(cv, px + (x2 < 8 ? -1 : 1), py, R.violet[0]);
  }
};

const bundles = {
  lavender: bundle(R.grey, (cv) => {
    const tips = [[3, 13], [6, 14], [9, 14], [12, 13]];
    fan(cv, tips, R.grey[1]);
    tips.forEach(([x, y]) => buds(cv, x < 8 ? 7 : 8, 7, x, y, 6));
  }),
  sage: bundle(R.sage, (cv) => {
    const tips = [[3, 12], [7, 14], [12, 12]];
    fan(cv, tips, R.sage[0]);
    for (const [x, y] of tips) blob(cv, x, y, 2.2, 1.5, R.sage);
    blob(cv, 5, 9, 1.8, 1.2, R.sage);
    blob(cv, 11, 9, 1.8, 1.2, R.sage);
  }),
  chamomile: bundle(R.moss, (cv) => {
    const tips = [[3, 12], [6, 13], [10, 13], [13, 12]];
    fan(cv, tips, R.moss[1]);
    tips.forEach(([x, y]) => bloom(cv, x, y + 1, R.cream[2], R.yellow[1], true));
  }),
  yarrow: bundle(R.fern, (cv) => {
    const tips = [[4, 11], [7, 12], [10, 12], [13, 11]];
    fan(cv, tips, R.fern[0]);
    for (const [x, y] of tips) {
      for (let dx = -1; dx <= 1; dx++) set(cv, x + dx, y + 1, R.cream[1]);
      for (let dx = -1; dx <= 1; dx++) set(cv, x + dx, y + 2, dx === 0 ? R.cream[2] : R.cream[0]);
    }
  }),
  rosemary: bundle(R.needle, (cv) => {
    const tips = [[3, 14], [6, 15], [10, 15], [13, 14]];
    fan(cv, tips, R.wood[1]);
    for (const [x, y] of tips) {
      for (let i = 0; i < 6; i += 2) {
        set(cv, x - 1, y - i, R.needle[2]);
        set(cv, x + 1, y - i - 1, R.needle[1]);
      }
      set(cv, x, y, R.blue[1]);
      set(cv, x, y - 3, R.blue[2]);
    }
  }),
  thyme: bundle(R.moss, (cv) => {
    const tips = [[4, 10], [6, 12], [9, 12], [12, 10]];
    fan(cv, tips, R.wood[1]);
    for (const [x, y] of tips) {
      blob(cv, x, y + 1, 1.6, 1.6, R.moss);
      set(cv, x + 1, y + 2, R.rose[1]);
      set(cv, x - 1, y, R.rose[2]);
    }
    blob(cv, 8, 9, 1.4, 1.4, R.moss);
  }),
  mint: bundle(R.mint, (cv) => {
    fan(cv, [[3, 12], [7, 14], [12, 12]], R.mint[0]);
    leaf(cv, 3, 11, 250, 4, 1.4, R.mint);
    leaf(cv, 7, 12, 270, 4, 1.5, R.mint);
    leaf(cv, 12, 11, 290, 4, 1.4, R.mint);
    leaf(cv, 5, 8, 215, 3.5, 1.2, R.mint);
    leaf(cv, 11, 8, 325, 3.5, 1.2, R.mint);
  }),
  nettle: bundle(R.dark, (cv) => {
    fan(cv, [[3, 12], [7, 14], [12, 12]], R.dark[1]);
    leaf(cv, 3, 11, 250, 4, 1.3, R.dark, 0.5);
    leaf(cv, 7, 12, 270, 4, 1.4, R.dark, 0.5);
    leaf(cv, 12, 11, 290, 4, 1.3, R.dark, 0.5);
    leaf(cv, 5, 8, 215, 3.5, 1.2, R.dark, 0.5);
    leaf(cv, 11, 8, 325, 3.5, 1.2, R.dark, 0.5);
    set(cv, 7, 12, R.moss[2]);
    set(cv, 3, 10, R.moss[2]);
    set(cv, 12, 10, R.moss[2]);
  }),
  valerian: bundle(R.moss, (cv) => {
    const tips = [[3, 11], [7, 12], [12, 11]];
    fan(cv, tips, R.moss[0]);
    leaf(cv, 5, 8, 215, 3, 1, R.moss);
    leaf(cv, 10, 8, 325, 3, 1, R.moss);
    for (const [x, y] of tips) {
      set(cv, x, y + 1, R.rose[2]);
      set(cv, x - 1, y + 2, R.pink[2]);
      set(cv, x + 1, y + 2, R.pink[1]);
      set(cv, x, y + 3, R.rose[1]);
      set(cv, x - 1, y + 3, R.pink[1]);
      set(cv, x + 1, y + 3, R.rose[0]);
    }
  }),
  mugwort: bundle(R.fern, (cv) => {
    const tips = [[3, 12], [6, 14], [10, 14], [13, 12]];
    fan(cv, tips, R.moss[1]);
    for (const [x, y] of tips) leaf(cv, x, y - 3, 270, 5, 1.1, R.silver, 0.5);
    set(cv, 8, 9, R.silver[2]);
  }),
  belladonna: bundle(R.deep, (cv) => {
    const tips = [[3, 11], [7, 13], [12, 11]];
    fan(cv, tips, R.wood[1]);
    leaf(cv, 5, 8, 215, 3, 1.1, R.deep);
    leaf(cv, 10, 8, 325, 3, 1.1, R.deep);
    berry(cv, 2, 11, R.black, R.black[2]);
    berry(cv, 11, 12, R.black, R.black[2]);
    bell(cv, 7, 13, R.plum);
    set(cv, 4, 10, R.plum[1]);
    set(cv, 10, 10, R.plum[2]);
  }),
  nightshade: bundle(R.deep, (cv) => {
    const tips = [[3, 11], [7, 13], [12, 11]];
    fan(cv, tips, R.deep[2]);
    leaf(cv, 5, 8, 215, 3, 1.1, R.deep);
    leaf(cv, 10, 8, 325, 3, 1.1, R.deep);
    berry(cv, 2, 11, R.red, R.red[2]);
    berry(cv, 11, 12, R.red, R.red[2]);
    berry(cv, 6, 13, R.red, R.red[2]);
    set(cv, 4, 9, R.violet[2]);
    set(cv, 5, 9, R.violet[1]);
    set(cv, 4, 10, R.yellow[1]);
    set(cv, 10, 10, R.violet[2]);
  }),
  wolfsbane: bundle(R.deep, (cv) => {
    const tips = [[3, 11], [7, 13], [12, 11]];
    fan(cv, tips, R.deep[2]);
    leaf(cv, 5, 8, 215, 3, 1.1, R.deep);
    leaf(cv, 10, 8, 325, 3, 1.1, R.deep);
    for (const [x, y] of tips) {
      // a hooded monkshood bell hanging from the tip
      set(cv, x - 1, y + 1, R.blue[0]);
      set(cv, x, y + 1, R.blue[2]);
      set(cv, x + 1, y + 1, R.blue[1]);
      set(cv, x - 1, y + 2, R.blue[1]);
      set(cv, x, y + 2, R.blue[1]);
      set(cv, x + 1, y + 2, R.blue[0]);
      set(cv, x, y + 3, R.blue[0]);
    }
  }),
  // Mandrake: a leaf tuft up top, tied; the forked root hangs below.
  mandrake: item((cv) => {
    leaf(cv, 7, 5, 100, 5, 1.2, R.moss);
    leaf(cv, 8, 5, 70, 5, 1.2, R.moss);
    leaf(cv, 7, 5, 140, 4, 1, R.deep);
    leaf(cv, 8, 5, 40, 4, 1, R.deep);
    tie(cv);
    rect(cv, 6, 7, 9, 11, R.root[1]);
    rect(cv, 6, 7, 6, 11, R.root[2]);
    rect(cv, 9, 8, 9, 11, R.root[0]);
    rect(cv, 5, 12, 6, 14, R.root[1]);
    rect(cv, 9, 12, 10, 14, R.root[0]);
    set(cv, 5, 15, R.root[0]);
    set(cv, 10, 15, R.root[0]);
    set(cv, 7, 12, R.root[0]);
    set(cv, 8, 12, R.root[0]);
    set(cv, 7, 8, R.wood[1], false); // eyes
    set(cv, 8, 8, R.wood[1], false);
    line(cv, 5, 9, 3, 10, R.root[0]); // arm rootlets
    line(cv, 10, 9, 12, 10, R.root[0]);
    set(cv, 3, 11, R.root[0]);
    set(cv, 12, 11, R.root[0]);
  }),
};

// ---------------------------------------------------------------- powders

const POWDER = {
  lavender: [ramp('#6b4a9a', '#8a66c0', '#b79ae0'), '#e8d8f8'],
  sage: [ramp('#6f8872', '#98b09c', '#c6d6c8'), '#e4eee4'],
  chamomile: [ramp('#c9992a', '#e8bd3e', '#f6d86a'), '#fbf5e4'],
  yarrow: [ramp('#c9b8b8', '#e5d6d0', '#f6ece6'), '#74986a'],
  rosemary: [ramp('#2a4431', '#3e6244', '#5c8657'), '#7a98d8'],
  thyme: [ramp('#6b6a38', '#8a8a48', '#a8aa62'), '#d4859c'],
  mint: [ramp('#3f7a32', '#62a845', '#97d065'), '#e6f6d8'],
  nettle: [ramp('#1c3320', '#2d4a2e', '#436a3f'), '#8db55f'],
  valerian: [ramp('#a58a5c', '#c9b07e', '#e6d4a6'), '#d4859c'],
  mugwort: [ramp('#7f8f88', '#a6b5ad', '#cfdad4'), '#4e7048'],
  belladonna: [ramp('#14101a', '#2a2232', '#4a3a58'), '#8f5f9e'],
  nightshade: [ramp('#6a1f3a', '#8c2a48', '#a8405a'), '#f6d86a'],
  wolfsbane: [ramp('#3f5390', '#5f78b8', '#8fa4d8'), '#dfe8ff'],
  mandrake: [ramp('#b89a6a', '#d6bc8a', '#efdcb0'), '#6b4a2f'],
};

/** Ground-herb texture: grains of the herb's ramp with a few contrasting flecks, fixed by position. */
function powderColour(herb, x, y) {
  const [r, speck] = POWDER[herb];
  const rand = rng(31000 + herb.length * 97 + x * 13 + y * 29 + herb.charCodeAt(0) * 7);
  const n = rand();
  if (n < 0.12) return hex(speck);
  if (n < 0.4) return r[2];
  if (n > 0.78) return r[0];
  return r[1];
}
const fill = (cv, herb, cells) => cells.forEach(([x, y]) => set(cv, x, y, powderColour(herb, x, y), false));
const cellsOf = (rows) => rows.flatMap(([y, x0, x1]) => Array.from({ length: x1 - x0 + 1 }, (_, i) => [x0 + i, y]));

const containers = {
  // temperate: a small cream ceramic bowl with a hot pink band, powder heaped above the rim
  bowl(cv, herb) {
    spans(cv, [
      [8, 3, 12, P.cream[2]],
      [9, 3, 12, P.pink[1]],
      [10, 4, 11, P.cream[1]],
      [11, 4, 11, P.cream[1]],
      [12, 5, 10, P.cream[0]],
      [13, 6, 9, P.cream[0]],
    ]);
    set(cv, 4, 10, P.cream[2]);
    set(cv, 5, 11, P.cream[2]);
    fill(cv, herb, cellsOf([[5, 6, 9], [6, 5, 10], [7, 4, 11]]));
  },
  // dry: a paper twist, ears at the top, powder spilled beside it
  twist(cv, herb) {
    spans(cv, [
      [4, 4, 5, P.paper[2]],
      [4, 10, 11, P.paper[1]],
      [5, 5, 6, P.paper[2]],
      [5, 9, 10, P.paper[1]],
      [6, 6, 9, P.paper[1]],
      [7, 6, 9, P.paper[0]],
      [8, 5, 10, P.paper[2]],
      [9, 4, 10, P.paper[1]],
      [10, 4, 10, P.paper[1]],
      [11, 4, 10, P.paper[1]],
      [12, 4, 10, P.paper[0]],
      [13, 5, 9, P.paper[0]],
    ]);
    rect(cv, 4, 10, 10, 10, P.twine[1], false); // twine
    set(cv, 11, 10, P.twine[0]);
    fill(cv, herb, cellsOf([[11, 12, 13], [12, 11, 14], [13, 10, 14], [14, 9, 15]]));
  },
  // wet: a glass vial with a cork, powder filling the lower part
  vial(cv, herb) {
    rect(cv, 6, 1, 9, 2, P.cork[1]);
    rect(cv, 6, 1, 9, 1, P.cork[2]);
    spans(cv, [
      [3, 6, 9, P.glass[2]],
      ...Array.from({ length: 10 }, (_, i) => [4 + i, 5, 10, P.glass[1]]),
      [14, 6, 9, P.glass[0]],
    ]);
    set(cv, 6, 5, P.glass[2], false);
    set(cv, 6, 6, P.glass[2], false);
    fill(cv, herb, cellsOf([[7, 6, 9], [8, 6, 9], [9, 6, 9], [10, 6, 9], [11, 6, 9], [12, 6, 9], [13, 6, 9]]));
  },
  // shade: a deep green cloth pouch cinched with hot pink string, powder peeking from the top
  pouch(cv, herb) {
    fill(cv, herb, cellsOf([[1, 7, 8], [2, 6, 9], [3, 5, 10]]));
    spans(cv, [
      [4, 5, 10, P.green[2]],
      [5, 6, 9, P.pink[1]],
      [6, 5, 10, P.green[1]],
      [7, 4, 11, P.green[1]],
      [8, 3, 12, P.green[1]],
      [9, 3, 12, P.green[1]],
      [10, 3, 12, P.green[1]],
      [11, 3, 12, P.green[0]],
      [12, 3, 12, P.green[0]],
      [13, 4, 11, P.green[0]],
      [14, 5, 10, P.green[0]],
    ]);
    set(cv, 10, 5, P.pink[2]);
    set(cv, 11, 5, P.pink[1]);
    set(cv, 11, 6, P.pink[0]);
    set(cv, 5, 8, P.green[2]);
    set(cv, 4, 9, P.green[2]);
    rect(cv, 8, 10, 10, 12, P.green[2], false); // a patched square
    set(cv, 9, 11, P.green[0], false);
    fill(cv, herb, cellsOf([[13, 13, 14], [14, 11, 15]]));
  },
  // woodland: a dark purple stoppered jar with a window onto the powder
  jar(cv, herb) {
    rect(cv, 6, 2, 9, 4, P.cork[1]);
    rect(cv, 6, 2, 9, 2, P.cork[2]);
    spans(cv, [
      [4, 5, 10, P.cork[0]],
      [5, 4, 11, P.purple[2]],
      ...Array.from({ length: 5 }, (_, i) => [6 + i, 3, 12, P.purple[1]]),
      [11, 3, 12, P.purple[1]],
      [12, 3, 12, P.purple[0]],
      [13, 3, 12, P.purple[0]],
      [14, 4, 11, P.purple[0]],
    ]);
    rect(cv, 5, 7, 10, 12, P.glass[0], false);
    fill(cv, herb, cellsOf([[8, 6, 10], [9, 5, 10], [10, 5, 10], [11, 5, 10], [12, 5, 10], [7, 6, 9]]));
    set(cv, 5, 7, P.glass[2], false);
    set(cv, 5, 8, P.glass[2], false);
  },
};

const CONTAINER_OF = { temperate: 'bowl', dry: 'twist', wet: 'vial', shade: 'pouch', woodland: 'jar' };

/** [group, herb] in manifest order. */
export const PREPARATION_HERBS = [
  ['temperate', 'lavender'], ['temperate', 'sage'], ['temperate', 'chamomile'], ['temperate', 'yarrow'],
  ['dry', 'rosemary'], ['dry', 'thyme'], ['wet', 'mint'], ['wet', 'nettle'],
  ['shade', 'valerian'], ['shade', 'mugwort'],
  ['woodland', 'belladonna'], ['woodland', 'nightshade'], ['woodland', 'wolfsbane'], ['woodland', 'mandrake'],
];

/** The bundle sprite function of a herb: () => 16 x 16 image. */
export const bundleSprite = (herb) => bundles[herb];
/** The powder sprite function of a herb in its group's container. */
export const powderSprite = (group, herb) => item((cv) => containers[CONTAINER_OF[group]](cv, herb));
