export const TOOL_SCENES = [
 ...['broom','wand'].flatMap(tool => ['gui','ground','firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand'].map(display => ({
  name:tool+'-'+display,view:'iso',scale:12,entries:[{at:[0,0,0],model:'herbalwitch:item/magic/'+tool+'/base',display}]
 }))),
 {name:'broom-world-pose',view:'iso',scale:12,entries:[{at:[0,0,0],model:'herbalwitch:item/magic/broom/base'}]},
 ...['broom_stand','wand_bench'].flatMap(station => [false,true].map(active => ({name:station+'-'+(active?'working':'idle'),view:'iso',scale:12,
  entries:[{at:[0,0,0],block:'herbalwitch:stations/tools/'+station+'/station',props:{part:0,facing:'north',active}},
   {at:[-1,0,0],block:'herbalwitch:stations/tools/'+station+'/station',props:{part:1,facing:'north',active}}]
 })))
];
