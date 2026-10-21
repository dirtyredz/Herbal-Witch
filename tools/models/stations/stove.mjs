// The witch's stove (owner reference: reference-stove-pot.png): a two-block cooking range in
// charcoal iron with dark wooden corner posts, gold caps and bands. On the iron top two
// burner plates carry the vessels (the kettle and the pot sit on them). In front: a gold-framed
// firebox door with a small grate, two gold-framed drawers with gold star emblems, a gold towel rail
// with a dark red cloth bearing a gold crescent; at the west end a gold rail with a hanging pan,
// a wooden spoon and a dried herb bundle; a black iron stovepipe with gold bands rises out of the
// back of the body between the burners (owner: never perched on a corner post). The firebox
// window and the burners glow while lit.
import { box } from '../geometry.mjs';
import { octagon, rivet } from '../shapes.mjs';
import { STATION } from '../textures.mjs';
import { multiblockModels, rectangle } from './multiblock.mjs';
import { bundle } from '../preparations/bundles.mjs';

const BURNERS = [8, -8]; // burner centres (x), one per cell
const BURNER_Z = 7.5;
const TOP = 15.75;
const PIPE = [-1.75, 1.75]; // the stovepipe rises at the back, between the burners

function base() {
  const els = [];
  // the iron body (its top under the plate), the top plate and the burner plates
  els.push(box([-14, 1.5, 1.5], [14, 14.5, 15], '#iron', { skip: ['up'] }));
  els.push(box([-15.5, 14.5, 0.5], [15.5, TOP, 15.75], '#iron_top'));
  for (const x of BURNERS) els.push(...octagon(x, BURNER_Z, TOP, 16, 9, 9, 2.25, '#iron', { skip: ['down'], third: false }));
  // wooden corner posts (their tops under the caps) with gold caps, bands and feet
  for (const x of [-14.75, 14.75]) {
    for (const z of [1.25, 14.75]) {
      els.push(box([x - 1.25, 1.5, z - 1.25], [x + 1.25, 14.25, z + 1.25], '#post', { skip: ['up', 'down'] }));
      els.push(box([x - 1.25, 0, z - 1.25], [x + 1.25, 1.5, z + 1.25], '#gold'));
      els.push(box([x - 1.35, 7, z - 1.35], [x + 1.35, 8, z + 1.35], '#gold', { skip: ['up', 'down'] }));
      els.push(box([x - 1.25, 14.25, z - 1.25], [x + 1.25, 16, z + 1.25], '#gold'));
    }
  }
  // the firebox door: a gold frame around an iron window, with a small grate
  els.push(box([-2, 3, 1], [9, 4, 1.5], '#gold', { skip: ['south'] }), box([-2, 11, 1], [9, 12, 1.5], '#gold', { skip: ['south'] }));
  els.push(box([-2, 4, 1], [-1, 11, 1.5], '#gold', { skip: ['south', 'up', 'down'] }), box([8, 4, 1], [9, 11, 1.5], '#gold', { skip: ['south', 'up', 'down'] }));
  els.push(box([-1, 4, 1.4], [8, 11, 1.5], '#iron', { skip: ['down', 'up', 'south', 'east', 'west'] }));
  els.push(box([-0.5, 5, 0.75], [7.5, 5.75, 1.25], '#iron'));
  for (const x of [0.5, 3.5, 6.5]) els.push(box([x - 0.35, 5.75, 0.8], [x + 0.35, 8, 1.2], '#iron'));
  els.push(box([8.5, 6.5, 0.5], [9.5, 8.5, 1], '#gold')); // the door latch
  // two gold-framed drawers with star emblems
  for (const [y0, y1] of [[3, 7], [7.5, 11.5]]) {
    els.push(box([-12.5, y0, 1.1], [-3.5, y1, 1.5], '#iron_top', { skip: ['south'] }));
    els.push(box([-12.6, y0 - 0.1, 1], [-3.4, y0 + 0.5, 1.1], '#gold'), box([-12.6, y1 - 0.5, 1], [-3.4, y1 + 0.1, 1.1], '#gold'));
    els.push(box([-8.5, (y0 + y1) / 2 - 1.5, 0.75], [-7.5, (y0 + y1) / 2 + 1.5, 1.1], '#gold'));
    els.push(box([-9.5, (y0 + y1) / 2 - 0.5, 0.8], [-8.5, (y0 + y1) / 2 + 0.5, 1.1], '#gold'), box([-7.5, (y0 + y1) / 2 - 0.5, 0.8], [-6.5, (y0 + y1) / 2 + 0.5, 1.1], '#gold'));
  }
  // the gold towel rail with its brackets, and the red cloth draped over it at the east end
  els.push(box([-13, 12.75, 0.25], [13, 13.5, 0.75], '#gold'));
  for (const x of [-13, -1, 13]) els.push(box([x - 0.5, 12.6, 0.75], [x + 0.5, 13.65, 1.5], '#gold'));
  els.push(box([9.5, 5, 0.1], [13.5, 13.5, 0.15], '#cloth', { decal: { north: [0, 0, 16, 16], south: [0, 0, 16, 16] }, skip: ['up', 'down', 'east', 'west'] }));
  // the west end: a gold rail with a hanging pan, a wooden spoon and a dried bundle
  els.push(box([-15.6, 11.5, 3], [-15.1, 12.25, 13], '#gold'));
  els.push(box([-15.5, 4.5, 4.75], [-14.75, 8.5, 8.25], '#iron'), box([-15.5, 5.25, 4], [-14.75, 7.75, 9], '#iron', { skip: ['west', 'east'] }));
  els.push(box([-15.35, 8.5, 6.1], [-14.9, 11.5, 6.9], '#iron'));
  els.push(box([-15.4, 5, 9.75], [-14.8, 11.25, 10.5], '#post'), box([-15.5, 4, 9.5], [-14.7, 5, 10.75], '#post'));
  els.push(...bundle('lavender', { cx: 11.7, cz: -15.05, top: 11.5, depth: 1.6, length: 0.7, seed: 7, across: 'x' }));
  els.push(rivet('west', -14, 3, 8), rivet('east', 14, 3, 8));
  // the stovepipe rises out of the body: a riveted flue box on the back wall, a gold collar on
  // the top plate, then the black pipe with gold bands and a gold rim, above the stove
  els.push(box([-2.75, 6, 15], [2.75, 14.5, 16], '#iron'));
  els.push(rivet('south', -1.75, 7, 16), rivet('south', 1.75, 7, 16));
  els.push(box([-2.5, TOP, 12.25], [2.5, 17, 16], '#gold'));
  els.push(box([PIPE[0], 17, 12.75], [PIPE[1], 31, 16], '#iron', { skip: ['down'] }));
  for (const y of [21, 26]) els.push(box([PIPE[0] - 0.25, y, 12.5], [PIPE[1] + 0.25, y + 1, 16], '#gold', { skip: ['up', 'down', 'south'] }));
  els.push(box([PIPE[0] - 0.5, 31, 12.25], [PIPE[1] + 0.5, 32, 16], '#gold'));
  return els;
}

function fire() {
  const els = [box([-1, 4, 1.3], [8, 11, 1.33], '#glow', { glow: true, skip: ['down', 'up', 'south', 'east', 'west'] })];
  for (const x of BURNERS) els.push(...octagon(x, BURNER_Z, 16, 16.1, 5.5, 5.5, 1.5, '#coals', { glow: true, skip: ['down'], third: false }));
  return els;
}

export function stove() {
  return multiblockModels({
    dir: 'block/stations/stove',
    cells: rectangle(2, 1),
    base: base(),
    overlay: { name: 'fire', elements: fire() },
    fit: 0.5,
    overflow: true,
    textures: {
      iron: STATION.iron, iron_top: STATION.iron, post: STATION.wood_post, gold: STATION.gold, cloth: STATION.cloth,
      parts: STATION.parts, coals: STATION.coals, glow: STATION.fire_glow, particle: STATION.iron,
    },
  });
}
