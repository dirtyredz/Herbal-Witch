// The set's texture density (tools/art/density.mjs): two texels per model unit. Every procedural
// texture comes out at its required size, the sprites and GUI atlases drawn at one texel per unit are
// exact 2x nearest-neighbour enlargements (so the game shows the same pixels), and every committed
// texture, whichever producer wrote it, is at the one density.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { textureManifest } from '../catalogue.mjs';
import { upscale } from '../pixels.mjs';
import { checkDensity, expectedSize, TEXTURE_SIZE, GUI_SIZE } from '../density.mjs';
import { listFiles } from '../output.mjs';
import { decodePng } from '../../image/png-decode.mjs';

const TEXTURES = join(import.meta.dirname, '../../../src/main/resources/assets/herbalwitch/textures');

test('upscale enlarges nearest-neighbour: every pixel becomes a factor x factor block', () => {
  const img = { size: 2, px: [[1, 0, 0, 255], [2, 0, 0, 255], [3, 0, 0, 255], [4, 0, 0, 0]] };
  const big = upscale(img, 2);
  assert.equal(big.size, 4);
  assert.deepEqual(big.px.map((p) => p[0]), [1, 1, 2, 2, 1, 1, 2, 2, 3, 3, 4, 4, 3, 3, 4, 4]);
  assert.throws(() => upscale(img, 1.5), /bad upscale factor/);
});

test('the density rule: 32 x 32 for blocks and items, 512 x 512 for GUI atlases, nothing else', () => {
  assert.equal(expectedSize('block/material/wood.png'), TEXTURE_SIZE);
  assert.equal(expectedSize('item/botany/dry/thyme/seeds.png'), 32);
  assert.equal(expectedSize('gui/stations/kettle.png'), GUI_SIZE);
  assert.doesNotThrow(() => checkDensity('item/x.png', { size: 32, px: [] }));
  assert.throws(() => checkDensity('item/x.png', { size: 16, px: [] }), /item\/x.png: 16 x 16, but the set paints at 2 texels per model unit \(32 x 32/);
  assert.throws(() => checkDensity('block/x.png', { size: 64, px: [] }), /64 x 64/);
  assert.throws(() => checkDensity('gui/stations/x.png', { size: 256, px: [] }), /512 x 512/);
});

test('every procedural texture is at the density, and the 1x-drawn ones are exact 2x enlargements', () => {
  let enlarged = 0;
  for (const [rel, make] of textureManifest()) {
    const img = make();
    assert.equal(img.size, expectedSize(rel), rel);
    const sameBlock = (x, y) => [[1, 0], [0, 1], [1, 1]].every(([dx, dy]) => String(img.px[(y + dy) * img.size + x + dx]) === String(img.px[y * img.size + x]));
    let uniform = true;
    for (let y = 0; y < img.size && uniform; y += 2) for (let x = 0; x < img.size && uniform; x += 2) uniform = sameBlock(x, y);
    if (rel.startsWith('item/') || rel.startsWith('gui/')) {
      assert.ok(uniform, `${rel} is drawn at one texel per unit and enlarged, so every 2 x 2 block is one colour`);
      enlarged++;
    }
  }
  assert.ok(enlarged > 70, `expected the sprites and GUI atlases, found ${enlarged}`);
});

test('every committed texture, procedural or Blockbench-owned, is at the one density', () => {
  const files = listFiles(TEXTURES, '.').filter((p) => p.endsWith('.png'));
  assert.ok(files.length > 150, `expected the whole texture set, found ${files.length}`);
  for (const rel of files) {
    const img = decodePng(readFileSync(join(TEXTURES, rel)), rel);
    const rule = rel.replace(/^\.\//, '');
    assert.doesNotThrow(() => checkDensity(rule, img), rule);
  }
});
