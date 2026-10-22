import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { decodeDataUrl } from '../textures.mjs';

const root = join(import.meta.dirname, '../../..');
const pieces = ['bait/grain','bait/root','bait/seed','bait/fish','bait/meat','bait/berry','care/calming_powder','capture/charm','capture/filled_charm'];
const project = piece => JSON.parse(readFileSync(join(root, 'src/art/blockbench/animals', piece, piece.split('/').at(-1) + '.bbmodel'), 'utf8'));

test('nine animal drafts own distinct 32px binary-alpha textures and parent display transforms', () => {
  const textures = new Set();
  for (const piece of pieces) {
    const p = project(piece); assert.equal(p.textures.length, 1);
    const image = decodeDataUrl(p.textures[0].source, piece);
    assert.equal(image.width, 32); assert.equal(image.height, 32);
    let visible = 0;
    for (let i = 3; i < image.rgba.length; i += 4) { assert.ok(image.rgba[i] === 0 || image.rgba[i] === 255); visible += image.rgba[i] === 255; }
    assert.ok(visible > 250 && visible < 800, `${piece}: silhouette occupancy ${visible}`);
    assert.ok(p.display.gui && p.display.ground && p.display.firstperson_righthand);
    assert.equal(p.elements.length, 1); assert.equal(p.elements[0].from[2], p.elements[0].to[2]);
    textures.add(p.textures[0].source);
    const sidecar = JSON.parse(readFileSync(join(root,'src/art/blockbench/animals',piece,'piece.json')));
    assert.equal(sidecar.owned_textures.sprite, `herbalwitch:block/pieces/animals/${piece}/sprite`);
  }
  assert.equal(textures.size, 9);
});

test('empty and filled charm art are separate authored presentation with no animal identity', () => {
  const empty = project('capture/charm'); const filled = project('capture/filled_charm');
  assert.notEqual(empty.textures[0].source, filled.textures[0].source);
  for (const p of [empty, filled]) assert.equal(p.elements[0].faces.south.tintindex, undefined);
});
