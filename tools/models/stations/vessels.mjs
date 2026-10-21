// The two heated vessels, sister pieces in the house style: dark plum bodies built from stacked
// octagonal layers and painted with gold moons, stars and pink flowers on olive vines, beaded
// copper-and-gold rims, gold knobs and studs. Both stand on heat (a stove burner, a hearth, a
// campfire) with their feet at y = 0. One block each, sized modestly (about 9 px across and 11
// tall, the owner's ruling for stove-top pieces) so a vessel reads as a pot on a stove, not a block.
//   Kettle (reference-kettle-front/angle.png): bulbous body, stepped domed lid, gold octagonal
//   knob, an arched copper handle with gold bands, an angled spout with a gold square nozzle.
//   Infusion pot (reference-infusion-pot.png): a round squat pot, domed lid with a gold knob on a
//   short post, banded rims at the lid seam and near the base, copper loop handles with gold caps,
//   gold clasps, short gold feet and a small gold plaque on one side.
import { box } from '../geometry.mjs';
import { octagon, octagonTrue } from '../shapes.mjs';
import { STATION } from '../textures.mjs';

const C = 8;
const BODY = { side: '#painted', top: '#plum', bottom: '#plum' };
const LID = { side: '#plum', top: '#lid', bottom: '#plum' };
const PLAIN_LID = { side: '#plum', top: '#plum', bottom: '#plum' };
const TEXTURES = {
  painted: STATION.plum_painted, plum: STATION.plum, lid: STATION.plum_lid, gold: STATION.gold,
  copper: STATION.copper, iron: STATION.iron, particle: STATION.plum_painted,
};

/** Gold studs around an octagonal rim of width w at height y (on its 4 straight faces). */
function studs(w, y, size = 1, per = 1) {
  const out = [];
  const h = w / 2;
  for (let i = 0; i < per; i++) {
    const a = per === 1 ? 0 : (i - (per - 1) / 2) * 3;
    out.push(
      box([C + a - size / 2, y, C - h - 0.5], [C + a + size / 2, y + size, C - h + 0.5], '#gold'),
      box([C + a - size / 2, y, C + h - 0.5], [C + a + size / 2, y + size, C + h + 0.5], '#gold'),
      box([C - h - 0.5, y, C + a - size / 2], [C - h + 0.5, y + size, C + a + size / 2], '#gold'),
      box([C + h - 0.5, y, C + a - size / 2], [C + h + 0.5, y + size, C + a + size / 2], '#gold'),
    );
  }
  return out;
}

export function kettle() {
  const els = [
    // body: a narrow foot ring, swelling to a painted belly, drawing in at the shoulder
    ...octagon(C, C, 0, 0.5, 5.5, 5.5, 1.5, '#plum', { third: false }),
    ...octagonTrue(C, C, 0.5, 1.5, 7.5, 7.5, 2.25, BODY, { skip: ['down'] }),
    ...octagonTrue(C, C, 1.5, 5.5, 9, 9, 2.5, BODY, { skip: ['down'] }),
    ...octagonTrue(C, C, 5.5, 6.5, 8, 8, 2.25, BODY, { skip: ['down'] }),
    // the beaded copper rim at the lid seam, studded with gold
    ...octagon(C, C, 6.5, 7.25, 7.25, 7.25, 1.75, '#copper', { skip: ['down'], third: false }),
    ...studs(7.25, 6.55, 0.6),
    // stepped domed lid and an octagonal gold knob
    ...octagon(C, C, 7.25, 8.25, 5.75, 5.75, 1.5, PLAIN_LID, { skip: ['down'], third: false }),
    ...octagon(C, C, 8.25, 8.75, 4, 4, 1, PLAIN_LID, { skip: ['down'], third: false }),
    box([7.4, 8.75, 7.4], [8.6, 9.1, 8.6], '#plum'),
    ...octagon(C, C, 9.1, 10, 2.25, 2.25, 0.5, '#gold', { third: false }),
  ];
  // the spout: a plum tube rising from the front of the belly, a gold collar and square nozzle
  const tilt = { origin: [8, 3.5, 3.75], axis: 'x', angle: -45 };
  els.push(
    box([7.25, 2.5, 2.75], [8.75, 7, 4.25], '#plum', { rot: tilt }),
    box([7.05, 4, 2.55], [8.95, 4.75, 4.45], '#gold', { rot: tilt, skip: ['up', 'down'] }),
    box([7.1, 7, 2.6], [8.9, 7.75, 4.4], '#gold', { rot: tilt }),
  );
  // the arched copper handle over the top, gold bands on its uprights and gold caps at the elbows
  for (const [x0, x1] of [[2.6, 3.6], [12.4, 13.4]]) {
    els.push(
      box([x0, 4.5, 7.5], [x1, 10.25, 8.5], '#copper'),
      box([x0 - 0.2, 6.5, 7.3], [x1 + 0.2, 7.25, 8.7], '#gold', { skip: ['up', 'down'] }),
      box([x0 - 0.2, 10, 7.3], [x1 + 0.2, 11.4, 8.7], '#gold'),
    );
  }
  els.push(
    box([3.8, 10.25, 7.5], [12.2, 11.25, 8.5], '#copper'),
    box([3.6, 4.6, 7.6], [4.1, 5.35, 8.4], '#copper'),
    box([11.9, 4.6, 7.6], [12.4, 5.35, 8.4], '#copper'),
  );
  return [{ path: 'block/stations/kettle/base', textures: TEXTURES, elements: els }];
}

export function infusionPot() {
  const els = [
    // short gold feet
    ...[[5.25, 5.25], [10.75, 5.25], [5.25, 10.75], [10.75, 10.75]].map(([x, z]) => box([x - 0.75, 0, z - 0.75], [x + 0.75, 1, z + 0.75], '#gold')),
    // squat round body: a banded base, the painted belly, a heavy banded rim at the lid seam
    ...octagonTrue(C, C, 1, 1.75, 8.5, 8.5, 2.5, { side: '#copper', top: '#plum', bottom: '#plum' }),
    ...octagonTrue(C, C, 1.75, 2.25, 9.25, 9.25, 2.75, { side: '#gold', top: '#plum', bottom: '#plum' }, { skip: ['down'] }),
    ...octagonTrue(C, C, 2.25, 6.25, 9.5, 9.5, 2.75, BODY, { skip: ['down'] }),
    ...octagonTrue(C, C, 6.25, 6.75, 10, 10, 3, { side: '#gold', top: '#plum', bottom: '#plum' }, { skip: ['down'] }),
    ...octagonTrue(C, C, 6.75, 7.75, 10.5, 10.5, 3, { side: '#copper', top: '#plum', bottom: '#plum' }, { skip: ['down'] }),
    // domed lid painted with the house motifs, a gold knob on a short post
    ...octagonTrue(C, C, 7.75, 8.75, 9, 9, 2.5, LID, { skip: ['down'] }),
    ...octagon(C, C, 8.75, 9.25, 4.5, 4.5, 1.25, PLAIN_LID, { skip: ['down'], third: false }),
    box([7.4, 9.25, 7.4], [8.6, 10, 8.6], '#copper'),
    box([6.85, 10, 6.85], [9.15, 11, 9.15], '#gold'),
  ];
  // gold clasps on the rim's four straight faces, standing proud of it
  for (const [x0, z0, x1, z1] of [[7.25, 2.6, 8.75, 2.85], [7.25, 13.15, 8.75, 13.4], [2.6, 7.25, 2.85, 8.75], [13.15, 7.25, 13.4, 8.75]]) {
    els.push(box([x0, 6.4, z0], [x1, 8.1, z1], '#gold'));
  }
  // copper loop handles with gold caps, left and right, clear of the body's faces
  for (const [x0, x1] of [[1.6, 2.6], [13.4, 14.4]]) {
    els.push(
      box([x0, 7.75, 6.25], [x1, 8.75, 9.75], '#copper'),
      box([x0, 6, 6.25], [x1, 7.75, 7.1], '#copper'),
      box([x0, 6, 8.9], [x1, 7.75, 9.75], '#copper'),
    );
  }
  // the small gold plaque on the east side
  els.push(box([12.75, 3.25, 7], [12.95, 5.25, 9], '#gold'));
  return [{ path: 'block/stations/infusion_pot/base', textures: TEXTURES, elements: els }];
}
