// Temperate-meadow herbs: lavender, sage, chamomile, yarrow.
import { R, set, line, leaf, blob, bloom, sprite, seeds, grow, sprout } from './sprites.mjs';

const spike = (cv, x, top, len) => {
  for (let i = 0; i < len; i++) {
    const y = top + i;
    const out = x < 8 ? -1 : 1; // the buds lean away from the centre so the spikes stay separate
    set(cv, x, y, i % 2 ? R.violet[1] : R.violet[2]);
    if (i > 0 && i % 2 === 1) set(cv, x + out, y, R.violet[0]);
  }
};

const lavender = grow(
  (cv, s) => {
    if (s === 1) {
      line(cv, 7, 15, 7, 9, R.grey[1]);
      leaf(cv, 7, 13, 150, 3, 0.7, R.grey);
      leaf(cv, 7, 13, 30, 3, 0.7, R.grey);
      return;
    }
    const tips = s === 2 ? [[3, 8], [6, 6], [9, 6], [12, 8]] : [[3, 7], [6, 5], [9, 5], [12, 7]];
    tips.forEach(([x, y], i) => line(cv, i < 2 ? 7 : 8, 15, x, y, R.grey[1]));
    leaf(cv, 7, 14, 160, 4, 0.8, R.grey);
    leaf(cv, 8, 14, 20, 4, 0.8, R.grey);
    leaf(cv, 7, 12, 135, 3, 0.7, R.grey);
    leaf(cv, 8, 12, 45, 3, 0.7, R.grey);
    if (s === 3) {
      spike(cv, 3, 1, 6);
      spike(cv, 6, 0, 6);
      spike(cv, 9, 0, 6);
      spike(cv, 12, 2, 5);
    }
  },
  (cv) => sprout(cv, 7, R.grey, 2, 0, 3),
);

const sage = grow(
  (cv, s) => {
    line(cv, 7, 15, 7, s === 1 ? 10 : s === 2 ? 6 : 3, R.sage[0]);
    line(cv, 8, 15, 8, s === 1 ? 11 : 8, R.sage[0]);
    const leaves = [[5, 12, 1.6, 1.1], [10, 11, 1.6, 1.1]];
    if (s >= 2) leaves.push([4, 9, 2, 1.3], [11, 8, 2, 1.3], [7, 7, 1.8, 1.4], [6, 13, 1.5, 1]);
    if (s === 3) leaves.push([3, 6, 2, 1.3], [12, 5, 2, 1.3], [7, 3, 1.8, 1.4], [9, 10, 2, 1.3], [5, 4, 1.6, 1.2], [10, 3, 1.6, 1.2]);
    for (const [x, y, rx, ry] of leaves) blob(cv, x, y, rx, ry, R.sage);
    if (s === 3) for (const [x, y] of [[4, 9], [11, 8], [7, 7]]) set(cv, x, y, R.sage[2]);
  },
  (cv) => sprout(cv, 7, R.sage, 2, 0, 2.5),
);

const chamomile = grow(
  (cv, s) => {
    if (s === 1) {
      line(cv, 7, 15, 7, 9, R.moss[1]);
      leaf(cv, 7, 13, 160, 3, 0.6, R.fern);
      leaf(cv, 7, 12, 20, 3, 0.6, R.fern);
      return;
    }
    const stems = s === 2 ? [[4, 9], [7, 6], [11, 8]] : [[3, 7], [7, 4], [11, 6], [8, 9]];
    stems.forEach(([x, y]) => line(cv, 7, 15, x, y + 1, R.moss[1]));
    leaf(cv, 7, 14, 165, 3, 0.6, R.fern);
    leaf(cv, 8, 14, 15, 3, 0.6, R.fern);
    leaf(cv, 7, 12, 140, 2.5, 0.6, R.fern);
    leaf(cv, 8, 11, 40, 2.5, 0.6, R.fern);
    if (s === 3) stems.forEach(([x, y]) => bloom(cv, x, y, R.cream[2], R.yellow[1], true));
  },
  (cv) => sprout(cv, 7, R.fern, 2, 0, 3),
);

const yarrow = grow(
  (cv, s) => {
    const top = s === 1 ? 10 : s === 2 ? 7 : 5;
    line(cv, 7, 15, 7, top, R.fern[0]);
    line(cv, 8, 15, 8, top + 3, R.fern[0]);
    const tufts = [[5, 13, 150], [10, 13, 30]];
    if (s >= 2) tufts.push([5, 10, 140], [10, 10, 40], [6, 7, 125]);
    for (const [x, y, d] of tufts) {
      leaf(cv, 7, y, d, 3.5, 0.7, R.fern, 0.4);
      set(cv, x, y - 1, R.fern[2]);
    }
    if (s === 3) {
      for (const [x, y] of [[4, 4], [6, 3], [8, 3], [10, 4], [5, 5], [7, 4], [9, 5], [11, 5]]) set(cv, x, y, R.cream[1]);
      for (const [x, y] of [[4, 3], [6, 2], [8, 2], [10, 3], [7, 3], [5, 4], [9, 4]]) set(cv, x, y, R.cream[2]);
      set(cv, 7, 5, R.cream[0]);
      set(cv, 5, 6, R.cream[0]);
      set(cv, 10, 6, R.cream[0]);
    }
  },
  (cv) => sprout(cv, 8, R.fern, 1, 0, 2.5),
);

export default {
  lavender: {
    stages: lavender,
    seeds: seeds(0, R.black, 'oval'),
    fresh: sprite((cv) => {
      line(cv, 4, 15, 10, 4, R.grey[1]);
      line(cv, 5, 15, 10, 5, R.grey[0]);
      leaf(cv, 6, 12, 160, 3, 0.7, R.grey);
      for (let i = 0; i < 5; i++) {
        set(cv, 10 - Math.floor(i / 2) * 0.5, 1 + i, i % 2 ? R.violet[1] : R.violet[2]);
        if (i % 2) set(cv, 12 - Math.floor(i / 2), 1 + i, R.violet[0]);
      }
      line(cv, 5, 12, 8, 12, R.twine[1]);
    }),
  },
  sage: {
    stages: sage,
    seeds: seeds(1, R.wood, 'round'),
    fresh: sprite((cv) => {
      line(cv, 7, 15, 7, 8, R.sage[0]);
      blob(cv, 7, 4, 2, 3, R.sage);
      blob(cv, 3, 8, 3, 1.6, R.sage);
      blob(cv, 11, 8, 3, 1.6, R.sage);
      blob(cv, 5, 12, 2.5, 1.4, R.sage);
      line(cv, 7, 3, 7, 6, R.sage[2]);
    }),
  },
  chamomile: {
    stages: chamomile,
    seeds: seeds(2, R.yellow, 'speck'),
    fresh: sprite((cv) => {
      line(cv, 4, 15, 4, 8, R.moss[1]);
      line(cv, 9, 15, 11, 6, R.moss[1]);
      leaf(cv, 5, 13, 30, 3, 0.7, R.fern);
      bloom(cv, 4, 7, R.cream[2], R.yellow[1], true);
      bloom(cv, 11, 5, R.cream[2], R.yellow[1], true);
      bloom(cv, 8, 10, R.cream[2], R.yellow[1], true);
      line(cv, 8, 12, 8, 14, R.moss[1]);
    }),
  },
  yarrow: {
    stages: yarrow,
    seeds: seeds(3, R.silver, 'long'),
    fresh: sprite((cv) => {
      line(cv, 6, 15, 8, 6, R.fern[0]);
      leaf(cv, 7, 12, 150, 3.5, 0.7, R.fern, 0.4);
      leaf(cv, 7, 10, 30, 3.5, 0.7, R.fern, 0.4);
      for (let x = 3; x <= 12; x++) set(cv, x, 4 + (x % 2), R.cream[1]);
      for (let x = 4; x <= 11; x++) set(cv, x, 3, R.cream[2]);
      for (let x = 5; x <= 10; x++) set(cv, x, 5 + (x % 2), R.cream[0]);
    }),
  },
};
