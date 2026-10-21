// Dried bundles are flat sprites that must never touch a neighbour's faces (owner feedback
// 0.2.0-dev.1: the sprite bled onto the host block's top and sides; dev.5 brings the 2D look back).
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { hungBundles, INSET } from '../../models/preparations/bundles.mjs';
import { HERB_LIST } from '../../art/botany/herbs.mjs';
import { MODELS } from '../../models/catalogue.mjs';

const spritePlanes = (elements) => elements.filter((e) => Object.values(e.faces).some((f) => f.texture.startsWith('#bundle_')));

test('a bundle sprite is a zero-thickness plane with only its two broad faces, inset from every cell boundary', () => {
  for (const m of hungBundles(HERB_LIST)) {
    const planes = spritePlanes(m.elements);
    assert.ok(planes.length >= 1, `${m.path} has a sprite plane`);
    for (const e of planes) {
      const faces = Object.keys(e.faces).sort().join();
      assert.ok(faces === 'north,south' || faces === 'east,west', `${m.path}: ${faces} (no up/down/edge faces to smear)`);
      assert.ok(e.from.filter((v, i) => v === e.to[i]).length === 1, `${m.path}: a flat plane`);
      for (let i = 0; i < 3; i++) {
        assert.ok(e.from[i] >= INSET && e.to[i] <= 16 - INSET, `${m.path}: axis ${i} reaches ${e.from[i]}..${e.to[i]}`);
      }
    }
  }
});

test('every sprite plane of the stations and furniture obeys the same rule within its cell', () => {
  for (const m of MODELS().filter((x) => /drying_rack\/bundles|stove\/assembly|apothecary_counter/.test(x.path))) {
    for (const e of spritePlanes(m.elements)) {
      assert.deepEqual(Object.keys(e.faces).filter((f) => f === 'up' || f === 'down'), [], `${m.path}: no horizontal faces`);
      const lo = e.from.map((v, i) => v === e.to[i] ? v : null).findIndex((v) => v !== null);
      const at = e.from[lo];
      // the plane's own coordinate keeps clear of the cell boundary (assembly models are 16 or 32 wide)
      assert.ok(((at % 16) + 16) % 16 >= INSET && ((at % 16) + 16) % 16 <= 16 - INSET, `${m.path}: plane at ${at}`);
    }
  }
});
