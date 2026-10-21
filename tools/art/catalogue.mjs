// The procedural texture manifest: every PNG tools/generate-textures.mjs writes, as paths relative
// to assets/herbalwitch/textures/, in a fixed order. Textures a Blockbench project owns (its
// piece.json `owned_textures`: the rug and the lamp shade) are not listed; tools/compile-blockbench.mjs
// writes those, and the ownership test keeps the two sets apart.
import { brass, glass, tincture, fabricPink, trimPurple } from './materials.mjs';
import { velvet, paintGreen, runner } from './patterns.mjs';
import temperate from './botany/temperate.mjs';
import dry from './botany/dry.mjs';
import wet from './botany/wet.mjs';
import shadeHerbs from './botany/shade.mjs';
import woodland from './botany/woodland.mjs';
import { soilDry, soilMoist } from './botany/garden.mjs';
import { plantParts } from './botany/parts.mjs';
import { HERB_LIST } from './botany/herbs.mjs';
import { iron, ember, flame } from './stations/materials.mjs';
import * as house from './stations/house.mjs';
import { mortarStone, plinthStone, pestleStone, herbPile } from './stations/stone.mjs';
import { ivy } from './foliage.mjs';
import * as interfaces from './stations/interfaces.mjs';
import { PREPARATION_HERBS, bundleSprite, powderSprite } from './preparations/materials.mjs';
import { PRODUCTS } from './preparations/products.mjs';

/** Furniture and house materials (block/material/). */
const MATERIALS = {
  brass, // hardware
  glass, // cutout bottle glass
  tincture, // bottle contents
  fabric_pink: fabricPink, // plain hot-pink upholstery
  trim_purple: trimPurple, // dark-purple piping, knobs, small details
  // the shared house materials (furniture, stations, garden): docs/ART.md
  wood: house.wood, // the dark brown house wood, grain along U (beams, rails, tops, frames)
  wood_post: house.woodPost, // the same wood, grain along V (posts, legs)
  wood_end: house.woodEnd, // end grain: caps and post tops
  iron: house.ironDark, // charcoal iron brackets, bands, plates
  gold: house.gold, // rivets, studs, knobs, bands
  copper: house.copper, // handles and beaded rims
  plum: house.plum, // dark purple surfaces
  ivy, // cutout: dense trailing ivy with small pink flowers
  velvet, // forest-green tufted upholstery
  paint_green: paintGreen, // deep green painted drawer fronts
  runner: () => runner(false), // cutout decal: the armchair's green runners with a pointed hem
  runner_magenta: () => runner(true), // cutout decal: the apothecary chest's magenta runner with a gold moon
};

/** Station block materials (block/stations/): untinted; flame and cloth are cutout. */
const STATION_MATERIALS = {
  iron, ember, flame, // the oil lantern's iron and flame, the bundle coals
  plum_painted: house.plumPainted, // kettle and pot bodies: moons, stars, flowers on vines
  plum_lid: house.plumLid, // vessel lids seen from above
  stone_dark: house.stoneDark, // hearth bricks
  plaque: house.plaque, // the hearth's moon plaque
  fire_glow: house.fireGlow, // lit firebox back walls
  coals: house.coals, // burners and grates
  cloth_red: house.clothRed, // cutout: the stove's towel
  mortar_stone: mortarStone,
  plinth_stone: plinthStone,
  pestle_stone: pestleStone,
  herb_pile: herbPile,
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
  for (const [name, make] of Object.entries(STATION_MATERIALS)) out.push([`block/stations/${name}.png`, make]);
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
