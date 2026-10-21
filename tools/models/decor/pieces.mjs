// The other cottage pieces, in the owner's dresser language (warm wood, black iron corner brackets
// with gold rivets, deep green drawer fronts with chunky gold knobs, a magenta runner with a gold
// moon, hanging herb bundles on gold rings) and the armchair's ivy standard:
//   apothecary counter: a one-block apothecary chest, its top full and sturdy for a lamp
//   wall shelf: the approved bottle composition kept, with gold-and-iron brackets and richer ivy
//   table lamp: a faceted magenta shade with gold bands on a dark stem (the lit lamp's shade glows)
//   small rug: the woven rug with gold tassels at each end
import { box } from '../geometry.mjs';
import { band, ivyDrape, octagon, octagonTrue, rivet } from '../shapes.mjs';
import { DECOR } from './textures.mjs';

const T = { ...DECOR, particle: DECOR.warm };

/** A chunky black-iron corner bracket at a corner of the body, with gold rivets on its two faces. */
function bracket(x, y, z, sx, sz) {
  const s = 2.5;
  const x0 = sx < 0 ? x : x - s;
  const z0 = sz < 0 ? z : z - s;
  return [
    box([x0, y, z0], [x0 + s, y + s, z0 + s], '#iron'),
    rivet(sz < 0 ? 'north' : 'south', x0 + s / 2, y + s / 2, sz < 0 ? z0 : z0 + s, '#gold', 0.75, 0.25),
    rivet(sx < 0 ? 'west' : 'east', sx < 0 ? x0 : x0 + s, y + s / 2, z0 + s / 2, '#gold', 0.75, 0.25),
  ];
}

export function counter() {
  const els = [
    box([1.5, 0, 1.5], [14.5, 1.5, 14.5], '#warm'), // recessed plinth
    box([1, 1.5, 1], [15, 13.5, 15], '#warm_v'),
    box([0, 13.5, 0], [16, 15.75, 16], '#warm'), // thick overhanging top
    // two deep green drawer fronts, inset in raised gold frames, with chunky gold knobs
    ...[[2.5, 6.5], [7.5, 12]].flatMap(([y0, y1]) => [
      box([2.5, y0, 0.75], [13.5, y1, 1], '#gold', { skip: ['south'] }),
      box([3, y0 + 0.5, 0.5], [13, y1 - 0.5, 0.75], '#paint', { skip: ['south'] }),
      box([3.5, (y0 + y1) / 2 - 1, 0], [5, (y0 + y1) / 2 + 1, 0.5], '#gold'),
      box([11, (y0 + y1) / 2 - 1, 0], [12.5, (y0 + y1) / 2 + 1, 0.5], '#gold'),
    ]),
    // gold studs along the top rail
    ...[3, 6, 10, 13].map((x) => rivet('north', x, 12.75, 1, '#gold', 0.75, 0.25)),
    // the magenta runner draped over the top and down the front
    box([6, 16, 0], [10, 16, 16], '#runner', { decal: { up: [0, 0, 16, 10] }, skip: ['down', 'north', 'south', 'east', 'west'] }),
  ];
  // the runner's front fall (a face just off the drawers)
  els.push(box([6, 6, 0], [10, 16, 0.01], '#runner', { decal: { north: [0, 0, 16, 16], south: [0, 0, 16, 16] }, skip: ['up', 'down', 'east', 'west'] }));
  // black iron corner brackets top and bottom, front and back
  for (const [x, sx] of [[1, -1], [15, 1]]) {
    for (const [z, sz] of [[1, -1], [15, 1]]) {
      els.push(...bracket(x, 1.5, z, sx, sz), ...bracket(x, 11, z, sx, sz));
    }
  }
  // a dried bundle hanging on a gold ring at the east side, and ivy over the west corner
  els.push(box([15, 11, 7.5], [15.75, 12, 8.5], '#gold'));
  els.push(box([15.4, 3.5, 5.5], [15.4, 11, 10.5], '#bundle', { decal: { west: [0, 0, 16, 16], east: [0, 0, 16, 16] }, skip: ['up', 'down', 'north', 'south'] }));
  els.push(...ivyDrape({ face: 'west', at: 0, along: [3.5, 8], top: 13.5, lengths: [9, 12], inside: true }));
  els.push(...ivyDrape({ face: 'north', at: 0, along: [13, 16], top: 13.5, lengths: [6, 10], inside: true }));
  return [{ path: 'block/decor/templates/apothecary_counter', textures: T, elements: els }];
}

export function lamp(lit) {
  const glow = lit ? { glow: true } : {};
  const els = [
    ...octagon(8, 8, 0, 1.5, 7, 7, 2, '#wood', { third: false }),
    ...octagon(8, 8, 1.5, 2.25, 5, 5, 1.5, '#gold', { third: false }),
    box([7.25, 2.25, 7.25], [8.75, 8.5, 8.75], '#wood_v'),
    band(7.25, 7.25, 8.75, 8.75, 4.5, 5.25, '#gold'),
    band(7.25, 7.25, 8.75, 8.75, 7, 7.5, '#plum'),
    // the faceted shade: wide below, drawn in at the top, gold-edged
    ...octagonTrue(8, 8, 8.5, 13, 9.5, 9.5, 2.75, '#lamp_shade', glow),
    ...octagonTrue(8, 8, 13, 15, 7, 7, 2, '#lamp_shade', glow),
    ...octagon(8, 8, 15, 15.75, 3, 3, 0.75, '#gold', { third: false }),
  ];
  els.push(...ivyDrape({ face: 'north', at: 7.25, along: [7, 9], top: 8, lengths: [5], clump: false }));
  return { path: `block/decor/templates/table_lamp_${lit ? 'on' : 'off'}`, textures: T, elements: els };
}

export function rug() {
  const els = [box([0, 0, 0], [16, 1, 16], '#rug', { decal: { all: [0, 0, 16, 16] } })];
  for (const z of [0.25, 15.25]) for (const x of [3, 6, 10, 13]) els.push(box([x - 0.5, 0, z], [x + 0.5, 0.75, z + 0.5], '#gold'));
  return [{ path: 'block/decor/templates/rug', textures: T, elements: els }];
}

export function shelf() {
  const els = [
    box([0, 7, 9], [16, 9, 16], '#wood'),
    box([1, 6, 9], [15, 7, 10], '#plum'),
    box([1, 9, 10], [15, 9.5, 15], '#pink'),
    box([0.5, 8.5, 8.75], [15.5, 9, 9], '#gold', { skip: ['south'] }),
    // wrought brackets: iron with gold rivets
    box([2, 3, 15], [3, 7, 16], '#iron'), box([13, 3, 15], [14, 7, 16], '#iron'),
    box([2, 6, 11], [3, 7, 15], '#iron'), box([13, 6, 11], [14, 7, 15], '#iron'),
    rivet('north', 2.5, 4, 15, '#gold', 0.75, 0.25), rivet('north', 13.5, 4, 15, '#gold', 0.75, 0.25),
    // the approved bottles: tall, small and round, with their tinctures, necks and caps
    box([3, 9, 11], [6, 14, 14], '#glass', { decal: { all: [0, 0, 16, 16] } }),
    box([3.5, 9.5, 11.5], [5.5, 12, 13.5], '#tincture'),
    box([4, 14, 12], [5, 15, 13], '#glass', { decal: { all: [0, 0, 16, 16] } }),
    box([4, 15, 12], [5, 16, 13], '#gold'),
    box([7.5, 9, 12], [9.5, 12, 14], '#glass', { decal: { all: [0, 0, 16, 16] } }),
    box([8, 9.5, 12.5], [9, 11, 13.5], '#tincture', { uvShift: [11, 0] }),
    box([8, 12, 12.5], [9, 13, 13.5], '#plum'),
    box([11, 9, 11], [14, 13, 14], '#glass', { decal: { all: [0, 0, 16, 16] } }),
    box([11.5, 9.5, 11.5], [13.5, 11.5, 13.5], '#tincture', { uvShift: [-1, 0] }),
    box([12, 13, 12], [13, 14, 13], '#glass', { decal: { all: [0, 0, 16, 16] } }),
    box([12, 14, 12], [13, 14.5, 13], '#gold'),
  ];
  els.push(
    ...ivyDrape({ face: 'north', at: 9, along: [0.5, 5], top: 9, lengths: [6, 9, 4] }),
    ...ivyDrape({ face: 'north', at: 9, along: [10.5, 15], top: 9, lengths: [5, 8] }),
    ...ivyDrape({ face: 'east', at: 15.5, along: [10, 15], top: 9, lengths: [7], clump: false }),
  );
  return [{ path: 'block/decor/templates/wall_shelf', textures: T, elements: els }];
}

export function decorPieces() {
  return [...counter(), ...shelf(), lamp(false), lamp(true), ...rug()];
}
