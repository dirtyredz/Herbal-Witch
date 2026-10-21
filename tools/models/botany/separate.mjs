// Organic models (herbs, bundles) are many small swatch cubes placed by rule and by seed, so two of
// them often end up sharing a face plane: a stem and the bud on it, a pair of leaves from one
// node, crossing petals. Each such pair z-fights. This pass finds them with the previewer's
// detector and pulls the smaller face of each pair a little inward along its own normal, which no
// eye can see on a cube a pixel wide but which takes the faces off one plane. Built pieces
// (stations, furniture) are fixed by hand instead: there every face is placed deliberately.
import { bakeElement } from '../../preview/geometry/elements.mjs';
import { coplanarOverlaps } from '../../preview/geometry/coplanar.mjs';
import { finish } from '../geometry.mjs';

const STEP = 0.12;
const AXIS = { west: 0, east: 0, down: 1, up: 1, north: 2, south: 2 };
const POSITIVE = new Set(['east', 'up', 'south']);

/** Faces of a finished element in baking order (bakeElement walks FACES in order). */
const FACE_ORDER = ['down', 'up', 'north', 'south', 'west', 'east'];

function area(e, face) {
  const a = AXIS[face];
  const [i, j] = [0, 1, 2].filter((k) => k !== a);
  return (e.to[i] - e.from[i]) * (e.to[j] - e.from[j]);
}

/** Returns the elements with no coplanar overlaps left (throws if it cannot get there). */
export function separateCoplanar(elements) {
  let els = elements.map((e) => ({ ...e, from: [...e.from], to: [...e.to] }));
  for (let round = 0; round < 12; round++) {
    const quads = [];
    els.forEach((e, i) => {
      const faces = FACE_ORDER.filter((f) => e.faces[f]);
      finish([e]).forEach((fe) => bakeElement(fe, () => 'x').forEach((q, k) => quads.push({ ...q, element: i, face: faces[k] })));
    });
    const pairs = coplanarOverlaps(quads);
    if (!pairs.length) return els;
    const moved = new Set();
    for (const { a, b } of pairs) {
      const qa = quads[a];
      const qb = quads[b];
      if (moved.has(qa.element) || moved.has(qb.element)) continue;
      // move the smaller face; on a tie the later element
      const sa = area(els[qa.element], qa.face);
      const sb = area(els[qb.element], qb.face);
      const q = sa < sb || (sa === sb && qa.element > qb.element) ? qa : qb;
      const e = els[q.element];
      const k = AXIS[q.face];
      const size = e.to[k] - e.from[k];
      const step = Math.min(STEP, size / 3);
      if (POSITIVE.has(q.face)) e.to[k] -= step;
      else e.from[k] += step;
      moved.add(q.element);
    }
  }
  throw new Error('could not separate coplanar faces');
}
