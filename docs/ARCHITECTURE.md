# Architecture - The Herbal Witch

How the mod is put together, now and as milestones land. Code map: [STRUCTURE.md](../STRUCTURE.md).
Plan of record: milestones 1-12 (docs/FEATURES.md); traceability: docs/catalogue/.

## Today (Milestone 1)

```
FinishCatalog (18) x ArtSpikeCatalog (5)
        |
ArtSpikeRegistration --> DecorBlocks / DecorItems (90 + 90, ids decor/<family>/<piece>/<palette>/<finish>)
        |                         |
  block adapters            DecorCreativeTab, DecorTags
  (HorizontalFurniture,          |
   WallShelf, TableLamp,   client: DecorColorHandlers (tint 0 frame, 1 fabric; blocks and items)
   vanilla CarpetBlock)    data:   finish wrapper models + states, item models, names,
                                   base crafting + stonecutter restyling, loot, tags
```

- **One block per piece and finish.** No appearance NBT, no custom model loader, no block entities.
  Stacks, drops, recipes, pick-block and recipe viewers all work as vanilla.
- **Rendering** is baked models only. Geometry lives once per piece in an authored template; each
  finish's wrapper binds its frame material; colours come from tint handlers registered for both the
  block and its item, so placed, held and dropped look alike. The lit lamp uses a second template whose
  shade elements carry `forge_data` full-bright lighting.
- **State.** Furniture keeps all state in block properties (`facing`, `lit`): saved with the chunk,
  synchronised to clients by vanilla block updates. Only the server mutates (`TableLampBlock.use`).
  No custom network channel exists yet.
- **Support.** Shelf (wall face) and lamp (sturdy centre below) check support on placement and in
  `updateShape`; losing it returns air, and vanilla destroys the block with its drop. Rugs use
  vanilla carpet rules.
- **Acquisition.** Shaped crafting makes the dark oak base; one stonecutter recipe per target finish
  lists every other finish of the same piece, so restyling is 1:1 and cannot change the piece.

## Whole-mod module map (plan section 2)

Paths under `src/main/java/io/github/dirtyredz/herbalwitch/`. Later milestones create these when
needed; nothing is scaffolded early.

| Home | Responsibility |
|---|---|
| `HerbalWitch.java` | Bootstrap only: registries, config, event wiring |
| `platform/network/`, `platform/config/`, `platform/permission/`, `platform/compat/<integration>/` | Channel + payload bounds; config registration; shared authorization boundary; optional integrations (foreign imports only here) |
| `witch/status/`, `witch/energy/`, `witch/initiation/` | Player capability and lifecycle; spending/recovery/progression; consent and founding |
| `botany/catalog/`, `crop/`, `foraging/`, `worldgen/` | Herb definitions, growth, gathering, placement (worldgen never owns cultivation rules) |
| `stations/<station>/`, `stations/process/` | One package per station (block, menu, recipe); shared process helpers only after two stations duplicate them |
| `brewing/product/`, `effect/`, `application/` | Products; effect mechanics; how an effect reaches a target |
| `companions/animals/{trust,capture,taming}/`, `companions/familiars/{bond,command,ability,home,recovery,species}/` | Animal relationships; familiar bonds without one god manager |
| `magic/ritual/`, `magic/ward/`, `magic/broom/`, `magic/wand/` | Rituals and altar adapters; home wards; broom entity; one class per wand mode |
| `shop/{catalog,trade,order,persistence,block}/` | Offers, transactions, orders, persistence, counter/register |
| `grimoire/{discovery,content,teaching}/` | Discoveries, recipe-backed entries, consenting teaching |
| `decor/{catalog,material,registry,block,shape}/`, `decor/<behavior>/` | Furniture: finishes and pieces; behaviour families (seating, storage, surfaces, lighting, textiles, fixtures, openings) |
| `client/<feature>/`, `client/compat/<viewer>/` | Screens, HUD, renderers, particles; optional EMI/JEI plugins |
| `data/client/<feature>/`, `data/server/<feature>/` | Datagen per feature, consuming feature catalogues |

## Authority and synchronisation (planned)

| State | Authoritative home | Client sees |
|---|---|---|
| Witch status, energy, practice, discoveries | Versioned player capability | Private snapshot on login/respawn/dimension change; bounded deltas |
| Station inventories and progress | Station block entity | Vanilla menu slots/data |
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

## Processing lifecycle (planned, Milestone 3)

Recipe data, serialization and mutation are separate; matching never consumes inputs. A station
reserves inputs, advances progress in its block entity, revalidates inputs and output capacity at
completion, then commits once. Magical jobs also need an authorised witch nearby while progressing
and at completion. Unloaded stations simply stop; there is no catch-up simulation.
