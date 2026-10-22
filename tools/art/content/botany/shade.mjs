// Shade herbs: valerian, mugwort.
import { R, leaf, blob } from '../../drawing/plant-motifs.mjs';
import { set, line, sprite } from '../../drawing/sprites.mjs';
import { seeds } from './seeds.mjs';

const head = (cv, x, y) => {
  blob(cv, x, y, 2.2, 1.7, R.pink);
  for (const [dx, dy] of [[-1, -1], [1, 0], [0, 1], [-2, 0]]) set(cv, x + dx, y + dy, R.pink[2]);
  set(cv, x + 1, y + 1, R.rose[1]);
  set(cv, x - 1, y + 1, R.rose[0]);
};

const fork = (cv, x, y, deg, len, w) => {
  leaf(cv, x, y, deg, len, w, R.moss, 0.3);
  leaf(cv, x, y, deg + 28, len * 0.8, w * 0.8, R.moss, 0.3);
  leaf(cv, x, y, deg - 28, len * 0.8, w * 0.8, R.moss, 0.3);
};

export default {
  valerian: {
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
