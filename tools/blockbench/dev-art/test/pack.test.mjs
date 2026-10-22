// The dev-art pack: what it contains, that a failed compile or validation keeps the last good
// pack, that publication stays inside its own folder and removes only what it published, and that
// the staged pack is validated before anything reaches the instance.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, mkdtempSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { compileAll } from '../../compile.mjs';
import { devPackSession, publish, stageFiles, validateStage, viewers, packDestination, PACK_NAME, MARKER } from '../pack.mjs';

const root = join(import.meta.dirname, '../../../..');
const compiled = compileAll(root);
const ids = new Set([...compiled.models.keys()].map((rel) => `herbalwitch:${rel.slice(0, -5)}`));
const shown = viewers(root, ids);

function instance() {
  const dir = mkdtempSync(join(tmpdir(), 'hw-inst-'));
  writeFileSync(join(dir, 'options.txt'), 'resourcePacks:["vanilla"]\n');
  mkdirSync(join(dir, 'mods'));
  writeFileSync(join(dir, 'mods/herbalwitch.jar'), 'jar');
  return dir;
}

test('the pack carries the compiled models, owned textures, and the item models and blockstates that show them; nothing else', () => {
  const files = stageFiles(root, compiled, shown);
  const keys = [...files.keys()];
  assert.ok(keys.includes('pack.mcmeta'));
  assert.equal(JSON.parse(files.get('pack.mcmeta')).pack.pack_format, 15);
  for (const k of keys.filter((x) => x !== 'pack.mcmeta')) assert.match(k, /^assets\/herbalwitch\/(models|blockstates|textures)\//, k);
  assert.ok(!keys.some((k) => /\.bbmodel$|piece\.json$|^data\/|\/lang\//.test(k)));
  assert.equal(keys.filter((k) => k.startsWith('assets/herbalwitch/models/block/')).length, 92);
  assert.equal(keys.filter((k) => k.startsWith('assets/herbalwitch/models/item/')).length, 36);
  assert.equal(keys.filter((k) => k.startsWith('assets/herbalwitch/blockstates/')).length, 36);
  const textures = keys.filter((k) => k.startsWith('assets/herbalwitch/textures/'));
  assert.equal(textures.length, compiled.textures.size);
  for (const k of textures) assert.ok(k.startsWith("assets/herbalwitch/textures/block/pieces/"), k);
  assert.deepEqual(validateStage(root, files, ids, shown), []);
});

test('the staged pack is validated before publication: a missing texture or coplanar faces fail', () => {
  const files = stageFiles(root, compiled, shown);
  const kettle = 'assets/herbalwitch/models/block/stations/kettle/base.json';
  const json = JSON.parse(files.get(kettle));
  json.textures.painted = 'herbalwitch:block/pieces/stations/kettle/no_such_texture';
  const broken = new Map(files).set(kettle, Buffer.from(JSON.stringify(json)));
  assert.ok(validateStage(root, broken, ids, shown).some((e) => /missing texture herbalwitch:block.pieces.stations.kettle.no_such_texture/.test(e)));
  const twin = JSON.parse(files.get(kettle));
  twin.elements.push(JSON.parse(JSON.stringify(twin.elements.find((e) => !e.rotation && e.faces.up))));
  twin.elements[twin.elements.length - 1].faces.up.uv = [0, 0, 1, 1];
  const fighting = new Map(files).set(kettle, Buffer.from(JSON.stringify(twin)));
  assert.ok(validateStage(root, fighting, ids, shown).some((e) => /coplanar faces overlap/.test(e)));
});

test('a failed compile or validation keeps the last good pack', () => {
  const inst = instance();
  let mode = 'good';
  const session = devPackSession({ root, instance: inst, compile: () => {
    if (mode === 'throw') throw new Error('stations/kettle: not valid JSON');
    if (mode === 'bad') {
      const models = new Map(compiled.models);
      const p = 'block/stations/kettle/base.json';
      models.set(p, models.get(p).replace('"herbalwitch:block/pieces/stations/kettle/painted"', '"herbalwitch:block/pieces/stations/kettle/gone"'));
      return { ...compiled, models };
    }
    return compiled;
  } });
  const first = session.run();
  assert.ok(first.ok, first.errors.join('\n'));
  const dest = join(inst, 'resourcepacks', PACK_NAME);
  const kettle = join(dest, 'assets/herbalwitch/models/block/stations/kettle/base.json');
  const good = readFileSync(kettle, 'utf8');
  mode = 'throw';
  assert.deepEqual(session.run().errors, ['stations/kettle: not valid JSON']);
  mode = 'bad';
  assert.equal(session.run().ok, false);
  assert.equal(readFileSync(kettle, 'utf8'), good);
  assert.equal(readFileSync(join(inst, 'mods/herbalwitch.jar'), 'utf8'), 'jar');
  assert.equal(readFileSync(join(inst, 'options.txt'), 'utf8'), 'resourcePacks:["vanilla"]\n');
});

test('publication removes only what it published, stays inside its folder and refuses a folder it did not make', () => {
  const inst = instance();
  const dest = packDestination(inst);
  publish(dest, new Map([['pack.mcmeta', Buffer.from('{}')], ['assets/herbalwitch/models/a.json', Buffer.from('a')], ['assets/herbalwitch/models/b/c.json', Buffer.from('c')]]));
  writeFileSync(join(dest, 'notes.txt'), 'mine');
  const second = publish(dest, new Map([['pack.mcmeta', Buffer.from('{}')], ['assets/herbalwitch/models/a.json', Buffer.from('a2')]]));
  assert.deepEqual(second.removed, ['assets/herbalwitch/models/b/c.json']);
  assert.ok(!existsSync(join(dest, 'assets/herbalwitch/models/b')), 'emptied folders go too');
  assert.equal(readFileSync(join(dest, 'notes.txt'), 'utf8'), 'mine');
  assert.throws(() => publish(dest, new Map([['../escape.json', Buffer.from('x')]])), /leaves the pack folder/);
  assert.ok(!existsSync(join(inst, 'resourcepacks/escape.json')));
  // a tampered manifest cannot steer a deletion outside the pack
  const marker = JSON.parse(readFileSync(join(dest, MARKER), 'utf8'));
  marker.files.push('../../options.txt');
  writeFileSync(join(dest, MARKER), JSON.stringify(marker));
  publish(dest, new Map([['pack.mcmeta', Buffer.from('{}')]]));
  assert.ok(existsSync(join(inst, 'options.txt')));
  const foreign = join(inst, 'resourcepacks', 'Other');
  mkdirSync(foreign, { recursive: true });
  assert.throws(() => publish(foreign, new Map()), /refusing to publish/);
  const squatter = join(mkdtempSync(join(tmpdir(), 'hw-sq-')), PACK_NAME);
  mkdirSync(squatter);
  assert.throws(() => publish(squatter, new Map()), /was not created by dev-art/);
});

test('the destination must be a .minecraft folder', () => {
  const empty = mkdtempSync(join(tmpdir(), 'hw-empty-'));
  assert.throws(() => packDestination(empty), /does not look like a \.minecraft folder/);
  assert.throws(() => packDestination(join(empty, 'missing')), /does not exist/);
});
