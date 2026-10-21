// The herb bed (owner references: reference-herb-bed-lavender.png, reference-joined-bed-2x2.png):
// a warm reddish-brown planter, two horizontal plank rails per side with a groove between them,
// chunky square corner posts rising just above the rails with dark end-grain caps and small gold
// rivets, and dark lumpy soil just below the rim. Parts are authored for the north side and the
// north-east corner; the multipart blockstate turns them (data/client/botany/garden/). Joined sides
// show soil instead of a rail, so beds side by side read as one planter.
import { box, turnY } from '../geometry.mjs';
import { MATERIAL } from '../textures.mjs';

/** Soil height: herb models root two pixels below their block, i.e. exactly at this surface. */
export const SOIL_Y = 14;
const RAIL_Z = [0.75, 2.5];
const SOIL_ONLY_TOP = { skip: ['down', 'north', 'south', 'east', 'west'] };

/** The two-rail wall section along the north side, between x0 and x1. */
function rail(x0, x1) {
  const [z0, z1] = RAIL_Z;
  return [
    box([x0, 0, z0 + 0.25], [x1, 1, z1], '#wood', { skip: ['up'] }), // recessed skirt
    box([x0, 1, z0], [x1, 7.25, z1], '#wood'),
    box([x0, 7.25, z0 + 0.75], [x1, 8.25, z1], '#wood_end', { skip: ['down', 'up'] }), // the groove
    box([x0, 8.25, z0], [x1, 15, z1], '#wood'),
  ];
}

/** The capped corner post at the north-east corner, with gold rivets on its two outer faces. */
function post() {
  return [
    box([13.25, 0, 0.25], [15.75, 15, 2.75], '#post'),
    box([13, 15, 0], [16, 16, 3], '#cap'),
    box([14, 11, 0], [15, 12, 0.25], '#gold'),
    box([14, 4, 0], [15, 5, 0.25], '#gold'),
    box([15.75, 11, 1], [16, 12, 2], '#gold'),
    box([15.75, 4, 1], [16, 5, 2], '#gold'),
  ];
}

const soil = {
  core: () => [box([2, 0, 2], [14, SOIL_Y, 14], '#soil', SOIL_ONLY_TOP)],
  edge: () => [box([2, 0, 0], [14, SOIL_Y, 2], '#soil', SOIL_ONLY_TOP)],
  corner: () => [box([14, 0, 0], [16, SOIL_Y, 2], '#soil', SOIL_ONLY_TOP)],
};

const WOOD = { wood: MATERIAL.wood, wood_end: MATERIAL.wood_end, post: MATERIAL.wood_post, cap: MATERIAL.wood_end, gold: MATERIAL.gold };
const SOIL = { soil: 'herbalwitch:block/botany/garden/soil_dry' };

export function bed() {
  const parts = 'block/botany/garden/parts';
  const wood = (name, elements) => ({ path: `${parts}/wood/${name}`, textures: { ...WOOD, particle: MATERIAL.wood }, elements });
  const out = [
    wood('rail', rail(2, 14)),
    wood('rail_end', rail(14, 16)),
    wood('rail_start', rail(0, 2)),
    wood('post', post()),
    ...Object.entries(soil).map(([name, make]) => ({ path: `${parts}/soil/${name}`, textures: { ...SOIL, particle: SOIL.soil }, elements: make() })),
  ];
  // the unjoined bed (its item, and the shape a lone bed shows): core soil, four rails, four posts
  const assembly = [...soil.core()];
  for (let q = 0; q < 4; q++) assembly.push(...turnY([...rail(2, 14), ...post()], q));
  out.push({ path: 'block/botany/garden/herb_bed', textures: { ...WOOD, ...SOIL, particle: MATERIAL.wood }, elements: assembly });
  return out;
}
