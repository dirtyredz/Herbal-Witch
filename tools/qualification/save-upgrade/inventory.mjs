// Inventory of one namespace's blocks, block entities, entities and player capabilities in a saved
// world (read only).
//
//   node tools/qualification/save-upgrade/inventory.mjs <world dir> [--namespace herbalwitch] [--json <out>]
//
// The result is one side of a save-upgrade comparison: every matching block with its full state,
// every block entity (plain NBT) and every entity, keyed by dimension and position or UUID.
import { writeFileSync, existsSync, readdirSync, readFileSync } from 'node:fs';
import { join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { readRegion, regionFiles, forEachBlock, stateString } from './anvil.mjs';
import { get, plain, readGzip } from './nbt.mjs';

const DIMENSIONS = { 'minecraft:overworld': '', 'minecraft:the_nether': 'DIM-1', 'minecraft:the_end': 'DIM1' };

export function inventory(worldDir, namespace = 'herbalwitch') {
  const prefix = `${namespace}:`;
  const result = { namespace, blocks: {}, blockEntities: {}, entities: {}, players: {}, chunks: 0, dataVersions: {} };
  for (const [dimension, sub] of Object.entries(DIMENSIONS)) {
    const base = sub ? join(worldDir, sub) : worldDir;
    for (const file of regionFiles(join(base, 'region'))) {
      for (const chunk of readRegion(file)) {
        result.chunks++;
        const dv = get(chunk.doc.root, 'DataVersion')?.v;
        result.dataVersions[dv] = (result.dataVersions[dv] ?? 0) + 1;
        forEachBlock(chunk.doc, (entry) => entry.get('Name').v.startsWith(prefix), (entry, x, y, z) => {
          result.blocks[`${dimension} ${x} ${y} ${z}`] = stateString(entry);
        });
        for (const be of get(chunk.doc.root, 'block_entities')?.v.items ?? []) {
          const id = be.get('id')?.v ?? '';
          if (!id.startsWith(prefix)) continue;
          result.blockEntities[`${dimension} ${be.get('x').v} ${be.get('y').v} ${be.get('z').v}`] = plain({ t: 10, v: be });
        }
      }
    }
    for (const file of regionFiles(join(base, 'entities'))) {
      for (const chunk of readRegion(file)) {
        for (const entity of get(chunk.doc.root, 'Entities')?.v.items ?? []) {
          const id = entity.get('id')?.v ?? '';
          const caps = entity.get('ForgeCaps');
          const touched = id.startsWith(prefix) || (caps && [...caps.v.keys()].some((k) => k.startsWith(prefix)));
          if (touched) result.entities[`${dimension} ${Array.from(entity.get('UUID').v).join(',')}`] = plain({ t: 10, v: entity });
        }
      }
    }
  }
  const players = join(worldDir, 'playerdata');
  if (existsSync(players)) {
    for (const f of readdirSync(players)) {
      if (!/^[0-9a-f-]{36}\.dat$/.test(f)) continue;
      const caps = readGzip(readFileSync(join(players, f))).root.v.get('ForgeCaps');
      const mine = {};
      for (const [k, v] of caps?.v ?? []) if (k.startsWith(prefix)) mine[k] = plain(v);
      result.players[f.slice(0, -4)] = mine;
    }
  }
  return result;
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const args = process.argv.slice(2);
  const opt = (name, fallback) => (args.includes(name) ? args[args.indexOf(name) + 1] : fallback);
  const inv = inventory(args[0], opt('--namespace', 'herbalwitch'));
  const counts = {};
  for (const s of Object.values(inv.blocks)) { const id = s.replace(/\[.*$/, ''); counts[id] = (counts[id] ?? 0) + 1; }
  console.log(`chunks ${inv.chunks}, data versions ${JSON.stringify(inv.dataVersions)}`);
  console.log(`blocks ${Object.keys(inv.blocks).length}, block entities ${Object.keys(inv.blockEntities).length}, entities ${Object.keys(inv.entities).length}, players ${Object.keys(inv.players).length}`);
  for (const [id, n] of Object.entries(counts).sort()) console.log(`  ${n}\t${id}`);
  const out = opt('--json');
  if (out) writeFileSync(out, JSON.stringify(inv, null, 1));
}
