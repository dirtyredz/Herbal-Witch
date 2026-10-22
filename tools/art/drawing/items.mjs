// Neutral item palette and four-edge outlining. The outside canvas is an open edge here.
import { image } from '../pixels.mjs';
import { ramp, N, canvas, set } from './sprites.mjs';
import { R } from './plant-motifs.mjs';

/** Shared palette: the owner's accents (hot pink, deep green, dark purple) plus container materials. */
export const P = {
  pink: ramp('#a82a5c', '#d8407a', '#ee7aa4'),
  green: ramp('#1f3f1e', '#2f5a2c', '#4a8040'),
  purple: ramp('#2f1a3a', '#4a2a55', '#6b4a7a'),
  paper: ramp('#b9a47c', '#d8c89c', '#efe4c6'),
  kraft: ramp('#8a6a44', '#b08d5c', '#d2b27c'),
  glass: ramp('#6f969c', '#a9ccd0', '#e0f0ee'),
  cork: ramp('#7a5530', '#a47a46', '#c79a5e'),
  burlap: ramp('#8f7440', '#b99a5c', '#d5ba7c'),
  amber: ramp('#a8601a', '#d88a22', '#f2b650'),
  cream: R.cream,
  wood: R.wood,
  twine: R.twine,
};

export function rect(cv, x0, y0, x1, y1, c, shaded = true) {
  for (let y = y0; y <= y1; y++) for (let x = x0; x <= x1; x++) set(cv, x, y, c, shaded);
}

/** Fills rows given as [y, x0, x1, colour] spans. */
export function spans(cv, rows, shaded = true) {
  for (const [y, x0, x1, c] of rows) for (let x = x0; x <= x1; x++) set(cv, x, y, c, shaded);
}

/** Like the herb sprites' finish(), but darkens every open edge of a shaded shape, all four sides. */
export function finishOutlined(cv, edge = 0.6) {
  const at = (x, y) => (x < 0 || y < 0 || x >= N || y >= N ? null : cv.px[y * N + x]);
  return image(N, (x, y) => {
    const c = cv.px[y * N + x];
    if (!c) return [0, 0, 0, 0];
    let [r, g, b] = c;
    if (cv.shaded[y * N + x] && [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([dx, dy]) => at(x + dx, y + dy) === null)) {
      [r, g, b] = [r * edge, g * edge, b * edge].map(Math.round);
    }
    return [r, g, b, 255];
  });
}

/** A 16 x 16 outlined sprite from a draw function. */
export const item = (draw) => () => {
  const cv = canvas();
  draw(cv);
  return finishOutlined(cv);
};
