// Generates The Herbal Witch's original block textures (deterministic, no dependencies).
//
//   node tools/generate-textures.mjs
//
// Writes exactly the files listed in TEXTURES (block/material/) and the botany set (HERBS and the
// garden soil tiles) under src/main/resources/assets/herbalwitch/textures/ and nothing else;
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
import { png } from './art/png.mjs';
import { woodGrain, paintedWood, cutStone, brass, glass, tincture } from './art/materials.mjs';
import { fabricFloral, rug, lampShade } from './art/patterns.mjs';
import temperate from './art/botany/temperate.mjs';
import dry from './art/botany/dry.mjs';
import wet from './art/botany/wet.mjs';
import shadeHerbs from './art/botany/shade.mjs';
import woodland from './art/botany/woodland.mjs';
import { soilDry, soilMoist } from './art/botany/garden.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const texRoot = join(root, 'src/main/resources/assets/herbalwitch/textures');
const outDir = join(texRoot, 'block/material');

// The complete texture manifest. GeneratedModelsTest checks every model reference against the files.
const TEXTURES = {
  wood_grain: woodGrain, // tinted: natural frames
  painted_wood: paintedWood, // tinted: painted frames
  cut_stone: cutStone, // tinted: the stone finish's frame
  fabric_floral: fabricFloral, // tinted: upholstery and counter panels
  rug: rug, // tinted: the small rug
  lamp_shade: lampShade, // tinted: the table lamp's shade
  brass: brass, // untinted hardware
  glass: glass, // untinted cutout bottle glass
  tincture: tincture, // untinted bottle contents
};

mkdirSync(outDir, { recursive: true });
for (const [name, make] of Object.entries(TEXTURES)) {
  writeFileSync(join(outDir, `${name}.png`), png(make()));
  console.log(`wrote material/${name}.png`);
}

// Botany: [group, herb, module] in a fixed order. Each herb writes block stage_0..3 and item seeds
// + fresh; the two garden soil tiles follow.
const HERBS = [
  ['temperate', 'lavender', temperate],
  ['temperate', 'sage', temperate],
  ['temperate', 'chamomile', temperate],
  ['temperate', 'yarrow', temperate],
  ['dry', 'rosemary', dry],
  ['dry', 'thyme', dry],
  ['wet', 'mint', wet],
  ['wet', 'nettle', wet],
  ['shade', 'valerian', shadeHerbs],
  ['shade', 'mugwort', shadeHerbs],
  ['woodland', 'belladonna', woodland],
  ['woodland', 'nightshade', woodland],
  ['woodland', 'wolfsbane', woodland],
  ['woodland', 'mandrake', woodland],
];

const botany = []; // [path relative to textures/, image]
const sheetRows = []; // per herb: stage_0..3, seeds, fresh
for (const [group, herb, mod] of HERBS) {
  const { stages, seeds, fresh } = mod[herb];
  const imgs = [...stages.map((make) => make()), seeds(), fresh()];
  stages.forEach((_, i) => botany.push([`block/botany/${group}/${herb}/stage_${i}.png`, imgs[i]]));
  botany.push([`item/botany/${group}/${herb}/seeds.png`, imgs[4]]);
  botany.push([`item/botany/${group}/${herb}/fresh.png`, imgs[5]]);
  sheetRows.push(imgs);
}
botany.push(['block/botany/garden/soil_dry.png', soilDry()]);
botany.push(['block/botany/garden/soil_moist.png', soilMoist()]);

for (const [rel, img] of botany) {
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
