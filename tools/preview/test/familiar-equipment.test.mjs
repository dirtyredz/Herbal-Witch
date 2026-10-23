import {test} from 'node:test';
import assert from 'node:assert/strict';
import {join} from 'node:path';
import {Resources} from '../assets/resources.mjs';
import {Baker} from '../geometry/bake.mjs';
import {sceneQuads} from '../scenes/world.mjs';
import {FAMILIAR_CREATURE_SCENES} from '../scenes/familiars/creatures.mjs';
import {FAMILIAR_EQUIPMENT_SCENES} from '../scenes/familiars/equipment.mjs';
import {zFighting} from '../report/zfighting.mjs';

test('eight authored accessories and nineteen equipped creature poses resolve real owned textures', () => {
  const resources = new Resources(join(import.meta.dirname, '../../..')), baker = new Baker(resources);
  assert.equal(FAMILIAR_CREATURE_SCENES.length, 19);
  assert.equal(FAMILIAR_EQUIPMENT_SCENES.length, 8);
  for (const scene of [...FAMILIAR_CREATURE_SCENES, ...FAMILIAR_EQUIPMENT_SCENES]) {
    const {quads} = sceneQuads(scene.entries, baker, resources);
    assert.ok(quads.length > 0, scene.name);
    for (const q of quads) {
      assert.ok(q.pos.every(p => p.every(n => Number.isFinite(n) && Math.abs(n) < 32)), scene.name);
      assert.ok(resources.texture(q.texture), `${scene.name}: ${q.texture}`);
    }
  }
  const models = new Set(FAMILIAR_EQUIPMENT_SCENES.map(s => s.entries[0].model));
  assert.deepEqual(zFighting(resources, baker, {models, blocks: new Set()}), []);
  assert.deepEqual(resources.diagnostics, []);
});
