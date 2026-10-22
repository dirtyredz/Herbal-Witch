// The item model of a multiblock is its cells put back together. Java block models only accept
// element coordinates in -16..32, so an assembly reaching beyond that (a three-wide sofa reaches
// x -32, a three-high lantern post y 48) is moved, whole, back inside the legal box. The authored
// Display transforms were set in Blockbench against the unmoved assembly, so each slot's
// translation is corrected by the move: Minecraft applies translation, then rotation (X, Y, Z),
// then scale about the block centre, so a geometry move d needs translation - R * S * d to land
// where the author saw it.

export const LEGAL_MIN = -16;
export const LEGAL_MAX = 32;
/** Minecraft clamps display translations to +-80 (1/16 block units). */
const TRANSLATION_LIMIT = 80;

/** The bounding box of elements (unrotated from/to; rotation stays inside a few pixels). */
export function bounds(elements) {
  const min = [Infinity, Infinity, Infinity];
  const max = [-Infinity, -Infinity, -Infinity];
  for (const e of elements) {
    for (let i = 0; i < 3; i++) {
      min[i] = Math.min(min[i], e.from[i]);
      max[i] = Math.max(max[i], e.to[i]);
    }
  }
  return { min, max };
}

/**
 * The move (model pixels, whole pixels per axis) that brings the box inside -16..32, or zeros when
 * it already fits. Throws when an axis spans more than 48 pixels: that piece cannot be one item model.
 */
export function legalShift({ min, max }) {
  return [0, 1, 2].map((i) => {
    if (max[i] - min[i] > LEGAL_MAX - LEGAL_MIN) throw new Error(`the item assembly spans ${max[i] - min[i]} px on axis ${'xyz'[i]}; at most 48 fit one model`);
    if (min[i] < LEGAL_MIN) return LEGAL_MIN - min[i];
    if (max[i] > LEGAL_MAX) return LEGAL_MAX - max[i];
    return 0;
  });
}

const rad = (deg) => (deg * Math.PI) / 180;
function rotateXYZ([rx, ry, rz], v) {
  // q = Rx * Ry * Rz applied to v: rotate by z first, then y, then x.
  let [x, y, z] = v;
  const [cz, sz] = [Math.cos(rad(rz)), Math.sin(rad(rz))];
  [x, y] = [x * cz - y * sz, x * sz + y * cz];
  const [cy, sy] = [Math.cos(rad(ry)), Math.sin(rad(ry))];
  [x, z] = [x * cy + z * sy, -x * sy + z * cy];
  const [cx, sx] = [Math.cos(rad(rx)), Math.sin(rad(rx))];
  [y, z] = [y * cx - z * sx, y * sx + z * cx];
  return [x, y, z];
}

const round = (v) => Math.round(v * 1000) / 1000;

/** Display slots corrected for a geometry move of d pixels. */
export function compensateDisplay(display, d) {
  if (!display || d.every((v) => v === 0)) return display;
  const out = {};
  for (const [slot, t] of Object.entries(display)) {
    // A left-hand slot is applied mirrored: x translation negated, y and z rotations reversed.
    const left = slot.includes('lefthand');
    const rotation = left ? [t.rotation[0], -t.rotation[1], -t.rotation[2]] : t.rotation;
    const scaled = d.map((v, i) => v * t.scale[i]);
    const moved = rotateXYZ(rotation, scaled);
    if (left) moved[0] = -moved[0];
    const translation = t.translation.map((v, i) => round(v - moved[i]));
    if (translation.some((v) => Math.abs(v) > TRANSLATION_LIMIT)) {
      throw new Error(`display ${slot}: correcting the item move ${JSON.stringify(d)} needs translation ${JSON.stringify(translation)}, beyond +-80; reduce that slot's scale`);
    }
    out[slot] = { ...t, translation };
  }
  return out;
}
