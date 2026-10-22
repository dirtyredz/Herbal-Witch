// The procedural texture manifest: every PNG tools/generate-textures.mjs writes, as paths relative
// to assets/herbalwitch/textures/, in a fixed order. Textures a Blockbench piece owns (piece.json
// `owned_textures`, under block/pieces/) are not listed; tools/compile-blockbench.mjs writes those,
// and the ownership test keeps the two sets apart.
//
// Density (tools/art/density.mjs): every entry comes out at two texels per model unit. The house
// materials, the soil and the herb atlas are drawn natively at 32 x 32; the item sprites are drawn on
// a 16 x 16 grid and the GUI atlases on vanilla's 256 x 256 sheet, then enlarged 2x nearest-neighbour
// (`drawnAt1x`), so they look exactly as before while their files carry the finer grid.
import temperate from './botany/temperate.mjs';
import dry from './botany/dry.mjs';
import wet from './botany/wet.mjs';
import shadeHerbs from './botany/shade.mjs';
import woodland from './botany/woodland.mjs';
import { soilDry, soilMoist } from './botany/garden.mjs';
import { plantParts } from './botany/parts.mjs';
import { HERB_LIST } from './botany/herbs.mjs';
import * as house from './stations/house.mjs';
import * as interfaces from './stations/interfaces.mjs';
import * as decorInterfaces from './interface/decor.mjs';
import { PREPARATION_HERBS, bundleSprite, powderSprite } from './preparations/materials.mjs';
import { PRODUCTS } from './preparations/products.mjs';
import { book } from './grimoire/book.mjs';
import { reader } from './grimoire/interface.mjs';
import { upscale } from './pixels.mjs';
import { TEXELS_PER_UNIT, checkDensity } from './density.mjs';

/** The shared house materials the procedural models bind (the herb bed and hung bundles): block/material/. */
const MATERIALS = {
  wood: house.wood, // the dark brown house wood, grain along U (rails, beams)
  wood_post: house.woodPost, // the same wood, grain along V (posts)
  wood_end: house.woodEnd, // end grain: caps and post tops
  gold: house.gold, // rivets and studs
};

const GROUP_ART = { temperate, dry, wet, shade: shadeHerbs, woodland };

/** A maker drawn at one texel per unit, enlarged to the set's density. */
const drawnAt1x = (make) => () => upscale(make(), TEXELS_PER_UNIT);

/**
 * [[path relative to textures/, () => image]] for every procedural texture, in write order. Each
 * maker checks its image against the density rule, so a producer drawing at the wrong size fails.
 */
export function textureManifest() {
  return drawnManifest().map(([rel, make]) => [rel, () => {
    const img = make();
    checkDensity(rel, img);
    return img;
  }]);
}

function drawnManifest() {
  const out = [];
  for (const [name, make] of Object.entries(MATERIALS)) out.push([`block/material/${name}.png`, make]);
  for (const [group, herb] of HERB_LIST) {
    const { seeds, fresh } = GROUP_ART[group][herb];
    out.push([`item/botany/${group}/${herb}/seeds.png`, drawnAt1x(seeds)], [`item/botany/${group}/${herb}/fresh.png`, drawnAt1x(fresh)]);
  }
  out.push(['block/botany/garden/soil_dry.png', soilDry], ['block/botany/garden/soil_moist.png', soilMoist]);
  out.push(['block/botany/parts.png', plantParts]); // swatches the 3D herbs and bundle ties are built from
  for (const [name, make] of Object.entries(interfaces)) out.push([`gui/stations/${name}.png`, drawnAt1x(make)]);
  for (const [name, make] of Object.entries(decorInterfaces)) out.push([`gui/decor/${name}.png`, drawnAt1x(make)]);
  for (const [group, herb] of PREPARATION_HERBS) {
    out.push([`item/preparations/herbs/${group}/${herb}/bundle.png`, drawnAt1x(bundleSprite(herb))]);
    out.push([`item/preparations/herbs/${group}/${herb}/powder.png`, drawnAt1x(powderSprite(group, herb))]);
  }
  for (const [rel, make] of PRODUCTS) out.push([`item/preparations/${rel}`, drawnAt1x(make)]);
  out.push(['item/grimoire/book.png', drawnAt1x(book)], ['gui/grimoire/reader.png', drawnAt1x(reader)]);
  return out;
}

/** Per herb [seeds, fresh] makers on their 16 x 16 drawing grid, for the review contact sheet. */
export const botanySheetRows = () => HERB_LIST.map(([group, herb]) => [GROUP_ART[group][herb].seeds, GROUP_ART[group][herb].fresh]);
