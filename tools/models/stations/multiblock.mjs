// A multiblock station's models from one assembly: per-cell permanent parts (part_<n>), per-cell
// overlays shown while lit/active (<overlay>_<n>, only where the overlay reaches), and the whole
// assembly for the item with display transforms that fit it in a slot. Cells match the Java
// layout (platform/multiblock/MultiblockLayout): row by row from the bottom, cell 0 the origin,
// "right" toward -x for a north-facing model.
import { splitCells, shift, assemblyDisplay } from '../geometry.mjs';

/** Cells of a width x height rectangle, in the Java layout's order. */
export function rectangle(width, height) {
  const cells = [];
  for (let up = 0; up < height; up++) for (let right = 0; right < width; right++) cells.push({ right, up });
  return cells;
}

/**
 * @param dir     model directory, e.g. 'block/stations/drying_rack'
 * @param cells   rectangle(...)
 * @param base    permanent elements in assembly space
 * @param overlay {name, elements} shown while lit/active, or null
 * @param fit     item scale relative to a one-block item
 * @param overflow keep geometry rising above the top row in the top cells (see splitCells)
 */
export function multiblockModels({ dir, cells, base, overlay, textures, fit, overflow = false }) {
  const out = [];
  const parts = splitCells(base, cells, { overflow });
  parts.forEach((elements, i) => {
    if (!elements.length) throw new Error(`${dir}: cell ${i} has no permanent geometry`);
    out.push({ path: `${dir}/part_${i}`, textures, elements });
  });
  // the item: the cell pieces put back together, so no face spans more than one texture
  const assembly = parts.flatMap((elements, i) => shift(elements, [-16 * cells[i].right, 16 * cells[i].up, 0]));
  if (overlay) {
    splitCells(overlay.elements, cells, { overflow }).forEach((elements, i) => {
      if (elements.length) out.push({ path: `${dir}/${overlay.name}_${i}`, textures, elements });
    });
  }
  const xs = cells.map((c) => -16 * c.right);
  const ys = cells.map((c) => 16 * c.up);
  const min = [Math.min(...xs), 0, 0];
  const max = [Math.max(...xs) + 16, Math.max(...ys) + 16, 16];
  out.push({ path: `${dir}/assembly`, textures, elements: assembly, display: assemblyDisplay(min, max, fit) });
  return out;
}
