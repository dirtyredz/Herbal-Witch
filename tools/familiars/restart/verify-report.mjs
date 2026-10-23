import {readFileSync} from 'node:fs';
import {join} from 'node:path';
import assert from 'node:assert/strict';
const root=process.argv[2];
const prepared=JSON.parse(readFileSync(join(root,'prepared.json'),'utf8'));
const verified=JSON.parse(readFileSync(join(root,'verified.json'),'utf8'));
assert.notEqual(prepared.process,verified.process,'verification reused the preparation JVM');
assert.equal(verified.cases,prepared.cases);
assert.ok(prepared.cases >= 20 && prepared.cases <= 192,'checkpoint capture outside qualification bounds');
assert.deepEqual(verified.failures,[]);
for(const phase of ['BINDING_PREPARED','ACTIVE','TRANSFER_PREPARED','INCAPACITATION_PREPARED','INCAPACITATED','REVIVAL_PREPARED'])
  assert.ok(prepared.phases.includes(phase),`missing actual ${phase} checkpoint`);
assert.ok(verified.staleItems && verified.corruptPreserved && verified.schemaPreserved && verified.indexBounded && verified.duplicatesQuarantined);
assert.ok(verified.captureLifecycle,'missing capture/owner lifecycle/unloaded-home restart evidence');
console.log(`PASS familiar restart: ${verified.cases} checkpoint snapshots, distinct JVMs ${prepared.process} -> ${verified.process}; stale item, corruption, schema, bounded index and duplicate admission verified.`);
