// Cuboid elements to textured quads, following Minecraft 1.20.1's FaceBakery: default UVs per
// face, explicit UVs, face UV rotation, element rotation about an origin on one axis (any angle
// the file states; legality is checked separately) with optional rescale. Coordinates stay in
// model pixels (0..16 per block); one quad = 4 corners in counter-clockwise order seen from outside.

export const FACES = ['down', 'up', 'north', 'south', 'west', 'east'];

export const NORMALS = {
  down: [0, -1, 0], up: [0, 1, 0], north: [0, 0, -1], south: [0, 0, 1], west: [-1, 0, 0], east: [1, 0, 0],
};

/** The four corners of a face, each with its default (s, t) parameter in [0,1]: s along u, t along v. */
function corners(face, [x0, y0, z0], [x1, y1, z1]) {
  switch (face) {
    case 'north': // seen from -z: u runs east -> west
      return [[[x1, y1, z0], 0, 0], [[x0, y1, z0], 1, 0], [[x0, y0, z0], 1, 1], [[x1, y0, z0], 0, 1]];
    case 'south':
      return [[[x0, y1, z1], 0, 0], [[x1, y1, z1], 1, 0], [[x1, y0, z1], 1, 1], [[x0, y0, z1], 0, 1]];
    case 'west':
      return [[[x0, y1, z0], 0, 0], [[x0, y1, z1], 1, 0], [[x0, y0, z1], 1, 1], [[x0, y0, z0], 0, 1]];
    case 'east':
      return [[[x1, y1, z1], 0, 0], [[x1, y1, z0], 1, 0], [[x1, y0, z0], 1, 1], [[x1, y0, z1], 0, 1]];
    case 'up': // top of texture is north
      return [[[x0, y1, z0], 0, 0], [[x1, y1, z0], 1, 0], [[x1, y1, z1], 1, 1], [[x0, y1, z1], 0, 1]];
    case 'down': // top of texture is south
      return [[[x0, y0, z1], 0, 0], [[x1, y0, z1], 1, 0], [[x1, y0, z0], 1, 1], [[x0, y0, z0], 0, 1]];
    default:
      throw new Error(`unknown face ${face}`);
  }
}

/** Minecraft's default UV rectangle for a face of the cuboid from..to. */
export function defaultUv(face, [x0, y0, z0], [x1, y1, z1]) {
  switch (face) {
    case 'down': return [x0, 16 - z1, x1, 16 - z0];
    case 'up': return [x0, z0, x1, z1];
    case 'north': return [16 - x1, 16 - y1, 16 - x0, 16 - y0];
    case 'south': return [x0, 16 - y1, x1, 16 - y0];
    case 'west': return [z0, 16 - y1, z1, 16 - y0];
    case 'east': return [16 - z1, 16 - y1, 16 - z0, 16 - y0];
    default: throw new Error(`unknown face ${face}`);
  }
}

/** Face-parameter (s, t) after a clockwise texture rotation of `deg` (0/90/180/270). */
function rotateParam(s, t, deg) {
  switch (((deg % 360) + 360) % 360) {
    case 0: return [s, t];
    case 90: return [t, 1 - s];
    case 180: return [1 - s, 1 - t];
    case 270: return [1 - t, s];
    default: throw new Error(`face rotation ${deg} is not a multiple of 90`);
  }
}

/** Rotates point p about `origin` around `axis` by `deg` (right-handed), with optional rescale. */
export function rotateAbout(p, { origin, axis, angle, rescale }) {
  const a = (angle * Math.PI) / 180;
  const c = Math.cos(a);
  const s = Math.sin(a);
  const [x, y, z] = [p[0] - origin[0], p[1] - origin[1], p[2] - origin[2]];
  let r;
  if (axis === 'x') r = [x, y * c - z * s, y * s + z * c];
  else if (axis === 'y') r = [x * c + z * s, y, -x * s + z * c];
  else if (axis === 'z') r = [x * c - y * s, x * s + y * c, z];
  else throw new Error(`bad rotation axis ${axis}`);
  if (rescale && angle !== 0) {
    const k = 1 / Math.cos(a);
    r = [axis === 'x' ? r[0] : r[0] * k, axis === 'y' ? r[1] : r[1] * k, axis === 'z' ? r[2] : r[2] * k];
  }
  return [r[0] + origin[0], r[1] + origin[1], r[2] + origin[2]];
}

export function rotateNormal(n, axis, angle) {
  return rotateAbout(n, { origin: [0, 0, 0], axis, angle, rescale: false });
}

/**
 * Bakes one element. `texture(ref)` resolves a face's '#var' to a texture id (null = unbound).
 * Returns quads {pos: [4 x [x,y,z]], uv: [4 x [u,v]] (0..16 units), texture, normal, shade,
 * emissive, cullface, tint}.
 */
export function bakeElement(element, texture) {
  const { from, to } = element;
  const quads = [];
  const emissive = (element.forge_data?.block_light ?? 0) > 0 || element.forge_data?.sky_light > 0;
  for (const face of FACES) {
    const f = element.faces?.[face];
    if (!f) continue;
    const uv = f.uv ?? defaultUv(face, from, to);
    const pos = [];
    const uvs = [];
    for (const [p, s, t] of corners(face, from, to)) {
      const [rs, rt] = rotateParam(s, t, f.rotation ?? 0);
      uvs.push([uv[0] + (uv[2] - uv[0]) * rs, uv[1] + (uv[3] - uv[1]) * rt]);
      pos.push(element.rotation ? rotateAbout(p, element.rotation) : p);
    }
    const normal = element.rotation
      ? rotateNormal(NORMALS[face], element.rotation.axis, element.rotation.angle)
      : NORMALS[face];
    quads.push({
      pos, uv: uvs, texture: texture(f.texture), ref: f.texture, normal,
      shade: element.shade !== false, emissive, cullface: f.cullface ?? null, tint: f.tintindex ?? -1,
    });
  }
  return quads;
}
