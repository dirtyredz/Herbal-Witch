// One-off migration (0.2.0-dev.8): every texture a Blockbench piece uses becomes owned by that
// piece, at textures/block/pieces/<project dir>/<texture variable>.png, starting as a pixel-identical
// copy of the shared material it replaces. Rewrites piece.json (bindings, owned_textures, particle)
// and the texture records in the .bbmodel, and writes the old-id map the appearance comparison uses
// (`node tools/compile-blockbench.mjs --compare <dir> --texture-map <map.json>`).
//
//   node tools/blockbench/migration/own-textures.mjs <alias-map.json>
//
// Kept as the record of how the owned paths were derived; it is not part of any build.
import { readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { loadCatalogue, isSharedTexture, ownedTextureId, SOURCE_DIR } from '../catalogue.mjs';
import { compileJson } from '../project.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '../../..');
const SHARED = isSharedTexture;
const alias = {};

const catalogue = loadCatalogue(root);
for (const dir of catalogue.projects) {
  const pieceFile = join(root, SOURCE_DIR, dir, 'piece.json');
  const piece = JSON.parse(readFileSync(pieceFile, 'utf8'));
  const old = { ...piece.textures };
  const owned = {};
  const next = {};
  for (const [v, id] of Object.entries(old)) {
    if (v === 'particle' || id.startsWith('#') || SHARED(id)) continue;
    next[v] = ownedTextureId(catalogue.ownedTextureDir, dir, v);
    owned[v] = next[v];
    alias[next[v]] = id;
  }
  const particle = old.particle;
  const particleVar = particle.startsWith('#') || SHARED(particle) ? null : Object.keys(next).find((v) => old[v] === particle);
  if (!particle.startsWith('#') && !SHARED(particle) && !particleVar) {
    // bound to a material the piece does not otherwise use: fall back to the piece's first owned texture
    console.log(`${dir}: particle ${particle} is not a piece texture; using ${Object.keys(next)[0]}`);
  }
  const textures = {};
  for (const [v, id] of Object.entries(old)) {
    if (v === 'particle') textures.particle = particle.startsWith('#') || SHARED(particle) ? particle : next[particleVar ?? Object.keys(next)[0]];
    else textures[v] = next[v] ?? id;
  }
  const out = { ...piece, textures, owned_textures: owned };
  // keep key order: project, textures, owned_textures, rest
  const ordered = { project: out.project, textures: out.textures, owned_textures: out.owned_textures };
  for (const [k, v] of Object.entries(out)) if (!(k in ordered)) ordered[k] = v;
  writeFileSync(pieceFile, `${JSON.stringify(ordered, null, 2).replace(/\[\n\s+(-?[\d.]+(?:,\n\s+-?[\d.]+)*)\n\s+\]/g, (m, body) => `[${body.replace(/,\n\s+/g, ', ')}]`)}\n`);

  const projectFile = join(root, SOURCE_DIR, dir, piece.project);
  const json = JSON.parse(readFileSync(projectFile, 'utf8'));
  for (const t of json.textures) {
    if (!next[t.id]) continue;
    t.name = `${t.id}.png`;
    t.folder = `${catalogue.ownedTextureDir}/${dir}`;
    t.namespace = 'herbalwitch';
  }
  writeFileSync(projectFile, compileJson(json));
}
writeFileSync(process.argv[2], `${JSON.stringify(alias, null, 2)}\n`);
console.log(`${Object.keys(alias).length} owned textures`);
