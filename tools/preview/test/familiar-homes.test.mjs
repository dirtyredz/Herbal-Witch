import {test} from 'node:test';
import assert from 'node:assert/strict';
import {join} from 'node:path';
import {Resources} from '../assets/resources.mjs';
import {Baker} from '../geometry/bake.mjs';
import {zFighting} from '../report/zfighting.mjs';
import {FAMILIAR_HOME_SCENES} from '../scenes/familiars/homes.mjs';

test('home drafts stand on their floor, fit one cell and never overlap coplanar faces', () => {
  const resources = new Resources(join(import.meta.dirname, '../../..')), baker = new Baker(resources);
  const models = new Set(['bed','nest','perch'].map(key => `herbalwitch:block/familiars/home/${key}/base`));
  assert.deepEqual(zFighting(resources,baker,{models,blocks:new Set()}),[]);
  for (const id of models) {
    const elements = baker.bake(id).model.elements;
    assert.equal(Math.min(...elements.map(e=>e.from[1])),0,id);
    assert.ok(elements.every(e=>e.from.every(n=>n>=0) && e.to.every(n=>n<=16)),id);
  }
  for (const scene of FAMILIAR_HOME_SCENES) {
    const homes=scene.entries.filter(e=>e.block), floors=scene.entries.filter(e=>e.proxy);
    assert.deepEqual(homes.map(e=>e.props.facing),['north','east','south','west']);
    for(const home of homes) assert.ok(floors.some(f=>f.at[0]===home.at[0] && f.at[2]===home.at[2] && f.at[1]===home.at[1]-1));
  }
});
