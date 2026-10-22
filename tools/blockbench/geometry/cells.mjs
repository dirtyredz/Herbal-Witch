// Multiblock cells and the cutting of assembly-space geometry into them. A cell is
// {right, up, depth}: steps to the viewer's right, upward and toward the back, matching the Java
// layout (platform/multiblock/MultiblockLayout). In assembly coordinates cell 0 is x 0..16,
// y 0..16, z 0..16; a step right is x - 16, up y + 16, back (depth) z + 16. Elements are cut in their
// unrotated frame, explicit UVs are cropped with the cut (flipped and rotated UVs included),
// rotation pivots move with their cell, faces created by a cut are omitted and authored omissions
// stay omitted.

const AXIS = { west: 0, east: 0, down: 1, up: 1, north: 2, south: 2 };

/** Cells of a width x height rectangle, in the Java layout's legacy order (row by row from the bottom). */
export function rectangle(width, height) {
  const cells = [];
  for (let up = 0; up < height; up++) for (let right = 0; right < width; right++) cells.push({ right, up, depth: 0 });
  return cells;
}

/**
 * The cells a descriptor declares: legacy [width, height], or an explicit list of [right, up, depth]
 * triples in canonical order (cell 0 first, the origin at [0, 0, 0]).
 */
export function declaredCells(cells) {
  if (cells.length === 2 && cells.every(Number.isInteger)) return rectangle(cells[0], cells[1]);
  return cells.map(([right, up, depth]) => ({ right, up, depth }));
}

/** Problems with an explicit cell list (empty when valid): origin first, no duplicates, face-connected. */
export function cellProblems(cells) {
  const problems = [];
  if (!cells.length) return ['no cells'];
  const key = (c) => `${c.right},${c.up},${c.depth}`;
  if (key(cells[0]) !== '0,0,0') problems.push('cell 0 must be the origin [0, 0, 0]');
  const seen = new Set();
  for (const c of cells) {
    if (![c.right, c.up, c.depth].every(Number.isInteger)) problems.push(`cell ${JSON.stringify(c)} is not whole steps`);
    if (seen.has(key(c))) problems.push(`cell ${key(c)} appears twice`);
    seen.add(key(c));
  }
  const reached = new Set([key(cells[0])]);
  const queue = [cells[0]];
  while (queue.length) {
    const c = queue.shift();
    for (const [dr, du, dd] of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
      const n = `${c.right + dr},${c.up + du},${c.depth + dd}`;
      if (seen.has(n) && !reached.has(n)) {
        reached.add(n);
        queue.push({ right: c.right + dr, up: c.up + du, depth: c.depth + dd });
      }
    }
  }
  if (reached.size !== seen.size) problems.push('cells are not face-connected');
  return problems;
}

/** The assembly-space offset (model pixels) of a cell's own coordinates. */
export const cellOffset = ({ right, up, depth }) => [-16 * right, 16 * up, 16 * (depth ?? 0)];

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

/** Moves elements by d model pixels (rotation origins too). */
export const shift = (elements, d) => elements.map((e) => ({
  ...e, from: move(e.from, d), to: move(e.to, d), ...(e.rotation ? { rotation: { ...e.rotation, origin: move(e.rotation.origin, d) } } : {}),
}));

/**
 * Splits assembly-space elements into per-cell lists in cell coordinates. With `overflow` the top
 * row also keeps what rises above the structure (a stovepipe), in its own coordinates above y = 16.
 */
export function splitCells(elements, cells, { overflow = false } = {}) {
  const out = cells.map(() => []);
  const topRow = Math.max(...cells.map((c) => c.up));
  cells.forEach((cell, ci) => {
    const lo = cellOffset(cell);
    // The legacy overflow allowance (the stovepipe) reaches y 48 of the assembly.
    const hi = [lo[0] + 16, overflow && cell.up === topRow ? Math.max(48, lo[1] + 16) : lo[1] + 16, lo[2] + 16];
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
      out[ci].push(...shift([{ ...e, from, to, faces }], lo.map((v) => -v)));
    }
  });
  return out;
}

const MIRROR_FACE = { east: 'west', west: 'east' };

/**
 * The elements mirrored left to right about x = centre (model pixels): east and west swap, every
 * face's texture is mirrored with it (u flipped, UV rotations reversed), y- and z-axis rotations
 * turn the other way. Used for the right-hinged twin of a left-hinged door.
 */
export function mirrorX(elements, centre) {
  return elements.map((e) => {
    const from = [2 * centre - e.to[0], e.from[1], e.from[2]];
    const to = [2 * centre - e.from[0], e.to[1], e.to[2]];
    const faces = {};
    for (const [f, face] of Object.entries(e.faces)) {
      const uv = [face.uv[2], face.uv[1], face.uv[0], face.uv[3]];
      const rotation = face.rotation ? (360 - face.rotation) % 360 : 0;
      const { rotation: _, ...rest } = face;
      faces[MIRROR_FACE[f] ?? f] = { ...rest, uv, ...(rotation ? { rotation } : {}) };
    }
    const rotation = e.rotation ? {
      ...e.rotation,
      angle: e.rotation.axis === 'x' ? e.rotation.angle : -e.rotation.angle,
      origin: [2 * centre - e.rotation.origin[0], e.rotation.origin[1], e.rotation.origin[2]],
    } : null;
    return { ...e, from, to, faces, ...(rotation ? { rotation } : {}) };
  });
}
