---
id: dk-b0d86cfb
type: task
created: 2026-10-22
status: done
since: 2026-10-22
area: structure
priority: P1
rank: y
parent:
fixes: []
blocked_by: []
relates: []
---
# WardService does five jobs

HW review round 1 (componentization).

magic/ward/service/WardService.java:37 (321 lines, static): the INDEX and lifecycle, focus detection (hearth/stove scan), ~15-rule activation refusal policy, owner controls (deactivate, release, radius, profile), per-tick charge and sweep.
Direction: WardActivation (refusal, activate, activateByRitual, charge), WardControls, WardFocus; WardService keeps the index and ticking.

## Implementation outcome (2026-10-22)

Split WardActivation, WardControls and WardFocus from WardService; menu and Home Protection call their owners directly. WardService retains its index and bounded ticking; WardLifecycle remains sole reconciliation authority. Added four characterization GameTests before extraction for unlit focus/radius, refusal/caps/routes and unload/expiry; compiled before and after. Offline fast gate passed, with unchanged resource/art hashes on both datagen runs. Runtime GameTests are assigned to the orchestrator.
