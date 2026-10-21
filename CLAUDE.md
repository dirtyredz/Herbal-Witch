# CLAUDE.md - working on The Herbal Witch

Standalone mod repo inside the `minecraft-mods` workspace (see `../../CLAUDE.md`). Orientation:
[README.md](README.md), [STRUCTURE.md](STRUCTURE.md), [docs/](docs/) (start with
docs/ARCHITECTURE.md and docs/catalogue/).

- **Commit identity:** `dirtyredz <dirtyredz@live.com>`. Private development: no GitHub repo, no
  release, no pushes, and never update the Evil Eye pack's pinned jar or config from here.
- **Standalone.** No RedzCore dependency and no code shared with other mods at runtime. Patterns from
  Roundwood/DankRecipeTransfer are adapted locally, with attribution where source was copied.
- **Headless only.** Never run `runClient`. Verification, in `mods/HerbalWitch/`:
  1. `node tools/generate-textures.mjs` and `node tools/generate-test-structures.mjs`
  2. `.\gradlew.bat runData` (run it twice when generation changed: output must be byte-identical)
  3. `.\gradlew.bat test build`
  4. `.\gradlew.bat runGameTestServer` (one GameTest server at a time across the workspace)
  5. `git diff --check`, and from the workspace root `.\tools\sync-mod-files.ps1 -Check`
  The owner checks visuals, transforms, shaders and two-client behaviour in the trial instance.
- **Generated resources are source.** Change a provider, run `runData`, commit the regenerated
  `src/generated/resources/` with the code. Never hand-edit generated JSON. `GeneratedResourcesTest`
  holds the exact manifest; when a file is renamed or removed, delete `src/generated` and regenerate.
- **Textures** come only from `tools/generate-textures.mjs` (explicit manifest). Template models in
  `src/main/resources/assets/herbalwitch/models/block/decor/templates/` are authored JSON with explicit
  UVs at one UV unit per model pixel; tint index 0 = frame, 1 = fabric; see docs/ART.md.
- **One catalogue.** Finishes only in `decor/catalog/FinishCatalog`, pieces only in their catalogue;
  update the independent lists in the tests to match. IDs (`decor/<family>/<piece>/<palette>/<finish>`)
  are permanent once a world uses them.
- **Layout:** every source and resource directory is declared in STRUCTURE.md `## Layout`; follow its
  structural rules (bootstrap-only roots, 12-file directories, 800-line files).
- **Version** lives only in `gradle.properties` (`mod_version`).
- Shared files (wrapper, `.gitattributes`, `.gitignore`) come from `../../tools/mod-template/`;
  never edit them here.
