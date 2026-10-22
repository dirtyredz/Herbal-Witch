---
id: dk-b8a8a00a
type: task
created: 2026-10-22
status: done
since: 2026-10-22
area: client
priority: P1
rank: zz
parent:
fixes: []
blocked_by: []
relates: []
---
# Viewer-neutral recipe text lives in client/compat ViewerLayout

HW review round 1 (componentization + abstraction).

client/compat/ViewerLayout.java:19 mixes EMI/JEI card geometry with requirements()/footer()/stacks(); client/grimoire/widgets/RecipePage.java:191 imports the optional-viewer package for requirements(); it also reads WitchRules.
Direction: move the viewer-neutral helpers beside stations/.../ProcessingCategories or a client recipe-text component; ViewerLayout keeps only card geometry.

## Implementation outcome (2026-10-22)

RecipeText now owns requirement text with explicit current multiplier, fractional-duration footer and counted stack copies; ViewerLayout is geometry only. Reader and EMI/JEI callers use the shared presentation. Three JUnit text contracts pass and two registered-stack/NBT GameTests compile after characterization before extraction. Full offline fast gate passed with unchanged resources.
