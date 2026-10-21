// Joined herb beds are watertight: for every arrangement (line, 2x2, L, U, T, plus, ring, diagonals)
// and at every wall height, nothing standing outside the planter can reach its inside through a gap
// in the wooden frame (owner feedback 0.2.0-dev.6: the inside corner of an L showed a notch). Beds
// take the connection props the game derives (sides and inner corners), then each cell's real
// blockstate parts are rasterised into a horizontal slice and flood-filled from the outside.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { Resources } from '../assets/resources.mjs';
import { Baker } from '../geometry/bake.mjs';
import { select } from '../scenes/blockstates.mjs';
import { beds } from '../scenes/catalogue.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '../../..');
const STEP = 0.25;
/** Heights (model px) through the skirt, lower rail, groove, upper rail and the rim above the soil. */
const HEIGHTS = [0.5, 4, 7.75, 12, 14.5];
/** The wall and post are at most this thick: nearer an exposed side than this is frame, not planter. */
const FRAME = 2.75;

const SHAPES = {
  single: [[0, 0]],
  line: [[0, 0], [1, 0], [2, 0]],
  square: [[0, 0], [1, 0], [0, 1], [1, 1]],
  L: [[0, 0], [1, 0], [0, 1]],
  'L reversed': [[1, 0], [0, 1], [1, 1]],
  U: [[0, 0], [2, 0], [0, 1], [1, 1], [2, 1]],
  'U upside down': [[0, 0], [1, 0], [2, 0], [0, 1], [2, 1]],
  T: [[0, 0], [1, 0], [2, 0], [1, 1]],
  plus: [[1, 0], [0, 1], [1, 1], [2, 1], [1, 2]],
  ring: [[0, 0], [1, 0], [2, 0], [0, 1], [2, 1], [0, 2], [1, 2], [2, 2]],
  'ring with a notch': [[0, 0], [1, 0], [2, 0], [0, 1], [2, 1], [0, 2], [2, 2]],
  'two beds meeting at a corner': [[0, 0], [1, 1]],
  'staircase': [[0, 0], [1, 0], [1, 1], [2, 1], [2, 2]],
};

/** [x0,y0,z0,x1,y1,z1] boxes of one cell's parts, turned by their blockstate rotation. */
function cellBoxes(baker, parts, cellX, cellZ) {
  const out = [];
  for (const part of parts) {
    for (const e of baker.bake(part.model).model.elements) {
      let [x0, , z0] = e.from;
      let [x1, , z1] = e.to;
      for (let k = 0; k < (part.y ?? 0) / 90; k++) [x0, z0, x1, z1] = [16 - z1, x0, 16 - z0, x1]; // a quarter turn clockwise
      out.push([x0 + cellX * 16, e.from[1], z0 + cellZ * 16, x1 + cellX * 16, e.to[1], z1 + cellZ * 16]);
    }
  }
  return out;
}

/** Cells (grid indices) from which the flood reaches the inside of the planter at height y. */
function leaks(boxes, entries, y, pad) {
  const xs = entries.map((e) => e.at[0]);
  const zs = entries.map((e) => e.at[2]);
  const minX = Math.min(...xs) * 16 - pad, minZ = Math.min(...zs) * 16 - pad;
  const w = Math.round(((Math.max(...xs) + 1) * 16 + pad - minX) / STEP);
  const d = Math.round(((Math.max(...zs) + 1) * 16 + pad - minZ) / STEP);
  const solid = new Uint8Array(w * d);
  for (const [x0, y0, z0, x1, y1, z1] of boxes) {
    if (y < y0 || y >= y1) continue;
    for (let i = Math.round((x0 - minX) / STEP); i < Math.round((x1 - minX) / STEP); i++) {
      for (let j = Math.round((z0 - minZ) / STEP); j < Math.round((z1 - minZ) / STEP); j++) solid[j * w + i] = 1;
    }
  }
  const bed = new Map(entries.map((e) => [`${e.at[0]},${e.at[2]}`, e.props]));
  const inside = (px, pz) => {
    const cx = Math.floor(px / 16), cz = Math.floor(pz / 16);
    const props = bed.get(`${cx},${cz}`);
    if (!props) return false;
    const lx = px - cx * 16, lz = pz - cz * 16;
    if ((props.north === false && lz < FRAME) || (props.south === false && lz > 16 - FRAME)) return false;
    if ((props.west === false && lx < FRAME) || (props.east === false && lx > 16 - FRAME)) return false;
    // the inner-corner square belongs to the wall, not the planter
    return !((props.inner_north_east && lx > 16 - FRAME && lz < FRAME) || (props.inner_east_south && lx > 16 - FRAME && lz > 16 - FRAME)
      || (props.inner_south_west && lx < FRAME && lz > 16 - FRAME) || (props.inner_west_north && lx < FRAME && lz < FRAME));
  };
  const seen = new Uint8Array(w * d);
  const stack = [];
  for (let j = 0; j < d; j++) {
    for (let i = 0; i < w; i++) {
      const px = minX + (i + 0.5) * STEP, pz = minZ + (j + 0.5) * STEP;
      // seeds: every open point outside any bed cell
      if (!solid[j * w + i] && !bed.has(`${Math.floor(px / 16)},${Math.floor(pz / 16)}`)) {
        seen[j * w + i] = 1;
        stack.push(j * w + i);
      }
    }
  }
  const found = new Set();
  while (stack.length) {
    const n = stack.pop();
    const i = n % w, j = (n - i) / w;
    const px = minX + (i + 0.5) * STEP, pz = minZ + (j + 0.5) * STEP;
    if (inside(px, pz)) found.add(`(${(px / 16).toFixed(2)}, ${(pz / 16).toFixed(2)}) cells`);
    for (const [di, dj] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const ni = i + di, nj = j + dj;
      if (ni < 0 || nj < 0 || ni >= w || nj >= d || seen[nj * w + ni] || solid[nj * w + ni]) continue;
      seen[nj * w + ni] = 1;
      stack.push(nj * w + ni);
    }
  }
  return [...found];
}

test('no arrangement of joined beds can be seen into or reached through the frame', () => {
  const res = new Resources(root);
  const baker = new Baker(res);
  const bs = res.json('blockstates', 'herbalwitch:botany/garden/herb_bed');
  for (const [name, cells] of Object.entries(SHAPES)) {
    const entries = beds(cells, { moisture: 0, enriched: false });
    const boxes = entries.flatMap((e) => cellBoxes(baker, select(bs, e.props), e.at[0], e.at[2]));
    for (const y of HEIGHTS) {
      const hit = leaks(boxes, entries, y, 8);
      assert.deepEqual(hit.slice(0, 3), [], `${name}: a gap in the frame at height ${y} reaches the planter`);
    }
  }
});

test('an inner corner flag is only ever set between two joined sides with the diagonal bed missing', () => {
  const [l] = beds(SHAPES.L.slice(0, 1).concat(SHAPES.L.slice(1)), {}).filter((e) => e.at[0] === 0 && e.at[2] === 0);
  assert.equal(l.props.inner_east_south, true); // the bed at the elbow of the L
  assert.equal(l.props.inner_north_east, false);
  for (const e of beds(SHAPES.square, {})) {
    for (const k of ['inner_north_east', 'inner_east_south', 'inner_south_west', 'inner_west_north']) assert.equal(e.props[k], false, `${k} in a full 2x2`);
  }
});
