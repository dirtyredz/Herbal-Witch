// Model inheritance as Minecraft 1.20.1 resolves it: the nearest model in the parent chain that has
// `elements` supplies the geometry; texture variables, display transforms and the render type merge
// root-first with children overriding. The vanilla parents this mod uses are built in below
// (pinned 1.20.1 values); any other vanilla parent fails explicitly instead of rendering wrongly.
import { splitId } from './resources.mjs';

const BLOCK_DISPLAY = {
  gui: { rotation: [30, 225, 0], translation: [0, 0, 0], scale: [0.625, 0.625, 0.625] },
  ground: { rotation: [0, 0, 0], translation: [0, 3, 0], scale: [0.25, 0.25, 0.25] },
  fixed: { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [0.5, 0.5, 0.5] },
  thirdperson_righthand: { rotation: [75, 45, 0], translation: [0, 2.5, 0], scale: [0.375, 0.375, 0.375] },
  firstperson_righthand: { rotation: [0, 45, 0], translation: [0, 0, 0], scale: [0.4, 0.4, 0.4] },
  firstperson_lefthand: { rotation: [0, 225, 0], translation: [0, 0, 0], scale: [0.4, 0.4, 0.4] },
};

const CROSS_ROTATION = { origin: [8, 8, 8], axis: 'y', angle: 45, rescale: true };

/** The vanilla models this mod inherits from (minecraft 1.20.1 assets). */
export const VANILLA = {
  'minecraft:block/block': { display: BLOCK_DISPLAY },
  'minecraft:block/cross': {
    parent: 'block/block',
    ambientocclusion: false,
    textures: { particle: '#cross' },
    elements: [
      {
        from: [0.8, 0, 8], to: [15.2, 16, 8], rotation: CROSS_ROTATION, shade: false,
        faces: { north: { uv: [0, 0, 16, 16], texture: '#cross' }, south: { uv: [0, 0, 16, 16], texture: '#cross' } },
      },
      {
        from: [8, 0, 0.8], to: [8, 16, 15.2], rotation: CROSS_ROTATION, shade: false,
        faces: { west: { uv: [0, 0, 16, 16], texture: '#cross' }, east: { uv: [0, 0, 16, 16], texture: '#cross' } },
      },
    ],
  },
  'minecraft:item/generated': {
    parent: 'builtin/generated',
    display: {
      ground: { rotation: [0, 0, 0], translation: [0, 2, 0], scale: [0.5, 0.5, 0.5] },
      fixed: { rotation: [0, 180, 0], translation: [0, 0, 0], scale: [1, 1, 1] },
    },
  },
  'minecraft:builtin/generated': { builtin: 'generated' },
};

const qualify = (id) => (id.includes(':') ? id : `minecraft:${id}`);

/**
 * Resolves model `id` against `res` (a Resources). Returns
 * {id, elements, textures, display, renderType, ao, builtin, chain} or throws on a missing model,
 * a cycle or an unsupported vanilla parent / custom loader.
 */
export function resolveModel(res, id) {
  const chain = [];
  const seen = new Set();
  let next = qualify(id);
  while (next) {
    if (seen.has(next)) throw new Error(`model inheritance cycle at ${next} (from ${id})`);
    seen.add(next);
    let model;
    if (next.startsWith('minecraft:')) {
      model = VANILLA[next];
      if (!model) throw new Error(`unsupported vanilla parent ${next} (from ${id})`);
    } else {
      model = res.json('models', next);
      if (!model) throw new Error(`missing model ${next} (from ${id})`);
      if (model.loader) throw new Error(`unsupported model loader ${model.loader} in ${next}`);
    }
    chain.push({ id: next, model });
    next = model.parent ? qualify(model.parent) : null;
  }
  let elements = null;
  let renderType = null;
  let ao = true;
  let builtin = null;
  const textures = {};
  const display = {};
  for (let i = chain.length - 1; i >= 0; i--) {
    const m = chain[i].model;
    if (m.elements) elements = m.elements;
    if (m.textures) Object.assign(textures, m.textures);
    if (m.display) Object.assign(display, m.display);
    if (m.render_type) renderType = m.render_type;
    if (m.ambientocclusion === false) ao = false;
    if (m.builtin) builtin = m.builtin;
  }
  return { id: qualify(id), elements: elements ?? [], textures, display, renderType, ao, builtin, chain: chain.map((c) => c.id) };
}

/**
 * Follows a texture reference ('#var' chains) to a texture id, or null when unbound.
 * Throws on an alias cycle.
 */
export function resolveTexture(model, ref) {
  let value = ref;
  const seen = new Set();
  while (value && value.startsWith('#')) {
    if (seen.has(value)) throw new Error(`texture alias cycle ${value} in ${model.id}`);
    seen.add(value);
    value = model.textures[value.slice(1)];
  }
  if (!value) return null;
  const [ns, path] = splitId(value);
  return `${ns}:${path}`;
}

/** True when a model leaves a texture its geometry uses unbound (an abstract template). */
export function isAbstract(model) {
  for (const e of model.elements) {
    for (const f of Object.values(e.faces ?? {})) if (!resolveTexture(model, f.texture)) return true;
  }
  if (model.builtin === 'generated') return !resolveTexture(model, '#layer0');
  return false;
}
