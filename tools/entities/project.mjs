// Separately pinned Blockbench 5.2.2 / project 5.0 entity contract. Block/item parsing stays unchanged.
import {decodePng} from '../image/png-decode.mjs';
export const EDITOR_VERSION='5.2.2', FORMAT_VERSION='5.0', LOGICAL_ATLAS=32, PHYSICAL_ATLAS=64;
const vector=(v,n=3)=>Array.isArray(v)&&v.length===n&&v.every(x=>Number.isFinite(x)&&Math.abs(x)<=128);
const fail=(where,message)=>{throw new Error(`${where}: ${message}`);};
const name=/^[a-z][a-z0-9_]{0,31}$/;

export function readEntityProject(project, where='entity project') {
  if(project.meta?.format_version!==FORMAT_VERSION||project.meta?.model_format!=='modded_entity') fail(where,`requires Blockbench ${EDITOR_VERSION}, format 5.0 modded_entity`);
  if(project.resolution?.width!==LOGICAL_ATLAS||project.resolution?.height!==LOGICAL_ATLAS) fail(where,'logical atlas must be 32 x 32');
  if(project.animations?.length||project.animation_controllers?.length) fail(where,'animations are procedural; animation-library tracks are unsupported');
  if(!Array.isArray(project.textures)||project.textures.length<1||project.textures.length>2) fail(where,'one or two independently owned appearance atlases required');
  const textures=new Map();
  for(const t of project.textures) {
    if(!name.test(t.id)||textures.has(t.id)||typeof t.source!=='string'||!t.source.startsWith('data:image/png;base64,')) fail(where,'textures need unique names and embedded PNG data');
    const bytes=Buffer.from(t.source.slice('data:image/png;base64,'.length),'base64'), image=decodePng(bytes,`${where}/${t.id}`);
    if(image.width!==PHYSICAL_ATLAS||image.height!==PHYSICAL_ATLAS) fail(where,'entity atlases export at 64 x 64 (two texels per model unit)');
    textures.set(t.id,bytes);
  }
  if(!Array.isArray(project.elements)||project.elements.length>128||!Array.isArray(project.groups)||project.groups.length>64) fail(where,'bounded cube and bone lists required');
  const elements=new Map(),groups=new Map(),used=new Set(),names=new Set(),bones=[];
  for(const cube of project.elements) {
    if(cube.type!=='cube'||cube.box_uv!==true||!vector(cube.from)||!vector(cube.to)||cube.from.some((n,i)=>n>=cube.to[i])) fail(where,`${cube.name}: positive cuboids with explicit box UV only`);
    if(!vector(cube.uv_offset,2)||cube.uv_offset.some(n=>n<0||!Number.isInteger(n))) fail(where,`${cube.name}: integer atlas origin required`);
    const [x,y,z]=cube.to.map((n,i)=>n-cube.from[i]),[u,v]=cube.uv_offset;
    if(u+2*(x+z)>LOGICAL_ATLAS||v+y+z>LOGICAL_ATLAS) fail(where,`${cube.name}: unfolded box UV leaves the logical atlas`);
    if(cube.rotation?.some(n=>n!==0)||cube.inflate||cube.rescale||cube.stretch?.some(n=>n!==1)) fail(where,`${cube.name}: animate bones, not individual cube transforms`);
    if(!cube.uuid||elements.has(cube.uuid)) fail(where,'duplicate or absent cube identity');
    elements.set(cube.uuid,{from:[...cube.from],to:[...cube.to],uv:[...cube.uv_offset],mirror:cube.mirror_uv===true});
  }
  for(const group of project.groups) {
    if(!group.uuid||groups.has(group.uuid)||!name.test(group.name)||names.has(group.name)||!vector(group.origin)||!vector(group.rotation??[0,0,0])) fail(where,'bones need unique names, explicit pivots and finite rotations');
    groups.set(group.uuid,group);names.add(group.name);
  }
  const seenBones=new Set();
  function walk(nodes,parent,depth) {
    if(depth>8||!Array.isArray(nodes)) fail(where,'bone hierarchy exceeds eight levels');
    for(const node of nodes) {
      if(typeof node==='string') fail(where,'every cube must belong to a named bone');
      const group=groups.get(node.uuid); if(!group||seenBones.has(node.uuid)) fail(where,'missing, cyclic or duplicated bone');
      seenBones.add(node.uuid);
      const bone={name:group.name,parent,pivot:[...group.origin],rotation:[...(group.rotation??[0,0,0])],cubes:[]};bones.push(bone);
      const children=[];
      for(const child of node.children??[]) {
        if(typeof child!=='string') {children.push(child);continue;}
        if(!elements.has(child)||used.has(child)) fail(where,'missing or repeated cube in outliner');
        used.add(child);bone.cubes.push(elements.get(child));
      }
      walk(children,group.name,depth+1);
    }
  }
  walk(project.outliner,null,0);
  if(used.size!==elements.size||seenBones.size!==groups.size||!names.has('body')||!names.has('head')) fail(where,'orphan geometry or missing body/head bone');
  return {bones,textures};
}
