---
id: dk-1a7eca23
type: task
created: 2026-10-22
status: done
since: 2026-10-22
area: structure
priority: P1
rank: zzt
parent:
fixes: []
blocked_by: []
relates: [dk-b0d86cfb]
---
# Ward package: WardService still a hub every sibling calls back into

HW review round 2, 2026-10-22 (abstraction P1, componentization P2).

WardActivation, WardControls, WardFocus static-import rules/stone/record/index/unindex/show from WardService; WardLifecycle calls WardService.index/show/entry/indexed/unindex; WardService calls WardFocus and WardLifecycle back: a package-level cycle.
Direction: WardService owns only WardIndex operations and ticking; rules() to a small WardContext/WorkingRules accessor; stone()/show() to the stone/block-entity side; index(level, record) into WardLifecycle; one-way dependencies.
