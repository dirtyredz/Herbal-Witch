// Compiles the Blockbench projects under src/art/blockbench/ into the mod's model JSON and the
// textures they own (deterministic, no dependencies; Blockbench itself is never run).
//
//   node tools/compile-blockbench.mjs                     compile and write (only changed files)
//   node tools/compile-blockbench.mjs --check             compile in memory; fail if committed output is stale
//   node tools/compile-blockbench.mjs --dev-pack "<trial .minecraft>" [--watch]
//                                                         publish the live dev-art resource pack into that
//                                                         instance (once, or on every save); writes nothing
//                                                         in the repository (docs/BLOCKBENCH.md)
//   node tools/compile-blockbench.mjs --refresh-previews  re-embed shared textures in the .bbmodel files
//                                                         after an intentional palette change
//   node tools/compile-blockbench.mjs --compare <dir> [--texture-map <json>]
//                                                         appearance parity of the current trees against a
//                                                         captured baseline (<dir>/main-assets and
//                                                         <dir>/generated-assets); the map names the baseline
//                                                         texture each renamed texture was copied from; textures
//                                                         compare at the game's sampling (an exact integer
//                                                         nearest-neighbour enlargement passes); writes nothing
//
// Writes only into the model directories src/art/blockbench/catalog.json declares and the owned
// texture files the projects declare, and removes stale JSON only in those directories. Run
// node tools/generate-textures.mjs first: shared texture previews are checked against its PNGs.
import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { compileAll, canonicalTextures, TEXTURE_ROOT } from './blockbench/compile.mjs';
import { loadPieces, SOURCE_DIR } from './blockbench/catalogue.mjs';
import { refreshPreviews } from './blockbench/textures.mjs';
import { compileJson } from './blockbench/project.mjs';
import { compareAppearance } from './blockbench/migration/compare.mjs';
import { devPackSession } from './blockbench/dev-art/pack.mjs';
import { watchSources } from './blockbench/dev-art/watch.mjs';
import { writeOutputs } from './art/output.mjs';
import { Resources } from './preview/assets/resources.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const assets = join(root, 'src/main/resources/assets/herbalwitch');
const args = process.argv.slice(2);
const arg = (name) => {
  const i = args.indexOf(name);
  return i === -1 ? null : args[i + 1];
};

function fail(lines) {
  for (const l of lines) console.error(`ERROR ${l}`);
  process.exit(1);
}

/** One dev-pack publication (never throws). */
function devPack(session) {
  const started = Date.now();
  try {
    return { ...session.run(), ms: Date.now() - started };
  } catch (e) {
    return { ok: false, errors: [e.message] };
  }
}

/** Reports a publication for the owner. */
function report(result) {
  const time = new Date().toLocaleTimeString();
  if (result.ok) {
    console.log(`[${time}] published ${result.dest} (${result.written.length} changed, ${result.removed.length} removed, ${result.ms} ms): ready for F3+T`);
  } else {
    console.error(`[${time}] NOT published; the pack in game is unchanged:`);
    for (const e of result.errors.slice(0, 20)) console.error(`  ${e}`);
    if (result.errors.length > 20) console.error(`  ... ${result.errors.length - 20} more`);
  }
  return result;
}

if (args.includes('--dev-pack')) {
  const instance = arg('--dev-pack');
  if (!instance || instance.startsWith('--')) fail(['--dev-pack needs the trial instance\'s .minecraft folder']);
  const session = devPackSession({ root, instance });
  const first = report(devPack(session));
  if (args.includes('--watch')) {
    watchSources(join(root, SOURCE_DIR), () => devPack(session), report);
    console.log(`watching ${SOURCE_DIR} for saves (Ctrl+C to stop)`);
  } else if (!first.ok) {
    process.exit(1);
  }
} else if (args.includes('--refresh-previews')) {
  const canonical = canonicalTextures(join(root, TEXTURE_ROOT));
  for (const { dir, piece, projectFile } of loadPieces(root)) {
    const { json, changed } = refreshPreviews(JSON.parse(readFileSync(projectFile, 'utf8')), piece, canonical.bytes);
    if (changed.length) {
      writeFileSync(projectFile, compileJson(json));
      console.log(`${dir}: refreshed ${changed.join(', ')}`);
    }
  }
} else if (arg('--compare')) {
  const dir = arg('--compare');
  const base = new Resources(root, { trees: [join(dir, 'generated-assets'), join(dir, 'main-assets')] });
  const { checked, differences } = compareAppearance(base, new Resources(root), arg('--texture-map') ? JSON.parse(readFileSync(arg('--texture-map'), 'utf8')) : {});
  console.log(`compared ${checked.models} models (${checked.items} items), ${checked.states} block states, ${checked.textures} textures`
    + ` (${checked.resampled} at a new resolution, identical at the game's sampling)`);
  if (differences.length) fail(differences);
  console.log('appearance identical');
} else {
  const check = args.includes('--check');
  let compiled;
  try {
    compiled = compileAll(root);
  } catch (e) {
    fail([e.message]);
  }
  const { models, textures, catalogue, pieces } = compiled;
  const m = writeOutputs(join(assets, 'models'), models, { name: 'compile-blockbench', dirs: catalogue.modelDirs }, { check });
  const t = writeOutputs(join(assets, 'textures'), textures, { name: 'compile-blockbench', dirs: catalogue.ownedTextureDir ? [catalogue.ownedTextureDir] : [], files: pieces.flatMap((p) => Object.values(p.piece.owned_textures).map((id) => `${id.split(':')[1]}.png`)), ext: '.png' }, { check });
  if (check && (m.problems.length || t.problems.length)) fail([...m.problems, ...t.problems, 'run node tools/compile-blockbench.mjs']);
  console.log(`${pieces.length} projects -> ${models.size} models, ${textures.size} textures: `
    + `${m.written.length + t.written.length} written, ${m.removed.length} removed, ${m.unchanged.length + t.unchanged.length} unchanged`);
}
