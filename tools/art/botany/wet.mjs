// Wet-ground herbs: mint, nettle.
import { R, set, line, leaf, sprite, seeds, grow, sprout } from './sprites.mjs';

const pair = (cv, x, y, len, w, r, jag, lift = 20) => {
  leaf(cv, x, y, 180 - lift, len, w, r, jag);
  leaf(cv, x, y, lift, len, w, r, jag);
};

const mint = grow(
  (cv, s) => {
    const top = s === 1 ? 9 : 5;
    line(cv, 7, 15, 7, top, R.mint[0]);
    line(cv, 8, 15, 8, top + 5, R.mint[0]);
    pair(cv, 7, 12, 3.5, 1, R.mint, 0.4);
    if (s >= 2) {
      pair(cv, 7, 9, 3.5, 1, R.mint, 0.4);
      pair(cv, 7, 6, 3, 1, R.mint, 0.4);
    }
  },
  (cv) => sprout(cv, 7, R.mint, 2, 0, 2.5),
);

const nettle = grow(
  (cv, s) => {
    const top = s === 1 ? 8 : 4;
    line(cv, 7, 15, 7, top, R.dark[0]);
    line(cv, 8, 15, 8, top + 2, R.dark[0]);
    pair(cv, 7, 13, 4, 1.2, R.dark, 0.5, 30);
    if (s >= 2) {
      pair(cv, 7, 10, 4, 1.2, R.dark, 0.5, 35);
      pair(cv, 7, 7, 3.5, 1.1, R.dark, 0.5, 40);
    }
  },
  (cv) => sprout(cv, 7, R.dark, 2, 0, 2.5),
);

export default {
  mint: {
    stages: mint,
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
    stages: nettle,
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
