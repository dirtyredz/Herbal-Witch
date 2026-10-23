// Judges qualifyPerformance evidence against the gates frozen before measurement
// (docs/qualification/performance.md). Prints a table and exits 1 when a gate or the harness fails.
//
//   node tools/qualification/performance/report.mjs <evidence dir>
import { readFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { resolve } from 'node:path';

export const GATES = Object.freeze({
  idleDeltaP95: 2, // ms over the matched control
  busyP95: 40,
  busyP99: 50,
  busyDeltaP95: 10, // ms over the matched vanilla control (same counts, vanilla stand-ins)
  checkpointP95: 100, // ms per durable commerce sale (a finding above this)
  saturationEntityGrowth: 0.02, // entity total may not grow by more than 2 % across measured windows
});

const worst = (windows, key) => Math.max(...windows.map((w) => w[key]));

export function judge(perf, restart) {
  const failures = [];
  const rows = [];
  const phases = perf.phases;
  for (const name of ['control', 'idle_cottage', 'busy_vanilla_control', 'busy_settlement', 'saturation', 'large_persistence']) {
    if (!phases[name] || phases[name].windows.length !== perf.protocol.windows) failures.push(`${name}: ${phases[name]?.windows.length ?? 0} measured windows`);
  }
  if (failures.length) return { failures, rows };
  const control = worst(phases.control.windows, 'p95');
  const row = (name, gate, value, pass) => { rows.push({ name, gate, value, pass }); if (!pass) failures.push(`${name}: ${value} (gate ${gate})`); };
  const idle = worst(phases.idle_cottage.windows, 'p95');
  row('idle cottage p95 delta ms', `<= ${GATES.idleDeltaP95}`, +(idle - control).toFixed(2), idle - control <= GATES.idleDeltaP95);
  const busy = phases.busy_settlement.windows;
  row('busy settlement p95 ms', `<= ${GATES.busyP95}`, worst(busy, 'p95'), worst(busy, 'p95') <= GATES.busyP95);
  row('busy settlement p99 ms', `<= ${GATES.busyP99}`, worst(busy, 'p99'), worst(busy, 'p99') <= GATES.busyP99);
  const matched = worst(phases.busy_vanilla_control.windows, 'p95');
  row('busy settlement p95 over matched vanilla ms', `<= ${GATES.busyDeltaP95}`, +(worst(busy, 'p95') - matched).toFixed(2), worst(busy, 'p95') - matched <= GATES.busyDeltaP95);
  for (const part of ['stations', 'familiars', 'creatures']) {
    const w = phases[`busy_part_${part}`]?.windows;
    if (w?.length) row(`busy part ${part} alone p95 over control ms`, 'reported', +(worst(w, 'p95') - control).toFixed(2), true);
  }
  const sat = phases.saturation.windows;
  const first = sat[0].census.entityTotal, last = sat[sat.length - 1].census.entityTotal;
  row('saturation entity growth', `<= ${GATES.saturationEntityGrowth * 100}%`, `${first} -> ${last}`, last <= first * (1 + GATES.saturationEntityGrowth));
  row('saturation familiar scans per tick', '<= 8 (shared budget)', Math.max(...sat.map((w) => w.census.familiarScansLastTick)), sat.every((w) => w.census.familiarScansLastTick <= 8));
  row('saturation familiar paths per tick', '<= 4 (shared budget)', Math.max(...sat.map((w) => w.census.familiarPathsLastTick)), sat.every((w) => w.census.familiarPathsLastTick <= 4));
  row('churn seat entities left', '0', perf.churn.seatEntitiesLeft, perf.churn.seatEntitiesLeft === 0);
  row('churn seated and menus opened', '100 / 100', `${perf.churn.seated} / ${perf.churn.menusOpened}`, perf.churn.seated === 100 && perf.churn.menusOpened === 100);
  const persistence = worst(phases.large_persistence.windows, 'p95');
  row('4,096 bonds steady p95 delta ms', `<= ${GATES.idleDeltaP95} (no roster tick scans)`, +(persistence - control).toFixed(2), persistence - control <= GATES.idleDeltaP95);
  row('bond create (durable) p95 ms', 'reported', perf.largePersistence.createMs.p95, true);
  row('roster page p95 ms', 'reported', perf.largePersistence.pageMs.p95, true);
  row('commerce sale checkpoint p95 ms', `<= ${GATES.checkpointP95}`, perf.commerceSaleMs.p95, perf.commerceSaleMs.p95 <= GATES.checkpointP95);
  if (restart) {
    row('restart index 4,096 bonds', 'reported', `${restart.indexTicks} ticks, ${restart.indexMs} ms`, true);
    row('restart first roster page size', '32', restart.firstPageSize, restart.firstPageSize === 32);
    if (restart.process === perf.process) failures.push('restart reused the measuring JVM');
  } else failures.push('missing restart evidence');
  return { failures, rows, control };
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const dir = process.argv[2];
  const perf = JSON.parse(readFileSync(join(dir, 'performance.json'), 'utf8'));
  const restart = existsSync(join(dir, 'restart.json')) ? JSON.parse(readFileSync(join(dir, 'restart.json'), 'utf8')) : null;
  const { failures, rows, control } = judge(perf, restart);
  console.log(`control p95 ${control} ms; protocol ${JSON.stringify(perf.protocol)}; jvm ${JSON.stringify(perf.jvm)}`);
  for (const [name, p] of Object.entries(perf.phases)) {
    for (const [i, w] of p.windows.entries()) console.log(`${name} window ${i + 1}: p50 ${w.p50} p95 ${w.p95} p99 ${w.p99} max ${w.max} >50ms ${w.over50ms} gc ${w.gcMillis}ms entities ${w.census.entityTotal} hwBE ${w.census.herbalWitchBlockEntityTotal}`);
  }
  for (const r of rows) console.log(`${r.pass ? 'PASS' : 'FAIL'}  ${r.name}: ${r.value} (${r.gate})`);
  if (failures.length) { console.log(`FAIL performance: ${failures.join('; ')}`); process.exit(1); }
  console.log('PASS performance: every frozen gate met');
}
