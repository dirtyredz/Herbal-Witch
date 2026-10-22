---
id: dk-3dab8e85
type: task
created: 2026-10-22
status: todo
since: 2026-10-22
area: structure
priority: P2
rank: zw
parent:
fixes: []
blocked_by: []
relates: [dk-bdb0e86e]
---
# OnlinePlayers: test registration API in production; EntityActionPermission bypasses it

HW review round 2.
- OnlinePlayers.register/Registration (:29) are public fake-player API used only by GameTests. Guard in DependencyDirectionTest or move to a gameTest helper.
- EntityActionPermission:109,138 use getPlayerList().getPlayer(uuid) instead of OnlinePlayers.find.
