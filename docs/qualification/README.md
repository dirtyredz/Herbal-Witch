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

RESULTS_TABLE
