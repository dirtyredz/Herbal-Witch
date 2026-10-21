// Garden soil tiles: 32 x 32 opaque, dark rich lumpy earth (owner reference: the herb bed's soil
// sits just below the rim, dark and textured with lumps). Moist soil is the same layout, darker.
// Seamless, so joined beds read as one continuous bed of soil.
import { image, rng, clamp } from '../pixels.mjs';
import { smoothNoise } from '../motifs.mjs';
import { hex } from './sprites.mjs';

const SIZE = 32;

function soil(seed, base, lump, shadowC, speck) {
  const rand = rng(seed);
  const n = smoothNoise(rng(seed + 1), SIZE, 4);
  const crumbs = smoothNoise(rng(seed + 2), SIZE, 2);
  return image(SIZE, (x, y) => {
    const t = n(x, y);
    const c = crumbs(x, y);
    // lumps: bright tops with a shadow just below them
    let col = base;
    if (c > 0.68) col = lump;
    else if (crumbs(x, (y + SIZE - 1) % SIZE) > 0.68) col = shadowC;
    let [r, g, b] = col;
    if (rand() < 0.03) [r, g, b] = speck;
    const f = 0.88 + t * 0.24 + (rand() - 0.5) * 0.06;
    return [clamp(r * f), clamp(g * f), clamp(b * f), 255];
  });
}

export const soilDry = () => soil(7101, hex('#4e3220'), hex('#6c4a2e'), hex('#321e12'), hex('#80603f'));
export const soilMoist = () => soil(7101, hex('#3e281a'), hex('#553925'), hex('#26170e'), hex('#62452e'));
