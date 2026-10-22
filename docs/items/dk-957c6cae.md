---
id: dk-957c6cae
type: task
created: 2026-10-22
status: todo
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
