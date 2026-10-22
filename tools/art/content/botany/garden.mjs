// Garden soil tiles: 32 x 32 opaque lumpy earth (owner reference: the herb bed's soil sits just
// below the rim, textured with lumps). Dry soil is a dusty grey-brown, lighter than the dark wood
// rails; watered soil is the same layout, near black and wet with cool glints, darker than the
// rails. The two must stay far apart (BotanyModelsTest checks the luminance gap): the 0.2.0 art
// pass made both dark and the watered state vanished. Seamless, so joined beds read as one.
import { image, rng, clamp } from '../../pixels.mjs';
import { smoothNoise } from '../../motifs.mjs';
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

export const soilDry = () => soil(7101, hex('#6e5a46'), hex('#8a755c'), hex('#4a3a2c'), hex('#a08a6c'));
export const soilMoist = () => soil(7101, hex('#26180f'), hex('#3a2617'), hex('#120a06'), hex('#6c6460'));
