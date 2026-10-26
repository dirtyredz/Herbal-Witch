---
id: dk-8d6e26aa
type: task
created: 2026-10-26
status: todo
since: 2026-10-26
area:
priority: P2
rank: zzzzn
parent:
fixes: []
blocked_by: []
relates: []
---
# Extract deterministic stored-gzip writer from PonderStructureProvider

Source: 2026-10-26 push review (componentization lens). data/client/grimoire/ponder/PonderStructureProvider.java (~107) both composes the structure template and hand-writes a deterministic gzip encoder (stored deflate blocks, CRC32, LE trailer), whose contract GOTCHAS pins (never NbtIo.writeCompressed). Direction: extract a StoredGzip utility, unit-tested without Minecraft; provider keeps composition only.
