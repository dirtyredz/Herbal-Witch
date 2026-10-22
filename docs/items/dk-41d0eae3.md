---
id: dk-41d0eae3
type: task
created: 2026-10-22
status: todo
since: 2026-10-22
area: structure
priority: P2
rank: zn
parent:
fixes: []
blocked_by: []
relates: [dk-0962cbb8]
---
# Witch status residue: public restore/snapshot, capability changed/disturb/setMeditating, sync pass-throughs

HW review round 2.
- WitchStatusService.restore/snapshot (:100) public; only ConversionCommit uses them. Fold into a conversion operation or make package-private.
- WitchCapability:89-126 public; EnergyEvents:64,76 call changed()/disturb(); MeditationAction:54,86 call setMeditating; WitchStatusService.of() public. Expose meditating/disturbance as service ops.
- WitchStatusService:176 resync/flush/tick/sendRules are one-line pass-throughs to WitchStatusSync; energyTea/restfulInfusion/tickCountdowns mixed into the generic API.
