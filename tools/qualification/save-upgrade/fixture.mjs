// Prepares an immutable save-upgrade fixture from a historical world (the source is only read).
//
//   node tools/qualification/save-upgrade/fixture.mjs <source world> <out dir> --label <name> --version <mod version>
//
// Keeps every overworld chunk holding a block of the namespace, plus a one-chunk margin, and the
// players' files. Foreign mods are absent from a Herbal Witch-only server, so their content is
// replaced by stable vanilla stand-ins BEFORE the candidate sees it: foreign blocks become stone,
// foreign biomes plains; foreign block entities, scheduled ticks, structure references and chunk
// capabilities are dropped, and foreign items inside our block entities are removed (and counted).
// The receipt records the source hashes, every substitution and the sanitised "before" inventory,
// which is what compare.mjs holds the upgraded world to. Entities are not carried (none of ours are
// stored in chunks before Milestone 8; later ones are covered by the restart harnesses).
import { createHash } from 'node:crypto';
import { mkdirSync, writeFileSync, readFileSync, readdirSync, statSync, existsSync, rmSync } from 'node:fs';
import { join, basename, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { readRegion, writeRegion, regionFiles, forEachBlock } from './anvil.mjs';
import { get, T, readGzip, writeGzip } from './nbt.mjs';
import { inventory } from './inventory.mjs';

const NAMESPACE = 'herbalwitch';
const ours = (id) => id.startsWith(`${NAMESPACE}:`);
const vanilla = (id) => id.startsWith('minecraft:') || !id.includes(':');
const kept = (id) => ours(id) || vanilla(id);

function sha256(file) { return createHash('sha256').update(readFileSync(file)).digest('hex'); }

function walk(dir, out = []) {
  for (const f of readdirSync(dir)) {
    const p = join(dir, f);
    if (statSync(p).isDirectory()) walk(p, out); else out.push(p);
  }
  return out;
}

/** Removes foreign items (anywhere in a compound) and returns how many stacks went. */
function stripForeignItems(tag, counts) {
  if (!tag) return;
  if (tag.t === T.compound) {
    for (const [k, v] of [...tag.v]) {
      if (v.t === T.list && v.v.of === T.compound) {
        v.v.items = v.v.items.filter((item) => {
          const id = item.get('id');
          if (id?.t === T.string && item.has('Count') && !kept(id.v)) { counts.foreignItems = (counts.foreignItems ?? 0) + 1; return false; }
          return true;
        });
      }
      stripForeignItems(v, counts);
    }
  } else if (tag.t === T.list && tag.v.of === T.compound) {
    for (const item of tag.v.items) stripForeignItems({ t: T.compound, v: item }, counts);
  }
}

function sanitizeChunk(doc, counts) {
  const root = doc.root;
  for (const section of get(root, 'sections')?.v.items ?? []) {
    const states = section.get('block_states');
    for (const entry of states?.v.get('palette')?.v.items ?? []) {
      const name = entry.get('Name').v;
      if (kept(name)) continue;
      counts.foreignBlockStates = (counts.foreignBlockStates ?? 0) + 1;
      entry.set('Name', { t: T.string, v: 'minecraft:stone' });
      entry.delete('Properties');
    }
    const biomes = section.get('biomes');
    for (let i = 0; i < (biomes?.v.get('palette')?.v.items.length ?? 0); i++) {
      const items = biomes.v.get('palette').v.items;
      if (!vanilla(items[i])) { items[i] = 'minecraft:plains'; counts.foreignBiomes = (counts.foreignBiomes ?? 0) + 1; }
    }
  }
  const bes = get(root, 'block_entities');
  if (bes) {
    bes.v.items = bes.v.items.filter((be) => {
      const id = be.get('id')?.v ?? '';
      if (!kept(id)) { counts.foreignBlockEntities = (counts.foreignBlockEntities ?? 0) + 1; return false; }
      const caps = be.get('ForgeCaps');
      if (caps) for (const k of [...caps.v.keys()]) if (!ours(k)) { caps.v.delete(k); counts.foreignCapabilities = (counts.foreignCapabilities ?? 0) + 1; }
      stripForeignItems({ t: T.compound, v: be }, counts);
      return true;
    });
  }
  for (const key of ['block_ticks', 'fluid_ticks']) {
    const ticks = get(root, key);
    if (ticks) ticks.v.items = ticks.v.items.filter((t) => kept(t.get('i').v));
  }
  root.v.set('structures', { t: T.compound, v: new Map([['References', { t: T.compound, v: new Map() }], ['starts', { t: T.compound, v: new Map() }]]) });
  const caps = get(root, 'ForgeCaps');
  if (caps) for (const k of [...caps.v.keys()]) if (!ours(k)) caps.v.delete(k);
}

/** Players keep their own data; foreign capabilities and items are dropped as for block entities. */
function sanitizePlayer(file, out, counts) {
  const doc = readGzip(readFileSync(file));
  const caps = doc.root.v.get('ForgeCaps');
  if (caps) for (const k of [...caps.v.keys()]) if (!ours(k)) caps.v.delete(k);
  for (const k of [...doc.root.v.keys()]) if (/[A-Z]?[a-z]+:/.test(k) && !ours(k)) doc.root.v.delete(k);
  stripForeignItems(doc.root, counts);
  writeFileSync(out, writeGzip(doc));
}

export function prepareFixture(source, outDir, { label, version }) {
  if (existsSync(outDir)) rmSync(outDir, { recursive: true });
  const world = join(outDir, 'world');
  mkdirSync(join(world, 'region'), { recursive: true });
  const counts = {};
  const regionDir = join(source, 'region');
  // First pass: which chunks hold our blocks or block entities.
  const wanted = new Set();
  const all = new Map();
  for (const file of regionFiles(regionDir)) {
    for (const chunk of readRegion(file)) {
      const cx = get(chunk.doc.root, 'xPos').v, cz = get(chunk.doc.root, 'zPos').v;
      all.set(`${cx},${cz}`, { file: basename(file), chunk });
      let hit = (get(chunk.doc.root, 'block_entities')?.v.items ?? []).some((be) => ours(be.get('id')?.v ?? ''));
      if (!hit) forEachBlock(chunk.doc, (entry) => ours(entry.get('Name').v), () => { hit = true; });
      if (hit) wanted.add(`${cx},${cz}`);
    }
  }
  const keep = new Set();
  for (const key of wanted) {
    const [cx, cz] = key.split(',').map(Number);
    for (let dx = -1; dx <= 1; dx++) for (let dz = -1; dz <= 1; dz++) if (all.has(`${cx + dx},${cz + dz}`)) keep.add(`${cx + dx},${cz + dz}`);
  }
  const byRegion = new Map();
  const written = new Set();
  for (const key of keep) {
    const { file, chunk } = all.get(key);
    const status = get(chunk.doc.root, 'Status')?.v;
    if (status !== 'minecraft:full' && status !== 'full') { counts.skippedUnfinishedChunks = (counts.skippedUnfinishedChunks ?? 0) + 1; continue; }
    sanitizeChunk(chunk.doc, counts);
    if (!byRegion.has(file)) byRegion.set(file, []);
    byRegion.get(file).push(chunk);
    written.add(key);
  }
  for (const [file, chunks] of byRegion) writeRegion(join(world, 'region', file), chunks);
  const players = join(source, 'playerdata');
  if (existsSync(players)) {
    mkdirSync(join(world, 'playerdata'), { recursive: true });
    for (const f of readdirSync(players)) if (/^[0-9a-f-]{36}\.dat$/.test(f)) sanitizePlayer(join(players, f), join(world, 'playerdata', f), counts);
  }
  const before = inventory(world, NAMESPACE);
  // Unfinished (proto) chunks are not carried: the candidate would finish generating them, so they are not history.
  const chunks = [...written].filter((k) => wanted.has(k)).map((k) => k.split(',').map(Number)).sort((a, b) => a[0] - b[0] || a[1] - b[1]);
  const sourceFiles = {};
  for (const f of walk(source)) {
    const rel = f.slice(source.length + 1).replaceAll('\\', '/');
    if (rel.startsWith('region/') || rel.startsWith('playerdata/') || rel === 'level.dat') sourceFiles[rel] = sha256(f);
  }
  const receipt = {
    label, version, source: resolve(source).replaceAll('\\', '/'), sourceFiles,
    keptChunks: written.size, kept: [...written].map((k) => k.split(',').map(Number)).sort((a, b) => a[0] - b[0] || a[1] - b[1]), chunks, substitutions: counts,
    summary: { blocks: Object.keys(before.blocks).length, blockEntities: Object.keys(before.blockEntities).length, players: Object.keys(before.players).length },
  };
  writeFileSync(join(outDir, 'receipt.json'), JSON.stringify(receipt, null, 1));
  writeFileSync(join(outDir, 'before.json'), JSON.stringify(before));
  // Plain chunk list for the Java side (one "x z" per line, chunks holding our content only).
  writeFileSync(join(outDir, 'chunks.txt'), chunks.map(([x, z]) => `${x} ${z}`).join('\n') + '\n');
  return receipt;
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const [source, out, ...rest] = process.argv.slice(2);
  const opt = (name) => (rest.includes(name) ? rest[rest.indexOf(name) + 1] : undefined);
  if (!source || !out || !opt('--label') || !opt('--version')) {
    console.error('usage: fixture.mjs <source world> <out dir> --label <name> --version <mod version>');
    process.exit(2);
  }
  const receipt = prepareFixture(source, out, { label: opt('--label'), version: opt('--version') });
  console.log(JSON.stringify({ label: receipt.label, keptChunks: receipt.keptChunks, chunks: receipt.chunks.length, substitutions: receipt.substitutions, summary: receipt.summary }, null, 1));
}
