# Features - The Herbal Witch

Implemented versus planned, by milestone. Acceptance evidence is linked when a milestone closes.
Detail per entry: docs/catalogue/{decor,stations,products,witchcraft}.md.

## Implemented

Version 0.6.1 is the structural prerequisite for M8/M9: grouped packages, feature-owned grimoire
content, shared recipe/roster contracts, guarded witch spending, split ward services, unified
preparations, shared client presentation and byte-identical art tooling. Gameplay, IDs, saves,
registration order and protocol 4 are preserved. The only intended corrections are Escape reaching
the enclosing screen and durable witch-state rollback when a spend participant throws.

Local fast-gate evidence: 335 existing + 31 added JUnit tests; 75 existing + 4 added Node tests;
171 existing + 26 added GameTest methods compile. Runtime GameTests and optional-mod qualification
are delegated to the orchestrator after the committed handoff; no client was launched. Owner checks
remain for all four panel hosts, changed-rule recipe text, drinks/oils, wards and two-client private
updates. Headless preview review and all 2,693 baseline resource/art hashes are recorded locally.
M8 ordinary-animal implementation is now a candidate on this branch; M9 implementation is in progress.
M9 prerequisites now include the neutral familiar-guide handoff, shared verified snapshot writer and
shared loaded companion identity index. Their tests preserve M8 capture bytes and guide ordering;
they do not constitute a 0.8.0 milestone implementation. The first M9 phase adds canonical per-bond
storage, conservative admission, eight species adapters, and paid altar binding. Its runtime gate is
being qualified; roster, commands, homes, equipment, abilities, preparations, recovery and art follow.
M8 adds six baits, personal loaded-time care state, existing-salve targeting, temporary goal-based
calming, reused M7 sleep, earned native taming/keepers, a private grimoire card, verified capture
checkpoints/reconciliation/recovery and sixteen showcase habitats. Exact boundaries and defaults:
[catalogue/animals.md](catalogue/animals.md). Local fast gate: 406 JUnit tests and 81 Node tests pass,
223 GameTests compile, and the implementation's two datagen runs left all 1,051 generated files byte-identical. Baseline
source declarations were 371 JUnit and 197 GameTests (not a separately executed baseline run).
All three art checks pass; nine animal sprites and the full preview gallery were inspected.
ANU job `HerbalWitch-m8-animals-0.7.0-20261022-185625-ed2` at `60cfacf` passed 406 JUnit and all
223 required GameTests (97 seconds). It fixes the first run's eight failures: the showcase's wrong
sleep-vial registry, the same lookup in the projectile fixture, premature navigation/transport
fixtures, and the pre-M8 grimoire expectations. The original order pin remains intact with explicit
M8 additions. Restart/fault scenarios, optional profiles and owner visuals remain unqualified;
version promotion to 0.7.0 awaits complete automated acceptance. Follow-ups remain in Docket.

Version 0.6.0 (local candidate): Milestone 7, advanced brewing, charms, the ritual circle and ward stones, is implemented on top of 0.5.0; see "0.6.0: advanced brewing, charms, circle and wards" below. Automated acceptance is tracked there; owner checks are pending.

Version 0.5.0: Milestone 6, initiation and energy, is implemented on top of 0.4.0; see "0.5.0: initiation and energy" below. Automated acceptance is tracked there; owner checks are pending.

Version 0.4.0: Milestone 5, the full furniture catalogue (94 pieces), is implemented on top of 0.3.0; see "0.4.0: the furniture catalogue" below. Headless acceptance passed (unit tests, GameTests on the ANU CI runner); owner visual and two-client checks are pending.

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
| `/herbalwitch showcase [group or section]` (operators): every registered block, labelled beside it, in two showcases (furniture, and everything else) each on a plot it levels and floors itself | 2 | done | ShowcaseCommandGameTests, ShowcaseBoundaryGameTests, ShowcaseContentGameTests, ShowcasePlanTest, ShowcaseCoverageTest |
| Five stations (drying rack, mortar, tea kettle, hearth, infusion pot) with menus, in-slot reservations, heat, automation sides, no catch-up | 3 | done; owner UI/art review pending | StationProcessingGameTests, ProcessBehaviourGameTests, ProcessReservationTest, ProcessCompletionTest, StationResourcesTest |
| Bundles, powders, 7 drinks, salve, oils, garden products, oil lantern, warmth and pest-repellent effects, resin | 3 | done | PreparationUseGameTests, PreparationBlockGameTests, PreparationResourcesTest |
| EMI and JEI categories; Farmer's Delight heat both ways; Thirst Was Taken hydration and purity | 3 | done; viewers are owner checks | CompatibilityGameTests (`-PcompatProfile=all`) |

### 0.6.0: advanced brewing, charms, circle and wards (Milestone 7)

Status: implemented. Automated acceptance: pure unit tests (ProcessRequirements, CircleLayout,
GardenBlessingPlan, WardCodec, WardIndex, PreparationCatalog, the new resource manifests) and GameTests
(MagicalProcess, AdvancedStation, AdvancedPreparation, Circle, Ward, Charm); their results are recorded in
the milestone report, and a green ANU run closes the wave. Detail: docs/catalogue/{stations,products,witchcraft}.md,
docs/ARCHITECTURE.md ("Stations", "Advanced witchcraft"), docs/DECISIONS.md ("0.6.0"). Protocol "4".

| Feature | Status |
|---|---|
| Witch recipes: nothing is reserved or run until a witch presses Start (Start, Resume, Cancel are vanilla menu buttons); progress only while the payer is online, alive and within 8 blocks; energy debited once at completion with practice awarded; Cancel consumes nothing | implemented |
| Recipe `requirements` (`witch`, `energy`) on the wire and in the fingerprint; `processingPercent` scales energy; mortar and infusion pot carry witch recipes (blinding dust, four oils) | implemented |
| Four advanced stations: Witch's Cauldron, Copper Still, Bottling Bench, Charm Workbench (shared block and entity classes, previewed placement, `active` state) | implemented; art is an agent first draft |
| 16 advanced preparations: 4 drinks, 6 thrown vials, 4 oils, blinding dust, moon essence; 17 four-dose mixtures; labelled vials (an NBT flag); 10 familiar preparations that refuse until M9 | implemented; the familiar effects wait for M9 |
| Control, combat and repellent effects (binding, sleep, steadfast, concealment, dazzled, thorns, repellents; weapon coating) with hard-control immunity and entity tags | implemented |
| Thrown preparations: radius 3, 32 targets, line of sight, per-thrower and per-dimension limits, shared 20-tick throw cooldown | implemented |
| One entity-action permission boundary (self, protected tag, foreign pets, PvP only when enabled, spawn protection, claim mods) | implemented |
| Charms: Protective Sachet, Undead Amulet, Arthropod Amulet; worn in the off hand or a Curios `charm` slot; charged by wearing, break when spent | implemented; Curios is compile-only and optional |
| Ritual circle: a 5x5 ring of 16 cells around the altar, placed whole, validated every 20 ticks | implemented; art is an agent first draft |
| Garden Blessing (15 energy, 200 ticks) and Home Protection (20 energy, 200 ticks); the witch aims at the bed or stone, then opens the altar | implemented |
| Ward stone: owner-only controls, radius 1..8 (12 near a hearth or stove), box height 4, undead/arthropod/both, 600 s charge, limits per owner, chunk and dimension, persisted world-wide | implemented; art is an agent first draft |
| Grimoire: four station pages, preparations, wards, charms, circle, garden blessing and home protection pages; 113 processing recipes | implemented |
| JEI and EMI ritual categories; recipe viewers show the witch requirement | implemented; viewers are owner checks |
| Showcase sections `cauldron`, `still`, `bottling_bench`, `charm_workbench`, `advanced_preparations`, `ritual_circle`, `ward_stone`, `charms`; rows packed to 126 wide, GameTest template 128 x 10 x 64 | implemented |
| Familiar effects of the familiar preparations | not implemented (M9) |

Scope notes: all M7 energy is spent through `WitchStatusService`; `FamiliarPreparationApplications.install`
is the M9 hook. Ward-charging GameTests run in their own batches (docs/GOTCHAS.md). Showcase bounds:
121 x 8 x 38 in two rows (template 128 x 10 x 64).

Owner checks pending: every advanced station in game (placement preview, GUI, Start, Resume, Cancel with
two clients, the active state); thrown vials and their particles; the weapon coating, thorns and
repellent behaviour against real mobs; sleep and binding on a boss and a villager; charms with and
without Curios; the circle placement and preview in every facing; the Garden Blessing and Home
Protection target selection; the ward screen, the boundary preview and ward overlap refusals; ward
protection after a restart; the agent first-draft art of the six new pieces and the item sprites;
resource reload; shaders.

### 0.5.0: initiation and energy (Milestone 6)

Status: implemented. Automated acceptance: pure unit tests, resource tests and GameTests
(WitchLifecycle, EnergyRecovery, AltarPlacement, AltarAction, AltarShowcase) are written; their results
are recorded in the milestone report, and a green ANU run closes the wave. Not covered: a true
out-of-process server restart (restart semantics are covered by codec and state unit tests and the
durable-persist failure GameTest), and the vanilla sleep path (fake players never complete it, so the
sleep reward is tested through `EnergyEvents.sleptThrough`). Detail: docs/catalogue/witchcraft.md,
docs/ARCHITECTURE.md ("Witchcraft"), docs/DECISIONS.md ("0.5.0"). Protocol "3".

| Feature | Status |
|---|---|
| Witch capability `herbalwitch:witch/status` (schema 1): initiated, energy, practice, tea cooldown, restful preparation, sleep day; private snapshots with epoch and revision | implemented |
| Energy: capacity from practice (100, ten thresholds, +10 each, final 200), passive rest, meditation, sleep, energy tea, restful infusion, nature and moon bonus | implemented |
| Functional ritual altar (2 x 1 x 2, one item, shared previewed placement, `active` state), zero-slot menu, 252 x 200 screen | implemented; art is an agent first draft |
| Founding of the first lineage by a survival player (one per world, durable claim, NEEDS_REVIEW on crash ambiguity) | implemented |
| Consenting initiation, operator `designate`, `inspect`, `founding inspect` and `founding resolve` | implemented |
| Attunement (solo practice) | implemented |
| Energy HUD and `herbalwitch-client.toml` `[hud]`; `herbalwitch-server.toml` rules synced to clients | implemented |
| Grimoire category "Witchcraft" (status, energy, recovery, altar, four ritual pages); founding and initiation grant the witchcraft and action discoveries | implemented |
| Showcase section `altar`; showcase 126 x 8 x 28 | implemented |
| Energy spent by production | implemented in 0.6.0 (M7); familiars not implemented (M9) |

Scope notes: production consumes energy only through `WitchStatusService` (done in 0.6.0); M9 familiar powers charge
the owner the same way.

Owner checks pending: solo founding; two-client initiation accept and decline; energy privacy;
reconnect and death; simultaneous attempts at one altar; the HUD at several GUI scales with chat and
other overlays; placement preview against result in every facing; the active and idle altar art (the
moon glow is orange, from the hearth's glow texture, and may want violet); item transforms; shaders;
resource reload; sleep in vanilla beds and M5 beds.

### 0.4.0: the furniture catalogue (Milestone 5)

Status: implemented; headless verified (unit tests, GameTests on the ANU CI runner). Owner visual and
two-client checks pending. Detail: docs/catalogue/decor.md (exact catalogue), docs/ARCHITECTURE.md
("Decor"), docs/DECISIONS.md ("0.4.0"). The showcase became 117 x 8 x 28 (it was 86 x 8 x 28); 0.5.0 made it 126 x 8 x 28.

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
| 8 | Ordinary-animal candidate implemented; ANU lifecycle/restart qualification and owner review pending |
| 9 | Multiple familiars: binding, roster, seven commands, homes, equipment, abilities, familiar brews, revival; familiar powers charge the owner's energy through `WitchStatusService` |
| 10 | Broom and wand |
| 11 | Apothecary commerce: register, counters, stock, offers, proceeds, history, order board |
| 12 | Complete-release qualification |

Not planned: publishing, pack integration or a release before the owner decides.
