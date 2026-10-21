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

test('compiled Blockbench projects are swept too: overlapping authored faces, and a base with its overlay', async () => {
  const { compileAll } = await import('../../blockbench/compile.mjs');
  const { cube, group, projectText, tempRoot, SOLID } = await import('../../blockbench/test/helpers.mjs');
  const piece = (extra) => ({ project: 'p.bbmodel', textures: { clay: 'herbalwitch:block/test/clay', particle: 'herbalwitch:block/test/clay' }, bounds: { min: [-16, 0, 0], max: [16, 32, 16] }, ...extra });
  const sweep = (tree, extra, blockstate) => {
    const tmp = tempRoot({ 'test/p': { piece: piece(extra), text: projectText('p', tree, [{ id: 'clay', png: SOLID }]) } }, { 'block/test/clay.png': SOLID }, ['block/test']);
    const { models } = compileAll(tmp);
    const overlay = new Map([...models].map(([rel, text]) => [`herbalwitch/models/${rel}`, Buffer.from(text)]));
    if (blockstate) overlay.set('herbalwitch/blockstates/test.json', Buffer.from(JSON.stringify(blockstate)));
    const res = new Resources(tmp, { trees: [join(tmp, 'src/main/resources/assets')], overlay });
    return zFighting(res, new Baker(res));
  };
  // two authored cubes whose tops share a plane
  const flat = sweep([cube([0, 0, 0], [8, 4, 8]), cube([4, 0, 4], [12, 4, 12])], { outputs: [{ model: 'block/test/flat', item: true }] });
  assert.ok(flat.some((e) => /herbalwitch:block\/test\/flat: coplanar faces overlap/.test(e)), flat.join('\n'));
  // a clean base and a clean overlay that fight only when the blockstate shows them together
  const tree = [group('body', [cube([0, 0, 0], [8, 4, 8])]), group('fire', [cube([2, 0, 2], [6, 4, 6])])];
  const blockstate = { multipart: [{ apply: { model: 'herbalwitch:block/test/x/part_0' } }, { when: { lit: 'true' }, apply: { model: 'herbalwitch:block/test/x/fire_0' } }] };
  const combo = sweep(tree, { multiblock: { dir: 'block/test/x', cells: [1, 1], overlays: [{ name: 'fire', group: 'fire' }] } }, blockstate);
  assert.ok(combo.some((e) => /herbalwitch:test \[lit=true\]: coplanar faces overlap/.test(e)), combo.join('\n'));
  assert.ok(!combo.some((e) => /herbalwitch:block\/test\/x\/part_0:|herbalwitch:test \[lit=false\]/.test(e)));
});
