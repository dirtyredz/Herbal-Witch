// Woodland (toxic) herbs: belladonna, nightshade, wolfsbane, mandrake. Darker and moodier.
import { R, set, line, leaf, blob, bell, berry, sprite, seeds, grow, sprout } from './sprites.mjs';

const belladonna = grow(
  (cv, s) => {
    const top = s === 1 ? 9 : 4;
    line(cv, 7, 15, 7, top, R.wood[0]);
    blob(cv, 5, 12, 2, 1.3, R.deep);
    blob(cv, 10, 11, 2, 1.3, R.deep);
    if (s >= 2) {
      line(cv, 7, 11, 3, 8, R.wood[0]);
      line(cv, 8, 10, 12, 7, R.wood[0]);
      blob(cv, 3, 7, 2.2, 1.5, R.deep);
      blob(cv, 12, 6, 2.2, 1.5, R.deep);
      blob(cv, 7, 5, 2.2, 1.6, R.deep);
      blob(cv, 5, 9, 2, 1.3, R.deep);
      blob(cv, 10, 9, 2, 1.3, R.deep);
    }
  },
  (cv) => sprout(cv, 7, R.deep, 2, 0, 2.5),
);

const nightshade = grow(
  (cv, s) => {
    if (s === 1) {
      line(cv, 7, 15, 7, 10, R.deep[1]);
      leaf(cv, 7, 12, 150, 3, 0.9, R.deep);
      leaf(cv, 7, 11, 30, 3, 0.9, R.deep);
      return;
    }
    line(cv, 7, 15, 7, 6, R.deep[1]);
    line(cv, 7, 11, 3, 6, R.deep[1]);
    line(cv, 8, 10, 12, 5, R.deep[1]);
    leaf(cv, 7, 13, 160, 3.5, 1, R.deep);
    leaf(cv, 8, 13, 20, 3.5, 1, R.deep);
    leaf(cv, 7, 9, 130, 3, 1, R.deep);
    leaf(cv, 8, 8, 50, 3, 1, R.deep);
  },
  (cv) => sprout(cv, 7, R.deep, 2, 1, 2.5),
);

const wolfsbane = grow(
  (cv, s) => {
    if (s === 1) {
      line(cv, 7, 15, 7, 10, R.deep[1]);
      leaf(cv, 7, 12, 160, 3, 0.8, R.deep, 0.3);
      leaf(cv, 7, 12, 20, 3, 0.8, R.deep, 0.3);
      leaf(cv, 7, 11, 90, 2.5, 0.8, R.deep);
      return;
    }
    line(cv, 7, 15, 7, 4, R.deep[1]);
    line(cv, 8, 15, 8, 10, R.deep[1]);
    for (const d of [170, 140, 40, 10]) leaf(cv, 7, 14, d, 4, 0.9, R.deep, 0.3);
    leaf(cv, 7, 10, 125, 3, 0.8, R.deep, 0.3);
    leaf(cv, 8, 10, 55, 3, 0.8, R.deep, 0.3);
  },
  (cv) => sprout(cv, 7, R.deep, 3, 0, 2),
);

const mandrake = grow(
  (cv, s) => {
    const fan = s === 1 ? [[150, 4], [30, 4]] : [[165, 5], [135, 5], [90, 4.5], [45, 5], [15, 5]];
    for (const [d, l] of fan) leaf(cv, d >= 90 ? 7 : 8, 15, d, l, 1.5, R.deep, 0.3);
  },
  (cv) => sprout(cv, 7, R.deep, 1, 0, 3),
);

export default {
  belladonna: {
    stages: belladonna,
    seeds: seeds(3, R.black, 'oval'),
    fresh: sprite((cv) => {
      line(cv, 4, 15, 8, 3, R.wood[0]);
      leaf(cv, 5, 12, 20, 4, 1.2, R.deep);
      leaf(cv, 6, 8, 150, 3.5, 1.2, R.deep);
      bell(cv, 9, 3, R.plum);
      berry(cv, 10, 8, R.black, R.black[2]);
      berry(cv, 7, 5, R.black, R.black[2]);
    }),
  },
  nightshade: {
    stages: nightshade,
    seeds: seeds(0, R.red, 'round'),
    fresh: sprite((cv) => {
      line(cv, 3, 14, 12, 4, R.deep[1]);
      leaf(cv, 5, 12, 100, 3.5, 1, R.deep);
      for (const [dx, dy] of [[0, -1], [-1, 0], [1, 0], [-1, 1], [1, 1]]) set(cv, 12 + dx, 3 + dy, R.violet[1]);
      set(cv, 12, 3, R.yellow[1]);
      berry(cv, 7, 7, R.red, R.red[2]);
      berry(cv, 9, 9, R.red, R.red[2]);
      berry(cv, 5, 9, R.red, R.red[2]);
    }),
  },
  wolfsbane: {
    stages: wolfsbane,
    seeds: seeds(1, R.blue, 'speck'),
    fresh: sprite((cv) => {
      line(cv, 7, 15, 7, 7, R.deep[1]);
      leaf(cv, 7, 13, 150, 4, 1, R.deep, 0.3);
      leaf(cv, 7, 12, 30, 4, 1, R.deep, 0.3);
      for (let y = 0; y < 7; y++) {
        const w = y < 2 ? 1 : 2;
        for (let dx = -w; dx <= w; dx++) set(cv, 7 + dx, y, dx === w ? R.blue[0] : y % 2 ? R.blue[1] : R.blue[2]);
      }
    }),
  },
  mandrake: {
    stages: mandrake,
    seeds: seeds(2, R.wood, 'round'),
    fresh: sprite((cv) => {
      line(cv, 7, 5, 7, 9, R.root[1]);
      line(cv, 6, 9, 4, 14, R.root[1]);
      line(cv, 8, 9, 11, 14, R.root[1]);
      line(cv, 7, 9, 7, 12, R.root[0]);
      blob(cv, 7, 7, 2, 2, R.root);
      set(cv, 5, 12, R.root[0]);
      set(cv, 10, 12, R.root[0]);
      leaf(cv, 7, 5, 140, 4, 1.2, R.deep);
      leaf(cv, 7, 5, 40, 4, 1.2, R.deep);
      leaf(cv, 7, 5, 90, 4, 1, R.deep);
    }),
  },
};
