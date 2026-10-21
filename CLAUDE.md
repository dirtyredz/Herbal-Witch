# CLAUDE.md - working on The Herbal Witch

Standalone mod repo inside the `minecraft-mods` workspace (see `../../CLAUDE.md`). Orientation:
[README.md](README.md), [STRUCTURE.md](STRUCTURE.md), [docs/](docs/) (start with
docs/ARCHITECTURE.md and docs/catalogue/).

- **Commit identity:** `dirtyredz <dirtyredz@live.com>`. Private development: no GitHub repo, no
  release, no pushes, and never update the Evil Eye pack's pinned jar or config from here.
- **Standalone.** No RedzCore dependency and no code shared with other mods at runtime. Patterns from
  Roundwood/DankRecipeTransfer are adapted locally, with attribution where source was copied.
- **Headless only.** Never run `runClient`. Verification, in `mods/HerbalWitch/`:
  1. `node tools/generate-textures.mjs`, `node tools/generate-models.mjs`, `node tools/compile-blockbench.mjs`
     (textures first) and `node tools/generate-test-structures.mjs`; Gradle also runs the three art
     producers before resources and datagen (`generateArt`) and their checks in `check` (`checkArt`),
     so Node 22+ must be on PATH
  2. `.\gradlew.bat runData` (run it twice when generation changed: output must be byte-identical)
  3. `.\gradlew.bat test build`
  4. `.\gradlew.bat runGameTestServer` (one GameTest server at a time across the workspace)
  5. `git diff --check`, and from the workspace root `.\tools\sync-mod-files.ps1 -Check`
  6. Optional-mod runtime profiles: `.\gradlew.bat runGameTestServer -PcompatProfile=<base|emi|jei|viewers|farmersdelight|thirst|all> -PcompatModsDir=<mods folder>`
     (the folder supplies the Farmer's Delight and Thirst jars; see docs/COMPATIBILITY.md)
  Art changes also: `node --test "tools/blockbench/test/*.test.mjs" "tools/blockbench/dev-art/test/*.test.mjs" "tools/preview/test/*.test.mjs"` and
  `node tools/preview-models.mjs --output <job folder>/previews/<name> --references <job folder>`,
  then review the sheets before any jar goes to a trial instance (docs/ART.md "Review").
  The owner checks visuals, transforms, shaders and two-client behaviour in the trial instance.
- **Generated resources are source.** Change a provider, run `runData`, commit the regenerated
  `src/generated/resources/` with the code. Never hand-edit generated JSON. `GeneratedResourcesTest`
  holds the exact manifest; when a file is renamed or removed, delete `src/generated` and regenerate.
- **Art has three producers with disjoint paths** (`tools/art/output.mjs`; each also has `--check`):
  `tools/generate-textures.mjs` (every texture in `tools/art/catalogue.mjs`), `tools/generate-models.mjs`
  (herbs, herb beds, hung bundles; edit `tools/models/`) and `tools/compile-blockbench.mjs` (the
  stations, furniture and oil lantern, plus the rug and lamp-shade textures; edit the `.bbmodel` in
  `src/art/blockbench/`, owner workflow docs/BLOCKBENCH.md). Never edit model JSON. One-off pieces
  belong in Blockbench, not generator code; item display transforms live in the projects, so item
  model providers only name a parent. Never launch Blockbench or a client; the owner edits and
  checks in game, agents compile, compare and preview headless. Materials are untinted: there is no
  tint index, no colour handler and no runtime recolouring; a new look is a new texture. The house
  style and the owner's reference pieces are in docs/ART.md.
- **One catalogue.** There is a single cottage design: `decor/catalog/FinishCatalog` has one entry
  (`COTTAGE`; the `Finish` type stays so a design can return later) and pieces live only in their
  catalogue. IDs are `decor/<family>/<piece>` and permanent once a world uses them. Update the
  independent lists in the tests to match.
- **Showcase:** every new block or feature must contribute to the showcase registry and its coverage
  tests; update labels, supports, sample states/inventories, and documented bounds in the same change.
  Sections register from their feature's registration (`showcase/layout/ShowcaseRegistry`).
- **Shared datagen outputs** (en_us.json, block/item tags, recipes, global loot modifiers) each have
  one provider; features contribute to it (docs/GOTCHAS.md).
- **Layout:** every source and resource directory is declared in STRUCTURE.md `## Layout`; follow its
  structural rules (bootstrap-only roots, 12-file directories, 800-line files).
- **Version** lives only in `gradle.properties` (`mod_version`).
- Shared files (wrapper, `.gitattributes`, `.gitignore`) come from `../../tools/mod-template/`;
  never edit them here.
