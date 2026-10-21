// Review scenes: small arrangements that show what single-model renders cannot — beds joining,
// herbs touching soil, vessels resting on heat, multiblocks assembled, furniture together. Entries
// use real blockstates; vanilla blocks are labelled proxies. Each scene may name an owner
// reference image (in the job folder) for a side-by-side comparison.

const HW = 'herbalwitch:';
const BED = `${HW}botany/garden/herb_bed`;
const plant = (group, herb) => `${HW}botany/${group}/${herb}/plant`;
const station = (name) => `${HW}stations/${name}/station`;

/** Beds at cells with connections derived from their neighbours (the game's rule: same block, same height). */
export function beds(cells, props = {}, y = 0) {
  const has = new Set(cells.map(([x, z]) => `${x},${z}`));
  return cells.map(([x, z]) => ({
    at: [x, y, z],
    block: BED,
    props: {
      ...props,
      north: has.has(`${x},${z - 1}`),
      south: has.has(`${x},${z + 1}`),
      west: has.has(`${x - 1},${z}`),
      east: has.has(`${x + 1},${z}`),
    },
  }));
}

/** A multiblock's cells, as the game places them: part index i at origin + right * dx + up * dy. */
export function multiblock(block, cells, at, props = {}) {
  // facing north: the viewer stands north, so "right" (from the viewer) is -x (west).
  return cells.map(([dx, dy], part) => ({ at: [at[0] - dx, at[1] + dy, at[2]], block, props: { ...props, part } }));
}

const RACK = [[0, 0], [1, 0], [0, 1], [1, 1]];
const HEARTH = [[0, 0], [1, 0], [0, 1], [1, 1]];
const STOVE = [[0, 0], [1, 0]];

export const SCENES = [
  {
    name: 'garden-joined-2x2',
    reference: 'reference-joined-bed-2x2.png',
    view: 'iso',
    entries: [
      ...beds([[0, 0], [1, 0], [0, 1], [1, 1]]),
      { at: [1, 1, 0], block: plant('temperate', 'chamomile'), props: { age: 3 } },
      { at: [0, 1, 0], block: plant('temperate', 'lavender'), props: { age: 3 } },
      { at: [1, 1, 1], block: plant('woodland', 'mandrake'), props: { age: 3 } },
      { at: [0, 1, 1], block: plant('dry', 'thyme'), props: { age: 3 } },
    ],
  },
  {
    name: 'garden-single-lavender',
    reference: 'reference-herb-bed-lavender.png',
    view: 'iso',
    entries: [...beds([[0, 0]], { moisture: 7 }), { at: [0, 1, 0], block: plant('temperate', 'lavender'), props: { age: 3 } }],
  },
  {
    name: 'garden-shapes',
    view: 'iso',
    entries: [
      ...beds([[0, 0], [1, 0], [2, 0]]), // line
      ...beds([[4, 0], [5, 0], [4, 1]]), // L
      ...beds([[7, 0], [8, 0], [9, 0], [8, 1]]), // T
      ...beds([[0, 3], [1, 3], [2, 3], [0, 4], [2, 4], [0, 5], [1, 5], [2, 5]]), // ring with an empty centre
      ...beds([[4, 3]], { moisture: 0 }),
      ...beds([[6, 3]], { moisture: 7 }),
      { at: [8, 0, 3], proxy: 'farmland' },
      { at: [8, 1, 3], block: plant('temperate', 'sage'), props: { age: 3 } },
      { at: [9, 0, 3], proxy: 'grass' },
      { at: [9, 1, 3], block: plant('wet', 'mint'), props: { age: 3 } },
    ],
  },
  {
    name: 'garden-growth',
    view: 'iso',
    entries: [
      ...beds([[0, 0], [1, 0], [2, 0], [3, 0]]),
      ...[0, 1, 2, 3].map((age) => ({ at: [3 - age, 1, 0], block: plant('woodland', 'belladonna'), props: { age } })),
    ],
  },
  { name: 'mortar', reference: 'reference-mortar.png', view: 'iso', entries: [{ at: [0, 0, 0], block: station('mortar'), props: { facing: 'north' } }] },
  {
    name: 'kettle',
    reference: 'reference-kettle-angle.png',
    view: 'isoWest',
    entries: [{ at: [0, 0, 0], block: station('kettle'), props: { facing: 'north' } }],
  },
  {
    name: 'infusion-pot',
    reference: 'reference-infusion-pot.png',
    view: 'iso',
    entries: [{ at: [0, 0, 0], block: station('infusion_pot'), props: { facing: 'north' } }],
  },
  {
    name: 'drying-rack',
    reference: 'reference-drying-rack.png',
    view: 'iso',
    entries: multiblock(station('drying_rack'), RACK, [1, 0, 0], { facing: 'north', active: true }),
  },
  {
    name: 'hearth',
    reference: 'reference-hearth.png',
    view: 'iso',
    entries: multiblock(station('hearth'), HEARTH, [1, 0, 0], { facing: 'north', lit: true }),
  },
  {
    name: 'stove',
    reference: 'reference-stove-pot.png',
    view: 'iso',
    entries: [
      ...multiblock(station('stove'), STOVE, [1, 0, 0], { facing: 'north', lit: true }),
      { at: [1, 1, 0], block: station('infusion_pot'), props: { facing: 'north' } },
      { at: [0, 1, 0], block: station('kettle'), props: { facing: 'north' } },
    ],
  },
  {
    name: 'stations-cold',
    view: 'iso',
    entries: [
      ...multiblock(station('drying_rack'), RACK, [1, 0, 0], { facing: 'north', active: false }),
      ...multiblock(station('hearth'), HEARTH, [4, 0, 0], { facing: 'north', lit: false }),
      ...multiblock(station('stove'), STOVE, [7, 0, 0], { facing: 'north', lit: false }),
      { at: [9, 0, 0], block: station('mortar'), props: { facing: 'north' } },
    ],
  },
  {
    name: 'cottage-furniture',
    view: 'iso',
    entries: [
      { at: [0, 0, 0], block: `${HW}decor/seating/armchair`, props: { facing: 'north' } },
      { at: [1, 0, 0], block: `${HW}decor/surfaces/apothecary_counter`, props: { facing: 'north' } },
      { at: [1, 1, 0], block: `${HW}decor/lighting/table_lamp`, props: { lit: true } },
      { at: [2, 0, 0], block: `${HW}decor/surfaces/apothecary_counter`, props: { facing: 'north' } },
      { at: [3, 0, 1], block: `${HW}decor/textiles/small_rug` },
      { at: [3, 1, 1], block: `${HW}decor/storage/small_wall_shelf`, props: { facing: 'north' } },
    ],
  },
];

export const MULTIBLOCKS = { RACK, HEARTH, STOVE };
