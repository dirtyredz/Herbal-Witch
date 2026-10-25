---
id: dk-5c5fd8ab
type: bug
created: 2026-10-25
status: done
since: 2026-10-25
area: persistence
priority: P1
rank: zzzzzzn
parent:
fixes: []
blocked_by: []
relates: []
---
# Verify familiar integrated-host receipts and finish baseline guards

Baseline review fixes: reproduced integrated-host familiar receipt precedence, shared host-aware player checkpoints and clone copying with commerce, localised guide ingredient/base/container role labels, refreshed STRUCTURE and dependency guards. Receipt schemas and feature failure policies remain unchanged.

Verification on 2026-10-25:
- Regression at 0724972 fails because level.dat Player is stale while playerdata has the expected receipt; local JUnit reports expected false but was true. ANU reproduction: HerbalWitch-baseline-fixes-20261025-175710-510 (2 tests, 1 expected failure).
- Implementation at b8c407f passes local test build verifyReleaseJar --offline (542 unit tests, 92 asset/tool tests).
- ANU build: HerbalWitch-baseline-fixes-20261025-175928-0eb, PASS (542 unit tests, build and release jar verification).
- ANU GameTests: HerbalWitch-baseline-fixes-20261025-175955-4cb, PASS (all 357 required tests, one full run in isolated world baseline-fixes-20261025).
- ANU used an existing locally cached GuideME 20.1.15 jar with matching SHA-256 CF8052AB3DA121012EFC59A20E731C24BFE7488DFA556C9B88809F42DEA3B24A, staged in build/baseline-fixes-inputs; no internet downloads.
- git diff --check, dk check and sync-mod-files.ps1 -Check passed. All 192 declared Java layout homes exist.

The actual integrated-server lifecycle remains owner in-game acceptance; filesystem regression coverage proves host precedence and marker verification. Existing owner acceptance item dk-b002e503 remains open. Deferred baseline debt and RecipeRoles traversal are unchanged.