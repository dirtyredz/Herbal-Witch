# STRUCTURE - The Herbal Witch

<!-- Last full review: never -->

Code map. Behaviour: [README.md](README.md). How it works: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
The whole-mod module map (where later milestones land) is in docs/ARCHITECTURE.md.

## Layout

- `src/main/java/io/github/dirtyredz/herbalwitch/` — mod bootstrap (`HerbalWitch.java`) only
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/catalog/` — plain finish and furniture descriptors: the 18 finishes, the art-spike pieces
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/registry/` — deferred registers, catalogue-to-block construction, creative tab, furniture tags
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/block/` — furniture block adapters: facing/shape, wall-mounted shelf, table lamp
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/shape/` — collision/selection geometry and its rotation
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/showcase/` — the decor showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/catalog/` — plain herb descriptors: the 14 herbs, habitats (groups), traits
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/registry/` — herb plants, seeds, fresh herbs, herb bed, tags, gardening tab, bootstrap
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/crop/` — herb plant blocks, seed/fresh items, growth rules, harvest by hand, tooltips
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/garden/` — the herb bed and its farmland-style moisture
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/conditions/` — reloadable discovery/yield conditions: model, reload listener, live evaluator
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/foraging/` — additive grass foraging (global loot modifier) and its eligibility
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/worldgen/` — herb patch feature keys and bootstraps, biome modifiers
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/showcase/` — the herb and garden showcase sections
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/permission/` — the shared authorization boundary for custom world changes
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/geometry/` — shape rotation shared by furniture and stations
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/compat/` — optional server adapters, loaded only when their mod is present (`thirst/`)
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/catalog/` — the five station identities
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/registry/` — station blocks, items, block entities, menus, recipe types, the apothecary tab, bootstrap and showcase registration
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/process/` — shared processing by composition: inventory with in-slot reservations, job, completion arithmetic, recipe format, reload-aware lookup
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/block/` — block behaviour shared by stations (facing, support, menu, ticker, drops)
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/menu/` — menu behaviour shared by stations (slots, quick-move that respects reservations)
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/heat/` — heat sources and conductors
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/showcase/` — the parameterised station showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/everyday/` — the Milestone 3 stations, one package each (`drying_rack`, `mortar`, `kettle`, `hearth`, `infusion_pot`; `block/`, `menu/`, `recipe/`)
- `src/main/java/io/github/dirtyredz/herbalwitch/brewing/product/` — preparations: `catalog/`, `registry/`, `item/`, `block/` (hung bundles), `lighting/` (oil lantern), `metadata/` (water-to-drink data), `foraging/` (resin), `showcase/`
- `src/main/java/io/github/dirtyredz/herbalwitch/brewing/application/` — what products do when used, once, server side
- `src/main/java/io/github/dirtyredz/herbalwitch/brewing/effect/` — the warmth and pest-repellent effects
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/command/` — `/herbalwitch showcase` registration and execution
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/layout/` — section contract, registry, placements and the bounded plan (no world writes)
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/build/` — preflight checks, the builder and sign labels (the only world writes)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/` — client-only mod-bus wiring (`HerbalWitchClient`) only
- `src/main/java/io/github/dirtyredz/herbalwitch/client/decor/` — furniture tint (colour) handlers
- `src/main/java/io/github/dirtyredz/herbalwitch/client/botany/` — herb bed frame tint
- `src/main/java/io/github/dirtyredz/herbalwitch/client/stations/` — station screens and shared widgets
- `src/main/java/io/github/dirtyredz/herbalwitch/client/compat/` — EMI (`emi/`) and JEI (`jei/`) plugins over one shared card layout
- `src/main/java/io/github/dirtyredz/herbalwitch/data/` — datagen provider registration only
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/language/` — the sole `en_us.json` writer
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/decor/` — furniture blockstates, finish models, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/botany/` — herb stage/bed models and states, item models, names and tooltips
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/showcase/` — showcase messages, headings and labels
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/tags/` — the sole block and item tag writers
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/recipes/` — the sole recipe provider
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/loot/` — the sole global loot modifier provider
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/decor/` — furniture recipes, loot, block and item tag contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/botany/` — herb recipes, loot, tags, biome tags, worldgen, conditions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/stations/` — station crafting, loot and tags; `recipes/` the processing recipe providers
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/preparations/` — lantern crafting, loot, tags, entity tags
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/compat/` — tags offered to optional mods (Farmer's Delight heat)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/stations/` — station blockstates, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/preparations/` — bundle and lantern models, item models, names
- `src/main/resources/` — mods.toml, pack.mcmeta, authored models (`models/block/decor/templates/`, `models/block/botany/garden/`), generated textures (`textures/block/material/`, `textures/{block,item}/botany/`)
- `src/generated/resources/` — datagen output, checked in and packaged (its `.cache/` is ignored and excluded); never hand-edited
- `src/test/java/io/github/dirtyredz/herbalwitch/` — pure JUnit tests (`decor/`, `botany/`, `showcase/`, `data/` with per-feature manifests and `data/fixture/`), no Minecraft bootstrap
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/` — Forge GameTests (`fixture/`, `decor/`, `botany/`, `showcase/`); never in the release jar
- `src/gameTest/resources/` — GameTest structure templates (`empty`, `showcase`, `showcase_section`)
- `tools/` — deterministic Node generators: textures (`generate-textures.mjs`, `art/`, `art/botany/`) and the GameTest structures; not packaged
- `docs/` — architecture, decisions, gotchas, features, art, compatibility; `docs/catalogue/` traces the design to implementation

## Components

| Component | Files | Responsibility |
|---|---|---|
| Finish catalogue | `decor/catalog/Finish`, `FinishCatalog` | The 18 coordinated finishes (7 natural, 11 painted/material); everything else iterates this list |
| Piece catalogue | `decor/catalog/FurnitureSpec`, `ArtSpikeCatalog` | Piece id, family, template, behaviour; the id/path scheme `decor/<family>/<piece>/<palette>/<finish>` |
| Registration | `decor/registry/ArtSpikeRegistration` | Catalogue x finishes -> blocks + items; finish-independent strength |
| Registers | `decor/registry/DecorBlocks`, `DecorItems`, `DecorCreativeTab`, `DecorTags` | Deferred registers, lookups by piece and finish, tab, tag keys |
| Block adapters | `decor/block/HorizontalFurnitureBlock`, `WallShelfBlock`, `TableLampBlock` | Facing + shape; wall support; surface support + server-side toggle. Rug is vanilla `CarpetBlock` |
| Shapes | `decor/shape/ArtSpikeShapes`, `ShapeRotation` | North-authored pixel boxes, turned like a blockstate y rotation |
| Client | `client/HerbalWitchClient`, `client/decor/DecorColorHandlers` | Block and item tints: index 0 frame, 1 fabric |
| Datagen | `data/**` | Finish wrapper models, states, item models, names, crafting + stonecutter restyling, loot, tags |
| Art pipeline | `tools/generate-textures.mjs`, `tools/art/{png,materials,patterns}.mjs`, `models/block/decor/templates/*.json` | 32 px neutral materials and motifs; Blockbench-style cuboid templates with explicit UVs |

## Dependency direction

`registry` -> `block`, `shape`, `catalog`; `block` -> nothing of ours (shapes arrive as constructor
arguments); `client` -> `registry`, `catalog`; `data` -> everything above. `catalog` imports only
`MapColor` from Minecraft, so the pure tests load it without a bootstrap. Nothing in `decor` imports
`client` or `data`. Future features follow the same rule: feature packages never import `client/` or
`data/`; client and datagen import features.

## Structural rules

- Java root holds bootstrap only; `client/` and `data/` roots hold wiring/registration only.
- About 800 lines per code file at most; review for extraction around 600.
- At most 12 direct source files per responsibility directory. Split a growing directory by
  responsibility, never into numbered batches.
- Generated resources stay bounded through family, piece and palette directories: every generated
  directory has fewer than 12 entries (`GeneratedResourcesTest` enforces it). Lit lamp models live in
  `.../<piece>/lit/<palette>/` for that reason.
- GameTests stay in `src/gameTest/`, outside the release source set.
- No RedzCore dependency and no shared runtime library (docs/DECISIONS.md).

## Structural notes

- `ArtSpikeRegistration` is the only place that knows which block class a piece uses; Milestone 5
  replaces it with a full-catalogue registration rather than growing it.
- Templates bind their fixed textures (fabric, brass, glass); finish wrappers bind only `frame`.
- `GeneratedResourcesTest` / `GeneratedModelsTest` keep their own independent lists of pieces and
  finishes on purpose, so a catalogue slip shows up as a diff.
