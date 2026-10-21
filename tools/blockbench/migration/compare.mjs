// Appearance parity between two resource sets (a captured baseline and the current trees): every
// block model's baked faces (positions, UVs, textures after resolution, shading, glow, cullfaces),
// render type and ambient occlusion; every item model's effective display transforms (parent
// chain merged, vanilla's left-hand fallbacks applied) and geometry; every blockstate combination
// as the game assembles it; and the decoded pixels of every texture any of them uses. Element
// order, JSON formatting and where a display field is inherited from may differ; appearance may
// not. Pure comparison: it never writes.
import { Baker } from '../../preview/geometry/bake.mjs';
import { variantRotation } from '../../preview/geometry/transforms.mjs';
import { enumerate, select, stateLabel } from '../../preview/scenes/blockstates.mjs';

const k4 = (v) => (Math.round(v * 1e4) / 1e4 + 0).toFixed(4);

/** A face as an order-free key: its corners as (position, uv) pairs, sorted, plus its render state. */
function quadKey(q) {
  const corners = q.pos.map((p, i) => `${p.map(k4)}|${q.uv[i].map(k4)}`).sort().join(' ');
  return `${q.texture}|${q.shade ? 's' : 'f'}|${q.emissive ? 'g' : '-'}|${q.cullface ?? '-'}|${corners}`;
}

function multisetDiff(a, b) {
  const count = new Map();
  for (const k of a) count.set(k, (count.get(k) ?? 0) + 1);
  for (const k of b) count.set(k, (count.get(k) ?? 0) - 1);
  const onlyA = [];
  const onlyB = [];
  for (const [k, n] of count) {
    for (let i = 0; i < n; i++) onlyA.push(k);
    for (let i = 0; i < -n; i++) onlyB.push(k);
  }
  return { onlyA, onlyB };
}

const SLOTS = ['thirdperson_righthand', 'thirdperson_lefthand', 'firstperson_righthand', 'firstperson_lefthand', 'ground', 'gui', 'head', 'fixed'];

/** Display transforms as the game applies them: defaults filled, left hands falling back to right. */
export function effectiveDisplay(display) {
  const full = (d) => d && { rotation: d.rotation ?? [0, 0, 0], translation: d.translation ?? [0, 0, 0], scale: d.scale ?? [1, 1, 1] };
  const out = {};
  for (const s of SLOTS) out[s] = full(display[s]) ?? null;
  out.thirdperson_lefthand ??= out.thirdperson_righthand;
  out.firstperson_lefthand ??= out.firstperson_righthand;
  return out;
}

const displayKey = (display) => JSON.stringify(Object.fromEntries(Object.entries(effectiveDisplay(display))
  .map(([s, d]) => [s, d && Object.fromEntries(Object.entries(d).map(([k, v]) => [k, v.map(k4)]))])));

function samePixels(a, b) {
  if (!a || !b) return a === b;
  return a.width === b.width && a.height === b.height && a.rgba.every((v, i) => v === b.rgba[i]);
}

/**
 * Compares `base` and `cand` (preview Resources). Returns {checked: {models, items, states,
 * textures}, differences: [string]}.
 */
export function compareAppearance(base, cand) {
  const bb = new Baker(base);
  const cb = new Baker(cand);
  const differences = [];
  const textures = new Set();
  const checked = { models: 0, items: 0, states: 0, textures: 0 };
  const quadsOf = (baker, id) => {
    const { model, quads } = baker.bake(id);
    for (const q of quads) if (q.texture) textures.add(q.texture);
    return { model, keys: quads.map(quadKey) };
  };
  const compareQuads = (what, a, b) => {
    const { onlyA, onlyB } = multisetDiff(a, b);
    if (onlyA.length || onlyB.length) {
      differences.push(`${what}: ${onlyA.length} baseline faces and ${onlyB.length} new faces differ (first: ${onlyA[0] ?? '-'} / ${onlyB[0] ?? '-'})`);
    }
  };
  const ids = (res, kind) => new Set(res.ids(kind));
  const models = new Set([...ids(base, 'models'), ...ids(cand, 'models')]);
  for (const id of [...models].sort()) {
    let a;
    let b;
    try {
      a = quadsOf(bb, id);
      b = quadsOf(cb, id);
    } catch (e) {
      differences.push(`${id}: ${e.message}`);
      continue;
    }
    const item = id.startsWith('herbalwitch:item/');
    if (a.model.builtin !== b.model.builtin) differences.push(`${id}: builtin ${a.model.builtin} -> ${b.model.builtin}`);
    if (a.model.builtin === 'generated') {
      if (a.model.textures.layer0 !== b.model.textures.layer0) differences.push(`${id}: layer0 differs`);
    }
    compareQuads(id, a.keys, b.keys);
    if ((a.model.renderType ?? null) !== (b.model.renderType ?? null)) differences.push(`${id}: render type ${a.model.renderType} -> ${b.model.renderType}`);
    if (a.model.ao !== b.model.ao) differences.push(`${id}: ambient occlusion ${a.model.ao} -> ${b.model.ao}`);
    if (item) {
      if (displayKey(a.model.display) !== displayKey(b.model.display)) differences.push(`${id}: effective display transforms differ`);
      checked.items++;
    }
    checked.models++;
  }
  const states = new Set([...ids(base, 'blockstates'), ...ids(cand, 'blockstates')]);
  for (const block of [...states].sort()) {
    const sa = base.json('blockstates', block);
    const sb = cand.json('blockstates', block);
    if (!sa || !sb) {
      differences.push(`${block}: blockstate ${sa ? 'removed' : 'added'}`);
      continue;
    }
    for (const props of enumerate(sa)) {
      const bake = (baker, bs) => select(bs, props).flatMap((p) => variantRotation(baker.bake(p.model).quads, p)).map(quadKey);
      try {
        compareQuads(`${block} [${stateLabel(props)}]`, bake(bb, sa), bake(cb, sb));
      } catch (e) {
        differences.push(`${block} [${stateLabel(props)}]: ${e.message}`);
      }
      checked.states++;
    }
  }
  for (const t of [...textures].sort()) {
    if (!samePixels(base.texture(t), cand.texture(t))) differences.push(`texture ${t}: pixels differ`);
    checked.textures++;
  }
  for (const d of [...base.diagnostics, ...cand.diagnostics]) differences.push(`${d.kind}: ${d.message}`);
  return { checked, differences };
}
