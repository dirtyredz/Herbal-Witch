// Preparation product sprites: 7 drinks, the salve, 2 oils, 6 garden preparations and 2 ingredients.
// 16 x 16, alpha 0/255, outlined, drawn with the kit in drawing/items.mjs. Bottles vary by
// silhouette (flask / tall / squat / conical) and carry a small hot pink or deep green label, so no
// two products differ by hue alone.
import { hex, set, line } from '../../drawing/sprites.mjs';
import { blob } from '../../drawing/plant-motifs.mjs';
import { P, rect, spans, item } from '../../drawing/items.mjs';

const ramp = (a, b, c) => [hex(a), hex(b), hex(c)];

// Each shape: rows of the glass body as [y, x0, x1], the cork rows, where the liquid starts (the rows
// above it are empty glass), and the box a label sits in.
const SHAPES = {
  flask: {
    cork: [[1, 7, 8], [2, 6, 9]],
    body: [[3, 7, 8], [4, 7, 8], [5, 6, 9], [6, 5, 10], [7, 4, 11], [8, 4, 11], [9, 4, 11], [10, 4, 11], [11, 4, 11], [12, 4, 11], [13, 5, 10], [14, 6, 9]],
    liquid: 7,
    label: [6, 9, 9, 12],
  },
  tall: {
    cork: [[1, 6, 9], [2, 6, 9]],
    body: [[3, 7, 8], [4, 7, 8], [5, 5, 10], ...Array.from({ length: 9 }, (_, i) => [6 + i, 5, 10])],
    liquid: 7,
    label: [6, 9, 9, 12],
  },
  squat: {
    cork: [[3, 6, 9], [4, 5, 10]],
    body: [[5, 6, 9], [6, 3, 12], ...Array.from({ length: 7 }, (_, i) => [7 + i, 2, 13]), [14, 3, 12]],
    liquid: 8,
    label: [5, 10, 9, 12],
  },
  conical: {
    cork: [[1, 7, 8], [2, 6, 9]],
    body: [[3, 7, 8], [4, 7, 8], [5, 7, 8], [6, 6, 9], [7, 5, 10], [8, 5, 10], [9, 4, 11], [10, 4, 11], [11, 3, 12], [12, 3, 12], [13, 2, 13], [14, 2, 13]],
    liquid: 9,
    label: [5, 10, 10, 13],
  },
};

/**
 * A glass bottle. opts: shape, liquid (a 3-colour ramp), label ([colour ramp, glyph cells]) or null,
 * stopper 'cork' | 'wick', glassTone to lighten the empty glass for clear liquids.
 */
function bottle(cv, { shape, liquid, label = null, stopper = 'cork', clear = false }) {
  const s = SHAPES[shape];
  const glass = clear ? P.glass[2] : P.glass[1];
  for (const [y, x0, x1] of s.body) {
    for (let x = x0; x <= x1; x++) {
      if (y < s.liquid) set(cv, x, y, glass, true);
      else set(cv, x, y, y === s.liquid ? liquid[2] : y >= 13 ? liquid[0] : liquid[1], true);
    }
  }
  const rows = s.body.filter(([y]) => y >= s.liquid);
  for (const [y, x0] of rows) {
    if (y > s.liquid && y < 14) set(cv, x0 + 1, y, liquid[2], true); // glass highlight down the left
  }
  const [cx0, ...rest] = s.cork;
  const corkRows = stopper === 'wick' ? [rest.at(-1) ?? cx0] : s.cork;
  for (const [y, x0, x1] of corkRows) rect(cv, x0, y, x1, y, P.cork[y === corkRows[0][0] ? 2 : 1]);
  if (stopper === 'wick') {
    const y = corkRows[0][0];
    for (let i = 0; i < y; i++) set(cv, 7, i, i === 0 ? hex('#3a2a20') : P.paper[2]);
    set(cv, 8, y - 1, P.paper[1]);
  }
  if (label) {
    const [lx0, ly0, lx1, ly1] = s.label;
    rect(cv, lx0, ly0, lx1, ly1, label[0][1], false);
    rect(cv, lx0, ly1, lx1, ly1, label[0][0], false);
    for (const [dx, dy, c] of label[1]) set(cv, lx0 + dx, ly0 + dy, c ?? P.cream[2], false);
  }
}

const pinkLabel = (glyph) => [P.pink, glyph];
const greenLabel = (glyph) => [P.green, glyph];
const dot = (...cells) => cells.map(([x, y, c]) => [x, y, c]);

const drink = (opts) => item((cv) => bottle(cv, opts));

export const drinks = {
  // rosy pink round flask, green label with a cross
  recovery_tea: drink({ shape: 'flask', liquid: ramp('#c46a82', '#e58aa2', '#f6b4c4'), label: greenLabel(dot([1, 0], [0, 1], [1, 1], [2, 1], [1, 2])) }),
  // bright green tall bottle, pink label with a bolt
  energy_tea: drink({ shape: 'tall', liquid: ramp('#3f9a2a', '#62c843', '#9ae86a'), label: pinkLabel(dot([2, 0], [1, 1], [2, 1], [1, 2], [2, 2], [1, 3])) }),
  // lavender squat bottle, green label with a crescent
  restful_infusion: drink({ shape: 'squat', liquid: ramp('#7a5ab0', '#a58ad8', '#cdb8f0'), label: greenLabel(dot([1, 0], [2, 0], [1, 1], [1, 2], [2, 2], [3, 1])) }),
  // amber-orange conical flask, green label with a flame
  warmth_remedy: drink({ shape: 'conical', liquid: ramp('#c9561a', '#ee7a24', '#f8a850'), label: greenLabel(dot([2, 0], [2, 1], [1, 2], [2, 2], [3, 2], [2, 3])) }),
  // icy blue tall bottle, pink label with a snow star
  cooling_remedy: drink({ shape: 'tall', liquid: ramp('#5aa0d0', '#8ccaf0', '#cfeeff'), label: pinkLabel(dot([1, 0], [2, 1], [1, 2], [2, 2], [2, 3])) }),
  // dusty yellow-grey squat bottle, pink label with a pick
  mining_tonic: drink({ shape: 'squat', liquid: ramp('#8a8466', '#b0aa88', '#d0cab0'), label: pinkLabel(dot([0, 0], [1, 0], [2, 0], [3, 0], [2, 1], [2, 2])) }),
  // teal round flask, pink label with an arrow
  travel_tonic: drink({ shape: 'flask', liquid: ramp('#2a8a86', '#3ab8b0', '#7adcd4'), label: pinkLabel(dot([1, 0], [0, 1], [1, 1], [2, 1], [1, 2])) }),
};

export const salves = {
  // a small round wooden tin, lid propped behind it, cream-pink salve inside, hot pink band
  healing_salve: item((cv) => {
    for (let y = 0; y <= 5; y++) {
      for (let x = 8; x <= 15; x++) {
        const d = ((x - 12) / 3.4) ** 2 + ((y - 3) / 2.4) ** 2;
        if (d <= 1.05) set(cv, x, y, d > 0.45 ? P.wood[2] : P.wood[1], true);
      }
    }
    spans(cv, [
      [5, 4, 10, P.wood[2]],
      [6, 3, 3, P.wood[2]], [6, 4, 10, ramp('#f8dedc', '#f4d0d0', '#fff0ee')[0]], [6, 11, 11, P.wood[2]],
      [7, 2, 2, P.wood[2]], [7, 3, 11, ramp('#f0c4c6', '#e8b0b6', '#fbe0e0')[0]], [7, 12, 12, P.wood[1]],
      [8, 2, 12, P.wood[2]],
      [9, 2, 12, P.wood[1]], [10, 2, 12, P.wood[1]],
      [11, 2, 12, P.pink[1]],
      [12, 2, 12, P.wood[0]], [13, 3, 11, P.wood[0]],
    ]);
    set(cv, 5, 6, hex('#ffffff'), false);
    set(cv, 6, 6, hex('#fff0ee'), false);
  }),
};

export const oils = {
  // clear amber oil in a tall bottle, a wick poking out of the stopper
  lantern_oil: drink({ shape: 'tall', liquid: ramp('#b8701a', '#e6982a', '#ffcf66'), stopper: 'wick', clear: true }),
  // murky green-brown oil in a squat bottle with a cream fish label
  fishing_blend: item((cv) => {
    bottle(cv, { shape: 'squat', liquid: ramp('#5a5a28', '#7a7a38', '#9a9a50') });
    rect(cv, 5, 10, 10, 12, P.paper[1], false);
    for (const [x, y] of [[6, 11], [7, 10], [7, 11], [7, 12], [8, 11], [9, 10], [9, 12]]) set(cv, x, y, P.green[1], false);
    set(cv, 6, 11, P.green[0], false);
  }),
};

/** A paper packet: rect body (x0..x1, y0..y1), a darker top flap of flapRows, per-spot decorations. */
function packet(cv, { x0, x1, y0, y1, paper, flap }) {
  rect(cv, x0, y0 + flap.length, x1, y1, paper[1]);
  rect(cv, x0 + 1, y0 + flap.length, x0 + 1, y1 - 1, paper[2], false);
  rect(cv, x0, y1, x1, y1, paper[0]);
  flap.forEach(([a, b], i) => rect(cv, a, y0 + i, b, y0 + i, i === flap.length - 1 ? paper[0] : paper[2]));
}

export const garden = {
  // a small burlap sack, tied with hot pink, brown granules on its face
  fertilizer: item((cv) => {
    rect(cv, 6, 1, 9, 2, P.burlap[2]);
    set(cv, 5, 2, P.burlap[1]);
    set(cv, 10, 2, P.burlap[1]);
    rect(cv, 6, 3, 9, 3, P.pink[1], false);
    set(cv, 10, 3, P.pink[0], false);
    set(cv, 5, 3, P.pink[2], false);
    const rows = [[4, 6, 9], [5, 5, 10], [6, 4, 11], ...Array.from({ length: 6 }, (_, i) => [7 + i, 3, 12]), [13, 4, 11], [14, 5, 10]];
    for (const [y, x0, x1] of rows) {
      for (let x = x0; x <= x1; x++) set(cv, x, y, (x + y) % 2 ? P.burlap[1] : y > 10 ? P.burlap[0] : P.burlap[2], true);
    }
    for (const [x, y] of [[5, 8], [7, 9], [9, 8], [6, 11], [10, 10], [8, 12], [11, 12]]) set(cv, x, y, hex(y % 2 ? '#4a3220' : '#6b4a2f'), false);
  }),
  // a rectangular cream packet with a yellow and pink sparkle
  flower_dust: item((cv) => {
    packet(cv, { x0: 3, x1: 12, y0: 2, y1: 14, paper: P.paper, flap: [[3, 12], [3, 12], [3, 12]] });
    rect(cv, 7, 4, 8, 5, P.pink[1], false); // seal
    const yel = hex('#f6d03a');
    for (const [x, y, c] of [[6, 9, yel], [5, 9, yel], [7, 9, yel], [6, 8, yel], [6, 10, yel], [10, 8, P.pink[2]], [9, 8, P.pink[1]], [11, 8, P.pink[1]], [10, 7, P.pink[1]], [10, 9, P.pink[1]], [9, 12, yel], [5, 12, P.pink[2]], [11, 11, yel]]) set(cv, x, y, c, false);
  }),
  // a narrower kraft packet with a pointed flap and brown/red speckles
  mushroom_dust: item((cv) => {
    packet(cv, { x0: 4, x1: 11, y0: 2, y1: 14, paper: P.kraft, flap: [[7, 8], [6, 9], [5, 10], [4, 11]] });
    const specks = [[5, 7, '#6b3a22'], [7, 8, '#c43c3c'], [9, 7, '#6b3a22'], [6, 10, '#c43c3c'], [10, 10, '#6b3a22'], [8, 11, '#c43c3c'], [5, 13, '#6b3a22'], [10, 13, '#c43c3c'], [7, 13, '#6b3a22'], [9, 9, '#8a2a2a']];
    for (const [x, y, c] of specks) set(cv, x, y, hex(c), false);
  }),
  // bright green liquid in a conical flask, a pale leaf on a deep green label
  growth_infusion: drink({
    shape: 'conical',
    liquid: ramp('#4ad04a', '#7cf06a', '#b4ff9a'),
    label: [P.green, dot([2, 0, hex('#9fe27a')], [1, 1, hex('#9fe27a')], [2, 1, hex('#9fe27a')], [3, 1, hex('#9fe27a')], [2, 2, hex('#9fe27a')], [2, 3, hex('#9fe27a')])],
  }),
  // purple-grey liquid in a tall bottle, a cream label with a bug crossed out in hot pink
  pest_repellent: item((cv) => {
    bottle(cv, { shape: 'tall', liquid: ramp('#6a5a78', '#8a7a98', '#aa9ab8') });
    rect(cv, 6, 9, 9, 12, P.paper[2], false);
    rect(cv, 7, 10, 8, 11, hex('#2a2232'), false);
    set(cv, 6, 10, hex('#2a2232'), false);
    set(cv, 9, 11, hex('#2a2232'), false);
    line(cv, 6, 12, 9, 9, P.pink[1]);
  }),
  // dark brown liquid in a squat bottle, a moss label
  soil_tonic: item((cv) => {
    bottle(cv, { shape: 'squat', liquid: ramp('#3a2418', '#563620', '#7a5232'), label: [ramp('#3f5d37', '#5f8a4a', '#8db55f'), dot([1, 1, hex('#c8e8a0')], [2, 1, hex('#c8e8a0')], [2, 0, hex('#c8e8a0')], [3, 2, hex('#c8e8a0')])] });
  }),
};

export const ingredients = {
  // pale yellow oil in a plain flask, no label
  plant_oil: drink({ shape: 'flask', liquid: ramp('#d8c870', '#f2e48c', '#fff6b8'), clear: true }),
  // a glossy amber lump with a drip and a hard highlight
  resin: item((cv) => {
    blob(cv, 8, 9, 5.2, 4, P.amber);
    blob(cv, 12, 12, 1.8, 1.8, P.amber);
    blob(cv, 4, 12, 1.5, 1.5, P.amber);
    blob(cv, 9, 5, 2.4, 1.6, P.amber);
    for (const [x, y] of [[5, 8], [6, 7], [6, 8], [9, 4]]) set(cv, x, y, hex('#fff2c4'), false);
    for (const [x, y] of [[10, 11], [11, 11], [9, 12]]) set(cv, x, y, hex('#8a4a14'), false);
  }),
};

/** [path relative to textures/item/preparations, sprite function] for all 18 products. */
export const PRODUCTS = [
  ...Object.entries(drinks).map(([k, f]) => [`drinks/${k}.png`, f]),
  ...Object.entries(salves).map(([k, f]) => [`salves/${k}.png`, f]),
  ...Object.entries(oils).map(([k, f]) => [`oils/${k}.png`, f]),
  ...Object.entries(garden).map(([k, f]) => [`garden/${k}.png`, f]),
  ...Object.entries(ingredients).map(([k, f]) => [`ingredients/${k}.png`, f]),
];
