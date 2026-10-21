// Shared 16 x 16 drawing primitives for the herb sprites. A Canvas holds colour-or-null pixels;
// finish() turns it into an image() with strictly 0/255 alpha (cutout rendering) and darkens the
// outer edge of shaded shapes so small plants read against any background.
import { image, rng } from '../pixels.mjs';

export { rng };
export const N = 16;

export const hex = (s) => [1, 3, 5].map((i) => parseInt(s.slice(i, i + 2), 16));

/** Colour ramps, dark / mid / light. Muted and slightly warm; toxic herbs sit darker. */
export const R = Object.fromEntries(
  Object.entries({
    grey: ['#4f6a55', '#76927a', '#9fb89f'],
    sage: ['#6f8872', '#98b09c', '#c6d6c8'],
    mint: ['#3f7a32', '#62a845', '#97d065'],
    dark: ['#243d2a', '#365a38', '#4f7f4a'],
    moss: ['#3f5d37', '#5f8a4a', '#8db55f'],
    fern: ['#4e7048', '#74986a', '#a0be8c'],
    needle: ['#2a4431', '#3e6244', '#5c8657'],
    deep: ['#1f3322', '#2d4a2e', '#436a3f'],
    wood: ['#4a3220', '#6b4a2f', '#8f6a44'],
    violet: ['#563783', '#7a56ad', '#a98bd8'],
    cream: ['#d9ccaa', '#efe4c6', '#fbf5e4'],
    yellow: ['#c9992a', '#e8bd3e', '#f6d86a'],
    pink: ['#c98a9d', '#e5aebe', '#f3ccd6'],
    rose: ['#b0607a', '#d4859c', '#eaaabb'],
    plum: ['#4a2a55', '#6b3f7a', '#8f5f9e'],
    blue: ['#3f5390', '#5f78b8', '#8fa4d8'],
    red: ['#8c2a2e', '#bd3b3d', '#e0675f'],
    black: ['#14101a', '#2a2232', '#6a5f78'],
    root: ['#a58a5c', '#c9b07e', '#e6d4a6'],
    twine: ['#8a6a44', '#b08d5c', '#d2b27c'],
    silver: ['#8fa59a', '#b9cbc1', '#e3ece6'],
  }).map(([k, v]) => [k, v.map(hex)]),
);

export function canvas() {
  return { px: new Array(N * N).fill(null), shaded: new Array(N * N).fill(false) };
}

export function set(cv, x, y, c, shaded = false) {
  x = Math.round(x);
  y = Math.round(y);
  if (x < 0 || y < 0 || x >= N || y >= N) return;
  cv.px[y * N + x] = c;
  cv.shaded[y * N + x] = shaded;
}

export function line(cv, x0, y0, x1, y1, c) {
  const n = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0), 1);
  for (let i = 0; i <= n; i++) set(cv, x0 + ((x1 - x0) * i) / n, y0 + ((y1 - y0) * i) / n, c);
}

/** Pointed leaf from (x, y) at deg (0 = right, 90 = up), length len, half-width w; jag adds serration. */
export function leaf(cv, x, y, deg, len, w, r, jag = 0) {
  const a = (deg * Math.PI) / 180;
  const dx = Math.cos(a);
  const dy = -Math.sin(a);
  for (let t = 0; t <= len; t += 0.5) {
    const hw = w * Math.sin((Math.PI * t) / len) + (jag && Math.round(t) % 2 === 0 ? jag : 0);
    for (let s = -Math.ceil(hw); s <= Math.ceil(hw); s += 0.5) {
      if (Math.abs(s) > hw + 0.01) continue;
      const ox = -dy * s;
      const oy = dx * s;
      const up = ox + oy < -0.3;
      const dn = ox + oy > 0.3;
      set(cv, x + dx * t + ox, y + dy * t + oy, up ? r[2] : dn ? r[0] : r[1], true);
    }
  }
}

/** Shaded filled ellipse. */
export function blob(cv, cx, cy, rx, ry, r) {
  for (let y = Math.floor(cy - ry); y <= Math.ceil(cy + ry); y++) {
    for (let x = Math.floor(cx - rx); x <= Math.ceil(cx + rx); x++) {
      const d = ((x - cx) / rx) ** 2 + ((y - cy) / ry) ** 2;
      if (d > 1.02) continue;
      const l = x - cx + (y - cy);
      set(cv, x, y, l < -0.6 ? r[2] : l > 0.8 ? r[0] : r[1], true);
    }
  }
}

/** Plus-shaped bloom (petals N/E/S/W) with a centre pixel; full = also the four corners (a daisy). */
export function bloom(cv, x, y, petal, centre, full = false) {
  for (const [dx, dy] of [[0, -1], [1, 0], [0, 1], [-1, 0]]) set(cv, x + dx, y + dy, petal);
  if (full) for (const [dx, dy] of [[-1, -1], [1, -1], [1, 1], [-1, 1]]) set(cv, x + dx, y + dy, petal);
  set(cv, x, y, centre);
}

/** 2 x 2 berry with a highlight pixel. */
export function berry(cv, x, y, r, hi) {
  set(cv, x, y, r[1]);
  set(cv, x + 1, y, r[1]);
  set(cv, x, y + 1, r[0]);
  set(cv, x + 1, y + 1, r[0]);
  set(cv, x, y, hi);
}

/** Drooping bell flower hanging from (x, y). */
export function bell(cv, x, y, r) {
  set(cv, x, y, r[0]);
  set(cv, x - 1, y + 1, r[1]);
  set(cv, x, y + 1, r[2]);
  set(cv, x + 1, y + 1, r[1]);
  set(cv, x - 1, y + 2, r[0]);
  set(cv, x + 1, y + 2, r[0]);
  set(cv, x, y + 2, r[1]);
}

/** Tiny seedling: stem of height h and a leaf pair. Stage 0 of most herbs. */
export function sprout(cv, x, r, h = 2, lean = 0, len = 2.5) {
  line(cv, x, 15, x + lean, 15 - h, r[1]);
  leaf(cv, x + lean, 15 - h, 160, len, 0.8, r);
  leaf(cv, x + lean, 15 - h, 20, len, 0.8, r);
}

/** Opaque pixel count helper for tests and tuning. */
export const count = (img) => img.px.filter((p) => p[3] === 255).length;

/** Converts a canvas to an image, darkening the edge of shaded shapes. */
export function finish(cv) {
  const at = (x, y) => (x < 0 || y < 0 || x >= N || y >= N ? undefined : cv.px[y * N + x]);
  return image(N, (x, y) => {
    const c = cv.px[y * N + x];
    if (!c) return [0, 0, 0, 0];
    let [r, g, b] = c;
    if (cv.shaded[y * N + x]) {
      const open = [[1, 0], [-1, 0], [0, -1]].some(([dx, dy]) => at(x + dx, y + dy) === null);
      if (open) [r, g, b] = [r * 0.82, g * 0.82, b * 0.82].map(Math.round);
    }
    return [r, g, b, 255];
  });
}

/** Builds a 16 x 16 sprite from a draw function: sprite((cv) => ...). */
export const sprite = (draw) => () => {
  const cv = canvas();
  draw(cv);
  return finish(cv);
};

/** The four growth stages of a herb: s0 draws the seedling, draw(cv, stage) the stages 1-3. */
export const grow = (draw, s0) => [sprite(s0), ...[1, 2, 3].map((s) => sprite((cv) => draw(cv, s)))];

const LAYOUTS = [
  [[6, 7], [9, 6], [8, 10], [5, 10], [11, 9]],
  [[7, 5], [5, 8], [9, 8], [7, 11], [11, 11]],
  [[4, 8], [7, 7], [10, 8], [6, 11], [9, 11], [12, 6]],
  [[8, 6], [6, 9], [10, 10], [4, 11], [8, 12]],
];

/** Seed cluster: layout picked by id, colours from ramp r, shape 'oval' | 'round' | 'speck' | 'long'. */
export function seeds(id, r, shape, extra = null) {
  return sprite((cv) => {
    const rand = rng(900 + id * 17);
    for (const [x, y] of LAYOUTS[id % LAYOUTS.length]) {
      const jx = Math.floor(rand() * 2);
      const jy = Math.floor(rand() * 2);
      const sx = x + jx;
      const sy = y + jy;
      if (shape === 'oval') {
        set(cv, sx, sy, r[1]);
        set(cv, sx + 1, sy, r[0]);
        set(cv, sx, sy - 1, r[2]);
      } else if (shape === 'round') {
        set(cv, sx, sy, r[2]);
        set(cv, sx + 1, sy, r[1]);
        set(cv, sx, sy + 1, r[1]);
        set(cv, sx + 1, sy + 1, r[0]);
      } else if (shape === 'long') {
        set(cv, sx, sy, r[1]);
        set(cv, sx, sy + 1, r[0]);
        set(cv, sx, sy - 1, r[2]);
      } else {
        set(cv, sx, sy, r[1]);
        set(cv, sx + 1, sy + 1, r[0]);
      }
    }
    if (extra) extra(cv);
  });
}
