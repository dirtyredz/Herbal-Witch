// Element authoring for the generated cuboid models: boxes and planes with per-face textures,
// position-based UVs at one UV unit per model pixel (so textures run continuously across
// neighbouring elements and cells), explicit "decal" UVs where a whole sprite is stretched over a
// face, legal one-axis rotations, cullfaces on the block boundary, and the splitting of a
// multiblock assembly into its cells. Pure data: tools/generate-models.mjs writes the JSON.
import { defaultUv } from '../preview/geometry/elements.mjs';

export const FACES = ['down', 'up', 'north', 'south', 'west', 'east'];
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

/** Face (s, t) axes as functions of a point, matching Minecraft's default UV orientation. */
const AXES = {
  north: [(p) => -p[0], (p) => -p[1]], south: [(p) => p[0], (p) => -p[1]],
  west: [(p) => p[2], (p) => -p[1]], east: [(p) => -p[2], (p) => -p[1]],
  up: [(p) => p[0], (p) => p[2]], down: [(p) => p[0], (p) => -p[2]],
};

/** Clips a decal UV rectangle to the part of the face a piece keeps. */
function clipDecal(face, decal, whole, piece) {
  const [sf, tf] = AXES[face];
  const range = (fn, a, b) => {
    const pts = [fn(a.from), fn(a.to)];
    return [Math.min(...pts), Math.max(...pts)];
  };
  const [s0, s1] = range(sf, whole);
  const [t0, t1] = range(tf, whole);
  const [p0, p1] = range(sf, piece);
  const [q0, q1] = range(tf, piece);
  const lerp = (a, b, k) => a + (b - a) * k;
  const fs = (v) => (s1 === s0 ? 0 : (v - s0) / (s1 - s0));
  const ft = (v) => (t1 === t0 ? 0 : (v - t0) / (t1 - t0));
  return [lerp(decal[0], decal[2], fs(p0)), lerp(decal[1], decal[3], ft(q0)), lerp(decal[0], decal[2], fs(p1)), lerp(decal[1], decal[3], ft(q1))];
}

/**
 * Splits an assembly (cell 0 at x 0..16, y 0..16; a cell `right` steps to the viewer's right sits
 * at x - 16 * right; `up` steps at y + 16 * up) into per-cell element lists, cutting elements at
 * cell boundaries in their unrotated frame and moving each piece into its cell's coordinates.
 * Pieces of one rotated element keep the same rotation about the same (moved) origin, so they
 * reassemble exactly. Faces created by a cut are dropped. With `overflow`, the top row also keeps
 * what rises above the structure (a stovepipe), in its own coordinates above y = 16.
 */
export function splitCells(elements, cells, { overflow = false } = {}) {
  const out = cells.map(() => []);
  const topRow = Math.max(...cells.map((c) => c.up));
  cells.forEach(({ right, up }, ci) => {
    const lo = [-16 * right, 16 * up, 0];
    const hi = [16 - 16 * right, overflow && up === topRow ? 48 : 16 + 16 * up, 16];
    for (const e of elements) {
      const from = e.from.map((v, i) => Math.max(v, lo[i]));
      const to = e.to.map((v, i) => Math.min(v, hi[i]));
      // keep planes (zero thickness) but not pieces outside the cell
      if (from.some((v, i) => v > to[i]) || from.filter((v, i) => v === to[i]).length > 1) continue;
      if (from.some((v, i) => v === to[i] && (e.from[i] !== e.to[i]))) continue;
      const faces = {};
      for (const [f, face] of Object.entries(e.faces)) {
        const axis = { west: 0, east: 0, down: 1, up: 1, north: 2, south: 2 }[f];
        const onMin = f === 'west' || f === 'down' || f === 'north';
        const edge = onMin ? from[axis] === e.from[axis] : to[axis] === e.to[axis];
        if (!edge) continue; // a cut face
        faces[f] = face.decal ? { ...face, decal: clipDecal(f, face.decal, e, { from, to }) } : face;
      }
      if (!Object.keys(faces).length) continue;
      const move = (p) => [p[0] - lo[0], p[1] - lo[1], p[2] - lo[2]];
      out[ci].push({
        ...e, from: move(from), to: move(to), faces,
        ...(e.rotation ? { rotation: { ...e.rotation, origin: move(e.rotation.origin) } } : {}),
      });
    }
  });
  return out;
}

/** Moves elements by (dx, dy, dz) model pixels (rotation origins too). */
export function shift(elements, [dx, dy, dz]) {
  const m = (p) => [p[0] + dx, p[1] + dy, p[2] + dz];
  return elements.map((e) => ({ ...e, from: m(e.from), to: m(e.to), ...(e.rotation ? { rotation: { ...e.rotation, origin: m(e.rotation.origin) } } : {}) }));
}

/** Mirrors elements across x = 8 (swapping east/west faces and negating rotations about y and z). */
export function mirrorX(elements) {
  return elements.map((e) => {
    const faces = {};
    for (const [f, face] of Object.entries(e.faces)) faces[f === 'east' ? 'west' : f === 'west' ? 'east' : f] = face;
    return {
      ...e, from: [16 - e.to[0], e.from[1], e.from[2]], to: [16 - e.from[0], e.to[1], e.to[2]], faces,
      ...(e.rotation ? { rotation: { ...e.rotation, origin: [16 - e.rotation.origin[0], e.rotation.origin[1], e.rotation.origin[2]],
        angle: e.rotation.axis === 'x' ? e.rotation.angle : -e.rotation.angle } } : {}),
    };
  });
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

const r4 = (v) => Math.round(v * 10000) / 10000 + 0;

/** Wraps a UV span into the 0..16 texture: shifts by whole textures, keeping its size. */
function wrap(a, b) {
  const lo = Math.min(a, b);
  const size = Math.abs(b - a);
  if (size > 16 + 1e-9) throw new Error(`face spans ${size} px, more than one texture`);
  let k = -Math.floor(lo / 16) * 16;
  if (lo + k + size > 16 + 1e-9) k = 16 - size - lo;
  return [a + k, b + k];
}

/**
 * Finishes elements for JSON: position UVs (wrapped, with uvShift), decal UVs, cullfaces on the
 * block boundary for unrotated elements, rounded numbers, stable key order.
 */
export function finish(elements) {
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
      const out = { uv: uv.map(r4), texture: face.texture };
      const axis = { west: 0, east: 0, down: 1, up: 1, north: 2, south: 2 }[f];
      const onMin = f === 'west' || f === 'down' || f === 'north';
      if (!e.rotation && (onMin ? e.from[axis] === 0 : e.to[axis] === 16)) out.cullface = f;
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

/**
 * A model JSON object. `textures` maps variables to texture ids; cutout when `cutout`; `open` lists
 * variables a template deliberately leaves for its children to bind.
 */
export function model({ textures, elements, cutout = false, parent = null, display = null, ao = true, open = [] }) {
  const used = new Set(elements.flatMap((e) => Object.values(e.faces).map((f) => f.texture.slice(1))));
  for (const v of used) if (!textures[v] && !open.includes(v)) throw new Error(`texture variable #${v} is not bound`);
  const bound = Object.fromEntries(Object.entries(textures).filter(([k]) => used.has(k) || k === 'particle'));
  return {
    ...(parent ? { parent } : {}),
    ...(ao ? {} : { ambientocclusion: false }),
    ...(cutout ? { render_type: 'minecraft:cutout' } : {}),
    textures: bound,
    ...(display ? { display } : {}),
    elements: finish(elements),
  };
}

function rotXYZ([x, y, z], [rx, ry, rz]) {
  const r = (a) => (a * Math.PI) / 180;
  // JOML rotationXYZ: v' = Rx * Ry * Rz * v
  let [a, b, c] = [x * Math.cos(r(rz)) - y * Math.sin(r(rz)), x * Math.sin(r(rz)) + y * Math.cos(r(rz)), z];
  [a, b, c] = [a * Math.cos(r(ry)) + c * Math.sin(r(ry)), b, -a * Math.sin(r(ry)) + c * Math.cos(r(ry))];
  return [a, b * Math.cos(r(rx)) - c * Math.sin(r(rx)), b * Math.sin(r(rx)) + c * Math.cos(r(rx))];
}

/**
 * Item display transforms for a model larger than one block (a multiblock assembly spanning
 * min..max model pixels): the vanilla block-item rotations, with scales shrunk by `fit` and
 * translations that bring the assembly's centre back to the slot centre.
 */
export function assemblyDisplay(min, max, fit) {
  const centre = min.map((v, i) => (v + max[i]) / 2 - 8);
  const ctx = {
    gui: [[30, 225, 0], 0.625], ground: [[0, 0, 0], 0.25], fixed: [[0, 0, 0], 0.5],
    thirdperson_righthand: [[75, 45, 0], 0.375], firstperson_righthand: [[0, 45, 0], 0.4], firstperson_lefthand: [[0, 225, 0], 0.4],
  };
  const out = {};
  for (const [name, [rotation, base]] of Object.entries(ctx)) {
    const s = base * fit;
    const t = rotXYZ(centre.map((v) => v * s), rotation).map((v) => Math.max(-80, Math.min(80, r4(-v))));
    out[name] = { rotation, translation: t, scale: [r4(s), r4(s), r4(s)] };
  }
  return out;
}
