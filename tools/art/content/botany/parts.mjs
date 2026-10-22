// The swatch atlas the 3D mature herbs are built from: an 8 x 8 grid of 4 x 4-texel colour swatches
// (stems, leaf greens, petals, berries), each with a lighter top-left and soft speckle so a small
// cube reads as a living surface. tools/models/botany/plants.mjs names swatches by SWATCHES index.
import { image, rng, clamp } from '../../pixels.mjs';
import { hex } from './sprites.mjs';

/** Swatch names in atlas order (row by row, 8 per row). */
export const SWATCHES = [
  'stem_dark', 'stem', 'leaf_dark', 'leaf', 'leaf_light', 'leaf_bright', 'sage', 'sage_light',
  'grey_green', 'needle', 'mint', 'mint_light', 'lavender', 'lavender_light', 'white', 'yellow',
  'yellow_deep', 'pink', 'cream', 'hood', 'hood_light', 'berry_black', 'berry_shine', 'berry_red',
  'berry_red_light', 'plum', 'root', 'bark', 'nettle', 'silver', 'blush', 'rosette',
  'rosette_light', 'olive', 'umbel', 'lavender_deep', 'thyme', 'thyme_light', 'thyme_flower', 'bell',
];

const COLOURS = {
  stem_dark: '#3e5a26', stem: '#5a7e34', leaf_dark: '#2f5c28', leaf: '#4a8c36', leaf_light: '#72b048', leaf_bright: '#9ccf58',
  sage: '#86a688', sage_light: '#b2cbaa', grey_green: '#6c8c70', needle: '#356043', mint: '#46a23c', mint_light: '#7ccc5c',
  lavender: '#8050c0', lavender_light: '#ae88e2', white: '#f2eee2', yellow: '#f2c235', yellow_deep: '#cc8f1c', pink: '#e47aaa',
  cream: '#e8ddb4', hood: '#4a46b4', hood_light: '#7c78e4', berry_black: '#24172c', berry_shine: '#6e5a80', berry_red: '#c22e2a',
  berry_red_light: '#ec6a50', plum: '#5c2c68', root: '#c4a272', bark: '#6a4a2a', nettle: '#2e5c2c', silver: '#a4b2a2',
  blush: '#ecc8d6', rosette: '#3a7a2e', rosette_light: '#62a446', olive: '#6e7c2e', umbel: '#f6f2e6', lavender_deep: '#5e3a96',
  thyme: '#4c7a3a', thyme_light: '#7aa456', thyme_flower: '#d8a6d6', bell: '#7a3a8c',
};

/** The UV rectangle (0..16 units) of a swatch's centre 2 x 2 texels, inset to avoid bleeding. */
export function swatchUv(name) {
  const i = SWATCHES.indexOf(name);
  if (i < 0) throw new Error(`unknown swatch ${name}`);
  const u = (i % 8) * 2;
  const v = Math.floor(i / 8) * 2;
  return [u + 0.25, v + 0.25, u + 1.75, v + 1.75];
}

export function plantParts() {
  const rand = rng(7301);
  return image(32, (x, y) => {
    const i = Math.floor(y / 4) * 8 + Math.floor(x / 4);
    const name = SWATCHES[i];
    if (!name) return [0, 0, 0, 255];
    const [r, g, b] = hex(COLOURS[name]);
    const lx = x % 4;
    const ly = y % 4;
    let f = 1 + (rand() - 0.5) * 0.1;
    if (lx + ly <= 1) f *= 1.14;
    else if (lx + ly >= 5) f *= 0.88;
    return [clamp(r * f), clamp(g * f), clamp(b * f), 255];
  });
}
