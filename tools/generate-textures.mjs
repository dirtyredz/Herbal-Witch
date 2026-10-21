// Generates The Herbal Witch's original block textures (deterministic, no dependencies).
//
//   node tools/generate-textures.mjs
//
// Writes exactly the files listed in TEXTURES to
// src/main/resources/assets/herbalwitch/textures/block/material/ and nothing else; re-running gives
// identical bytes. Materials: tools/art/materials.mjs; decorative motifs: tools/art/patterns.mjs.
import { writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { png } from './art/png.mjs';
import { woodGrain, paintedWood, cutStone, brass, glass, tincture } from './art/materials.mjs';
import { fabricFloral, rug, lampShade } from './art/patterns.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const outDir = join(root, 'src/main/resources/assets/herbalwitch/textures/block/material');

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
