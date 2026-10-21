// Generates The Herbal Witch's original block textures (deterministic, no dependencies).
//
//   node tools/generate-textures.mjs
//
// Writes exactly the files listed in TEXTURES (block/material/), the botany set (HERBS and the
// garden soil tiles) and the station/preparation set (block/stations, gui/stations, item/preparations) under src/main/resources/assets/herbalwitch/textures/ and nothing else;
// re-running gives identical bytes. Materials: tools/art/materials.mjs; decorative motifs:
// tools/art/patterns.mjs; herbs, seeds, fresh items and soil: tools/art/botany/.
//
//   node tools/generate-textures.mjs --contact-sheet <path.png>
//
// additionally writes one review sheet (every botany sprite at x6, a row per herb) to that path,
// which belongs outside the repo; it is never written by default.
import { writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { png } from './image/png-encode.mjs';
import { brass, glass, tincture, darkWood, fabricPink, trimPurple } from './art/materials.mjs';
import { rug, lampShade, velvet, paintGreen, runner } from './art/patterns.mjs';
import temperate from './art/botany/temperate.mjs';
import dry from './art/botany/dry.mjs';
import wet from './art/botany/wet.mjs';
import shadeHerbs from './art/botany/shade.mjs';
import woodland from './art/botany/woodland.mjs';
import { soilDry, soilMoist } from './art/botany/garden.mjs';
import { plantParts } from './art/botany/parts.mjs';
import { HERB_LIST } from './art/botany/herbs.mjs';
import { iron, ember, flame } from './art/stations/materials.mjs';
import * as house from './art/stations/house.mjs';
import { mortarStone, plinthStone, pestleStone, herbPile } from './art/stations/stone.mjs';
import { ivy, bundle } from './art/foliage.mjs';
import * as interfaces from './art/stations/interfaces.mjs';
import { PREPARATION_HERBS, bundleSprite, powderSprite } from './art/preparations/materials.mjs';
import { PRODUCTS } from './art/preparations/products.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const texRoot = join(root, 'src/main/resources/assets/herbalwitch/textures');
const outDir = join(texRoot, 'block/material');

// The complete texture manifest. GeneratedModelsTest checks every model reference against the files.
const TEXTURES = {
  dark_wood: darkWood, // frames, legs, boards
  brass: brass, // hardware
  glass: glass, // cutout bottle glass
  tincture: tincture, // bottle contents
  fabric_pink: fabricPink, // plain hot-pink upholstery
  trim_purple: trimPurple, // dark-purple piping, knobs, small details
  rug: rug, // the small rug
  lamp_shade: lampShade, // the table lamp's shade
  // the shared house materials (furniture, stations, garden): docs/ART.md
  wood: house.wood, // warm reddish-brown, grain along U (beams, rails, tops)
  wood_post: house.woodPost, // the same wood, grain along V (posts, legs)
  wood_end: house.woodEnd, // end grain: caps and post tops
  iron: house.ironDark, // charcoal iron brackets, bands, plates
  gold: house.gold, // rivets, studs, knobs, bands
  copper: house.copper, // handles and beaded rims
  plum: house.plum, // dark purple surfaces
  ivy: ivy, // cutout: dense trailing ivy with small pink flowers
  velvet: velvet, // forest-green tufted upholstery
  paint_green: paintGreen, // deep green painted drawer fronts
  runner: runner, // cutout decal: draped runners with a pointed hem
};

// Station block materials (block/stations/): untinted; flame is cutout.
const STATION_TEXTURES = {
  iron, ember, flame, // the oil lantern's iron and flame, the bundles' coals
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
  bundle_green: () => bundle('green'), // cutout: rack bundles
  bundle_lavender: () => bundle('lavender'),
  bundle_white: () => bundle('white'),
  bundle_yellow: () => bundle('yellow'),
};

mkdirSync(outDir, { recursive: true });
for (const [name, make] of Object.entries(TEXTURES)) {
  writeFileSync(join(outDir, `${name}.png`), png(make()));
  console.log(`wrote material/${name}.png`);
}

// Botany: [group, herb, module] in a fixed order. Each herb writes block stage_0..2 and item seeds
// + fresh; the two garden soil tiles follow.
const GROUP_ART = { temperate, dry, wet, shade: shadeHerbs, woodland };
const HERBS = HERB_LIST.map(([group, herb]) => [group, herb, GROUP_ART[group]]);

const botany = []; // [path relative to textures/, image]
const sheetRows = []; // per herb: stage_0..2, seeds, fresh
for (const [group, herb, mod] of HERBS) {
  const { stages, seeds, fresh } = mod[herb];
  const imgs = [...stages.map((make) => make()), seeds(), fresh()];
  stages.forEach((_, i) => botany.push([`block/botany/${group}/${herb}/stage_${i}.png`, imgs[i]]));
  botany.push([`item/botany/${group}/${herb}/seeds.png`, imgs[3]]);
  botany.push([`item/botany/${group}/${herb}/fresh.png`, imgs[4]]);
  sheetRows.push(imgs);
}
botany.push(['block/botany/garden/soil_dry.png', soilDry()]);
botany.push(['block/botany/garden/soil_moist.png', soilMoist()]);
botany.push(['block/botany/parts.png', plantParts()]); // swatches the 3D mature herbs are built from

for (const [rel, img] of botany) {
  mkdirSync(dirname(join(texRoot, rel)), { recursive: true });
  writeFileSync(join(texRoot, rel), png(img));
  console.log(`wrote ${rel}`);
}

// Stations: block materials, the five GUI atlases, then the preparation item sprites.
const stations = [];
for (const [name, make] of Object.entries(STATION_TEXTURES)) stations.push([`block/stations/${name}.png`, make()]);
for (const [name, make] of Object.entries(interfaces)) stations.push([`gui/stations/${name}.png`, make()]);
for (const [group, herb] of PREPARATION_HERBS) {
  stations.push([`item/preparations/herbs/${group}/${herb}/bundle.png`, bundleSprite(herb)()]);
  stations.push([`item/preparations/herbs/${group}/${herb}/powder.png`, powderSprite(group, herb)()]);
}
for (const [rel, make] of PRODUCTS) stations.push([`item/preparations/${rel}`, make()]);

for (const [rel, img] of stations) {
  mkdirSync(dirname(join(texRoot, rel)), { recursive: true });
  writeFileSync(join(texRoot, rel), png(img));
  console.log(`wrote ${rel}`);
}

// Review-only contact sheet: 16 px sprites at x6 with 4 px gaps on mid grey. The encoder takes
// square images, so the sheet is padded to a square.
const sheetArg = process.argv.indexOf('--contact-sheet');
if (sheetArg !== -1) {
  const out = process.argv[sheetArg + 1];
  if (!out) throw new Error('--contact-sheet needs an output path');
  const SCALE = 6;
  const GAP = 4;
  const cell = 16 * SCALE;
  const side = GAP + Math.max(6, sheetRows.length) * (cell + GAP);
  const px = new Array(side * side).fill([128, 128, 128, 255]);
  sheetRows.forEach((row, r) => {
    row.forEach((img, c) => {
      for (let y = 0; y < cell; y++) {
        for (let x = 0; x < cell; x++) {
          const p = img.px[Math.floor(y / SCALE) * 16 + Math.floor(x / SCALE)];
          if (p[3] === 255) px[(GAP + r * (cell + GAP) + y) * side + GAP + c * (cell + GAP) + x] = p;
        }
      }
    });
  });
  mkdirSync(dirname(out), { recursive: true });
  writeFileSync(out, png({ size: side, px }));
  console.log(`wrote contact sheet ${out}`);
}
