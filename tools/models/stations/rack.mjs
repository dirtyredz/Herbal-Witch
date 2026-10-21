// The drying rack (owner reference: reference-drying-rack.png): a wide wooden A-frame, two blocks
// wide and two tall. At each end two splayed reddish-brown legs meet under a black-iron cap; iron
// feet and bands carry gold rivets; a horizontal top beam with gold strap clips spans both ends.
// While the rack holds anything, four dried bundles hang from gold hooks on the beam, each tied
// with twine and each a different herb (green leafy, lavender, white-flowered, yellow-flowered).
// They show occupancy, not which herb is drying.
import { box, plane } from '../geometry.mjs';
import { rivet } from '../shapes.mjs';
import { STATION } from '../textures.mjs';
import { multiblockModels, rectangle } from './multiblock.mjs';

const ENDS = [13, -13]; // x of the two A-frames (assembly space: x -16..16)
const BEAM_Y = [24, 27];

function aFrame(x) {
  const els = [];
  // two legs splayed front and back, meeting under the cap
  for (const angle of [22.5, -22.5]) {
    const rot = { origin: [x, 24, 8], axis: 'x', angle };
    els.push(
      box([x - 1.5, 2.5, 6.5], [x + 1.5, 24, 9.5], '#post', { rot }),
      box([x - 1.75, 6, 6.25], [x + 1.75, 7.5, 9.75], '#iron', { rot, skip: ['up', 'down'] }), // iron band
      box([x - 1.75, 15, 6.25], [x + 1.75, 16.5, 9.75], '#iron', { rot, skip: ['up', 'down'] }),
    );
  }
  // iron-capped feet with gold rivets, and the base beam between them
  for (const [z0, z1] of [[0.5, 3.75], [12.25, 15.5]]) {
    els.push(box([x - 2, 0, z0], [x + 2, 3, z1], '#iron'));
    els.push(rivet(x > 0 ? 'east' : 'west', x + (x > 0 ? 2 : -2), 1.5, (z0 + z1) / 2));
  }
  els.push(rivet('north', x, 1.5, 0.5), rivet('south', x, 1.5, 15.5));
  els.push(box([x - 1.25, 0.5, 3], [x + 1.25, 2.5, 13], '#wood_z'));
  // the iron cap where the legs meet, gold-riveted on three faces
  els.push(box([x - 2.25, 22.5, 5.75], [x + 2.25, 27.5, 10.25], '#iron'));
  els.push(rivet('north', x, 25, 5.75), rivet('south', x, 25, 10.25), rivet(x > 0 ? 'east' : 'west', x + (x > 0 ? 2.25 : -2.25), 25, 8));
  return els;
}

const HOOKS = [9, 3, -3, -9];

function base() {
  const els = [...aFrame(ENDS[0]), ...aFrame(ENDS[1])];
  // the top beam, with gold strap clips and hooks
  els.push(box([-10.75, BEAM_Y[0], 6.75], [10.75, BEAM_Y[1], 9.25], '#wood'));
  for (const x of HOOKS) {
    els.push(box([x - 0.75, BEAM_Y[0] - 0.25, 6.5], [x + 0.75, BEAM_Y[1] + 0.25, 9.5], '#gold'));
    els.push(box([x - 0.5, BEAM_Y[0] - 2, 7.5], [x + 0.5, BEAM_Y[0] - 0.25, 8.5], '#gold'));
  }
  return els;
}

const BUNDLES = ['#bundle_green', '#bundle_lavender', '#bundle_white', '#bundle_yellow'];

function bundles() {
  const els = [];
  HOOKS.forEach((x, i) => {
    const top = BEAM_Y[0] - 1.5;
    const tex = BUNDLES[i];
    const h = 16;
    const turn = (angle) => ({ rot: { origin: [x, top, 8], axis: 'y', angle } });
    els.push(
      plane('z', 8, x - 4.5, x + 4.5, top - h, top, tex), plane('x', x, 3.5, 12.5, top - h, top, tex),
      plane('z', 8, x - 4, x + 4, top - h + 1, top, tex, turn(45)), plane('z', 8, x - 4, x + 4, top - h + 1, top, tex, turn(-45)),
    );
  });
  return els;
}

export function dryingRack() {
  return multiblockModels({
    dir: 'block/stations/drying_rack',
    cells: rectangle(2, 2),
    base: base(),
    overlay: { name: 'bundles', elements: bundles() },
    fit: 0.5,
    textures: {
      wood: STATION.wood, wood_z: STATION.wood, post: STATION.wood_post, iron: STATION.iron, gold: STATION.gold,
      bundle_green: STATION.bundle_green, bundle_lavender: STATION.bundle_lavender, bundle_white: STATION.bundle_white,
      bundle_yellow: STATION.bundle_yellow, particle: STATION.wood,
    },
  });
}
