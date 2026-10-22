// Neutral 16 x 16 canvas and herb-style cutout finishing. Edge rules are intentionally distinct from items.mjs.
import { image, rng } from '../pixels.mjs';
export { rng };

export const N = 16;

export const hex = (s) => [1, 3, 5].map((i) => parseInt(s.slice(i, i + 2), 16));
export const ramp = (a, b, c) => [hex(a), hex(b), hex(c)];

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
