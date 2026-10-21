// The drying rack (owner reference: reference-drying-rack.png): a wide wooden A-frame, two blocks
// wide and two tall. At each end two dark wooden legs rise from black-iron feet, leaning inward at
// 22.5 degrees so each lands squarely on its foot (owner feedback: legs must never float), and
// cross just under a black-iron cap; iron feet and bands carry gold rivets; a horizontal top beam
// with gold strap clips spans both ends. While the rack holds anything, four dried bundles hang
// from gold hooks on the beam (3D cube bundles: green leafy, lavender, white-flowered,
// yellow-flowered). They show occupancy, not which herb is drying.
import { box } from '../geometry.mjs';
import { rivet } from '../shapes.mjs';
import { STATION } from '../textures.mjs';
import { multiblockModels, rectangle } from './multiblock.mjs';
import { bundle } from '../preparations/bundles.mjs';

const ENDS = [13, -13]; // x of the two A-frames (assembly space: x -16..16)
const BEAM_Y = [24, 27];
const FEET = [1.75, 14.25]; // foot centres (z)
const FOOT_TOP = 3;
const LEG = 22.5; // leg length from its foot

function aFrame(x) {
  const els = [];
  // each leg stands on its foot (pivot sunk just into the foot top) and leans inward; the back
  // leg is a touch narrower where they cross, like a halved joint, so their sides never coincide
  for (const [zf, angle, hw] of [[FEET[0], 22.5, 1.5], [FEET[1], -22.5, 1.3]]) {
    const base = FOOT_TOP - 0.6;
    const rot = { origin: [x, base, zf], axis: 'x', angle };
    els.push(
      box([x - hw, base, zf - 1.5], [x + hw, base + LEG, zf + 1.5], '#post', { rot }),
      box([x - hw - 0.25, base + 4, zf - 1.75], [x + hw + 0.25, base + 5.5, zf + 1.75], '#iron', { rot, skip: ['up', 'down'] }), // iron bands
      box([x - hw - 0.25, base + 11, zf - 1.75], [x + hw + 0.25, base + 12.5, zf + 1.75], '#iron', { rot, skip: ['up', 'down'] }),
    );
  }
  // iron feet with gold rivets, and the base beam between them
  for (const zf of FEET) {
    els.push(box([x - 2, 0, zf - 1.5], [x + 2, FOOT_TOP, zf + 1.5], '#iron'));
    els.push(rivet(x > 0 ? 'east' : 'west', x + (x > 0 ? 2 : -2), 1.5, zf));
  }
  els.push(rivet('north', x, 1.5, FEET[0] - 1.5), rivet('south', x, 1.5, FEET[1] + 1.5));
  els.push(box([x - 1.25, 0.5, FEET[0] + 1.5], [x + 1.25, 2.5, FEET[1] - 1.5], '#wood_z'));
  // the iron cap over the crossing, gold-riveted on three faces
  els.push(box([x - 2.25, 20, 3.75], [x + 2.25, 27.5, 12.25], '#iron'));
  els.push(rivet('north', x, 25, 3.75), rivet('south', x, 25, 12.25), rivet(x > 0 ? 'east' : 'west', x + (x > 0 ? 2.25 : -2.25), 25, 8));
  return els;
}

const HOOKS = [8.5, 3, -3, -8.5];

function base() {
  const els = [...aFrame(ENDS[0]), ...aFrame(ENDS[1])];
  // the top beam, with gold strap clips and hooks
  els.push(box([-10.75, BEAM_Y[0], 6.75], [10.75, BEAM_Y[1], 9.25], '#wood'));
  for (const x of HOOKS) {
    els.push(box([x - 0.75, BEAM_Y[0] - 0.25, 6.5], [x + 0.75, BEAM_Y[1] + 0.25, 9.5], '#gold'));
    els.push(box([x - 0.5, BEAM_Y[0] - 1.75, 7.5], [x + 0.5, BEAM_Y[0] - 0.25, 8.5], '#gold'));
  }
  return els;
}

const BUNDLES = ['mint', 'lavender', 'chamomile', 'yellow'];

function bundles() {
  return HOOKS.flatMap((x, i) => bundle(BUNDLES[i], { cx: x, cz: 8, top: BEAM_Y[0] - 1.75, length: 1.1, seed: 50 + i }));
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
      parts: STATION.parts, particle: STATION.wood,
    },
  });
}
