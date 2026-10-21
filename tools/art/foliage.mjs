// Cutout foliage shared by furniture and stations: dense trailing ivy with small pink flowers
// (the owner's vine standard). 32 x 32, alpha 0/255. (Dried bundles are 3D cube models since
// 0.2.0-dev.2: tools/models/preparations/bundles.mjs.)
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
      if (y % 2 === 1) {
        ivyLeaf(g, (((Math.round(sx + side * 2.5)) % SIZE) + SIZE) % SIZE, y, side, rand);
        side = -side;
      }
    }
    // a fuller clump at the top of each stem
    ivyLeaf(g, (x0 + 2) % SIZE, 1, 1, rand);
    ivyLeaf(g, (x0 + SIZE - 2) % SIZE, 2, -1, rand);
  }
  for (let i = 0; i < 5; i++) {
    const x = 2 + Math.floor(rand() * 28);
    const y = 3 + Math.floor(rand() * 22);
    flower(g, x, y, i % 3 === 0 ? MAGENTA : PINK, 5);
  }
  return toImage(g);
}
