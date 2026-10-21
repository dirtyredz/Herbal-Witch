// Dried herb bundles built from small cubes, like the growing herbs (owner feedback 0.2.0-dev.1:
// the flat bundle sprite bled onto the host block's faces; dev.2: solid stacked blobs with no
// stems). A bundle hangs upside down: a short gathered stem bunch under the hook, a hot-pink twine
// tie, then thin visible stems fanning down into a few small sparse leaf and flower clusters in
// the herb's own colours, every face one swatch of the parts atlas (#parts). Slim and airy: about
// 4 px across, nothing solid. Shared by the hung bundle block (ceiling and wall forms, one model
// pair per herb), the drying rack, the stove and the apothecary counter. About 20 elements each.
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
 * One bundle hanging from (cx, top, cz): stems tied at the top, fanning below, about 12 px long
 * (scaled by `length`). `depth` limits its z extent (a bundle against a wall is flatter). Leaves
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
  const half = depth / 2;
  const T = Math.min(0.8, depth); // one stem is under a pixel thick: slim, airy, never a solid column
  const box1 = (x0, x1, d0, d1, z0, z1, swatch) => sw([cx + x0, y(d1), cz + z0], [cx + x1, y(d0), cz + z1], swatch);
  const g = Math.min(0.7, half - 0.1); // the gathered stems and the twine are a little wider than one stem
  const w = Math.min(1, half);
  const out = [
    box1(-0.7, 0.7, 0, 2.4, -g, g, stems), // the stems gathered under the hook
    box1(-1, 1, 1.4, 2.2, -w, w, 'pink'), // the twine tie
    box1(-T / 2, T / 2, 2.2, 9.6, -T / 2, T / 2, stems), // centre stem
  ];
  const rand = rng(4200 + seed);
  const leaf = (xc, d, s, swatch) => {
    const t = Math.min(s, depth);
    const zc = (rand() - 0.5) * Math.max(0, Math.min(2, depth) - t);
    out.push(box1(xc - s / 2, xc + s / 2, d - s / 2, d + s / 2, zc - t / 2, zc + t / 2, swatch));
  };
  // two side stems fan out below the tie in two kinked steps, each carrying a few small leaves
  for (const s of [-1, 1]) {
    out.push(box1(s * 0.6 - T / 2, s * 0.6 + T / 2, 2.2, 5, -T / 2, T / 2, stems));
    out.push(box1(s * 1.55 - T / 2, s * 1.55 + T / 2, 4.8, 8.6, -T / 2, T / 2, stems));
    leaf(s * (1.2 + 0.7 + rand() * 0.2), 3.5, 1.2, head);
    leaf(s * (1.95 + 0.5 + rand() * 0.15), 5.6 + rand() * 0.3, 1.3, light);
    leaf(s * (1.95 + 0.5 + rand() * 0.15), 7.4 + rand() * 0.3, 1.2, head);
    leaf(s * 1.55, 9.2 + rand() * 0.3, 1.2, floret); // the floret at its stem tip
  }
  leaf(0, 10.2, 1.2, floret);
  if (depth >= 3) {
    // a front and a back stem with a leaf each, so the bunch has some depth
    for (const s of [-1, 1]) {
      const zc = s * Math.min(0.95, half - 0.5);
      out.push(box1(-T / 2, T / 2, 3, 7, zc - T / 2, zc + T / 2, stems));
      out.push(box1(-0.55, 0.55, 6.6, 7.7, zc + s * 0.4 - 0.55, zc + s * 0.4 + 0.55, s > 0 ? head : light));
    }
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
