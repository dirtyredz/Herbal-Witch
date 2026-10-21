// The hearth (owner reference: reference-hearth.png): a gothic fireplace two blocks wide and two
// tall. Charcoal stone-brick firebox under a stepped arch, an iron grate before the fire,
// dark wooden mantel posts and shelf with gold bands and square caps, olive ivy with
// small flowers draped over the mantel corners, a dark stone chimney breast with a gold moon-and-
// stars plaque and gold trim, dark stone floor slabs in front, iron fire tools leaning on one side
// and a small log crate on the other. The fire (glowing back wall, flames, coals) shows while lit.
import { box, plane } from '../geometry.mjs';
import { rivet, ivyDrape } from '../shapes.mjs';
import { STATION } from '../textures.mjs';
import { multiblockModels, rectangle } from './multiblock.mjs';

const OPEN = [-6.5, 6.5]; // firebox opening, x
const OPEN_TOP = 10.5;

function base() {
  const els = [];
  // floor slabs in front, and the hearth stone inside
  els.push(box([-15.5, 0, 0], [15.5, 1.5, 6], '#stone'));
  els.push(box([-12, 0, 6], [12, 1.5, 16], '#stone', { skip: ['north'] }));
  // firebox: brick jambs, a lintel stepped into a gothic arch, the back wall
  els.push(box([-12, 1.5, 5], [OPEN[0], 14.5, 16], '#stone', { skip: ['down'] }));
  els.push(box([OPEN[1], 1.5, 5], [12, 14.5, 16], '#stone', { skip: ['down'] }));
  els.push(box([OPEN[0], OPEN_TOP + 1.5, 5], [OPEN[1], 14.5, 16], '#stone', { skip: ['down'] }));
  els.push(box([OPEN[0], OPEN_TOP, 5], [-4, OPEN_TOP + 1.5, 15], '#stone', { skip: ['up', 'west'] }));
  els.push(box([4, OPEN_TOP, 5], [OPEN[1], OPEN_TOP + 1.5, 15], '#stone', { skip: ['up', 'east'] }));
  els.push(box([-4, OPEN_TOP + 0.75, 5], [-2, OPEN_TOP + 1.5, 15], '#stone', { skip: ['up'] }));
  els.push(box([2, OPEN_TOP + 0.75, 5], [4, OPEN_TOP + 1.5, 15], '#stone', { skip: ['up'] }));
  // the back wall rises to the lintel, so the back is solid brick (owner feedback: no gaps behind)
  els.push(box([OPEN[0], 1.5, 12], [OPEN[1], OPEN_TOP + 1.5, 16], '#stone', { skip: ['up', 'down', 'west', 'east'] }));
  els.push(box([-1.25, 11.75, 4.5], [1.25, 14, 5], '#gold')); // keystone
  els.push(rivet('north', -9, 12.5, 5), rivet('north', 9, 12.5, 5)); // gold star studs
  // iron grate before the fire
  els.push(box([-5.5, 3.5, 6.5], [5.5, 4.25, 7.25], '#iron'));
  els.push(box([-5.5, 1.5, 9], [5.5, 2.25, 9.75], '#iron', { skip: ['down'] }));
  for (const x of [-4.85, -1.75, 1.75, 4.85]) els.push(box([x - 0.5, 1.5, 6.35], [x + 0.5, 5.5, 7.4], '#iron', { skip: ['down'] }));
  // mantel posts with gold bands, and the shelf with gold square caps
  for (const x of [-13.75, 13.75]) {
    els.push(box([x - 1.75, 3, 3.5], [x + 1.75, 15, 7.5], '#post', { skip: ['down'] }));
    els.push(box([x - 2, 4, 3.25], [x + 2, 5.25, 7.75], '#gold', { skip: ['up', 'down'] }));
    els.push(box([x - 2, 11, 3.25], [x + 2, 12.25, 7.75], '#gold', { skip: ['up', 'down'] }));
    els.push(box([x - 2.25, 1.5, 3.25], [x + 2.25, 3, 7.75], '#gold', { skip: ['down'] }));
  }
  els.push(box([-15.5, 15, 3], [15.5, 18, 10], '#wood'));
  els.push(box([-15, 14.5, 3.75], [15, 15, 9.5], '#wood', { skip: ['up'] }));
  // brick behind the mantel, closing the back between the firebox and the chimney breast
  els.push(box([-12, 14.5, 10], [12, 18, 16], '#stone', { skip: ['down'] }));
  for (const x of [-14, 14]) els.push(box([x - 1.75, 15.5, 2.5], [x + 1.75, 18.5, 5], '#gold'));
  // the chimney breast with its moon plaque, gold trim and stepped cap
  els.push(box([-11, 18, 8.5], [11, 28, 16], '#stone', { skip: ['down'] }));
  els.push(box([-6, 19.5, 8.25], [6, 26.5, 8.5], '#plaque', { decal: { north: [0, 0, 16, 16] }, skip: ['down', 'up', 'south', 'east', 'west'] }));
  els.push(box([-11.5, 26.5, 8], [11.5, 27.5, 15.9], '#gold', { skip: ['down'] }));
  els.push(box([-10, 28, 9.5], [10, 31, 16], '#stone', { skip: ['down'] }));
  els.push(box([-8, 31, 10.5], [8, 32, 16], '#stone', { skip: ['down'] }));
  // fire tools leaning at the east end, a log crate at the west end
  els.push(box([14.25, 1.5, 0.5], [14.75, 11, 1], '#iron'), box([13.75, 10.5, 0.25], [15.25, 11.5, 1.25], '#gold'));
  els.push(box([15, 1.5, 1.25], [15.5, 10, 1.75], '#iron'), box([14.5, 1.5, 0.75], [16, 3, 2.25], '#iron'));
  els.push(box([-15.6, 1.5, 0.4], [-12.35, 2, 3.4], '#post', { skip: ['down'] }));
  for (const z of [0.4, 2.9]) els.push(box([-15.6, 2, z], [-12.35, 5.5, z + 0.5], '#post'));
  els.push(box([-15.1, 2, 0.9], [-12.85, 3.5, 2.9], '#log'), box([-15.1, 3.5, 1.15], [-12.85, 5, 2.65], '#log'));
  for (const [x, z] of [[-15.7, 0.3], [-12.95, 0.3]]) els.push(box([x, 5.5, z], [x + 0.7, 6, z + 0.7], '#gold'));
  // ivy draped over the mantel corners
  els.push(...ivyDrape({ face: 'north', at: 3, along: [8.5, 15.5], top: 18, lengths: [7, 12, 9, 5] }));
  els.push(...ivyDrape({ face: 'north', at: 3, along: [-15.5, -8.5], top: 18, lengths: [5, 10, 13, 6] }));
  for (const x of [-12, 12]) els.push(box([x - 2.5, 18, 3.5], [x + 2.5, 19, 7.5], '#ivy', { decal: { all: [0, 0, 8, 6] } }));
  return els;
}

function fire() {
  return [
    box([-6, 1.5, 8], [6, 2.75, 11.75], '#coals', { glow: true, skip: ['down'] }),
    box([OPEN[0], 1.75, 11.9], [OPEN[1], OPEN_TOP, 11.95], '#glow', { glow: true, skip: ['up', 'down', 'south', 'east', 'west'] }),
    plane('z', 9.5, -6, 6, 2.5, 10.25, '#flame', { glow: true, uv: [0, 7, 16, 16] }),
    plane('z', 10.75, -5, 5, 2.5, 9, '#flame', { glow: true, uv: [4, 8, 14, 16] }),
    plane('x', 0, 8, 11.5, 2.5, 9.5, '#flame', { glow: true, uv: [6, 7, 14, 16] }),
  ];
}

export function hearth() {
  return multiblockModels({
    dir: 'block/stations/hearth',
    cells: rectangle(2, 2),
    base: base(),
    overlay: { name: 'fire', elements: fire() },
    fit: 0.5,
    textures: {
      stone: STATION.stone_dark, plaque: STATION.plaque, wood: STATION.wood, post: STATION.wood_post, log: STATION.wood_end,
      gold: STATION.gold, iron: STATION.iron, ivy: STATION.ivy, coals: STATION.coals, glow: STATION.fire_glow,
      flame: STATION.flame, particle: STATION.stone_dark,
    },
  });
}
