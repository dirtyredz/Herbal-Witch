// The two model producers and the texture producer own disjoint paths: neither can overwrite or
// delete the other's assets, an undeclared output fails, and the checked-in trees agree with
// both producers (and never collide with the datagen tree).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { existsSync, mkdtempSync, mkdirSync, readFileSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { compileAll } from '../compile.mjs';
import { OWNED, proceduralModels } from '../../models/catalogue.mjs';
import { textureManifest } from '../../art/catalogue.mjs';
import { writeOutputs, listFiles } from '../../art/output.mjs';

const root = join(import.meta.dirname, '../../..');
const inside = (p, d) => p.startsWith(`${d}/`);

test('procedural models, Blockbench models and both texture sets are disjoint and each inside its owner', () => {
  const blockbench = compileAll(root);
  const procedural = proceduralModels();
  for (const p of procedural.keys()) {
    assert.ok(OWNED.some((d) => inside(p, d)), `procedural ${p} outside its directories`);
    assert.ok(!blockbench.catalogue.modelDirs.some((d) => inside(p, d)), `procedural ${p} inside a Blockbench directory`);
  }
  for (const p of blockbench.models.keys()) {
    assert.ok(!procedural.has(p), `${p} written by both producers`);
    assert.ok(!OWNED.some((d) => inside(p, d)), `Blockbench ${p} inside a procedural directory`);
  }
  for (const a of OWNED) for (const b of blockbench.catalogue.modelDirs) assert.ok(!inside(a, b) && !inside(b, a) && a !== b, `${a} / ${b} overlap`);
  const generated = new Set(textureManifest().map(([rel]) => rel));
  for (const rel of blockbench.textures.keys()) assert.ok(!generated.has(rel), `texture ${rel} has two producers`);
});

test('the committed trees match both producers, and nothing they write is also in the datagen tree', () => {
  const models = join(root, 'src/main/resources/assets/herbalwitch/models');
  const check = (files, owner) => writeOutputs(models, files, owner, { check: true }).problems;
  assert.deepEqual(check(proceduralModels(), { name: 'procedural', dirs: OWNED }), []);
  const bb = compileAll(root);
  assert.deepEqual(check(bb.models, { name: 'blockbench', dirs: bb.catalogue.modelDirs }), []);
  for (const p of [...bb.models.keys(), ...proceduralModels().keys()]) {
    assert.ok(!existsSync(join(root, 'src/generated/resources/assets/herbalwitch/models', p)), `${p} also in src/generated`);
  }
});

test('a producer cannot write outside its paths, and its cleanup never touches another producer', () => {
  const dir = mkdtempSync(join(tmpdir(), 'hw-own-'));
  const put = (rel, text) => {
    mkdirSync(join(dir, rel, '..'), { recursive: true });
    writeFileSync(join(dir, rel), text);
  };
  const a = { name: 'a', dirs: ['block/a'] };
  const b = { name: 'b', dirs: ['block/b'], files: ['textures/shared/b.png'], ext: '.json' };
  writeOutputs(dir, new Map([['block/a/one.json', '1']]), a, { log: () => {} });
  writeOutputs(dir, new Map([['block/b/two.json', '2']]), b, { log: () => {} });
  put('block/a/stale.json', 'old');
  put('block/elsewhere/keep.json', 'keep');
  assert.throws(() => writeOutputs(dir, new Map([['block/b/two.json', 'x']]), a), /a: block\/b\/two.json is outside the paths this producer owns/);
  assert.throws(() => writeOutputs(dir, new Map([['block/a/../b/two.json', 'x']]), a), /bad output path/);
  const run = writeOutputs(dir, new Map([['block/a/one.json', '1']]), a, { log: () => {} });
  assert.deepEqual(run.removed, ['block/a/stale.json']);
  assert.equal(readFileSync(join(dir, 'block/b/two.json'), 'utf8'), '2');
  assert.ok(existsSync(join(dir, 'block/elsewhere/keep.json')));
  assert.deepEqual(listFiles(dir, 'block').sort(), ['block/a/one.json', 'block/b/two.json', 'block/elsewhere/keep.json']);
});

test('check mode reports missing, stale and unexpected files without writing', () => {
  const dir = mkdtempSync(join(tmpdir(), 'hw-chk-'));
  const owner = { name: 'a', dirs: ['m'] };
  mkdirSync(join(dir, 'm'), { recursive: true });
  writeFileSync(join(dir, 'm/old.json'), 'x');
  writeFileSync(join(dir, 'm/changed.json'), 'before');
  const { problems } = writeOutputs(dir, new Map([['m/new.json', 'n'], ['m/changed.json', 'after']]), owner, { check: true });
  assert.deepEqual(problems.sort(), ['missing m/new.json', 'stale m/changed.json', 'unexpected m/old.json']);
  assert.equal(readFileSync(join(dir, 'm/changed.json'), 'utf8'), 'before');
});
