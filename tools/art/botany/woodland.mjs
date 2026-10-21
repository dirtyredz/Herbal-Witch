// Woodland (toxic) herbs: belladonna, nightshade, wolfsbane, mandrake. Darker and moodier.
import { R, set, line, leaf, blob, bell, berry, sprite, seeds } from './sprites.mjs';

export default {
  belladonna: {
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
