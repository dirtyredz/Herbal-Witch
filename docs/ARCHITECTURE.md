# Architecture - The Herbal Witch

How the mod is put together. Code map: [STRUCTURE.md](../STRUCTURE.md).
Plan of record: milestones 1-12 (docs/FEATURES.md); traceability: docs/catalogue/.

Ward responsibilities are explicit in `magic/ward/service`: `WardActivation` owns refusal order and
direct/already-paid ritual charging, `WardControls` owns owner settings/release, and `WardFocus`
scans loaded cells for a functional hearth or stove (unlit blocks count). Dependencies run one way:
those three and `WardLifecycle` use `WardIndex.loaded()` (the loaded index) and `WardContext` (the rules)
and never `WardService`, which sits on top with the server lifecycle and bounded ticks/sweeps;
`WardLifecycle` alone reconciles stones with saved records and indexes them.
Unloaded wards neither protect nor consume charge, and loading never catches up elapsed time.

Preparations use one `DrinkApplication` and one `OilApplication`, with shared lazy `TimedEffect`
facts. Tea, restful infusion, healing, coating and target clearing remain explicit operations.
`HerbalDrinkItem`/`HerbalOilItem` own the use boundaries and container returns; construction data
retains the former advanced preparations' label presentation. `EffectRegistration` installs the
existing effect listeners in their original order and priorities.

Feature registrations contribute their own grimoire sources. each source's numeric `PRIORITY`
(a constant on its `*Entries` class, registered through the thread-safe `Contributions` holder) fixes
reading order independently of registry/showcase/packet bootstrap: Guide 0, Witch 100, Brewing 200,
Preparations 300, Station 400, Decor 500, Herb 600, StationRecipe 700, Altar 800, Ward 900, Charm 1000, Animal 1100
(`ContributionsTest` pins it; a new feature picks a number between). Equal priorities keep insertion order;
duplicate keys still keep the first source. Core content, knowledge, reading and network packages
consume `HerbSummary`, `DiscoveryForms`, `RecipeReferences` and live `RecipeView` contracts.
Botany supplies cached summaries keyed by condition-set identity; stations supply recipe/tag
generation. Recipe-manager, tag, definition and herb changes still invalidate server content, and
client disconnect still clears all connection data. The herb wire record remains protocol 4.

`FamiliarGuideContribution` beside the familiar application seam provides a bootstrap-only handoff.
Brewing registers a lazy deferred fallback. A bonding feature installs its own content source and
numeric priority; the earlier fallback then emits nothing, preserving first-key-wins without a late
override or an import of companions into brewing. Until installation, the M7 familiar page is unchanged.

## Current system (0.6.1 structural refactor: Milestones 1-7 plus the art pass; furniture is Milestone 5, initiation and energy Milestone 6, advanced brewing, charms, circle and wards Milestone 7)

M8 groundwork adds `companions/animals`: exact profiles and datapack membership are separate;
tags can revoke a tested adapter but cannot create one. Every server Mob receives a small lazy
provider (`herbalwitch:animals/care`); only initialized state advances its persisted loaded clock.
Schema 1 preserves unsupported/malformed payloads unchanged and refuses mutation. Personal trust
views are immutable, capacity refuses a new player without eviction, and native ownership wins
over the keeper unless representations conflict (then ownership-sensitive actions fail closed).
The care service owns bait, existing-salve targeting and calming; the adoption service calls native
taming APIs or records an exclusive keeper. One Forge entity-interaction path prevents double-hand
consumption. The private zero-slot card revalidates book access, target, range, sight, permission,
nonce and revision. The animal root remains a grouping parent; M9 will consume immutable trust views.

Added AI retains native selectors: a low-priority bait goal uses a spatial holder index, fair bucket
cursors, a bounded query queue and dimension-wide check/path/active budgets. Calming is a removable
priority-zero MOVE goal, not a panic-goal replacement. Goats/axolotls retain their Brain and bats their
flight. Source loss, danger, damage and reload stop the temporary behavior. Existing M7 sleep is reused.

`capture/data/CaptureSavedData` is the sole Overworld authority. UUID/generation records survive
release permanently. Item NBT contains only an opaque token and bounded presentation. The checked
writer owns explicit checkpoints and overridden ordinary file saves: bounded NBT envelope, SHA-256,
temp file, force, atomic replace, verified read-back. Failure keeps dirty state and locks transport;
unsupported/corrupt files are never replaced by empty data. Capture PREPARED precedes exact source
removal; STORED precedes item issuance. Release PREPARED precedes an exact-object admission permit;
RELEASED precedes item replacement. Payload compaction occurs only after verified release. Startup
quarantines incomplete or un-compacted final records; recovery advances generation and never spawns.
The loaded cross-dimension index rejects stale/duplicate incarnations without inferring death from
unload. Exact vanilla serialization adapters reject unknown capability/persistent data before capture.

The filesystem mechanics now live in `platform/persistence/{SnapshotEnvelope,VerifiedSnapshotWriter}`.
`CaptureCheckpoint` retains its exact HWC8/version-1 envelope, failure return and logging policy;
`CaptureSavedData` still owns dirty state and transport lockout. `companions/lifecycle` owns the shared
loaded-object index, lookup and retirement. Capture still decides generations, phases and exact-object
permits. Admission also checks already loaded level UUID maps for sources predating their first record;
no scan loads a chunk. Leave events remove only the exact object and shutdown clears only that server.

Showcase plans include at most 32 pure entity descriptors. Habitat blocks are constructed first;
the entity phase validates insertion and removes only its own samples if one fails. The full M8
layout is 121 x 8 x 58, with sixteen adult animals and the original 65,536-write ceiling unchanged.
Dedicated-server/restart qualification remains outside the local fast gate; see catalogue/animals.md.

Everything below is server-authoritative; clients get vanilla block updates, menu slots and menu data.
The one custom channel, `herbalwitch:main` (protocol "5", `platform/network/HerbalWitchNetwork`), carries
the grimoire's six bounded messages, the witch's two (`WitchSnapshot`, `WitchRulesUpdate`) and the
altar's two (`AltarRequest`, `AltarUpdate`), then animal request/snapshot messages, registered in that order; any wire change bumps the protocol
(protocol 2 let entry keys name furniture pages; protocol 3 added the witch and altar messages; protocol 4
grew `WitchRulesUpdate` with the working rules; protocol 5 adds the private animal card). Paths
are under `src/main/java/io/github/dirtyredz/herbalwitch/`.

**Shared read contracts.** `platform/recipe/RecipeView` exposes a live recipe's identity,
description and fingerprint without station execution. Grimoire pages retain that same object;
`RecipeIngredients` supplies primary, base and packaging queries in their original order.
`apothecary/stations/catalog/ProcessingCategories` contains registry-free category facts, while
`registry/ProcessingRecipeBindings` resolves their registered types. Public hintless pages use
`GrimoireEntry.page`; hinted and recipe entries keep explicit flags. `grimoire/catalog/WitchcraftKeys`
owns permanent discovery keys, and `platform/time/TickTime` distinguishes rounded countdowns from
fractional recipe duration text.

**Online players.** `platform/player/OnlinePlayers` owns lookup and enumeration for one server,
wired once by `HerbalWitch`. Real players take precedence over independently scoped fake registrations;
removed fakes and registrations from a stopped server are excluded. Initiation owns consent only.
Teaching keeps its own range, dimension, eligibility and name-order rules over the shared roster.
`OnlinePlayers.register` is fake-player API for GameTests only (`DependencyDirectionTest` rejects it in
main); `EntityActionPermission` resolves principals through `OnlinePlayers.find` like everyone else.

**Witch mutation boundary.** Durable `WitchState` setters and the mutating energy/sleep arithmetic
are package-private in `magic/witch/status`. Drink recovery, online countdowns and completed sleep
enter through named `WitchStatusService` operations. `WitchStatusSync` owns epochs and private
snapshot/rules emission behind the existing service entry points; save fields and protocol stay fixed.
GameTest setup uses helpers in its own status package, which are excluded from the release jar.

`WitchStatusService.spend(player, units, change)` coordinates paid operations in hundredths:
check, snapshot, participant preparation, debit, change, successful practice, success side effects,
then private synchronization. False results and runtime exceptions restore durable witch state;
participants restore their inventories or recipients. A rollback exception cannot prevent durable
restoration or replace an original exception. Transient disturbance remains set after a debit.
Founding and designation retain their separate no-energy conversion path. This is a synchronous
boundary, without crash atomicity or retraction of already-emitted conversion notifications.

**Decor (`decor/`).** One cottage design (`FinishCatalog`, a single entry) and 94 pieces described in
seven family catalogues (`decor/catalog/pieces/`), each a `FurnitureSpec`: id, family, art group, wave,
behaviour (`FurnitureBehavior`), mount (`MountSpec`: support and cells, `(right, up, depth)` from the
origin), seats or slots. `FurnitureCatalog.ALL` is what is registered (every wave up to `DELIVERED`).
- *Composition:* `registry/DecorBlockFactory` builds one class per behaviour: `structure/DecorAssemblyBlock`
  (static pieces and multi-cell rugs: facing, cell index, shape, support, teardown),
  `behavior/seating/SeatingBlock`, `behavior/storage/StorageFurnitureBlock`,
  `behavior/DecorLightBlock` (lit, light 14 on every cell), `behavior/sleeping/DecorBedBlock`
  (vanilla `BedBlock` with a `cell` property) and `behavior/DecorDoorBlock` (vanilla `DoorBlock`);
  the 0.1.0 small rug stays vanilla carpet. Support (`structure/FurnitureSupport`): floor, wall, ceiling,
  corner or carpet, checked per supported cell and never through an unloaded chunk.
- *Placement:* `structure/FurniturePlacementItem` extends the shared `platform/placement/StructurePlacementItem`
  (also the altar's item). It asks the block for its origin state (facing, a door's
  hinge, a bed's direction), resolves the plan (`platform/placement/PlacementResolver`: the anchor cell on
  the aimed cell; a hanging piece anchors its top cell), checks every cell (`PlacementChecks`: loaded,
  border, height, replaceable, entities, support, then the build permission) and commits it
  (`PlacementCommit`, inside Forge's snapshot capture: one multi-place event, deferred neighbour updates,
  a full restore including the item on cancellation). The client preview (`client/placement/`, shared by
  every `StructurePlacementItem`: furniture and the altar) draws the same plan as see-through models with
  a green or red outline. Per-cell collision shapes come from assembly boxes through the shared
  `platform/geometry/CellShapes` (`FurnitureShapes` and `AltarShapes` use it).
- *Teardown:* breaking any cell removes the piece through neighbour updates; only cell 0 has loot (a
  door's lower half, a bed's foot), so one item drops; creative removes the origin quietly; losing support
  and explosions take the same path; multi-cell pieces refuse pistons.
- *Seating:* `behavior/seating`: seats per piece (`SeatLayout`), the aimed seat only, refused when taken
  or without headroom, an invisible never-saved `SeatEntity` per sitter that removes itself when empty or
  when its furniture goes; logout and death stand the sitter up (`SeatEvents`).
- *Storage:* `behavior/storage`: the origin cell's `FurnitureStorageEntity` holds one schema-versioned
  `FurnitureInventory` (27 or 54 for cabinets, 3/6/9 single-item slots for displays) behind one
  `FurnitureStorageMenu`; a cabinet's other cells carry an empty entity that hands the item-handler
  capability to the origin; displays have no automation and sync what they show in their update tag
  (`display/DisplaySnapshot`, on change only), drawn at authored anchors (`display/DisplaySpec`) by
  `client/decor/storage/FurnitureDisplayRenderer`. Contents drop once, from the origin. A shelf placed
  before 0.4.0 creates an empty inventory the first time it is used.
- *Access:* `structure/FurnitureAccess` lets any non-spectator within reach use furniture (adventure mode
  included); building permission applies to placement only.
- *Grimoire:* `decor/grimoire/DecorEntries` contributes one public `furniture|` page per piece
  (blurb, size, mounting, use, seats or slots, crafting; lookalikes link their station).

**Botany (`apothecary/botany/`).**
- *State and growth:* a herb is a plant block with age 0..3 (crop or bush form). It grows by vanilla
  random ticks and needs crop light to grow, but survives without light so worldgen can place it. Seeds
  plant only on farmland or the herb bed (`garden/`, farmland-style moisture, never trampled).
- *Harvest:* use on a mature plant gives 2 fresh + 1 seed and resets the age (bushes to 1); a favoured
  harvest or an enriched bed adds fresh herbs. Adventure players cannot harvest. Breaking drops through
  the loot table.
- *Conditions:* data per herb (`data/herbalwitch/herb_conditions/<group>/<herb>.json`: habitat tag,
  discovery chance, harvest bonus, liked conditions). `apothecary/botany/conditions` owns the reload listener; it
  validates the whole set and rejects the entire reload on any error, keeping the previous set. The
  live evaluator reads rain, low sky, night and full moon.
- *Foraging:* a global loot modifier on player-broken grass-like blocks adds starter seeds and one
  habitat herb at its discovery chance.
- *Worldgen:* per herb a configured and placed feature, added by per-group biome modifiers onto habitat
  biome tags. Existing chunks are not retrofitted.

**Herb bed joining (`apothecary/botany/garden/`).** `HerbBedBlock` carries boolean `north/east/south/west`
properties, computed by `GardenBedConnections`: a side is joined when the same bed block stands beside
it at the same height. They are set on placement, in `onPlace` for direct `setBlock`, in `updateShape`
on neighbour change, and on rotate/mirror; beds saved before 0.2.0 are repaired by their random tick.
Four more booleans (`inner_north_east` ... `inner_west_north`) flag an inside corner (both sides joined, no
bed on the diagonal): set by `refreshCorners`, and refreshed on diagonal beds by `onPlace`/`onRemove`.
Connection is visual only; moisture and enrichment are unchanged. The blockstate is multipart
(`data/client/botany/garden/HerbBedBlockstates` + `HerbBedModels`) over part models under
`models/block/botany/garden/parts/{wood,soil,moist}`.

**Art pipeline.** Sources: procedural textures (`tools/art/`), procedural models (`tools/models/`:
herbs, herb beds, hung bundles; the grimoire's book sprite and reader atlas in `tools/art/content/grimoire/` and `tools/art/interface/grimoire.mjs`) and
Blockbench projects (`src/art/blockbench/`: stations, furniture, oil lantern, the grimoire stand and
bookshelf, the ritual altar; a `.bbmodel` plus a `piece.json` each). Three producers (`generate-textures.mjs`,
`generate-models.mjs`, `compile-blockbench.mjs`) each own disjoint paths, enforced by
`tools/art/output.mjs` (write only declared paths, clean only own directories) and each has `--check`.
They write the Minecraft assets under `src/main/resources`; Java datagen then adds blockstates and item
wrappers that only name their parent model, everything into `src/generated`; the jar packages both.
Item display lives in the compiled parent model, not Java; `ModelResolver` and the test fixtures merge
inherited display. Gradle (`gradle/art.gradle`) runs the producers before resources and datagen
(`generateArt`) and the checks in `check` (`checkArt`). The owner workflow is docs/BLOCKBENCH.md.
Every texture is at one density, two texels per model unit (`tools/art/density.mjs`: 32 x 32 block and
item textures, 512 x 512 GUI atlases, UVs still in 16 units); the texture producer, the Blockbench
compiler and `TextureDensityTest`/`TemplateChecks` refuse any other size.
`tools/preview-models.mjs` (with `tools/preview/**`) renders headless previews (family contact sheets,
herbs on beds, item GUI sheet, review scenes, owner-reference comparisons, a manifest); tests:
`node --test "tools/preview/test/*.test.mjs"`. Previews approximate directional shading only (no
ambient occlusion, world light, shaders, animation, mipmaps or item extrusion).

**Showcase (`showcase/`).** Three separated steps. `layout/` builds a bounded plan from registered
sections with no world access (lazy state suppliers keep it unit-testable). `construction/` runs the
preflight (loaded, border, build height, permission, solid floor, no block entities, no entities) and
only then the builder, the sole world writer. `command/` registers `/herbalwitch showcase` (level 2).
Each feature registers its own section; coverage tests require every block to appear. The altar's
section (`magic/ritual/showcase`) builds an idle altar and a supply barrel and starts nothing. Sections
pack into rows no wider than `ShowcasePlan.ROW_WIDTH` (126), so adding a section grows the depth first;
the GameTest template is 128 x 10 x 100 and `ShowcasePlanTest` fails if a plan outgrows it. Sections belong to a `ShowcaseGroup` (`furniture`: decor; `witchcraft`: the rest, the default); each group is its own plan and plot, and the bare command builds both, `ShowcaseCommand.SEPARATION` apart. Signs stand beside their block (rule on `ShowcaseSection.placements()`).

**Stations (`apothecary/stations/`).**
- *Composition:* `StationBlock`, `StationEntity` and `StationMenu` carry block and menu boilerplate.
  Processing rules live in `process/`: `ProcessInventory` (slot roles; reserved counts stay in their
  slots), `ProcessJob` (exact shapeless matching; recipe id + content fingerprint), `ProcessCompletion`
  (pure capacity arithmetic), `ProcessRecipeJson`/`ProcessRecipeIndex` (one bounded format; per-type
  sorted cache). `ProcessStation` ties them to a block entity by composition. Since Milestone 7 `process/`
  has four sub-packages: `recipe/` (the recipe type, JSON and wire format, index; shared facts in `platform/recipe/`),
  `ProcessMatcher`, `execution/` (`ProcessCommit`, `ProcessExecutionPolicy`) and
  `authorization/` (below), and `apothecary/stations/block/ProcessStationEntity` is the shared block-entity base all
  eight process stations (four everyday, four advanced) sit on.
- *Reservations and completion:* ingredients stay in their slots, counted as reserved, and cannot be
  extracted or swapped by menus or automation. At completion the result and all returns must fit before
  anything is consumed; then one commit.
- *Menus, capabilities, sides:* vanilla data slots, distance and block checks, quick-move that never
  moves reserved items, no custom packets. Item-handler capability per side: ingredients from above,
  base/package/fuel from the sides, products and returns out of the bottom. Closing a menu leaves the
  station running.
- *Heat:* `heat/` reads tags: `herbalwitch:heat_sources` (must be LIT where the block has that
  property) directly below, or one `herbalwitch:heat_conductors` block then a source. Waterlogged gives
  none. Heated stations show Needs heat until then. The Witch's Stove (`apothecary/stations/everyday/stove/`) is a heat source
  that reuses the hearth's block entity type, menu and screen.
- *Multiblocks:* `platform/multiblock/` (`MultiblockLayout`, `MultiblockStructure`) describes a
  station's cells; layouts are named in `StationCatalog` (`StationSpec.layout`). The drying rack and
  hearth are 2x2. `StationBlock` places the other cells (all must be replaceable, grounded cells
  supported), breaking any cell removes all, and only the origin cell (part 0) holds the block entity
  and drops the item once (never in creative). Any cell opens the origin's menu; lit/active is shared
  by every cell. Blockstates are multipart: `part_<n>` per cell plus `fire_<n>`/`bundles_<n>` overlays
  while lit/active. The item uses `models/block/stations/<station>/assembly`.
- *Reload and persistence:* the recipe index bumps a generation on server data reload; a job re-matches
  only on inventory revision or reload generation, and a changed or removed recipe cancels it (items
  stay, progress resets). Jobs save with schema version 2 (schema 1 loads; a newer, unknown schema is
  preserved byte for byte and the station idles until its inventory changes). There is no catch-up
  simulation: unloaded stations do not progress.
- *Advanced stations and witch recipes (Milestone 7).* The Witch's Cauldron (one block, heat below,
  `cauldron`, 600 ticks, water base), the Copper Still (1 x 2 tall, heat, `distillation`, 800 ticks), the
  Bottling Bench (2 x 1, `bottling`, 100 ticks, no witch) and the Charm Workbench (2 x 1, `charm_work`,
  200 ticks) live under `apothecary/stations/advanced/<station>/` on one `AdvancedStationBlock` and
  `AdvancedStationEntity`; they place through `StructurePlacementItem` and show an `active` state.
  A recipe carries `requirements` (`"requirements": {"witch": true, "energy": N}`), on the wire and in the
  fingerprint; `effectiveEnergy(percent)` rounds up and `processingPercent` (WorkingRules) scales it. Each
  recipe type has a policy, ORDINARY, MAGICAL or EITHER (`ProcessExecutionPolicy`). A MAGICAL recipe
  reserves and runs nothing until a witch presses Start: one batch per Start, the starter is the payer
  (`StationOperator`). Progress runs only while the payer is online, alive, in the dimension and within 8
  blocks (`MagicalProcessPolicy`); energy is debited once at completion through `WitchStatusService`, with
  practice awarded. Cancel consumes nothing. Others cannot take over; they can cancel only a batch whose
  lease ended. Start, Resume and Cancel are vanilla `clickMenuButton` messages (`StationOperations`, rate
  limit 5 per 20 ticks; no new packets) and the client shows `MagicalProcessPanel`. New statuses are
  NEEDS_WITCH, NEEDS_OPERATOR and NEEDS_ENERGY. The mortar and infusion pot also carry witch recipes
  (blinding dust, four oils).

**Products and applications (`apothecary/brewing/`).** Products are items (drinks, salve, oils, powders, garden
products) and two blocks (hung bundles, oil lantern). `application/` runs what a product does, once,
server side, through the permission boundary for garden actions; failures consume nothing. Effects
(`warmth`, pest repellent) live in `effect/`. The Apothecary creative tab belongs to `apothecary/stations/`
(stations depend on preparations, never the reverse).
- *Advanced preparations (Milestone 7).* `AdvancedProductCatalog` holds 16 (four drinks, six thrown vials,
  four oils, blinding dust, the moon essence ingredient), `FamiliarPreparationCatalog` ten familiar
  preparations (they refuse with "available with familiar bonding" until M9 installs
  `application/FamiliarPreparationApplications`) and `MixtureCatalog` 17 four-dose mixtures that
  the bottling bench portions into four bottles (gunpowder and paper variants give labelled vials, a flag
  in the item NBT). Effects are `MobEffect`s with ids `brewing/effects/<x>` (binding, sleep, steadfast,
  concealment, dazzled, thorns, undead and arthropod repellent), so persistence is vanilla's; steadfast
  grants 100 ticks of hard-control immunity afterwards. Entity tags decide who can be controlled:
  `undead`, `arthropods`, `hard_control_immune`, `hard_control_allowed`, `protected_entities` (villager,
  wandering trader by default); the item tag `coatable_weapons` limits the venom coating (the off-hand
  blade, 8 hits or 60 s). Thrown vials and dust (`apothecary/brewing/projectile/`) burst in radius 3, hit at most 32
  targets with line of sight, live 100 ticks, and are limited to 4 in flight per thrower and 128 per
  dimension behind a shared 20-tick throw cooldown. Every harmful application goes through
  `platform/permission/entity/EntityActionPermission` (below).

**Metadata carrier.** `apothecary/brewing/product/metadata/DrinkMetadata` is a hand-off for data riding from the
kettle's water bottle to the drink. Empty unless an adapter installs it.

**Entity permission (`platform/permission/entity/`).** One boundary for harmful entity actions:
`EntityActionPermission` refuses the user themselves, a gone target, anything in `protected_entities`,
another player's pet, a player target unless `preparationPvp` is on, the server allows PvP and teams
allow it, and anything inside spawn protection; when a recognised claim mod is loaded
(`ProtectionAdapters`) entity actions are restricted, because the mod cannot ask the claim mod.
`HarmAttribution` attributes the harm to a responsible player, and `checkProtect` is the narrower check wards use. Fake
players are treated as players.

**Compat adapters.** `platform/compat/thirst` registers the drinks with Thirst Was Taken by name (no
compile dependency) and installs the purity carrier; Farmer's Delight is tags only
(`data/server/compat`); EMI and JEI plugins (`client/compat`) are compile-only against their APIs and
describe recipes from the same `ProcessRecipeDescription`. Adapters load only when their mod is present.
`platform/compat/curios/CuriosCharmAdapter` (compile-only Curios API 5.14.1) is registered only when
Curios is loaded; `data/herbalwitch/curios/entities/charms.json` and the item tag `curios:charm` give
players a `charm` slot.

**Grimoire (`grimoire/`, Milestone 4).** A recipe-backed book of herbs, stations and processing recipes,
with each player's own discoveries marked. Advisory only: no crafting, processing, automation or viewer
reads it as permission, and later magic must not either.
- *Content:* `GrimoireContent` is an immutable index of `GrimoireEntry` values keyed by `EntryKey` (kind
  GUIDE/HERB/STATION/RECIPE plus a resource id, serialised `kind|ns:path`). Entries come from registered
  `GrimoireContentSource`s (`GuideEntries`, `StationEntries`, `HerbEntries`, `StationRecipeEntries`), which run
  on both sides from the same inputs, so server and client build the same index. Later milestones add a
  source for their own entries. Recipe pages come from the recipes themselves and never depend on editorial
  data; `ProcessingCategories` (`apothecary/stations/catalog`) supplies category/station facts, and
  `ProcessingRecipeBindings` resolves recipe types for adapters shared with EMI and JEI.
- *Editorial data:* `data/<ns>/grimoire_entries/**.json` (`GrimoireDefinitionLoader`) holds prose, grouping,
  links and hints only; mechanics fields are rejected, the whole set is validated, an invalid reload keeps
  the previous set, and guide links are checked within the set.
- *Build and reload ordering:* `ServerGrimoireContent` rebuilds with a new revision whenever the recipe
  manager, tag generation, editorial set or stable herb-summary identity changes, checked on access. Listeners hear each revision: `ContentSync` (on datapack sync it sends the
  editorial definitions and the server's herb conditions; recipe bodies come from vanilla recipe sync) and
  teaching, which cancels invitations whose recipe vanished or changed. Herb conditions are server-only, so clients use the synced summaries.
- *Discoveries:* a personal capability (`herbalwitch:grimoire/discoveries`, server players only), stored by
  `DiscoveryCodec` (schema 1 `{schema, keys}`; empty legacy data migrates; unknown kinds are kept; an
  unsupported schema, malformed data or more than 4096 keys is preserved untouched and read-only).
  `DiscoveryEvents` copies it on clone (death, End return) and sends private snapshots on login, respawn and
  dimension change; observation runs at login, on opening a reader and every 20 ticks, skipped when
  inventory and content are unchanged. `DiscoveryService` holds the policy: any herb form carried discovers
  the herb; a carried result discovers the recipe; so do all primary ingredients being familiar (a carried stack
  passes the ingredient, or a known herb's registered form does). Main inventory and offhand only. The
  service is the seam later features use to grant discoveries; the grimoire capability stays authoritative
  over witch status (Milestone 6): completing founding or initiation grants the witchcraft and action
  page discoveries through it, as records, never permission.
- *Research and teaching:* `ResearchService` (`ResearchEligibility`, `Familiarity`) at a stand or bookshelf:
  one familiar primary ingredient, instant, free, nothing consumed (no-primary recipes use base or package).
  `TeachingService` is pure rules with an injected clock and world (`TeachingWorld` is the server adapter and
  the GameTest seam): 8 blocks, same dimension, alive, not spectating; 600-tick expiry; one outgoing per
  teacher and one incoming per recipient; 100-tick cooldown; duplicates never refresh. Acceptance names the
  teacher and recipe and revalidates reader, range, teacher session and knowledge and the recipe
  fingerprint, then grants that one recipe. `TeachingEvents` holds one transient service per server and
  cancels on logout, death, dimension change, expiry, a closed teacher reader and content reload.
  Invitations start only from a research block reader.
- *Access and sessions:* the book is a stack of one with no knowledge NBT. `ResearchAccessBlock` is one cell with
  horizontal facing, needs support below, has no block entity and no storage. `GrimoireMenu` has zero slots
  and a server-issued session nonce; `GrimoireAccess` decides validity (book: a grimoire in inventory or
  offhand; block: still there, loaded, within 8 blocks, `mayInteract`) and vanilla closes the menu when it
  fails. `ReaderService` validates menu id, nonce, content revision and access before any change, computes
  missing counts against main inventory and offhand at most every 5 ticks and sends only changes
  (`IngredientRequirements` uses Forge `Ingredient.test`; `MissingIngredients` is a pure, deterministic allocator).
- *Network:* six bounded messages through `GrimoirePackets` (`ReaderRequest`, `TeachingRequest` to the server;
  `ReaderUpdate`, `DiscoveryUpdate`, `ContentUpdate`, `TeachingUpdate` to the client) with fixed directions;
  handlers are injected, and the client handler is installed by client code. `PayloadBounds` caps chunks at
  64 records and 24 KiB and the total at 4096; `ChunkAssembly` applies a snapshot atomically.
- *Client:* `client/grimoire` is the two-page `GrimoireScreen`, `GrimoireView`, widgets, and the
  `StationGrimoirePanel` shared through `GrimoirePanelHost` by process, hearth, altar and ward screens. Client state
  is cleared on disconnect and rebuilt on recipe and tag updates.

**Witchcraft (`magic/witch/`, `magic/ritual/`, Milestone 6).** Becoming a witch, the energy a witch
holds, and the altar where rituals happen. `witch` and `ritual` sit under a `magic/` grouping parent
(DECISIONS) to keep the Java root narrow. Numbers and procedures: docs/catalogue/witchcraft.md.
- *Dependencies:* `apothecary/brewing` -> `magic/witch` services for recovery. Magic presentation
  adapters contribute grimoire contracts; core grimoire consumers do not import magic implementations.
  Rituals use witch services, grimoire discovery contracts, botany/brewing and platform adapters.
  Bootstrap/showcase and optional compatibility wiring remain explicit integration bridges (STRUCTURE.md).
- *State (`witch/status`):* the capability `herbalwitch:witch/status`, server players only, schema 1:
  `{schema, initiated, initiation (uuid), energy, practice, tea, restful, sleepDay}`. Energy and
  practice are fixed-point hundredths; capacity is derived from practice and the rules and never saved.
  Missing data is a fresh non-witch. A missing or unsupported schema, or a malformed field, is preserved
  unchanged and the player is treated as a non-witch (logged; `/herbalwitch witch inspect` shows it).
  The clone copy runs once on death and End return regardless of keepInventory (the same
  revive/invalidate pattern as discoveries). Activities (rest, meditation) clear on death, logout and
  dimension change. `WitchStatusService` is the mutation API (named recovery, conversion and `spend` operations);
  M7 production uses its transaction and M9 familiars must. Mutable helpers stay in `status/`.
- *Rules (`witch/config`):* `WitchServerConfig` (`herbalwitch-server.toml`, per world) is validated as a
  whole into an immutable `WitchRules`. An invalid edit keeps the previous rules and logs. A valid
  change cancels pending invitations and running rituals, clamps energy without refilling and resends
  the rules. Clients receive them (`WitchRulesUpdate`) so the grimoire quotes live numbers; page prose
  never hard-codes one (a test enforces it).
- *Energy (`witch/energy`):* `EnergyBalance` holds the numbers; `EnergyRecovery`, `RecoveryConditions`,
  `DrinkRecovery` and `PracticeProgress` hold policy/arithmetic; `status/{EnergyService,SleepRecovery}` mutate state;
  `EnergyEvents` is the Forge adapter. Rest, meditation, the nature and moon bonus, sleep, energy tea
  and the restful infusion are specified in docs/catalogue/witchcraft.md. The sleep reward listens to
  `SleepFinishedTimeEvent`, the server's sleep-completion path, with `EnergyEvents.sleptThrough` as the
  seam tests drive.
- *Founding (`witch/initiation/founding`):* `FoundingSavedData` in the Overworld
  (`data/herbalwitch_founding.dat`, schema 1) with states OPEN, CLAIMED, COMMITTING, COMPLETE and
  NEEDS_REVIEW. CLAIMED is transient (a restart reopens). COMMITTING is a durable claim written straight
  to the file right before the irreversible completion; if that write fails, completion aborts before
  anything is consumed. A world loaded in COMMITTING, or an unreadable record, is NEEDS_REVIEW. The first
  conversion by any route closes OPEN. There is no automatic reopening; an operator resolves a review
  state (`/herbalwitch witch founding`).
- *Initiation (`witch/initiation`):* `InitiationConsent` holds invitations (one outgoing per witch, one
  incoming per recipient, expiry and cooldown, duplicates never extend). Acceptance revalidates both
  players, the altar, sessions, status, the offering and the cost, takes the invitation by its exact id
  (a replay or a spoofed id does nothing), then starts the ritual. There are no InitiationRequest or
  InitiationUpdate messages: consent requests ride the menu-bound `AltarRequest` and invitation state
  rides `AltarUpdate`; notifications are chat lines.
- *Altar (`magic/ritual`):* `altar/` is the 2 x 1 x 2 multi-cell block and its block entity;
  `menu/AltarMenu` has zero slots (offerings come from the payer's main inventory and offhand at
  completion, exact stacks captured at the start); `menu/AltarAccess` decides use (alive, non-spectator,
  same dimension, whole altar loaded, eyes within 4 blocks of the footprint with line of sight,
  `ActionPermissionService.mayUse`, adventure allowed); `session/` holds one operation per altar and one
  per player; `action/` holds founding, initiation, meditation and attunement behind `AltarActions`;
  `catalog/AltarCatalog` names the actions. Requests are rate-limited (`platform/network/RateLimit`,
  5 per second per player), bounded (`PayloadBounds.MAX_REQUEST_BYTES` 4096) and must name the open
  menu id, the session nonce and the current rules fingerprint. `ActionPermissionService.mayUse` is
  use, not build: loaded, border, height and spawn protection.
- *Snapshots:* the witch's own state reaches only that player: a new epoch on login, respawn and
  dimension change, a revision that increments, at most every 5 ticks, and immediately after a
  conversion or ritual result. The client ignores an older epoch or revision and clears on disconnect and
  on local player replacement. The witch and grimoire client caches are separate: the rules packet
  invalidates the grimoire content so pages requote the numbers.
- *Client:* `client/magic/witch` holds `WitchClientState`, `WitchClientConfig` (`herbalwitch-client.toml`
  `[hud]`) and `hud/` (`EnergyOverlay`, `EnergyHudLayout`); `client/magic/ritual` is `AltarScreen` and its
  widgets.
- *Grimoire:* the category "Witchcraft": `witchcraft/status`, `witchcraft/energy` and
  `witchcraft/recovery` (guide kind, `magic/witch/grimoire/WitchEntries`), the altar station page
  and `ritual/{founding,initiation,meditation,attunement}` (`magic/ritual/grimoire/AltarEntries`).

**Advanced witchcraft (`magic/charm/`, `magic/ritual/circle/`, `magic/ward/`, Milestone 7).** Numbers and
procedures: docs/catalogue/witchcraft.md. All numbers below are `WorkingRules` inside `WitchRules`
(`magic/witch/config/WorkingRules`): one config section group (`workings`, `wards`, `charms`), one sync and
one fingerprint. Defaults are `processingPercent` 100, `gardenBlessingCost` 15, `gardenBlessingTicks`
200, `homeProtectionCost` 20, `homeProtectionTicks` 200, `wardActivationCost` 20, `wardChargeSeconds`
600, `wardRadius` 8, `wardFocusRadius` 12, `wardHeight` 4, `wardsPerOwner` 4, `wardsPerDimension` 256,
`charmChargeSeconds` 300 and `preparationPvp` false.
- *Charms (`magic/charm`):* `CharmCatalog` names the Protective Sachet (every mob, 15% softer, at most 1
  heart), the Undead Amulet and the Arthropod Amulet (30%, at most 1.5 hearts). `CharmEquipment` finds a
  worn charm in the off hand or a Curios `charm` slot; `CharmEvents` counts wearing time with a
  per-player tick counter, adds used seconds to the stack's NBT and breaks the charm when
  `charmChargeSeconds` is spent; `CharmProtection` reduces the damage. The workbench makes them.
- *Ritual circle (`magic/ritual/circle`, `catalog/CircleCatalog`):* a 5x5 ring of 16 cells around the
  altar, placed whole through the item with the shared preview (`CirclePlacementPreview`); the item model is
  a flat sprite because the Blockbench compiler supports `multiblock.assembly: false`. `CircleValidation`
  checks the ring every 20 ticks: a broken circle cancels the ritual and consumes nothing, unloaded cells
  pause it. Circle rituals are altar actions that need a complete circle (`action/CircleRituals`): Garden
  Blessing (nettle powder, bone meal, soil tonic; 15 energy, 200 ticks) enriches the herb beds of the 5x5
  square around a chosen bed within 8 blocks and returns the bottle (`GardenBlessingPlan` is the pure
  plan); Home Protection (sage powder, amethyst shard; 20 energy, 200 ticks) charges a ward stone the
  witch may bind. The witch picks the bed or stone by looking at it before opening the altar
  (`client/magic/ritual/RitualTargets`).
- *Wards (`magic/ward`):* a ward stone is bound to its owner and activated by a witch within 8 blocks for
  20 energy (practice awarded); its charge lasts 600 loaded seconds and cannot be topped up while charged.
  Only the owner can deactivate, release (which unbinds), set the radius (1 to the maximum) and cycle the
  profile (undead, arthropods or both, default both); the controls are menu buttons (`WardMenu`,
  `WardAccess`), no packets. The radius is 8, or 12 with a functional hearth or stove within 4 blocks
  (rechecked on every sweep); the area is a box of radius by `wardHeight` 4. Limits: 4 per owner, 256
  active per dimension, 8 centres per chunk, 4096 records; active wards may not overlap. `WardSavedData`
  (`herbalwitch_wards`, schema 1) is world-wide, with `WardRecord` and `WardCodec`; `service/WardIndex` is
  the in-memory chunk index. `targeting/WardProtection` suppresses targeting through
  `LivingChangeTargetEvent` and bounded sweeps (4-block slices, 32 mobs per sweep, 256 per dimension tick,
  every 20 ticks). Breaking a stone retires its record; a copied record id is inert.
- *Client:* `client/magic/ward/{WardScreen, WardBoundaryPreview}`, `client/magic/ritual/widgets/CirclePanel`,
  `client/brewing/PreparationRenderers`, and ritual categories for JEI and EMI over
  `client/compat/ritual/RitualCardLayout`.
- *Grimoire:* station pages for the four new stations; the witchcraft pages for brewing, preparations and
  familiar preparations (`apothecary/stations/grimoire/BrewingEntries` and
  `apothecary/brewing/grimoire/AdvancedPreparationEntries`, registered by their owning features), wards, charms, the circle and the two circle rituals; 113 processing
  recipe pages.

**Client presentation.** `client/recipe/RecipeText` owns counted stack copies, requirement text
with an explicit current multiplier, and fractional duration strings. `ViewerLayout` is geometry
only. `EntryPage` owns page flow, clipping, prose, links and teaching; `RecipeBody` renders live
recipes. `GrimoirePanelHost` shares persistent panel initialization, tick, rendering, tooltip
precedence and input fallback across processing, hearth, altar and ward screens. Escape bypasses
focused search; other typing keys (including inventory) remain consumed by focused panel search.

**Drawing ownership.** `tools/art/drawing/{sprites,plant-motifs,items}.mjs` supplies reusable
primitives; seed layouts remain under `content/botany/seeds.mjs`. Herb and item outline algorithms
retain their distinct edge/darkening rules. GUI atlases use `interface/kit.mjs`; the reader's slot
call preserves its frame coordinates. Frozen manifest/PNG hashes guard byte parity.

## Whole-mod module map (plan section 2)

M9 binding introduces bounded per-bond `herbalwitch_familiar_<uuid>.dat` shards in the Overworld.
`FamiliarStore` derives owner keyset pages and creature lineage indexes in batches of 32 files; an
incomplete or corrupt index cannot authorize a mutation. Loaded candidates wait without a persistent
NoAI flag. Every shard uses the shared forced, atomic, verified writer for checkpoints and normal saves.
Prepared records and unresolved receipts become quarantined on restart; absence never means death.

Binding delegates to an altar contribution with a 200-loaded-tick session, circle validation, exact
offering snapshots and repeated care/ownership checks. `FamiliarBindingService` prepares recovery before
discarding the source, admits one exact candidate, spends through `WitchStatusService.spend`, and only
compacts after verified bond/player completion. Entity references contain identity and generation only.
M8 transport generations and released tombstones remain intact; exact converted lineage is contributed
to capture reconciliation, while the bond UUID index rejects old sources without binding capabilities.

Altar action descriptions are a bootstrap registry with permanent string identities. The original six
remain built-ins; contributed handlers share the same sessions and rate-limited packet path. Protocol 6
encodes action identities explicitly. The client presents one contributed action at a time and sends a
remembered creature/bond UUID; only the server determines whether it is a valid target.

The grimoire contributes a neutral menu action for the private familiar roster. Owner-index keyset
pages carry at most 32 summaries and 24 KiB through protocol 7, with a container nonce and owner
revision. Live observations update snapshots without making unloaded creatures actionable. The nearby
detail card reuses M8's inspection router and continuously checks book, ownership, sight and reach.
No inventory, receipt or recovery payload enters these summary packets.

Commands persist desired behavior and a monotonically ordered mobile reservation in the same shard.
The derived mobile index includes unloaded records and is separate from the live-entity index. Owner
logout/death touches only those assignments; cap changes keep the oldest. A single mutually exclusive
goal gives control, hazard escape and low-health retreat priority over its selected command. Fair
dimension queues bound real path creation and entity iterator queries. No command requests a chunk.
Loaded recall uses `TRANSFER_PREPARED`, a newer generation and the same exact-candidate admission and
verified payment receipt as binding; absence never licenses a spawn.

Paths under `src/main/java/io/github/dirtyredz/herbalwitch/`. Implemented so far: decor, apothecary/{botany,stations,brewing},
showcase, platform/permission, platform/network, compat, grimoire and magic/{witch,ritual,charm,ward}. Later milestones create the rest when needed.

| Home | Responsibility |
|---|---|
| `HerbalWitch.java` | Bootstrap only: registries, config, event wiring |
| `platform/network/`, `platform/config/`, `platform/permission/`, `platform/compat/<integration>/` | Channel + payload bounds; config registration; shared authorization boundary; optional integrations (foreign imports only here) |
| `magic/witch/{status,energy,initiation(+founding),config,network,command,registry}` (implemented, 0.5.0; the plan named top-level `witch/`) | Player capability and lifecycle; spending/recovery/progression; consent and founding; rules; wire; operator commands |
| `apothecary/botany/catalog/`, `crop/`, `foraging/`, `HerbWorldgen` | Herb definitions, growth, gathering, placement (worldgen never owns cultivation rules) |
| `apothecary/stations/everyday/<station>/`, `apothecary/stations/advanced/<station>/`, `apothecary/stations/process/{recipe,execution,authorization}` (matching is the flat `process/ProcessMatcher`) | One package per station (block, menu, recipe); shared processing by composition (implemented; advanced stations 0.6.0) |
| `apothecary/brewing/product/`, `effect/`, `application/`, `projectile/` | Products; effect mechanics; how an effect reaches a target; thrown preparations |
| `companions/animals/{trust,capture,taming}/`, `companions/familiars/{bond,command,ability,home,recovery,species}/` | Animal relationships; familiar bonds without one god manager |
| `magic/ritual/{catalog,registry,altar,circle,menu,session,action,network,grimoire,showcase}` (implemented, 0.5.0; `circle/` 0.6.0), `magic/ward/` and `magic/charm/` (implemented, 0.6.0), `magic/broom/`, `magic/wand/` | The altar, its menu, sessions and actions; the ritual circle; home wards; charms; broom entity; one class per wand mode |
| `shop/{catalog,trade,order,persistence,block}/` | Offers, transactions, orders, persistence, counter/register |
| `grimoire/{catalog,registry,access,content,knowledge,reading,network}/`, `grimoire/showcase/` (implemented) | Ids and registration; book, research blocks and reader menu (`access/{item,block,menu}`); recipe-backed entries and editorial reload; personal knowledge (`knowledge/{discovery,research,teaching}`); reader sessions and ingredient requirements (`reading/`, using `platform/inventory`); the six messages |
| `decor/{catalog,registry,showcase}/`, `decor/structure/`, `decor/behavior/{seating,storage,sleeping}/` (doors and lights sit directly in `decor/behavior/`) | Furniture (implemented, 0.4.0): the design and 94 pieces; shared structure (block, placement, support, shapes); one implementation per behaviour |
| `client/<feature>/`, `client/compat/<viewer>/` | Screens, HUD, renderers, particles; optional EMI/JEI plugins (`client/placement/` is the shared structure preview; `client/magic/witch/{hud}`, `client/magic/ritual/{widgets}` are M6) |
| `data/client/<feature>/`, `data/server/<feature>/` | Datagen per feature, consuming feature catalogues |

## Authority and synchronisation (rows not marked implemented are planned)

| State | Authoritative home | Client sees |
|---|---|---|
| Discoveries (implemented, Milestone 4: `herbalwitch:grimoire/discoveries`, schema 1) | Versioned player capability, server players only | Private snapshot on login/respawn/dimension change; bounded deltas |
| Witch status, energy, practice (implemented, Milestone 6: `herbalwitch:witch/status`, schema 1) | Versioned player capability, server players only | Private snapshot (epoch + revision) on login/respawn/dimension change and after conversions and ritual results; rules for the grimoire |
| Station inventories and progress (implemented; job schema 2) | Station block entity | Vanilla menu slots/data; Start, Resume and Cancel are menu buttons |
| Founding eligibility (implemented, Milestone 6) | Overworld `FoundingSavedData`, schema 1 | Eligibility/result only, through the altar's `AltarUpdate` |
| Animal trust/ownership | Entity capability (vanilla ownership kept) | Interacting player, trackers |
| Familiar bonds, commands, homes, recovery | Overworld `FamiliarSavedData` by bond id | Owner's roster; visual state nearby |
| Capture payloads | Overworld `CaptureSavedData` | Opaque token + display summary on the item |
| Wards (implemented, Milestone 7: `herbalwitch_wards`, schema 1) | World-wide `WardSavedData` (dimension-qualified records) plus an in-memory chunk index | The owner's ward screen (menu data); the boundary preview |
| Shop stock, proceeds, history | `ShopSavedData`; register stores shop id | Public offers; owner-only management |
| Furniture | Block properties (facing, cell, lit, bed and door states); the origin cell's storage entity; seat entities (never saved) | Vanilla updates; display contents in the block-entity update tag on change; open menus |

Rules: clients send intent and ids, never inventories, costs or results; servers revalidate sender,
menu/session, revision, distance, dimension, loaded chunk, ownership, permission, cooldown,
ingredients and energy immediately before mutating, on the server thread; payloads are bounded;
nothing loads a chunk because a packet named it and nothing adds chunk tickets; a record has one
authoritative owner; persistent formats carry schema versions with migration tests.

## Processing lifecycle (implemented, Milestone 3)

Recipe data, serialization and mutation are separate; matching never consumes inputs. A station
reserves inputs in their slots, advances progress in its block entity, revalidates inputs and output
capacity at completion, then commits once. Magical jobs (Milestone 7, see "Advanced stations and witch
recipes" above) also need an authorised witch nearby while progressing and at completion. Unloaded
stations simply stop; there is no catch-up simulation.
