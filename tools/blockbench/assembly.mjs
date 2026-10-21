// Turns one authored project into its Minecraft models: the state outputs of a single-block piece
// (which groups each keeps, which glow) and the cells of a multiblock. A multiblock is authored
// whole in assembly coordinates (cell 0 at x 0..16, y 0..16; a cell `right` steps to the viewer's
// right sits at x - 16 * right, `up` steps at y + 16 * up, matching the Java layout in
// platform/multiblock/MultiblockLayout) and split here: elements are cut at cell boundaries in their
// unrotated frame, explicit UVs are cropped with the cut (flipped and rotated UVs included),
// rotation pivots move with their cell, faces created by a cut are omitted and authored omissions
// stay omitted. Cullfaces are recomputed for each output. The item model is the cell pieces put back.
import { boundaryCullface } from '../models/geometry.mjs';

const AXIS = { west: 0, east: 0, down: 1, up: 1, north: 2, south: 2 };
const GLOW = { block_light: 15, sky_light: 15 };

/** Cells of a width x height rectangle, in the Java layout's order (row by row from the bottom). */
export function rectangle(width, height) {
  const cells = [];
  for (let up = 0; up < height; up++) for (let right = 0; right < width; right++) cells.push({ right, up });
  return cells;
}

/** Face parameter (s, t) in [0, 1] of point p on a face of box from..to (Minecraft's corner order). */
function param(face, p, from, to) {
  const f = (i, v, flip) => {
    const d = to[i] - from[i];
    const k = d === 0 ? 0 : (v - from[i]) / d;
    return flip ? 1 - k : k;
  };
  switch (face) {
    case 'north': return [f(0, p[0], true), f(1, p[1], true)];
    case 'south': return [f(0, p[0], false), f(1, p[1], true)];
    case 'west': return [f(2, p[2], false), f(1, p[1], true)];
    case 'east': return [f(2, p[2], true), f(1, p[1], true)];
    case 'up': return [f(0, p[0], false), f(2, p[2], false)];
    case 'down': return [f(0, p[0], false), f(2, p[2], true)];
    default: throw new Error(`unknown face ${face}`);
  }
}

/**
 * The UV rectangle of the part piece.from..piece.to of a face whose whole UV is `uv` on whole.from..to,
 * honouring the face's UV rotation (0/90/180/270): the sub-face maps to a sub-rectangle of the
 * rotated unit square, which is then laid onto the original rectangle (flips carry through).
 */
export function cropUv(face, uv, rotation, whole, piece) {
  const [a0, b0] = param(face, piece.from, whole.from, whole.to);
  const [a1, b1] = param(face, piece.to, whole.from, whole.to);
  const [s0, s1] = [Math.min(a0, a1), Math.max(a0, a1)];
  const [t0, t1] = [Math.min(b0, b1), Math.max(b0, b1)];
  let rs;
  let rt;
  switch (((rotation ?? 0) % 360 + 360) % 360) {
    case 0: [rs, rt] = [[s0, s1], [t0, t1]]; break;
    case 90: [rs, rt] = [[t0, t1], [1 - s1, 1 - s0]]; break;
    case 180: [rs, rt] = [[1 - s1, 1 - s0], [1 - t1, 1 - t0]]; break;
    case 270: [rs, rt] = [[1 - t1, 1 - t0], [s0, s1]]; break;
    default: throw new Error(`face rotation ${rotation}`);
  }
  const lerp = (x, y, k) => x + (y - x) * k;
  return [lerp(uv[0], uv[2], rs[0]), lerp(uv[1], uv[3], rt[0]), lerp(uv[0], uv[2], rs[1]), lerp(uv[1], uv[3], rt[1])];
}

const move = (p, d) => [p[0] + d[0], p[1] + d[1], p[2] + d[2]];

/**
 * Splits assembly-space elements into per-cell lists in cell coordinates. With `overflow` the top
 * row also keeps what rises above the structure (a stovepipe), in its own coordinates above y = 16.
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
      if (from.some((v, i) => v > to[i])) continue; // outside the cell
      if (from.some((v, i) => v === to[i] && e.from[i] !== e.to[i])) continue; // only touches the cell
      const faces = {};
      for (const [f, face] of Object.entries(e.faces)) {
        const axis = AXIS[f];
        const onMin = f === 'west' || f === 'down' || f === 'north';
        const edge = onMin ? from[axis] === e.from[axis] : to[axis] === e.to[axis];
        if (!edge) continue; // a face created by the cut
        const whole = from.every((v, i) => v === e.from[i]) && to.every((v, i) => v === e.to[i]);
        faces[f] = whole ? face : { ...face, uv: cropUv(f, face.uv, face.rotation, e, { from, to }) };
      }
      if (!Object.keys(faces).length) continue;
      const d = lo.map((v) => -v);
      out[ci].push({
        ...e, from: move(from, d), to: move(to, d), faces,
        ...(e.rotation ? { rotation: { ...e.rotation, origin: move(e.rotation.origin, d) } } : {}),
      });
    }
  });
  return out;
}

/** Moves elements by d model pixels (rotation origins too). */
export const shift = (elements, d) => elements.map((e) => ({
  ...e, from: move(e.from, d), to: move(e.to, d), ...(e.rotation ? { rotation: { ...e.rotation, origin: move(e.rotation.origin, d) } } : {}),
}));

/** Recomputes every face's cullface for the block the elements now sit in. */
export function withCullfaces(elements) {
  return elements.map((e) => {
    const faces = {};
    for (const [f, face] of Object.entries(e.faces)) {
      const { cullface, ...rest } = face;
      const c = boundaryCullface(e, f);
      faces[f] = c ? { ...rest, cullface: c } : rest;
    }
    return { ...e, faces };
  });
}

const glowing = (elements, on) => elements.map((e) => (on(e) ? { ...e, forge_data: GLOW } : e));
const topGroup = (e) => e.path[0];

/**
 * The model outputs of a single-block piece: [{path, elements, item}] where each output keeps the
 * elements outside its `exclude` groups and makes its `emissive` groups glow.
 */
export function singleOutputs(piece, elements) {
  return piece.outputs.map((o) => {
    const kept = elements.filter((e) => !(o.exclude ?? []).includes(topGroup(e)));
    const emissive = new Set(o.emissive ?? []);
    return { path: o.model, elements: glowing(kept, (e) => e.path.some((g) => emissive.has(g))), item: !!o.item };
  });
}

/**
 * The model outputs of a multiblock: part_<n> (permanent, every cell), <overlay>_<n> (only cells
 * an overlay reaches) and the assembly (the item, the parts put back).
 */
export function multiblockOutputs(piece, elements) {
  const m = piece.multiblock;
  const cells = rectangle(m.cells[0], m.cells[1]);
  const overlays = m.overlays ?? [];
  const overlayOf = (e) => overlays.find((o) => e.path.includes(o.group));
  const out = [];
  const parts = splitCells(elements.filter((e) => !overlayOf(e)), cells, { overflow: !!m.overflow });
  parts.forEach((els, i) => {
    if (!els.length) throw new Error(`${m.dir}: cell ${i} has no permanent geometry`);
    out.push({ path: `${m.dir}/part_${i}`, elements: withCullfaces(els) });
  });
  for (const o of overlays) {
    const els = glowing(elements.filter((e) => overlayOf(e) === o), () => !!o.emissive);
    if (!els.length) throw new Error(`${m.dir}: overlay ${o.name} (group ${o.group}) is empty`);
    splitCells(els, cells, { overflow: !!m.overflow }).forEach((cellEls, i) => {
      if (cellEls.length) out.push({ path: `${m.dir}/${o.name}_${i}`, elements: withCullfaces(cellEls) });
    });
  }
  const assembly = parts.flatMap((els, i) => shift(els, [-16 * cells[i].right, 16 * cells[i].up, 0]));
  out.push({ path: `${m.dir}/assembly`, elements: withCullfaces(assembly), item: true });
  return out;
}
