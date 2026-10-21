// Generates The Herbal Witch's procedural textures (deterministic, no dependencies).
//
//   node tools/generate-textures.mjs [--check] [--contact-sheet <path.png>]
//
// Writes exactly the files tools/art/catalogue.mjs lists (furniture and house materials, the botany
// set, station materials, GUI atlases, preparation sprites) under
// src/main/resources/assets/herbalwitch/textures/ and nothing else; re-running gives identical bytes
// and only changed files are rewritten. Textures painted in a Blockbench project (the rug, the lamp
// shade) belong to tools/compile-blockbench.mjs. With --check nothing is written and the command
// fails when a committed texture is missing or stale.
//
// --contact-sheet additionally writes one review sheet (every botany sprite at x6, a row per herb)
// to that path, which belongs outside the repo; it is never written by default.
import { writeFileSync, mkdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { png } from './image/png-encode.mjs';
import { textureManifest, botanySheetRows } from './art/catalogue.mjs';
import { writeOutputs } from './art/output.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const check = process.argv.includes('--check');
const files = new Map(textureManifest().map(([rel, make]) => [rel, png(make())]));
const result = writeOutputs(join(root, 'src/main/resources/assets/herbalwitch/textures'), files,
  { name: 'generate-textures', dirs: [], files: [...files.keys()], ext: '.png' }, { check });
if (check && result.problems.length) {
  for (const p of result.problems) console.error(`ERROR ${p}`);
  process.exit(1);
}
console.log(`${files.size} procedural textures: ${result.written.length} written, ${result.unchanged.length} unchanged`);

// Review-only contact sheet: 16 px sprites at x6 with 4 px gaps on mid grey. The encoder takes
// square images, so the sheet is padded to a square.
const sheetArg = process.argv.indexOf('--contact-sheet');
if (sheetArg !== -1) {
  const out = process.argv[sheetArg + 1];
  if (!out) throw new Error('--contact-sheet needs an output path');
  const rows = botanySheetRows().map((makers) => makers.map((make) => make()));
  const SCALE = 6;
  const GAP = 4;
  const cell = 16 * SCALE;
  const side = GAP + Math.max(6, rows.length) * (cell + GAP);
  const px = new Array(side * side).fill([128, 128, 128, 255]);
  rows.forEach((row, r) => {
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
