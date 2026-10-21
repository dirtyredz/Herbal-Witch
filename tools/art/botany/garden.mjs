// Garden soil tiles: 32 x 32 opaque tilled earth, dry and moist, with the same furrow layout.
import { image, rng, clamp } from '../pixels.mjs';
import { hex } from './sprites.mjs';

const SIZE = 32;

function soil(seed, base, ridge, furrow, speck) {
  const rand = rng(seed);
  return image(SIZE, (x, y) => {
    // Furrows run along x every 8 rows, wavering slightly; ridges sit between them.
    const wave = Math.round(Math.sin(x * 0.35 + (y >> 3)) * 0.8);
    const row = (((y + wave) % 8) + 8) % 8;
    let c = row === 0 || row === 1 ? furrow : row === 4 ? ridge : base;
    const n = (rand() - 0.5) * 14;
    let [r, g, b] = c;
    if (rand() < 0.05) [r, g, b] = speck;
    return [clamp(r + n), clamp(g + n * 0.9), clamp(b + n * 0.8), 255];
  });
}

export const soilDry = () => soil(7101, hex('#b49872'), hex('#c7ad88'), hex('#8e7452'), hex('#d6c3a0'));
export const soilMoist = () => soil(7101, hex('#6b4f36'), hex('#7a5c41'), hex('#4b3624'), hex('#5a4129'));
