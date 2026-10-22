// Temperate-meadow herbs: lavender, sage, chamomile, yarrow.
import { R, leaf, blob, bloom } from '../../drawing/plant-motifs.mjs';
import { set, line, sprite } from '../../drawing/sprites.mjs';
import { seeds } from './seeds.mjs';

export default {
  lavender: {
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
