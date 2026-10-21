// Z-fighting detection: two quads facing the same way in the same plane whose areas overlap render
// at identical depth, so the GPU picks between them per pixel and the surface flickers. Works on
// baked quads (any element or blockstate rotation), so rotated leaves and turned parts are covered.
// Quads facing opposite ways (a two-sided plane, two touching boxes) are not a conflict.

/** Planes closer than this (model pixels, 1/16 block) count as one: depth precision cannot separate them. */
export const PLANE_TOLERANCE = 0.02;

const dot = (a, b) => a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
const sub = (a, b) => [a[0] - b[0], a[1] - b[1], a[2] - b[2]];
const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]];
const unit = (v) => {
  const l = Math.hypot(...v);
  return l < 1e-12 ? null : v.map((c) => c / l);
};

/** The quad's true facing from its corners (a degenerate quad, e.g. a zero-width face, has none). */
function facing(q) {
  const n = unit(cross(sub(q.pos[1], q.pos[0]), sub(q.pos[3], q.pos[0])));
  if (!n) return null;
  return dot(n, q.normal) < 0 ? n.map((c) => -c) : n;
}

const area2 = (poly) => poly.reduce((s, p, i) => {
  const q = poly[(i + 1) % poly.length];
  return s + p[0] * q[1] - q[0] * p[1];
}, 0) / 2;

/** Area of the intersection of two convex polygons (Sutherland-Hodgman, both counter-clockwise). */
function overlapArea(a, b) {
  let out = a;
  for (let i = 0; i < b.length && out.length; i++) {
    const p = b[i];
    const q = b[(i + 1) % b.length];
    const side = (r) => (q[0] - p[0]) * (r[1] - p[1]) - (q[1] - p[1]) * (r[0] - p[0]);
    const input = out;
    out = [];
    for (let j = 0; j < input.length; j++) {
      const cur = input[j];
      const prev = input[(j + input.length - 1) % input.length];
      const sc = side(cur);
      const sp = side(prev);
      if (sc >= 0) {
        if (sp < 0) out.push(lerp(prev, cur, sp / (sp - sc)));
        out.push(cur);
      } else if (sp >= 0) out.push(lerp(prev, cur, sp / (sp - sc)));
    }
  }
  return out.length < 3 ? 0 : Math.abs(area2(out));
}

const lerp = (a, b, t) => [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t];

/**
 * Pairs of quads that would z-fight: same facing, the same plane (within `tolerance`), overlapping by more
 * than `minArea` square model pixels. Returns [{a, b, area}] with quad indices into `quads`.
 */
export function coplanarOverlaps(quads, { minArea = 1e-3, tolerance = PLANE_TOLERANCE } = {}) {
  const groups = new Map();
  quads.forEach((q, i) => {
    const n = facing(q);
    if (!n) return;
    const key = n.map((v) => Math.round(v * 1000) / 1000 + 0).join(',');
    if (!groups.has(key)) groups.set(key, { n, items: [] });
    groups.get(key).items.push(i);
  });
  const out = [];
  for (const { n, items } of groups.values()) {
    if (items.length < 2) continue;
    // a 2D basis in the plane, oriented so polygons seen from outside are counter-clockwise
    const helper = Math.abs(n[0]) < 0.9 ? [1, 0, 0] : [0, 1, 0];
    const u = unit(cross(helper, n));
    const v = cross(n, u);
    const flat = items.map((i) => {
      const poly = quads[i].pos.map((p) => [dot(p, u), dot(p, v)]);
      return area2(poly) < 0 ? poly.reverse() : poly;
    });
    const planes = items.map((i) => dot(n, quads[i].pos[0]));
    for (let x = 0; x < items.length; x++) {
      for (let y = x + 1; y < items.length; y++) {
        if (Math.abs(planes[x] - planes[y]) > tolerance) continue;
        const area = overlapArea(flat[x], flat[y]);
        if (area > minArea) out.push({ a: items[x], b: items[y], area });
      }
    }
  }
  return out;
}

/** A short human description of a quad for reports: its facing and its corner bounds. */
export function describeQuad(q) {
  const lo = [0, 1, 2].map((k) => Math.min(...q.pos.map((p) => p[k])));
  const hi = [0, 1, 2].map((k) => Math.max(...q.pos.map((p) => p[k])));
  const r = (v) => Math.round(v * 100) / 100;
  return `${q.ref ?? q.texture} [${lo.map(r)}]..[${hi.map(r)}]`;
}
