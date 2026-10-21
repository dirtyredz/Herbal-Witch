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
