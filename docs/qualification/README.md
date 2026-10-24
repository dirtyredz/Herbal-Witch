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

Candidate `1.0.0-rc.1`. The production jar has not changed since f5f1107 (`git diff f5f1107 b90d9e3 -- src/main`
is empty); later commits change only GameTests and docs, so the production-jar harnesses ran at f5f1107 and
the GameTest and restart harnesses ran again at the final commit b90d9e3. ANU job ids:

| Harness | Commit | Job | Result |
|---|---|---|---|
| `test build runGameTestServer` (fresh world) | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-193032-560 | 525 JUnit, build, all 356 GameTests passed |
| profile emi | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-193036-ddb | all 356 passed |
| profile jei | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-193039-203 | all 356 passed |
| profile viewers | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-193041-043 | all 356 passed |
| profile farmersdelight | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-195121-269 | R_FD |
| profile thirst | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-195130-710 | R_TH |
| profile curios | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-195136-111 | R_CU |
| profile all | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-195139-8cd | R_ALL |
| profile doggy | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-195142-fdc | R_DOG |
| `familiarRestartTest magicToolsRestartTest commerceRestartTest captureRestartTest` | b90d9e3 | HerbalWitch-b90d9e3de632387daf6bc50e-20261023-193058-067 | familiar 76 checkpoints; tools; commerce 25 checks; capture 11 boundaries, 5 quarantined and recovered once, corrupt ledger preserved |
| `saveUpgradeTest` (0.2.0-dev.9 and 0.6.1 worlds) | f5f1107 | HerbalWitch-m12-release-qualificatio-20261023-163958-807 | both fixtures: no unreviewed change, reload byte-stable, saved jobs resume |
| `qualifyDedicatedServer` (base, optional, doggy, kubejs) | f5f1107 | HerbalWitch-m12-release-qualificatio-20261023-164001-4e7 | every boot reached Done, every marker printed, no Herbal Witch exception |
| `qualifyPerformance` | f5f1107 | HerbalWitch-m12-release-qualificatio-20261023-164003-755 | every frozen gate passed (performance.md) |
| Local fast gate | b90d9e3 | Valhalla | `test build` green (90 Node art tests), 4 qualification Node tests, `git diff --check`, sync check, `dk check` clean |

Jars: ANU builds the candidate as SHA-256 `fdb4f17c...2667` (the same at f5f1107 and b90d9e3), Valhalla as
`2cc2b6e8...e08e`. Every entry is byte-identical; only the reobfuscation step's MANIFEST.MF entry timestamp
differs. A published jar should still be the one built from the tagged commit and hashed then.

Intermittent GameTests met during M12 and their causes (all test defects; repros in the commit messages):
neighbours' wide sweeps deleting entities (f5f1107); a fresh plot not entity-ticking on its first tick, which
hid harvest drops and a spawned cow (c50cd5c, 0670804); the offline-rest check landing on the heal boundary
tick (b90d9e3). The capture-release "destination" refusal seen once (...175808-c88, dk-a58050f7) did not recur in 24
repeats; its assertion now reports reach and position if it does.
