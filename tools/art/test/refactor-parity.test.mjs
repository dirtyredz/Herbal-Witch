import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { textureManifest } from '../catalogue.mjs';
import { png } from '../../image/png-encode.mjs';
import { canvas, set, finish } from '../content/botany/sprites.mjs';
import { finishOutlined } from '../content/preparations/materials.mjs';
import { reader } from '../interface/grimoire.mjs';
import { Sheet, slot } from '../interface/kit.mjs';

const baseline = JSON.parse(readFileSync(new URL('./fixtures/refactor-textures.json', import.meta.url), 'utf8'));

test('the procedural texture manifest retains its pre-refactor order', () => {
  assert.deepEqual(textureManifest().map(([path]) => path), baseline.map(({ path }) => path));
});

test('every generated PNG retains its pre-refactor bytes', () => {
  for (const [i, [path, make]] of textureManifest().entries()) {
    assert.equal(createHash('sha256').update(png(make())).digest('hex'), baseline[i].sha256, path);
  }
});

test('herb and item outlines retain their distinct edge and darkening rules', () => {
  const cv = canvas();
  for (const [x, y] of [[0, 0], [1, 0], [0, 1], [8, 8]]) set(cv, x, y, [100, 150, 200], true);
  assert.deepEqual(finish(cv).px[0], [100, 150, 200, 255]);
  assert.deepEqual(finishOutlined(cv).px[0], [60, 90, 120, 255]);
  assert.deepEqual(finish(cv).px[8 * 16 + 8], [82, 123, 164, 255]);
  assert.deepEqual(finishOutlined(cv).px[8 * 16 + 8], [60, 90, 120, 255]);
  assert.deepEqual(finish(cv).px[15], [0, 0, 0, 0]);
});

test('the reader frame at 0,184 matches the kit item anchor at 1,185', () => {
  const atlas = reader();
  const kit = new Sheet();
  slot(kit, 1, 185);
  for (let y = 184; y < 202; y++) for (let x = 0; x < 18; x++) {
    assert.deepEqual(atlas.px[y * 256 + x], kit.get(x, y), `${x},${y}`);
  }
});
