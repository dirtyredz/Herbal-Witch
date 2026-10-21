// The two heated vessels, sister pieces in the house style: dark plum bodies built from stacked
// octagonal layers and painted with gold moons, stars and pink flowers on olive vines, beaded
// copper-and-gold rims, gold knobs and studs. Both stand on heat (a stove burner, a hearth, a
// campfire) with their feet at y = 0. One block each.
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
    // body: a narrow foot ring, swelling to a wide painted belly, drawing in at the shoulder
    ...octagon(C, C, 0, 1, 8, 8, 2, '#plum', { third: false }),
    ...octagonTrue(C, C, 1, 2.5, 10.5, 10.5, 3, BODY, { skip: ['down'] }),
    ...octagonTrue(C, C, 2.5, 8, 12.5, 12.5, 3.5, BODY, { skip: ['down'] }),
    ...octagonTrue(C, C, 8, 9.5, 11, 11, 3, BODY, { skip: ['down'] }),
    // the beaded copper rim at the lid seam, studded with gold
    ...octagon(C, C, 9.5, 10.5, 10, 10, 2.5, '#copper', { skip: ['down'] }),
    ...studs(10, 9.5, 1.25, 2),
    // stepped domed lid with gold studs and an octagonal gold knob
    ...octagon(C, C, 10.5, 11.75, 8, 8, 2, PLAIN_LID, { skip: ['down'] }),
    ...octagon(C, C, 11.75, 12.5, 5.5, 5.5, 1.5, PLAIN_LID, { skip: ['down'], third: false }),
    ...studs(8, 10.75, 0.75),
    box([7.25, 12.5, 7.25], [8.75, 13, 8.75], '#plum'),
    ...octagon(C, C, 13, 14.25, 3, 3, 0.75, '#gold', { third: false }),
  ];
  // the spout: a plum tube rising from the front of the belly, a gold collar and square nozzle
  const tilt = { origin: [8, 5, 2], axis: 'x', angle: -45 };
  els.push(
    box([7, 4, 0.75], [9, 10.5, 2.75], '#plum', { rot: tilt }),
    box([6.75, 6, 0.5], [9.25, 7, 3], '#gold', { rot: tilt, skip: ['up', 'down'] }),
    box([6.5, 10.5, 0.25], [9.5, 11.75, 3.25], '#gold', { rot: tilt }),
    box([7.25, 11.75, 1], [8.75, 11.8, 2.5], '#iron', { rot: tilt, skip: ['down', 'north', 'south', 'east', 'west'] }),
  );
  // the arched copper handle over the top, gold bands on its uprights and gold elbow caps
  for (const [x0, x1] of [[1.25, 2.75], [13.25, 14.75]]) {
    els.push(
      box([x0, 7, 7.25], [x1, 14.5, 8.75], '#copper'),
      box([x0 - 0.25, 8.5, 7], [x1 + 0.25, 9.5, 9], '#gold', { skip: ['up', 'down'] }),
      box([x0 - 0.25, 11, 7], [x1 + 0.25, 12, 9], '#gold', { skip: ['up', 'down'] }),
      box([x0 - 0.25, 14, 7], [x1 + 0.25, 16, 9], '#gold'),
    );
  }
  els.push(
    box([2.75, 14.5, 7.25], [13.25, 16, 8.75], '#copper'),
    box([2.75, 7.5, 7.5], [3.75, 8.5, 8.5], '#copper'),
    box([12.25, 7.5, 7.5], [13.25, 8.5, 8.5], '#copper'),
  );
  return [{ path: 'block/stations/kettle/base', textures: TEXTURES, elements: els }];
}

export function infusionPot() {
  const els = [
    // short gold feet
    ...[[4, 4], [12, 4], [4, 12], [12, 12]].map(([x, z]) => box([x - 1, 0, z - 1], [x + 1, 1.25, z + 1], '#gold')),
    // squat round body with a banded base and a banded rim at the lid seam
    ...octagonTrue(C, C, 1.25, 2.75, 12.5, 12.5, 3.5, { side: '#copper', top: '#plum', bottom: '#plum' }),
    ...octagonTrue(C, C, 2.75, 9.25, 13, 13, 3.75, BODY, { skip: ['down'] }),
    ...octagonTrue(C, C, 9.25, 10.75, 14, 14, 4, { side: '#copper', top: '#plum', bottom: '#plum' }, { skip: ['down'] }),
    ...studs(14, 9.25, 1.5),
    // domed lid with the house motifs on top and a gold knob on a short post
    ...octagonTrue(C, C, 10.75, 12, 12, 12, 3.5, LID, { skip: ['down'] }),
    ...octagon(C, C, 12, 12.75, 8, 8, 2.25, LID, { skip: ['down'] }),
    box([7.25, 12.75, 7.25], [8.75, 13.75, 8.75], '#copper'),
    box([6.5, 13.75, 6.5], [9.5, 15.25, 9.5], '#gold'),
    // copper loop handles with gold caps, left and right
    box([0.25, 7.5, 6], [1.75, 8.75, 10], '#copper'),
    box([0.25, 8.75, 6], [1.75, 9.5, 7], '#copper'),
    box([0.25, 8.75, 9], [1.75, 9.5, 10], '#copper'),
    box([1, 7, 5.25], [2.25, 10, 6.5], '#gold'),
    box([1, 7, 9.5], [2.25, 10, 10.75], '#gold'),
    box([14.25, 7.5, 6], [15.75, 8.75, 10], '#copper'),
    box([14.25, 8.75, 6], [15.75, 9.5, 7], '#copper'),
    box([14.25, 8.75, 9], [15.75, 9.5, 10], '#copper'),
    box([13.75, 7, 5.25], [15, 10, 6.5], '#gold'),
    box([13.75, 7, 9.5], [15, 10, 10.75], '#gold'),
    // the small gold plaque on the east side
    box([14.5, 4.5, 6.75], [14.75, 6.5, 9.25], '#gold'),
  ];
  return [{ path: 'block/stations/infusion_pot/base', textures: TEXTURES, elements: els }];
}

