# STRUCTURE - The Herbal Witch

<!-- Last full review: never -->

Code map. Behaviour: [README.md](README.md). How it works: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
The whole-mod module map (where later milestones land) is in docs/ARCHITECTURE.md.

## Layout

- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/content/input/` — neutral herb summaries with fixed protocol-4 preference codes
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/content/recipe/` — ordered queries over feature-registered live recipe views
- `src/test/java/io/github/dirtyredz/herbalwitch/structure/` — dependency direction and durable-mutation boundary guards

- `src/main/java/io/github/dirtyredz/herbalwitch/platform/time/` — tick constants and explicit ceiling/fractional duration formatting
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/player/` — server-lifecycle online roster and independently scoped fake registrations
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/platform/player/` — roster precedence, removal and lifecycle contracts
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/magic/witch/status/` — test-only durable-state setup helpers
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/recipe/` — immutable processing facts, live `RecipeView` and ordered ingredient queries
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/inventory/` — deterministic missing-ingredient allocation
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/time/` — duration boundaries and fractional text
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/recipe/` — processing requirement codec and bounds
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/inventory/` — allocation characterization
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/stations/catalog/` — registry-free category facts
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/platform/recipe/` — registered ingredient semantics and ordering
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/grimoire/content/` — entry flags, defensive copies and link order

- `src/main/java/io/github/dirtyredz/herbalwitch/` — mod bootstrap (`HerbalWitch.java`) only
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/catalog/` — plain descriptors: the single cottage finish, the piece descriptor, behaviours, mounts, the whole catalogue
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/catalog/pieces/` — the seven family catalogues (94 pieces)
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/registry/` — deferred registers (blocks, items, block entities, menus, the seat entity), the behaviour-to-block factory, bootstrap, creative tab, furniture tags
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/structure/` — what every piece shares: the assembly block, layouts, support, access, the checked placement item
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/structure/shape/` — collision/selection geometry cut per cell and facing; `families/` the authored boxes per family
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/behavior/` — one implementation per behaviour: `seating/`, `storage/` (`display/`), `sleeping/`, `openings/`, `lighting/`
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/showcase/` — the decor showcase section, its footprint packing and its samples
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/placement/` — pure placement resolution, world checks and the transactional commit for multi-cell pieces
- `src/main/java/io/github/dirtyredz/herbalwitch/client/decor/` — decor client wiring; `placement/` (the preview), `storage/` (the storage screen, the display renderer)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/decor/state/` — blockstate mapping per behaviour (furniture, assemblies, lights, beds, doors)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/decor/recipes/` — the family recipe contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/` — grouping parent only: botany, brewing and stations; no implementation in the parent
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/catalog/` — plain herb descriptors: the 14 herbs, habitats (groups), traits
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/registry/` — herb plants, seeds, fresh herbs, herb bed, tags, gardening tab, bootstrap
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/crop/` — herb plant blocks, seed/fresh items, growth rules, harvest by hand, tooltips
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/garden/` — the herb bed, its farmland-style moisture and its joining to neighbouring beds
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/conditions/` — reloadable discovery/yield conditions: model, reload listener, live evaluator
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/foraging/` — additive grass foraging (global loot modifier) and its eligibility
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/worldgen/` — herb patch feature keys and bootstraps, biome modifiers
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/showcase/` — the herb and garden showcase sections
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/permission/` — the shared authorization boundary for custom world changes; `entity/` (harmful entity actions, claim-mod adapters, harm attribution, the ward protection check)
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/geometry/` — shape rotation shared by furniture and stations
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/multiblock/` — multiblock cell layout and lifecycle (placement, removal, shared state) used by the rack, hearth and stove
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/compat/` — optional server adapters, loaded only when their mod is present (`thirst/`; `curios/` the compile-only Curios charm-slot adapter)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/catalog/` — the ten station identities (six everyday, four advanced) and processing categories
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/registry/` — station blocks, items, block entities, menus, recipe types, the apothecary tab, bootstrap and showcase registration
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/` — shared processing by composition: inventory with in-slot reservations, job (schema 2), completion arithmetic, status
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/recipe/` — the recipe type, its JSON and wire format, the reload-aware index and the requirements (witch flag, energy)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/matching/` — recipe matching against an inventory
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/execution/` — the reservation and completion commit, and the ordinary/magical/either execution policy
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/authorization/` — who may start, resume and cancel a witch recipe: the operator, the magical policy (range 8) and the rate-limited operations
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/block/` — `StationBlock`, `StationEntity`, `ProcessStationEntity` and `StationMenu`: block behaviour shared by stations (facing, support, menu, ticker, drops; the process-station base)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/heat/` — heat sources and conductors
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/showcase/` — the parameterised station showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/everyday/` — one flat package per everyday station (`drying_rack`, `mortar`, `kettle`, `hearth`, `stove`, `infusion_pot`), containing its block, menu and recipe adapters
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/advanced/` — shared `AdvancedStationBlock` and `AdvancedStationEntity`, then one flat package per station (`cauldron`, `still`, `bottling_bench`, `charm_workbench`)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/product/` — preparations: `catalog/`, `registry/`, `item/`, `block/` (hung bundles), `lighting/` (oil lantern), `metadata/` (water-to-drink data), `foraging/` (resin), `showcase/`; Milestone 7 adds the advanced, familiar and mixture catalogues with their items and registries
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/application/` — unified drink/oil application tables, thrown preparations, containers and the familiar application seam
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/effect/` — `TimedEffect` lazy effect facts, ordered `EffectRegistration`, warmth/pest/plain effects; `combat/` (weapon coating), `control/` (binding, sleep, steadfast, concealment, dazzled), `repellent/` (target profiles and filters)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/projectile/` — thrown preparations: the projectile entity, its impact and the in-flight limits
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/catalog/` — plain identities: the book, the two research blocks, the reader menu
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/registry/` — book, research blocks and reader menu registration; event wiring, the Apothecary tab contribution, the showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/access/` — ways to open a reader: `item/` (the grimoire), `block/` (the shared research block and its shapes), `menu/` (the zero-slot reader menu and its validated access)
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/content/` — typed entry keys, entries, the immutable content index, the source contract and the server's current content; `recipe/` (live recipe projection), `reference/` (guides, herbs, stations, herb forms), `reload/` (editorial definitions and their reload listener)
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/knowledge/` — how knowledge is gained: `discovery/` (capability, codec, lifecycle, acquisition), `research/` (familiarity and eligibility, research), `teaching/` (invitations and consent)
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/reading/` — reader sessions and server-computed counts; `ingredients/` (requirements and the pure allocator)
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/network/` — the grimoire's bounded messages, their codecs and content sync
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/showcase/` — the grimoire showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/network/` — the mod's channel, payload bounds and chunked snapshot assembly (no gameplay policy)
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/recipe/` — immutable recipe descriptions and requirements, shared by processing and its readers
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/inventory/` — deterministic missing-ingredient allocation, shared by the reader and altar offerings
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/grimoire/` — herb page contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/grimoire/` — preparation guidance contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/grimoire/` — station pages and live recipe projections
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/grimoire/` — furniture page contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/witch/grimoire/` — witchcraft page contributions
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/recipe/` — recipe requirements and codec tests
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/inventory/` — the unchanged allocator suite
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/witch/` — witch status and energy (Milestone 6): `status/` (state, codec, capability, lifecycle, the status service and private sync), `energy/` (balance, arithmetic, practice, recovery, sleep, drinks), `initiation/` (consent; `founding/` the world record), `config/` (server rules, including `WorkingRules`), `network/` (snapshot, rules), `command/`, `registry/`
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/ritual/` — the functional altar (Milestone 6): `catalog/`, `registry/`, `altar/` (block, origin entity, shapes), `menu/` (zero-slot menu, access, screen sync), `session/` (sessions, offerings, the server lifecycle), `action/` (founding, initiation, meditation, attunement, the conversion commit, request dispatch; Garden Blessing and Home Protection), `network/`, `grimoire/`, `showcase/`; Milestone 7 adds `circle/` (the ritual circle block, layout and validation)
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/charm/` — charms (Milestone 7): `catalog/`, `item/`, `equipment/` (worn rules, charge, protection events), `registry/`, `grimoire/`, `showcase/`
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/ward/` — ward stones (Milestone 7): `catalog/`, `block/`, `menu/`, `persistence/` (records, codec, world SavedData), `service/` (chunk index, lifecycle, sweeps), `targeting/`, `registry/`, `grimoire/`, `showcase/`
- `src/main/java/io/github/dirtyredz/herbalwitch/client/placement/` — the placement preview shared by every checked structure item
- `src/main/java/io/github/dirtyredz/herbalwitch/client/witch/` — the witch client cache, HUD settings and wiring; `hud/` (the energy overlay and its pure layout)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/ritual/` — the altar screen and its client state, the aimed-target reader and the circle placement preview; `widgets/` (action, consent and circle panels)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/ward/` — the ward screen and the boundary preview
- `src/main/java/io/github/dirtyredz/herbalwitch/client/brewing/` — thrown-preparation renderers
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/magic/` — `ritual/` (altar and circle states, item models, English), `witch/` (witch English), `charm/` (item models, English), `ward/` (states, English)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/magic/` — `ritual/` (altar and circle crafting, loot, tags), `witch/` (the nature biome tag), `charm/` (tags), `ward/` (crafting, loot)
- `src/main/resources/assets/herbalwitch/models/block/ritual/` — the altar's compiled models (`part_<n>`, `active_<n>`, `assembly`; compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/ritual/circle/` — the ritual circle's 32 compiled cell models (`part_<n>`, `active_<n>`; no `assembly`, the item is a sprite). An enumerated exception to the 12-file cap: one model per cell state of the 16-cell ring
- `src/main/resources/assets/herbalwitch/models/block/ward/` — the ward stone's compiled models, `ward_stone/{base,active}.json`
- `src/main/resources/data/herbalwitch/curios/` — the Curios `charm` slot assignment for players (`entities/charms.json`); inert without Curios
- `src/main/resources/assets/herbalwitch/textures/gui/witch/` — the energy HUD, altar screen and ward screen atlases (generated)
- `src/test/java/io/github/dirtyredz/herbalwitch/magic/` — pure witch, ritual and ward tests: `witch/` (status, energy, initiation, founding, payloads), `ritual/` (sessions, circle layout, garden blessing plan), `ward/` (record codec, chunk index)
- `src/test/java/io/github/dirtyredz/herbalwitch/client/` — pure client arithmetic tests (`witch/hud/`)
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/magic/` — witch (`witch/`), altar and circle (`ritual/`), `ward/` and `charm/` GameTests
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/command/` — `/herbalwitch showcase` registration and execution
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/layout/` — section contract, registry, placements and the bounded plan (no world writes)
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/construction/` — preflight checks, the builder and sign labels (the only world writes)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/` — client-only mod-bus wiring (`HerbalWitchClient`) only
- `src/main/java/io/github/dirtyredz/herbalwitch/client/stations/` — station screens and shared widgets; `widgets/` (the magical-process panel: Start, Resume, Cancel, energy)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/compat/` — EMI (`emi/`) and JEI (`jei/`) plugins over one shared card layout; `ritual/` (the ritual card layout both viewers use)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/grimoire/` — the reader screen, the reusable book view, the station book panel, connection-local client state; `widgets/` (index, page, consent panel, buttons)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/` — datagen provider registration only
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/language/` — the sole `en_us.json` writer
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/decor/` — furniture blockstates, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/botany/` — herb stage models and states, item models, names and tooltips
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/botany/garden/` — the herb bed's multipart state and moist soil parts
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/stations/` — station blockstates, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/preparations/` — bundle and lantern models, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/showcase/` — showcase messages, headings and labels
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/grimoire/` — research block states, grimoire item models, the grimoire's English
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/grimoire/` — grimoire crafting, research block loot and tags, the editorial `grimoire_entries`
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/tags/` — the sole block and item tag writers
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/recipes/` — the sole recipe provider
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/loot/` — the sole global loot modifier provider
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/decor/` — furniture recipes, loot, block and item tag contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/botany/` — herb recipes, loot, tags, biome tags, worldgen, conditions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/stations/` — station crafting, loot and tags; `recipes/` the processing recipe providers
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/preparations/` — lantern crafting, loot, tags, entity tags
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/compat/` — tags offered to optional mods (Farmer's Delight heat)
- `src/main/resources/META-INF/` — `mods.toml` and mod metadata
- `src/main/resources/assets/herbalwitch/models/block/decor/templates/` — the five 0.1.0 furniture templates (compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/decor/pieces/` — every other piece's compiled models, `<family>/<group>/<piece>/` (`base`, `off`/`on`, or `part_<n>`, `lit_<n>`, `mirrored_<n>`, `assembly`)
- `src/main/resources/assets/herbalwitch/textures/gui/decor/` — the storage menu atlases (generated)
- `src/main/resources/assets/herbalwitch/models/block/botany/garden/` — the herb bed assembly and its parts (`parts/wood/`, `parts/soil/`; generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/botany/` — herb models `<group>/<herb>/{young_0..2,mature}.json` (generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/preparations/templates/` — the oil lantern templates (compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/preparations/bundles/` — hung bundle models `<group>/<herb>/{ceiling,wall}.json` (generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/stations/` — station models, one directory per station (`base`, or `part_<n>`, overlays and `assembly` for multiblocks; compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/grimoire/` — the stand and bookshelf models, `<piece>/base.json` (compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/textures/block/material/` — the shared house `wood`, `wood_post`, `wood_end` and `gold` the herb bed and hung bundles use (generated)
- `src/main/resources/assets/herbalwitch/textures/block/pieces/` — the textures each Blockbench piece owns, `<stations|decor|preparations>/<piece>/<texture>.png`, written by tools/compile-blockbench.mjs from the projects
- `src/main/resources/assets/herbalwitch/textures/block/botany/` — the swatch atlas `parts.png` every herb and dried bundle is built from, and the bed soil (generated)
- `src/main/resources/assets/herbalwitch/textures/gui/stations/` — station interface atlases (generated)
- `src/main/resources/assets/herbalwitch/textures/item/botany/` — seed and fresh-herb sprites `<group>/<herb>/` (generated)
- `src/main/resources/assets/herbalwitch/textures/item/preparations/` — bundle, powder and product sprites (generated)
- `src/main/resources/assets/herbalwitch/textures/item/grimoire/` — the grimoire's sprite (generated)
- `src/main/resources/assets/herbalwitch/textures/gui/grimoire/` — the reader atlas: the open book and its page sprites (generated)
- `src/generated/resources/` — datagen output, checked in and packaged (its `.cache/` is ignored and excluded); never hand-edited
- `src/art/blockbench/` — Blockbench sources, never packaged (110 projects): `catalog.json` (the project list, pinned format, owned model directories) and `<stations|decor|preparations|grimoire|ritual|ward>/<piece>/` with `<piece>.bbmodel` and `piece.json`; Milestone 5 pieces at `decor/pieces/<family>/<group>/<piece>/` (docs/BLOCKBENCH.md)
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/botany/` — pure JUnit tests for catalogues and conditions
- `src/test/java/io/github/dirtyredz/herbalwitch/data/` — generated-resource tests: `GeneratedResourcesTest`, per-feature manifests and tests in `art/` (the Blockbench manifest), `apothecary/botany/`, `decor/`, `preparations/`, `showcase/`, `apothecary/stations/`, and `fixture/` helpers (model resolution, template checks, inherited item displays)
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/geometry/` — shape rotation tests
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/multiblock/` — 3D and sparse layout tests
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/placement/` — placement resolution tests
- `src/test/java/io/github/dirtyredz/herbalwitch/decor/` — pure furniture tests: `catalog/` (the inventory), `behavior/` (seats, storage, displays, beds)
- `src/test/java/io/github/dirtyredz/herbalwitch/data/decor/manifest/` — the independent 94-piece inventory the decor resource tests read
- `src/test/java/io/github/dirtyredz/herbalwitch/showcase/` — showcase plan and coverage tests
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/` — reservation and completion arithmetic tests; `recipe/` (requirements: witch flag, energy rounding, wire and fingerprint)
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/` — preparation catalogue tests (the advanced, familiar and mixture inventories)
- `src/test/java/io/github/dirtyredz/herbalwitch/grimoire/` — pure grimoire tests: `knowledge/` (discovery state and codec, research eligibility, teaching), `reading/` (the allocator), `content/` (editorial definitions)
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/network/` — payload bounds and chunk assembly tests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/grimoire/` — grimoire GameTests: discovery, content, ingredients, access, teaching
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/fixture/` — shared GameTest world, player, botany, station, magic (`MagicFixtures`) and showcase fixtures
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/decor/` — furniture GameTests; `placement/`, `seating/`, `storage/`, `sleeping/`, `openings/`, `lighting/` per behaviour
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/apothecary/botany/` — herb, garden, foraging and worldgen GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/showcase/` — showcase command, boundary and content GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/apothecary/stations/` — station GameTests; `process/` the shared process behaviour tests; `advanced/` the four advanced stations and the witch-recipe flow
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/preparations/` — product use, block and advanced-preparation GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/compat/` — optional-integration GameTests (profile dependent)
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/platform/` — cross-cutting block guards (occlusion)
- `src/gameTest/resources/data/herbalwitch/structures/` — GameTest structure templates (`empty`, `showcase` at 128 x 10 x 64, `showcase_section`); never in the release jar
- `tools/` — deterministic Node tools: `generate-textures.mjs`, `generate-models.mjs`, `compile-blockbench.mjs` (also the dev-art pack), `generate-test-structures.mjs`, `preview-models.mjs`; not packaged
- `tools/image/` — PNG encode/decode, the bitmap font and contact-sheet composition shared by generators and the previewer
- `tools/preview/` — the headless model previewer: `assets/` (resource lookup, model inheritance, vanilla parents), `geometry/` (element baking, transforms, item sprites, coplanar-face detection), `render/` (cameras, rasterizer), `scenes/` (blockstate evaluation, review scenes), `report/` (coverage, the z-fighting sweep, sheets, manifest), `test/` (node --test)
- `tools/art/` — shared pixel code (`pixels`, `motifs`), the set's texture density rule (`density`: two texels per model unit), the procedural texture manifest (`catalogue`) and ownership-checked output writing shared by the three producers (`output`); `interface/` (the house GUI kit and the decor storage atlases); `test/` (node --test: density)
- `tools/art/content/botany/` — herb sprites per habitat group, the herb list, the parts atlas, garden soil
- `tools/art/materials/` — shared house wood and gold (`house.mjs`)
- `tools/art/interface/` — all GUI atlases (`stations`, `grimoire`, `decor`, `witch`) and the shared house GUI kit
- `tools/models/` — the procedural model code: `geometry` (boxes, planes, position UVs, buried-face removal), `model-output` (model JSON serialisation shared with the Blockbench compiler), `textures`, `catalogue` (families and owned directories); `apothecary/botany/` (bed, herbs, coplanar separation for organic models), `preparations/` (dried bundles, also the Blockbench attachments)
- `tools/art/content/preparations/` — preparation materials and product sprites; `advanced.mjs` holds advanced, familiar and mixture sprites
- `tools/art/content/magic/` — charm and circle item sprites (`sprites.mjs`)
- `tools/art/content/grimoire/` — the grimoire book sprite (`book.mjs`)
- `tools/blockbench/` — the Blockbench compiler: `project` (the pinned .bbmodel contract), `catalogue` (catalog.json, piece.json), `textures` (owned and shared slots), `assembly` (state outputs, multiblock cells and variants), `geometry/` (cell cutting and mirroring, legal item assemblies), `attachments` (generated bundles), `compile`
- `tools/blockbench/migration/` — importer from model JSON and the appearance-parity comparator
- `tools/blockbench/dev-art/` — the live dev-art resource pack: in-memory staging, validation, publication into a trial instance, save watching
- `tools/blockbench/test/`, `tools/blockbench/dev-art/test/` — node --test suites and their `fixtures/`
- `gradle/` — `art.gradle` (the art pipeline tasks); the wrapper comes from the workspace template
- `docs/` — architecture, decisions, gotchas, features, art, compatibility; `docs/catalogue/` traces the design to implementation

## Components

| Component | Files | Responsibility |
|---|---|---|
| Design catalogue | `decor/catalog/*` (`FinishCatalog` one entry, `FurnitureSpec`, `FurnitureBehavior`, `MountSpec`, `FurnitureCatalog`), `decor/catalog/pieces/*` | The single cottage design and the 94 pieces; ids `decor/<family>/<piece>`; waves as metadata |
| Decor registration and structure | `decor/registry/*`, `decor/structure/**`, `platform/placement/*` | Catalogue -> blocks, items, block entities, menus, the seat entity; the shared assembly block, support, shapes and checked placement |
| Decor behaviours | `decor/behavior/{seating,storage,sleeping,openings,lighting}` | Seats, origin inventories and displays, vanilla-compatible beds and door, lights |
| Botany | `apothecary/botany/**` | Herb catalogue, plants, bed, growth, harvest, conditions reload, foraging, worldgen |
| Station base | `apothecary/stations/block/{StationBlock,StationEntity,ProcessStationEntity}`, `apothecary/stations/block/StationMenu` | Shared block and menu boilerplate (facing, support, ticker, drops, slot rules) |
| Processing | `apothecary/stations/process/**` | Composition, not a superclass: inventory with reservations, job, completion; `recipe/` format, index and requirements, `matching/`, `execution/` policy and commit, `authorization/` operator and magical policy |
| Stations | `apothecary/stations/everyday/<station>/`, `apothecary/stations/registry/*`, `apothecary/stations/heat/*` | The six everyday stations, their registration and heat rules |
| Advanced stations | `apothecary/stations/advanced/**` | Witch's Cauldron, Copper Still, Bottling Bench, Charm Workbench on one `AdvancedStationBlock`/`AdvancedStationEntity`; witch recipes start by menu button |
| Products | `apothecary/brewing/product/**`, `apothecary/brewing/application/**`, `apothecary/brewing/effect/**`, `apothecary/brewing/projectile/*` | Bundles, powders, drinks, salve, oils, garden products, oil lantern, the 16 advanced preparations, mixtures, effects, thrown preparations |
| Charms | `magic/charm/**`, `platform/compat/curios/*` | Sachet and amulets, worn rules and charge, the Curios slot adapter |
| Ritual circle | `magic/ritual/circle/*`, `magic/ritual/action/{Circle,GardenBlessing,HomeProtection}*` | The 5x5 ring, its validation, Garden Blessing and Home Protection |
| Wards | `magic/ward/**` | `WardActivation`, `WardControls`, `WardFocus`; `WardService` index/ticks, `WardLifecycle` reconciliation, bounded target-suppression sweeps |
| Metadata carrier | `apothecary/brewing/product/metadata/DrinkMetadata` | Water-to-drink data hand-off; the Thirst adapter installs it |
| Showcase | `showcase/{layout,construction,command}`, `*/showcase/` sections | Plan (pure), preflight and builder (world writes), command |
| Permission | `platform/permission/**` | `ActionContext` + `ActionPermissionService`; `entity/` the one boundary for harmful entity actions |
| Compat | `platform/compat/**` (Thirst, Curios), `client/compat/**`, `data/server/compat/` | Thirst adapter; EMI/JEI plugins; Farmer's Delight tags |
| Client | `client/HerbalWitchClient`, `client/stations/*` | Station screens and widgets; no colour handlers |
| Datagen | `data/**` | Models, states, names, recipes, loot, tags, worldgen, conditions |
| Art pipeline | `tools/generate-textures.mjs`, `tools/art/**`, `tools/generate-models.mjs`, `tools/models/**`, `gradle/art.gradle` | Seeded generation of materials, sprites, GUI atlases and the procedural models (herbs, beds, hung bundles); producers own disjoint paths |
| Blockbench pieces | `src/art/blockbench/**`, `tools/compile-blockbench.mjs`, `tools/blockbench/**` | Hand-edited one-off pieces (stations, furniture, lantern) compiled to models and owned textures; the dev-art pack |
| Art review | `tools/preview-models.mjs`, `tools/preview/**` | Headless previews, coverage and reference comparisons |
| Multiblocks | `platform/multiblock/*`, `apothecary/stations/block/StationBlock` | Cell layout, placement and removal, the origin cell, shared lit/active state |
| Grimoire catalogue and registry | `grimoire/catalog/GrimoireCatalog`, `grimoire/registry/*` | Permanent ids (`grimoire/book`, `stand`, `bookshelf`, menu `reader`), registration, creative tab, server event wiring |
| Grimoire access | `grimoire/access/{item,block,menu}` | The book item, the one-cell research block (no block entity, no storage), the zero-slot reader menu and the access rule (book: carried; block: loaded, within 8 blocks) |
| Grimoire content | `grimoire/content/**` | Immutable index of entries built from registered `GrimoireContentSource`s (guide, herb, station, recipe); `ServerGrimoireContent` revisions; editorial `grimoire_entries` loader |
| Grimoire knowledge | `grimoire/knowledge/{discovery,research,teaching}` | Personal discovery capability and codec, observation policy, research eligibility and service, consenting teaching (pure rules plus a server adapter) |
| Grimoire reading | `grimoire/reading/*`, `reading/ingredients/*` | Reader sessions and the server reader service; ingredient requirements and the missing-ingredient allocator |
| Grimoire network | `grimoire/network/*`, `platform/network/*` | Six bounded messages and codec on the one channel `herbalwitch:main`; content sync; payload bounds and chunk assembly |
| Station descriptions | `apothecary/stations/catalog/ProcessingCategories` | The one recipe-type-to-station mapping shared by the grimoire, EMI and JEI |
| Grimoire client | `client/grimoire/*`, `client/grimoire/widgets/*` | Two-page book screen, view model, station panel and button, client state cache, teaching panel |

## Dependency direction

- Features (`decor`, `botany`, `stations`, `brewing`, `platform`) never import `client/` or `data/`;
  client and datagen import features.
- `stations` -> `brewing` (preparations, metadata) and `botany`; `brewing` -> `botany`. Nothing imports
  `stations` except `data/`, `client/` and the showcase wiring. `platform/compat` -> `brewing`.
- `grimoire` -> `stations`, `botany`, `brewing` and `platform/network`. The feature packages
  (`stations`, `botany`, `brewing`, `platform`, `decor`) never import `grimoire`; the station screens in
  `client/stations` reach it through `client/grimoire` (the station panel). `client/` and `data/` import
  `grimoire`.
- `decor` registry -> behavior, structure, catalog; behaviours -> structure -> catalog and `platform/{multiblock,placement}`. `grimoire` -> `decor` (the furniture pages), never the reverse.
- `catalog` packages import only plain Minecraft value types (e.g. `MapColor`), so the pure unit tests
  load them without a Minecraft bootstrap.
- Future features follow the same rule.

## Structural rules

- Java root holds bootstrap only; `client/` and `data/` roots hold wiring/registration only.
- About 800 lines per code file at most; review for extraction around 600.
- At most 12 direct source files per responsibility directory. Split a growing directory by
  responsibility, never into numbered batches. Stations sit under `apothecary/stations/everyday/` for this reason.
- Every generated directory has fewer than 12 entries (`GeneratedResourcesTest` enforces it), except the twelve enumerated
  registry-derived decor directories (blockstates, item models and loot of the seating, storage, surfaces and fixtures
  families), whose file names are permanent block ids; each must hold exactly the decor manifest's files.
- GameTests stay in `src/gameTest/`, outside the release source set.
- No RedzCore dependency and no shared runtime library (docs/DECISIONS.md).

## Structural notes

- `DecorBlockFactory` is the only place that knows which block class a piece uses: one constructor per
  behaviour, never one per piece. `decor/` groups its packages into `structure/` (what every piece shares)
  and `behavior/` (one implementation per behaviour) so no level passes ten entries; the M5 plan's flat
  `decor/{block,layout,support,seating,storage,...}` siblings were a deliberate deviation (docs/DECISIONS.md).
- The family shape classes were derived once from the first-draft Blockbench projects
  (`artifacts/.../2026-10-22-herbal-witch-m5/author/gen-shapes.mjs`) and are hand-edited source now.
- `tools/art/` has eleven entries since `interface/` (the shared GUI kit) arrived; the next addition should
  group the per-feature art folders rather than add a sibling.
- Stations share `StationBlock`/`StationMenu` for block and menu boilerplate only. Processing rules
  live in `apothecary/stations/process/` and are used by composition: a station owns a `ProcessStation`, and no
  station extends a processing base class.
- Milestone 7 added no top-level Java package: charms, wards and the circle sit under `magic/`, the
  advanced stations under `apothecary/stations/advanced/`, entity permission under `platform/permission/entity/`.
  `apothecary/stations/` was at its breadth cap, so the station authorization lives inside `apothecary/stations/process/`
  (`authorization/`), beside `recipe/`, `matching/` and `execution/`; the four advanced stations share one
  `AdvancedStationBlock`/`AdvancedStationEntity` rather than one block class each (docs/DECISIONS.md).
- `models/block/ritual/circle/` holds 32 files, an enumerated exception to the 12-file directory cap: the
  ring has 16 cells with an idle and an active model each, and the compiler names them by cell.
- `GeneratedResourcesTest` and the per-feature manifests keep their own independent lists on purpose,
  so a catalogue slip shows up as a diff.
- The grimoire is grouped by concern to keep every level under about ten entries: `access/` (item,
  block, menu), `knowledge/` (discovery, research, teaching: what a player knows and may learn) and
  `reading/` (sessions, the ingredient allocator). The plan named `grimoire/{block,item,menu,discovery,
  research,teaching,ingredients}` as siblings; the grouping is a deliberate deviation (docs/DECISIONS.md).
  Teaching keeps pure rules (`TeachingService`) apart from the server adapter (`TeachingWorld`).
