// The house style for stations (docs/ART.md "Station style guide"): warm reddish-brown wood,
// charcoal iron, gold and copper hardware, dark plum vessels painted with gold moons, stars and
// pink flowers on olive vines, charcoal stone, firelight. 32 x 32, untinted, opaque unless noted.
import { rng, clamp, shade, mix, image } from '../pixels.mjs';
import { grid, moon, star, flower, sprig, overlay, smoothNoise, put, GOLD, PINK, MAGENTA } from '../motifs.mjs';

const SIZE = 32;

/** Wood grain running along U (beams, rails, mantels) or along V (posts, legs) when vertical. */
function grain(seed, dark, light, vertical) {
  const rand = rng(seed);
  const offs = Array.from({ length: SIZE }, () => rand() * 0.8);
  return image(SIZE, (px, py) => {
    const [x, y] = vertical ? [py, px] : [px, py];
    const wave = Math.sin(x * 0.11 + y * 0.05) * 0.7 + Math.sin(x * 0.04 + 1.3) * 0.9;
    const ring = (((y + wave + offs[y]) % 6) + 6) % 6;
    let t = 0.55 + Math.sin((y + wave) * 0.7) * 0.12;
    if (ring < 0.7) t = 0.12;
    else if (ring < 1.4) t = 0.34;
    t += (rand() - 0.5) * 0.12;
    if (rand() < 0.025) t *= 0.5; // pore
    return shade(mix(dark, light, Math.max(0, Math.min(1, t))), 1);
  });
}

export const wood = () => grain(9101, [84, 42, 22], [170, 94, 50], false);
export const woodPost = () => grain(9102, [78, 39, 21], [160, 88, 47], true);

/** End grain for caps and post tops: dark rings around a centre. */
export function woodEnd() {
  const rand = rng(9103);
  return image(SIZE, (x, y) => {
    const d = Math.hypot((x % 16) - 7.5, (y % 16) - 7.5);
    const ring = d % 3 < 1 ? 0.75 : 1;
    return shade([92, 46, 26], ring * (0.92 + rand() * 0.14));
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
 * band of gold moons and stars across the shoulder and pink/magenta flowers on olive sprigs
 * around the lower half, on plum. Repeats every 16 texels so each face shows one full motif.
 */
export function plumPainted() {
  // Vessel bellies span model y 1..9.5 (texture rows 13..30): the moon band sits on rows 15..24,
  // the flowers and vine sprigs on rows 23..30.
  const base = plum();
  const g = grid();
  for (const ox of [0, 16]) {
    moon(g, ox + 8, 19, 4.3);
    star(g, ox + 2, 16, 1);
    star(g, ox + 14, 15, 1);
    star(g, ox + 14, 21, 1);
    sprig(g, ox + 2, 31, 7, 0.2);
    sprig(g, ox + 12, 31, 7, -0.15);
    flower(g, ox + 3, 25, PINK);
    flower(g, ox + 6, 28, MAGENTA);
    flower(g, ox + 12, 25, PINK);
    flower(g, ox + 15, 28, MAGENTA);
    flower(g, ox + 9, 29, PINK);
  }
  return overlay(base, g);
}

/** A vessel lid seen from above (u = x, v = z): a central moon with stars and flowers around it. */
export function plumLid() {
  const base = plum();
  const g = grid();
  moon(g, 16, 16, 6);
  for (const [x, y] of [[8, 9], [24, 8], [7, 23], [25, 24], [16, 5]]) star(g, x, y, 1);
  for (const [x, y, r] of [[10, 15, PINK], [22, 18, MAGENTA], [14, 25, PINK], [20, 7, MAGENTA]]) flower(g, x, y, r);
  sprig(g, 9, 21, 5, 0.3);
  sprig(g, 23, 25, 6, -0.3);
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
