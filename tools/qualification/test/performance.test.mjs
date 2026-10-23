import { test } from 'node:test';
import assert from 'node:assert/strict';
import { judge, GATES } from '../performance/report.mjs';

const census = (n) => ({ entityTotal: n, herbalWitchBlockEntityTotal: 0, familiarScansLastTick: 2, familiarPathsLastTick: 1 });
const windows = (p95, p99 = p95, n = 100) => [1, 2, 3].map(() => ({ p50: 1, p95, p99, max: p99, over50ms: 0, gcMillis: 0, census: census(n) }));
const perf = (busyP95) => ({
  process: 1, protocol: { windows: 3 },
  phases: { control: { windows: windows(10) }, idle_cottage: { windows: windows(11) }, busy_settlement: { windows: windows(busyP95) },
    busy_vanilla_control: { windows: windows(12) }, saturation: { windows: windows(30) }, large_persistence: { windows: windows(10.5) } },
  churn: { seatEntitiesLeft: 0, seated: 100, menusOpened: 100 },
  largePersistence: { createMs: { p95: 5 }, pageMs: { p95: 0.1 } }, commerceSaleMs: { p95: 20 },
});
const restart = { process: 2, indexTicks: 10, indexMs: 500, firstPageSize: 32 };

test('the frozen gates pass a run inside them', () => {
  assert.deepEqual(judge(perf(18), restart).failures, []);
});

test('a busy p95 over the gate, a missing restart and a missing window all fail', () => {
  assert.ok(judge(perf(GATES.busyP95 + 1), restart).failures.some((f) => f.startsWith('busy settlement p95 ms')));
  assert.ok(judge(perf(18), null).failures.includes('missing restart evidence'));
  const short = perf(18); short.phases.saturation.windows.pop();
  assert.ok(judge(short, restart).failures.some((f) => f.startsWith('saturation')));
});
