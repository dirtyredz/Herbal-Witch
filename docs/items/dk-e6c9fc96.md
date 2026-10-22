---
id: dk-e6c9fc96
type: task
created: 2026-10-22
status: todo
since: 2026-10-22
area: structure
priority: P2
rank: zt
parent:
fixes: []
blocked_by: []
relates: []
---
# Grimoire registries: feature-naming Order enum, five static hooks

HW review round 2.
GrimoireContent.Order (:33) enumerates every feature in the core, so M8/M9 edit core; RecipeReferences.PROVIDERS and DiscoveryForms.FORMS are plain non-concurrent collections; ServerGrimoireContent has two setter seams. Direction: GrimoireSources/GrimoireContributions holder with per-feature priority, thread-safe, explicit ordering; GrimoireContent keeps the immutable index.
