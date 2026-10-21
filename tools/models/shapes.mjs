// Rounded and decorative building blocks for the house style: octagonal (chamfered) slabs built
// from overlapping boxes, stacked vessel profiles, rivets and studs, bands, and ivy drapes.
import { box, plane } from './geometry.mjs';

/** How far a chamfer wall's exposed top or bottom sits inside its layer, off the crossing parts' plane. */
export const CHAMFER_INSET = 0.1;

/**
 * An octagonal slab centred on (cx, cz): a w x d footprint with corners cut by c. Built from
 * rows along z that tile the footprint without overlapping (so no two top faces share a plane over
 * the same area and nothing z-fights): the full-width middle and a narrower row at each end, plus a
 * half step between them for a softer, stepped chamfer when c >= 2 (unless opts.third is false).
 * A row's face toward a wider neighbour is hidden and skipped. `tex` as in box().
 */
export function octagon(cx, cz, y0, y1, w, d, c, tex, opts = {}) {
  const hw = w / 2;
  const hd = d / 2;
  const stepped = c >= 2 && opts.third !== false;
  // [z0, z1, inset] rows from north to south
  const rows = stepped
    ? [[-hd, -hd + c / 2, c], [-hd + c / 2, -hd + c, c / 2], [-hd + c, hd - c, 0], [hd - c, hd - c / 2, c / 2], [hd - c / 2, hd, c]]
    : [[-hd, -hd + c, c], [-hd + c, hd - c, 0], [hd - c, hd, c]];
  const { third, ...rest } = opts;
  return rows.filter(([z0, z1]) => z1 > z0).map(([z0, z1, inset]) => {
    const skip = [...(rest.skip ?? [])];
    // the face toward the middle is covered by the wider neighbour
    if (inset > 0) skip.push(z0 < 0 ? 'south' : 'north');
    return box([cx - hw + inset, y0, cz + z0], [cx + hw - inset, y1, cz + z1], tex, { ...rest, skip });
  });
}

/**
 * A true octagonal slab: the tiled rows plus four 45-degree corner walls whose outer faces lie on
 * the chamfers, so every one of the eight sides is flat (7 elements).
 */
export function octagonTrue(cx, cz, y0, y1, w, d, c, tex, opts = {}) {
  return [
    ...octagon(cx, cz, y0, y1, w, d, c, tex, { ...opts, third: false }),
    ...chamfers(cx, cz, y0, y1, w, d, c, c / Math.SQRT2, tex, opts),
  ];
}

/**
 * The four 45-degree corner walls of an octagon, each `t` thick inward from its chamfer. A wall
 * overlaps the straight parts it joins, so its exposed top and bottom sit CHAMFER_INSET inside the
 * layer instead of sharing their plane (an invisible step that keeps the surface from flickering).
 */
export function chamfers(cx, cz, y0, y1, w, d, c, t, tex, opts = {}) {
  const hw = w / 2;
  const hd = d / 2;
  const L = c * Math.SQRT2;
  const top = opts.skip?.includes('up') ? y1 : y1 - CHAMFER_INSET;
  const bottom = opts.skip?.includes('down') ? y0 : y0 + CHAMFER_INSET;
  const out = [];
  for (const [sx, sz] of [[1, -1], [-1, -1], [1, 1], [-1, 1]]) {
    // chamfer midpoint, then step inward along the corner's inward normal
    const mx = cx + sx * (hw - c / 2);
    const mz = cz + sz * (hd - c / 2);
    const bx = mx - (sx * t) / 2 / Math.SQRT2;
    const bz = mz - (sz * t) / 2 / Math.SQRT2;
    const angle = sx === sz ? 45 : -45;
    out.push(box([bx - L / 2, bottom, bz - t / 2], [bx + L / 2, top, bz + t / 2], tex,
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
 * spec: {face: 'north'|'south'|'east'|'west', at (the face plane), along: [a0, a1], top, lengths,
 * clump (a leaf cube at the anchor), inside (hang on the inner side, for faces on the block edge)}
 */
export function ivyDrape({ face, at, along, top, lengths, clump = true, inside = false }, tex = '#ivy', leaf = '#ivy') {
  const out = [];
  // strands hang just outside the face, or just inside it when the face is the block's own edge
  const off = (face === 'north' || face === 'west' ? -0.3 : 0.3) * (inside ? -1 : 1);
  const axis = face === 'north' || face === 'south' ? 'z' : 'x';
  const span = (along[1] - along[0]) / lengths.length;
  lengths.forEach((len, i) => {
    const a0 = along[0] + i * span;
    // sample a different column of the sprite for each strand so they do not repeat
    const u0 = (i * 5) % 12;
    out.push(plane(axis, at + off * (1 + (i % 2)), a0, a0 + span, top - len, top, tex, { uv: [u0, 0, u0 + 4, (len / 16) * 16] }));
  });
  if (clump) {
    // a leaf cube a little in from every plane it could share: past the strands, inside the edges
    // of the post or rail it sits on, just under the top
    const mid = (along[0] + along[1]) / 2;
    const p = at + off * 2.6;
    const [x0, x1, z0, z1] = axis === 'z' ? [mid - 1.4, mid + 1.4, Math.min(at, p), Math.max(at, p)] : [Math.min(at, p), Math.max(at, p), mid - 1.4, mid + 1.4];
    out.push(box([x0, top - 2.1, z0], [x1, top - 0.1, z1], leaf, { decal: { all: [2, 2, 6, 6] } }));
  }
  return out;
}
