// Turns one authored project into its Minecraft models: the state outputs of a single-block piece
// (which groups each keeps, which glow) and the cells of a multiblock. A multiblock is authored
// whole in assembly coordinates (geometry/cells.mjs: cell 0 at x 0..16, y 0..16, z 0..16; a step
// to the viewer's right at x - 16, up at y + 16, back at z + 16, matching the Java layout in
// platform/multiblock/MultiblockLayout) and split per cell. Cullfaces are recomputed for each
// output. The item model is the cell pieces put back, moved inside the legal -16..32 box when the
// structure is larger (geometry/item-assembly.mjs).
import { boundaryCullface } from '../models/geometry.mjs';
import { declaredCells, splitCells, shift, cellOffset, mirrorX } from './geometry/cells.mjs';
import { bounds, legalShift } from './geometry/item-assembly.mjs';

export { rectangle, cropUv, splitCells, shift } from './geometry/cells.mjs';

const GLOW = { block_light: 15, sky_light: 15 };

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

/** The x centre of a structure's columns, about which a mirrored variant turns. */
function mirrorCentre(cells) {
  const rights = cells.map((c) => c.right);
  return 8 - 8 * (Math.max(...rights) + Math.min(...rights));
}

/**
 * The model outputs of a multiblock: part_<n> (permanent, every cell), <overlay>_<n> (only cells
 * an overlay reaches), <variant>_<n> (every cell: the permanent geometry with its own glowing
 * groups, exclusions or mirroring) and the assembly (the item, the parts put back, with `shift`
 * the move that kept it inside the legal model box).
 */
export function multiblockOutputs(piece, elements) {
  const m = piece.multiblock;
  const cells = declaredCells(m.cells);
  const overlays = m.overlays ?? [];
  const overlayOf = (e) => overlays.find((o) => e.path.includes(o.group));
  const out = [];
  const permanent = elements.filter((e) => !overlayOf(e));
  const parts = splitCells(permanent, cells, { overflow: !!m.overflow });
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
  for (const v of m.variants ?? []) {
    const emissive = new Set(v.emissive ?? []);
    let els = glowing(permanent.filter((e) => !(v.exclude ?? []).includes(topGroup(e))), (e) => e.path.some((g) => emissive.has(g)));
    if (v.mirror) els = mirrorX(els, mirrorCentre(cells));
    splitCells(els, cells, { overflow: !!m.overflow }).forEach((cellEls, i) => {
      if (!cellEls.length) throw new Error(`${m.dir}: variant ${v.name} leaves cell ${i} empty`);
      out.push({ path: `${m.dir}/${v.name}_${i}`, elements: withCullfaces(cellEls) });
    });
  }
  const assembled = parts.flatMap((els, i) => shift(els, cellOffset(cells[i])));
  const d = legalShift(bounds(assembled));
  out.push({ path: `${m.dir}/assembly`, elements: withCullfaces(shift(assembled, d)), item: true, shift: d });
  return out;
}
