// Whole-model transforms applied after baking: blockstate x/y rotations (vanilla ModelRotation: x
// first, then y, both clockwise in degrees about the block centre), item display transforms
// (rotation XYZ, translation, scale about the centre) and placement offsets in a scene.
import { rotateAbout } from './elements.mjs';

const CENTRE = [8, 8, 8];

function mapQuads(quads, fn, fnNormal) {
  return quads.map((q) => ({ ...q, pos: q.pos.map(fn), normal: fnNormal(q.normal) }));
}

/** Applies a blockstate variant's x/y rotation. uvlock is not supported and fails explicitly. */
export function variantRotation(quads, { x = 0, y = 0, uvlock = false } = {}) {
  if (uvlock && (x || y)) throw new Error('uvlock is not supported by the previewer');
  for (const v of [x, y]) if (v % 90 !== 0) throw new Error(`blockstate rotation ${v} is not a multiple of 90`);
  let out = quads;
  if (x) {
    const r = { origin: CENTRE, axis: 'x', angle: -x };
    out = mapQuads(out, (p) => rotateAbout(p, r), (n) => rotateAbout(n, { ...r, origin: [0, 0, 0] }));
  }
  if (y) {
    const r = { origin: CENTRE, axis: 'y', angle: -y };
    out = mapQuads(out, (p) => rotateAbout(p, r), (n) => rotateAbout(n, { ...r, origin: [0, 0, 0] }));
    out = out.map((q) => (q.cullface ? { ...q, cullface: turn(q.cullface, y) } : q));
  }
  return out;
}

const RING = ['north', 'east', 'south', 'west'];

/** A horizontal face direction turned clockwise (seen from above) by deg. */
function turn(face, deg) {
  const i = RING.indexOf(face);
  return i < 0 ? face : RING[(i + deg / 90) % 4];
}

/** Applies an item display transform ({rotation, translation, scale}) about the model centre. */
export function displayTransform(quads, t) {
  if (!t) return quads;
  const [rx, ry, rz] = t.rotation ?? [0, 0, 0];
  const [tx, ty, tz] = t.translation ?? [0, 0, 0];
  const [sx, sy, sz] = t.scale ?? [1, 1, 1];
  const rot = (p, o) => {
    // JOML rotationXYZ: v' = Rx * Ry * Rz * v
    let r = rotateAbout(p, { origin: o, axis: 'z', angle: rz });
    r = rotateAbout(r, { origin: o, axis: 'y', angle: ry });
    return rotateAbout(r, { origin: o, axis: 'x', angle: rx });
  };
  return quads.map((q) => ({
    ...q,
    pos: q.pos.map((p) => {
      const scaled = [CENTRE[0] + (p[0] - CENTRE[0]) * sx, CENTRE[1] + (p[1] - CENTRE[1]) * sy, CENTRE[2] + (p[2] - CENTRE[2]) * sz];
      const r = rot(scaled, CENTRE);
      return [r[0] + tx, r[1] + ty, r[2] + tz];
    }),
    normal: rot(q.normal, [0, 0, 0]),
  }));
}

/** Moves quads to block cell (bx, by, bz) of a scene (16 model pixels per block). */
export function translate(quads, [bx, by, bz]) {
  return quads.map((q) => ({ ...q, pos: q.pos.map(([x, y, z]) => [x + bx * 16, y + by * 16, z + bz * 16]) }));
}
