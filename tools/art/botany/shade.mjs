// Shade herbs: valerian, mugwort.
import { R, set, line, leaf, blob, sprite, seeds, grow, sprout } from './sprites.mjs';

const head = (cv, x, y) => {
  blob(cv, x, y, 2.2, 1.7, R.pink);
  for (const [dx, dy] of [[-1, -1], [1, 0], [0, 1], [-2, 0]]) set(cv, x + dx, y + dy, R.pink[2]);
  set(cv, x + 1, y + 1, R.rose[1]);
  set(cv, x - 1, y + 1, R.rose[0]);
};

const valerian = grow(
  (cv, s) => {
    if (s === 1) {
      line(cv, 7, 15, 7, 9, R.moss[1]);
      leaf(cv, 7, 14, 155, 3.5, 0.8, R.moss, 0.3);
      leaf(cv, 7, 14, 25, 3.5, 0.8, R.moss, 0.3);
      leaf(cv, 7, 11, 125, 2.5, 0.7, R.moss);
      return;
    }
    const tops = s === 2 ? [[7, 5], [4, 8]] : [[7, 3], [3, 6], [11, 5]];
    for (const [x, y] of tops) line(cv, 7, 15, x, y + 1, R.moss[0]);
    leaf(cv, 7, 14, 155, 4, 0.9, R.moss, 0.3);
    leaf(cv, 8, 14, 25, 4, 0.9, R.moss, 0.3);
    leaf(cv, 7, 11, 130, 3, 0.8, R.moss, 0.3);
    leaf(cv, 8, 10, 45, 3, 0.8, R.moss, 0.3);
    if (s === 3) for (const [x, y] of tops) head(cv, x, y);
  },
  (cv) => sprout(cv, 7, R.moss, 2, 0, 3),
);

const fork = (cv, x, y, deg, len, w) => {
  leaf(cv, x, y, deg, len, w, R.moss, 0.3);
  leaf(cv, x, y, deg + 28, len * 0.8, w * 0.8, R.moss, 0.3);
  leaf(cv, x, y, deg - 28, len * 0.8, w * 0.8, R.moss, 0.3);
};

const mugwort = grow(
  (cv, s) => {
    const top = s === 1 ? 9 : s === 2 ? 6 : 3;
    line(cv, 7, 15, 7, top, R.moss[0]);
    fork(cv, 7, 13, 150, 4, 0.9);
    if (s >= 2) fork(cv, 7, 12, 30, 4, 0.9);
    if (s >= 2) fork(cv, 7, 9, 145, 3.5, 0.8);
    if (s === 3) {
      fork(cv, 7, 8, 35, 3.5, 0.8);
      fork(cv, 7, 5, 140, 3, 0.7);
      fork(cv, 7, 4, 40, 3, 0.7);
      for (const [x, y] of [[4, 11], [10, 10], [4, 8], [10, 6], [5, 5], [9, 3]]) set(cv, x, y, R.silver[1]);
      for (const [x, y] of [[3, 13], [11, 13], [3, 10], [11, 8]]) set(cv, x, y, R.silver[0]);
    }
  },
  (cv) => sprout(cv, 7, R.moss, 2, 0, 2.5),
);

export default {
  valerian: {
    stages: valerian,
    seeds: seeds(1, R.root, 'oval'),
    fresh: sprite((cv) => {
      line(cv, 6, 15, 8, 5, R.moss[0]);
      leaf(cv, 7, 13, 160, 3.5, 0.8, R.moss, 0.3);
      leaf(cv, 7, 11, 25, 3, 0.8, R.moss, 0.3);
      head(cv, 8, 4);
      head(cv, 4, 7);
      head(cv, 12, 7);
      line(cv, 7, 8, 5, 8, R.moss[0]);
      line(cv, 8, 8, 11, 8, R.moss[0]);
    }),
  },
  mugwort: {
    stages: mugwort,
    seeds: seeds(2, R.fern, 'long'),
    fresh: sprite((cv) => {
      line(cv, 3, 15, 9, 6, R.moss[0]);
      fork(cv, 8, 8, 60, 5, 1);
      fork(cv, 5, 12, 25, 4, 0.9);
      fork(cv, 5, 11, 140, 3.5, 0.8);
      for (const [x, y] of [[9, 4], [11, 5], [8, 6], [7, 10], [4, 9]]) set(cv, x, y, R.silver[2]);
    }),
  },
};
