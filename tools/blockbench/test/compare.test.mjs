// The parity comparator's texture rule: textures compare at the game's sampling, so an exact integer
// nearest-neighbour enlargement (the 0.2.0-dev.9 move to 32 x 32) is identical and anything else is not.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { sameAtSampling } from '../migration/compare.mjs';

const tex = (size, fn) => {
  const rgba = new Uint8Array(size * size * 4);
  for (let y = 0; y < size; y++) for (let x = 0; x < size; x++) rgba.set(fn(x, y), (y * size + x) * 4);
  return { width: size, height: size, rgba };
};
const art = (x, y) => [x * 13 % 256, y * 7 % 256, (x ^ y) * 5 % 256, (x + y) % 3 ? 255 : 0];

test('equal sizes compare texel for texel', () => {
  assert.ok(sameAtSampling(tex(16, art), tex(16, art)));
  assert.ok(!sameAtSampling(tex(16, art), tex(16, (x, y) => (x === 3 && y === 4 ? [1, 2, 3, 255] : art(x, y)))));
});

test('an exact nearest-neighbour enlargement matches in either direction; one changed texel does not', () => {
  const small = tex(16, art);
  const big = tex(32, (x, y) => art(x >> 1, y >> 1));
  assert.ok(sameAtSampling(small, big));
  assert.ok(sameAtSampling(big, small));
  const touched = tex(32, (x, y) => (x === 7 && y === 9 ? [9, 9, 9, 255] : art(x >> 1, y >> 1)));
  assert.ok(!sameAtSampling(small, touched));
});

test('a non-integer ratio, a changed aspect or a missing texture never match', () => {
  assert.ok(!sameAtSampling(tex(16, art), tex(24, art)));
  assert.ok(!sameAtSampling(tex(16, art), { width: 32, height: 16, rgba: new Uint8Array(32 * 16 * 4) }));
  assert.ok(!sameAtSampling(tex(16, art), null));
  assert.ok(sameAtSampling(null, null));
});
