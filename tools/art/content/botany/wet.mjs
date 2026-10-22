// Wet-ground herbs: mint, nettle.
import { R, leaf } from '../../drawing/plant-motifs.mjs';
import { set, line, sprite } from '../../drawing/sprites.mjs';
import { seeds } from './seeds.mjs';

const pair = (cv, x, y, len, w, r, jag, lift = 20) => {
  leaf(cv, x, y, 180 - lift, len, w, r, jag);
  leaf(cv, x, y, lift, len, w, r, jag);
};

export default {
  mint: {
    seeds: seeds(0, R.mint, 'speck'),
    fresh: sprite((cv) => {
      line(cv, 3, 15, 8, 2, R.mint[0]);
      pair(cv, 4, 12, 3.5, 1, R.mint, 0.4, 35);
      pair(cv, 6, 7, 3.5, 1, R.mint, 0.4, 35);
      leaf(cv, 8, 2, 70, 3, 0.8, R.mint);
      line(cv, 3, 14, 4, 14, R.twine[1]);
    }),
  },
  nettle: {
    seeds: seeds(3, R.dark, 'round'),
    fresh: sprite((cv) => {
      line(cv, 7, 15, 7, 6, R.dark[0]);
      leaf(cv, 7, 6, 90, 5, 1.8, R.dark, 0.6);
      leaf(cv, 7, 12, 150, 5, 1.4, R.dark, 0.6);
      leaf(cv, 7, 10, 25, 5, 1.4, R.dark, 0.6);
      set(cv, 7, 3, R.moss[2]);
    }),
  },
};
