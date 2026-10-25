---
id: dk-09dcae9d
type: task
created: 2026-10-25
status: todo
since: 2026-10-25
area: structure
priority: P2
rank: zzzy
parent:
fixes: []
blocked_by: []
relates: []
---
# MagicalProcessPolicy: extract operator lease (StationOperator) and MagicalPayment; ShopStore batch indexer

Baseline review 2026-10-25 (componentization). apothecary/stations/process/authorization/MagicalProcessPolicy.java:38 mixes execution policy, operator lease state machine and energy payment via WitchStatusService. ShopStore.java:28 also owns 32-per-tick directory indexing: extract ShopShardScanner when it next changes.
