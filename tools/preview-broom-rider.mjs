import {readFileSync,mkdirSync,writeFileSync} from 'node:fs';
import {join,dirname} from 'node:path';
import {fileURLToPath} from 'node:url';
import {Resources} from './preview/assets/resources.mjs';
import {Baker} from './preview/geometry/bake.mjs';
import {render} from './preview/render/rasterizer.mjs';
import {VIEWS,view} from './preview/render/camera.mjs';
import {boxFrame} from './preview/report/views.mjs';
import {decodePng} from './image/png-decode.mjs';
import {png} from './image/png-encode.mjs';
const root=join(dirname(fileURLToPath(import.meta.url)),'..'),index=process.argv.indexOf('--output');if(index<0)throw Error('usage: --output <folder>');const output=process.argv[index+1],source=join(root,'build/broom-rider-preview');const pose=JSON.parse(readFileSync(join(source,'pose.json'))),skin=decodePng(readFileSync(join(source,'player.png')),'player');
const resources=new Resources(root),baker=new Baker(resources),rider=pose.quads.map(q=>({...q,pos:q.pos.map(([x,y,z])=>[-x*pose.playerScale,y*pose.playerScale+16*pose.playerOffset,z*pose.playerScale]),normal:[-q.normal[0],q.normal[1],q.normal[2]]}));
const broom=baker.bake('herbalwitch:item/magic/broom/base').quads.map(q=>({...q,pos:q.pos.map(([x,y,z])=>[x-8,y-8+16*pose.broomHeight,z-8])}));
mkdirSync(output,{recursive:true});for(const cam of [VIEWS.iso,VIEWS.front,view('side',90,15)]){const shot=render([...rider,...broom],cam,id=>id==='native:player'?skin:resources.texture(id),{scale:8,frame:boxFrame(cam,[-22,-4,-26],[22,33,26])});if(shot.missing.size)throw Error('Missing textures: '+[...shot.missing]);writeFileSync(join(output,'ridden-'+cam.name+'.png'),png(shot.image));}
writeFileSync(join(output,'manifest.json'),JSON.stringify({source:'Actual vanilla PlayerModel geometry and cached skin; representative vanilla seated pose, runtime broom and rider offsets',ownerChecks:'Live rider animation, camera, shaders and two-client latency remain unverified.'},null,2));console.log('Rendered three native-player broom riding views');
