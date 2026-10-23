# Performance (Milestone 12)

## Protocol (frozen before the first measurement)

`qualifyPerformance` (ANU only) runs one dedicated GameTest JVM on a fresh flat world with natural mob
spawning off, then a second JVM on the same world. Tick durations come from vanilla's own tick ring
(`MinecraftServer.tickTimes`), one sample per tick, so the measurement adds nothing to the tick it
measures. Each phase builds its workload on its own force-loaded 48 x 48 plot far from the GameTest
plots, warms up 600 ticks, then measures three consecutive 800-tick windows (2,400 ticks, two minutes
at 20 TPS). The worst window counts. The control phase is the same JVM and world before any workload.

Deviation from the plan's protocol (five minutes' warm-up, three ten-minute repetitions): the shared
ANU machine also serves Plex and the CI runner, so each workload gets 30 s of warm-up and 2 minutes of
measurement in one JVM. Windows are repeated inside the run instead of across runs. The figures are
therefore a qualification signal on this hardware, not a capacity claim.

| Workload | Population (fixed in `Workloads.java`) |
|---|---|
| control | nothing (the GameTest world idling) |
| idle cottage | 256 Herbal Witch block entities (96 idle stations, 96 stocked single-cell storage/display pieces, 32 oil lanterns, 32 hearths) and a 16 x 16 joined herb bed garden with growing herbs (random ticks on) |
| busy settlement | 128 processors (64 mortars grinding 64 bundles each, 64 idle kettles), 128 stocked storage pieces, 8 online owners with 3 following familiars each, 32 offline home guards on beds, 8 deployed brooms, 16 villagers, 200 cows/sheep/pigs/chickens with care state |
| saturation | the busy settlement twice (16 owners, per-owner caps kept) |
| churn | 100 cycles: place a seat, sit, stand, break it; place a cabinet, open, close, break it |
| large persistence | 4,096 bonds for one owner created through the durable store, none loaded; then steady ticks; a second JVM times startup indexing |
| commerce | 32 sales, each forcing and verifying the buyer's player file and the shop shard |

## Gates (`tools/qualification/performance/report.mjs`, frozen)

| Gate | Threshold |
|---|---|
| Idle cottage p95 over control | at most 2 ms |
| Busy settlement p95 / p99 | at most 40 ms / 50 ms |
| Busy settlement p95 over control | at most 10 ms |
| Saturation | entity total grows at most 2 % across windows; familiar work stays within its shared budgets (8 scans, 4 paths per dimension tick) |
| Churn | no seat entity left; 100 seatings and 100 menus |
| 4,096 bonds | steady p95 over control at most 2 ms (no roster scans per tick); first roster page of 32 after restart |
| Commerce sale | p95 at most 100 ms (a finding above it) |

Algorithmic budgets (familiar scans/paths, animal candidates/paths, ward checks, projectile caps, wand
collection bounds, villager evaluations) are asserted deterministically by the existing GameTests and
unit tests, not by wall-clock thresholds.

## Results

See the README results table for the job id; the full per-window numbers, populations by type and GC
time are in the job's `run/performance/<uuid>/evidence/performance.json` on ANU and copied to
`C:/Users/dirty/projects/artifacts/minecraft-mods/jobs/2026-10-23-herbal-witch-m12/runs/`.
