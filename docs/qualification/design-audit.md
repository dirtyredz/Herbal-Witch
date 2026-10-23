# Design audit (Milestone 12)

Source: `design.txt` at `C:/Users/dirty/projects/artifacts/evil-eye-pack/jobs/2026-10-21-herbal-witch/design.txt` (96 lines). Every promise is split into an independently checkable row, traced to code, generated resources, tests and docs. Evidence is test class (and method where known) names. Candidate: branch `m12-release-qualification`. Paths: `HW/` = `src/main/java/io/github/dirtyredz/herbalwitch/`; `R/` = `src/generated/resources/data/herbalwitch/recipes/`; `L` = `src/generated/resources/assets/herbalwitch/lang/en_us.json`; `DEC` = `docs/DECISIONS.md`; `CAT` = `docs/catalogue/`; `FEAT` = `docs/FEATURES.md`; `COMPAT` = `docs/COMPATIBILITY.md`.

## Status legend
- implemented: code and resources exist and an automated test or qualification run covers it.
- implemented-unverified: code exists; no automated test found for the specific promise.
- approved-replacement: a DECISIONS entry or documented owner ruling replaces the promise (cited).
- interpretation (owner to confirm): implemented under a reading of the design that the owner should confirm.
- deferred: documented as not shipped (cited); the owner confirmation noted per row.
- owner check: cannot run headless; a release-readiness item, not a missing feature.

## S0. Vision (line 4)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-0-1 | 4 | All systems ship together in one release | implemented (M12 qualification) | DEC "2026-10-20 Integrated decor, staged development, one complete release" | M12 qualification (this audit) |

## S1. Initiation and energy (line 6)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-1-1 | 6 | Everyone starts non-witch | implemented | `HW/magic/witch/status/` | WitchStateTest, WitchCodecTest |
| D-1-2 | 6 | Witches initiate players at an altar | implemented | `HW/magic/ritual/action/`, `magic/witch/initiation/InitiationConsent` | AltarActionGameTests, InitiationConsentTest |
| D-1-3 | 6 | Initiation needs consent | implemented | `InitiationConsent` (invite, expiry 600) | InitiationConsentTest, AltarSessionTest |
| D-1-4 | 6 | Small offering | implemented | fresh sage + lavender + amethyst shard, 50 energy | AltarActionGameTests |
| D-1-5 | 6 | New witch receives a grimoire | implemented | `magic/ritual/action/ConversionCommit` | AltarActionGameTests, WitchLifecycleGameTests |
| D-1-6 | 6 | Witch-energy bar | implemented | `HW/client/magic/witch/hud/` | EnergyHudLayoutTest |
| D-1-7 | 6 | Configurable founding NPC witch | approved-replacement | survival-player founder (`magic/witch/initiation/founding/`, `foundingEnabled`); no NPC | DEC "2026-10-22 0.5.0: A survival founder and no NPC" |
| D-1-8 | 6 | Admin designation | implemented | `/herbalwitch witch designate` | InitiationConsentTest |
| D-1-9 | 6 | First witch in new world / single player | implemented | `FoundingService`, `FoundingSavedData` | FoundingStateTest, WitchLifecycleGameTests |
| D-1-10 | 6 | Non-witches grow herbs | implemented | no witch check in planting/harvest | HerbPlantingGameTests, HerbHarvestGameTests |
| D-1-11 | 6 | Non-witches buy products | implemented | shop buying open to any non-banned player | ShopPermissionGameTests, ShopPurchaseGameTests |
| D-1-12 | 6 | Non-witches use most products | implemented | everyday products and stations are non-witch | PreparationUseGameTests, StationProcessingGameTests |
| D-1-13 | 6 | Non-witches cannot do magical brewing | implemented | `apothecary/stations/process/authorization/`, recipe `requirements.witch` | MagicalProcessGameTests, ProcessRequirementsTest |
| D-1-14 | 6 | Non-witches cannot perform rituals | implemented | altar actions require witch | AltarActionGameTests |
| D-1-15 | 6 | Energy powers rituals/spells | implemented | `WitchStatusService.spend` | WitchSpendGameTests, WitchSpendTest |
| D-1-16 | 6 | Energy powers advanced brews | implemented | `requirements.energy` in R/processing/** | MagicalProcessGameTests |
| D-1-17 | 6 | Energy powers wards | implemented | `HW/magic/ward/` (20) | WardSpendGameTests |
| D-1-18 | 6 | Energy powers familiar powers | implemented | `companions/familiars/ability/` | FamiliarAbilityGameTests |
| D-1-19 | 6 | Energy powers utility wands | implemented | `HW/magic/wand/` | WandUtilityGameTests |
| D-1-20 | 6 | Energy optionally powers broom flight | implemented | `broom.energyPerSecond` (default 0) | BroomFlightCostTest, BroomFlightGameTests |
| D-1-21 | 6 | Recovery: rest | implemented | `magic/witch/energy/EnergyRecovery` | EnergyRecoveryTest, EnergyRecoveryGameTests |
| D-1-22 | 6 | Recovery: teas | implemented | energy tea +25, restful infusion +30 | PreparationUseGameTests, SleepRecoveryTest |
| D-1-23 | 6 | Recovery: altar meditation | implemented | `magic/ritual/action/MeditationAction` | AltarActionGameTests |
| D-1-24 | 6 | Recovery: nature bonus | implemented | tag `witch/energy/nature` +10% | EnergyRecoveryTest |
| D-1-25 | 6 | Recovery: moon bonus | implemented | full moon +15%, cap 25% | EnergyRecoveryTest |
| D-1-26 | 6 | When empty, magic pauses | implemented | spends refuse; zero never harms | EnergyServiceTest, WitchSpendTest |
| D-1-27 | 6 | Capacity grows through practice | implemented | 100 to 200 over 10 thresholds | PracticeProgressTest |

## S2. Ingredients and gardening (line 8)
Herb rows: ids `botany/<group>/<herb>/{plant,seeds,fresh}`, drying `R/processing/drying/**`, grinding `R/processing/grinding/herbs/**`, `HW/apothecary/botany/catalog/HerbCatalog`. Evidence for all: HerbCatalogTest, BotanyResourcesTest, HerbGrowthGameTests.
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-2-1 | 8 | Lavender | implemented | HerbCatalog.LAVENDER (temperate, bush) | herb tests |
| D-2-2 | 8 | Sage | implemented | SAGE (temperate, bush) | herb tests |
| D-2-3 | 8 | Rosemary | implemented | ROSEMARY (dry, bush) | herb tests |
| D-2-4 | 8 | Thyme | implemented | THYME (dry, bush) | herb tests |
| D-2-5 | 8 | Mint | implemented | MINT (wet, bush) | herb tests |
| D-2-6 | 8 | Chamomile | implemented | CHAMOMILE (temperate, crop) | herb tests |
| D-2-7 | 8 | Nettle | implemented | NETTLE (wet, crop) | herb tests |
| D-2-8 | 8 | Yarrow | implemented | YARROW (temperate, crop) | herb tests |
| D-2-9 | 8 | Valerian | implemented | VALERIAN (shade, crop) | herb tests |
| D-2-10 | 8 | Mugwort | implemented | MUGWORT (shade, crop) | herb tests |
| D-2-11 | 8 | Belladonna | implemented | BELLADONNA (woodland, bush) | herb tests |
| D-2-12 | 8 | Nightshade | implemented | NIGHTSHADE (woodland, bush) | herb tests |
| D-2-13 | 8 | Wolfsbane | implemented | WOLFSBANE (woodland, crop) | herb tests |
| D-2-14 | 8 | Mandrake | implemented | MANDRAKE (woodland, crop) | herb tests |
| D-2-15 | 8 | Mushrooms | implemented | vanilla mushrooms in `R/processing/grinding/mushroom_dust.json` | StationResourcesTest |
| D-2-16 | 8 | Moss | implemented | `moss_block` in grinding/fertilizer, infusion/soil_tonic | StationResourcesTest, PreparationUseGameTests |
| D-2-17 | 8 | Roots | implemented | `hanging_roots` (soil_tonic); carrot/beetroot (root bait) | StationResourcesTest, AnimalBaitGameTests |
| D-2-18 | 8 | Resin | implemented | `preparations/ingredients/resin`, spruce-leaf loot modifier, lantern_oil | PreparationResourcesTest |
| D-2-19 | 8 | Flowers | implemented | dandelion (flower_dust), starter-seed recipes | PreparationUseGameTests, HerbAcquisitionGameTests |
| D-2-20 | 8 | Minerals | implemented | quartz, amethyst, sand, iron nugget, copper | AdvancedStationGameTests, StationResourcesTest |
| D-2-21 | 8 | Select animal ingredients | implemented | bone, spider eye, fermented spider eye, ink sac, honey, string, slime (vanilla drops) | StationProcessingGameTests, AdvancedStationGameTests |
| D-2-22 | 8 | Biome influences rare plants | implemented | `HerbHabitat` biome tags | HerbWorldgenGameTests, HerbConditionTest |
| D-2-23 | 8 | Weather influences | implemented | `Preference.RAIN` | HerbConditionTest |
| D-2-24 | 8 | Night influences | implemented | `Preference.NIGHT` | HerbConditionTest |
| D-2-25 | 8 | Moon phase influences | implemented | `Preference.FULL_MOON` only | HerbConditionTest |
| D-2-26 | 8 | Seeds | implemented | 14 seeds, 4 starter recipes R/botany/*/seeds.json | HerbPlantingGameTests, HerbAcquisitionGameTests |
| D-2-27 | 8 | Growing beds | implemented | `botany/garden/herb_bed` | GardenBedGameTests, GardenBedConnectionGameTests |
| D-2-28 | 8 | Harvestable bushes | implemented | `Form.BUSH` (7 herbs) | HerbHarvestGameTests |
| D-2-29 | 8 | Decorative herb gardens | implemented | joined beds, hung bundles, `decor/fixtures/planter_box` | GardenBedConnectionGameTests, BlockOcclusionGameTests |
| D-2-30 | 8 | Property: restorative | implemented | `HerbTrait.RESTORATIVE` | HerbCatalogTest |
| D-2-31 | 8 | Property: calming | implemented | CALMING | HerbCatalogTest |
| D-2-32 | 8 | Property: toxic | implemented | TOXIC | HerbCatalogTest |
| D-2-33 | 8 | Property: protective | implemented | PROTECTIVE | HerbCatalogTest |
| D-2-34 | 8 | Property: growth | implemented | GROWTH | HerbCatalogTest |
| D-2-35 | 8 | Property: attraction | implemented | ATTRACTION | HerbCatalogTest |
| D-2-36 | 8 | Property: binding | implemented | BINDING | HerbCatalogTest |
| D-2-37 | 8 | Property: sight | implemented | SIGHT | HerbCatalogTest |
| D-2-38 | 8 | Property: speed | implemented | SPEED | HerbCatalogTest |
| D-2-39 | 8 | Properties legible | implemented | grimoire herb pages (`HerbContentFacts`) | GrimoireContentGameTests |

## S3. Stations (lines 9-26)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-3-1 | 10 | Small interface, limited slots, clear output | implemented | `HW/apothecary/stations/**` | StationResourcesTest, ProcessingCategoriesTest |
| D-3-2 | 10 | Grimoire recipe lookup | implemented | `StationRecipeEntries` | GrimoireContentGameTests, GrimoireAccessGameTests |
| D-3-3 | 11 | Rack dries/preserves into bundles | implemented | `stations/drying_rack/station`, 14 drying recipes | StationProcessingGameTests |
| D-3-4 | 12 | Mortar grinds herbs | implemented | `stations/mortar/station` | StationProcessingGameTests |
| D-3-5 | 12 | Mortar grinds minerals | implemented | sand (blinding dust), bone to bone meal; no mineral-only powder | MagicalProcessGameTests |
| D-3-6 | 12 | Mortar makes pigments | implemented | 4 dye recipes | StationProcessingGameTests |
| D-3-7 | 12 | Mortar makes blinding dust | implemented | `grinding/blinding_dust.json` | MagicalProcessGameTests |
| D-3-8 | 13 | Kettle: teas | implemented | R/processing/steeping (7) | StationProcessingGameTests |
| D-3-9 | 13 | Kettle: tonics | implemented | mining_tonic, travel_tonic | StationProcessingGameTests |
| D-3-10 | 13 | Kettle: simple infusions | implemented | restful_infusion, remedies | StationProcessingGameTests |
| D-3-11 | 13 | Kettle requires heat | implemented | `apothecary/stations/heat/` tags | StationProcessingGameTests, CompatibilityGameTests |
| D-3-12 | 14 | Hearth heats kettle | implemented | `stations/hearth`, `stove` | StationMultiblockGameTests |
| D-3-13 | 14 | Hearth heats cauldron | implemented | heat source below | AdvancedStationGameTests |
| D-3-14 | 14 | Hearth heats infusion pot | implemented | heat source below | StationProcessingGameTests |
| D-3-15 | 14 | Hearth is a home anchor | interpretation (owner to confirm) | the hearth is the ward focus that anchors the guarded home (ward radius 12 near a hearth or stove); no separate home-anchor mechanic | WardGameTests, WandIntegrationGameTests; `magic/ward/service/WardFocus` |
| D-3-16 | 14 | Hearth is a ward focus | implemented | `WardFocus` | WardGameTests |
| D-3-17 | 15 | Cauldron: liquid potions | implemented | `stations/cauldron/station` | MagicalProcessGameTests |
| D-3-18 | 15 | Cauldron: throwables | implemented | R/processing/cauldron/combat/* | AdvancedPreparationGameTests |
| D-3-19 | 15 | Cauldron: animal brews | implemented | read as familiar tonics + sleep mixture | AdvancedPreparationGameTests, AnimalCaptureGameTests |
| D-3-20 | 15 | Cauldron: familiar tonics | implemented | R/processing/cauldron/familiar/* | FamiliarPreparationGameTests |
| D-3-21 | 16 | Infusion pot: salves | implemented | `infusion/healing_salve` | PreparationUseGameTests |
| D-3-22 | 16 | Infusion pot: oils | implemented | infusion/oils/* | PreparationContractGameTests |
| D-3-23 | 16 | Infusion pot: repellents | implemented | pest/undead/arthropod | AdvancedPreparationGameTests |
| D-3-24 | 16 | Infusion pot: lantern oils | implemented | `infusion/lantern_oil` | PreparationBlockGameTests |
| D-3-25 | 17 | Still: rare essences | implemented | `distillation/moon` | AdvancedStationGameTests |
| D-3-26 | 17 | Still: advanced extracts | implemented | venomleaf, warding | MagicalProcessGameTests |
| D-3-27 | 18 | Bottling: bottle/portion | implemented | R/processing/bottling/** | AdvancedStationGameTests |
| D-3-28 | 18 | Bottling: label | implemented | labelled variants (NBT flag) | AdvancedStationGameTests, AdvancedResourcesTest |
| D-3-29 | 18 | Bottling: package | implemented | gunpowder + paper thrown variant | AdvancedStationGameTests |
| D-3-30 | 19 | Altar: initiation | implemented | `magic/ritual/altar` | AltarActionGameTests |
| D-3-31 | 19 | Altar: familiar binding | implemented | `ritual/familiar` actions | FamiliarBindingGameTests |
| D-3-32 | 19 | Altar: home blessings | implemented | Garden Blessing, Home Protection | CircleGameTests, GardenBlessingPlanTest |
| D-3-33 | 19 | Altar: revival | implemented | `ritual/familiar/revival` | FamiliarRecoveryGameTests |
| D-3-34 | 19 | Altar: wards | implemented | Home Protection charges ward stone | WardGameTests |
| D-3-35 | 19 | Ritual circle | implemented | `magic/ritual/circle` | CircleGameTests, CircleLayoutTest |
| D-3-36 | 20 | Charm bench: sachets | implemented | `charm_work/protective_sachet` | CharmGameTests |
| D-3-37 | 20 | Charm bench: amulets | implemented | undead/arthropod amulets | CharmGameTests |
| D-3-38 | 20 | Charm bench: capture charms | implemented | `animals/charm_work/capture_charm` | AnimalCaptureGameTests |
| D-3-39 | 20 | Charm bench: protection tokens | implemented | read as sachet + amulets | CharmGameTests |
| D-3-40 | 21 | Grimoire stand: research herbs | implemented | `grimoire/stand`, `bookshelf` | GrimoireAccessGameTests, ResearchEligibilityTest |
| D-3-41 | 21 | Grimoire stand: recipes | implemented | recipe-backed pages | GrimoireContentGameTests |
| D-3-42 | 21 | Grimoire stand: rituals | implemented | `AltarEntries`, `WitchEntries` | ContentContractGameTests |
| D-3-43 | 22 | Beds/nests/perches assign rest | implemented | `familiars/home/*` | FamiliarHomeGameTests, FamiliarHomeRulesTest |
| D-3-44 | 22 | Homes give recovery | implemented | safe rest, also offline | FamiliarHomeGameTests |
| D-3-45 | 23 | Ward stone defines guarded area | implemented | radius 1-8 (12 focus) | WardGameTests, WardCodecTest, WardIndexTest |
| D-3-46 | 23 | Ward stone targeting settings | implemented | profile undead/arthropod/both | WardGameTests |
| D-3-47 | 24 | Counter/register/shelves/jars stock | implemented | `shop/register`, `stock_shelf`, `stock_jar` | ShopStockGameTests |
| D-3-48 | 24 | ... sell | implemented | `ShopTrades` | ShopPurchaseGameTests |
| D-3-49 | 24 | ... display | implemented | linked fixture projections | ShopLifecycleGameTests |
| D-3-50 | 24 | ... track goods | implemented | stock, offers, ledger | ShopCodecTest, ShopPersistenceGameTests |
| D-3-51 | 25 | Broom stand crafts | implemented | `stations/tools/broom_stand`, `broom_work/craft` | ToolStationGameTests |
| D-3-52 | 25 | Broom stand repairs | implemented | `broom_work/repair` | ToolStationGameTests |
| D-3-53 | 26 | Wand bench creates wands | implemented | `stations/tools/wand_bench`, `wand_carving/craft` | ToolStationGameTests |

## S4. Everyday products (lines 28-35)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-4-1 | 28 | Healing salves | implemented | `preparations/salves/healing_salve` | PreparationUseGameTests |
| D-4-2 | 28 | Recovery teas | implemented | `drinks/recovery_tea` | PreparationContractGameTests |
| D-4-3 | 29 | Energy teas | implemented | `drinks/energy_tea` | PreparationUseGameTests, EnergyRecoveryGameTests |
| D-4-4 | 29 | Restful sleep infusions | implemented | `drinks/restful_infusion` | SleepRecoveryTest |
| D-4-5 | 30 | Warmth remedy | implemented | `drinks/warmth_remedy` | PreparationUseGameTests |
| D-4-6 | 30 | Cooling remedy | implemented | `drinks/cooling_remedy` (Fire Resistance) | PreparationContractGameTests |
| D-4-7 | 30 | Night vision | implemented | `advanced/drinks/night_vision` | AdvancedPreparationGameTests |
| D-4-8 | 30 | Underwater breathing | implemented | `advanced/drinks/water_breathing` | PreparationContractGameTests |
| D-4-9 | 31 | Mining tonic | implemented | `drinks/mining_tonic` | PreparationContractGameTests |
| D-4-10 | 31 | Travel tonic | implemented | `drinks/travel_tonic` | PreparationContractGameTests |
| D-4-11 | 32 | Lantern oil | implemented | `oils/lantern_oil`, oil lantern | PreparationBlockGameTests |
| D-4-12 | 32 | Fishing blends | implemented | `oils/fishing_blend` | PreparationContractGameTests |
| D-4-13 | 32 | Protective sachets | implemented | `magic/charms/protective_sachet` | CharmGameTests |
| D-4-14 | 33 | Garden fertilizers | implemented | `garden/fertilizer` | PreparationUseGameTests |
| D-4-15 | 33 | Growth infusions | implemented | `garden/growth_infusion` | PreparationUseGameTests |
| D-4-16 | 33 | Crop-yield blessings | implemented | Garden Blessing (herb beds only) | CircleGameTests, GardenBlessingPlanTest |
| D-4-17 | 34 | Pest repellents | implemented | `garden/pest_repellent` | PreparationUseGameTests |
| D-4-18 | 34 | Soil tonics | implemented | `garden/soil_tonic` | PreparationUseGameTests |
| D-4-19 | 34 | Flower growth dust | implemented | `garden/flower_dust` | PreparationUseGameTests |
| D-4-20 | 34 | Mushroom growth dust | implemented | `garden/mushroom_dust` | PreparationUseGameTests.mushroomDustGrowsAMushroomWhereOneCanLive |
| D-4-21 | 35 | Home protection wards | implemented | ward stone + Home Protection | WardGameTests |
| D-4-22 | 35 | Dyes | implemented | 4 grinding recipes | StationProcessingGameTests |
| D-4-23 | 35 | Decorative herbal bundles | implemented | 14 hanging bundle blocks | PreparationBlockGameTests, BlockOcclusionGameTests |

## S5. Combat and enemy mixtures (lines 36-42)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-5-1 | 37 | Poison vials | implemented | `advanced/throwables/poison` | AdvancedPreparationGameTests |
| D-5-2 | 37 | Venom oils | implemented | `advanced/oils/venom` | AdvancedPreparationGameTests |
| D-5-3 | 38 | Blinding powders | implemented | `advanced/dusts/blinding` | AdvancedPreparationGameTests.weakeningBlindingAndThornsDoWhatTheyPromise |
| D-5-4 | 38 | Concealment smoke | implemented | `throwables/concealment` | AdvancedPreparationGameTests |
| D-5-5 | 39 | Slowing mixture | implemented | `throwables/slowing` | AdvancedPreparationGameTests |
| D-5-6 | 39 | Binding mixture | implemented | `throwables/binding` | AdvancedPreparationGameTests |
| D-5-7 | 39 | Weakening mixture | implemented | `throwables/weakening` | AdvancedPreparationGameTests.weakeningBlindingAndThornsDoWhatTheyPromise |
| D-5-8 | 39 | Temporary sleep mixture | implemented | `throwables/sleep` | AdvancedPreparationGameTests |
| D-5-9 | 40 | Thorn retaliation oils | implemented | `advanced/oils/thorn` | AdvancedPreparationGameTests.weakeningBlindingAndThornsDoWhatTheyPromise |
| D-5-10 | 41 | Resistance draughts | implemented | `advanced/drinks/resistance` | PreparationContractGameTests |
| D-5-11 | 41 | Healing mixtures | implemented | `advanced/drinks/healing` | AdvancedPreparationGameTests |
| D-5-12 | 42 | Mob-specific repellent mixtures | implemented | undead/arthropod repellent oils | AdvancedPreparationGameTests |
| D-5-13 | 42 | Mob-specific protective charms | implemented | undead/arthropod amulets | CharmGameTests |

## S6. Balance and config (line 43)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-6-1 | 43 | Duration | implemented | CAT/products.md durations | PreparationCatalogTest |
| D-6-2 | 43 | Recipe costs | implemented | `requirements.energy` | ProcessRequirementsTest |
| D-6-3 | 43 | Cooldowns | implemented | 20-tick throw cooldown | AdvancedPreparationGameTests |
| D-6-4 | 43 | Mob resistance | implemented | tags `hard_control_immune`, `hard_control_allowed`, Steadfast | AdvancedPreparationGameTests |
| D-6-5 | 43 | PvP targeting configurable | implemented | `workings.preparationPvp` | AdvancedPreparationGameTests |
| D-6-6 | 43 | Boss immunities configurable | implemented | datapack tag `hard_control_immune` | AdvancedPreparationGameTests |

## S7. Regular animals (line 45)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-7-1 | 45 | Herbal bait | implemented | 6 baits | AnimalBaitGameTests, AnimalAiGameTests |
| D-7-2 | 45 | Calming powder | implemented | `animals/care/calming_powder` | AnimalAiGameTests |
| D-7-3 | 45 | Sleep immobilizes eligible mobs | implemented | M7 sleep reused | AnimalCaptureGameTests |
| D-7-4 | 45 | Capture charms transport | implemented | `data/herbalwitch_captures.dat` | AnimalCaptureGameTests, AnimalRecoveryGameTests, CaptureStateTest, CaptureCheckpointTest, CaptureRestartGameTests |
| D-7-5 | 45 | Species-specific trust | implemented | `companions/animals/trust` | TrustRulesTest |
| D-7-6 | 45 | Species-specific feeding | implemented | bait groups per species | AnimalCareGameTests |
| D-7-7 | 45 | Taming earned | implemented | 80/100 trust + 4/5 feeds | AnimalTamingGameTests |
| D-7-8 | 45 | Animal salves heal | implemented | targeted healing salve | AnimalCareGameTests |
| D-7-9 | 45 | Vanilla first, allowlist | implemented | tags `animals/*` | AnimalEligibilityTest, AnimalResourcesTest |
| D-7-10 | 45 | Optional modded-mob adapters | deferred (optional by design) | design line 45 calls them optional, line 83 forbids untested universal promises; `moddedParticipation` off by default; no adapter ships; DEC 2026-10-22 M8, COMPAT "Ordinary animals". Owner to confirm. | FamiliarDoggyExclusionGameTests (exclusion) |
| D-7-11 | 45 | No promise for bosses/modded | implemented | closed vanilla adapters | AnimalEligibilityTest |
| D-7-12 | 45 | Preserve mob state | implemented | `capture/CapturePayload` | CapturePayloadTest, AnimalPayloadTest |
| D-7-13 | 45 | Preserve ownership | implemented | owner/keeper precedence | AnimalStateGameTests, AnimalTamingGameTests |

## S8. Multiple familiars and home protection (line 47)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-8-1 | 47 | Bind multiple animals | implemented | `companions/familiars/bond/` | FamiliarBindingGameTests, FamiliarSpeciesBindingGameTests |
| D-8-2 | 47 | Binding at the altar | implemented | altar + circle | FamiliarBindingGameTests |
| D-8-3 | 47 | Own name | implemented-unverified | `FamiliarBindingService` stores the mob name (vanilla name tag); no rename UI | none |
| D-8-4 | 47 | Bond level | implemented | `FamiliarBondProgress` | FamiliarBondProgressTest |
| D-8-5 | 47 | Equipment | implemented | `familiars/equipment/*` | FamiliarEquipmentGameTests |
| D-8-6 | 47 | Home location | implemented | `familiars/home` | FamiliarHomeGameTests |
| D-8-7 | 47 | Command: Follow | implemented | `FamiliarCommand.FOLLOW` | FamiliarCommandGameTests, FamiliarFlightGameTests |
| D-8-8 | 47 | Command: Stay | implemented | STAY | FamiliarCommandGameTests |
| D-8-9 | 47 | Command: Guard Witch | implemented | GUARD_WITCH | FamiliarGuardSecurityGameTests |
| D-8-10 | 47 | Command: Protect Home | implemented | PROTECT_HOME | FamiliarLoadGameTests |
| D-8-11 | 47 | Command: Scout | implemented | `command/ai/FamiliarScout`; shared SCOUT command | FamiliarPowerCoverageGameTests.aScoutWarnsOnlyItsWitch |
| D-8-12 | 47 | Command: Passive | implemented | PASSIVE | FamiliarCommandGameTests |
| D-8-13 | 47 | Command: Return Home | implemented | RETURN_HOME | FamiliarHomeGameTests |
| D-8-14 | 47 | Configurable follow cap | implemented | `mobilePerOwner` 1-8 | FamiliarConfigGameTests |
| D-8-15 | 47 | No cap on total bonds | implemented | per-bond shards | FamiliarLoadGameTests, FamiliarRosterGameTests |
| D-8-16 | 47 | Different homes/tasks | implemented | per-bond home + command | FamiliarHomeGameTests |
| D-8-17 | 47 | Guards detect and repel hostiles | implemented | `FamiliarGuard`, REPEL | FamiliarGuardSecurityGameTests |
| D-8-18 | 47 | Guards alert their witch | implemented | `observation/FamiliarAlerts` | FamiliarPowerCoverageGameTests.aScoutWarnsOnlyItsWitch (private, one per threat per 5 s) |
| D-8-19 | 47 | Protect a designated radius | implemented | `guardRadius` 1-24 | FamiliarLoadGameTests |
| D-8-20 | 47 | Never attack friendly visitors/players | implemented | target filters + permission rechecks | FamiliarGuardSecurityGameTests |
| D-8-21 | 47 | Respects loaded chunks | implemented | no tickets | FamiliarLoadGameTests |
| D-8-22 | 47 | Respects protected claims | implemented | `PrincipalPermission`; unsupported claim mods suspend automation | FamiliarGuardSecurityGameTests |
| D-8-23 | 47 | Retreat | implemented | hazard retreat | FamiliarFlightGameTests |
| D-8-24 | 47 | Incapacitated, not dead | implemented | `familiars/recovery` | FamiliarRecoveryGameTests |
| D-8-25 | 47 | Altar revival | implemented | `ritual/familiar/revival` | FamiliarRecoveryGameTests |

## S9. Species roles and bond growth (lines 48-57)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-9-1 | 48 | Eight species bindable | implemented | `familiars/species` | FamiliarSpeciesBindingGameTests, FamiliarAcquisitionGameTests |
| D-9-2 | 49 | Cat: sense threats | implemented | `FamiliarPower.THREATS` | FamiliarPowerCoverageGameTests.catSensesHostile |
| D-9-3 | 49 | Cat: sense harmful magic | implemented | `FamiliarSenseTask` marks harmful `PreparationProjectile` | FamiliarPowerCoverageGameTests.catSensesAHarmfulPreparationInFlight |
| D-9-4 | 50 | Raven: scout | implemented | shared SCOUT command (no raven-specific power) | FamiliarPowerCoverageGameTests.aScoutWarnsOnlyItsWitch |
| D-9-5 | 50 | Raven: warn | implemented | `FamiliarAlerts.warn` | FamiliarPowerCoverageGameTests.aScoutWarnsOnlyItsWitch |
| D-9-6 | 50 | Raven: retrieve small objects | implemented | RETRIEVE | FamiliarRetrievalGameTests |
| D-9-7 | 51 | Owl: nighttime scouting | implemented | THREATS at night | FamiliarSenseTest |
| D-9-8 | 51 | Owl: herb finding | implemented | HERBS | FamiliarPowerCoverageGameTests.owlFindsMatureHerb |
| D-9-9 | 52 | Fox: item retrieval | implemented | RETRIEVE | FamiliarRetrievalGameTests |
| D-9-10 | 52 | Fox: stealth | implemented | `FamiliarSpeciesSpec.stealthy` (fox); `FamiliarBuffEffects` projects shared concealment; dropped while distracting (`FamiliarCombatPowers.distracting`) | FamiliarPowerCoverageGameTests.aFoxIsStealthyUntilItDistracts |
| D-9-11 | 52 | Fox: distractions | implemented | DISTRACT | FamiliarAbilityGameTests |
| D-9-12 | 53 | Wolf: defense | implemented | INTERCEPT | FamiliarAbilityGameTests |
| D-9-13 | 53 | Wolf: tracking | implemented | TRACK | FamiliarPowerCoverageGameTests.wolfTracksHostile |
| D-9-14 | 53 | Wolf: home guarding | implemented | PROTECT_HOME | FamiliarLoadGameTests |
| D-9-15 | 54 | Snake: venom | implemented | VENOM | FamiliarAbilityGameTests |
| D-9-16 | 54 | Snake: creature detection | implemented | CREATURES | FamiliarPowerCoverageGameTests.snakeSensesCreature |
| D-9-17 | 55 | Rabbit: plant finding | implemented | HERBS | FamiliarSenseTest |
| D-9-18 | 55 | Rabbit: garden aid | implemented | GARDEN | FamiliarAbilityGameTests |
| D-9-19 | 56 | Bat: cave scouting | implemented | shared SCOUT command | FamiliarPowerCoverageGameTests.aScoutWarnsOnlyItsWitch |
| D-9-20 | 56 | Bat: detect creatures in darkness | implemented | CREATURES, light <= 7 | FamiliarPowerCoverageGameTests.batSensesDarkCreature |
| D-9-21 | 57 | Bond grows: care | implemented | `FamiliarCareContribution` | FamiliarProgressGameTests |
| D-9-22 | 57 | Bond grows: feeding | implemented | max-trust feeds for owned familiars | FamiliarProgressGameTests |
| D-9-23 | 57 | Bond grows: travel | implemented | `FamiliarBondProgress.tick` | FamiliarBondProgressTest |
| D-9-24 | 57 | Bond grows: healing | implemented | salve path | FamiliarProgressGameTests |
| D-9-25 | 57 | Bond grows: rituals | implemented | Harmony, Heartwood, Spirit Well | FamiliarProgressGameTests |
| D-9-26 | 57 | Useful without combat | implemented | sensing, retrieval, garden aid | FamiliarPowerCoverageGameTests |

## S10. Familiar preparations (lines 58-70)
Evidence for all rows: FamiliarPreparationGameTests, FamiliarBuffRulesTest.
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-10-1 | 59 | Guardian's Brew | implemented | `familiar/tonics/guardian` (+4 health, +2 armor) | as above |
| D-10-2 | 60 | Swiftpaw Tonic | implemented | `tonics/swiftpaw` | as above |
| D-10-3 | 61 | Nightwatch Infusion | implemented | `tonics/nightwatch` | as above |
| D-10-4 | 62 | Moonveil Elixir | implemented | `tonics/moonveil` | as above |
| D-10-5 | 63 | Warding Essence | implemented | `essences/warding` | as above |
| D-10-6 | 64 | Emberguard Oil | implemented | `oils/emberguard` | as above |
| D-10-7 | 65 | Venomleaf Extract | implemented | `essences/venomleaf` | as above |
| D-10-8 | 66 | Spiritbond: stronger bond | implemented | `tonics/spiritbond` (+1 care XP) | as above |
| D-10-9 | 66 | Spiritbond: energy synergy | implemented | -20% power cost | FamiliarBuffRulesTest, FamiliarAbilityGameTests |
| D-10-10 | 67 | Hearthkeeper's Brew | implemented | `tonics/hearthkeeper` | as above |
| D-10-11 | 68 | Healing Salve | implemented | existing salve via animal-healing path | AnimalCareGameTests |
| D-10-12 | 69 | Shadowstep Draught | implemented | `tonics/shadowstep` | as above |
| D-10-13 | 70 | Temporary buffs from potions/oils/salves | implemented | 3,600 loaded ticks | as above |
| D-10-14 | 70 | Permanent upgrades via altar ritual | implemented | `ritual/familiar/heartwood`, `spirit_well` | FamiliarProgressGameTests |
| D-10-15 | 70 | Powers use energy and/or cooldowns | implemented | `FamiliarPower` | FamiliarAbilityGameTests |

## S11. Brooms, wands, rituals (line 72)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-11-1 | 72 | Broom crafted at stand | implemented | `magic/broom/broom`, `broom_work/craft` | ToolStationGameTests |
| D-11-2 | 72 | Broom repair | implemented | `broom_work/repair` | ToolStationGameTests |
| D-11-3 | 72 | Ridden as transportation | implemented | `HW/magic/broom/` | BroomFlightGameTests, BroomDeploymentGameTests |
| D-11-4 | 72 | Not a weapon | implemented-unverified | no attack/damage code in `magic/broom` | none |
| D-11-5 | 72 | Configurable speed | implemented | `broom.speed` etc. | BroomRulesTest, BroomMotionTest |
| D-11-6 | 72 | Configurable energy cost | implemented | `broom.energyPerSecond` | BroomFlightCostTest |
| D-11-7 | 72 | Configurable fall protection | implemented | `broom.fallProtection` | BroomLifecycleGameTests |
| D-11-8 | 72 | Server permissions | implemented | `broom.enabled`, `dimensions`, owner binding | BroomRulesTest |
| D-11-9 | 72 | One wand, selectable noncombat modes | implemented | 7 modes | WandModeTest, WandNetworkGameTests |
| D-11-10 | 72 | Wand: harvest | implemented | Harvest | WandGardenGameTests |
| D-11-11 | 72 | Wand: plant | implemented | Plant | WandGardenGameTests |
| D-11-12 | 72 | Wand: water | implemented | Water | WandGardenGameTests |
| D-11-13 | 72 | Wand: collect drops | implemented | Collect | WandUtilityGameTests |
| D-11-14 | 72 | Wand: light hearths | implemented | Light | WandUtilityGameTests |
| D-11-15 | 72 | Wand: light candles | implemented | Light | WandUtilityGameTests |
| D-11-16 | 72 | Wand: check wards | implemented | "Ritual/Ward Focus" opens ward owner menu | WandIntegrationGameTests.wardFocusKeepsExistingActivationPriceAndOwnerControls |
| D-11-17 | 72 | Wand: recall familiars | implemented | "Familiar Commands" mode, paid recall | WandIntegrationGameTests, FamiliarCommandGameTests |
| D-11-18 | 72 | Ritual: initiation | implemented | see D-1-2 | AltarActionGameTests |
| D-11-19 | 72 | Ritual: familiar binding | implemented | see D-8-2 | FamiliarBindingGameTests |
| D-11-20 | 72 | Ritual: familiar recovery | implemented | revival | FamiliarRecoveryGameTests |
| D-11-21 | 72 | Ritual: garden blessings | implemented | Garden Blessing | CircleGameTests |
| D-11-22 | 72 | Ritual: energy restoration | implemented | meditation | AltarActionGameTests |
| D-11-23 | 72 | Ritual: home protection | implemented | Home Protection | CircleGameTests |
| D-11-24 | 72 | Moon-phase bonuses | implemented | energy +15%; full-moon herb conditions | EnergyRecoveryTest, HerbConditionTest |
| D-11-25 | 72 | No long waiting | implemented | rituals 100-400 ticks | AltarActionGameTests |

## S12. Apothecary (line 74)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-12-1 | 74 | Name shops | implemented | `ShopMetadataRules` | ShopLifecycleGameTests |
| D-12-2 | 74 | Display stock on shelves | implemented | `stock_shelf`, `stock_jar` | ShopLifecycleGameTests |
| D-12-3 | 74 | Set prices | implemented | 1-64 emeralds per lot | ShopStockGameTests |
| D-12-4 | 74 | Use registers | implemented | `shop/register` | ShopLifecycleGameTests |
| D-12-5 | 74 | Accept customer orders | implemented | order board requests (nonbinding; owner associates an offer) | ShopOrderGameTests, ShopOrderRulesTest |
| D-12-6 | 74 | Sell while offline | implemented | no owner-online check in `ShopTrades`; villager customers also need the register's chunk loaded (CAT/commerce.md) | SurvivalLoopGameTests.forageToShopToNonWitchUse (owner absent from server player list during sale) |
| D-12-7 | 74 | Non-witches inspect | implemented | public browse | ShopPermissionGameTests |
| D-12-8 | 74 | Non-witches buy | implemented | any non-banned player | ShopPurchaseGameTests |
| D-12-9 | 74 | Non-witches use goods | implemented | ordinary items | PreparationUseGameTests |
| D-12-10 | 74 | Configurable emerald pricing | implemented | plain emeralds, owner-set prices, `ShopServerConfig` | ShopPurchasePlanTest |
| D-12-11 | 74 | Adapter for existing economies | approved-replacement | design allowed emeralds OR adapter; DEC "2026-10-23 M11" (no external economy adapter), COMPAT "Commerce" | n/a |
| D-12-12 | 74 | Shop permissions | implemented | owner-only management, bans, public-buying toggle | ShopPermissionGameTests |
| D-12-13 | 74 | Stock tracking | implemented | 27 stock slots, revisions | ShopStockGameTests, ShopCodecTest |
| D-12-14 | 74 | Payment handling | implemented | prepare/apply/checkpoint/commit | ShopPurchaseGameTests, ShopRecoveryTest, ShopReplayGameTests |
| D-12-15 | 74 | Purchase history | implemented | 128-event owner-only ledger | ShopCodecTest, ShopPermissionGameTests |
| D-12-16 | 74 | Simple order board | implemented | `shop/order_board` | ShopOrderGameTests |
| D-12-17 | 74 | Witch-to-witch ingredient trading | implemented via shops | any player may buy seeds, herbs, bundles, powders from a witch's shop (`ShopProductPolicy`); no separate trade UI | ShopCatalogTest, ShopPurchaseGameTests |
| D-12-18 | 74 | Teaching recipes | implemented | `grimoire/knowledge/teaching` | GrimoireTeachingGameTests, TeachingServiceTest |

## S13. Grimoire (line 76)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-13-1 | 76 | Records herbs | implemented | discovery capability | DiscoveryGameTests, DiscoveryStateTest |
| D-13-2 | 76 | Records effects | implemented | product/herb pages | GrimoireContentGameTests |
| D-13-3 | 76 | Records recipes | implemented | recipe-backed pages | GrimoireContentGameTests |
| D-13-4 | 76 | Records stations | implemented | station pages | GrimoireContentGameTests |
| D-13-5 | 76 | Records animal trust requirements | implemented | animal card (`AnimalEntries`) | AnimalInspectionGameTests, ContentContractGameTests |
| D-13-6 | 76 | Records familiar powers | implemented | familiar guide | ContentContractGameTests, ContentOrderFamiliars |
| D-13-7 | 76 | Records rituals | implemented | ritual pages | ContentContractGameTests |
| D-13-8 | 76 | Recipes show exact station | implemented | `ProcessingCategories` | ProcessingCategoriesTest |
| D-13-9 | 76 | Missing ingredients shown | implemented | inventory allocation | MissingIngredientsTest, GrimoireIngredientGameTests |
| D-13-10 | 76 | Gameplay loop (forage/grow, prepare, brew, use/sell/give) | implemented | whole chain | SurvivalLoopGameTests (forage, dry, grind, steep, shop, non-witch purchase and use; witch-only cauldron brew charged once), StationProcessingGameTests, AnimalCareGameTests |
| D-13-11 | 76 | Gradual discovery | implemented | observation policy, shallow research | DiscoveryGameTests, ResearchEligibilityTest |
| D-13-12 | 76 | Optional experimentation | interpretation (owner to confirm) | no experimentation mechanic; DEC "2026-10-22 0.3.0" (discovery advisory, research shallow) | n/a |
| D-13-13 | 76 | Not an exhausting skill tree | implemented | discovery gates nothing | DiscoveryGameTests |

## S14. Compatibility and platform (lines 78-84)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-14-1 | 78 | Document target version | implemented | MC 1.20.1, Java 17 (DEC 2026-10-20, COMPAT, README) | n/a |
| D-14-2 | 78 | Document loader | implemented | Forge 47.4.10 | n/a |
| D-14-3 | 79 | Blocks | implemented | `*Blocks` registries | GeneratedResourcesTest |
| D-14-4 | 79 | Block entities | implemented | stations, shops, altar | StationProcessingGameTests |
| D-14-5 | 79 | Recipes | implemented | `processing/*` types | RecipeIngredientsTest |
| D-14-6 | 79 | Items | implemented | `*Items` | GeneratedResourcesTest |
| D-14-7 | 79 | Inventory screens | implemented | menus/screens | RecipeTextGameTests, GrimoirePanelHostTest |
| D-14-8 | 79 | Effects | implemented | vanilla MobEffects | AdvancedPreparationGameTests |
| D-14-9 | 79 | Mobs/AI | implemented | raven, owl, snake | FamiliarAcquisitionGameTests, AnimalAiGameTests |
| D-14-10 | 79 | HUD overlays | implemented | energy HUD | EnergyHudLayoutTest |
| D-14-11 | 79 | Loot tables | implemented | `loot_tables`, `loot_modifiers` | BotanyResourcesTest |
| D-14-12 | 79 | Biome tags | implemented | `tags/worldgen` | HerbWorldgenGameTests |
| D-14-13 | 79 | Persistent data | implemented | capabilities, SavedData | WitchCodecTest, FamiliarPersistenceTest, ShopCodecTest |
| D-14-14 | 80 | Energy server-authoritative | implemented | `WitchStatusService` | WitchPayloadTest, WitchSpendTest |
| D-14-15 | 80 | Witch status server-authoritative | implemented | same | WitchStateTest |
| D-14-16 | 80 | Shops server-authoritative | implemented | ShopSession nonce/sequence | ShopSessionTest |
| D-14-17 | 80 | Familiars server-authoritative | implemented | opaque target tokens | FamiliarWireTest |
| D-14-18 | 80 | Wards server-authoritative | implemented | `WardSavedData` | WardContractGameTests |
| D-14-19 | 80 | Initiation server-authoritative | implemented | `AltarSessionService` | AltarSessionTest |
| D-14-20 | 80 | Sync to clients | implemented | epoch/revision snapshots | WitchPayloadTest, PayloadBoundsTest |
| D-14-21 | 81 | Protected land | implemented | `ActionPermissionService`/`PrincipalPermission`; no claim adapters | WandPermissionGameTests, ShopPermissionGameTests |
| D-14-22 | 81 | PvP rules | implemented | `preparationPvp` + server PvP/teams | AdvancedPreparationGameTests |
| D-14-23 | 81 | Ownership | implemented | owner-bound broom, wards, bonds | BroomLifecycleGameTests, WardGameTests |
| D-14-24 | 81 | Chunk loading | implemented | no tickets | FamiliarLoadGameTests |
| D-14-25 | 81 | Death/restart persistence | implemented | restart harnesses: familiar, magic tools, commerce, capture (captureRestartTest: CaptureRestartGameTests, M8, eleven ledger boundaries plus a corrupt ledger); saveUpgradeTest (owner's real 0.2.0-dev.9 and 0.6.1 worlds opened by the candidate in three JVMs) | FamiliarRestartGameTests, MagicToolsRestartGameTests, ShopRestartGameTests, CaptureRestartGameTests |
| D-14-26 | 81 | Dimension changes | implemented | capability handle renewal (DEC 2026-10-23) | FamiliarCommandGameTests |
| D-14-27 | 82 | Config: balance | implemented | `WitchServerConfig`/`WorkingRules` | WitchResourcesTest |
| D-14-28 | 82 | Config: initiation | implemented | `initiation.*` | WitchLifecycleGameTests |
| D-14-29 | 82 | Config: broom flight | implemented | `BroomConfigValues` | BroomRulesTest |
| D-14-30 | 82 | Config: familiar caps | implemented | `FamiliarServerConfig` | FamiliarConfigGameTests |
| D-14-31 | 82 | Config: PvP effects | implemented | `preparationPvp` | AdvancedPreparationGameTests |
| D-14-32 | 82 | Config: shop economy | implemented | `ShopServerConfig` (caps, budgets); currency fixed to emeralds | NpcDemandTest, ShopDemandGameTests |
| D-14-33 | 82 | Config: modded-mob allowlists | implemented | `moddedParticipation` + `animals/*` tags | AnimalResourcesTest, AnimalEligibilityTest |
| D-14-34 | 83 | Vanilla mobs first | implemented | 16 vanilla profiles | AnimalEligibilityTest |
| D-14-35 | 83 | Third-party only via tested adapters | implemented | none shipped; Doggy Talents excluded | FamiliarDoggyExclusionGameTests |
| D-14-36 | 84 | Test in survival | implemented-unverified | GameTest players default to survival via PlayerFixtures; no survival-specific assertion | none specific |
| D-14-37 | 84 | Test in creative | implemented-unverified | creative paths asserted in shop and capture tests | ShopPermissionGameTests, AnimalCaptureGameTests |
| D-14-38 | 84 | Test in single player | owner check | the integrated host's level.dat player checkpoint (commerce) cannot run headless; release-readiness item, not a missing feature (FEAT) | none (headless) |
| D-14-39 | 84 | Test on dedicated server | implemented | qualifyDedicatedServer (installed Forge 47.4.10 server with reobfuscated release jar; alone, with optional mods, Doggy Talents, KubeJS) plus ANU `runGameTestServer` | whole GameTest suite |
| D-14-40 | 84 | Test reloads | implemented | rules/recipe reload | FamiliarConfigGameTests, ProcessBehaviourGameTests, ContentContractGameTests |
| D-14-41 | 84 | Test crashes | implemented (documented limits) | deterministic checkpoint replay across processes (familiar, magic tools, commerce, capture); no random power-loss claim | FamiliarRestartGameTests, MagicToolsRestartGameTests, ShopRestartGameTests, CaptureRestartGameTests |
| D-14-42 | 84 | Test simultaneous players | implemented-unverified | multi-owner load tests; two-client checks are owner checks (FEAT) | FamiliarLoadGameTests, ShopReplayGameTests |

## S15. Definition of a complete release (lines 86-95)
| ID | Line | Requirement | Status | Implementation | Evidence |
|---|---|---|---|---|---|
| D-15-1 | 86 | Stations craftable and working | implemented | R/stations/**, R/magic/**, R/shop/**, R/familiars/home/** | StationResourcesTest, AdvancedStationGameTests, ToolStationGameTests |
| D-15-2 | 87 | Every product category usable with effects | implemented | see S4/S5 | PreparationContractGameTests, AdvancedPreparationGameTests |
| D-15-3 | 88 | Non-witch can become witch | implemented | founding, initiation, designate | WitchLifecycleGameTests, AltarActionGameTests |
| D-15-4 | 89 | Energy functions | implemented | see S1 | EnergyRecoveryGameTests |
| D-15-5 | 89 | Rituals function | implemented | see S11 | AltarActionGameTests, CircleGameTests |
| D-15-6 | 89 | Broom functions | implemented | see S11 | BroomFlightGameTests |
| D-15-7 | 89 | Wand functions | implemented | see S11 | WandUtilityGameTests |
| D-15-8 | 90 | Animal taming/capture functions | implemented | see S7 | AnimalTamingGameTests, AnimalCaptureGameTests |
| D-15-9 | 90 | Familiar binding, commands, home guarding | implemented | see S8 | FamiliarBindingGameTests, FamiliarCommandGameTests, FamiliarLoadGameTests |
| D-15-10 | 91 | Familiar potions and protection | implemented | see S10 | FamiliarPreparationGameTests |
| D-15-11 | 92 | Witch opens shop; non-witch buys and uses | implemented | see S12 | SurvivalLoopGameTests, ShopLifecycleGameTests, ShopPurchaseGameTests |
| D-15-12 | 93 | State persists across saves | implemented | verified SavedData shards | ShopPersistenceGameTests, FamiliarPersistenceTest |
| D-15-13 | 93 | State persists across MP restarts | implemented | captureRestartTest (CaptureRestartGameTests, M8; Docket dk-f585c58f now qualified), saveUpgradeTest, familiar/tools/commerce harnesses | FamiliarRestartGameTests, MagicToolsRestartGameTests, ShopRestartGameTests, CaptureRestartGameTests |
| D-15-14 | 94 | All systems ship together | implemented (M12 qualification) | see D-0-1 | M12 qualification |
| D-15-15 | 95 | Developer decisions finalized | implemented | MC 1.20.1/Forge, energy 100-200, 14 herbs, emeralds, vanilla animals, follow cap 3, PvP off, durations: DEC, COMPAT, CAT/* | WitchResourcesTest, PreparationCatalogTest |

## Not implemented as designed
| ID | Status | Note |
|---|---|---|
| D-1-7 | approved-replacement | Founding NPC replaced by survival founder + operator designate (DEC 0.5.0) |
| D-12-11 | approved-replacement | Emeralds chosen; no economy adapter (DEC M11, COMPAT) |
| D-3-15 | interpretation (owner to confirm) | Hearth "home anchor" = ward focus; no separate mechanic |
| D-13-12 | interpretation (owner to confirm) | No experimentation mechanic; advisory/shallow discovery (DEC 0.3.0) |
| D-7-10 | deferred (optional by design) | No modded-mob adapters; owner to confirm |
| D-14-38 | owner check | Integrated-host level.dat checkpoint cannot run headless |

Semantic notes: mineral grinding is sand and bone only (D-3-5); "animal brews" read as familiar tonics + sleep mixture (D-3-19); "protection tokens" read as sachet + amulets (D-3-39); crop-yield blessing affects herb beds only (D-4-16); moon phases are full-moon only (D-2-25, D-11-24); boss immunity is a datapack tag, not a toml key (D-6-6); raven/bat scouting is the shared SCOUT command (D-9-4, D-9-19).

## Counts
Total rows: 355.

| Status | Count |
|---|---|
| implemented (incl. "implemented (M12 qualification)", "implemented via shops", "implemented (documented limits)") | 344 |
| implemented-unverified | 5 (D-8-3, D-11-4, D-14-36, D-14-37, D-14-42) |
| approved-replacement | 2 (D-1-7, D-12-11) |
| interpretation (owner to confirm) | 2 (D-3-15, D-13-12) |
| deferred (optional by design) | 1 (D-7-10) |
| owner check | 1 (D-14-38) |
| missing | 0 |
