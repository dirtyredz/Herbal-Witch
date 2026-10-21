// Model JSON serialisation shared by the two model producers: the procedural generator
// (tools/generate-models.mjs) and the Blockbench compiler (tools/compile-blockbench.mjs). Both hand
// over elements whose faces already carry explicit UVs ({uv, texture, cullface?, rotation?}); this
// module only rounds numbers, fixes key order and binds texture variables, so the two producers
// write the same JSON style.

export const FACES = ['down', 'up', 'north', 'south', 'west', 'east'];

/** Rounds to 4 decimals (and turns -0 into 0), the precision every model file is written at. */
export const r4 = (v) => Math.round(v * 10000) / 10000 + 0;

/** Elements with explicit faces as JSON objects: stable key order, rounded numbers. */
export function serializeElements(elements) {
  return elements.map((e) => {
    const faces = {};
    for (const f of FACES) {
      const face = e.faces[f];
      if (!face) continue;
      if (!Array.isArray(face.uv) || face.uv.length !== 4) throw new Error(`${f} face has no explicit uv`);
      const out = { uv: face.uv.map(r4) };
      if (face.rotation) out.rotation = face.rotation;
      out.texture = face.texture;
      if (face.cullface) out.cullface = face.cullface;
      faces[f] = out;
    }
    const el = { from: e.from.map(r4), to: e.to.map(r4) };
    if (e.rotation) el.rotation = { angle: e.rotation.angle, axis: e.rotation.axis, origin: e.rotation.origin.map(r4), ...(e.rotation.rescale ? { rescale: true } : {}) };
    if (e.shade === false) el.shade = false;
    if (e.forge_data) el.forge_data = e.forge_data;
    el.faces = faces;
    return el;
  });
}

/** Texture variables (without '#') the elements' faces use. */
export function usedVariables(elements) {
  return new Set(elements.flatMap((e) => Object.values(e.faces).map((f) => f.texture.slice(1))));
}

/**
 * A model JSON object. `textures` maps variables to texture ids (only used ones and `particle` are
 * written); cutout when `cutout`; `open` lists variables a template deliberately leaves for its
 * children to bind.
 */
export function modelJson({ textures, elements, cutout = false, parent = null, display = null, ao = true, open = [] }) {
  const used = usedVariables(elements);
  for (const v of used) if (!textures[v] && !open.includes(v)) throw new Error(`texture variable #${v} is not bound`);
  const bound = Object.fromEntries(Object.entries(textures).filter(([k]) => used.has(k) || k === 'particle'));
  return {
    ...(parent ? { parent } : {}),
    ...(ao ? {} : { ambientocclusion: false }),
    ...(cutout ? { render_type: 'minecraft:cutout' } : {}),
    textures: bound,
    ...(display ? { display } : {}),
    elements: serializeElements(elements),
  };
}

/** The bytes a model file is written as. */
export const modelText = (json) => JSON.stringify(json, null, 2) + '\n';
