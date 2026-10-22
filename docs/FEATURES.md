# Features - The Herbal Witch

Implemented versus planned, by milestone. Acceptance evidence is linked when a milestone closes.
Detail per entry: docs/catalogue/{decor,stations,products}.md.

## Implemented

Version 0.4.0 (local candidate): Milestone 5, the full furniture catalogue (94 pieces), is implemented on top of 0.3.0; see "0.4.0: the furniture catalogue" below. Headless acceptance passed (unit tests, GameTests on the ANU CI runner); owner visual and two-client checks are pending.

Version 0.3.0: Milestone 4, the grimoire, is implemented on top of the 0.2.0-dev.10 content below. Automated acceptance passed (118 unit tests, 97 GameTests); owner playtesting and the EMI/JEI profiles are pending.

Version 0.2.0-dev.10 (local candidate; builds on the 0.1.0 content; dev.2 answered the ten dev.1 art findings, dev.3 the dev.2 bundle and rack findings; dev.4 stopped the herb bed and rugs occluding neighbours, with an occlusion GameTest; dev.5 brought back 2D dried bundles and the kettle-sized mortar; dev.6 moves the one-off pieces to Blockbench authoring with no gameplay change; dev.7 adds the herb bed inner corner; dev.8 makes every texture of every Blockbench piece the piece's own, paintable without touching another piece; dev.9 puts the whole set at 32 x 32, two texels per model pixel, with no visible change; dev.10 brings in the owner's hand-reworked tea kettle from Blockbench, with a thin arched bail handle). Owner review of art, station UIs and viewers is pending.

| Feature | Milestone | Status | Evidence |
|---|---|---|---|
| Standalone Forge 1.20.1 build, datagen, isolated GameTests | 1 | done | `test build`, `runGameTestServer` green |
| One cottage design (dark wood, green and magenta accents, gold trim, ivy, untinted materials) | 1 | done; restyled in 0.2.0-dev.1; owner art review pending | DecorResourcesTest, GeneratedResourcesTest |
| Armchair, Apothecary Counter, Small Wall Shelf, Table Lamp, Small Rug: the five 0.1.0 blocks, `decor/<family>/<piece>` (the catalogue grew to 94 pieces in 0.4.0, below; the shelf became a display) | 1 | done; owner art review pending | DecorContentGameTests |
| Facing, wall/surface/floor support, support-loss drops | 1 | done | DecorPlacementGameTests |
| Lamp on/off with light 14, saved in the block state | 1 | done; two-client sync is an owner check | DecorInteractionGameTests |
| Fourteen herbs (4 ages, crop or bush), seeds, fresh herbs, herb bed, starter recipes | 2 | done; owner art review pending | HerbCatalogTest, BotanyResourcesTest, BotanyModelsTest, GardenBedGameTests, HerbGrowthGameTests, HerbPlantingGameTests |
| Harvest by use, grass foraging, reloadable conditions, habitat worldgen | 2 | done | HerbHarvestGameTests, HerbAcquisitionGameTests, HerbWorldgenGameTests, HerbConditionTest |
| `/herbalwitch showcase [section]` (operators): every registered block, labelled, on a plot it levels and floors itself | 2 | done | ShowcaseCommandGameTests, ShowcaseBoundaryGameTests, ShowcaseContentGameTests, ShowcasePlanTest, ShowcaseCoverageTest |
| Five stations (drying rack, mortar, tea kettle, hearth, infusion pot) with menus, in-slot reservations, heat, automation sides, no catch-up | 3 | done; owner UI/art review pending | StationProcessingGameTests, ProcessBehaviourGameTests, ProcessReservationTest, ProcessCompletionTest, StationResourcesTest |
| Bundles, powders, 7 drinks, salve, oils, garden products, oil lantern, warmth and pest-repellent effects, resin | 3 | done | PreparationUseGameTests, PreparationBlockGameTests, PreparationResourcesTest |
| EMI and JEI categories; Farmer's Delight heat both ways; Thirst Was Taken hydration and purity | 3 | done; viewers are owner checks | CompatibilityGameTests (`-PcompatProfile=all`) |

### 0.4.0: the furniture catalogue (Milestone 5)

Status: implemented; headless verified (unit tests, GameTests on the ANU CI runner). Owner visual and
two-client checks pending. Detail: docs/catalogue/decor.md (exact catalogue), docs/ARCHITECTURE.md
("Decor"), docs/DECISIONS.md ("0.4.0"). The showcase is now 117 x 8 x 28 (it was 86 x 8 x 28).

| Feature | Status |
|---|---|
| 94 canonical pieces in seven families (seating 13, storage 27, surfaces 15, lighting 8, textiles 6, fixtures 22, openings 3); ids `decor/<family>/<piece>`, 89 added in waves A (22), B (30), C (37) | implemented; art is agent first drafts, owner review pending |
| Checked, transactional placement: one resolver for preview and server, whole piece or nothing, Forge multi-place event, see-through preview with green/red outline; wall, ceiling, corner and carpet support | implemented |
| Seating (aimed seat, invisible never-saved seat entity, stand on logout or death), including the swing and desk chair | implemented |
| Cabinets (27 or 54 slots, hopper and item-handler access on every cell) and displays (3, 6 or 9 one-item slots, items drawn on the shelf) | implemented |
| Beds (vanilla sleep and respawn, one sleeper, canopy included) and the door (vanilla door rules) | implemented |
| Lights toggle by use, light 14 while lit, no fuel or heat | implemented |
| Lookalikes (work table, altar table, decorative drying rack, kitchen range, fireplace, firepit) are decoration, not stations | implemented |
| One public grimoire page per piece (size, mounting, use, crafting; lookalikes link their station) | implemented |
| Showcase section `decor`: footprint-aware grid with signs, walls and ceilings, lit lights, stocked storage, the door in its four states | implemented |
| Blockbench projects for all 89 new pieces, compiled to the same resource paths; the small wall shelf lost its modelled bottles | implemented; owner refines |
| Optional-mod profiles (EMI, JEI, Farmer's Delight, Thirst) | not rerun for this milestone |

Owner checks pending: placement preview equals result across rotations, walls, ceilings and door hinges;
seating pose and dismount; bed sleep and wake; two-player occupancy of seats and beds; displayed items and
simultaneous storage access on two clients; door collision; lighting; large item transforms;
neighbouring-block occlusion and shaders; and the Blockbench edit workflow on representative new pieces.

### 0.3.0: the Grimoire (Milestone 4)

Status: implemented; automated acceptance passed (unit tests and GameTests). Owner checks pending. Detail:
README "The Grimoire", docs/ARCHITECTURE.md, docs/catalogue/stations.md.

| Feature | Status |
|---|---|
| Grimoire book, Grimoire Stand and Witch's Bookshelf (permanent ids `grimoire/book`, `stand`, `bookshelf`), shapeless crafting, "Apothecary" tab | implemented |
| Recipe-backed reader: herbs, stations, processing recipes, guide pages; editorial prose from data packs (`grimoire_entries`) | implemented |
| Personal discoveries (capability, schema 1, private sync, survives death) with an observation policy | implemented |
| Free, instant, shallow research at the research blocks | implemented |
| Missing ingredients against main inventory and offhand (water bottles, returns) | implemented |
| Consenting teaching of one recipe, with expiry, cooldown and revalidation | implemented |
| "Grimoire" button and book panel on station and hearth screens | implemented |
| Six bounded network messages on channel `herbalwitch:main` | implemented |
| Showcase section `grimoire`; showcase now 86 x 8 x 28 | implemented |
| Stand and bookshelf Blockbench projects, book sprite and reader atlas | agent-authored first drafts; owner refines |
| Discovery gating of crafting, processing, automation or EMI/JEI | not planned (advisory by design) |
| Viewer transfer handlers, hiding undiscovered recipes in EMI/JEI | not implemented |

Owner checks pending: follow acquisition, drying, grinding and brewing using only the book; compare
missing counts with the inventory (water and returned bottles included); station links and a reload with
neither, either and both viewers; consent, privacy, reconnect and death with two real clients; model
transforms, readable UI, targeting, support, neighbour occlusion and shaders. EMI/JEI runtime profiles
(`emi`, `jei`, `viewers`) were not run for 0.3.0.

### 0.2.0-dev.1: art pass

Status: implemented, unit tests and GameTests passing; owner review in the trial instance is pending.

| Feature | Status |
|---|---|
| Herb beds join: four side properties, soil instead of rails where the same bed is adjacent; connects on placement, setBlock, neighbour change, rotate/mirror; old beds repair on random tick | implemented |
| Herbs rooted in the soil: planted-cross template for ages 0-2, a 3D mature model per herb (14), shared swatch atlas | implemented |
| Stations redrawn in one house style; drying rack and hearth become 2x2 multiblocks (same block entity, menu, recipes) | implemented |
| New Witch's Stove: two-wide range, two burners, hearth fuel rules and block entity; heat-source tags; craftable | implemented |
| Furniture restyled (same five ids) | implemented |
| `tools/generate-models.mjs` writes all models (since dev.6 the one-off pieces come from `tools/compile-blockbench.mjs`); `tools/preview-models.mjs` renders headless previews | implemented |
| Showcase: joined-bed garden section, stove section, multiblocks placed cell by cell | implemented |

### 0.2.0-dev.6: Blockbench authoring

No new gameplay: the same blocks, items, states and textures, authored differently.

| Feature | Status |
|---|---|
| Twelve one-off pieces (six stations, five decor pieces, oil lantern) editable as Blockbench projects, compiled to the same resource paths | implemented; appearance identical to dev.5 |
| Live dev-art resource pack for a trial instance (`--dev-pack ... --watch`, F3+T) | implemented; owner check in game pending |
| Procedural families (herbs, herb beds, hung bundles, shared materials) | unchanged |

## Planned

| Milestone | Scope |
|---|---|
| 6 | Initiation and energy: witch capability, consent, founding ritual, altar, energy HUD and recovery |
| 7 | Advanced brewing, charms, rituals, wards: cauldron, still, bottling bench, charm bench, ritual circle, ward stone |
| 8 | Ordinary animals: trust, bait, calming, sleep, earned taming, healing, capture/release |
| 9 | Multiple familiars: binding, roster, seven commands, homes, equipment, abilities, familiar brews, revival |
| 10 | Broom and wand |
| 11 | Apothecary commerce: register, counters, stock, offers, proceeds, history, order board |
| 12 | Complete-release qualification |

Not planned: publishing, pack integration or a release before the owner decides.
