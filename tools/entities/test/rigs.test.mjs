import {test} from 'node:test';import assert from 'node:assert/strict';import fs from 'node:fs';import {join} from 'node:path';
import {compileEntities} from '../compile.mjs';import {readEntityProject} from '../project.mjs';import {posedQuads} from '../poses.mjs';
import {parseProject} from '../../blockbench/project.mjs';import {png} from '../../image/png-encode.mjs';
const root=join(import.meta.dirname,'../../..');
const source=()=>JSON.parse(fs.readFileSync(join(root,'src/art/entities/raven/raven.bbmodel'),'utf8'));

test('three editable rigs own four deterministic 64px appearance atlases',()=>{
 const first=compileEntities(root),second=compileEntities(root);assert.deepEqual(first,second);
 assert.deepEqual([...first.models.keys()],['raven','owl','snake'].map(s=>`entity_models/familiars/${s}/rig.json`));
 assert.deepEqual([...first.textures.keys()],['raven/raven','raven/crow','owl/owl','snake/snake'].map(s=>`textures/entity/familiars/${s}.png`));
 for(const text of first.models.values()){const rig=JSON.parse(text);assert.deepEqual(rig.atlas,[32,32]);assert.ok(rig.bones.length<=64);assert.ok(rig.bones.some(b=>b.name===rig.equipmentBone));}
});
test('entity-only bones do not weaken the block parser and unsupported features fail explicitly',()=>{
 const raw=source();assert.throws(()=>parseProject(JSON.stringify(raw)),/java_block|Java Block|model_format/);
 for(const mutate of [p=>p.meta.format_version='6.0',p=>p.elements[0].type='mesh',p=>p.elements[0].rotation=[1,0,0],p=>p.elements[0].uv_offset=[31,31],p=>p.outliner.push(p.outliner[0]),p=>p.groups[0].origin=[Infinity,0,0],p=>p.animations=[{}]]){
  const bad=source();mutate(bad);assert.throws(()=>readEntityProject(bad));
 }
 const low=source();low.textures[0].source='data:image/png;base64,'+png({width:32,height:32,px:new Uint8Array(32*32*4)}).toString('base64');
 assert.throws(()=>readEntityProject(low),/64 x 64/);
});
test('posed geometry keeps ground contact and changes wings and snake segments',()=>{
 for(const text of compileEntities(root).models.values()) {
  const rig=JSON.parse(text),appearance=Object.keys(rig.appearances)[0],idle=posedQuads(rig,appearance),moving=posedQuads(rig,appearance,rig.species==='snake'?'slither':'flight',2);
  assert.equal(Math.min(...idle.flatMap(q=>q.pos.map(p=>p[1]))),0);
  assert.notDeepEqual(idle.map(q=>q.pos),moving.map(q=>q.pos));
  assert.ok(moving.every(q=>q.pos.every(p=>p.every(Number.isFinite))));
  assert.ok(moving.every(q=>q.uv.every(uv=>uv.every(n=>n>=0&&n<=16))));
  if(rig.species==='snake')assert.equal(Math.min(...moving.flatMap(q=>q.pos.map(p=>p[1]))),0);
  else assert.ok(Math.max(...moving.flatMap(q=>q.pos.map(p=>Math.abs(p[0]))))>Math.max(...idle.flatMap(q=>q.pos.map(p=>Math.abs(p[0])))));
 }
});
