---
id: dk-b85b4c9a
type: task
created: 2026-10-22
status: done
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

## Implementation outcome (2026-10-22)

Completed the mechanical art moves and neutral drawing extraction: sprites, plant motifs, item drawing; botanical seed layouts remain feature-owned. Reader atlas now uses the interface kit at equivalent frame coordinates. All 147 procedural PNG hashes and manifest order match the frozen baseline; all 2,693 resource/art files match base. Three producers --check, 79 Node tests, two datagen passes and test build passed. Headless preview rendered 50 images covering 801/801 models with no diagnostics; relevant sheets and reader atlas inspected. tools/art now has ten direct entries.
