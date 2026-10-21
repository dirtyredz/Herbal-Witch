// Depth-buffered CPU rasterization of baked quads: orthographic projection, nearest-neighbour
// texture sampling, cutout alpha (texels under 10% alpha are discarded, as the game's cutout layer
// does), back-face culling, Minecraft's directional face shading (Forge's diffuse-light formula)
// and full brightness for emissive faces. Renders supersampled, then box-filters down, so edges
// are clean but output stays deterministic.
import { project } from './camera.mjs';

const ALPHA_CUTOFF = 26;
const MISSING = [[248, 0, 248], [0, 0, 0]];

/** Minecraft's face shade for a unit normal (up 1.0, down 0.5, north/south 0.8, east/west 0.6). */
export function diffuse([x, y, z]) {
  const l = Math.hypot(x, y, z) || 1;
  const [nx, ny, nz] = [x / l, y / l, z / l];
  return Math.min(1, nx * nx * 0.6 + ny * ny * (ny > 0 ? 1 : 0.5) + nz * nz * 0.8);
}

/**
 * Renders quads with camera `cam` at `scale` output pixels per model pixel. `textures(id)` returns
 * {width, height, rgba} or null. Options: frame = {minX, minY, maxX, maxY} in projected model
 * units (default: fitted to the quads plus a margin), ss = supersampling factor.
 * Returns {image: {width, height, px}, missing: Set of texture ids that were absent}.
 */
export function render(quads, cam, textures, { scale = 8, frame = null, ss = 3, margin = 1 } = {}) {
  const projected = quads.map((q) => q.pos.map((p) => project(cam, p)));
  if (!frame) {
    let minX = Infinity;
    let minY = Infinity;
    let maxX = -Infinity;
    let maxY = -Infinity;
    for (const pts of projected) {
      for (const [x, y] of pts) {
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
      }
    }
    if (!Number.isFinite(minX)) minX = minY = maxX = maxY = 0;
    frame = { minX: minX - margin, minY: minY - margin, maxX: maxX + margin, maxY: maxY + margin };
  }
  const width = Math.max(1, Math.ceil((frame.maxX - frame.minX) * scale));
  const height = Math.max(1, Math.ceil((frame.maxY - frame.minY) * scale));
  const W = width * ss;
  const H = height * ss;
  const k = scale * ss;
  const colour = new Uint8Array(W * H * 4);
  const depth = new Float64Array(W * H).fill(Infinity);
  const missing = new Set();

  quads.forEach((q, qi) => {
    const n = q.normal;
    const facing = n[0] * cam.forward[0] + n[1] * cam.forward[1] + n[2] * cam.forward[2];
    if (facing >= -1e-9) return; // back face
    const tex = q.texture ? textures(q.texture) : null;
    if (!tex) missing.add(q.texture ?? `unbound ${q.ref}`);
    const light = q.emissive || !q.shade ? 1 : diffuse(n);
    const pts = projected[qi].map(([x, y, z]) => [(x - frame.minX) * k, (y - frame.minY) * k, z]);
    for (const [a, b, c] of [[0, 1, 2], [0, 2, 3]]) {
      triangle(pts[a], pts[b], pts[c], q.uv[a], q.uv[b], q.uv[c]);
    }

    function triangle(p0, p1, p2, t0, t1, t2) {
      const area = (p1[0] - p0[0]) * (p2[1] - p0[1]) - (p2[0] - p0[0]) * (p1[1] - p0[1]);
      if (Math.abs(area) < 1e-12) return;
      const x0 = Math.max(0, Math.floor(Math.min(p0[0], p1[0], p2[0])));
      const x1 = Math.min(W - 1, Math.ceil(Math.max(p0[0], p1[0], p2[0])));
      const y0 = Math.max(0, Math.floor(Math.min(p0[1], p1[1], p2[1])));
      const y1 = Math.min(H - 1, Math.ceil(Math.max(p0[1], p1[1], p2[1])));
      for (let y = y0; y <= y1; y++) {
        for (let x = x0; x <= x1; x++) {
          const px = x + 0.5;
          const py = y + 0.5;
          const w0 = ((p1[0] - px) * (p2[1] - py) - (p2[0] - px) * (p1[1] - py)) / area;
          const w1 = ((p2[0] - px) * (p0[1] - py) - (p0[0] - px) * (p2[1] - py)) / area;
          const w2 = 1 - w0 - w1;
          if (w0 < -1e-9 || w1 < -1e-9 || w2 < -1e-9) continue;
          const z = w0 * p0[2] + w1 * p1[2] + w2 * p2[2];
          const i = y * W + x;
          if (z >= depth[i] - 1e-6) continue;
          const u = w0 * t0[0] + w1 * t1[0] + w2 * t2[0];
          const v = w0 * t0[1] + w1 * t1[1] + w2 * t2[1];
          let rgb;
          if (tex) {
            const tx = Math.min(tex.width - 1, Math.max(0, Math.floor((u / 16) * tex.width)));
            const ty = Math.min(tex.height - 1, Math.max(0, Math.floor((v / 16) * tex.height)));
            const ti = (ty * tex.width + tx) * 4;
            if (tex.rgba[ti + 3] < ALPHA_CUTOFF) continue;
            rgb = [tex.rgba[ti], tex.rgba[ti + 1], tex.rgba[ti + 2]];
          } else {
            rgb = MISSING[(Math.floor(u / 2) + Math.floor(v / 2)) & 1];
          }
          depth[i] = z;
          colour[i * 4] = Math.round(rgb[0] * light);
          colour[i * 4 + 1] = Math.round(rgb[1] * light);
          colour[i * 4 + 2] = Math.round(rgb[2] * light);
          colour[i * 4 + 3] = 255;
        }
      }
    }
  });

  const px = new Uint8Array(width * height * 4);
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      let r = 0;
      let g = 0;
      let b = 0;
      let a = 0;
      for (let sy = 0; sy < ss; sy++) {
        for (let sx = 0; sx < ss; sx++) {
          const i = ((y * ss + sy) * W + x * ss + sx) * 4;
          if (colour[i + 3]) {
            r += colour[i];
            g += colour[i + 1];
            b += colour[i + 2];
            a++;
          }
        }
      }
      if (a) px.set([Math.round(r / a), Math.round(g / a), Math.round(b / a), Math.round((a * 255) / (ss * ss))], (y * width + x) * 4);
    }
  }
  return { image: { width, height, px }, missing, frame };
}
