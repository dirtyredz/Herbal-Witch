// Validates commerce restart evidence: two distinct JVMs and every required boundary reconciled.
import fs from 'node:fs';
import path from 'node:path';

const root = process.argv[2];
const prepared = JSON.parse(fs.readFileSync(path.join(root, 'prepared.json'), 'utf8'));
const verified = JSON.parse(fs.readFileSync(path.join(root, 'verified.json'), 'utf8'));
if (prepared.process === verified.process) throw new Error('Restart must use distinct JVMs');
const required = [];
for (const kind of ['deposit', 'purchase', 'withdraw', 'proceeds']) {
  for (const boundary of ['prepare_unwritten', 'prepared', 'player_saved', 'commit_unwritten', 'committed']) required.push(`${kind}.${boundary}`);
}
required.push('npc.prepared', 'npc.debited', 'purchase.older_player_file', 'purchase.missing_player_file', 'corrupt_shard_preserved');
const missing = required.filter((name) => !verified.checks.includes(name));
if (missing.length) throw new Error(`Missing commerce restart checks: ${missing.join(', ')}`);
for (const name of Object.keys(prepared.scenarios)) if (!verified.checks.includes(name)) throw new Error(`Prepared scenario not verified: ${name}`);
console.log(`PASS: commerce restart in two dedicated JVMs (${verified.checks.length} checks)`);
