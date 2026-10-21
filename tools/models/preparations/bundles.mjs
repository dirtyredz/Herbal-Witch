// Dried herb bundles built from small cubes, like the growing herbs (owner feedback 0.2.0-dev.1:
// the flat bundle sprite bled onto the host block's faces). A bundle hangs upside down: a short
// tied stem bunch, a hot-pink twine band, then a tapering head of leaves with a few florets in the
// herb's own colours, every face one swatch of the parts atlas (#parts). Shared by the hung bundle
// block (ceiling and wall forms, one model pair per herb), the drying rack, the stove and the
// apothecary counter. About a dozen elements each.
import { box } from '../geometry.mjs';
import { swatchUv } from '../../art/botany/parts.mjs';
import { rng } from '../../art/pixels.mjs';
import { separateCoplanar } from '../botany/separate.mjs';

/** [stems, head, head light, florets] swatches per herb (dried colours from the parts atlas). */
export const BUNDLE_PALETTES = {
  lavender: ['grey_green', 'grey_green', 'lavender_deep', 'lavender_light'],
  sage: ['stem', 'sage', 'sage_light', 'grey_green'],
  chamomile: ['stem', 'leaf', 'leaf_light', 'white'],
  yarrow: ['stem', 'leaf_light', 'leaf', 'umbel'],
  rosemary: ['bark', 'needle', 'leaf_dark', 'lavender_light'],
  thyme: ['bark', 'thyme', 'thyme_light', 'thyme_flower'],
  mint: ['stem', 'mint', 'mint_light', 'leaf_bright'],
  nettle: ['stem_dark', 'nettle', 'leaf_dark', 'leaf'],
  valerian: ['stem', 'leaf', 'leaf_dark', 'blush'],
  mugwort: ['stem_dark', 'silver', 'grey_green', 'bark'],
  belladonna: ['stem_dark', 'leaf_dark', 'leaf', 'berry_black'],
  nightshade: ['stem_dark', 'leaf_dark', 'leaf', 'berry_red'],
  wolfsbane: ['stem', 'leaf_dark', 'hood', 'hood_light'],
  mandrake: ['root', 'rosette', 'rosette_light', 'lavender'],
  // the drying rack's yellow-flowered bundle
  yellow: ['stem', 'leaf', 'leaf_light', 'yellow'],
};

const sw = (from, to, swatch) => box(from, to, '#parts', { decal: { all: swatchUv(swatch) } });

/**
 * One bundle hanging from (cx, top, cz): stems tied at the top, the head below, about 12 px long
 * (scaled by `length`). `depth` limits its z extent (a bundle against a wall is flatter). Florets
 * are placed by `seed` so neighbouring bundles differ. With `across: 'x'` the bundle is turned a
 * quarter so its flat side lies along z (against a west or east face): cx/cz and depth still name
 * the bundle's own width and depth axes, which are then swapped.
 */
export function bundle(palette, { cx, cz, top, depth = 5, length = 1, seed = 1, across = 'z' }) {
  if (across === 'x') {
    const swap = (p) => [p[2], p[1], p[0]];
    const FLIP = { north: 'west', south: 'east', west: 'north', east: 'south', up: 'up', down: 'down' };
    return bundle(palette, { cx, cz, top, depth, length, seed }).map((e) => ({
      ...e, from: swap(e.from), to: swap(e.to), faces: Object.fromEntries(Object.entries(e.faces).map(([f, v]) => [FLIP[f], v])),
    }));
  }
  const [stems, head, light, floret] = BUNDLE_PALETTES[palette] ?? (() => { throw new Error(`no bundle palette ${palette}`); })();
  const y = (d) => top - d * length;
  const dz = (w) => Math.min(w, depth) / 2;
  const stemHalf = Math.min(0.9, depth / 2 - 0.1);
  const out = [
    sw([cx - 1, y(3), cz - stemHalf], [cx + 1, top, cz + stemHalf], stems),
    sw([cx - 1.2, y(2.4), cz - stemHalf - 0.2], [cx + 1.2, y(1.6), cz + stemHalf + 0.2], 'pink'), // the twine
  ];
  // the head: tapering layers, each a little off the one above so no faces share a plane
  const layers = [[3, 5.5, 4, head], [5.5, 8.5, 5, light], [8.5, 10.5, 3.6, head], [10.5, 12, 1.6, light]];
  for (const [d0, d1, w, s] of layers) out.push(sw([cx - w / 2, y(d1), cz - dz(w)], [cx + w / 2, y(d0), cz + dz(w)], s));
  // florets and leaf tips standing out of the head
  const rand = rng(4200 + seed);
  for (let i = 0; i < 5; i++) {
    const d = 4.2 + i * 1.45 + rand() * 0.3;
    const side = i % 2 ? 1 : -1;
    const s = 1.1 + rand() * 0.3;
    // the widest layer the floret reaches into decides how far out it sits
    const half = Math.max(...layers.filter(([d0, d1]) => d - s / 2 < d1 && d + s / 2 > d0).map(([, , w]) => w / 2));
    const x = cx + side * (half + 0.5 + rand() * 0.3 - s / 2); // half sunk, its outer face clear of the layer
    const t = Math.min(s, depth); // a floret never sticks out past the bundle's depth
    const z = cz + (rand() - 0.5) * Math.max(0, Math.min(3, depth) - t);
    out.push(sw([x - s / 2, y(d) - s / 2, z - t / 2], [x + s / 2, y(d) + s / 2, z + t / 2], i === 2 ? light : floret));
  }
  return separateCoplanar(out);
}

const HUNG = 'block/preparations/bundles';

/** The hung bundle block's models: per herb a ceiling form on a wooden peg and a wall form. */
export function hungBundles(herbs) {
  return herbs.flatMap(([group, herb], i) => {
    const textures = {
      parts: 'herbalwitch:block/botany/parts', wood: 'herbalwitch:block/material/wood',
      particle: `herbalwitch:item/preparations/herbs/${group}/${herb}/bundle`,
    };
    const peg = (from, to) => box(from, to, '#wood');
    return [
      {
        path: `${HUNG}/${group}/${herb}/ceiling`, textures, ao: false,
        elements: [peg([7.25, 14.5, 7.25], [8.75, 16, 8.75]), ...bundle(herb, { cx: 8, cz: 8, top: 14.5, seed: i })],
      },
      {
        path: `${HUNG}/${group}/${herb}/wall`, textures, ao: false,
        elements: [peg([7.25, 12.25, 12.75], [8.75, 13.5, 16]), ...bundle(herb, { cx: 8, cz: 13.6, top: 12.25, depth: 3.2, length: 0.95, seed: i })],
      },
    ];
  });
}
