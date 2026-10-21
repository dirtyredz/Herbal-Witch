// The witch's stove (owner reference: reference-stove-pot.png): a two-block cooking range in
// charcoal iron with reddish-brown wooden corner posts, gold caps and bands. On the iron top two
// burner plates carry the vessels (the kettle and the pot sit on them). In front: a gold-framed
// firebox door with a small grate, two gold-framed drawers with gold star emblems, a gold towel rail
// with a dark red cloth bearing a gold crescent; at the west end a gold rail with a hanging pan,
// a wooden spoon and a dried herb bundle; at the back a black iron stovepipe with gold bands. The
// firebox window and the burners glow while lit.
import { box, plane } from '../geometry.mjs';
import { octagon, rivet } from '../shapes.mjs';
import { STATION } from '../textures.mjs';
import { multiblockModels, rectangle } from './multiblock.mjs';

const BURNERS = [8, -8]; // burner centres (x), one per cell
const TOP = 15.75;

function base() {
  const els = [];
  // the iron body, its top plate and the burner plates
  els.push(box([-14, 1.5, 1.5], [14, 14.5, 15], '#iron'));
  els.push(box([-15.5, 14.5, 0.5], [15.5, TOP, 15.75], '#iron_top'));
  for (const x of BURNERS) els.push(...octagon(x, 8, TOP, 16, 10, 10, 2.5, '#iron', { skip: ['down'], third: false }));
  // wooden corner posts with gold caps, bands and feet
  for (const x of [-15, 15]) {
    for (const z of [1.25, 14.75]) {
      els.push(box([x - 1.25, 1, z - 1.25], [x + 1.25, 14.5, z + 1.25], '#post'));
      els.push(box([x - 1.5, 0, z - 1.5], [x + 1.5, 1.5, z + 1.5], '#gold'));
      els.push(box([x - 1.5, 7, z - 1.5], [x + 1.5, 8, z + 1.5], '#gold', { skip: ['up', 'down'] }));
      els.push(box([x - 1.5, 14.25, z - 1.5], [x + 1.5, 16, z + 1.5], '#gold'));
    }
  }
  // the firebox door: a gold frame around an iron window, with a small grate
  els.push(box([-2, 3, 1], [9, 4, 1.5], '#gold'), box([-2, 11, 1], [9, 12, 1.5], '#gold'));
  els.push(box([-2, 4, 1], [-1, 11, 1.5], '#gold'), box([8, 4, 1], [9, 11, 1.5], '#gold'));
  els.push(box([-1, 4, 1.4], [8, 11, 1.5], '#iron', { skip: ['down', 'up', 'south', 'east', 'west'] }));
  els.push(box([-0.5, 5, 0.75], [7.5, 5.75, 1.25], '#iron'));
  for (const x of [0.5, 3.5, 6.5]) els.push(box([x - 0.35, 5, 0.75], [x + 0.35, 8, 1.25], '#iron'));
  els.push(box([8.5, 6.5, 0.5], [9.5, 8.5, 1], '#gold')); // the door latch
  // two gold-framed drawers with star emblems
  for (const [y0, y1] of [[3, 7], [7.5, 11.5]]) {
    els.push(box([-12.5, y0, 1.1], [-3.5, y1, 1.5], '#iron_top', { skip: ['south'] }));
    els.push(box([-12.5, y0, 1], [-3.5, y0 + 0.5, 1.1], '#gold'), box([-12.5, y1 - 0.5, 1], [-3.5, y1, 1.1], '#gold'));
    els.push(box([-8.5, (y0 + y1) / 2 - 1.5, 0.75], [-7.5, (y0 + y1) / 2 + 1.5, 1.1], '#gold'));
    els.push(box([-9.5, (y0 + y1) / 2 - 0.5, 0.75], [-6.5, (y0 + y1) / 2 + 0.5, 1.1], '#gold'));
  }
  // the gold towel rail with its brackets, and the red cloth draped over it at the east end
  els.push(box([-13, 12.75, 0.25], [13, 13.5, 0.75], '#gold'));
  for (const x of [-13, -1, 13]) els.push(box([x - 0.5, 12.5, 0.25], [x + 0.5, 13.75, 1.5], '#gold'));
  els.push(box([9.5, 5, 0.1], [13.5, 13.5, 0.25], '#cloth', { decal: { north: [0, 0, 16, 16], south: [0, 0, 16, 16] }, skip: ['up', 'down', 'east', 'west'] }));
  // the west end: a gold rail with a hanging pan, a spoon and a dried bundle
  els.push(box([-15.5, 11.5, 3], [-15, 12.25, 13], '#gold'));
  els.push(...octagon(-14.6, 6.5, 4, 9, 0.6, 5, 1.25, '#iron', { third: false }).map((e) => ({ ...e, from: [-15.4, e.from[1], e.from[2]], to: [-14.6, e.to[1], e.to[2]] })));
  els.push(box([-15.2, 9, 6.1], [-14.8, 11.5, 6.9], '#iron'));
  els.push(box([-15.4, 5, 9.75], [-14.8, 11.5, 10.5], '#post'), box([-15.5, 4, 9.5], [-14.7, 6, 10.75], '#post'));
  els.push(plane('x', -15.25, 11, 14, 4, 11.5, '#bundle'));
  els.push(rivet('west', -14, 3, 8), rivet('east', 14, 3, 8));
  // the stovepipe at the back of the west cell, behind its burner, rising above the top
  els.push(box([-15.5, TOP, 14.25], [-12, 31, 16], '#iron', { skip: ['down'] }));
  for (const y of [19, 26]) els.push(box([-15.75, y, 14], [-11.75, y + 1, 16], '#gold', { skip: ['up', 'down', 'south'] }));
  els.push(box([-16, 31, 13.75], [-11.5, 32, 16], '#gold'));
  return els;
}

function fire() {
  const els = [box([-1, 4, 1.35], [8, 11, 1.38], '#glow', { glow: true, skip: ['down', 'up', 'south', 'east', 'west'] })];
  for (const x of BURNERS) els.push(...octagon(x, 8, 16, 16.05, 6, 6, 1.5, '#coals', { glow: true, skip: ['down'], third: false }));
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
      bundle: STATION.bundle_lavender, coals: STATION.coals, glow: STATION.fire_glow, particle: STATION.iron,
    },
  });
}
