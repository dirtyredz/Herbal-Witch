import {test} from 'node:test';
import assert from 'node:assert/strict';
import {join} from 'node:path';
import {Resources} from '../assets/resources.mjs';
import {Baker} from '../geometry/bake.mjs';
import {sceneQuads} from '../scenes/world.mjs';
import {TOOL_SCENES} from '../scenes/magic/tools.mjs';
import {textureManifest} from '../../art/catalogue.mjs';
test('tool review covers both held sides, GUI, ground and the untransformed ridden model',()=>{
 const res=new Resources(join(import.meta.dirname,'../../..')),baker=new Baker(res);
 assert.equal(TOOL_SCENES.length,17);
 for(const scene of TOOL_SCENES){const {quads}=sceneQuads(scene.entries,baker,res);assert.ok(quads.length>0,scene.name);for(const q of quads)assert.ok(res.texture(q.texture),q.texture);}
 assert.deepEqual(sceneQuads(TOOL_SCENES.find(s=>s.name==='broom-world-pose').entries,baker,res).quads,baker.bake('herbalwitch:item/magic/broom/base').quads);
 assert.deepEqual(textureManifest().filter(([p])=>p.startsWith('gui/stations/tools/')).map(([p])=>p),['gui/stations/tools/broom_stand.png','gui/stations/tools/wand_bench.png']);
});
