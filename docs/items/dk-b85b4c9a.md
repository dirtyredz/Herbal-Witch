---
id: dk-b85b4c9a
type: task
created: 2026-10-22
status: todo
since: 2026-10-22
area: tools
priority: P1
rank: w
parent:
fixes: []
blocked_by: []
relates: []
---
# tools/art at 11 entries; GUI atlases in four homes; primitives in botany

HW review round 1.

tools/art/ has 11 entries. Atlas generators live in interface/{decor,witch}.mjs, stations/interfaces.mjs and grimoire/interface.mjs; kit in interface/kit.mjs. preparations/magic.mjs (charm/circle sprites) imports pixel primitives from botany/sprites.mjs.
Direction: every atlas under tools/art/interface/<feature>.mjs; magic.mjs to tools/art/magic/ or similar; shared pixel primitives out of botany/sprites.mjs; bring tools/art to <=10.
