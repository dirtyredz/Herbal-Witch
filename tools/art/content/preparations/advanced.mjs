// Advanced preparation sprites (Milestone 7): the player drinks, throwing vials, oil phials, the blinding
// dust and moon essence, the familiar tonics / essences / oil, and the four-dose cauldron mixtures.
// 16 x 16, alpha 0/255, outlined, drawn with the kit in preparations/materials.mjs. Each family has its
// own silhouette so a stack reads at a glance before its colour does:
//   potion  round-bellied corked bottle, cream label with an ink glyph   (player drinks)
//   vial    small glass globe, short neck wrapped in twine, a fuse tail  (throwables)
//   phial   tall bottle sealed with plum wax, a dark label               (oils)
//   slim    narrow gold-capped vial, a mark glowing in the liquid        (essences)
//   gourd   two-lobed stoppered gourd with a paw print                   (familiar tonics)
//   crock   big round flask, wide cork, four tally marks on its label    (cauldron mixtures)
// Exports ADVANCED as [[path relative to textures/item/preparations/, maker]].
import { hex, set } from '../botany/sprites.mjs';
import { P, rect, spans, item } from './materials.mjs';

const ramp = (a, b, c) => [hex(a), hex(b), hex(c)];
const INK = hex('#3a2a20');

/** Draws glyph rows at (x0, y0): '.' is skipped, every other char looks up its colour in `key`. */
function glyph(cv, x0, y0, rows, key) {
  rows.forEach((row, dy) => [...row].forEach((ch, dx) => {
    if (ch !== '.' && key[ch]) set(cv, x0 + dx, y0 + dy, key[ch], false);
  }));
}

/** A glyph centred on a box (x0..x1, y0..y1). */
function centred(cv, [x0, y0, x1, y1], rows, key) {
  const w = Math.max(...rows.map((r) => r.length));
  glyph(cv, x0 + Math.floor((x1 - x0 + 1 - w) / 2), y0 + Math.floor((y1 - y0 + 1 - rows.length) / 2), rows, key);
}

// Each shape: glass body rows [y, x0, x1], cork/cap rows [y, x0, x1, colour], the first liquid row.
const SHAPES = {
  potion: {
    cork: [[1, 7, 8, P.cork[2]], [2, 6, 9, P.cork[1]]],
    body: [[3, 7, 8], [4, 7, 8], [5, 6, 9], [6, 4, 11], ...Array.from({ length: 7 }, (_, i) => [7 + i, 3, 12]), [14, 5, 10]],
    level: 6,
    label: [4, 9, 11, 13],
  },
  vial: {
    cork: [[3, 7, 8, P.cork[2]], [4, 6, 9, P.cork[1]]],
    body: [[5, 7, 8], [6, 7, 8], [7, 5, 10], [8, 4, 11], ...Array.from({ length: 5 }, (_, i) => [9 + i, 3, 12]), [14, 5, 10]],
    level: 9,
  },
  phial: {
    cork: [[1, 7, 8, P.cork[2]], [2, 6, 9, P.purple[2]]],
    body: [[3, 7, 8], [4, 7, 8], [5, 6, 9], ...Array.from({ length: 9 }, (_, i) => [6 + i, 5, 10])],
    level: 7,
    label: [6, 8, 9, 12],
  },
  slim: {
    cork: [[1, 7, 8, P.cork[2]], [2, 6, 9, P.amber[2]], [3, 6, 9, P.amber[1]]],
    body: [...Array.from({ length: 10 }, (_, i) => [4 + i, 5, 10]), [14, 6, 9]],
    level: 6,
  },
  gourd: {
    cork: [[1, 7, 8, P.cork[2]], [2, 6, 9, P.cork[1]]],
    body: [[3, 7, 8], [4, 6, 9], [5, 5, 10], [6, 5, 10], [7, 7, 8], [8, 7, 8], [9, 4, 11], [10, 3, 12], ...Array.from({ length: 3 }, (_, i) => [11 + i, 2, 13]), [14, 3, 12], [15, 5, 10]],
    level: 7,
  },
  crock: {
    cork: [[1, 6, 9, P.cork[2]]],
    trim: [[2, 5, 10]],
    body: [[3, 6, 9], [4, 5, 10], [5, 4, 11], [6, 3, 12], ...Array.from({ length: 6 }, (_, i) => [7 + i, 2, 13]), [13, 3, 12], [14, 4, 11]],
    level: 6,
    label: [4, 10, 12, 13],
  },
};

/** The glass body: empty glass above the level, the liquid ramp below it, a highlight down the left. */
function bottle(cv, shapeName, liq, { clear = false, trim = null } = {}) {
  const s = SHAPES[shapeName];
  const last = s.body.at(-1)[0];
  for (const [y, x0, x1] of s.body) {
    for (let x = x0; x <= x1; x++) {
      if (y < s.level) set(cv, x, y, clear ? P.glass[2] : P.glass[1], true);
      else set(cv, x, y, y === s.level ? liq[2] : y >= last - 1 ? liq[0] : liq[1], true);
    }
  }
  for (const [y, x0] of s.body) if (y > s.level && y < last) set(cv, x0 + 1, y, liq[2], true);
  for (const [y, x0, x1, c] of s.cork) rect(cv, x0, y, x1, y, c);
  for (const [y, x0, x1] of s.trim ?? []) rect(cv, x0, y, x1, y, trim ?? P.cork[1]);
  return s;
}

/** A label box on a shape: paper ramp, bottom row darker, then the glyph centred inside. */
function label(cv, s, bg, rows, key) {
  const [x0, y0, x1, y1] = s.label;
  rect(cv, x0, y0, x1, y1, bg[1], false);
  rect(cv, x0, y1, x1, y1, bg[0], false);
  centred(cv, [x0, y0, x1, y1 - 1], rows, key);
}

// ---------------------------------------------------------------- shared colour tables

const LIQ = {
  night_vision: ramp('#1e2f7a', '#2f4aa8', '#6a86e0'),
  water_breathing: ramp('#1f7d86', '#2fb0b4', '#78e0d8'),
  resistance: ramp('#5a6068', '#8a929c', '#c4cad2'),
  healing: ramp('#b02a48', '#e04868', '#f890a8'),
  poison: ramp('#2c6a18', '#4aa826', '#86d84a'),
  concealment: ramp('#4a4a54', '#74747e', '#a4a4ae'),
  slowing: ramp('#6f8394', '#98aebe', '#c8dae6'),
  binding: ramp('#5a5a1e', '#808030', '#a8a850'),
  weakening: ramp('#5a4a66', '#80708c', '#a898b4'),
  sleep: ramp('#8a70c0', '#b49ae0', '#dcc8f8'),
  guardian: ramp('#5f6e5f', '#8a9c88', '#b8c8b4'),
  swiftpaw: ramp('#2f9a62', '#52d08a', '#9af0b8'),
  nightwatch: ramp('#c05a10', '#ee8a1c', '#ffc060'),
  moonveil: ramp('#26277a', '#3f3fb0', '#7878e8'),
  spiritbond: ramp('#7a2a9a', '#a850d0', '#d8a0f0'),
  hearthkeeper: ramp('#a01c18', '#d83a24', '#ff7e50'),
  shadowstep: ramp('#150a24', '#2c1648', '#5a3a8a'),
};

// ---------------------------------------------------------------- player drinks (potion)

const drinkOf = (liq, rows, key) => item((cv) => {
  const s = bottle(cv, 'potion', liq);
  label(cv, s, P.paper, rows, key);
});

const drinks = {
  // deep blue, a watching eye on the label
  night_vision: drinkOf(LIQ.night_vision, ['.##.', '#oo#', '.##.'], { '#': hex('#1b2a6a'), o: hex('#3ac0ee') }),
  // sea teal, a bubble ring with a glint
  water_breathing: drinkOf(LIQ.water_breathing, ['.##.', '#o.#', '.##.'], { '#': hex('#0f5a62'), o: hex('#ffffff') }),
  // iron grey, a shield
  resistance: drinkOf(LIQ.resistance, ['####', '#oo#', '.##.'], { '#': hex('#363c46'), o: hex('#9aa4b0') }),
  // rose red, a pink heart
  healing: drinkOf(LIQ.healing, ['##.##', '#####', '.###.', '..#..'], { '#': hex('#d02a58') }),
};

// ---------------------------------------------------------------- throwing vials (vial)

const FUSE = ramp('#3a2a20', '#b08d5c', '#d2b27c');

/** A small glass globe with a twine-wrapped neck, a loose fuse tail and a spark. */
const vialOf = (liq, rows, key) => item((cv) => {
  bottle(cv, 'vial', liq);
  rect(cv, 6, 5, 9, 5, FUSE[2], false); // twine wrap round the neck
  rect(cv, 6, 6, 9, 6, FUSE[1], false);
  set(cv, 10, 5, FUSE[1]); // the fuse tail
  set(cv, 11, 4, FUSE[2]);
  set(cv, 11, 3, FUSE[1]);
  set(cv, 12, 2, hex('#f6d03a'));
  set(cv, 13, 1, hex('#ee7a24'));
  set(cv, 12, 1, hex('#fff2a8'));
  glyph(cv, 6, 10, rows, key);
});

const throwables = {
  poison: vialOf(LIQ.poison, ['.##.', '#oo#', '.##.', '.#.#'], { '#': hex('#e6efc4'), o: hex('#1a3a10') }),
  concealment: vialOf(LIQ.concealment, ['..##', '.#..', '#.#.', '.#..'], { '#': hex('#dcdce6') }),
  slowing: vialOf(LIQ.slowing, ['####', '.##.', '.##.', '####'], { '#': hex('#33465a') }),
  binding: vialOf(LIQ.binding, ['.##.', '#..#', '#..#', '.##.'], { '#': hex('#e8cc88') }),
  weakening: vialOf(LIQ.weakening, ['..#..', '..#..', '#.#.#', '.###.', '..#..'], { '#': hex('#2a1f34') }),
  sleep: vialOf(LIQ.sleep, ['.##.', '#...', '#...', '.##.'], { '#': hex('#fff6c8') }),
};

// ---------------------------------------------------------------- oil phials (phial)

const oilOf = (liq, bg, rows, key) => item((cv) => {
  const s = bottle(cv, 'phial', liq);
  set(cv, 9, 3, P.purple[2]); // a drip of wax down the neck
  label(cv, s, bg, rows, key);
});

const oils = {
  venom: oilOf(ramp('#4a7a10', '#7aae1c', '#b0dc3a'), P.paper, ['..#.', '.##.', '####', '.##.'], { '#': hex('#2c4a08') }),
  thorn: oilOf(ramp('#5a1420', '#8a2030', '#b8404c'), P.paper, ['.#..', '##.#', '.#..', '.###'], { '#': hex('#4a0e1a') }),
  undead_repellent: oilOf(ramp('#bfb59a', '#e0d8c0', '#f6f0e0'), P.purple, ['####', '#..#', '####', '.#.#'], { '#': hex('#efe8d0') }),
  arthropod_repellent: oilOf(ramp('#a8601a', '#d88a22', '#f2b650'), P.green, ['#..#', '.##.', '.##.', '#..#'], { '#': hex('#d8f0a0') }),
};

// ---------------------------------------------------------------- dusts

const dusts = {
  // a plum paper twist tied with pink twine, glittering pale dust in the belly and sparkles about it
  blinding: item((cv) => {
    spans(cv, [
      [2, 5, 6, P.purple[2]], [2, 9, 10, P.purple[1]],
      [3, 6, 9, P.purple[2]],
      [4, 7, 8, P.purple[1]],
      [6, 5, 10, P.purple[2]],
      [7, 4, 11, P.purple[1]],
      ...Array.from({ length: 4 }, (_, i) => [8 + i, 3, 12, P.purple[1]]),
      [12, 4, 11, P.purple[0]],
      [13, 5, 10, P.purple[0]],
    ]);
    rect(cv, 6, 5, 9, 5, P.pink[1], false); // the tie
    set(cv, 10, 5, P.pink[2]);
    set(cv, 5, 6, P.pink[0]);
    const white = hex('#ffffff');
    const ice = hex('#cfe8ff');
    const gold = hex('#fff3b0');
    for (const [x, y, c] of [[5, 8, white], [7, 9, ice], [9, 8, white], [6, 10, gold], [10, 10, ice], [8, 11, white], [11, 8, ice], [5, 12, gold], [9, 12, ice]]) set(cv, x, y, c, false);
    for (const [cx, cy] of [[2, 3], [13, 9], [1, 12]]) {
      for (const [dx, dy] of [[0, 0], [1, 0], [-1, 0], [0, 1], [0, -1]]) set(cv, cx + dx, cy + dy, dx === 0 && dy === 0 ? white : gold);
    }
  }),
};

// ---------------------------------------------------------------- essences (slim)

const essenceOf = (liq, rows, key) => item((cv) => {
  bottle(cv, 'slim', liq);
  glyph(cv, 6, 8, rows, key);
});

const MOON = ['.##.', '#...', '#...', '#...', '.##.'];
const LEAF = ['..##', '.#oo', '#oo.', 'o#..'];

const essences = {
  moon: essenceOf(ramp('#9a90b8', '#c4bad8', '#ece6f8'), MOON, { '#': hex('#5a4a88') }),
};

// ---------------------------------------------------------------- familiar tonics (gourd)

const PAW = ['.#..#.', '#....#', '.####.', '.####.', '..##..'];

/** A stoppered two-lobed gourd, twine at the waist, a paw print on the belly. */
const tonicOf = (liq, ink) => item((cv) => {
  bottle(cv, 'gourd', liq);
  rect(cv, 6, 8, 9, 8, FUSE[1], false); // waist twine
  set(cv, 6, 8, FUSE[2], false);
  set(cv, 10, 9, FUSE[0]);
  centred(cv, [3, 10, 12, 14], PAW, { '#': ink });
});

const CREAM = hex('#fbf5e4');
const DARK = hex('#2a1f2c');
const tonics = {
  guardian: tonicOf(LIQ.guardian, DARK),
  swiftpaw: tonicOf(LIQ.swiftpaw, DARK),
  nightwatch: tonicOf(LIQ.nightwatch, DARK),
  moonveil: tonicOf(LIQ.moonveil, CREAM),
  spiritbond: tonicOf(LIQ.spiritbond, CREAM),
  hearthkeeper: tonicOf(LIQ.hearthkeeper, CREAM),
  shadowstep: tonicOf(LIQ.shadowstep, hex('#b8a0e8')),
};

const familiarEssences = {
  // pale quartz, a sage leaf
  warding: essenceOf(ramp('#c0b4b8', '#e6dcdc', '#fff8f6'), LEAF, { '#': hex('#4e7048'), o: hex('#74986a') }),
  // dark green, a pale venom leaf
  venomleaf: essenceOf(ramp('#142a18', '#1f4a22', '#3a7a36'), LEAF, { '#': hex('#7cc060'), o: hex('#b4e890') }),
};

const familiarOils = {
  // ember orange, a flame on a dark label
  emberguard: oilOf(ramp('#b8300e', '#ee5a1a', '#ffa040'), P.purple, ['..#.', '.##.', '####', '.##.'], { '#': hex('#ffd84a') }),
};

// ---------------------------------------------------------------- four-dose mixtures (crock)

const TRIM = { utility: P.green[2], combat: P.pink[1], familiar: P.purple[2] };

/** A big round flask, wide cork, a coloured wax band by category and four tally marks. */
const mixtureOf = (liq, category) => item((cv) => {
  const s = bottle(cv, 'crock', liq, { trim: TRIM[category] });
  const [x0, y0, x1, y1] = s.label;
  rect(cv, x0, y0, x1, y1, P.paper[1], false);
  rect(cv, x0, y1, x1, y1, P.paper[0], false);
  for (const x of [x0 + 1, x0 + 3, x0 + 5, x0 + 7]) {
    set(cv, x, y0 + 1, INK, false);
    set(cv, x, y0 + 2, INK, false);
  }
});

const mixtures = {
  utility: ['night_vision', 'water_breathing', 'resistance', 'healing'],
  combat: ['poison', 'concealment', 'slowing', 'binding', 'weakening', 'sleep'],
  familiar: ['guardian', 'swiftpaw', 'nightwatch', 'moonveil', 'spiritbond', 'hearthkeeper', 'shadowstep'],
};

const group = (dir, makers) => Object.entries(makers).map(([k, f]) => [`${dir}/${k}.png`, f]);

/** [path relative to textures/item/preparations, sprite function] for the advanced preparation sprites. */
export const ADVANCED = [
  ...group('advanced/drinks', drinks),
  ...group('advanced/throwables', throwables),
  ...group('advanced/oils', oils),
  ...group('advanced/dusts', dusts),
  ...group('advanced/essences', essences),
  ...group('familiar/tonics', tonics),
  ...group('familiar/essences', familiarEssences),
  ...group('familiar/oils', familiarOils),
  ...Object.entries(mixtures).flatMap(([category, names]) => names.map((n) => [`mixtures/${category}/${n}.png`, mixtureOf(LIQ[n], category)])),
];
