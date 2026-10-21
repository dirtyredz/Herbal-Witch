// Stone for the mortar and pestle (owner reference: a chunky mottled grey-taupe bowl with moss and
// small pink flowers creeping over it): soft pixel mottling, never stripes. Plus the ground-herb
// pile in the bowl. 32 x 32, opaque.
import { rng, shade, mix, image } from '../pixels.mjs';
import { grid, flower, overlay, smoothNoise, put, PINK } from '../motifs.mjs';

const SIZE = 32;
const MOSS = [[62, 92, 34], [92, 126, 44], [138, 160, 62]];

function mottled(seed, dark, light) {
  const n = smoothNoise(rng(seed), SIZE, 3);
  const m = smoothNoise(rng(seed + 1), SIZE, 7);
  const rand = rng(seed + 2);
  return image(SIZE, (x, y) => {
    const t = n(x, y) * 0.6 + m(x, y) * 0.4;
    return shade(mix(dark, light, t), 0.94 + rand() * 0.1);
  });
}

/** Moss patches: blobs whose likelihood falls off with distance from rows `from` (texels). */
function moss(g, seed, count, rows) {
  const rand = rng(seed);
  for (let i = 0; i < count; i++) {
    const cx = rand() * SIZE;
    const cy = rows[0] + rand() * (rows[1] - rows[0]);
    const r = 1.5 + rand() * 2.8;
    for (let y = Math.floor(cy - r); y <= cy + r; y++) {
      for (let x = Math.floor(cx - r); x <= cx + r; x++) {
        const d = Math.hypot(x - cx, (y - cy) * 1.3);
        if (d > r || rand() < d / r * 0.5) continue;
        const shadeIdx = d < r * 0.4 ? 2 : d < r * 0.75 ? 1 : 0;
        put(g, ((x % SIZE) + SIZE) % SIZE, y, MOSS[shadeIdx]);
      }
    }
  }
  return rand;
}

/** The bowl: grey-taupe mottled stone, moss creeping from the rim and the foot, a few pink flowers. */
export function mortarStone() {
  // The bowl's sides span model y 3.5..11, i.e. texture rows 10..25: moss spills from the rim
  // (rows 9..15) and gathers low on the bowl (rows 20..26), with a few flowers on it.
  const g = grid();
  const rand = moss(g, 9201, 18, [8, 15]);
  moss(g, 9202, 10, [19, 26]);
  for (let i = 0; i < 7; i++) flower(g, 2 + Math.floor(rand() * 28), 10 + Math.floor(rand() * 15), PINK);
  return overlay(mottled(9203, [104, 96, 88], [170, 160, 146]), g);
}

/** The plinth and pestle: the same stone, plainer, a little moss at the base only. */
export function plinthStone() {
  const g = grid();
  moss(g, 9204, 5, [26, 31]);
  return overlay(mottled(9205, [96, 90, 84], [150, 142, 132]), g);
}

/** The pestle: a cooler, smoother grey-taupe. */
export const pestleStone = () => mottled(9206, [104, 98, 96], [158, 150, 146]);

/** Freshly ground herbs heaped in the bowl: bright greens with lighter leaf flecks. */
export function herbPile() {
  const rand = rng(9207);
  const n = smoothNoise(rng(9208), SIZE, 3);
  return image(SIZE, (x, y) => {
    const t = n(x, y);
    let c = t > 0.66 ? [150, 204, 72] : t > 0.38 ? [98, 156, 46] : [56, 104, 32];
    if (rand() < 0.05) c = [190, 222, 96];
    return shade(c, 0.95 + rand() * 0.08);
  });
}
