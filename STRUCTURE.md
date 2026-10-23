# STRUCTURE - The Herbal Witch

<!-- Last full review: 2026-10-22 (0.6.1 structural refactor; round 2 fixes applied) -->

Code map. Behaviour: [README.md](README.md). How it works: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
The whole-mod module map (where later milestones land) is in docs/ARCHITECTURE.md.

## Layout

M10 tools: `apothecary/stations/advanced/{broom_stand,wand_bench}/` owns station adapters, recipes and
repair transforms; `data/server/stations/recipes/tools/` contributes acquisition to the sole recipe
writer. `data/client/magic/tools/` owns item parents; `client/stations/tools/` owns repair presentation.
`magic/{broom,wand}/{item,registry}/` owns physical tool registration.
`src/art/blockbench/magic/{broom,wand}/` and `src/art/blockbench/stations/tools/{broom_stand,wand_bench}/`
own editable geometry and textures. Compiled handheld parents live in `models/item/magic/<tool>/`,
station models in `models/block/stations/tools/<station>/`, and textures below `textures/block/pieces/`
mirroring project directories. `tools/art/interface/tool-stations.mjs` supplies their GUI atlases.
GameTests live in `src/gameTest/java/io/github/dirtyredz/herbalwitch/apothecary/stations/tools/`.

M10 foundation: `magic/broom/catalog/` owns physical constants;
`magic/broom/persistence/` owns strict item condition data; `magic/broom/flight/` owns pure movement
and fixed-point cost arithmetic; `magic/broom/network/` owns ephemeral rider input leases.
`magic/wand/catalog/` owns stable selector IDs and `magic/wand/persistence/` owns mode preferences.
Tests mirror these responsibilities under `src/test/java/io/github/dirtyredz/herbalwitch/magic/`,
with the wand's selector tests directly in `magic/wand/`.

```text
herbalwitch/
  HerbalWitch.java
  apothecary/                 botany/ -> cultivation; brewing/ -> preparations; stations/ -> processing
  client/                    screens, rendering, recipe text and optional viewers; magic/ -> ritual, ward, witch
  companions/                animals/ care and transport; lifecycle/ loaded identities; familiars/ consume trust views
  data/                      generated resources, one writer per shared output
  decor/                     catalogue, structures and furniture behaviour
  grimoire/                  contracts, content, knowledge, reading and transport
  magic/                     witch/, ritual/, charm/, ward/, broom/, wand/
  platform/                  compat/, geometry/, inventory/, multiblock/, network/, permission/,
                             persistence/, placement/, player/, recipe/, time/
  showcase/                  planning, construction and commands
```

**src/main/java: root**

- `src/main/java/io/github/dirtyredz/herbalwitch/` — mod bootstrap (`HerbalWitch.java`) only

**src/main/java: apothecary**

- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/` — grouping parent only: botany, brewing and stations; no implementation in the parent
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/` — grouping parent; `HerbWorldgen` (herb patch feature keys and bootstraps, biome modifiers) is the only loose file
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/catalog/` — plain herb descriptors: the 14 herbs, habitats (groups), traits
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/conditions/` — reloadable discovery/yield conditions: model, reload listener, live evaluator
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/crop/` — herb plant blocks, seed/fresh items, growth rules, harvest by hand, tooltips
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/foraging/` — additive grass foraging (global loot modifier) and its eligibility
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/garden/` — the herb bed, its farmland-style moisture and its joining to neighbouring beds
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/grimoire/` — herb pages and condition-to-summary adapter
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/registry/` — herb plants, seeds, fresh herbs, herb bed, tags, gardening tab, bootstrap
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/showcase/` — the herb and garden showcase sections
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/application/` — unified drink/oil application tables, thrown preparations, containers, familiar application and guide-ownership seams
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/effect/` — `TimedEffect` lazy effect facts, ordered `EffectRegistration`, warmth/pest/plain effects; `combat/` (weapon coating), `control/` (binding, sleep, steadfast, concealment, dazzled), `repellent/` (target profiles and filters)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/grimoire/` — preparation guidance contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/product/` — preparations: `catalog/`, `registry/`, `item/`, `lighting/` (oil lantern), `metadata/` (water-to-drink data), `showcase/`, with `DriedBundleBlock` (hung bundles) and `ResinDropModifier` (resin) loose; Milestone 7 adds the advanced, familiar and mixture catalogues with their items and registries
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/projectile/` — thrown preparations: the projectile entity, its impact and the in-flight limits
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/advanced/` — shared `AdvancedStationBlock` and `AdvancedStationEntity`, then one flat package per station (`cauldron`, `still`, `bottling_bench`, `charm_workbench`)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/block/` — `StationBlock`, `StationEntity`, `ProcessStationEntity` and `StationMenu`: block behaviour shared by stations (facing, support, menu, ticker, drops; the process-station base)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/catalog/` — the ten station identities (six everyday, four advanced) and processing categories
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/everyday/` — one flat package per everyday station (`drying_rack`, `mortar`, `kettle`, `hearth`, `stove`, `infusion_pot`), containing its block, menu and recipe adapters
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/grimoire/` — brewing guidance, station pages and live recipe projections
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/heat/` — heat sources and conductors
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/` — shared processing by composition: inventory with in-slot reservations, job (schema 2), completion arithmetic, status, and `ProcessMatcher` (recipe matching against an inventory)
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/authorization/` — who may start, resume and cancel a witch recipe: the operator, the magical policy, `ProcessingRules.RANGE` (8) and rate-limited operations
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/execution/` — the reservation and completion commit, and the ordinary/magical/either execution policy
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/recipe/` — the recipe type, its JSON and wire format and reload-aware index; shared description/requirements live in `platform/recipe/`
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/registry/` — station registers, `ProcessingRecipeBindings` type lookup, the apothecary tab, bootstrap and showcase registration
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations/showcase/` — the parameterised station showcase section

**src/main/java: companions**

- `src/main/java/io/github/dirtyredz/herbalwitch/companions/` and `companions/animals/` — grouping parents only.
- `companions/lifecycle/` — loaded object-identity index and cross-dimension lookup/admission/retirement; no capture or bond records.
- `companions/familiars/` — grouping parent; `catalog/` holds permanent species/action facts, `registry/` wires contributions, `command/` owns serialized command identities.
- `companions/familiars/bond/` — binding eligibility and paid conversion; `data/` owns bounded per-bond shards and derived owner/entity indexes, `entity/` owns lightweight references and admission, `transaction/` owns durable player receipts.
- `companions/familiars/species/` — exact animal participation and payload adapters; `ground/` and `flying/` contain thin native representations and shared flight mechanics.
- `companions/familiars/ritual/` — feature-owned altar session handlers through the neutral action registry.
- `companions/familiars/menu/` and `network/` — owner-derived roster pages, nearby detail sessions and bounded request/projection messages.
- `companions/familiars/config/` — atomic bounded server rule snapshots; acquisition switches never delete bonds.
- `companions/familiars/command/` — command transitions, derived mobile reservations and loaded-only recall; `ai/` owns mutually exclusive behavior and observations, `navigation/` owns safe destinations and queued paths.
- `companions/familiars/work/` — shared fair per-dimension scan/path queues and bounded engine queries; `observation/` owns private owner/target alert throttling.
- `companions/familiars/ability/` — payment, reserve and scheduling; `sensing/` owns incremental plant/entity observations and `action/` owns concrete garden/guard effects. `observation/` also owns short-lived private action tokens; `client/familiars/state/` caches their bounded projections and `render/` draws observed positions. Tests live in `src/{test,gameTest}/.../companions/familiars/ability/`.
- `companions/familiars/home/` — shared bed/nest/perch block, identity-only block entity, canonical occupancy index, assignment, safe rest and loaded-only destinations.
- `companions/familiars/showcase/` — functional home and familiar feature sections in the witchcraft group.
- `src/{test,gameTest}/java/io/github/dirtyredz/herbalwitch/companions/familiars/home/` — compatibility, occupancy, real placement/support loss, replacement identity, pending homes and loaded rest.
- `src/art/blockbench/familiars/homes/<bed|nest|perch>/` — one-off owned-texture projects and sidecars; compiled models at `src/main/resources/assets/herbalwitch/models/block/familiars/home/<piece>/`, textures at `textures/block/pieces/familiars/homes/<piece>/`.
- `tools/preview/scenes/familiars/` — actual home blockstates in each facing and floor-support context.
- `platform/permission/PrincipalPermission` — UUID-aware automation location checks, vanilla spawn protection and additive protection policies; unsupported claims refuse automation.
- `client/familiars/` — client bootstrap; `state/` retains only the current private page and `screen/` renders roster/detail cards.
- `src/test/java/io/github/dirtyredz/herbalwitch/companions/familiars/network/` — wire limits and private projection contracts.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/familiars/{fixture,menu}/` — real bound entities and private session/nearby access checks.
- `src/test/java/io/github/dirtyredz/herbalwitch/companions/familiars/command/` — persisted reservation order, shared mobile classification, queue fairness and hard configuration bounds.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/familiars/command/` — real navigation, sleep, guard authorization, unloaded reservations and paid incarnation recall.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/familiars/config/` — live cap reduction and acquisition-only switches against real bonds.
- `data/{client,server}/familiars/` — language/entity tags, home states/item parents, empty loot and exact crafting recipes through the shared writers.
- `src/test/java/io/github/dirtyredz/herbalwitch/companions/familiars/bond/` — shard bounds, indexing, corruption, receipt and write uncertainty tests.
- `src/test/java/io/github/dirtyredz/herbalwitch/data/familiars/` — independent resource inventory.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/familiars/bond/` — real altar binding, cancellation and stale source admission tests in separate batches.
- `src/test/java/io/github/dirtyredz/herbalwitch/companions/lifecycle/` — duplicate, removed and late-leave object identity tests.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/lifecycle/` — real-entity admission, retirement and replacement tests.
- `companions/animals/catalog/`, `species/`, `config/`, `registry/` — bait identities, exact species profiles and live eligibility, validated server limits, bootstrap and tags.
- `companions/animals/trust/` — lazy Mob capability, immutable personal views, schema codec and loaded-clock lifecycle.
- `companions/animals/care/taming/` — authoritative native-owner/keeper resolution; care responsibilities group beneath `care/` to keep the animal root narrow.
- `companions/animals/care/interaction/` — one server interaction path, targeted existing salve, bait and powder; shared entity-policy contribution.
- `companions/animals/care/ai/` — removable goal instances, bounded dimension budgets, fair query queue and spatial bait-holder index.
- `companions/animals/inspection/{menu,network}/` — continuously validated zero-slot card, private bounded snapshot and adoption request.
- `companions/animals/capture/` — item token, capture/release policy and transactions; `data/` owns the sole ledger, checkpoint writer and load reconciliation; `payload/` owns exact vanilla adapters and NBT validation; `command/` owns operator recovery.
- `companions/animals/{grimoire,showcase}/` — feature-owned guidance at priority 1100 and sixteen live animal habitats.
- `src/main/java/io/github/dirtyredz/herbalwitch/client/animals/` — card presentation through the shared grimoire panel host; no server authority.
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/animals/` — language contribution and parent-only item model provider.
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/animals/` — contributions to the sole tag and processing-recipe writers.
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/tags/HerbalWitchEntityTagsProvider.java` — sole entity-tag writer; preparations and animals contribute independently.
- `src/test/java/io/github/dirtyredz/herbalwitch/companions/animals/{trust,species}/` — pure relationship, codec, eligibility and bounds tests.
- `src/test/java/io/github/dirtyredz/herbalwitch/companions/animals/{care,inspection,capture}/` — AI counters, wire bounds, ledger transitions, payload validation and persistence fault tests.
- `src/test/java/io/github/dirtyredz/herbalwitch/data/animals/` — independent animal resource manifest and exact tag matrix.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/animals/` — real-entity capability and ownership tests, compiled locally and run by the orchestrator.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/animals/fixture/` — test-only unique players and real animal setup.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/animals/care/ai/` — real bait movement and fair spatial-index tests.
- `src/art/blockbench/animals/{bait,care,capture}/<piece>/` — nine independent projects with embedded 32px textures and sidecars.
- `src/main/resources/assets/herbalwitch/{models/block/animals,textures/block/pieces/animals}/` — compiler-owned art; generated item wrappers live under `models/item/animals/`.
- `showcase/layout/ShowcaseEntitySample.java` — pure bounded entity descriptors; `showcase/construction/ShowcaseEntitySpawner.java` runs after habitats and cleans up only its own failed invocation.

**src/main/java: apothecary/shop (Milestone 11 commerce)**

- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/` — grouping parent only: apothecary commerce
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/catalog/` — registry-free piece descriptors, sale eligibility, exact metadata rules and the villager product catalogue
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/config/` — validated commerce rules and `herbalwitch-commerce-server.toml`
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/registry/` — blocks, items, block entities, the menu, server lifecycle and bootstrap wiring
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/fixture/` — grouping parent: `block/` and `link/`
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/fixture/block/` — thin physical adapters: register, displays, board, identity-only block entities, shapes
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/fixture/link/` — register binding lifecycle, fixture links, projections, interaction and Forge event adapters
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/ledger/` — canonical per-shop records, stock, offers, codec, verified shards, store and derived indexes
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/ledger/transaction/` — the one transfer path, player receipts and checkpoints, recovery decisions, reconciliation and operator commands
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/trade/` — pure inventory model and transfer plans, player transfer adapters, owner management
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/session/` — access checks, sessions, the zero-slot menu, bounded request/update messages and role-filtered snapshots
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/order/` — nonbinding customer requests: record, pure rules, server adapter
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/customer/` — real villager customer eligibility and the bounded visit scheduler
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/customer/demand/` — the commerce clock and the bounded villager spending record
- `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/guide/` — the commerce grimoire source and the inert showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/client/stations/shop/` — commerce screen, projection cache and display renderer
- `src/main/java/io/github/dirtyredz/herbalwitch/client/stations/shop/widgets/` — one panel per commerce screen tab
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/stations/shop/` — commerce blockstates, item parents and English
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/stations/shop/` — commerce crafting, loot and tag contributions to the sole writers
- `src/main/resources/assets/herbalwitch/models/block/shop/` — compiled commerce models (register closed/open, shelf, jar, board)
- `src/main/resources/assets/herbalwitch/textures/block/pieces/shop/` — textures the four commerce projects own
- `src/main/resources/assets/herbalwitch/textures/gui/shop/` — the commerce screen atlas (generated by `tools/art/interface/shop.mjs`)
- `src/art/blockbench/shop/` — the four editable commerce projects: `register`, `stock_shelf`, `stock_jar`, `order_board`
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/shop/` — pure commerce tests by responsibility (`catalog`, `ledger`, `trade`, `order`, `session`, `customer`)
- `src/test/java/io/github/dirtyredz/herbalwitch/data/stations/shop/` — the independent commerce resource manifest and resource tests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/apothecary/shop/` — commerce GameTests by responsibility; `fixture/` shared shop setup; `persistence/` restart qualification
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/apothecary/shop/ledger/transaction/` — the test-only player checkpoint fault probe (same package as the production seam)
- `tools/qualification/restart/commerce/` — the commerce restart report validator and its README
- `tools/preview/scenes/shop/` — commerce review scenes

**src/main/java: client**

- `src/main/java/io/github/dirtyredz/herbalwitch/client/` — client-only mod-bus wiring (`HerbalWitchClient`) only
- `src/main/java/io/github/dirtyredz/herbalwitch/client/brewing/` — thrown-preparation renderers
- `src/main/java/io/github/dirtyredz/herbalwitch/client/compat/` — EMI (`emi/`) and JEI (`jei/`) plugins over one shared card layout; `ritual/` (the ritual card layout both viewers use)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/decor/` — decor client wiring; `storage/` (the storage screen, the display renderer)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/grimoire/` — the reader/view, `GrimoirePanelHost` shared by four screens, book panel and connection-local state; `widgets/` (`EntryPage` shell, `RecipeBody`, index, consent and buttons)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/magic/` — grouping parent for the witchcraft client: `witch/`, `ritual/`, `ward/`
- `src/main/java/io/github/dirtyredz/herbalwitch/client/magic/ritual/` — the altar screen and its client state, the aimed-target reader and the circle placement preview; `widgets/` (action, consent and circle panels)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/magic/ward/` — the ward screen and the boundary preview
- `src/main/java/io/github/dirtyredz/herbalwitch/client/magic/witch/` — the witch client cache, HUD settings and wiring; `hud/` (the energy overlay and its pure layout)
- `src/main/java/io/github/dirtyredz/herbalwitch/client/placement/` — the placement preview shared by every checked structure item
- `src/main/java/io/github/dirtyredz/herbalwitch/client/recipe/` — recipe text and counted stack presentation shared by reader and viewers
- `src/main/java/io/github/dirtyredz/herbalwitch/client/stations/` — station screens and shared widgets; `widgets/` (the magical-process panel: Start, Resume, Cancel, energy)

**src/main/java: data**

- `src/main/java/io/github/dirtyredz/herbalwitch/data/` — datagen provider registration only
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/botany/` — herb stage models and states, item models, names and tooltips
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/botany/garden/` — the herb bed's multipart state and moist soil parts
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/decor/` — furniture blockstates, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/decor/state/` — blockstate mapping per behaviour (furniture, assemblies, lights, beds, doors)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/grimoire/` — research block states, grimoire item models, the grimoire's English
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/language/` — the sole `en_us.json` writer
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/magic/` — `ritual/` (altar and circle states, item models, English), `witch/` (witch English), `charm/` (item models, English), `ward/` (states, English)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/preparations/` — bundle and lantern models, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/showcase/` — showcase messages, headings and labels
- `src/main/java/io/github/dirtyredz/herbalwitch/data/client/stations/` — station blockstates, item models, names
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/botany/` — herb recipes, loot, tags, biome tags, worldgen, conditions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/compat/` — tags offered to optional mods (Farmer's Delight heat)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/decor/` — furniture recipes, loot, block and item tag contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/decor/recipes/` — the family recipe contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/grimoire/` — grimoire crafting, research block loot and tags, the editorial `grimoire_entries`
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/loot/` — the sole global loot modifier provider
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/magic/` — `ritual/` (altar and circle crafting, loot, tags), `witch/` (the nature biome tag), `charm/` (tags), `ward/` (crafting, loot)
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/preparations/` — lantern crafting, loot, tags, entity tags
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/recipes/` — the sole recipe provider
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/stations/` — station crafting, loot and tags; `recipes/` the processing recipe providers
- `src/main/java/io/github/dirtyredz/herbalwitch/data/server/tags/` — the sole block and item tag writers

**src/main/java: decor**

- `src/main/java/io/github/dirtyredz/herbalwitch/decor/behavior/` — one implementation per behaviour: the door (`DecorDoorBlock`) and light (`DecorLightBlock`) blocks, then `seating/`, `storage/` (`display/`), `sleeping/`
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/catalog/` — plain descriptors: the single cottage finish, the piece descriptor, behaviours, mounts, the whole catalogue
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/catalog/pieces/` — the seven family catalogues (94 pieces)
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/grimoire/` — furniture page contributions
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/registry/` — deferred registers (blocks, items, block entities, menus, the seat entity), the behaviour-to-block factory, bootstrap, creative tab, furniture tags
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/showcase/` — the decor showcase section, its footprint packing and its samples
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/structure/` — what every piece shares: the assembly block, layouts, support, access, the checked placement item
- `src/main/java/io/github/dirtyredz/herbalwitch/decor/structure/shape/` — collision/selection geometry cut per cell and facing; `families/` the authored boxes per family

**src/main/java: grimoire**

- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/access/` — ways to open a reader: `item/` (the grimoire), `block/` (the shared research block and its shapes), `menu/` (the zero-slot reader menu and its validated access)
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/catalog/` — plain identities: the book, the two research blocks, the reader menu
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/content/` — typed entry keys, entries, the immutable content index, the source contract and the server's current content; `input/` (neutral summaries), `recipe/` (registered recipe queries), `reference/` (guide pages and discovery forms), `reload/` (editorial definitions and their reload listener)
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/content/input/` — neutral herb summaries with fixed protocol-4 preference codes
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/content/recipe/` — ordered queries over feature-registered live recipe views
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/knowledge/` — how knowledge is gained: `discovery/` (capability, codec, lifecycle, acquisition), `research/` (familiarity and eligibility, research), `teaching/` (invitations and consent)
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/network/` — the grimoire's bounded messages, their codecs and content sync
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/reading/` — reader sessions and server-computed counts, and `IngredientRequirements` (reader requirements; allocation lives in `platform/inventory/`)
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/registry/` — book, research blocks and reader menu registration; event wiring, the Apothecary tab contribution, the showcase section
- `src/main/java/io/github/dirtyredz/herbalwitch/grimoire/showcase/` — the grimoire showcase section

**src/main/java: magic**

- `src/main/java/io/github/dirtyredz/herbalwitch/magic/charm/` — charms (Milestone 7): `catalog/`, `item/`, `equipment/` (worn rules, charge, protection events), `registry/`, `grimoire/`, `showcase/`
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/ritual/` — the functional altar (Milestone 6): `catalog/`, `registry/`, `altar/` (block, origin entity, shapes), `menu/` (zero-slot menu, access, screen sync), `session/` (sessions, offerings, the server lifecycle), `action/` (founding, initiation, meditation, attunement, the conversion commit, request dispatch; Garden Blessing and Home Protection), `network/`, `grimoire/`, `showcase/`; Milestone 7 adds `circle/` (the ritual circle block, layout and validation)
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/ward/` — ward stones (Milestone 7): `catalog/`, `block/`, `menu/`, `persistence/` (records, codec, world SavedData), `service/` (index/ticks, activation, owner controls, focus and reconciliation), `targeting/`, `registry/`, `grimoire/`, `showcase/`
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/witch/` — witch status and energy (Milestone 6): `status/` (state, codec, capability, lifecycle, spending, mutation helpers and private sync), `energy/` (balance, pure arithmetic, practice, environmental recovery and drink/event adapters), `initiation/` (consent; `founding/` the world record), `config/` (server rules, including `WorkingRules`), `network/` (snapshot, rules), `command/`, `registry/`
- `src/main/java/io/github/dirtyredz/herbalwitch/magic/witch/grimoire/` — witchcraft page contributions

**src/main/java: platform**

- `src/main/java/io/github/dirtyredz/herbalwitch/platform/compat/` — optional server adapters, loaded only when their mod is present (`thirst/`; `curios/` the compile-only Curios charm-slot adapter)
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/geometry/` — shape rotation and `CellShapes` cell/facing cuts shared by furniture, stations and the altar
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/inventory/` — deterministic missing-ingredient allocation
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/multiblock/` — multiblock cell layout and lifecycle (placement, removal, shared state) used by the rack, hearth and stove
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/network/` — the mod's channel, payload bounds and chunked snapshot assembly (no gameplay policy)
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/permission/` — the shared authorization boundary for custom world changes; `entity/` (harmful entity actions, claim-mod adapters, harm attribution, the ward protection check)
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/persistence/` — checksummed snapshot envelope and serialized temp/force/atomic-replace/read-back writer; feature adapters retain failure policy and schemas.
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/placement/` — pure placement resolution, world checks and the transactional commit for multi-cell pieces
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/player/` — server-lifecycle online roster and independently scoped fake registrations
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/recipe/` — immutable processing facts, live `RecipeView` and ordered ingredient queries
- `src/main/java/io/github/dirtyredz/herbalwitch/platform/time/` — tick constants and explicit ceiling/fractional duration formatting

**src/main/java: showcase**

- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/command/` — `/herbalwitch showcase` registration and execution
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/construction/` — preflight checks, the builder and sign labels (the only world writes)
- `src/main/java/io/github/dirtyredz/herbalwitch/showcase/layout/` — section contract, registry, placements and the bounded plan (no world writes)

**src/main/resources**

- `src/main/resources/assets/herbalwitch/models/block/botany/` — herb models `<group>/<herb>/{young_0..2,mature}.json` (generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/botany/garden/` — the herb bed assembly and its parts (`parts/wood/`, `parts/soil/`; generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/decor/pieces/` — every other piece's compiled models, `<family>/<group>/<piece>/` (`base`, `off`/`on`, or `part_<n>`, `lit_<n>`, `mirrored_<n>`, `assembly`)
- `src/main/resources/assets/herbalwitch/models/block/decor/templates/` — the five 0.1.0 furniture templates (compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/grimoire/` — the stand and bookshelf models, `<piece>/base.json` (compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/preparations/bundles/` — hung bundle models `<group>/<herb>/{ceiling,wall}.json` (generated by tools/generate-models.mjs)
- `src/main/resources/assets/herbalwitch/models/block/preparations/templates/` — the oil lantern templates (compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/ritual/altar/` — the altar's compiled models (`part_<n>`, `active_<n>`, `assembly`; compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/ritual/circle/` — the ritual circle's 32 compiled cell models (`part_<n>`, `active_<n>`; no `assembly`, the item is a sprite). An enumerated exception to the 12-file cap: one model per cell state of the 16-cell ring
- `src/main/resources/assets/herbalwitch/models/block/stations/` — station models, one directory per station (`base`, or `part_<n>`, overlays and `assembly` for multiblocks; compiled from src/art/blockbench by tools/compile-blockbench.mjs)
- `src/main/resources/assets/herbalwitch/models/block/ward/` — the ward stone's compiled models, `ward_stone/{base,active}.json`
- `src/main/resources/assets/herbalwitch/textures/block/botany/` — the swatch atlas `parts.png` every herb and dried bundle is built from, and the bed soil (generated)
- `src/main/resources/assets/herbalwitch/textures/block/material/` — the shared house `wood`, `wood_post`, `wood_end` and `gold` the herb bed and hung bundles use (generated)
- `src/main/resources/assets/herbalwitch/textures/block/pieces/` — the textures each Blockbench piece owns, `<stations|decor|preparations>/<piece>/<texture>.png`, written by tools/compile-blockbench.mjs from the projects
- `src/main/resources/assets/herbalwitch/textures/gui/decor/` — the storage menu atlases (generated)
- `src/main/resources/assets/herbalwitch/textures/gui/grimoire/` — the reader atlas: the open book and its page sprites (generated)
- `src/main/resources/assets/herbalwitch/textures/gui/stations/` — station interface atlases (generated)
- `src/main/resources/assets/herbalwitch/textures/gui/witch/` — the energy HUD, altar screen and ward screen atlases (generated)
- `src/main/resources/assets/herbalwitch/textures/item/botany/` — seed and fresh-herb sprites `<group>/<herb>/` (generated)
- `src/main/resources/assets/herbalwitch/textures/item/grimoire/` — the grimoire's sprite (generated)
- `src/main/resources/assets/herbalwitch/textures/item/preparations/` — bundle, powder and product sprites (generated)
- `src/main/resources/data/herbalwitch/curios/` — the Curios `charm` slot assignment for players (`entities/charms.json`); inert without Curios
- `src/main/resources/META-INF/` — `mods.toml` and mod metadata

**src/generated**

- `src/generated/resources/` — datagen output, checked in and packaged (its `.cache/` is ignored and excluded); never hand-edited

**src/art**

- `src/art/blockbench/` — Blockbench sources, never packaged (122 block/item projects): `catalog.json` (the project list, pinned format, owned model directories) and `<stations|decor|preparations|grimoire|ritual|ward>/<piece>/` with `<piece>.bbmodel` and `piece.json`; Milestone 5 pieces at `decor/pieces/<family>/<group>/<piece>/`, M8 at `animals/<group>/<piece>/`, M9 homes at `familiars/homes/<piece>/` (docs/BLOCKBENCH.md)

**src/test/java**

- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/botany/` — pure JUnit tests for catalogues and conditions
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/` — preparation catalogue tests (the advanced, familiar and mixture inventories)
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/application/` — familiar-guide handoff and registration-order regression tests.
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/stations/catalog/` — registry-free category facts
- `src/test/java/io/github/dirtyredz/herbalwitch/apothecary/stations/process/` — reservation and completion arithmetic tests (requirements tests live in `platform/recipe/`)
- `src/test/java/io/github/dirtyredz/herbalwitch/client/` — headless client tests (`magic/witch/hud/`, `grimoire/` panel routing and `recipe/` text contracts)
- `src/test/java/io/github/dirtyredz/herbalwitch/client/grimoire/` — headless panel routing across the four screen hosts
- `src/test/java/io/github/dirtyredz/herbalwitch/client/recipe/` — translation keys, argument types and explicit rule multipliers
- `src/test/java/io/github/dirtyredz/herbalwitch/data/` — generated-resource tests: `GeneratedResourcesTest`, per-feature manifests and tests in `art/` (the Blockbench manifest), `botany/`, `decor/`, `grimoire/`, `magic/`, `preparations/`, `showcase/`, `stations/`, and `fixture/` helpers (model resolution, template checks, inherited item displays)
- `src/test/java/io/github/dirtyredz/herbalwitch/data/decor/manifest/` — the independent 94-piece inventory the decor resource tests read
- `src/test/java/io/github/dirtyredz/herbalwitch/decor/` — pure furniture tests: `catalog/` (the inventory), `behavior/` (seats, storage, displays, beds)
- `src/test/java/io/github/dirtyredz/herbalwitch/grimoire/` — pure grimoire tests: `network/` (protocol bytes/bounds), `knowledge/` (discovery, research, teaching), `content/` (editorial definitions, the source priority order and `Contributions`)
- `src/test/java/io/github/dirtyredz/herbalwitch/magic/` — pure witch, ritual and ward tests: `witch/` (status, energy, initiation, founding, payloads), `ritual/` (sessions, circle layout, garden blessing plan), `ward/` (record codec, chunk index)
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/geometry/` — shape rotation tests
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/inventory/` — allocation characterization
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/multiblock/` — 3D and sparse layout tests
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/network/` — payload bounds and chunk assembly tests
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/persistence/` — exact capture-envelope compatibility, filesystem fault stages and malformed/oversized snapshots.
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/placement/` — placement resolution tests
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/recipe/` — processing requirement codec and bounds
- `src/test/java/io/github/dirtyredz/herbalwitch/platform/time/` — duration boundaries and fractional text
- `src/test/java/io/github/dirtyredz/herbalwitch/showcase/` — showcase plan and coverage tests
- `src/test/java/io/github/dirtyredz/herbalwitch/structure/` — dependency direction and durable-mutation boundary guards

**src/gameTest/java**

- `src/gameTest/java/io/github/dirtyredz/herbalwitch/apothecary/botany/` — herb, garden, foraging and worldgen GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/apothecary/brewing/` — product use, block and advanced-preparation GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/apothecary/stations/` — station GameTests; `process/` the shared process behaviour tests; `advanced/` the four advanced stations and the witch-recipe flow
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/client/recipe/` — counted-stack NBT/copy and duration characterization
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/compat/` — optional-integration GameTests (profile dependent)
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/decor/` — furniture GameTests; `placement/`, `seating/`, `storage/`, `sleeping/`, `openings/`, `lighting/` per behaviour (test names, not main packages)
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/fixture/` — shared GameTest world, player, botany, station, magic (`MagicFixtures`) and showcase fixtures
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/grimoire/` — grimoire GameTests: discovery, ingredients, access, teaching; `content/` covers entry factories, ordered source contracts and reloads
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/grimoire/content/` — entry flags, defensive copies, link order and the frozen pre-M8 order pin with explicit M8 additions at their declared priorities
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/magic/` — witch (`witch/`), altar and circle (`ritual/`), `ward/` and `charm/` GameTests
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/magic/witch/status/` — test-only durable-state setup helpers
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/platform/` — cross-cutting block guards (occlusion); `recipe/` covers counted ingredient order and matching
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/platform/player/` — roster precedence, removal and lifecycle contracts
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/platform/recipe/` — registered ingredient semantics and ordering
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/showcase/` — showcase command, boundary and content GameTests

**src/gameTest/resources**

- `src/gameTest/resources/data/herbalwitch/structures/` — GameTest structure templates (`empty`, `showcase` at 128 x 10 x 100, `showcase_section`); never in the release jar

**tools**

- `tools/` — deterministic Node tools: `generate-textures.mjs`, `generate-models.mjs`, `compile-blockbench.mjs` (also the dev-art pack), `generate-test-structures.mjs`, `preview-models.mjs`; not packaged
- `tools/art/` — shared pixel code (`pixels`), the set's texture density rule (`density`: two texels per model unit), the procedural texture manifest (`catalogue`) and ownership-checked output writing shared by the three producers (`output`); `drawing/` (neutral sprite/item primitives and plant motifs), `content/` (feature art), `materials/`, `interface/` and `test/` (density and frozen byte parity)
- `tools/art/content/botany/` — herb sprites per habitat group, the herb list, the parts atlas, garden soil (`garden.mjs` owns the smooth value noise it uses)
- `tools/art/content/botany/seeds.mjs` — botanical seed layouts over the shared drawing primitives
- `tools/art/content/grimoire/` — the grimoire book sprite (`book.mjs`)
- `tools/art/content/magic/` — charm and circle item sprites (`sprites.mjs`)
- `tools/art/content/preparations/` — preparation materials and product sprites; `advanced.mjs` holds advanced, familiar and mixture sprites
- `tools/art/drawing/` — neutral sprite canvas/outline primitives, reusable plant motifs and item drawing
- `tools/art/interface/` — all GUI atlases (`stations`, `grimoire`, `decor`, `witch`) and the shared house GUI kit
- `tools/art/materials/` — shared house wood and gold (`house.mjs`, which owns the gold ramp)
- `tools/art/test/fixtures/` — frozen pre-refactor procedural manifest order and PNG hashes
- `tools/blockbench/` — the Blockbench compiler: `project` (the pinned .bbmodel contract), `catalogue` (catalog.json, piece.json), `textures` (owned and shared slots), `assembly` (state outputs, multiblock cells and variants), `geometry/` (cell cutting and mirroring, legal item assemblies), `attachments` (generated bundles), `compile`
- `tools/blockbench/dev-art/` — the live dev-art resource pack: in-memory staging, validation, publication into a trial instance, save watching
- `tools/blockbench/migration/` — importer from model JSON and the appearance-parity comparator
- `tools/blockbench/test/`, `tools/blockbench/dev-art/test/` — node --test suites and their `fixtures/`
- `tools/image/` — PNG encode/decode, the bitmap font and contact-sheet composition shared by generators and the previewer
- `tools/models/` — the procedural model code: `geometry` (boxes, planes, position UVs, buried-face removal), `model-output` (model JSON serialisation shared with the Blockbench compiler), `textures`, `catalogue` (families and owned directories); `botany/` (bed, herbs, coplanar separation for organic models), `preparations/` (dried bundles, also the Blockbench attachments)
- `tools/preview/` — the headless model previewer: `assets/` (resource lookup, model inheritance, vanilla parents), `geometry/` (element baking, transforms, item sprites, coplanar-face detection), `render/` (cameras, rasterizer), `scenes/` (blockstate evaluation, review scenes), `report/` (coverage, the z-fighting sweep, sheets, manifest), `test/` (node --test)

**qualification (Milestone 12; never packaged)**

- `src/gameTest/java/io/github/dirtyredz/herbalwitch/qualification/` — grouping parent: cross-milestone release qualification
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/qualification/upgrade/` — save-upgrade phases: open a historical world's chunks and players, touch, re-save, first job tick
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/qualification/integration/` — cross-milestone loops through production entry points (forage to shop to non-witch use; witch-only brewing charged once)
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/qualification/performance/` — the frozen performance workloads, tick sampling from vanilla's tick ring, population census and the restart indexing probe (run by `qualifyPerformance` only)
- `tools/qualification/performance/` — the performance gate judge (`report.mjs`, gates frozen before measurement)
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/qualification/localization/` — every registered block, item, entity, effect and tab has an English name
- `tools/qualification/` — grouping parent: release qualification tooling (Node and PowerShell)
- `tools/qualification/restart/` — grouping parent: the M9-M11 restart report validators
- `tools/qualification/restart/familiars/` — familiar restart report
- `tools/qualification/restart/magic-tools/` — broom/wand restart report and queue script
- `tools/qualification/restart/commerce/` — commerce restart report
- `tools/qualification/restart/capture/` — M8 capture restart report and its README
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/companions/animals/persistence/` — capture restart qualification: real captures/releases stopped at each ledger boundary, reconciled in a second JVM
- `tools/qualification/save-upgrade/` — lossless NBT and Anvil reading/writing, fixture sanitising, inventories and the upgrade comparison

**other (gradle, docs)**

- `docs/` — architecture, decisions, gotchas, features, art, compatibility; `docs/catalogue/` traces the design to implementation
- `gradle/` — `art.gradle` (the art pipeline tasks) and `qualification.gradle` (every ANU-only dedicated-JVM harness, one shared phase factory); the wrapper comes from the workspace template

## Components

| Component | Files | Responsibility |
|---|---|---|
| Design catalogue | `decor/catalog/*` (`FinishCatalog` one entry, `FurnitureSpec`, `FurnitureBehavior`, `MountSpec`, `FurnitureCatalog`), `decor/catalog/pieces/*` | The single cottage design and the 94 pieces; ids `decor/<family>/<piece>`; waves as metadata |
| Decor registration and structure | `decor/registry/*`, `decor/structure/**`, `platform/placement/*` | Catalogue -> blocks, items, block entities, menus, the seat entity; the shared assembly block, support, shapes and checked placement |
| Decor behaviours | `decor/behavior/{DecorDoorBlock,DecorLightBlock,seating,storage,sleeping}` | Seats, origin inventories and displays, vanilla-compatible beds and door, lights |
| Botany | `apothecary/botany/**` | Herb catalogue, plants, bed, growth, harvest, conditions reload, foraging, worldgen (`HerbWorldgen`) |
| Station base | `apothecary/stations/block/{StationBlock,StationEntity,ProcessStationEntity}`, `apothecary/stations/block/StationMenu` | Shared block and menu boilerplate (facing, support, ticker, drops, slot rules) |
| Processing | `apothecary/stations/process/**` | Composed `ProcessStation`: inventory with reservations, job, completion; `recipe/` format and index, `ProcessMatcher`, `execution/` policy and commit, `authorization/` operator and magical policy |
| Stations | `apothecary/stations/everyday/<station>/`, `apothecary/stations/registry/*`, `apothecary/stations/heat/*` | The six everyday stations, their registration and heat rules |
| Advanced stations | `apothecary/stations/advanced/**` | Witch's Cauldron, Copper Still, Bottling Bench, Charm Workbench on one `AdvancedStationBlock`/`AdvancedStationEntity`; witch recipes start by menu button |
| Products | `apothecary/brewing/product/**`, `apothecary/brewing/application/**`, `apothecary/brewing/effect/**`, `apothecary/brewing/projectile/*` | Bundles, powders, drinks, salve, oils, garden products, oil lantern, the 16 advanced preparations, mixtures, effects, thrown preparations |
| Charms | `magic/charm/**`, `platform/compat/curios/*` | Sachet and amulets, worn rules and charge, the Curios slot adapter |
| Ritual circle | `magic/ritual/circle/*`, `magic/ritual/action/{Circle,GardenBlessing,HomeProtection}*` | The 5x5 ring, its validation, Garden Blessing and Home Protection |
| Wards | `magic/ward/**` | `WardActivation`, `WardControls`, `WardFocus` and `WardLifecycle` (reconciliation, stone/record, indexing) depend only on `WardIndex.loaded()` and `WardContext`; `WardService` is the top (server lifecycle, ticks, bounded target-suppression sweeps) |
| Metadata carrier | `apothecary/brewing/product/metadata/DrinkMetadata` | Water-to-drink data hand-off; the Thirst adapter installs it |
| Showcase | `showcase/{layout,construction,command}`, `*/showcase/` sections | Plan (pure), preflight and builder (world writes), command |
| Permission | `platform/permission/**` | `ActionContext` + `ActionPermissionService`; `entity/` the one boundary for harmful entity actions |
| Compat | `platform/compat/**` (Thirst, Curios), `client/compat/**`, `data/server/compat/` | Thirst adapter; EMI/JEI plugins; Farmer's Delight tags |
| Client | `client/HerbalWitchClient`, `client/stations/*`, `client/recipe/RecipeText` | Mod-bus wiring; station screens and widgets; shared recipe text (materials are untinted, no colour handlers) |
| Datagen | `data/**` | Models, states, names, recipes, loot, tags, worldgen, conditions |
| Art pipeline | `tools/generate-textures.mjs`, `tools/art/**`, `tools/generate-models.mjs`, `tools/models/**`, `gradle/art.gradle` | Seeded generation of materials, sprites, GUI atlases and the procedural models (herbs, beds, hung bundles); producers own disjoint paths |
| Blockbench pieces | `src/art/blockbench/**`, `tools/compile-blockbench.mjs`, `tools/blockbench/**` | Hand-edited one-off pieces (stations, furniture, lantern) compiled to models and owned textures; the dev-art pack |
| Art review | `tools/preview-models.mjs`, `tools/preview/**` | Headless previews, coverage and reference comparisons |
| Multiblocks | `platform/multiblock/*`, `apothecary/stations/block/StationBlock` | Cell layout, placement and removal, the origin cell, shared lit/active state |
| Grimoire catalogue and registry | `grimoire/catalog/GrimoireCatalog`, `grimoire/registry/*` | Permanent ids (`grimoire/book`, `stand`, `bookshelf`, menu `reader`), registration, creative tab, server event wiring |
| Grimoire access | `grimoire/access/{item,block,menu}` | The book item, the one-cell research block (no block entity, no storage), the zero-slot reader menu and the access rule (book: carried; block: loaded, within 8 blocks) |
| Grimoire content | `grimoire/content/**` | Sources ordered by each feature's own numeric `PRIORITY` through the thread-safe `Contributions` holder (also behind `DiscoveryForms` and `RecipeReferences`); neutral `HerbSummary`; server revisions and editorial reload |
| Grimoire knowledge | `grimoire/knowledge/{discovery,research,teaching}` | Personal discovery capability and codec, observation policy, research eligibility and service, consenting teaching (pure rules plus a server adapter) |
| Grimoire reading | `grimoire/reading/*` | Reader sessions and the server reader service; ingredient requirements backed by `platform/inventory/MissingIngredients` |
| Grimoire network | `grimoire/network/*`, `platform/network/*` | Six bounded messages and codec on the one channel `herbalwitch:main`; content sync; payload bounds and chunk assembly |
| Station descriptions | `apothecary/stations/catalog/ProcessingCategories` | Registry-free category/station facts; registered types resolved by `registry/ProcessingRecipeBindings` |
| Shared values | `platform/{recipe,inventory,time}/` | Live recipe views, immutable descriptions, ingredient allocation and explicit duration rounding |
| Online players | `platform/player/OnlinePlayers` | One server roster, real-player precedence and scoped fake registrations (`register` is GameTest-only; `DependencyDirectionTest` guards it) |
| Witch status | `magic/witch/status/{WitchStatusService,WitchSpend,WitchStatusSync}` | Named mutation operations, transactional spending, private snapshot cadence |
| Witch rules and lifecycle | `magic/witch/{config,energy,initiation,network,command,registry}/` | Validated rules/sync, recovery policy, consent/founding, capability lifecycle and administration |
| Altar and ritual sessions | `magic/ritual/{altar,menu,session,action,network}/` | Checked altar access, offerings, one session per altar/player, ritual completion and private menu updates |
| Client witchcraft | `client/magic/witch/{hud}/`, `client/magic/ritual/{widgets}/`, `client/magic/ward/`, `client/brewing/` | Private status caches/HUD, altar/ward screens and previews, thrown-preparation rendering |
| Client structures | `client/placement/`, `client/decor/storage/` | Checked-placement previews, furniture storage screens and display rendering |
| Shared presentation | `client/recipe/RecipeText`, `client/grimoire/GrimoirePanelHost` | Recipe strings/counts; panel lifecycle, tooltips and input fallback |
| Grimoire client | `client/grimoire/*`, `client/grimoire/widgets/*` | Two-page book screen, view model, station panel and button, client state cache, teaching panel |
| Commerce | `apothecary/shop/**`, `client/stations/shop/**`, `data/{client,server}/stations/shop/` | Per-shop records and the one verified transfer path, fixtures and links, sessions and menus, requests, villager customers and demand budgets |

## Dependency direction

```text
feature grimoire/ + registration adapters -> grimoire contracts
grimoire content/knowledge/reading/network -> neutral facts + platform recipe/inventory/network
apothecary/stations -> apothecary/brewing -> apothecary/botany
processing + brewing -> magic/witch services
magic/ward focus -> stations registry; magic/charm equipment -> brewing effects
apothecary/shop -> brewing/botany/charm catalogues, decor counter (by id), platform persistence/permission/network/placement; nothing imports shop
client + datagen -> features and contracts
```

- Features never import client/datagen. Core grimoire consumers never import feature implementations
  or client/viewer code; `DependencyDirectionTest` guards these boundaries.
- Magic presentation adapters contribute sources; durable witch mutation does not depend on page
  implementations. Shared recipe contracts do not depend on station execution or registration.
- Bootstrap, showcase wiring and `platform/compat/{thirst,curios}` are integration bridges;
  these intentional adapters can import features. They are not neutral platform value contracts.
- Decor registry -> behaviour/structure/catalogue; behaviours -> structure -> catalogue and
  platform placement/geometry. Catalogues remain loadable without registry bootstrap.

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
- `tools/art/` has nine direct entries: four stable modules (`catalogue`, `density`, `output`, `pixels`) plus `content/`, `drawing/`, `interface/`, `materials/` and `test/`. Feature art belongs under `content/`; GUI atlases under `interface/`.
- `StationBlock`/`StationMenu` share block/menu boilerplate. `ProcessStationEntity` is the shared
  block-entity base; it owns a composed `ProcessStation`. Algorithms remain in `apothecary/stations/process/`.
- Milestone 7 added no top-level Java package: charms, wards and the circle sit under `magic/`, the
  advanced stations under `apothecary/stations/advanced/`, entity permission under `platform/permission/entity/`.
  `apothecary/stations/` was at its breadth cap, so the station authorization lives inside `apothecary/stations/process/`
  (`authorization/`), beside `recipe/` and `execution/`; the four advanced stations share one
  `AdvancedStationBlock`/`AdvancedStationEntity` rather than one block class each (docs/DECISIONS.md).
- `models/block/ritual/circle/` holds 32 files, an enumerated exception to the 12-file directory cap: the
  ring has 16 cells with an idle and an active model each, and the compiler names them by cell.
- `GeneratedResourcesTest` and the per-feature manifests keep their own independent lists on purpose,
  so a catalogue slip shows up as a diff.
- The grimoire is grouped by concern to keep every level under about ten entries: `access/` (item,
  block, menu), `knowledge/` (discovery, research, teaching: what a player knows and may learn) and
  `reading/` (sessions and reader ingredient requirements). The plan named `grimoire/{block,item,menu,discovery,
  research,teaching,ingredients}` as siblings; the grouping is a deliberate deviation (docs/DECISIONS.md).
  Teaching keeps pure rules (`TeachingService`) apart from the server adapter (`TeachingWorld`).

- Small feature `grimoire/`, targeting and integration packages remain deliberate ownership boundaries.
  The single-writer datagen homes (`data/server/{recipes,loot}`, `data/client/language`) and focused
  client/item adapters (`client/{brewing,compat/ritual,stations/widgets}`, `magic/charm/item`) are also intentional.
  Small typed recipe adapters retain registered type/serializer bounds; no configurable recipe framework.
  Broader TeachingNotifier/client-cache splits, catalogue dispatch, nullable policy/slot-layout cleanup
  and furniture placement, storage predicates and migration scripts remain separate P2 work.

M9 additions: `companions/familiars/equipment/` owns canonical slots/cargo and transfer receipts. `client/familiars/{model,render}/` reads compiled rigs and attaches equipment; `state/FamiliarVisualCache` holds cosmetics. `src/art/blockbench/familiars/equipment/<gear>/` owns eight pieces. `src/art/entities/{raven,owl,snake}/` holds creature projects. `tools/entities/` owns the compiler/poses, with `test/`; `tools/compile-entity-models.mjs` produces `entity_models/familiars/<species>/` and `textures/entity/familiars/<species>/`. Equipment tests live in `src/{test,gameTest}/java/io/github/dirtyredz/herbalwitch/companions/familiars/equipment/`; geometry tests in `src/test/java/io/github/dirtyredz/herbalwitch/client/familiars/`.

Recovery lives in `companions/familiars/recovery/`: lethal damage checkpointing, paid revival and retained-payload administrative resolution. `companions/familiars/admin/` owns its level-two command adapter; `ritual/RevivalAction` owns the circle session. Real lifecycle tests live in `src/gameTest/.../companions/familiars/recovery/`.

Bond progression and durable named upgrades live in `companions/familiars/bond/`; `interaction/FamiliarCareContribution` observes the animal module's neutral `AnimalCareContributions` seam. The shared `ritual/FamiliarUpgradeAction` serves Heartwood, Spirit Well and Harmony. No familiar implementation is imported by animal care.

`companions/familiars/preparation/` owns canonical buff rules, application validation, runtime projections, damage/Shadowstep events and familiar poison policy. M7 keeps the shared item/container, concealment, poison application and delayed harm-attribution implementations.

Retrieval lives in `companions/familiars/ability/retrieval/`: exact loose-item eligibility, the durable
pickup transaction, and immutable source-retirement receipts. `platform/permission/entity/ItemPickupPermission`
owns the neutral entity-pickup veto (including Forge pickup cancellation); it is not block placement.
`data/server/familiars/FamiliarRetrievalTags` contributes the small-object allowlist to the one item-tag writer.

M9 acquisition/presentation layout: `companions/familiars/acquisition/` owns safe passive spawn
placement rules; `data/server/familiars/` owns its biome tags/modifiers. Creative egg projects live
in `src/art/blockbench/familiars/spawn_egg/<raven|owl|snake>/`, compiled to matching block-model
and owned texture directories. `companions/familiars/grimoire/` owns the installed guide source;
`data/client/familiars/FamiliarGuideTranslations` supplies its prose. `showcase/` owns nine inert
appearance samples and supply barrels. Acquisition tests live in `src/gameTest/.../familiars/acquisition/`.

Restart qualification belongs in gradle/qualification.gradle and tools/qualification/restart/familiars/. Test-world
support lives in src/gameTest/.../companions/familiars/persistence/; the test-only SnapshotProbe
adapter lives in src/gameTest/.../platform/persistence/. Neither ships in the release jar.

Native pose exports live in src/test/.../client/familiars/FamiliarNativePoseTest.java; tools/preview-native-familiars.mjs renders them with the owned equipment models. Runtime pose and attachment ownership remains client/familiars/{model,render}/.

Optional native-payload compatibility lives in platform/compat/curios/CuriosNativePayload; it depends only on NBT. Doggy Talents exclusion qualification lives beside binding tests and has no foreign class imports. Optional runtime profile versions remain in gradle.properties.

- `magic/broom/{config,entity,flight,interaction,lifecycle,network,persistence,registry}/`: validated flight rules, physical entity/item ownership, authenticated rider intent and server movement.
- `magic/wand/config/`: local utility limits and five prices, installed alongside broom settings in the existing witch rules/config/sync.
- `src/gameTest/java/io/github/dirtyredz/herbalwitch/magic/broom/fixture/`: bounded flight arena setup and authenticated actor fixtures.

- `client/broom/`: remappable intent controls, authoritative condition/state HUD, advisory deployment outline and the reload-aware item-model entity renderer.
- `data/client/magic/tools/`, `src/test/.../data/magic/tools/`: tool-owned language and independent generated-resource manifest.

- `magic/wand/{action,use,network}/`: seven utility adapters, actor-wide limits, server targeting and held-stack selector validation.
- `client/wand/`: remappable selector and mode HUD; no casting authority.
- `apothecary/botany/harvest/`: shared reversible hand/wand herb harvest plan and commit.
- `src/gameTest/.../magic/wand/fixture/`: actual held-item utility fixtures; sibling GameTests cover gardening, collection, lighting and selection.

- `magic/{broom,wand}/{grimoire,showcase}/`: tool-owned live-rule guides and separate compact station/practice plots, registered at bootstrap after the established witchcraft sections.

- `src/gameTest/.../magic/tools/restart/`: actual world save/reopen qualification for vehicles, packed preferences and interrupted repair. `gradle/qualification.gradle` isolates the two dedicated runs; `tools/qualification/restart/magic-tools/` queues them and validates distinct-process evidence.

- `src/test/.../client/fixture/NativeVertices` shares actual vanilla model export between familiar and broom pose tests. `client/broom/BroomRiderPoseTest` exports the seated player; `tools/preview-broom-rider.mjs` combines it with the owned broom model and runtime offsets.

The M10 item/recipe/control reference lives in `docs/catalogue/tools.md`; witchcraft and familiar catalogues link their unchanged service authority.
