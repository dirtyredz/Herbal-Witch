import fs from 'node:fs';
import path from 'node:path';
const root=process.argv[2]; const a=JSON.parse(fs.readFileSync(path.join(root,'prepared.json'))),b=JSON.parse(fs.readFileSync(path.join(root,'verified.json')));
if(a.process===b.process)throw Error('Restart must use distinct JVMs');
for(const key of ['world_entity','no_lease','packed_metadata','seven_modes','repair_paused','cancel_conserves','no_offline_spend','old_nonce_refused','one_broom'])if(!b.checks.includes(key))throw Error('Missing '+key);
console.log('PASS: physical tool restart in two dedicated JVMs');
