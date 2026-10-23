// Review scenes (each rendered at its own scale, default 8 px per model pixel): small arrangements that show what single-model renders cannot — beds joining,
// herbs touching soil, vessels resting on heat, multiblocks assembled, furniture together. Entries
// use real blockstates; vanilla blocks are labelled proxies. Each scene may name an owner
// reference image (in the job folder) for a side-by-side comparison.

import { HERB_LIST } from '../../art/content/botany/herbs.mjs';
import { FAMILIAR_EQUIPMENT_SCENES } from './familiars/equipment.mjs';
import { FAMILIAR_CREATURE_SCENES } from './familiars/creatures.mjs';
import { FAMILIAR_HOME_SCENES } from './familiars/homes.mjs';

import {TOOL_SCENES} from './magic/tools.mjs';
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
      // an inner corner: both sides joined but the bed on the diagonal missing (the inside of an L, U or ring)
      inner_north_east: has.has(`${x},${z - 1}`) && has.has(`${x + 1},${z}`) && !has.has(`${x + 1},${z - 1}`),
      inner_east_south: has.has(`${x + 1},${z}`) && has.has(`${x},${z + 1}`) && !has.has(`${x + 1},${z + 1}`),
      inner_south_west: has.has(`${x},${z + 1}`) && has.has(`${x - 1},${z}`) && !has.has(`${x - 1},${z + 1}`),
      inner_west_north: has.has(`${x - 1},${z}`) && has.has(`${x},${z - 1}`) && !has.has(`${x - 1},${z - 1}`),
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
const bundleBlock = (group, herb) => `${HW}preparations/herbs/${group}/${herb}/bundle`;

/** Every herb's four ages, youngest on the left, on one joined row of beds. */
const growthRows = HERB_LIST.map(([group, herb]) => ({
  name: `growth-${herb}`,
  view: 'iso',
  scale: 10,
  entries: [
    ...beds([[0, 0], [1, 0], [2, 0], [3, 0]], { moisture: 7 }),
    ...[0, 1, 2, 3].map((age) => ({ at: [3 - age, 1, 0], block: plant(group, herb), props: { age } })),
  ],
}));

export const SCENES = [
  ...TOOL_SCENES,
  ...FAMILIAR_HOME_SCENES, ...FAMILIAR_CREATURE_SCENES, ...FAMILIAR_EQUIPMENT_SCENES,
  {
    name: 'garden-joined-2x2',
    reference: 'reference-joined-bed-2x2.png',
    view: 'iso',
    scale: 11,
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
    scale: 16,
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
    // the inside corners of joined shapes (owner feedback 0.2.0-dev.6): closed with the rails' detail, no notch
    name: 'garden-inner-corners',
    view: 'iso',
    scale: 12,
    entries: [
      ...beds([[0, 0], [1, 0], [0, 1]]), // L (a 2 x 2 with one bed missing)
      ...beds([[3, 0], [5, 0], [3, 1], [4, 1], [5, 1]]), // U
      ...beds([[0, 3], [1, 3], [0, 4], [1, 4]]), // 2 x 2: no inside corner
      ...beds([[3, 3], [4, 3], [5, 3], [4, 4], [4, 5]]), // T: two inside corners
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
  { name: 'mortar', reference: 'reference-mortar.png', view: 'iso', scale: 18, entries: [{ at: [0, 0, 0], block: station('mortar'), props: { facing: 'north' } }] },
  {
    name: 'kettle',
    reference: 'reference-kettle-angle.png',
    view: 'isoWest',
    scale: 18,
    entries: [{ at: [0, 0, 0], block: station('kettle'), props: { facing: 'north' } }],
  },
  {
    name: 'infusion-pot',
    reference: 'reference-infusion-pot.png',
    view: 'iso',
    scale: 18,
    entries: [{ at: [0, 0, 0], block: station('infusion_pot'), props: { facing: 'north' } }],
  },
  {
    name: 'drying-rack',
    reference: 'reference-drying-rack.png',
    view: 'isoWest',
    entries: multiblock(station('drying_rack'), RACK, [1, 0, 0], { facing: 'north', active: true }),
  },
  {
    name: 'hearth',
    reference: 'reference-hearth.png',
    view: 'isoWest',
    entries: multiblock(station('hearth'), HEARTH, [1, 0, 0], { facing: 'north', lit: true }),
  },
  {
    name: 'stove',
    reference: 'reference-stove-pot.png',
    view: 'isoWest',
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
    name: 'armchair',
    reference: 'reference-armchair.png',
    view: 'iso',
    scale: 18,
    entries: [{ at: [0, 0, 0], block: `${HW}decor/seating/armchair`, props: { facing: 'north' } }],
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
  {
    // the grimoire's research pieces in a little study, against one wall: two witch's bookshelves,
    // the grimoire stand pulled forward, the armchair, and a lit lamp on the counter
    name: 'grimoire-study',
    view: 'iso',
    scale: 12,
    entries: [
      { at: [0, 0, 1], block: `${HW}grimoire/bookshelf`, props: { facing: 'north' } },
      { at: [1, 0, 1], block: `${HW}grimoire/bookshelf`, props: { facing: 'north' } },
      { at: [2, 0, 0], block: `${HW}grimoire/stand`, props: { facing: 'north' } },
      { at: [3, 0, 1], block: `${HW}decor/seating/armchair`, props: { facing: 'north' } },
      { at: [4, 0, 1], block: `${HW}decor/surfaces/apothecary_counter`, props: { facing: 'north' } },
      { at: [4, 1, 1], block: `${HW}decor/lighting/table_lamp`, props: { lit: true } },
    ],
  },
  {
    name: 'grimoire-stand',
    view: 'iso',
    scale: 18,
    entries: [{ at: [0, 0, 0], block: `${HW}grimoire/stand`, props: { facing: 'north' } }],
  },
  {
    name: 'grimoire-bookshelf-back',
    view: 'back',
    scale: 14,
    entries: [{ at: [0, 0, 0], block: `${HW}grimoire/bookshelf`, props: { facing: 'north' } }],
  },
];

/** Hung bundles under a ceiling and on a wall, against proxy stone, to show nothing leaks onto the host. */
const hungBundles = ['iso', 'isoWest'].map((view) => ({
  name: `bundles-hung-${view}`,
  view,
  scale: 12,
  entries: [
    { at: [0, 1, 0], proxy: 'stone' }, { at: [0, 0, 0], block: bundleBlock('temperate', 'lavender'), props: { face: 'ceiling', facing: 'north' } },
    { at: [2, 1, 0], proxy: 'stone' }, { at: [2, 0, 0], block: bundleBlock('temperate', 'chamomile'), props: { face: 'ceiling', facing: 'north' } },
    { at: [4, 0, 1], proxy: 'stone' }, { at: [4, 0, 0], block: bundleBlock('dry', 'rosemary'), props: { face: 'wall', facing: 'north' } },
    { at: [6, 0, 0], proxy: 'stone' }, { at: [5, 0, 0], block: bundleBlock('woodland', 'nightshade'), props: { face: 'wall', facing: 'west' } },
  ],
}));

SCENES.push(
  // the watered state on a joined bed (it vanished in 0.2.0-dev.1): dry beside wet
  { name: 'garden-joined-2x2-watered', view: 'iso', scale: 9, entries: [...beds([[0, 0], [1, 0], [0, 1], [1, 1]], { moisture: 7 }), ...beds([[3, 0], [4, 0], [3, 1], [4, 1]], { moisture: 0 })] },
  ...growthRows,
  ...hungBundles,
  { name: 'hearth-back', view: 'back', entries: multiblock(station('hearth'), HEARTH, [1, 0, 0], { facing: 'north', lit: false }) },
  { name: 'stove-back', view: 'back', entries: multiblock(station('stove'), STOVE, [1, 0, 0], { facing: 'north', lit: false }) },
);

export const MULTIBLOCKS = { RACK, HEARTH, STOVE };
