# Architecture - The Herbal Witch

How the mod is put together. Code map: [STRUCTURE.md](../STRUCTURE.md).
Plan of record: milestones 1-12 (docs/FEATURES.md); traceability: docs/catalogue/.

## Current system (0.4.0: Milestones 1-5 plus the art pass; furniture is Milestone 5)

Everything below is server-authoritative; clients get vanilla block updates, menu slots and menu data.
The one custom channel, `herbalwitch:main` (protocol "2", `platform/network/HerbalWitchNetwork`), carries
only the grimoire's six bounded messages (protocol 2: entry keys may name furniture pages). Paths are
under `src/main/java/io/github/dirtyredz/herbalwitch/`.

**Decor (`decor/`).** One cottage design (`FinishCatalog`, a single entry) and 94 pieces described in
seven family catalogues (`decor/catalog/pieces/`), each a `FurnitureSpec`: id, family, art group, wave,
behaviour (`FurnitureBehavior`), mount (`MountSpec`: support and cells, `(right, up, depth)` from the
origin), seats or slots. `FurnitureCatalog.ALL` is what is registered (every wave up to `DELIVERED`).
- *Composition:* `registry/DecorBlockFactory` builds one class per behaviour: `structure/DecorAssemblyBlock`
  (static pieces and multi-cell rugs: facing, cell index, shape, support, teardown),
  `behavior/seating/SeatingBlock`, `behavior/storage/StorageFurnitureBlock`,
  `behavior/lighting/DecorLightBlock` (lit, light 14 on every cell), `behavior/sleeping/DecorBedBlock`
  (vanilla `BedBlock` with a `cell` property) and `behavior/openings/DecorDoorBlock` (vanilla `DoorBlock`);
  the 0.1.0 small rug stays vanilla carpet. Support (`structure/FurnitureSupport`): floor, wall, ceiling,
  corner or carpet, checked per supported cell and never through an unloaded chunk.
- *Placement:* `structure/FurniturePlacementItem` asks the block for its origin state (facing, a door's
  hinge, a bed's direction), resolves the plan (`platform/placement/PlacementResolver`: the anchor cell on
  the aimed cell; a hanging piece anchors its top cell), checks every cell (`PlacementChecks`: loaded,
  border, height, replaceable, entities, support, then the build permission) and commits it
  (`PlacementCommit`, inside Forge's snapshot capture: one multi-place event, deferred neighbour updates,
  a full restore including the item on cancellation). The client preview (`client/decor/placement/`)
  draws the same plan as see-through models with a green or red outline.
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
- *Grimoire:* `grimoire/content/reference/DecorEntries` contributes one public `furniture|` page per piece
  (blurb, size, mounting, use, seats or slots, crafting; lookalikes link their station).

**Botany (`botany/`).**
- *State and growth:* a herb is a plant block with age 0..3 (crop or bush form). It grows by vanilla
  random ticks and needs crop light to grow, but survives without light so worldgen can place it. Seeds
  plant only on farmland or the herb bed (`garden/`, farmland-style moisture, never trampled).
- *Harvest:* use on a mature plant gives 2 fresh + 1 seed and resets the age (bushes to 1); a favoured
  harvest or an enriched bed adds fresh herbs. Adventure players cannot harvest. Breaking drops through
  the loot table.
- *Conditions:* data per herb (`data/herbalwitch/herb_conditions/<group>/<herb>.json`: habitat tag,
  discovery chance, harvest bonus, liked conditions). `botany/conditions` owns the reload listener; it
  validates the whole set and rejects the entire reload on any error, keeping the previous set. The
  live evaluator reads rain, low sky, night and full moon.
- *Foraging:* a global loot modifier on player-broken grass-like blocks adds starter seeds and one
  habitat herb at its discovery chance.
- *Worldgen:* per herb a configured and placed feature, added by per-group biome modifiers onto habitat
  biome tags. Existing chunks are not retrofitted.

**Herb bed joining (`botany/garden/`).** `HerbBedBlock` carries boolean `north/east/south/west`
properties, computed by `GardenBedConnections`: a side is joined when the same bed block stands beside
it at the same height. They are set on placement, in `onPlace` for direct `setBlock`, in `updateShape`
on neighbour change, and on rotate/mirror; beds saved before 0.2.0 are repaired by their random tick.
Four more booleans (`inner_north_east` ... `inner_west_north`) flag an inside corner (both sides joined, no
bed on the diagonal): set by `refreshCorners`, and refreshed on diagonal beds by `onPlace`/`onRemove`.
Connection is visual only; moisture and enrichment are unchanged. The blockstate is multipart
(`data/client/botany/garden/HerbBedBlockstates` + `HerbBedModels`) over part models under
`models/block/botany/garden/parts/{wood,soil,moist}`.

**Art pipeline.** Sources: procedural textures (`tools/art/`), procedural models (`tools/models/`:
herbs, herb beds, hung bundles; the grimoire's book sprite and reader atlas in `tools/art/grimoire/`) and
Blockbench projects (`src/art/blockbench/`: stations, furniture, oil lantern, the grimoire stand and
bookshelf; a `.bbmodel` plus a `piece.json` each). Three producers (`generate-textures.mjs`,
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
Each feature registers its own section; coverage tests require every block to appear.

**Stations (`stations/`).**
- *Composition:* `StationBlock`, `StationEntity` and `StationMenu` carry block and menu boilerplate.
  Processing rules live in `process/`: `ProcessInventory` (slot roles; reserved counts stay in their
  slots), `ProcessJob` (exact shapeless matching; recipe id + content fingerprint), `ProcessCompletion`
  (pure capacity arithmetic), `ProcessRecipeJson`/`ProcessRecipeIndex` (one bounded format; per-type
  sorted cache). `ProcessStation` ties them to a block entity by composition.
- *Reservations and completion:* ingredients stay in their slots, counted as reserved, and cannot be
  extracted or swapped by menus or automation. At completion the result and all returns must fit before
  anything is consumed; then one commit.
- *Menus, capabilities, sides:* vanilla data slots, distance and block checks, quick-move that never
  moves reserved items, no custom packets. Item-handler capability per side: ingredients from above,
  base/package/fuel from the sides, products and returns out of the bottom. Closing a menu leaves the
  station running.
- *Heat:* `heat/` reads tags: `herbalwitch:heat_sources` (must be LIT where the block has that
  property) directly below, or one `herbalwitch:heat_conductors` block then a source. Waterlogged gives
  none. Heated stations show Needs heat until then. The Witch's Stove (`stations/stove/`) is a heat source
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
  stay, progress resets). Jobs save with schema version 1. There is no catch-up simulation: unloaded
  stations do not progress.

**Products and applications (`brewing/`).** Products are items (drinks, salve, oils, powders, garden
products) and two blocks (hung bundles, oil lantern). `application/` runs what a product does, once,
server side, through the permission boundary for garden actions; failures consume nothing. Effects
(`warmth`, pest repellent) live in `effect/`. The Apothecary creative tab belongs to `stations/`
(stations depend on preparations, never the reverse).

**Metadata carrier.** `brewing/product/metadata/DrinkMetadata` is a hand-off for data riding from the
kettle's water bottle to the drink. Empty unless an adapter installs it.

**Compat adapters.** `platform/compat/thirst` registers the drinks with Thirst Was Taken by name (no
compile dependency) and installs the purity carrier; Farmer's Delight is tags only
(`data/server/compat`); EMI and JEI plugins (`client/compat`) are compile-only against their APIs and
describe recipes from the same `ProcessRecipeDescription`. Adapters load only when their mod is present.

**Grimoire (`grimoire/`, Milestone 4).** A recipe-backed book of herbs, stations and processing recipes,
with each player's own discoveries marked. Advisory only: no crafting, processing, automation or viewer
reads it as permission, and later magic must not either.
- *Content:* `GrimoireContent` is an immutable index of `GrimoireEntry` values keyed by `EntryKey` (kind
  GUIDE/HERB/STATION/RECIPE plus a resource id, serialised `kind|ns:path`). Entries come from registered
  `GrimoireContentSource`s (`GuideEntries`, `StationEntries`, `HerbEntries`, `StationRecipeEntries`), which run
  on both sides from the same inputs, so server and client build the same index. Later milestones add a
  source for their own entries. Recipe pages come from the recipes themselves and never depend on editorial
  data; `ProcessingCategories` (`stations/description`) is the one recipe-type-to-station mapping shared with
  EMI and JEI.
- *Editorial data:* `data/<ns>/grimoire_entries/**.json` (`GrimoireDefinitionLoader`) holds prose, grouping,
  links and hints only; mechanics fields are rejected, the whole set is validated, an invalid reload keeps
  the previous set, and guide links are checked within the set.
- *Build and reload ordering:* `ServerGrimoireContent` rebuilds with a new revision whenever the recipe
  manager, the tag generation, the editorial set or the herb conditions are a different instance than at the
  last build, checked on access. Listeners hear each revision: `ContentSync` (on datapack sync it sends the
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
  even when witch status (Milestone 6) arrives.
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
  `StationGrimoirePanel` button on station and hearth screens (station menus are untouched). Client state
  is cleared on disconnect and rebuilt on recipe and tag updates.

## Whole-mod module map (plan section 2)

Paths under `src/main/java/io/github/dirtyredz/herbalwitch/`. Implemented so far: decor, botany, stations, brewing,
showcase, platform/permission, platform/network, compat and grimoire. Later milestones create the rest when needed.

| Home | Responsibility |
|---|---|
| `HerbalWitch.java` | Bootstrap only: registries, config, event wiring |
| `platform/network/`, `platform/config/`, `platform/permission/`, `platform/compat/<integration>/` | Channel + payload bounds; config registration; shared authorization boundary; optional integrations (foreign imports only here) |
| `witch/status/`, `witch/energy/`, `witch/initiation/` | Player capability and lifecycle; spending/recovery/progression; consent and founding |
| `botany/catalog/`, `crop/`, `foraging/`, `worldgen/` | Herb definitions, growth, gathering, placement (worldgen never owns cultivation rules) |
| `stations/everyday/<station>/`, `stations/process/` | One package per station (block, menu, recipe); shared processing by composition (implemented) |
| `brewing/product/`, `effect/`, `application/` | Products; effect mechanics; how an effect reaches a target |
| `companions/animals/{trust,capture,taming}/`, `companions/familiars/{bond,command,ability,home,recovery,species}/` | Animal relationships; familiar bonds without one god manager |
| `magic/ritual/`, `magic/ward/`, `magic/broom/`, `magic/wand/` | Rituals and altar adapters; home wards; broom entity; one class per wand mode |
| `shop/{catalog,trade,order,persistence,block}/` | Offers, transactions, orders, persistence, counter/register |
| `grimoire/{catalog,registry,access,content,knowledge,reading,network}/`, `grimoire/showcase/` (implemented) | Ids and registration; book, research blocks and reader menu (`access/{item,block,menu}`); recipe-backed entries and editorial reload; personal knowledge (`knowledge/{discovery,research,teaching}`); reader sessions and the missing-ingredient allocator (`reading/`); the six messages |
| `decor/{catalog,registry,showcase}/`, `decor/structure/`, `decor/behavior/{seating,storage,sleeping,openings,lighting}/` | Furniture (implemented, 0.4.0): the design and 94 pieces; shared structure (block, placement, support, shapes); one implementation per behaviour |
| `client/<feature>/`, `client/compat/<viewer>/` | Screens, HUD, renderers, particles; optional EMI/JEI plugins |
| `data/client/<feature>/`, `data/server/<feature>/` | Datagen per feature, consuming feature catalogues |

## Authority and synchronisation (rows not marked implemented are planned)

| State | Authoritative home | Client sees |
|---|---|---|
| Discoveries (implemented, Milestone 4: `herbalwitch:grimoire/discoveries`, schema 1) | Versioned player capability, server players only | Private snapshot on login/respawn/dimension change; bounded deltas |
| Witch status, energy, practice | Versioned player capability (Milestone 6) | Private snapshot on login/respawn/dimension change; bounded deltas |
| Station inventories and progress (implemented) | Station block entity | Vanilla menu slots/data |
| Founding eligibility | Overworld `FoundingSavedData` | Eligibility/result only |
| Animal trust/ownership | Entity capability (vanilla ownership kept) | Interacting player, trackers |
| Familiar bonds, commands, homes, recovery | Overworld `FamiliarSavedData` by bond id | Owner's roster; visual state nearby |
| Capture payloads | Overworld `CaptureSavedData` | Opaque token + display summary on the item |
| Wards | Overworld `WardSavedData` (dimension-qualified) | Owner views; indicators |
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
capacity at completion, then commits once. Magical jobs (later milestones) also need an authorised
witch nearby while progressing and at completion. Unloaded stations simply stop; there is no catch-up
simulation.
