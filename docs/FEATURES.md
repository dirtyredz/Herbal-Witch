# Features - The Herbal Witch

Implemented versus planned, by milestone. Acceptance evidence is linked when a milestone closes.
Detail per entry: docs/catalogue/{decor,stations,products}.md.

## Implemented

Version 0.2.0-dev.9 (local candidate; builds on the 0.1.0 content; dev.2 answered the ten dev.1 art findings, dev.3 the dev.2 bundle and rack findings; dev.4 stopped the herb bed and rugs occluding neighbours, with an occlusion GameTest; dev.5 brought back 2D dried bundles and the kettle-sized mortar; dev.6 moves the one-off pieces to Blockbench authoring with no gameplay change; dev.7 adds the herb bed inner corner; dev.8 makes every texture of every Blockbench piece the piece's own, paintable without touching another piece; dev.9 puts the whole set at 32 x 32, two texels per model pixel, with no visible change). Owner review of art, station UIs and viewers is pending.

| Feature | Milestone | Status | Evidence |
|---|---|---|---|
| Standalone Forge 1.20.1 build, datagen, isolated GameTests | 1 | done | `test build`, `runGameTestServer` green |
| One cottage design (dark wood, green and magenta accents, gold trim, ivy, untinted materials) | 1 | done; restyled in 0.2.0-dev.1; owner art review pending | DecorResourcesTest, GeneratedResourcesTest |
| Armchair, Apothecary Counter, Small Wall Shelf, Table Lamp, Small Rug: five blocks, `decor/<family>/<piece>` | 1 | done; owner art review pending | DecorContentGameTests |
| Facing, wall/surface/floor support, support-loss drops | 1 | done | DecorPlacementGameTests |
| Lamp on/off with light 14, saved in the block state | 1 | done; two-client sync is an owner check | DecorInteractionGameTests |
| Fourteen herbs (4 ages, crop or bush), seeds, fresh herbs, herb bed, starter recipes | 2 | done; owner art review pending | HerbCatalogTest, BotanyResourcesTest, BotanyModelsTest, GardenBedGameTests, HerbGrowthGameTests, HerbPlantingGameTests |
| Harvest by use, grass foraging, reloadable conditions, habitat worldgen | 2 | done | HerbHarvestGameTests, HerbAcquisitionGameTests, HerbWorldgenGameTests, HerbConditionTest |
| `/herbalwitch showcase [section]` (operators): every registered block, labelled, on a plot it levels and floors itself | 2 | done | ShowcaseCommandGameTests, ShowcaseBoundaryGameTests, ShowcaseContentGameTests, ShowcasePlanTest, ShowcaseCoverageTest |
| Five stations (drying rack, mortar, tea kettle, hearth, infusion pot) with menus, in-slot reservations, heat, automation sides, no catch-up | 3 | done; owner UI/art review pending | StationProcessingGameTests, ProcessBehaviourGameTests, ProcessReservationTest, ProcessCompletionTest, StationResourcesTest |
| Bundles, powders, 7 drinks, salve, oils, garden products, oil lantern, warmth and pest-repellent effects, resin | 3 | done | PreparationUseGameTests, PreparationBlockGameTests, PreparationResourcesTest |
| EMI and JEI categories; Farmer's Delight heat both ways; Thirst Was Taken hydration and purity | 3 | done; viewers are owner checks | CompatibilityGameTests (`-PcompatProfile=all`) |

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
| 4 | Grimoire: discovery, station recipes, missing ingredients, research stand/bookshelf, teaching |
| 5 | Decor waves A/B/C: the rest of the catalogue by behaviour family; seating, storage, beds, doors, multiblocks |
| 6 | Initiation and energy: witch capability, consent, founding ritual, altar, energy HUD and recovery |
| 7 | Advanced brewing, charms, rituals, wards: cauldron, still, bottling bench, charm bench, ritual circle, ward stone |
| 8 | Ordinary animals: trust, bait, calming, sleep, earned taming, healing, capture/release |
| 9 | Multiple familiars: binding, roster, seven commands, homes, equipment, abilities, familiar brews, revival |
| 10 | Broom and wand |
| 11 | Apothecary commerce: register, counters, stock, offers, proceeds, history, order board |
| 12 | Complete-release qualification |

Not planned: publishing, pack integration or a release before the owner decides.
