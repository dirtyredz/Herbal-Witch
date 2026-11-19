# CLAUDE.md - working on The Herbal Witch

Standalone mod repo inside the `minecraft-mods` workspace (see `../../CLAUDE.md`). Orientation:
[README.md](README.md), [STRUCTURE.md](STRUCTURE.md), [docs/](docs/) (start with
docs/ARCHITECTURE.md and docs/catalogue/).

- **Commit identity:** `dirtyredz <dirtyredz@live.com>`. Private development with shared hosted CI: pushes and release dispatch follow the global rules (Claude owns the push; ordinary release dispatch needs no owner ask). Never update the Evil Eye pack's pinned jar or config from here.
- **Standalone.** No RedzCore dependency and no code shared with other mods at runtime. Patterns from
  Roundwood/DankRecipeTransfer are adapted locally, with attribution where source was copied.
- **Headless only.** Never run `runClient`. Verification, in `mods/HerbalWitch/`:
  1. `node tools/generate-textures.mjs`, `node tools/generate-models.mjs`, `node tools/compile-blockbench.mjs`
     (textures first) and `node tools/generate-test-structures.mjs`; Gradle also runs the four art
     producers before resources and datagen (`generateArt`) and their checks in `check` (`checkArt`),
     so Node 22+ must be on PATH
  2. `.\gradlew.bat runData` (run it twice when generation changed: output must be byte-identical)
  3. `.\gradlew.bat test build`
  4. `.\gradlew.bat runGameTestServer` (one GameTest server at a time across the workspace)
  5. `git diff --check`, and from the workspace root `.\tools\sync-mod-files.ps1 -Check`
  6. Optional-mod runtime profiles: `.\gradlew.bat runGameTestServer -PcompatProfile=<base|emi|jei|viewers|farmersdelight|thirst|curios|doggy|all> -PcompatModsDir=<mods folder>` (`ponder` is a separate opt-in client-animation profile, never part of `all`)
     (the folder supplies the Farmer's Delight and Thirst jars; see docs/COMPATIBILITY.md)
  Creature art also: `node tools/compile-entity-models.mjs` and its `--check`; Gradle includes both.
  Art changes also: `node --test "tools/art/test/*.test.mjs" "tools/blockbench/test/*.test.mjs" "tools/blockbench/dev-art/test/*.test.mjs" "tools/preview/test/*.test.mjs" "tools/entities/test/*.test.mjs"` (tools/qualification changes: `"tools/qualification/test/*.test.mjs"`) and
  `node tools/preview-models.mjs --output <job folder>/previews/<name> --references <job folder>`,
  then review the sheets before any jar goes to the mod's test instance (docs/ART.md "Review").
  The owner checks visuals, transforms, shaders and two-client behaviour in the mod's test instance.
- **Fast gate** (every change; under a few minutes): the art producers when their inputs changed, `runData`
  (twice, byte-identical, when generation changed), `.\gradlew.bat test build`, the Node art suites when
  art or tools changed, `git diff --check`, `sync-mod-files.ps1 -Check`. GameTests run on ANU, not locally:
  commit, then from the workspace root `tools/anu-test.ps1 -Mod HerbalWitch -Ref <branch> -Path <worktree>
  -Tasks "test runGameTestServer" -Wait` (space-separated tasks; tools/anu/README.md). A milestone wave
  closes only on the full gate plus a green ANU run and reviewed preview sheets.
- **Generated resources are source.** Change a provider, run `runData`, commit the regenerated
  `src/generated/resources/` with the code. Never hand-edit generated JSON. `GeneratedResourcesTest`
  holds the exact manifest; when a file is renamed or removed, delete `src/generated` and regenerate.
- **Art has four producers with disjoint paths** (`tools/generate-textures.mjs`, `tools/generate-models.mjs`, `tools/compile-blockbench.mjs`, `tools/compile-entity-models.mjs`); never edit model JSON or launch Blockbench or a client. Edit the owning `.bbmodel` in `src/art/blockbench/` or `src/art/entities/`. Details, preview-review commands and house style: docs/ART.md and docs/BLOCKBENCH.md.
- **Design contracts** (read the section before touching the area, all in docs/ARCHITECTURE.md "Subsystem contracts"): furniture is one `FurnitureSpec` per piece, never a block class per piece; mutate witch energy only through `WitchStatusService`; bump `HerbalWitchNetwork.PROTOCOL` on any wire change; harmful entity actions go through `EntityActionPermission`; advanced stations share one block/entity class; commerce value moves only through `ShopTransfer`; every new block/feature registers with the showcase; shared datagen outputs have one provider each (docs/GOTCHAS.md). IDs `decor/<family>/<piece>` are permanent once a world uses them.
- **Restart and release qualification harnesses are ANU-only**, outside `build`/`check`, never run locally (register: `gradle/qualification.gradle`; commands and rules: docs/qualification/README.md). Version promotion requires their green results.
- **Layout:** every source and resource directory is declared in STRUCTURE.md `## Layout`; follow its
  structural rules (bootstrap-only roots, 12-file directories, 800-line files).
- **Version** lives only in `gradle.properties` (`mod_version`).
- Shared files (wrapper, `.gitattributes`, `.gitignore`) come from `../../tools/mod-template/`;
  never edit them here.
