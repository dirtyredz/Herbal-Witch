// The shared house materials the procedural models use (the herb bed and hung bundles): the dark
// brown wood, its end grain, and polished gold. 32 x 32, untinted, opaque. Blockbench pieces own
// their textures instead (docs/ART.md "Textures").
import { rng, clamp, shade, image } from '../pixels.mjs';

const SIZE = 32;
const GOLD = [[154, 104, 30], [212, 160, 58], [242, 201, 90]]; // shadow, mid, light

/**
 * The house's dark brown wood (the armchair's, the owner's standard for the whole set): rich grain
 * with dark latewood lines and pores, along U (beams, rails, tops) or along V (posts, legs). The
 * horizontal grain is the armchair's original texture, pixel for pixel.
 */
function grain(seed, vertical) {
  const rand = rng(seed);
  const lineOffset = Array.from({ length: SIZE }, () => rand() * 0.7);
  const dark = [59, 36, 22];
  const light = [90, 58, 34];
  return image(SIZE, (px, py) => {
    const [x, y] = vertical ? [py, px] : [px, py];
    const wave = Math.sin(x * 0.19 + y * 0.06) * 1.7 + Math.sin(x * 0.06 + 0.7) * 2.4;
    const ring = (((y + wave + lineOffset[y]) % 6) + 6) % 6;
    let t = 0.35 + Math.sin((y + wave) * 1.1) * 0.18;
    if (ring < 0.9) t = 0; // latewood line stays dark
    else if (ring < 1.8) t = 0.2;
    t += (rand() - 0.5) * 0.18;
    t = Math.max(0, Math.min(1, t));
    let c = [0, 1, 2].map((i) => dark[i] + (light[i] - dark[i]) * t);
    if (rand() < 0.03) c = c.map((v) => v * 0.72); // pore
    return [clamp(c[0]), clamp(c[1]), clamp(c[2]), 255];
  });
}

export const wood = () => grain(6607, false);
export const woodPost = () => grain(6608, true);

/** End grain for caps and post tops: dark rings around a centre, a shade under the plank faces. */
export function woodEnd() {
  const rand = rng(9103);
  return image(SIZE, (x, y) => {
    const d = Math.hypot((x % 16) - 7.5, (y % 16) - 7.5);
    const ring = d % 3 < 1 ? 0.72 : 1;
    return shade([66, 40, 25], ring * (0.92 + rand() * 0.14));
  });
}

/** Polished gold: warm mid tone, a diagonal sheen and darker edges every 2 model pixels. */
export function gold() {
  const rand = rng(9106);
  return image(SIZE, (x, y) => {
    const sheen = Math.cos(((x - y) / SIZE) * Math.PI * 2) * 0.12;
    const edge = (x + y) % 7 === 0 ? 1.1 : 1;
    return shade(GOLD[1], (1 + sheen) * edge * (0.96 + rand() * 0.08));
  });
}
