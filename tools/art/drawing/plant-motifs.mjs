// Reusable botanical palette and motifs, independent of feature catalogues.
import { hex, set } from './sprites.mjs';

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
