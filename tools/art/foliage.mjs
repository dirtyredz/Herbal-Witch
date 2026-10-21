// Cutout foliage shared by furniture and stations: dense trailing ivy with small pink flowers
// (the owner's vine standard), and hanging dried-herb bundles tied with twine. 32 x 32, alpha 0/255.
import { rng } from './pixels.mjs';
import { grid, put, flower, PINK, MAGENTA } from './motifs.mjs';

const SIZE = 32;
const IVY = [[34, 66, 30], [56, 98, 40], [86, 132, 54], [120, 160, 72]];
const STEM = [70, 58, 30];

function toImage(g) {
  return { size: SIZE, px: g.px.map((p) => (p ? [p[0], p[1], p[2], 255] : [0, 0, 0, 0])) };
}

/** A small ivy leaf (5 x 4 texels, three lobes) at (x, y) facing left or right. */
function ivyLeaf(g, x, y, dir, rand) {
  const tone = rand() < 0.35 ? 1 : 0;
  const shape = ['.##..', '#####', '.###.', '..#..'];
  shape.forEach((row, ry) => {
    for (let rx = 0; rx < 5; rx++) {
      if (row[rx] !== '#') continue;
      const lx = dir > 0 ? rx : 4 - rx;
      const c = ry === 0 ? IVY[2 + tone] : ry === 1 && rx === 2 ? IVY[0] : IVY[1 + tone];
      put(g, x + lx - 2, y + ry - 1, c);
    }
  });
}

/**
 * Dense trailing ivy: several stems wander down from the top edge at different lengths, clumped
 * with leaves both sides and dotted with five-petal pink flowers. Tiles horizontally.
 */
export function ivy() {
  const rand = rng(9301);
  const g = grid();
  const stems = [[2, 30], [8, 20], [13, 31], [19, 24], [24, 29], [29, 17]];
  for (const [x0, len] of stems) {
    let side = rand() < 0.5 ? -1 : 1;
    const phase = rand() * 6;
    for (let y = 0; y < len; y++) {
      const sx = x0 + Math.sin(y * 0.35 + phase) * 1.8;
      put(g, ((Math.round(sx) % SIZE) + SIZE) % SIZE, y, STEM);
      if (y % 3 === 1) {
        ivyLeaf(g, (((Math.round(sx + side * 2.5)) % SIZE) + SIZE) % SIZE, y, side, rand);
        side = -side;
      }
    }
    // a fuller clump at the top of each stem
    ivyLeaf(g, (x0 + 2) % SIZE, 1, 1, rand);
    ivyLeaf(g, (x0 + SIZE - 2) % SIZE, 2, -1, rand);
  }
  for (let i = 0; i < 9; i++) {
    const x = 1 + Math.floor(rand() * 30);
    const y = 2 + Math.floor(rand() * 24);
    flower(g, x, y, i % 3 === 0 ? MAGENTA : PINK, 5);
  }
  return toImage(g);
}

/** Bundle colourings: [leaf ramp, flower colour or null]. */
const BUNDLES = {
  green: [[[50, 92, 34], [82, 132, 46], [126, 170, 64]], null],
  lavender: [[[60, 86, 46], [92, 122, 62], [128, 156, 84]], [[96, 56, 150], [140, 92, 200], [184, 146, 232]]],
  white: [[[58, 96, 42], [92, 136, 58], [132, 170, 80]], [[200, 196, 176], [238, 234, 218], [255, 252, 240]]],
  yellow: [[[66, 98, 34], [104, 138, 48], [146, 174, 66]], [[200, 150, 30], [236, 192, 52], [252, 226, 110]]],
};

/**
 * A hanging dried-herb bundle, stems up: twine-tied neck near the top, a hook loop above it, and a
 * spray widening downwards with flower clusters toward the tips. Stretched over crossed planes.
 */
export function bundle(kind) {
  const [leaf, bloom] = BUNDLES[kind];
  const rand = rng(9310 + Object.keys(BUNDLES).indexOf(kind));
  const g = grid();
  for (let y = 0; y < 6; y++) put(g, 16, y, [70, 46, 30]); // hanging cord
  for (let y = 6; y < 11; y++) for (let x = 14; x < 19; x++) put(g, x, y, y % 2 ? [176, 140, 88] : [150, 116, 70]); // twine
  // a full spray: many stems fanning from the tie, each thick with leaves (two texels either side)
  for (let s = 0; s < 15; s++) {
    const spread = (s - 7) * 1.7;
    const len = 16 + Math.floor(rand() * 5);
    for (let y = 11; y < 11 + len && y < 32; y++) {
      const t = (y - 11) / 20;
      const x = 16 + spread * t + Math.sin(y * 0.6 + s) * 0.7;
      put(g, x, y, leaf[0]);
      for (const d of [-2, -1, 1, 2]) {
        if (rand() < (Math.abs(d) === 1 ? 0.8 : 0.4)) put(g, x + d, y, leaf[1 + (rand() < 0.45 ? 1 : 0)]);
      }
      if (bloom && t > 0.3 && rand() < 0.35) put(g, x + (rand() < 0.5 ? -1 : 1), y, bloom[rand() < 0.5 ? 1 : 2]);
    }
  }
  if (bloom) {
    for (let i = 0; i < 16; i++) {
      const y = 18 + Math.floor(rand() * 12);
      const x = 16 + (rand() - 0.5) * (y - 11) * 1.4;
      put(g, x, y, bloom[1]);
      put(g, x + 1, y, bloom[2]);
      put(g, x, y + 1, bloom[0]);
    }
  }
  return toImage(g);
}
