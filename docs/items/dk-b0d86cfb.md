---
id: dk-b0d86cfb
type: task
created: 2026-10-22
status: todo
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
