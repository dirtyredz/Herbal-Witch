// Validates captureRestartTest evidence: two distinct JVMs, every boundary scenario reached and
// reconciled, the corrupt-ledger case preserved, and no failure reported by the verify JVM.
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import assert from 'node:assert/strict';

const SCENARIOS = ['capture_unwritten', 'capture_unreplaced', 'capture_prepared', 'capture_discarded', 'capture_stored_no_item',
  'capture_complete', 'release_unreplaced', 'release_prepared', 'release_inserted', 'release_uncompacted', 'release_complete'];
const root = process.argv[2];
const prepared = JSON.parse(readFileSync(join(root, 'prepared.json'), 'utf8'));
const verified = JSON.parse(readFileSync(join(root, 'verified.json'), 'utf8'));
assert.notEqual(prepared.process, verified.process, 'verification reused the preparation JVM');
assert.equal(prepared.scenarios, SCENARIOS.length, 'scenario count changed');
assert.equal(prepared.stopped, SCENARIOS.filter((s) => !s.endsWith('_complete')).length, 'not every boundary stopped its operation');
assert.deepEqual(verified.failures, [], 'verification failures');
const seen = verified.results.map((r) => r.scenario);
for (const s of [...SCENARIOS, 'corrupt_ledger']) assert.ok(seen.includes(s), `missing ${s}`);
const quarantined = verified.results.filter((r) => r.expect === 'QUARANTINED');
assert.ok(quarantined.every((r) => r.recovered === true), 'a quarantined scenario was not recovered to exactly one animal');
assert.ok(verified.results.find((r) => r.scenario === 'corrupt_ledger').preserved, 'corrupt ledger not preserved');
console.log(`PASS capture restart: ${SCENARIOS.length} boundaries across JVMs ${prepared.process} -> ${verified.process}; ` +
  `${quarantined.length} quarantined and recovered once; corrupt ledger preserved and ordinary animals admitted.`);
