// Generates the mod's procedural cuboid models (deterministic, no dependencies).
//
//   node tools/generate-models.mjs [--check]
//
// Writes the herb, herb-bed and hung-bundle models under
// src/main/resources/assets/herbalwitch/models/ from the code in tools/models/, and deletes any
// other JSON in the directories it owns (so a renamed part never lingers); it never touches the
// Blockbench-compiled pieces (tools/compile-blockbench.mjs). Faces buried inside a solid box are
// dropped (they cannot be seen and would z-fight with the faces that can). Every face gets UVs at
// one UV unit per model pixel from its position (or a stretched sprite where a model asks for one),
// so textures run on across neighbouring elements. With --check nothing is written: the command
// fails when a committed model is missing, stale or unexpected. Review the result with
// tools/preview-models.mjs before handing it on.
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { OWNED, proceduralModels } from './models/catalogue.mjs';
import { writeOutputs } from './art/output.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const check = process.argv.includes('--check');
const files = proceduralModels();
const result = writeOutputs(join(root, 'src/main/resources/assets/herbalwitch/models'), files,
  { name: 'generate-models', dirs: OWNED }, { check });
if (check && result.problems.length) {
  for (const p of result.problems) console.error(`ERROR ${p}`);
  process.exit(1);
}
console.log(`${files.size} procedural models: ${result.written.length} written, ${result.removed.length} removed, ${result.unchanged.length} unchanged`);
