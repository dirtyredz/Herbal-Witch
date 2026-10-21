// Rounded and decorative building blocks for the house style: octagonal (chamfered) slabs built
// from overlapping boxes, stacked vessel profiles, rivets and studs, bands, and ivy drapes.
import { box, plane } from './geometry.mjs';

/**
 * An octagonal slab centred on (cx, cz): a w x d footprint with corners cut by c, built from two
 * crossing boxes (plus a third for a softer, stepped chamfer when c >= 2). `tex` as in box().
 * Inner faces hidden by the union are skipped where they are fully covered.
 */
export function octagon(cx, cz, y0, y1, w, d, c, tex, opts = {}) {
  const hw = w / 2;
  const hd = d / 2;
  const out = [
    box([cx - hw, y0, cz - hd + c], [cx + hw, y1, cz + hd - c], tex, opts),
    box([cx - hw + c, y0, cz - hd], [cx + hw - c, y1, cz + hd], tex, opts),
  ];
  if (c >= 2 && opts.third !== false) out.push(box([cx - hw + c / 2, y0, cz - hd + c / 2], [cx + hw - c / 2, y1, cz + hd - c / 2], tex, opts));
  return out;
}

/**
 * A true octagonal slab: the crossing boxes plus four 45-degree corner boxes whose outer faces lie
 * on the chamfers, so every one of the eight sides is flat (6 elements).
 */
export function octagonTrue(cx, cz, y0, y1, w, d, c, tex, opts = {}) {
  const out = octagon(cx, cz, y0, y1, w, d, c, tex, { ...opts, third: false });
  out.length = 2;
  out.push(...chamfers(cx, cz, y0, y1, w, d, c, c / Math.SQRT2, tex, opts));
  return out;
}

/** The four 45-degree corner walls of an octagon, each `t` thick inward from its chamfer. */
export function chamfers(cx, cz, y0, y1, w, d, c, t, tex, opts = {}) {
  const hw = w / 2;
  const hd = d / 2;
  const L = c * Math.SQRT2;
  const out = [];
  for (const [sx, sz] of [[1, -1], [-1, -1], [1, 1], [-1, 1]]) {
    // chamfer midpoint, then step inward along the corner's inward normal
    const mx = cx + sx * (hw - c / 2);
    const mz = cz + sz * (hd - c / 2);
    const bx = mx - (sx * t) / 2 / Math.SQRT2;
    const bz = mz - (sz * t) / 2 / Math.SQRT2;
    const angle = sx === sz ? 45 : -45;
    out.push(box([bx - L / 2, y0, bz - t / 2], [bx + L / 2, y1, bz + t / 2], tex,
      { ...opts, rot: { origin: [bx, (y0 + y1) / 2, bz], axis: 'y', angle } }));
  }
  return out;
}

/**
 * A hollow octagonal ring (a vessel rim or bowl wall): four straight walls and four 45-degree corner
 * walls, `t` thick, outer footprint w x d with chamfer c (8 elements).
 */
export function octagonRing(cx, cz, y0, y1, w, d, c, t, tex, opts = {}) {
  const hw = w / 2;
  const hd = d / 2;
  return [
    box([cx - hw + c, y0, cz - hd], [cx + hw - c, y1, cz - hd + t], tex, opts),
    box([cx - hw + c, y0, cz + hd - t], [cx + hw - c, y1, cz + hd], tex, opts),
    box([cx - hw, y0, cz - hd + c], [cx - hw + t, y1, cz + hd - c], tex, opts),
    box([cx + hw - t, y0, cz - hd + c], [cx + hw, y1, cz + hd - c], tex, opts),
    ...chamfers(cx, cz, y0, y1, w, d, c, t, tex, opts),
  ];
}

/**
 * A rounded vessel: a stack of octagonal layers, profile = [[y0, y1, width, chamfer], ...] around
 * (cx, cz). Each layer's top only where it is wider than the layer above (a visible shoulder).
 */
export function vessel(cx, cz, profile, tex, opts = {}) {
  const out = [];
  profile.forEach(([y0, y1, w, c], i) => {
    const above = profile[i + 1];
    const below = profile[i - 1];
    const skip = [...(opts.skip ?? [])];
    if (above && above[2] >= w) skip.push('up');
    if (below && below[2] >= w) skip.push('down');
    const make = opts.exact ? octagonTrue : octagon;
    out.push(...make(cx, cz, y0, y1, w, w * (opts.aspect ?? 1), c, tex, { ...opts, skip }));
  });
  return out;
}

/** A square stud/rivet of size s protruding p from the face of a surface at (x, y, z). */
export function rivet(face, x, y, z, tex = '#gold', s = 1, p = 0.5) {
  const h = s / 2;
  switch (face) {
    case 'north': return box([x - h, y - h, z - p], [x + h, y + h, z], tex);
    case 'south': return box([x - h, y - h, z], [x + h, y + h, z + p], tex);
    case 'west': return box([x - p, y - h, z - h], [x, y + h, z + h], tex);
    case 'east': return box([x, y - h, z - h], [x + p, y + h, z + h], tex);
    case 'up': return box([x - h, y, z - h], [x + h, y + p, z + h], tex);
    default: throw new Error(face);
  }
}

/** A band wrapped around a rectangular post (x0..x1, z0..z1) between y0 and y1, standing out by t. */
export function band(x0, z0, x1, z1, y0, y1, tex, t = 0.25) {
  return box([x0 - t, y0, z0 - t], [x1 + t, y1, z1 + t], tex, { skip: ['up', 'down'] });
}

/**
 * Ivy draped down a vertical face: a few cutout planes of varied length hanging from `top`,
 * slightly off the surface, plus a small clump of leaf cubes where it is anchored.
 * spec: {face: 'north'|'south'|'east'|'west', at (the face plane), along: [a0, a1], top, lengths}
 */
export function ivyDrape({ face, at, along, top, lengths, clump = true }, tex = '#ivy', leaf = '#ivy') {
  const out = [];
  const off = face === 'north' || face === 'west' ? -0.3 : 0.3;
  const axis = face === 'north' || face === 'south' ? 'z' : 'x';
  const span = (along[1] - along[0]) / lengths.length;
  lengths.forEach((len, i) => {
    const a0 = along[0] + i * span;
    // sample a different column of the sprite for each strand so they do not repeat
    const u0 = (i * 5) % 12;
    out.push(plane(axis, at + off * (1 + (i % 2)), a0, a0 + span, top - len, top, tex, { uv: [u0, 0, u0 + 4, (len / 16) * 16] }));
  });
  if (clump) {
    const mid = (along[0] + along[1]) / 2;
    const p = at + off * 2;
    const [x0, x1, z0, z1] = axis === 'z' ? [mid - 1.5, mid + 1.5, Math.min(at, p), Math.max(at, p)] : [Math.min(at, p), Math.max(at, p), mid - 1.5, mid + 1.5];
    out.push(box([x0, top - 1.5, z0], [x1, top + 0.5, z1], leaf, { decal: { all: [2, 2, 6, 6] } }));
  }
  return out;
}
