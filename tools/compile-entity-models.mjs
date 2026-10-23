// Blockbench creature rigs own only entity_models/familiars and textures/entity/familiars.
import {join} from 'node:path';
import {compileEntities} from './entities/compile.mjs';
import {writeOutputs} from './art/output.mjs';
const root=join(import.meta.dirname,'..'),assets=join(root,'src/main/resources/assets/herbalwitch');
const compiled=compileEntities(root),check=process.argv.includes('--check');
const models=writeOutputs(assets,compiled.models,{name:'entity rigs',dirs:['entity_models/familiars'],ext:'.json'},{check,log:()=>{}});
const textures=writeOutputs(assets,compiled.textures,{name:'entity textures',dirs:['textures/entity/familiars'],ext:'.png'},{check,log:()=>{}});
const problems=[...models.problems,...textures.problems];
console.log(`3 entity projects -> ${compiled.models.size} rigs, ${compiled.textures.size} owned atlases: ${models.written.length+textures.written.length} written, ${models.unchanged.length+textures.unchanged.length} unchanged`);
if(problems.length){problems.forEach(p=>console.error(p));process.exitCode=1;}
