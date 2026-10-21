# STRUCTURE - The Herbal Witch

<!-- Last full review: never -->

Code map. Behaviour: [README.md](README.md). How it works: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
The whole-mod module map (where later milestones land) is in docs/ARCHITECTURE.md.

## Layout

- `src/main/java/io/github/dirtyredz/herbalwitch/` — mod bootstrap (`HerbalWitch.java`) only
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/catalog/` — plain descriptors: the single cottage finish, the five furniture pieces
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/registry/` — deferred registers, catalogue-to-block construction, creative tab, furniture tags
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/block/` — furniture block adapters: facing/shape, wall-mounted shelf, table lamp
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/shape/` — collision/selection geometry
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/showcase/` — the decor showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/catalog/` — plain herb descriptors: the 14 herbs, habitats (groups), traits
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/registry/` — herb plants, seeds, fresh herbs, herb bed, tags, gardening tab, bootstrap
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/crop/` — herb plant blocks, seed/fresh items, growth rules, harvest by hand, tooltips
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/garden/` — the herb bed, its farmland-style moisture and its joining to neighbouring beds
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/conditions/` — reloadable discovery/yield conditions: model, reload listener, live evaluator
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/foraging/` — additive grass foraging (global loot modifier) and its eligibility
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/worldgen/` — herb patch feature keys and bootstraps, biome modifiers
- `src/main/java/io/github/dirtyredz/herbalwitch/botany/showcase/` — the herb and garden showcase sections
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/permission/` — the shared authorization boundary for custom world changes
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/geometry/` — shape rotation shared by furniture and stations
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/multiblock/` — multiblock cell layout and lifecycle (placement, removal, shared state) used by the rack, hearth and stove
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/compat/` — optional server adapters, loaded only when their mod is present (`thirst/`)
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/catalog/` — the five station identities
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/registry/` — station blocks, items, block entities, menus, recipe types, the apothecary tab, bootstrap and showcase registration
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/process/` — shared processing by composition: inventory with in-slot reservations, job, completion arithmetic, recipe format, reload-aware lookup
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/block/` — `StationBlock` and `StationEntity`: block behaviour shared by stations (facing, support, menu, ticker, drops)
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/menu/` — `StationMenu`: menu behaviour shared by stations (slots, quick-move that respects reservations)
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/heat/` — heat sources and conductors
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/showcase/` — the parameterised station showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/stations/everyday/` — the everyday stations, one package each (`drying_rack`, `mortar`, `kettle`, `hearth`, `stove`, `infusion_pot`; `block/`, `menu/`, `recipe/`)
- `src/main/java/io/github/dirtyredz/herbalwitch/brewing/product/` — preparations: `catalog/`, `registry/`, `item/`, `block/` (hung bundles), `lighting/` (oil lantern), `metadata/` (water-to-drink data), `foraging/` (resin), `showcase/`
- `src/main/java/io/github/dirtyredz/herbalwitch/brewing/application/` — what products do when used, once, server side
- `src/main/java/io/github/dirtyredz/herbalwitch/brewing/effect/` — the warmth and pest-repellent effects
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/command/` — `/herbalwitch showcase` registration and execution
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/layout/` — section contract, registry, placements and the bounded plan (no world writes)
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/construction/` — preflight checks, the builder and sign labels (the only world writes)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/` — client-only mod-bus wiring (`HerbalWitchClient`) only
- `src/main/java/io/github/dirtyredz/herbalwitch/client/stations/` — station screens and shared widgets
- `src/main/java/io/github/dirtyredz/herbalwitch/client/compat/` — EMI (`emi/`) and JEI (`jei/`) plugins over one shared card layout
- `src/main/java/io/github/dirtyredz/herbalwitch/data/` — datagen provider registration only
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/language/` — the sole `en_us.json` writer
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/decor/` — furniture blockstates, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/botany/` — herb stage models and states, item models, names and tooltips
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/botany/garden/` — the herb bed's multipart state and moist soil parts
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/stations/` — station blockstates, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/preparations/` — bundle and lantern models, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/showcase/` — showcase messages, headings and labels
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/tags/` — the sole block and item tag writers
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/recipes/` — the sole recipe provider
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/loot/` — the sole global loot modifier provider
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/decor/` — furniture recipes, loot, block and item tag contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/botany/` — herb recipes, loot, tags, biome tags, worldgen, conditions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/stations/` — station crafting, loot and tags; `recipes/` the processing recipe providers
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/preparations/` — lantern crafting, loot, tags, entity tags
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/compat/` — tags offered to optional mods (Farmer's Delight heat)
- `src/main/resources/META-INF/` — `mods.toml` and mod metadata
- `src/main/resources/assets/herbalwitch/models/block/decor/templates/` — furniture templates (compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/botany/garden/` — the herb bed assembly and its parts (`parts/wood/`, `parts/soil/`; generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/botany/` — herb models `<group>/<herb>/{young_0..2,mature}.json` (generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/preparations/templates/` — the oil lantern templates (compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/preparations/bundles/` — hung bundle models `<group>/<herb>/{ceiling,wall}.json` (generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/stations/` — station models, one directory per station (`base`, or `part_<n>`, overlays and `assembly` for multiblocks; compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/textures/block/material/` — the shared untinted materials: furniture fabrics and the house materials (generated), plus `rug` and `lamp_shade` (painted in their Blockbench projects)
- `src/main/resources/assets/herbalwitch/textures/block/botany/` — the swatch atlas `parts.png` every herb and dried bundle is built from, and the bed soil (generated)
- `src/main/resources/assets/herbalwitch/textures/block/stations/` — shared station materials (generated)
- `src/main/resources/assets/herbalwitch/textures/gui/stations/` — station interface atlases (generated)
- `src/main/resources/assets/herbalwitch/textures/item/botany/` — seed and fresh-herb sprites `<group>/<herb>/` (generated)
- `src/main/resources/assets/herbalwitch/textures/item/preparations/` — bundle, powder and product sprites (generated)
- `src/generated/resources/` — datagen output, checked in and packaged (its `.cache/` is ignored and excluded); never hand-edited
- `src/art/blockbench/` — Blockbench sources, never packaged: `catalog.json` (the project list, pinned format, owned model directories) and `<stations|decor|preparations>/<piece>/` with `<piece>.bbmodel` and `piece.json` (docs/BLOCKBENCH.md)
- `src/test/java/io/github/dirtyredz/herbalwitch/botany/` — pure JUnit tests for catalogues and conditions
- `src/test/java/io/github/dirtyredz/herbalwitch/data/` — generated-resource tests: `GeneratedResourcesTest`, per-feature manifests and tests in `art/` (the Blockbench manifest), `botany/`, `decor/`, `preparations/`, `showcase/`, `stations/`, and `fixture/` helpers (model resolution, template checks, inherited item displays)
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/geometry/` — shape rotation tests
- `src/test/java/io/github/dirtyredz/herbalwitch/showcase/` — showcase plan and coverage tests
- `src/test/java/io/github/dirtyredz/herbalwitch/stations/process/` — reservation and completion arithmetic tests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/fixture/` — shared GameTest world, player, botany, station and showcase fixtures
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/decor/` — furniture GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/botany/` — herb, garden, foraging and worldgen GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/showcase/` — showcase command, boundary and content GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/stations/` — station GameTests; `process/` the shared process behaviour tests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/preparations/` — product use and block GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/compat/` — optional-integration GameTests (profile dependent)
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/platform/` — cross-cutting block guards (occlusion)
- `src/gameTest/resources/data/herbalwitch/structures/` — GameTest structure templates (`empty`, `showcase`, `showcase_section`); never in the release jar
- `tools/` — deterministic Node tools: `generate-textures.mjs`, `generate-models.mjs`, `compile-blockbench.mjs` (also the dev-art pack), `generate-test-structures.mjs`, `preview-models.mjs`; not packaged
- `tools/image/` — PNG encode/decode, the bitmap font and contact-sheet composition shared by generators and the previewer
- `tools/preview/` — the headless model previewer: `assets/` (resource lookup, model inheritance, vanilla parents), `geometry/` (element baking, transforms, item sprites, coplanar-face detection), `render/` (cameras, rasterizer), `scenes/` (blockstate evaluation, review scenes), `report/` (coverage, the z-fighting sweep, sheets, manifest), `test/` (node --test)
- `tools/art/` — shared pixel code (`pixels`, `motifs`), furniture materials and textiles (`materials`, `patterns`), ivy and bundles (`foliage`), the procedural texture manifest (`catalogue`) and ownership-checked output writing shared by the three producers (`output`)
- `tools/art/botany/` — herb sprites per habitat group, the herb list, the parts atlas, garden soil
- `tools/art/stations/` — the house-style station materials (`house`, `stone`), lantern materials and interface atlases (`interfaces.mjs` holds the layout)
- `tools/models/` — the procedural model code: `geometry` (boxes, planes, position UVs, buried-face removal), `model-output` (model JSON serialisation shared with the Blockbench compiler), `textures`, `catalogue` (families and owned directories); `botany/` (bed, herbs, coplanar separation for organic models), `preparations/` (dried bundles, also the Blockbench attachments)
- `tools/art/preparations/` — preparation materials and product sprites
- `tools/blockbench/` — the Blockbench compiler: `project` (the pinned .bbmodel contract), `catalogue` (catalog.json, piece.json), `textures` (owned and shared slots), `assembly` (state outputs, multiblock cells), `attachments` (generated bundles), `compile`
- `tools/blockbench/migration/` — importer from model JSON and the appearance-parity comparator
- `tools/blockbench/dev-art/` — the live dev-art resource pack: in-memory staging, validation, publication into a trial instance, save watching
- `tools/blockbench/test/`, `tools/blockbench/dev-art/test/` — node --test suites and their `fixtures/`
- `gradle/` — `art.gradle` (the art pipeline tasks); the wrapper comes from the workspace template
- `docs/` — architecture, decisions, gotchas, features, art, compatibility; `docs/catalogue/` traces the design to implementation

## Components

| Component | Files | Responsibility |
|---|---|---|
| Design catalogue | `decor/catalog/Finish`, `FinishCatalog` (one entry, `COTTAGE`), `FurnitureSpec`, `ArtSpikeCatalog` | The single cottage design and the five pieces; ids `decor/<family>/<piece>` |
| Decor registration | `decor/registry/*`, `decor/block/*`, `decor/shape/ArtSpikeShapes` | Catalogue -> blocks + items, creative tab, tags; facing/shape, wall shelf, table lamp; rug is vanilla `CarpetBlock` |
| Botany | `botany/**` | Herb catalogue, plants, bed, growth, harvest, conditions reload, foraging, worldgen |
| Station base | `stations/block/StationBlock`, `StationEntity`, `stations/menu/StationMenu` | Shared block and menu boilerplate (facing, support, ticker, drops, slot rules) |
| Processing | `stations/process/*` | Composition, not a superclass: inventory with reservations, job, completion, recipe format and index |
| Stations | `stations/everyday/<station>/{block,menu,recipe}`, `stations/registry/*`, `stations/heat/*` | The five stations, their registration and heat rules |
| Products | `brewing/product/**`, `brewing/application/*`, `brewing/effect/*` | Bundles, powders, drinks, salve, oils, garden products, oil lantern, effects |
| Metadata carrier | `brewing/product/metadata/DrinkMetadata` | Water-to-drink data hand-off; the Thirst adapter installs it |
| Showcase | `showcase/{layout,construction,command}`, `*/showcase/` sections | Plan (pure), preflight and builder (world writes), command |
| Permission | `platform/permission/*` | `ActionContext` + `ActionPermissionService` |
| Compat | `platform/compat/**`, `client/compat/**`, `data/server/compat/` | Thirst adapter; EMI/JEI plugins; Farmer's Delight tags |
| Client | `client/HerbalWitchClient`, `client/stations/*` | Station screens and widgets; no colour handlers |
| Datagen | `data/**` | Models, states, names, recipes, loot, tags, worldgen, conditions |
| Art pipeline | `tools/generate-textures.mjs`, `tools/art/**`, `tools/generate-models.mjs`, `tools/models/**`, `gradle/art.gradle` | Seeded generation of materials, sprites, GUI atlases and the procedural models (herbs, beds, hung bundles); producers own disjoint paths |
| Blockbench pieces | `src/art/blockbench/**`, `tools/compile-blockbench.mjs`, `tools/blockbench/**` | Hand-edited one-off pieces (stations, furniture, lantern) compiled to models and owned textures; the dev-art pack |
| Art review | `tools/preview-models.mjs`, `tools/preview/**` | Headless previews, coverage and reference comparisons |
| Multiblocks | `platform/multiblock/*`, `stations/block/StationBlock` | Cell layout, placement and removal, the origin cell, shared lit/active state |

## Dependency direction

- Features (`decor`, `botany`, `stations`, `brewing`, `platform`) never import `client/` or `data/`;
  client and datagen import features.
- `stations` -> `brewing` (preparations, metadata) and `botany`; `brewing` -> `botany`. Nothing imports
  `stations` except `data/`, `client/` and the showcase wiring. `platform/compat` -> `brewing`.
- `decor` registry -> block, shape, catalog; blocks take their shapes as constructor arguments.
- `catalog` packages import only plain Minecraft value types (e.g. `MapColor`), so the pure unit tests
  load them without a Minecraft bootstrap.
- Future features follow the same rule.

## Structural rules

- Java root holds bootstrap only; `client/` and `data/` roots hold wiring/registration only.
- About 800 lines per code file at most; review for extraction around 600.
- At most 12 direct source files per responsibility directory. Split a growing directory by
  responsibility, never into numbered batches. Stations sit under `stations/everyday/` for this reason.
- Every generated directory has fewer than 12 entries (`GeneratedResourcesTest` enforces it).
- GameTests stay in `src/gameTest/`, outside the release source set.
- No RedzCore dependency and no shared runtime library (docs/DECISIONS.md).

## Structural notes

- `ArtSpikeRegistration` is the only place that knows which block class a piece uses; Milestone 5
  replaces it with a full-catalogue registration rather than growing it.
- Stations share `StationBlock`/`StationMenu` for block and menu boilerplate only. Processing rules
  live in `stations/process/` and are used by composition: a station owns a `ProcessStation`, and no
  station extends a processing base class.
- `GeneratedResourcesTest` and the per-feature manifests keep their own independent lists on purpose,
  so a catalogue slip shows up as a diff.
