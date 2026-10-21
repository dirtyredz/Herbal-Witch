# Features - The Herbal Witch

Implemented versus planned, by milestone. Acceptance evidence is linked when a milestone closes.
Detail per entry: docs/catalogue/{decor,stations,products}.md.

## Implemented

Version 0.1.0-dev.4 (local candidate). Owner review of art, station UIs and viewers is pending.

| Feature | Milestone | Status | Evidence |
|---|---|---|---|
| Standalone Forge 1.20.1 build, datagen, isolated GameTests | 1 | done | `test build`, `runGameTestServer` green |
| One cottage design (dark wood, green and pink accents, vine leaves, untinted materials) | 1 | done; owner art review pending | DecorResourcesTest, GeneratedResourcesTest |
| Armchair, Apothecary Counter, Small Wall Shelf, Table Lamp, Small Rug: five blocks, `decor/<family>/<piece>` | 1 | done; owner art review pending | DecorContentGameTests |
| Facing, wall/surface/floor support, support-loss drops | 1 | done | DecorPlacementGameTests |
| Lamp on/off with light 14, saved in the block state | 1 | done; two-client sync is an owner check | DecorInteractionGameTests |
| Fourteen herbs (4 ages, crop or bush), seeds, fresh herbs, herb bed, starter recipes | 2 | done; owner art review pending | HerbCatalogTest, BotanyResourcesTest, BotanyModelsTest, GardenBedGameTests, HerbGrowthGameTests, HerbPlantingGameTests |
| Harvest by use, grass foraging, reloadable conditions, habitat worldgen | 2 | done | HerbHarvestGameTests, HerbAcquisitionGameTests, HerbWorldgenGameTests, HerbConditionTest |
| `/herbalwitch showcase [section]` (operators): every registered block, labelled, on a flat plot | 2 | done | ShowcaseCommandGameTests, ShowcaseBoundaryGameTests, ShowcaseContentGameTests, ShowcasePlanTest, ShowcaseCoverageTest |
| Five stations (drying rack, mortar, tea kettle, hearth, infusion pot) with menus, in-slot reservations, heat, automation sides, no catch-up | 3 | done; owner UI/art review pending | StationProcessingGameTests, ProcessBehaviourGameTests, ProcessReservationTest, ProcessCompletionTest, StationResourcesTest |
| Bundles, powders, 7 drinks, salve, oils, garden products, oil lantern, warmth and pest-repellent effects, resin | 3 | done | PreparationUseGameTests, PreparationBlockGameTests, PreparationResourcesTest |
| EMI and JEI categories; Farmer's Delight heat both ways; Thirst Was Taken hydration and purity | 3 | done; viewers are owner checks | CompatibilityGameTests (`-PcompatProfile=all`) |

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
