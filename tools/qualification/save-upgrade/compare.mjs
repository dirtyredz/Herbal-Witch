// Judges one save-upgrade run against its fixture.
//
//   node tools/qualification/save-upgrade/compare.mjs <prepared fixture dir> <run dir>
//
// before.json (the sanitised historical world) -> after1/ (the candidate's first save) -> after2/
// (a second candidate process's save) plus the three JVMs' evidence. Rules, in order:
//   1. every historical block is still there with the same id and every saved property value;
//      a property the old version did not have may appear (it takes its default);
//   2. no block of ours appears that the history did not have;
//   3. every historical block entity keeps its id and every saved field, except the reviewed
//      migrations in expectations.json; a block entity created for an old block holds nothing;
//   4. player capabilities keep every saved field;
//   5. after2 equals after1 exactly (a reload changes nothing: migrations are idempotent);
//   6. three distinct JVMs ran, none reported a failure, and every station's first tick conserved items.
import { readFileSync, writeFileSync, existsSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { inventory } from './inventory.mjs';

const here = dirname(fileURLToPath(import.meta.url));
const json = (f) => JSON.parse(readFileSync(f, 'utf8'));

function parseState(s) {
  const m = /^([^[]+)(?:\[(.*)\])?$/.exec(s);
  const props = {};
  for (const pair of (m[2] ?? '').split(',').filter(Boolean)) { const [k, v] = pair.split('='); props[k] = v; }
  return { id: m[1], props };
}

const deepEqual = (a, b) => JSON.stringify(a) === JSON.stringify(b);

const emptyCompound = (v) => v !== null && typeof v === 'object' && !Array.isArray(v) && Object.keys(v).length === 0;

/**
 * Field paths (a.b.c) present in `before` whose value differs or vanished in `after`. An empty compound
 * that is no longer written carries no data (Forge omits an empty ForgeCaps once foreign capabilities
 * are gone), so its absence is not a change.
 */
function changedFields(before, after, prefix = '') {
  const out = [];
  if (before === null || typeof before !== 'object' || Array.isArray(before)) {
    if (!deepEqual(before, after)) out.push(prefix || '(value)');
    return out;
  }
  for (const [k, v] of Object.entries(before)) {
    const path = prefix ? `${prefix}.${k}` : k;
    if (after === null || typeof after !== 'object' || !(k in after)) { if (!emptyCompound(v)) out.push(path); continue; }
    out.push(...changedFields(v, after[k], path));
  }
  return out;
}

function countItems(be) {
  let n = 0;
  const walk = (v) => {
    if (Array.isArray(v)) v.forEach(walk);
    else if (v && typeof v === 'object') {
      if (typeof v.id === 'string' && v.Count !== undefined) n += Number(String(v.Count).replace(/b$/, ''));
      Object.values(v).forEach(walk);
    }
  };
  walk(be);
  return n;
}

export function compare(fixtureDir, runDir, expectations = json(join(here, 'expectations.json'))) {
  const before = json(join(fixtureDir, 'before.json'));
  const receipt = json(join(fixtureDir, 'receipt.json'));
  const after1 = inventory(join(runDir, 'after1'));
  const after2 = inventory(join(runDir, 'after2'));
  const evidence = Object.fromEntries(['upgrade', 'reload', 'behave'].map((p) => [p, existsSync(join(runDir, 'evidence', `${p}.json`)) ? json(join(runDir, 'evidence', `${p}.json`)) : null]));
  const violations = [];
  const notes = { addedProperties: {}, migratedFields: {}, newFields: {}, createdBlockEntities: 0, jobOutcomes: {} };
  const allowed = (id, path) => (expectations.migrations[id] ?? []).some((rule) => path === rule.field || path.startsWith(`${rule.field}.`));

  // 1-2. Blocks.
  for (const [pos, state] of Object.entries(before.blocks)) {
    const now = after1.blocks[pos];
    if (!now) { violations.push(`block lost at ${pos}: ${state}`); continue; }
    const a = parseState(state), b = parseState(now);
    if (a.id !== b.id) { violations.push(`block changed at ${pos}: ${state} -> ${now}`); continue; }
    for (const [k, v] of Object.entries(a.props)) if (b.props[k] !== v) violations.push(`property ${k} at ${pos}: ${v} -> ${b.props[k]} (${a.id})`);
    for (const k of Object.keys(b.props)) if (!(k in a.props)) (notes.addedProperties[`${a.id} ${k}`] ??= new Set()).add(b.props[k]);
  }
  for (const pos of Object.keys(after1.blocks)) if (!(pos in before.blocks)) violations.push(`block appeared at ${pos}: ${after1.blocks[pos]}`);

  // 3. Block entities.
  for (const [pos, be] of Object.entries(before.blockEntities)) {
    const now = after1.blockEntities[pos];
    if (!now) { violations.push(`block entity lost at ${pos}: ${be.id}`); continue; }
    if (now.id !== be.id) { violations.push(`block entity id changed at ${pos}: ${be.id} -> ${now.id}`); continue; }
    for (const path of changedFields(be, now)) {
      if (allowed(be.id, path)) (notes.migratedFields[`${be.id} ${path}`] ??= 0), notes.migratedFields[`${be.id} ${path}`]++;
      else violations.push(`block entity field ${path} changed at ${pos} (${be.id}): ${JSON.stringify(be[path.split('.')[0]])} -> ${JSON.stringify(now[path.split('.')[0]])}`);
    }
    for (const k of Object.keys(now)) if (!(k in be)) (notes.newFields[`${be.id} ${k}`] ??= 0), notes.newFields[`${be.id} ${k}`]++;
    if (countItems(now) !== countItems(be)) violations.push(`stored item count changed at ${pos} (${be.id}): ${countItems(be)} -> ${countItems(now)}`);
  }
  for (const [pos, be] of Object.entries(after1.blockEntities)) {
    if (pos in before.blockEntities) continue;
    notes.createdBlockEntities++;
    if (countItems(be) !== 0) violations.push(`created block entity at ${pos} (${be.id}) holds ${countItems(be)} items`);
    const rule = (expectations.createdBlockEntities ?? {})[be.id];
    const block = parseState(after1.blocks[pos] ?? 'none').id;
    if (!rule || !rule.blocks.includes(block)) violations.push(`unexpected new block entity at ${pos}: ${be.id} on ${block}`);
  }

  // 4. Players.
  for (const [uuid, caps] of Object.entries(before.players)) {
    const now = after1.players[uuid];
    if (!now) { violations.push(`player ${uuid} lost`); continue; }
    for (const [cap, value] of Object.entries(caps)) for (const path of changedFields(value, now[cap] ?? null)) violations.push(`player ${uuid} ${cap} field ${path} changed`);
  }

  // 5. Idempotence.
  if (!deepEqual(after1.blocks, after2.blocks)) violations.push('a second load changed block states');
  for (const [pos, be] of Object.entries(after1.blockEntities)) if (!deepEqual(be, after2.blockEntities[pos])) violations.push(`a second load changed the block entity at ${pos} (${be.id})`);
  if (Object.keys(after2.blockEntities).length !== Object.keys(after1.blockEntities).length) violations.push('a second load changed the block entity count');
  if (!deepEqual(after1.players, after2.players)) violations.push('a second load changed player capabilities');

  // 6. Evidence.
  const pids = new Set();
  for (const [phase, e] of Object.entries(evidence)) {
    if (!e) { violations.push(`missing ${phase} evidence`); continue; }
    pids.add(e.process);
    for (const f of e.failures) violations.push(`${phase}: ${f}`);
    if (e.chunks !== receipt.chunks.length) violations.push(`${phase} loaded ${e.chunks} of ${receipt.chunks.length} chunks`);
  }
  if (pids.size !== 3) violations.push(`expected three distinct JVMs, saw ${pids.size}`);
  for (const job of evidence.behave?.jobs ?? []) notes.jobOutcomes[`${job.outcome} (${job.recipeNow})`] = (notes.jobOutcomes[`${job.outcome} (${job.recipeNow})`] ?? 0) + 1;
  for (const p of evidence.upgrade?.players ?? []) {
    const caps = before.players[p.uuid] ?? {};
    const status = caps['herbalwitch:witch/status'];
    if (status && (status.initiated === '1b') !== p.witch) violations.push(`player ${p.uuid} witch flag decoded as ${p.witch}`);
    const keys = caps['herbalwitch:grimoire/discoveries']?.keys;
    if (keys && keys.length !== p.discoveries) violations.push(`player ${p.uuid} decoded ${p.discoveries} of ${keys.length} discoveries`);
  }

  for (const k of Object.keys(notes.addedProperties)) notes.addedProperties[k] = [...notes.addedProperties[k]];
  return {
    fixture: receipt.label, version: receipt.version, pass: violations.length === 0,
    counts: { blocks: Object.keys(before.blocks).length, blockEntities: Object.keys(before.blockEntities).length, players: Object.keys(before.players).length, chunks: receipt.chunks.length, jvms: pids.size },
    notes, violations,
  };
}

if (process.argv[1] && fileURLToPath(import.meta.url) === (await import('node:path')).resolve(process.argv[1])) {
  const [fixtureDir, runDir] = process.argv.slice(2);
  const result = compare(fixtureDir, runDir);
  writeFileSync(join(runDir, 'compare.json'), JSON.stringify(result, null, 1));
  console.log(JSON.stringify({ ...result, violations: result.violations.slice(0, 40), violationCount: result.violations.length }, null, 1));
  if (!result.pass) process.exit(1);
  console.log(`PASS save upgrade ${result.fixture} (${result.version}): ${result.counts.blocks} blocks, ${result.counts.blockEntities} block entities, ${result.counts.players} players across ${result.counts.jvms} JVMs`);
}
