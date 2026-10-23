// Assembling scenes: blocks at cell positions, each drawn through its real blockstate (or a model
// directly), plus labelled vanilla proxies (flat-coloured cubes standing in for grass, dirt,
// farmland and stone: their heights are real, their textures are not). Faces with a cullface
// against a full proxy cube are culled as the game would.
import {creatureQuads} from './familiars/creatures.mjs';
import { select, enumerate } from './blockstates.mjs';
import { variantRotation, translate, displayTransform } from '../geometry/transforms.mjs';
import { bakeElement } from '../geometry/elements.mjs';

/** Proxy blocks: [height in model pixels, top colour, side colour]. */
export const PROXIES = {
  grass: [16, [96, 140, 64], [118, 88, 62]],
  dirt: [16, [118, 88, 62], [110, 82, 58]],
  farmland: [15, [82, 56, 38], [110, 82, 58]],
  stone: [16, [124, 124, 124], [112, 112, 112]],
  smooth_stone: [16, [158, 158, 158], [146, 146, 146]],
};

/** Synthetic proxy textures ('proxy:<name>/<top|side>'): flat colour with a light checker. */
export function proxyTexture(id) {
  const m = /^proxy:(\w+)\/(top|side)$/.exec(id);
  if (!m || !PROXIES[m[1]]) return null;
  const c = PROXIES[m[1]][m[2] === 'top' ? 1 : 2];
  const rgba = new Uint8Array(4 * 4 * 4);
  for (let i = 0; i < 16; i++) {
    const f = ((i % 4) + (i >> 2)) % 2 ? 1 : 0.92;
    rgba.set([Math.round(c[0] * f), Math.round(c[1] * f), Math.round(c[2] * f), 255], i * 4);
  }
  return { width: 4, height: 4, rgba };
}

function proxyQuads(name) {
  const [h] = PROXIES[name];
  const faces = {};
  for (const f of ['down', 'up', 'north', 'south', 'west', 'east']) {
    faces[f] = { texture: `#${f === 'up' ? 'top' : 'side'}`, cullface: f === 'up' && h < 16 ? undefined : f };
  }
  return bakeElement({ from: [0, 0, 0], to: [16, h, 16], faces }, (ref) => `proxy:${name}/${ref.slice(1)}`);
}

/** A complete default state for a blockstate file: its first enumerated state, facing north. */
export function defaults(bs) {
  const first = enumerate(bs)[0] ?? {};
  return 'facing' in first ? { ...first, facing: 'north' } : first;
}

const NEIGHBOUR = { north: [0, 0, -1], south: [0, 0, 1], west: [-1, 0, 0], east: [1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] };

/**
 * Quads for a scene: entries [{at: [x,y,z], block?: id, props?: {}, model?: id, y?: rot, proxy?: name}].
 * `baker` bakes models, `res` reads blockstates. Returns {quads, used: [{block, props, models}]}.
 */
export function sceneQuads(entries, baker, res) {
  const full = new Set(entries.filter((e) => e.proxy && PROXIES[e.proxy][0] === 16).map((e) => e.at.join(',')));
  const quads = [];
  const used = [];
  for (const e of entries) {
    let local;
    if (e.creature) local = creatureQuads(e, res, baker);
    else if (e.proxy) local = proxyQuads(e.proxy);
    else if (e.model) {
      const baked = baker.bake(e.model);
      if (e.display && !baked.model.display[e.display]) throw new Error('Missing display transform ' + e.display);
      local = e.display ? displayTransform(baked.quads, baked.model.display[e.display]) : variantRotation(baked.quads, {y:e.y ?? 0});
    }
    else {
      const bs = res.json('blockstates', e.block);
      if (!bs) throw new Error(`missing blockstate ${e.block}`);
      const props = { ...defaults(bs), ...Object.fromEntries(Object.entries(e.props ?? {}).map(([k, v]) => [k, String(v)])) };
      const picks = select(bs, props);
      used.push({ block: e.block, props, models: picks.map((p) => p.model) });
      local = picks.flatMap((p) => variantRotation(baker.bake(p.model).quads, p));
    }
    for (const q of translate(local, e.at)) {
      if (q.cullface) {
        const d = NEIGHBOUR[q.cullface];
        if (full.has([e.at[0] + d[0], e.at[1] + d[1], e.at[2] + d[2]].join(','))) continue;
      }
      quads.push(q);
    }
  }
  return { quads, used };
}
