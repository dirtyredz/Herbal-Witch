# Release qualification (Milestone 12)

The evidence that the candidate does what the design promised, keeps old worlds, survives restarts and
crashes at its durable boundaries, runs on a real dedicated server beside the supported mods, stays
within its performance budget and reads in English. Owner acceptance and publishing are separate.

| Document | Holds |
|---|---|
| [design-audit.md](design-audit.md) | Every promise in the original design traced to code and tests, or marked |
| [save-compatibility.md](save-compatibility.md) | Forward guarantee; the owner's real 0.2.0-dev.9 and 0.6.1 worlds upgraded |
| [performance.md](performance.md) | Frozen protocol and gates; measured results |
| [extensibility.md](extensibility.md) | Configuration files, datapack seams, KubeJS |
| [owner-acceptance.md](owner-acceptance.md) | The owner's numbered in-game checklist, M1-M12 |
| [release-readiness.md](release-readiness.md) | What is not release-ready, with Docket ids |

## Harnesses (all ANU only, never part of `build` or `check`)

| Task | What it runs | Pass means |
|---|---|---|
| `test build runGameTestServer -PgameTestRunId=<id>` | JUnit, art checks, every GameTest on a fresh world | All required tests pass |
| `runGameTestServer -PcompatProfile=<p> ...` | The same GameTests with optional mods (emi, jei, viewers, farmersdelight, thirst, curios, all, doggy) | All required tests pass |
| `captureRestartTest` | M8 capture/release stopped at eleven ledger boundaries, reconciled in a second JVM, plus a corrupt ledger | Exactly one animal or a recoverable payload, never two |
| `familiarRestartTest`, `magicToolsRestartTest`, `commerceRestartTest` | M9-M11 durable boundaries across two JVMs | Their validators (READMEs under `tools/qualification/restart/`) |
| `saveUpgradeTest -PsaveUpgradeFixtures=<dir>` | The owner's historical worlds opened in three candidate JVMs | compare.mjs finds no unreviewed change |
| `qualifyDedicatedServer` | Installed Forge 47.4.10 server with the reobfuscated jar: alone (every block id placed, read back after restart), with the optional mods' release jars, with Doggy Talents Next, with KubeJS | Every boot reaches Done, every marker prints, exit 0, no Herbal Witch exception |
| `qualifyPerformance` | Frozen workloads, matched vanilla control, restart indexing | report.mjs gates |

Local fast gate: art producers, `runData` twice (byte-identical), `test build`, the Node suites,
`git diff --check`, `tools/sync-mod-files.ps1 -Check`, `dk check`.

## Results for the candidate

Candidate `1.0.0-rc.1` at 1b74146. The production jar has not changed since f5f1107
(`git diff f5f1107 1b74146 -- src/main` is empty); later commits change GameTests, the GameTest world and docs.
So `qualifyDedicatedServer`, which boots the reobfuscated production jar on the installed Forge server, stands
from f5f1107, and every harness that runs inside a GameTest JVM ran again at 1b74146 in the superflat world.
ANU job ids:

| Harness | Commit | Job | Result |
|---|---|---|---|
| `test build runGameTestServer` (fresh world) | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204128-5da | 525 JUnit, build, all 357 GameTests passed |
| profile emi | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204132-853 | all 357 passed |
| profile jei | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204135-5f2 | all 357 passed |
| profile viewers | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204138-3cb | all 357 passed |
| profile farmersdelight | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204141-0b4 | all 357 passed |
| profile thirst | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204144-703 | all 357 passed |
| profile curios | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204146-b5f | all 357 passed |
| profile all | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204149-bde | all 357 passed |
| profile doggy | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204152-6e4 | all 358 passed |
| `familiarRestartTest magicToolsRestartTest commerceRestartTest captureRestartTest` | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204154-9b6 | familiar 76 checkpoints across distinct JVMs; physical tools; commerce 25 checks; capture 11 boundaries, 5 quarantined and recovered once, corrupt ledger preserved, ordinary animals admitted |
| `qualifyPerformance` | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204157-aef | every frozen gate passed ([performance.md](performance.md)) |
| `saveUpgradeTest` (0.2.0-dev.9 and 0.6.1 worlds) | 1b74146 | HerbalWitch-1b7414627fc396af96ea98f0-20261023-204200-14f | 0.2.0-dev.9: 1,809 blocks, 49 block entities, 1 player; 0.6.1: 472 blocks, 65 block entities, 1 player; three JVMs each, no unreviewed change |
| `qualifyDedicatedServer` (base, optional, doggy, kubejs) | f5f1107 | HerbalWitch-m12-release-qualificatio-20261023-164001-4e7 | every boot reached Done, every marker printed, no Herbal Witch exception |
| Local fast gate | 1b74146 | Valhalla | `test build` green (90 Node art tests), 4 qualification Node tests, `git diff --check`, sync check, `dk check` clean |

Jars: ANU builds the candidate as SHA-256 `fdb4f17c...2667` (the same at f5f1107 and later), Valhalla as
`2cc2b6e8...e08e`. Every entry is byte-identical; only the reobfuscation step's MANIFEST.MF entry timestamp
differs. A published jar should still be the one built from the tagged commit and hashed then.

Intermittent GameTests met during M12, each root-caused and fixed (test-environment or test defects, none in
production code; repros in the commit messages):

- The GameTest world was normal terrain, so every plot sat in a deepslate cave: gravel fell into a capture
  release cell (curios ...195136-111; most likely also the earlier undiagnosed refusal ...175808-c88),
  cave mobs and terrain broke villager and familiar tests. Fixed by a superflat, no-spawn world (e47753c,
  d39767f) with the plots that hold roaming mobs penned (1b74146). `TestWorldGameTests` guards it.
- Neighbours' wide entity sweeps deleted other tests' mobs and drops (f5f1107).
- A fresh plot is not entity-ticking on its first tick, which hid harvest drops and a spawned cow (c50cd5c,
  0670804; confirmed by an ItemEntity join spy, c8b59f8 ...183255-e1b).
- The offline-rest check landed on the heal boundary tick (b90d9e3).
