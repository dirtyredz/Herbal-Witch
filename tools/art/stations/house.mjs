// The house style for stations (docs/ART.md "The house style"): dark brown wood,
// charcoal iron, gold and copper hardware, dark plum vessels painted with gold moons, stars and
// pink flowers on olive vines, charcoal stone, firelight. 32 x 32, untinted, opaque unless noted.
import { rng, clamp, shade, mix, image } from '../pixels.mjs';
import { grid, moon, star, flower, sprig, overlay, smoothNoise, put, GOLD, PINK, MAGENTA } from '../motifs.mjs';

const SIZE = 32;

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

/** Charcoal iron with a faint plum cast, soft hammered mottling and a few bright specks. */
export function ironDark() {
  const rand = rng(9104);
  const n = smoothNoise(rng(9105), SIZE, 6);
  return image(SIZE, (x, y) => {
    let f = 0.82 + n(x, y) * 0.3 + (rand() - 0.5) * 0.08;
    if (rand() < 0.015) f *= 1.5;
    return shade([46, 41, 52], f);
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

/** Copper for handles and bands: red-orange with darker stripes along U (a rounded bar). */
export function copper() {
  const rand = rng(9107);
  return image(SIZE, (x, y) => {
    const band = Math.sin((y % 8) / 8 * Math.PI);
    return shade(mix([110, 46, 22], [200, 104, 52], band), 0.94 + rand() * 0.1);
  });
}

/** Dark plum glaze for kettle and pot lids and plain surfaces. */
export function plum() {
  const n = smoothNoise(rng(9108), SIZE, 5);
  const rand = rng(9109);
  return image(SIZE, (x, y) => shade(mix([46, 28, 54], [74, 46, 84], n(x, y)), 0.95 + rand() * 0.08));
}

/**
 * The painted vessel body (kettle and pot side faces, sampled by position: v = 16 - model y): a
 * gold moon with stars across the belly and pink/magenta flowers on olive sprigs below it, on plum.
 * Repeats every 16 texels so each face shows one full motif.
 */
export function plumPainted() {
  // The modest stove-top vessels' bellies span model y 1.5..6.25 (texture rows 19.5..29): the
  // moon band sits on rows 20.5..26.5, the flowers on rows 26..29 and the vine sprigs below.
  const base = plum();
  const g = grid();
  for (const ox of [0, 16]) {
    moon(g, ox + 8, 23.5, 2.8);
    star(g, ox + 3, 22, 1);
    star(g, ox + 13, 24, 1);
    sprig(g, ox + 2, 31, 5, 0.2);
    sprig(g, ox + 12, 31, 5, -0.15);
    flower(g, ox + 3, 27, PINK);
    flower(g, ox + 8, 28, MAGENTA);
    flower(g, ox + 12.5, 27, PINK);
    flower(g, ox + 15, 28, MAGENTA);
  }
  return overlay(base, g);
}

/** A vessel lid seen from above (u = x, v = z): a central moon with stars and flowers around it. */
export function plumLid() {
  const base = plum();
  const g = grid();
  // the knob covers the centre: the moon sits beside it, flowers and sprigs around the rim
  moon(g, 16, 9.5, 4.5);
  for (const [x, y] of [[8, 12], [24, 12], [9, 23], [23, 24], [16, 26]]) star(g, x, y, 1);
  for (const [x, y, r] of [[7, 17], [25, 18], [12, 25], [21, 21]].map(([x, y], i) => [x, y, i % 2 ? MAGENTA : PINK])) flower(g, x, y, r);
  sprig(g, 6, 22, 5, 0.3);
  sprig(g, 26, 23, 5, -0.3);
  return overlay(base, g);
}

/** Charcoal stone bricks (16 x 8 texels, offset rows) with darker mortar joints. */
export function stoneDark() {
  const rand = rng(9110);
  const n = smoothNoise(rng(9111), SIZE, 4);
  return image(SIZE, (x, y) => {
    const row = Math.floor(y / 8);
    const bx = (x + (row % 2) * 8) % 16;
    const joint = y % 8 === 7 || bx === 15;
    const brick = Math.floor((x + (row % 2) * 8) / 16) + row * 3;
    const tone = 0.88 + ((brick * 37) % 11) / 60;
    if (joint) return shade([30, 27, 34], 0.95 + rand() * 0.1);
    return shade(mix([52, 48, 58], [74, 69, 80], n(x, y)), tone * (0.95 + rand() * 0.08));
  });
}

/** Hearth plaque: charcoal stone with a gold crescent moon and two stars (stretched over a face). */
export function plaque() {
  const rand = rng(9112);
  const g = grid();
  moon(g, 16, 16, 9);
  star(g, 5, 16, 2);
  star(g, 27, 16, 2);
  return overlay(image(SIZE, () => shade([40, 36, 46], 0.92 + rand() * 0.12)), g);
}

/** Firebox glow: the back wall of a lit fire, bright yellow low down fading to deep orange. */
export function fireGlow() {
  const rand = rng(9113);
  const n = smoothNoise(rng(9114), SIZE, 4);
  return image(SIZE, (x, y) => {
    const t = Math.min(1, Math.max(0, y / SIZE * 0.8 + n(x, y) * 0.45));
    return shade(mix([168, 52, 18], [255, 196, 70], t), 0.94 + rand() * 0.1);
  });
}

/** Dark red cloth with a gold crescent, stars and a gold fringe at the bottom (stretched decal). */
export function clothRed() {
  const rand = rng(9115);
  const g = grid();
  moon(g, 15, 11, 6);
  star(g, 8, 20, 1);
  star(g, 22, 22, 1);
  flower(g, 12, 23, PINK);
  for (let x = 2; x < 30; x++) put(g, x, 27, GOLD[1]);
  for (let x = 3; x < 30; x += 3) for (let y = 28; y < 32; y++) put(g, x, y, GOLD[y === 31 ? 0 : 1]);
  return overlay(image(SIZE, (x, y) => {
    if (y >= 28 && x % 3 !== 0) return [0, 0, 0, 0];
    return shade((x + y) % 2 ? [122, 28, 34] : [108, 24, 30], 0.95 + rand() * 0.08);
  }), g);
}

/** Glowing coals for burners and grates (brighter than the plain ember tile). */
export function coals() {
  const rand = rng(9116);
  return image(SIZE, (x, y) => {
    const glow = Math.sin(x * 0.6) * Math.sin(y * 0.55 + 1) + Math.sin((x + y) * 0.33 + 2) * 0.5;
    let c = [110, 24, 14];
    if (glow > 0.45) c = [255, 150, 46];
    else if (glow > -0.15) c = [214, 76, 24];
    if (rand() < 0.06) c = [255, 222, 110];
    return [clamp(c[0]), clamp(c[1]), clamp(c[2]), 255];
  });
}
