// The procedural texture manifest: every PNG tools/generate-textures.mjs writes, as paths relative
// to assets/herbalwitch/textures/, in a fixed order. Textures a Blockbench piece owns (piece.json
// `owned_textures`, under block/pieces/) are not listed; tools/compile-blockbench.mjs writes those,
// and the ownership test keeps the two sets apart.
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
import { PREPARATION_HERBS, bundleSprite, powderSprite } from './preparations/materials.mjs';
import { PRODUCTS } from './preparations/products.mjs';

/** The shared house materials the procedural models bind (the herb bed and hung bundles): block/material/. */
const MATERIALS = {
  wood: house.wood, // the dark brown house wood, grain along U (rails, beams)
  wood_post: house.woodPost, // the same wood, grain along V (posts)
  wood_end: house.woodEnd, // end grain: caps and post tops
  gold: house.gold, // rivets and studs
};

const GROUP_ART = { temperate, dry, wet, shade: shadeHerbs, woodland };

/** [[path relative to textures/, () => image]] for every procedural texture, in write order. */
export function textureManifest() {
  const out = [];
  for (const [name, make] of Object.entries(MATERIALS)) out.push([`block/material/${name}.png`, make]);
  for (const [group, herb] of HERB_LIST) {
    const { seeds, fresh } = GROUP_ART[group][herb];
    out.push([`item/botany/${group}/${herb}/seeds.png`, seeds], [`item/botany/${group}/${herb}/fresh.png`, fresh]);
  }
  out.push(['block/botany/garden/soil_dry.png', soilDry], ['block/botany/garden/soil_moist.png', soilMoist]);
  out.push(['block/botany/parts.png', plantParts]); // swatches the 3D herbs and bundle ties are built from
  for (const [name, make] of Object.entries(interfaces)) out.push([`gui/stations/${name}.png`, make]);
  for (const [group, herb] of PREPARATION_HERBS) {
    out.push([`item/preparations/herbs/${group}/${herb}/bundle.png`, bundleSprite(herb)]);
    out.push([`item/preparations/herbs/${group}/${herb}/powder.png`, powderSprite(group, herb)]);
  }
  for (const [rel, make] of PRODUCTS) out.push([`item/preparations/${rel}`, make]);
  return out;
}

/** Per herb [seeds, fresh] makers, for the review contact sheet. */
export const botanySheetRows = () => HERB_LIST.map(([group, herb]) => [GROUP_ART[group][herb].seeds, GROUP_ART[group][herb].fresh]);
