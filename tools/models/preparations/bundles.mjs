// Dried herb bundles as flat 2D sprites (the owner's preferred look, 0.2.0-dev.1 and again from
// dev.5): the herb's own 16 x 16 bundle sprite (item/preparations/herbs/<group>/<herb>/bundle,
// stems up under a hot-pink tie, the spray fanning below) stretched over zero-thickness cutout
// planes, plus a small 3D tie and, where a bundle hangs from a block, a wooden peg.
// The dev.1 sprites bled onto the host block (owner screenshot 08-dried-herb-leak.png): the planes
// were one pixel thick, so their stretched top and bottom faces drew a smear on the neighbour's
// faces, and they ran right up to the cell boundary. Now a plane has only its two broad faces (no
// up/down/edge faces to smear), and every plane keeps at least INSET model pixels from every cell
// boundary, so it never lies on or pokes through a neighbouring block's face.
// Shared by the hung bundle block (ceiling and wall forms, one model pair per herb), the drying
// rack, the stove and the apothecary counter.
import { box, plane } from '../geometry.mjs';
import { swatchUv } from '../../art/content/botany/parts.mjs';
import { HERB_LIST } from '../../art/content/botany/herbs.mjs';

/** Minimum distance (model px) of a bundle plane from any cell boundary. */
export const INSET = 0.75;

const GROUP_OF = Object.fromEntries(HERB_LIST.map(([group, herb]) => [herb, group]));

/** Texture variable a bundle plane binds for a herb. */
export const bundleVar = (herb) => `bundle_${herb}`;

/** The model textures binding the sprites of the given herbs (plus the parts atlas for the tie). */
export function bundleTextures(herbs) {
  return Object.fromEntries(herbs.map((h) => {
    if (!GROUP_OF[h]) throw new Error(`no bundle sprite for ${h}`);
    return [bundleVar(h), `herbalwitch:item/preparations/herbs/${GROUP_OF[h]}/${h}/bundle`];
  }));
}

/**
 * One bundle hanging from `top`. `at: [x, z]` is its centre; `across` says which way it faces:
 * 'z' (a plane seen from north and south), 'x' (seen from west and east) or 'cross' (both planes,
 * for a bundle that hangs free). `width` x `height` model px. The small pink tie box wraps the
 * sprite's own tie so the bundle is not wholly flat.
 */
export function bundle(herb, { at, top, width = 8, height = 12, across = 'z' }) {
  const [cx, cz] = at;
  const tex = `#${bundleVar(herb)}`;
  const half = width / 2;
  const out = [];
  if (across === 'z' || across === 'cross') out.push(plane('z', cz, cx - half, cx + half, top - height, top, tex));
  if (across === 'x' || across === 'cross') out.push(plane('x', cx, cz - half, cz + half, top - height, top, tex));
  // the sprite's tie is rows 5-6 of 16, 4 columns wide
  const tieW = Math.max(1.2, (4 * width) / 16 + 0.2);
  const tieH = (2 * height) / 16 + 0.2;
  const tieTop = top - (5 * height) / 16 + 0.15;
  const dx = across === 'x' ? 0.4 : tieW / 2;
  const dz = across === 'z' ? 0.4 : across === 'x' ? tieW / 2 : tieW / 2;
  out.push(box([cx - dx, tieTop - tieH, cz - dz], [cx + dx, tieTop, cz + dz], '#parts', { decal: { all: swatchUv('pink') } }));
  return out;
}

const HUNG = 'block/preparations/bundles';

/** The hung bundle block's models: per herb a ceiling form on a wooden peg and a wall form. */
export function hungBundles(herbs) {
  return herbs.flatMap(([group, herb]) => {
    const textures = {
      ...bundleTextures([herb]), parts: 'herbalwitch:block/botany/parts', wood: 'herbalwitch:block/material/wood',
      particle: `herbalwitch:item/preparations/herbs/${group}/${herb}/bundle`,
    };
    const peg = (from, to) => box(from, to, '#wood');
    return [
      {
        path: `${HUNG}/${group}/${herb}/ceiling`, textures, ao: false,
        elements: [peg([7.25, 14.5, 7.25], [8.75, 16, 8.75]), ...bundle(herb, { at: [8, 8], top: 14.5, across: 'cross' })],
      },
      {
        path: `${HUNG}/${group}/${herb}/wall`, textures, ao: false,
        elements: [peg([7.25, 12.25, 12.75], [8.75, 13.75, 16]), ...bundle(herb, { at: [8, 14.25], top: 13.5 })],
      },
    ];
  });
}
