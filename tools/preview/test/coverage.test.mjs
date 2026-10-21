// The real mod: every blockstate state and item model resolves with real textures, every model
// file is rendered (abstract templates through a descendant), every review scene builds from real
// blocks, and rendering twice gives identical pixels.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { Resources } from '../assets/resources.mjs';
import { Baker } from '../geometry/bake.mjs';
import { coverage } from '../report/coverage.mjs';
import { SCENES } from '../scenes/catalogue.mjs';
import { renderScene, renderItem } from '../report/views.mjs';

const root = join(dirname(fileURLToPath(import.meta.url)), '../../..');

test('every state, item and model renders with real textures', () => {
  const res = new Resources(root);
  const cov = coverage(res, new Baker(res));
  assert.deepEqual(cov.errors, []);
  assert.equal(cov.models.covered, cov.models.total);
  assert.ok(cov.states.length > 100 && cov.items.length > 50);
});

test('every review scene builds from real blocks without missing textures', () => {
  const res = new Resources(root);
  const baker = new Baker(res);
  for (const scene of SCENES) {
    const shot = renderScene(res, baker, scene.entries, scene.view, 1);
    assert.deepEqual([...shot.missing], [], scene.name);
  }
});

test('rendering is deterministic', () => {
  const a = new Resources(root);
  const b = new Resources(root);
  const id = 'herbalwitch:item/stations/kettle/station';
  assert.deepEqual(renderItem(a, new Baker(a), id, 48).image.px, renderItem(b, new Baker(b), id, 48).image.px);
});

test('explicit staged roots: a pack laid over the mod resolves, and a path in two trees is a conflict', async () => {
  const { mkdtempSync, mkdirSync, writeFileSync, readFileSync } = await import('node:fs');
  const { tmpdir } = await import('node:os');
  const stage = mkdtempSync(join(tmpdir(), 'hw-stage-'));
  const rel = 'herbalwitch/models/block/stations/kettle/base.json';
  mkdirSync(join(stage, dirname(rel)), { recursive: true });
  writeFileSync(join(stage, rel), readFileSync(join(root, 'src/main/resources/assets', rel)));
  const trees = [stage, join(root, 'src/generated/resources/assets')];
  const staged = new Resources(root, { trees });
  const scope = { blocks: new Set(['herbalwitch:stations/kettle/station']), items: new Set(['herbalwitch:item/stations/kettle/station']), models: new Set(['herbalwitch:block/stations/kettle/base']) };
  const ok = coverage(staged, new Baker(staged), scope);
  assert.deepEqual(ok.errors.filter((e) => !/missing texture/.test(e)), []); // textures live in the third tree, left out here
  const both = new Resources(root, { trees: [stage, ...trees.slice(1), join(root, 'src/main/resources/assets')] });
  const dup = coverage(both, new Baker(both), scope);
  assert.ok(dup.errors.some((e) => /conflict: herbalwitch:models\/block\/stations\/kettle\/base.json exists in more than one resource tree/.test(e)), dup.errors.join('\n'));
});

test('an in-memory overlay wins over the trees, as a resource pack wins over the mod', () => {
  const res = new Resources(root, { overlay: new Map([['herbalwitch/models/block/stations/kettle/base.json', Buffer.from('{"elements":[]}')]]) });
  assert.deepEqual(res.json('models', 'herbalwitch:block/stations/kettle/base'), { elements: [] });
  assert.equal(res.diagnostics.length, 0);
});
