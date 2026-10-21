// Geometry and resolution semantics: default UVs, element and blockstate rotations, rescale,
// inheritance, alias cycles, unsupported parents, multipart evaluation and the rasterizer's depth,
// cutout and determinism.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { bakeElement, defaultUv, rotateAbout } from '../geometry/elements.mjs';
import { variantRotation, displayTransform } from '../geometry/transforms.mjs';
import { resolveModel, resolveTexture } from '../assets/models.mjs';
import { select, enumerate } from '../scenes/blockstates.mjs';
import { render } from '../render/rasterizer.mjs';
import { VIEWS } from '../render/camera.mjs';

const near = (a, b, eps = 1e-9) => a.every((v, i) => Math.abs(v - b[i]) < eps);

test('default UVs follow vanilla FaceBakery', () => {
  assert.deepEqual(defaultUv('north', [2, 3, 4], [10, 12, 14]), [6, 4, 14, 13]);
  assert.deepEqual(defaultUv('up', [2, 3, 4], [10, 12, 14]), [2, 4, 10, 14]);
  assert.deepEqual(defaultUv('east', [2, 3, 4], [10, 12, 14]), [2, 4, 12, 13]);
});

test('asymmetric UVs land on the right corners', () => {
  const [q] = bakeElement({ from: [0, 0, 0], to: [16, 16, 0], faces: { north: { uv: [0, 0, 4, 8], texture: '#t' } } }, () => 't');
  // north face: u runs east to west, so the top-east corner carries u=0, v=0
  const i = q.pos.findIndex((p) => near(p, [16, 16, 0]));
  assert.deepEqual(q.uv[i], [0, 0]);
  const j = q.pos.findIndex((p) => near(p, [0, 0, 0]));
  assert.deepEqual(q.uv[j], [4, 8]);
});

test('element rotation is right-handed about its origin, rescale widens', () => {
  assert.ok(near(rotateAbout([16, 8, 8], { origin: [8, 8, 8], axis: 'y', angle: 90 }), [8, 8, 0]));
  const r = rotateAbout([16, 8, 8], { origin: [8, 8, 8], axis: 'y', angle: 45, rescale: true });
  assert.ok(Math.abs(Math.hypot(r[0] - 8, r[2] - 8) - 8 / Math.cos(Math.PI / 4)) < 1e-9);
});

test('blockstate y=90 turns a north-facing front to the east', () => {
  const quads = bakeElement({ from: [0, 0, 0], to: [16, 16, 1], faces: { north: { texture: '#t' } } }, () => 't');
  const [east] = variantRotation(quads, { y: 90 });
  assert.ok(near(east.normal, [1, 0, 0]));
  assert.throws(() => variantRotation(quads, { y: 90, uvlock: true }), /uvlock/);
});

test('display scale shrinks about the centre', () => {
  const quads = bakeElement({ from: [0, 0, 0], to: [16, 16, 16], faces: { up: { texture: '#t' } } }, () => 't');
  const [up] = displayTransform(quads, { scale: [0.5, 0.5, 0.5] });
  assert.ok(up.pos.every((p) => p.every((v) => v >= 4 - 1e-9 && v <= 12 + 1e-9)));
});

const fakeRes = (models) => ({ json: (kind, id) => models[id] ?? null });

test('inheritance: nearest elements, merged textures, child wins', () => {
  const res = fakeRes({
    'herbalwitch:a': { parent: 'herbalwitch:b', textures: { side: 'herbalwitch:block/x' } },
    'herbalwitch:b': { textures: { side: 'herbalwitch:block/y', all: '#side' }, elements: [{ from: [0, 0, 0], to: [1, 1, 1], faces: {} }] },
  });
  const m = resolveModel(res, 'herbalwitch:a');
  assert.equal(m.elements.length, 1);
  assert.equal(resolveTexture(m, '#all'), 'herbalwitch:block/x');
});

test('cycles, missing parents and unsupported vanilla parents fail', () => {
  assert.throws(() => resolveModel(fakeRes({ 'herbalwitch:a': { parent: 'herbalwitch:a' } }), 'herbalwitch:a'), /cycle/);
  assert.throws(() => resolveModel(fakeRes({}), 'herbalwitch:nope'), /missing model/);
  assert.throws(() => resolveModel(fakeRes({ 'herbalwitch:a': { parent: 'block/cube_all' } }), 'herbalwitch:a'), /unsupported vanilla parent/);
  const m = resolveModel(fakeRes({ 'herbalwitch:a': { textures: { x: '#y', y: '#x' } } }), 'herbalwitch:a');
  assert.throws(() => resolveTexture(m, '#x'), /alias cycle/);
});

test('vanilla cross resolves with its two rotated planes', () => {
  const m = resolveModel(fakeRes({ 'herbalwitch:p': { parent: 'minecraft:block/cross', textures: { cross: 'herbalwitch:block/p' } } }), 'herbalwitch:p');
  assert.equal(m.elements.length, 2);
  assert.equal(m.ao, false);
  assert.equal(resolveTexture(m, '#particle'), 'herbalwitch:block/p');
});

test('variants match exactly; multipart applies every true condition', () => {
  const variants = { variants: { 'age=0': { model: 'a' }, 'age=1': { model: 'b' } } };
  assert.equal(select(variants, { age: '1' })[0].model, 'b');
  const multipart = { multipart: [{ apply: { model: 'core' } }, { when: { north: 'false' }, apply: { model: 'wall' } },
    { when: { OR: [{ east: 'true' }, { west: 'true' }] }, apply: { model: 'joined' } }] };
  assert.deepEqual(select(multipart, { north: 'false', east: 'false', west: 'true' }).map((p) => p.model), ['core', 'wall', 'joined']);
  assert.equal(enumerate(multipart).length, 8);
});

test('rasterizer: depth order, cutout holes and identical output on repeat', () => {
  const solid = { width: 1, height: 1, rgba: new Uint8Array([200, 0, 0, 255]) };
  const hole = { width: 2, height: 1, rgba: new Uint8Array([0, 0, 200, 255, 0, 0, 0, 0]) };
  const textures = (id) => (id === 'solid' ? solid : hole);
  const back = bakeElement({ from: [0, 0, 8], to: [16, 16, 8], faces: { north: { uv: [0, 0, 16, 16], texture: '#a' } } }, () => 'solid');
  const front = bakeElement({ from: [0, 0, 4], to: [16, 16, 4], faces: { north: { uv: [0, 0, 16, 16], texture: '#b' } } }, () => 'cut');
  const frame = { minX: -16, minY: -16, maxX: 0, maxY: 0 };
  const a = render([...back, ...front], VIEWS.front, textures, { scale: 1, frame, ss: 1 });
  const b = render([...front, ...back], VIEWS.front, textures, { scale: 1, frame, ss: 1 });
  assert.deepEqual(a.image.px, b.image.px);
  // front view: screen x runs west; u 0..8 (left half of the texture) is the blue half, drawn on the east side
  const px = (x, y) => [...a.image.px.subarray((y * 16 + x) * 4, (y * 16 + x) * 4 + 3)];
  const colours = new Set([px(2, 8).join(), px(13, 8).join()]);
  assert.ok(colours.has('0,0,160') && colours.has('160,0,0'), [...colours].join(' | '));
});
