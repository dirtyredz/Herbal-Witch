// Generates the mod's cuboid models (deterministic, no dependencies).
//
//   node tools/generate-models.mjs
//
// Writes the station, furniture, herb and herb-bed models under
// src/main/resources/assets/herbalwitch/models/ from the code in tools/models/, and deletes any
// other JSON in the directories it owns (so a renamed part never lingers). Every face gets UVs at
// one UV unit per model pixel from its position (or a stretched sprite where a model asks for one),
// so textures run on across neighbouring elements and across a multiblock's cells. Review the
// result with tools/preview-models.mjs before handing it on.
import { writeFileSync, mkdirSync, readdirSync, rmSync, existsSync, statSync } from 'node:fs';
import { dirname, join, relative, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { model } from './models/geometry.mjs';
import { needsCutout } from './models/textures.mjs';
import { MODELS, OWNED } from './models/catalogue.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const modelsRoot = join(root, 'src/main/resources/assets/herbalwitch/models');

const written = new Set();
for (const m of MODELS()) {
  const file = join(modelsRoot, `${m.path}.json`);
  if (written.has(file)) throw new Error(`two models write ${m.path}`);
  const json = model({ textures: m.textures, elements: m.elements, cutout: m.cutout ?? needsCutout(m.elements), display: m.display, ao: m.ao ?? true, open: m.open });
  mkdirSync(dirname(file), { recursive: true });
  writeFileSync(file, JSON.stringify(json, null, 2) + '\n');
  written.add(file);
  console.log(`wrote ${m.path}.json (${json.elements.length} elements)`);
}

function walk(dir) {
  if (!existsSync(dir)) return [];
  return readdirSync(dir).flatMap((n) => {
    const f = join(dir, n);
    return statSync(f).isDirectory() ? walk(f) : [f];
  });
}
for (const dir of OWNED) {
  for (const f of walk(join(modelsRoot, dir))) {
    if (f.endsWith('.json') && !written.has(f)) {
      rmSync(f);
      console.log(`removed ${relative(modelsRoot, f).split(sep).join('/')}`);
    }
  }
}
