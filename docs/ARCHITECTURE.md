# Architecture - The Herbal Witch

How the mod is put together. Code map: [STRUCTURE.md](../STRUCTURE.md).
Plan of record: milestones 1-12 (docs/FEATURES.md); traceability: docs/catalogue/.

## Current system (0.2.0-dev.3: Milestones 1-3 plus the art pass and its first review fixes)

Everything below is server-authoritative; clients get vanilla block updates, menu slots and menu data.
No custom network channel exists. Paths are under `src/main/java/io/github/dirtyredz/herbalwitch/`.

**Decor (`decor/`).** One cottage design: `FinishCatalog` holds a single entry and the five pieces
(`ArtSpikeCatalog`) register as `decor/<family>/<piece>`, one block and item each. No appearance NBT,
no block entities, no tints: materials are untinted textures bound directly by authored templates, and
blockstates use the templates directly. State lives in block properties (`facing`, `lit`). The shelf
(wall face) and lamp (sturdy centre below) check support on placement and in `updateShape`; the rug is
vanilla carpet. Only the server toggles the lamp. The 0.2.0 restyle changed models and textures only.

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

**Model and preview tooling.** `tools/generate-models.mjs` (with `tools/models/**`) writes every
station, furniture, herb (template and mature) and herb-bed model, and the part models above.
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

## Whole-mod module map (plan section 2)

Paths under `src/main/java/io/github/dirtyredz/herbalwitch/`. Implemented so far: decor, botany, stations, brewing,
showcase, platform/permission and compat. Later milestones create the rest when needed.

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
| `grimoire/{discovery,content,teaching}/` | Discoveries, recipe-backed entries, consenting teaching |
| `decor/{catalog,registry,block,shape}/`, `decor/<behavior>/` | Furniture: the design and pieces; behaviour families (seating, storage, surfaces, lighting, textiles, fixtures, openings) |
| `client/<feature>/`, `client/compat/<viewer>/` | Screens, HUD, renderers, particles; optional EMI/JEI plugins |
| `data/client/<feature>/`, `data/server/<feature>/` | Datagen per feature, consuming feature catalogues |

## Authority and synchronisation (rows not marked implemented are planned)

| State | Authoritative home | Client sees |
|---|---|---|
| Witch status, energy, practice, discoveries | Versioned player capability | Private snapshot on login/respawn/dimension change; bounded deltas |
| Station inventories and progress (implemented) | Station block entity | Vanilla menu slots/data |
| Founding eligibility | Overworld `FoundingSavedData` | Eligibility/result only |
| Animal trust/ownership | Entity capability (vanilla ownership kept) | Interacting player, trackers |
| Familiar bonds, commands, homes, recovery | Overworld `FamiliarSavedData` by bond id | Owner's roster; visual state nearby |
| Capture payloads | Overworld `CaptureSavedData` | Opaque token + display summary on the item |
| Wards | Overworld `WardSavedData` (dimension-qualified) | Owner views; indicators |
| Shop stock, proceeds, history | `ShopSavedData`; register stores shop id | Public offers; owner-only management |
| Furniture | Block properties (now); storage furniture's block entity (later) | Vanilla updates; open menus |

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
