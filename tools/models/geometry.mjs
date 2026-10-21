// Element authoring for the procedural cuboid models (herbs, herb beds, hung bundles and the
// generated attachments of Blockbench projects): boxes and planes with per-face textures,
// position-based UVs at one UV unit per model pixel (so textures run continuously across
// neighbouring elements), explicit "decal" UVs where a whole sprite is stretched over a face, legal
// one-axis rotations, cullfaces on the block boundary and buried-face removal. Pure data:
// model-output.mjs serialises, tools/generate-models.mjs writes.
import { defaultUv } from '../preview/geometry/elements.mjs';
import { FACES, serializeElements } from './model-output.mjs';

export { FACES };
const AXIS = { west: 0, east: 0, down: 1, up: 1, north: 2, south: 2 };
const POSITIVE = new Set(['east', 'up', 'south']);
const LEGAL_ANGLES = new Set([-45, -22.5, 0, 22.5, 45]);

/**
 * A cuboid. `tex` is a '#variable' for every face, or {all, side, top, bottom, north, ...}
 * (most specific wins). Options: skip (faces to leave out), rot ({axis, angle, origin}),
 * decal ({face: [u1, v1, u2, v2]} stretched UVs; 'all' for every face), glow (full block light),
 * shade (false for flat lighting), uvShift ([du, dv] added to position UVs, wrapped).
 */
export function box(from, to, tex, opts = {}) {
  const faces = {};
  for (const f of FACES) {
    if (opts.skip?.includes(f)) continue;
    const t = typeof tex === 'string' ? tex
      : tex[f] ?? (f === 'up' ? tex.top : f === 'down' ? tex.bottom : tex.side) ?? tex.all;
    if (!t) continue;
    const decal = opts.decal?.[f] ?? opts.decal?.all;
    faces[f] = { texture: t, ...(decal ? { decal } : {}) };
  }
  if (opts.rot && !LEGAL_ANGLES.has(opts.rot.angle)) throw new Error(`illegal rotation ${opts.rot.angle}`);
  return {
    from: [...from], to: [...to], faces,
    ...(opts.rot ? { rotation: { origin: [...opts.rot.origin], axis: opts.rot.axis, angle: opts.rot.angle, ...(opts.rot.rescale ? { rescale: true } : {}) } } : {}),
    ...(opts.shade === false ? { shade: false } : {}),
    ...(opts.glow ? { forge_data: { block_light: 15, sky_light: 15 } } : {}),
    ...(opts.uvShift ? { uvShift: opts.uvShift } : {}),
  };
}

/**
 * A two-sided flat plane for cutout sprites, perpendicular to `axis` ('x' or 'z') at `at`,
 * spanning [a0, a1] along the other horizontal axis and [y0, y1]. The sprite is stretched over it
 * (decal UVs, default the whole sprite); the same UVs on both sides read unmirrored from either side,
 * as vanilla's cross does.
 */
export function plane(axis, at, a0, a1, y0, y1, tex, opts = {}) {
  const uv = opts.uv ?? [0, 0, 16, 16];
  const [from, to, faces] = axis === 'z'
    ? [[a0, y0, at], [a1, y1, at], { north: uv, south: uv }]
    : [[at, y0, a0], [at, y1, a1], { west: uv, east: uv }];
  return box(from, to, tex, { ...opts, skip: FACES.filter((f) => !(f in faces)), decal: faces });
}

const TURN = { north: 'east', east: 'south', south: 'west', west: 'north', up: 'up', down: 'down' };

/**
 * Turns elements clockwise (seen from above) about the block centre by quarter turns, as a
 * blockstate y rotation would: (x, z) -> (16 - z, x). Faces, rotation axes and origins follow.
 */
export function turnY(elements, quarters) {
  let out = elements;
  for (let k = 0; k < ((quarters % 4) + 4) % 4; k++) {
    out = out.map((e) => {
      const p = ([x, y, z]) => [16 - z, y, x];
      const a = p(e.from);
      const b = p(e.to);
      const faces = {};
      for (const [f, face] of Object.entries(e.faces)) faces[TURN[f]] = face;
      let rotation;
      if (e.rotation) {
        const { axis, angle } = e.rotation;
        rotation = { ...e.rotation, origin: p(e.rotation.origin),
          axis: axis === 'x' ? 'z' : axis === 'z' ? 'x' : 'y', angle: axis === 'z' ? -angle : angle };
      }
      return { ...e, from: [Math.min(a[0], b[0]), a[1], Math.min(a[2], b[2])], to: [Math.max(a[0], b[0]), b[1], Math.max(a[2], b[2])],
        faces, ...(rotation ? { rotation } : {}) };
    });
  }
  return out;
}

/** Wraps a UV span into the 0..16 texture: shifts by whole textures, keeping its size. */
function wrap(a, b) {
  const lo = Math.min(a, b);
  const size = Math.abs(b - a);
  if (size > 16 + 1e-9) throw new Error(`face spans ${size} px, more than one texture`);
  let k = -Math.floor(lo / 16) * 16;
  if (lo + k + size > 16 + 1e-9) k = 16 - size - lo;
  return [a + k, b + k];
}

/** The cullface a face gets: an unrotated element's face lying on the block boundary culls against it. */
export function boundaryCullface(e, f) {
  if (e.rotation) return null;
  const axis = AXIS[f];
  return (POSITIVE.has(f) ? e.to[axis] === 16 : e.from[axis] === 0) ? f : null;
}

/**
 * Gives every face an explicit UV: position UVs (wrapped, with uvShift) or its decal, and a
 * cullface on the block boundary for unrotated elements. The result is what model-output.mjs
 * serialises (and what the Blockbench compiler's attachments merge into authored geometry).
 */
export function explicitUvs(elements) {
  return elements.map((e) => {
    const faces = {};
    for (const f of FACES) {
      const face = e.faces[f];
      if (!face) continue;
      let uv;
      if (face.decal) uv = face.decal;
      else {
        const d = defaultUv(f, e.from, e.to);
        const [su, sv] = e.uvShift ?? [0, 0];
        const [u0, u1] = wrap(d[0] + su, d[2] + su);
        const [v0, v1] = wrap(d[1] + sv, d[3] + sv);
        uv = [u0, v0, u1, v1];
      }
      const cullface = boundaryCullface(e, f);
      faces[f] = { uv, texture: face.texture, ...(cullface ? { cullface } : {}) };
    }
    const { uvShift, ...rest } = e;
    return { ...rest, faces };
  });
}

/** Elements as model JSON (explicit UVs, then serialised): what the coplanar separation bakes. */
export const finish = (elements) => serializeElements(explicitUvs(elements));


/**
 * Drops faces that can never be seen: an unrotated face with an opaque, unrotated box directly in
 * front of its whole area, closed on every side a line of sight could leave it by (all faces but
 * the one facing back at the buried face). Such a face costs a quad and, when it shares a plane
 * with a visible face, z-fights. `opaque(element)` says whether an element blocks the view.
 */
export function dropBuriedFaces(elements, opaque) {
  const OPPOSITE = { up: 'down', down: 'up', north: 'south', south: 'north', west: 'east', east: 'west' };
  const solids = elements.filter((e) => !e.rotation && opaque(e));
  const closed = (s, f) => FACES.every((g) => g === OPPOSITE[f] || s.faces[g]);
  const eps = 1e-3;
  return elements.map((e) => {
    if (e.rotation) return e;
    const faces = {};
    for (const [f, face] of Object.entries(e.faces)) {
      const a = AXIS[f];
      const at = POSITIVE.has(f) ? e.to[a] : e.from[a];
      const front = at + (POSITIVE.has(f) ? eps : -eps);
      const buried = solids.some((s) => s !== e && closed(s, f) && s.from[a] < front && front < s.to[a]
        && [0, 1, 2].every((k) => k === a || (s.from[k] <= e.from[k] && e.to[k] <= s.to[k])));
      if (!buried) faces[f] = face;
    }
    return { ...e, faces };
  }).filter((e) => Object.keys(e.faces).length);
}
