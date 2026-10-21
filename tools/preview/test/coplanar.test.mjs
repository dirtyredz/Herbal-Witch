// Z-fighting: the detector's rules on hand-made boxes, then the guard over the mod's own models and
// blockstate combinations (owner feedback 0.2.0-dev.1: flickering coplanar faces on furniture tops).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { bakeElement } from '../geometry/elements.mjs';
import { coplanarOverlaps } from '../geometry/coplanar.mjs';
import { Resources } from '../assets/resources.mjs';
import { Baker } from '../geometry/bake.mjs';
import { zFighting } from '../report/zfighting.mjs';

const ALL = ['down', 'up', 'north', 'south', 'west', 'east'];
const cube = (from, to, extra = {}) => bakeElement({ from, to, faces: Object.fromEntries(ALL.map((f) => [f, { texture: '#t' }])), ...extra }, () => 't');

test('two boxes sharing a top plane over the same area z-fight', () => {
  const quads = [...cube([0, 0, 0], [4, 2, 4]), ...cube([2, 0, 2], [6, 2, 6])];
  const hits = coplanarOverlaps(quads);
  // tops and bottoms overlap over 2 x 2; the touching side faces only meet along an edge
  assert.equal(hits.length, 2);
  assert.ok(hits.every((h) => Math.abs(h.area - 4) < 1e-9));
});

test('faces back to back, side by side or a step apart do not', () => {
  assert.equal(coplanarOverlaps([...cube([0, 0, 0], [4, 2, 4]), ...cube([0, 2, 0], [4, 4, 4])]).length, 0); // stacked
  assert.equal(coplanarOverlaps([...cube([0, 0, 0], [4, 2, 4]), ...cube([4, 0, 0], [8, 2, 4])]).length, 0); // abutting
  assert.equal(coplanarOverlaps([...cube([0, 0, 0], [4, 2, 4]), ...cube([1, 0.1, 1], [3, 2.1, 3])]).length, 0); // offset 0.1
});

test('rotated elements are compared after rotation', () => {
  const rot = { origin: [4, 1, 4], axis: 'y', angle: 45 };
  const quads = [...cube([2, 0, 2], [6, 2, 6], { rotation: rot }), ...cube([3, 0, 3], [5, 2, 5], { rotation: rot })];
  assert.equal(coplanarOverlaps(quads).length, 2); // the same tops and bottoms, turned together
  const crossed = [...cube([2, 0, 2], [6, 2, 6]), ...cube([2, 0, 2], [6, 2, 6], { rotation: rot })];
  assert.equal(coplanarOverlaps(crossed).length, 2); // a turned copy still shares the top and bottom planes
});

test('no model or blockstate combination of the mod has overlapping coplanar faces', () => {
  const root = join(dirname(fileURLToPath(import.meta.url)), '../../..');
  const res = new Resources(root);
  const errors = zFighting(res, new Baker(res));
  assert.deepEqual(errors.slice(0, 20), [], `${errors.length} z-fighting faces; first ones listed`);
});
