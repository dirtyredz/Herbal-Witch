// Coverage: bake every state of every blockstate and every item model, resolve every texture they
// reference, and account for every model file in the mod (abstract templates count as covered when
// a rendered model inherits from them). Anything missing or unrendered is a diagnostic, never a
// silent placeholder.
import { enumerate, select, stateLabel } from '../scenes/blockstates.mjs';
import { resolveModel, resolveTexture, isAbstract } from '../assets/models.mjs';

/**
 * Returns {states: [{block, props, models}], items: [id], errors: [string], models: {total, covered}}.
 */
export function coverage(res, baker) {
  const errors = [];
  const states = [];
  const textures = new Set();
  const check = (where, ids) => {
    for (const id of ids) {
      textures.add(id);
      if (!res.texture(id)) errors.push(`${where}: missing texture ${id}`);
    }
  };
  for (const block of res.ids('blockstates')) {
    const bs = res.json('blockstates', block);
    let combos;
    try {
      combos = enumerate(bs);
    } catch (e) {
      errors.push(`${block}: ${e.message}`);
      continue;
    }
    for (const props of combos) {
      try {
        const picks = select(bs, props);
        if (picks.length === 0) errors.push(`${block} [${stateLabel(props)}]: selects no model`);
        for (const p of picks) {
          const { quads } = baker.bake(p.model);
          const unbound = quads.filter((q) => !q.texture).map((q) => q.ref);
          if (unbound.length) errors.push(`${block} [${stateLabel(props)}] ${p.model}: unbound ${[...new Set(unbound)].join(' ')}`);
          check(`${block} ${p.model}`, quads.filter((q) => q.texture).map((q) => q.texture));
        }
        states.push({ block, props, models: picks.map((p) => p.model) });
      } catch (e) {
        errors.push(`${block} [${stateLabel(props)}]: ${e.message}`);
      }
    }
  }
  const items = res.ids('models').filter((id) => id.startsWith('herbalwitch:item/'));
  for (const id of items) {
    try {
      const { model, quads } = baker.bake(id);
      if (model.builtin === 'generated') {
        for (let l = 0; model.textures[`layer${l}`]; l++) check(id, [resolveTexture(model, `#layer${l}`)]);
      } else {
        if (quads.length === 0) errors.push(`${id}: item has no geometry`);
        check(id, quads.filter((q) => q.texture).map((q) => q.texture));
        if (quads.some((q) => !q.texture)) errors.push(`${id}: unbound texture`);
      }
    } catch (e) {
      errors.push(`${id}: ${e.message}`);
    }
  }
  const all = res.ids('models');
  let covered = 0;
  for (const id of all) {
    if (baker.used.has(id)) {
      covered++;
      continue;
    }
    let abstract = false;
    try {
      abstract = isAbstract(resolveModel(res, id));
    } catch (e) {
      errors.push(`${id}: ${e.message}`);
    }
    errors.push(abstract ? `${id}: abstract template with no rendered descendant` : `${id}: model is never rendered`);
  }
  for (const d of res.diagnostics) errors.push(`${d.kind}: ${d.message}`);
  return { states, items, errors, textures: [...textures].sort(), models: { total: all.length, covered } };
}
