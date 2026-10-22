// Dry-ground herbs: rosemary, thyme.
import { R } from '../../drawing/plant-motifs.mjs';
import { set, line, sprite } from '../../drawing/sprites.mjs';
import { seeds } from './seeds.mjs';

export default {
  rosemary: {
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
