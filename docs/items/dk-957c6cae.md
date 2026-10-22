---
id: dk-957c6cae
type: task
created: 2026-10-22
status: done
since: 2026-10-22
area: structure
priority: P1
rank: z
parent:
fixes: []
blocked_by: []
relates: []
---
# Drink and oil applications split by milestone, not concern

HW review round 1 (componentization).

brewing/application/DrinkApplication.Drink and application/advanced/DrinkApplications.Draught are both effect-supplier/duration/amplifier enums with copied fields; SECOND lives in the old class. OilApplication/OilApplications repeat it; ThrowableApplications extends the pattern. Items, registry and showcase must know which enum a drink belongs to.
Direction: one drink application with one effect-table shape and a shared tick constant (special cases as explicit branches); same for oils.

## Implementation outcome (2026-10-22)

Unified all 11 drinks and 7 oils behind one application enum and item adapter each, sharing effect/TimedEffect. Retained explicit tea/rest/healing/coating/targeting behavior, start/finish eligibility, server application, containers, creative use and advanced-only labels. Flattened ThrowableApplications and FamiliarPreparationApplications. Added three table-driven GameTests before merging; compiled before/after. Offline fast gate passed: 354 unit tests, 79 Node tests, 191 compiled GameTests; all 2693 resource/art hashes unchanged.
