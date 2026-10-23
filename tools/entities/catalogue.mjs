import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import {readEntityProject} from './project.mjs';
export const SOURCE='src/art/entities';
export function loadEntityProjects(root) {
  const catalogue=JSON.parse(readFileSync(join(root,SOURCE,'catalog.json'),'utf8'));
  if(catalogue.schema!==1||JSON.stringify(catalogue.species)!==JSON.stringify(['raven','owl','snake'])) throw new Error('Entity catalogue must name raven, owl and snake exactly once');
  return catalogue.species.map(species=>{
    const folder=join(root,SOURCE,species),sidecar=JSON.parse(readFileSync(join(folder,'rig.json'),'utf8'));
    if(sidecar.schema!==1||sidecar.project!==`${species}.bbmodel`||sidecar.species!==species) throw new Error(`${species}: invalid rig sidecar`);
    const parsed=readEntityProject(JSON.parse(readFileSync(join(folder,sidecar.project),'utf8')),`${species}/${sidecar.project}`);
    if(typeof sidecar.equipmentBone!=='string'||!parsed.bones.some(b=>b.name===sidecar.equipmentBone)) throw new Error(`${species}: equipment bone is missing`);
    const appearances=Object.keys(sidecar.appearances??{});
    if(JSON.stringify(appearances)!==JSON.stringify(species==='raven'?['raven','crow']:[species])||appearances.some(a=>sidecar.appearances[a]!==a||!parsed.textures.has(a))||parsed.textures.size!==appearances.length) throw new Error(`${species}: owned appearance texture mismatch`);
    return {species,...parsed,equipmentBone:sidecar.equipmentBone,appearances};
  });
}
