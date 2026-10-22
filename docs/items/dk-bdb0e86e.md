---
id: dk-bdb0e86e
type: task
created: 2026-10-22
status: done
since: 2026-10-22
area: structure
priority: P1
rank: zt
parent:
fixes: []
blocked_by: []
relates: []
---
# Online-player roster seam in platform

HW review round 1 (abstraction).

The online-player-by-UUID lookup lives in magic/witch/initiation/InitiationEvents.java:59 with a testPlayers list in production code; 17 callers including stations (MagicalProcessPolicy:66,95) and every ritual action. grimoire/knowledge/teaching/TeachingWorld.java:38 repeats it with its own test seam.
Direction: platform OnlinePlayers.find(UUID) owning the server reference and the test override; InitiationEvents and TeachingWorld use it.

## Implementation outcome (2026-10-22)

Installed platform/player/OnlinePlayers with one bootstrap-owned server lifecycle, UUID lookup, ordered enumeration, real-player precedence, removed-fake exclusion and independent AutoCloseable registrations. Initiation, teaching and every ritual/station lookup now use it. Teaching retains range/eligibility/name sorting. Added three isolated roster GameTests and migrated both fixture families; all GameTest sources compile. Fast gate passed (344 JUnit, 79 Node, two byte-identical datagen runs, original 2693 resource/art hashes unchanged, shared-file and whitespace checks). Runtime GameTests remain assigned to the orchestrator.
