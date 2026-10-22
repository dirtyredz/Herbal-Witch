---
id: dk-9aa8a47a
type: task
created: 2026-10-22
status: todo
since: 2026-10-22
area: structure
priority: P2
rank: t
parent:
fixes: []
blocked_by: []
relates: []
---
# Brewing registry catch-alls: PreparationRegistration wiring, AdvancedPreparations id switch

HW review round 1.

- PreparationRegistration.registerAdvanced():62 registers 9 effect listeners, clears projectile limits, installs PvP into EntityActionPermission. Direction: brewing/effect/EffectRegistration.
- AdvancedPreparations.java:55 maps 16 products by string switch on spec.id(); throwables() re-derives delivery kind. Direction: delivery kind on ProductSpec/catalogue; registry loop dispatches on it.
