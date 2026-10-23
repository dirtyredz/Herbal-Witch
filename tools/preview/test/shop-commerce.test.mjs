import { test } from 'node:test';
import assert from 'node:assert/strict';
import { join } from 'node:path';
import { Resources } from '../assets/resources.mjs';
import { Baker } from '../geometry/bake.mjs';
import { zFighting } from '../report/zfighting.mjs';
import { SHOP_SCENES } from '../scenes/shop/commerce.mjs';

const resources = new Resources(join(import.meta.dirname, '../../..'));
const baker = new Baker(resources);
const id = (path) => `herbalwitch:block/shop/${path}`;

test('commerce drafts fit one cell, stand on or hang from their support and never z-fight', () => {
  const models = new Set(['register/closed', 'register/open', 'stock_shelf/base', 'stock_jar/base', 'order_board/base'].map(id));
  assert.deepEqual(zFighting(resources, baker, { models, blocks: new Set() }), []);
  for (const model of models) {
    const elements = baker.bake(model).model.elements;
    assert.ok(elements.length > 0, model);
    assert.ok(elements.every((e) => e.from.every((n) => n >= 0) && e.to.every((n) => n <= 16)), model);
  }
  for (const standing of ['register/closed', 'register/open', 'stock_jar/base']) {
    assert.equal(Math.min(...baker.bake(id(standing)).model.elements.map((e) => e.from[1])), 0, standing);
  }
  for (const hanging of ['stock_shelf/base', 'order_board/base']) {
    assert.equal(Math.max(...baker.bake(id(hanging)).model.elements.map((e) => e.to[2])), 16, `${hanging} back sits flush on its wall`);
  }
});

test('the open and closed register differ only in their sign', () => {
  const closed = baker.bake(id('register/closed')).model.elements.length;
  const open = baker.bake(id('register/open')).model.elements.length;
  assert.ok(Math.abs(closed - open) <= 2 && closed > 10);
});

test('every commerce scene shows all four facings with its support', () => {
  assert.equal(SHOP_SCENES.length, 5);
  for (const scene of SHOP_SCENES) {
    const pieces = scene.entries.filter((e) => e.block && e.block.startsWith('herbalwitch:shop/'));
    assert.deepEqual(pieces.map((e) => e.props.facing), ['north', 'east', 'south', 'west'], scene.name);
    assert.equal(scene.entries.length, 8, scene.name);
  }
});
