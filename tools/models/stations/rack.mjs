// The drying rack (owner reference: reference-drying-rack.png): a wide wooden A-frame, two blocks
// wide and two tall. At each end two dark wooden legs rise from black-iron feet, leaning inward at
// 22.5 degrees so each lands squarely on its foot (owner feedback: legs must never float), and
// meet at an apex under a small black-iron cap (owner feedback dev.2: they must not cross past each
// other, and the caps must be small); iron feet and the cap carry gold rivets; a horizontal top beam
// with gold strap clips spans both ends. While the rack holds anything, four dried bundles hang
// from gold hooks on the beam (3D cube bundles: green leafy, lavender, white-flowered,
// yellow-flowered). They show occupancy, not which herb is drying.
import { box } from '../geometry.mjs';
import { rivet } from '../shapes.mjs';
import { STATION } from '../textures.mjs';
import { multiblockModels, rectangle } from './multiblock.mjs';
import { bundle } from '../preparations/bundles.mjs';
import { separateCoplanar } from '../botany/separate.mjs';

const ENDS = [13, -13]; // x of the two A-frames (assembly space: x -16..16)
const BEAM_Y = [24, 27];
const FEET = [1.75, 14.25]; // foot centres (z)
const FOOT_TOP = 3;
const LEG = 17; // leg length from its foot: the centre lines meet at z 8, 16.4 up the slope
const APEX_Y = FOOT_TOP - 0.6 + 15.1; // height where the leg centre lines meet

function aFrame(x) {
  const els = [];
  const side = x > 0 ? 'east' : 'west';
  // each leg stands on its foot (pivot sunk just into the foot top) and leans inward at 22.5
  // degrees; both reach the centre line (z 8) together at the apex, so they meet at a point and
  // never cross. The back leg is a touch narrower so their sides never coincide.
  for (const [zf, angle, hw] of [[FEET[0], 22.5, 1.5], [FEET[1], -22.5, 1.3]]) {
    const base = FOOT_TOP - 0.6;
    const rot = { origin: [x, base, zf], axis: 'x', angle };
    els.push(
      box([x - hw, base, zf - 1.5], [x + hw, base + LEG, zf + 1.5], '#post', { rot }),
      box([x - hw - 0.25, base + 4, zf - 1.75], [x + hw + 0.25, base + 5.5, zf + 1.75], '#iron', { rot, skip: ['up', 'down'] }), // iron band
    );
  }
  // iron feet with gold rivets, and the base beam between them
  for (const zf of FEET) {
    els.push(box([x - 2, 0, zf - 1.5], [x + 2, FOOT_TOP, zf + 1.5], '#iron'));
    els.push(rivet(side, x + (x > 0 ? 2 : -2), 1.5, zf));
  }
  els.push(rivet('north', x, 1.5, FEET[0] - 1.5), rivet('south', x, 1.5, FEET[1] + 1.5));
  els.push(box([x - 1.25, 0.5, FEET[0] + 1.5], [x + 1.25, 2.5, FEET[1] - 1.5], '#wood_z'));
  // the leg pair's apex: a short post up from where the legs meet, under a small iron cap
  els.push(box([x - 1.4, APEX_Y - 1.5, 6.3], [x + 1.4, 24, 9.7], '#post'));
  els.push(box([x - 2, 24, 6], [x + 2, 27.5, 10], '#iron'));
  els.push(rivet('north', x, 25.75, 6), rivet('south', x, 25.75, 10), rivet(side, x + (x > 0 ? 2 : -2), 25.75, 8));
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
  // neighbouring bundles' leaves can share a plane, so the four are separated together
  return separateCoplanar(HOOKS.flatMap((x, i) => bundle(BUNDLES[i], { cx: x, cz: 8, top: BEAM_Y[0] - 1.75, length: 1.1, seed: 50 + i })));
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
