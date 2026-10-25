---
id: dk-68afcef1
type: task
created: 2026-10-25
status: todo
since: 2026-10-25
area: structure
priority: P2
rank: zzzz
parent:
fixes: []
blocked_by: []
relates: []
---
# One RecipeRoles traversal for guide, grimoire, EMI and JEI presenters

Baseline review 2026-10-25 (abstraction). ProcessRecipeDescription is walked into roles four times: guide ProcessingRecipeRenderer.java:26-30, RecipeBody.java:82-100, EmiProcessRecipe.java:30-53, JeiProcessCategory.java:71-76. Provide one ordered role traversal feeding all four.
