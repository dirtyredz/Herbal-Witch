// Native geometry/UVs are exported by FamiliarNativePoseTest using Minecraft's ModelPart renderer.
// Vanilla textures are read from the locally cached dependency; neither is copied into mod assets.
import {readFileSync,mkdirSync,writeFileSync} from 'node:fs';
import {join,dirname} from 'node:path';
import {fileURLToPath} from 'node:url';
import {Resources} from './preview/assets/resources.mjs';
import {Baker} from './preview/geometry/bake.mjs';
import {render} from './preview/render/rasterizer.mjs';
import {VIEWS} from './preview/render/camera.mjs';
import {boxFrame} from './preview/report/views.mjs';
import {contactSheet} from './image/contact-sheet.mjs';
import {decodePng} from './image/png-decode.mjs';
import {png} from './image/png-encode.mjs';
const root=join(dirname(fileURLToPath(import.meta.url)),'..');
const index=process.argv.indexOf('--output');if(index<0)throw Error('usage: node tools/preview-native-familiars.mjs --output <preview folder>');
const output=process.argv[index+1],source=join(root,'build/familiar-native-previews');
const poses=JSON.parse(readFileSync(join(source,'poses.json'),'utf8'));
const resources=new Resources(root),baker=new Baker(resources);
const gear={cat:'cat_bell',fox:'fox_pouch',wolf:'wolf_harness',rabbit:'rabbit_sachet',bat:'bat_chime'};
const textures=new Map(Object.keys(gear).map(s=>[`native:${s}`,decodePng(readFileSync(join(source,s+'.png')),s)]));
const lookup=id=>textures.get(id)??resources.texture(id);
function equipment(scene) {
 const m=scene.anchor,[x,y,z,s,pitch=0]=scene.attachment, angle=pitch*Math.PI/180;
 const transform=(p,normal=false)=>{
   const [u,v,w]=normal?[p[0],-p[1],-p[2]]:[(p[0]-8)*s/16,-(p[1]-8)*s/16,-(p[2]-8)*s/16];
   const [a,b,c]=[u+(normal?0:x/16),v*Math.cos(angle)-w*Math.sin(angle)+(normal?0:y/16),v*Math.sin(angle)+w*Math.cos(angle)+(normal?0:z/16)];
   const q=[m[0]*a+m[4]*b+m[8]*c+(normal?0:m[12]),m[1]*a+m[5]*b+m[9]*c+(normal?0:m[13]),m[2]*a+m[6]*b+m[10]*c+(normal?0:m[14])];
   return normal?[q[0],-q[1],q[2]]:[16*q[0],24-16*q[1],16*q[2]];
 };
 return baker.bake(`herbalwitch:block/familiars/equipment/${gear[scene.species]}/base`).quads.map(q=>({...q,pos:q.pos.map(p=>transform(p)),normal:transform(q.normal,true)}));
}
mkdirSync(output,{recursive:true});const cells=[];
for(const scene of poses){
 const quads=[...scene.quads,...equipment(scene)];
 const shot=render(quads,VIEWS.iso,lookup,{scale:6,frame:boxFrame(VIEWS.iso,[-25,-4,-20],[25,30,20])});
 if(shot.missing.size)throw Error(`Missing ${[...shot.missing]}`);
 const name=`${scene.species}-${scene.pose}`;writeFileSync(join(output,name+'.png'),png(shot.image));
 cells.push({label:name,image:shot.image});
}
writeFileSync(join(output,'native-review.png'),png(contactSheet({title:'Native familiar poses with runtime equipment anchors',cells,columns:4,cellW:cells[0].image.width,cellH:cells[0].image.height})));
writeFileSync(join(output,'manifest.json'),JSON.stringify({poses:poses.length,source:'Actual vanilla ModelPart cubes/UVs; runtime rest projection and attachment transforms',approximation:'Representative native locomotion samples, directional shading; owner in-game animation/shader review pending'},null,2)+'\n');
console.log(`Rendered ${poses.length} native equipped poses to ${output}`);
