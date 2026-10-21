# Features - The Herbal Witch

Implemented versus planned, by milestone. Acceptance evidence is linked when a milestone closes.
Detail per entry: docs/catalogue/{decor,stations,products}.md.

## Implemented

| Feature | Milestone | Status | Evidence |
|---|---|---|---|
| Standalone Forge 1.20.1 build, datagen, isolated GameTests | 1 | done (0.1.0-dev.1) | `test build`, `runGameTestServer` green |
| 18 coordinated finishes (7 natural, 11 painted/material) | 1 | done; awaiting owner art review | FinishCatalogTest |
| Armchair, Apothecary Counter, Small Wall Shelf, Table Lamp, Small Rug in every finish (90 blocks) | 1 | done; awaiting owner art review | DecorContentGameTests, GeneratedResourcesTest |
| Facing, wall/surface/floor support, support-loss drops | 1 | done | DecorPlacementGameTests |
| Lamp on/off with light 14, saved in the block state | 1 | done; two-client sync is an owner check | DecorInteractionGameTests |
| Crafting of the dark oak base; stonecutter restyling 1:1 within a piece | 1 | done | DecorContentGameTests, DecorInteractionGameTests |
| Generated original textures (32 px), tinted baked models | 1 | done; owner art review pending | DecorModelsTest |
| Fourteen herbs (4 ages, crop or bush), seeds, fresh herbs, herb bed, starter recipes, grass foraging, habitat worldgen, reloadable conditions | 2 | done (0.1.0-dev.2); owner art review pending | botany GameTests, BotanyResourcesTest, BotanyModelsTest |
| `/herbalwitch showcase [section]` (operators): every registered block, labelled, on a flat plot | 2 | done | Showcase*GameTests, ShowcasePlanTest |
| Five stations (drying rack, mortar, tea kettle, hearth, infusion pot) with menus, in-slot reservations, heat, automation sides, no catch-up | 3 | done (0.1.0-dev.3); owner UI/art review pending | StationProcessingGameTests, ProcessBehaviourGameTests, StationResourcesTest |
| Bundles, powders, 7 teas, salve, oils, garden products, oil lantern, warmth and pest-repellent effects, resin | 3 | done (0.1.0-dev.3) | PreparationUseGameTests, PreparationBlockGameTests, PreparationResourcesTest |
| EMI and JEI categories; Farmer's Delight heat both ways; Thirst Was Taken hydration and purity | 3 | done; viewers are owner checks | CompatibilityGameTests (-PcompatProfile=all) |

## Planned

| Milestone | Scope |
|---|---|
| 2 | Herbs and garden: 14 named herbs (starter four first), seeds, crops/bushes, beds, foraging, biome placement |
| 3 | Everyday preparation: drying rack, mortar, kettle, hearth, infusion pot; bundles, powders, teas, salves, oils, fertiliser; first recipe-viewer categories |
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
