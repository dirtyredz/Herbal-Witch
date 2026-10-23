import {loadEntityProjects} from './catalogue.mjs';
export function compileEntities(root) {
  const models=new Map(),textures=new Map();
  for(const project of loadEntityProjects(root)) {
    const appearances={};
    for(const name of project.appearances) {
      const id=`entity/familiars/${project.species}/${name}`;
      appearances[name]=`herbalwitch:textures/${id}.png`;textures.set(`textures/${id}.png`,project.textures.get(name));
    }
    const model={schema:1,species:project.species,atlas:[32,32],equipmentBone:project.equipmentBone,appearances,bones:project.bones};
    models.set(`entity_models/familiars/${project.species}/rig.json`,JSON.stringify(model,null,2)+'\n');
  }
  return {models,textures};
}
