// Dry-ground herbs: rosemary, thyme.
import { R, set, line, sprite, seeds, grow, sprout } from './sprites.mjs';

const needles = (cv, x, y, side, r) => {
  set(cv, x + side, y, r[1]);
  set(cv, x + side * 2, y - 1, r[0]);
};

const rosemary = grow(
  (cv, s) => {
    if (s === 1) {
      line(cv, 7, 15, 7, 9, R.wood[1]);
      for (let y = 10; y <= 13; y += 1.5) {
        needles(cv, 7, y, -1, R.needle);
        needles(cv, 7, y + 0.5, 1, R.needle);
      }
      return;
    }
    const stems = [[5, 7], [8, 4]];
    line(cv, 7, 15, 7, 12, R.wood[1]);
    for (const [x, y] of stems) {
      line(cv, 7, 12, x, y, R.wood[1]);
      for (let t = y + 1; t <= 12; t += 1) {
        const px = x + (7 - x) * ((t - y) / (12 - y + 0.01));
        needles(cv, Math.round(px), t, -1, R.needle);
        needles(cv, Math.round(px), t + 0.5, 1, R.needle);
      }
      set(cv, x, y - 1, R.needle[2]);
    }
  },
  (cv) => sprout(cv, 7, R.needle, 3, 0, 2),
);

const thymeMat = (cv, s) => {
  const spread = s === 1 ? 3 : 5;
  const top = s === 1 ? 13 : 11;
  for (let x = 8 - spread; x <= 7 + spread; x++) {
    const d = Math.abs(x - 7.5);
    const h = Math.round(top + (d / spread) * (15 - top));
    for (let y = h; y <= 15; y++) {
      set(cv, x, y, (x + y) % 3 === 0 ? R.moss[2] : (x * 2 + y) % 3 === 0 ? R.moss[0] : R.moss[1], true);
    }
  }
  if (s >= 2) line(cv, 7, 15, 3, 10, R.wood[0]);
  if (s >= 2) line(cv, 8, 15, 12, 11, R.wood[0]);
};

export default {
  rosemary: {
    stages: rosemary,
    seeds: seeds(1, R.needle, 'long'),
    fresh: sprite((cv) => {
      line(cv, 3, 14, 12, 3, R.wood[1]);
      for (let i = 0; i < 8; i++) {
        const x = 4 + i;
        const y = 13 - i;
        set(cv, x - 1, y - 1, R.needle[1]);
        set(cv, x + 1, y + 1, R.needle[0]);
        set(cv, x - 2, y - 1, R.needle[0]);
        set(cv, x + 2, y + 1, R.needle[1]);
      }
      set(cv, 10, 7, R.blue[2]);
      set(cv, 6, 11, R.blue[2]);
      line(cv, 3, 14, 3, 15, R.wood[0]);
    }),
  },
  thyme: {
    stages: grow(thymeMat, (cv) => sprout(cv, 7, R.moss, 1, 0, 2.5)),
    seeds: seeds(2, R.wood, 'speck'),
    fresh: sprite((cv) => {
      line(cv, 3, 13, 12, 6, R.wood[0]);
      line(cv, 6, 11, 5, 6, R.wood[0]);
      line(cv, 9, 9, 11, 12, R.wood[0]);
      for (const [x, y] of [[3, 12], [5, 11], [7, 9], [9, 8], [11, 6], [5, 7], [5, 5], [10, 11], [11, 13], [7, 12], [4, 14]]) {
        set(cv, x, y, R.moss[1], true);
        set(cv, x + 1, y, R.moss[2]);
      }
      for (const [x, y] of [[6, 5], [12, 5], [8, 10], [4, 9]]) set(cv, x, y, R.rose[2]);
    }),
  },
};
