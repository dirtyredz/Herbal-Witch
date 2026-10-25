---
id: dk-ed397e9e
type: task
created: 2026-10-25
status: todo
since: 2026-10-25
area: structure
priority: P1
rank: zzzzz
parent:
fixes: []
blocked_by: []
relates: []
---
# Split AltarSessionService: action-handler registry and session validator

Baseline review 2026-10-25 (componentization). magic/ritual/session/AltarSessionService.java (283 lines) mixes the session registry, the HANDLERS plugin seam, per-tick revalidation/cancellation, altar glow and Forge handlers. Extract AltarActionHandlers and AltarSessionValidator; keep the service as registry and lifecycle owner.
