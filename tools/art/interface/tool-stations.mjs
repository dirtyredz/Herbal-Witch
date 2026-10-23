import {C, Sheet, panel, slot, inventory} from './kit.mjs';
function atlas(base) {
 const s=new Sheet();panel(s);inventory(s);
 for(const x of [30,48,66])slot(s,x,26);slot(s,124,35);if(base)slot(s,48,58);
 const inside=(x,y)=>(x<=14&&y>=5&&y<=11)||(x>=15&&x<=23&&Math.abs(y-8)<=8-(x-15));
 for(let y=0;y<17;y++)for(let x=0;x<24;x++)if(inside(x,y)){
  const edge=[[1,0],[-1,0],[0,1],[0,-1]].some(([dx,dy])=>!inside(x+dx,y+dy));
  s.set(92+x,34+y,edge?C.line:C.parchLo);s.set(176+x,14+y,edge?C.green:C.pink);
 }
 return s.image();
}
export const broom_stand=()=>atlas(true);
export const wand_bench=()=>atlas(false);
