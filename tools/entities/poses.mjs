import {bakeElement,rotateAbout} from '../preview/geometry/elements.mjs';

/** Degrees in authored Y-up space; runtime reflects X/Z angles for Minecraft's Y-down bones. */
export function poseAngles(species,pose,time=0) {
  const angles={head:[0,Math.sin(time*.08)*4,0]};
  if(species==='snake') {
    if(pose==='walk'||pose==='slither') {angles.body=[0,Math.sin(time*.5)*6,0];angles.middle=[0,Math.sin(time*.5+.8)*12,0];angles.tail=[0,Math.sin(time*.5+1.6)*18,0];}
    if(pose==='rest'){angles.middle=[0,35,0];angles.tail=[0,35,0];}
    if(pose==='attack'){angles.head=[-18,0,0];angles.jaw=[25,0,0];}
  } else {
    if(pose==='walk'){angles.leg_left=[Math.sin(time*.7)*25,0,0];angles.leg_right=[-Math.sin(time*.7)*25,0,0];}
    if(pose==='flight'){const flap=50+Math.sin(time*.7)*25;angles.wing_left=[0,0,-flap];angles.wing_right=[0,0,flap];angles.leg_left=[-30,0,0];angles.leg_right=[-30,0,0];}
    if(pose==='rest'){angles.head=[-8,0,0];angles.wing_left=[0,0,-4];angles.wing_right=[0,0,4];}
    if(pose==='attack'){angles.head=[-18,0,0];angles.tail=[10,0,0];}
  }
  return angles;
}
function boxFaces(cube) {
  const [u,v]=cube.uv,[x,y,z]=cube.to.map((n,i)=>n-cube.from[i]);
  const boxes={up:[u+z,v,u+z+x,v+z],down:[u+z+x,v,u+z+2*x,v+z],west:[u,v+z,u+z,v+z+y],north:[u+z,v+z,u+z+x,v+z+y],east:[u+z+x,v+z,u+2*z+x,v+z+y],south:[u+2*z+x,v+z,u+2*(z+x),v+z+y]};
  return Object.fromEntries(Object.entries(boxes).map(([face,uv])=>[face,{uv:uv.map(n=>n/2),texture:'#atlas'}]));
}
/** Transform an authored point through its animated bone and every parent, without a runtime dependency. */
export function transformBonePoint(point,boneName,rig,angles,normal=false) {
  const bones=new Map(rig.bones.map(b=>[b.name,b]));let bone=bones.get(boneName),result=[...point];
  while(bone) {
    const origin=normal?[0,0,0]:bone.pivot;
    const rotation=bone.rotation.map((n,i)=>n+(angles[bone.name]?.[i]??0));
    for(let axis=0;axis<3;axis++) if(rotation[axis]) result=rotateAbout(result,{origin,axis:'xyz'[axis],angle:rotation[axis]});
    bone=bones.get(bone.parent);
  }
  return result;
}
export function posedQuads(rig,appearance,pose='idle',time=0) {
  const texture=rig.appearances[appearance]?.replace('herbalwitch:textures/','herbalwitch:').replace(/\.png$/,'');if(!texture)throw new Error('Unknown entity appearance');
  const angles=poseAngles(rig.species,pose,time),quads=[];
  for(const bone of rig.bones)for(const cube of bone.cubes) {
    const baked=bakeElement({from:cube.from,to:cube.to,faces:boxFaces(cube)},()=>texture);
    for(const q of baked) quads.push({...q,pos:q.pos.map(p=>transformBonePoint(p,bone.name,rig,angles)),normal:transformBonePoint(q.normal,bone.name,rig,angles,true)});
  }
  return quads;
}
