// Headless model previews for art review (deterministic, no game client, no dependencies).
//
//   node tools/preview-models.mjs --output <dir> [--references <dir>] [--scale <px per model pixel>]
//
// Reads the mod's blockstates, models and textures (authored and generated trees), checks that
// every state and item resolves with real textures and that every model file is rendered, then
// writes into <dir> (which belongs outside the repo):
//   sheets/<family>-{iso,front}.png   every state family at one fixed scale
//   sheets/herbs-on-beds.png          every herb age planted in a herb bed
//   sheets/items-gui.png              every item as the GUI shows it
//   scenes/<name>.png                 joined beds, multiblocks, vessels on heat, furniture
//   compare/<name>.png                owner reference beside the render (with --references)
//   manifest.json                     coverage, diagnostics, settings and input SHA-256s
// Exits 1 when anything is missing, unbound, unsupported or unrendered, or when any model or
// blockstate combination has overlapping coplanar faces facing the same way (z-fighting).
//
// Approximations: Minecraft's directional face shading only. No ambient occlusion, world light,
// shaders, animation, mipmapping, or item extrusion; emissive faces are drawn at full brightness.
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { png } from './image/png-encode.mjs';
import { Resources } from './preview/assets/resources.mjs';
import { Baker } from './preview/geometry/bake.mjs';
import { coverage } from './preview/report/coverage.mjs';
import { zFighting } from './preview/report/zfighting.mjs';
import { reviewSet } from './preview/report/review.mjs';
import { inputHashes, combinedHash } from './preview/report/manifest.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const arg = (name, fallback = null) => {
  const i = process.argv.indexOf(name);
  return i === -1 ? fallback : process.argv[i + 1];
};
const out = arg('--output');
if (!out) {
  console.error('usage: node tools/preview-models.mjs --output <dir> [--references <dir>] [--scale <n>]');
  process.exit(2);
}
const scale = Number(arg('--scale', '8'));

const res = new Resources(root);
const baker = new Baker(res);
const cov = coverage(res, baker);
const { images, errors } = reviewSet(res, baker, cov, { referenceDir: arg('--references'), scale });
const allErrors = [...cov.errors, ...zFighting(res, baker), ...errors];

for (const [name, img] of images) {
  const file = join(out, name);
  mkdirSync(dirname(file), { recursive: true });
  writeFileSync(file, png(img));
}
const hashes = inputHashes(root);
const manifest = {
  settings: { scale, views: ['iso (from north-east, 30 deg)', 'front (from north)', 'gui (display.gui)'], supersampling: 3 },
  coverage: { states: cov.states.length, items: cov.items.length, models: cov.models, textures: cov.textures.length },
  images: [...images.keys()].sort(),
  errors: allErrors,
  inputs: { combined: combinedHash(hashes), files: hashes },
  approximations: 'directional shading only; no AO, world light, shaders, animation, mipmaps or item extrusion; emissive drawn full-bright',
};
mkdirSync(out, { recursive: true });
writeFileSync(join(out, 'manifest.json'), JSON.stringify(manifest, null, 2) + '\n');
console.log(`rendered ${images.size} images: ${cov.states.length} states, ${cov.items.length} items, ${cov.models.covered}/${cov.models.total} models -> ${out}`);
console.log(`inputs ${manifest.inputs.combined}`);
if (allErrors.length) {
  for (const e of allErrors) console.error(`ERROR ${e}`);
  process.exit(1);
}
