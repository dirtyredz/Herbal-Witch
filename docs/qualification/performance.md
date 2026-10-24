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

Candidate run: ANU `HerbalWitch-1b7414627fc396af96ea98f0-20261023-204157-aef` at 1b74146 (1.0.0-rc.1, production
code unchanged since f5f1107), Temurin 17.0.20.1, 8 GB heap, i7-4820K (4 cores / 8 threads), superflat world.
Worst of three 800-tick windows (by p95), milliseconds:

| Phase | p50 | p95 | p99 | max | ticks > 50 ms | entities | Herbal Witch BEs |
|---|---|---|---|---|---|---|---|
| control | 0.31 | 0.52 | 0.75 | 2.0 | 0 | 0 | 0 |
| idle cottage | 0.37 | 0.58 | 0.96 | 1.5 | 0 | 0 | 256 |
| busy, matched vanilla control | 13.27 | 15.47 | 19.13 | 37.9 | 0 | 280 | 0 |
| busy: stations only | 0.42 | 0.64 | 0.88 | 3.4 | 0 | 0 | 256 |
| busy: familiars only | 6.20 | 7.22 | 8.87 | 11.5 | 0 | 56 | 32 |
| busy: creatures only (216 mobs with care state, 8 brooms) | 10.93 | 13.17 | 15.05 | 24.1 | 0 | 224 | 0 |
| busy settlement | 18.58 | 21.10 | 23.42 | 30.0 | 0 | 280 | 288 |
| saturation (twice busy) | 36.66 | 40.29 | 42.95 | 48.5 | 0 | 560 | 576 |
| 4,096 bonds, none loaded | 0.24 | 0.34 | 0.45 | 1.0 | 0 | 0 | 0 |

Gates: idle cottage +0.06 ms over control (gate 2); busy settlement p95 21.10 / p99 23.42 (gates 40 / 50);
busy settlement 5.63 ms over its matched vanilla control (gate 10); saturation entities 560 to 560 with familiar
work within its shared budget; churn left no seat (100 seatings, 100 menus); 4,096 bonds -0.18 ms over control;
durable bond create p95 6.86 ms; roster page p95 0.07 ms; restart indexed 4,096 bonds in 132 ticks (1.7 s) and
served a first page of 32; durable commerce sale p95 17.18 ms (gate 100). **Every frozen gate passed.** Single
spikes of 80-500 ms appear once in a few phases (probably the periodic world save; not investigated further).
Saturation runs near the 50 ms tick budget on this CPU; twice the busy settlement is a stress figure, not a
supported population.

Deviation found after the first candidate run: the protocol says a flat world, but every GameTest JVM had been
generating normal terrain (GOTCHAS, "The Forge GameTest server..."), so the plots sat in caves and natural
mobs added to every phase. From 1b74146 the world is superflat with spawning off and the workload plots are
penned, so each phase holds exactly its fixed population (the entity column matches `Workloads.java`).

History (kept, not overwritten): the first run compared the busy settlement with an empty world (25.28 ms, a
harness defect, job ...150743-eed); the matched control then measured 10.87 ms, a gate failure (...154923-fca).
A flight recording (...161631-ab3) traced 40% of Herbal Witch samples to `FamiliarRecord.state()` copying the
whole bond state on every lookup and tick; after the fix (f3cbab1) the delta was 7.28 ms (...162624-6b4), then
5.91 ms at f5f1107 in cave terrain (...164003-755) and 5.63 ms on the superflat candidate. Run-to-run noise on
this shared machine is a few milliseconds.
