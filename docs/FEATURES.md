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
M9 gameplay is implemented; the final acceptance table below supersedes the historical phase status. Private 0.8.0 passed headless acceptance. M8 ordinary-animal history follows.
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
| 16 advanced preparations: 4 drinks, 6 thrown vials, 4 oils, blinding dust, moon essence; 17 four-dose mixtures; labelled vials (an NBT flag); 10 familiar preparations deferred to M9 | implemented; familiar effects now implemented in M9 below |
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
| Familiar effects of the familiar preparations | deferred in M7; implemented and tested in M9 below |

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
| Energy spent by production | implemented in 0.6.0 (M7); familiar spending implemented in M9 below |

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

Nothing is planned beyond the release candidate (M12, below): owner acceptance, publishing and pack
integration are the owner's decisions (docs/qualification/release-readiness.md). M8 capture restart
qualification, open since 0.7.0, passed in M12 (captureRestartTest).

Not planned: publishing, pack integration or a release before the owner decides.

### M9: multiple familiars — final qualification

All gameplay phases and headless acceptance are complete in private 0.8.0;
owner visual, shader, reload and two-client checks remain pending independently.

| Area | Automated coverage |
|---|---|
| Eight species, paid binding and canonical identity | Species matrix, ownership/trust refusal, conversion cancellation, stale-source admission, M8 release-to-binding interop |
| Private unlimited roster, detail and seven commands | Keyset pages, forged/stale sessions, real ground/flight navigation, cap reductions, offline reservations, friendly-target refusal |
| Homes and safe rest | Real placement/support loss, compatibility/occupancy, instance replacement, pending unloaded homes, navigation and offline healing |
| Equipment, progression and preparations | Real menu/shift-click/cursor escrow, cargo conservation, shared charm charge, stable modifiers, paid upgrades, all eleven products and shared poison |
| Paid bounded powers | Every species power, no-result/zero-energy/permission refusal, changed target tokens, retrieval tombstones, finite offline reserve and configuration invalidation |
| Incapacitation and revival | No drops/XP, retained inventory, exact paid restoration, failed insertion, generation refusal and administrative recovery |
| Restart and crash boundaries | ANU `HerbalWitch-9f3627badbc6a2751684cbde-20261023-055219-9f9` at `9f3627b`: 76 actual checkpoint snapshots across two JVMs, all required evidence passed |
| Work limits | 120 paginated bonds, three owners at the three-mobile cap, three loaded offline home guards; scan/path counters and forced-chunk baseline; source guard forbids chunk/ticket acquisition |
| Acquisition, grimoire and showcase | Daytime spawn rules, creative eggs, feature-owned root plus eight guide pages, nine inert appearances and complete labelled supply barrels |
| Art | 1,100 generated files unchanged by repeat datagen; 86 Node tests; owned Blockbench gear/eggs/homes and raven/crow/owl/snake rigs; 19 new-species and 20 native equipped poses visually reviewed |

Base and optional-profile results passed (evidence below). The restart runner is isolated from
normal build/check and uses checkpoint replay rather than random process termination. See
[restart procedure](../tools/qualification/restart/familiars/README.md) and [familiar reference](catalogue/familiars.md).
Phase commits, failed runs and corrections are recorded in Docket `dk-5b3234c4`.
M9 acceptance runs (ANU logs live under G:/mods-ci/logs/<job>.log):

| Check | Commit / job | Result |
|---|---|---|
| Base, test runGameTestServer | ef20d02 / HerbalWitch-ef20d0279dc31c6fabdcfa34-20261023-060516-ea8 | 460 JUnit and 285 required GameTests passed |
| EMI | 2237de0 / HerbalWitch-2237de0e20833d758241c5fd-20261023-055428-ccd | 285 passed |
| JEI | 2237de0 / HerbalWitch-2237de0e20833d758241c5fd-20261023-060113-41c | 285 passed |
| Combined viewers, Curios, Farmer's Delight, Thirst | 2237de0 / HerbalWitch-2237de0e20833d758241c5fd-20261023-060136-92c | 285 passed |
| Doggy Talents Next | 2237de0 / HerbalWitch-2237de0e20833d758241c5fd-20261023-060146-4e1 | 286 passed |
| Separate-process restart | 9f3627b / HerbalWitch-9f3627badbc6a2751684cbde-20261023-055219-9f9 | 76 checkpoints; JVMs 14588 and 6480; passed |

Local offline runData/test/build/compileGameTestJava passed; 460 Java tests and 86 Node tests, datagen wrote zero of 1,100 existing generated files. Workspace sync passed four mods; release-jar exclusions and Docket checks passed. The last fixture correction removes neighboring dropped items before showcase setup: the diagnostic had reported 'operator fixture preflight: Oak Sign is in the way at 500 -59 5'. Runtime showcase occupancy checks were correct and remain unchanged. Earlier failures and fixes remain in the Docket phase history.

Owner checks still pending: species silhouettes and animation, equipped/rest poses, UI and private synchronization with two clients, shaders and F3+T. No client, Blockbench, pack installation or public release was performed. There is no available protection-mod adapter; unsupported-claim refusal and UUID-aware permission seams are tested instead.

## M10: broom and wand ? private 0.9.0

Both checked-placement crafting stations and screens, owner-bound broom flight/lifecycle, seven wand selectors, shared harvesting/fuel transactions, owned Blockbench drafts, grimoire guidance and four separately labelled witchcraft showcase sections are implemented. The combined showcase is 125 x 8 x 60. Automated acceptance passed before promotion to private 0.9.0; owner visual/two-client checks remain separate.

| Check | Commit / ANU job | Result |
|---|---|---|
| Base, offline test build runGameTestServer | 7d76a67 / HerbalWitch-7d76a67c6362a4cad151b615-20261023-090203-b2c | 479 unit tests, build/art checks and all 319 required GameTests passed; 285 seconds |
| Final 0.9.0, offline test build runGameTestServer | f59f2f5 / HerbalWitch-f59f2f5c750658c598c379f9-20261023-093324-fa2 | 479 unit tests, build/art checks and all 319 required GameTests passed; 253 seconds |
| Two-process physical restart | bbbe6f8 / HerbalWitch-bbbe6f8e9d4d04918caee244-20261023-083739-389 | Nine assertions passed; JVMs 2092 and 21788; 130 seconds |
| EMI | 7d76a67 / HerbalWitch-7d76a67c6362a4cad151b615-20261023-090220-edc | All 319 required GameTests passed; 220 seconds |
| JEI | 7d76a67 / HerbalWitch-7d76a67c6362a4cad151b615-20261023-090222-9da | All 319 required GameTests passed; 241 seconds |
| Combined viewers | 7d76a67 / HerbalWitch-7d76a67c6362a4cad151b615-20261023-090225-a87 | All 319 required GameTests passed; 207 seconds |
| Combined viewers, Curios, Farmer's Delight, Thirst | 7d76a67 / HerbalWitch-7d76a67c6362a4cad151b615-20261023-090622-03a | All 319 required GameTests passed; 246 seconds |

Local offline runData twice matched all 1,117 generated files. The local test/build/compileGameTestJava gate passed: 479 Java tests and 87 Node tests. All 17 tool/station poses and three native-player riding views were inspected in the M10 job previews directory. Jar inspection found no test classes, fixtures, structures, tooling or foreign dependencies; diff, workspace-sync and Docket guards passed. ANU profiles use existing cached inputs with offline dependency resolution.

The restart harness saves actual deployed/packed tools and an interrupted repair in one JVM, then recovers them in another without reconstructing world objects. It proves one world broom, no restored lease, packed metadata, seven mode preferences, paused repair, cancellation conservation, no offline spending and stale-nonce refusal. It does not promise power-loss atomicity across Minecraft player/chunk files. See [tool reference](catalogue/tools.md) and [restart procedure](../tools/qualification/restart/magic-tools/README.md).

Acceptance found and corrected vanilla mounted-vehicle save ownership, crop-fixture lighting, and existing familiar fixture assumptions about bounds, random position, bounded-query contamination and binding readiness. Familiar gameplay remained unchanged. Phase commits and failed/canceled runs remain in Docket dk-2d540cf6; successful reruns do not erase that history. M11 commerce remains independent and unimplemented here.

Owner follow-up: survival crafting/repair and all controls, live rider animation/camera, GUI scales, shaders, F3+T, two-client latency/reconnect and both familiar/ritual entry points in the dedicated mod-test instance. No client, Blockbench UI, pack installation or public release was performed.

The final local 0.9.0 gate passed in 3m26s. The private jar is `build/libs/herbalwitch-1.20.1-0.9.0.jar` (5,085 entries), SHA-256 `be30724d4e19c7fc2981aaf00c1c8e5087f45d0644793761eee7fec05d264ecd`; its metadata is 0.9.0 and it contains no test/tool/fixture or foreign class files. Docket dk-2d540cf6 is complete.

## M11: apothecary commerce - private 0.10.0

Shop register, stock shelf, stock jar and order board (owned Blockbench first drafts), per-shop verified
records, one prepare/apply/player-checkpoint/commit transfer path for deposits, withdrawals, purchases and
proceeds, proof-based restart and login reconciliation with operator inspect/reconcile/discard, sessions
with nonce and sequence replay protection, role-filtered screens, fixture linking and display projections,
real villager customers with durable bounded demand budgets, nonbinding requests, ten grimoire guides and an
inert showcase section (witchcraft showcase stays 125 x 8 x 60, 62,713 writes). Catalogue:
[commerce](catalogue/commerce.md).

| Evidence | Commit / job | Result |
|---|---|---|
| Local fast gate | 85e886c | runData twice byte-identical; shop, showcase, resource and dependency JUnit suites and `build -x test` (incl. checkArt) green; 90 Node art/preview tests. Two local full `test build` runs were interrupted by an external Gradle daemon stop; the full 519-test JUnit suite ran on ANU |
| ANU test runGameTestServer (first) | dbdb00b / HerbalWitch-m11-commerce-0.10.0-20261023-124323-d72 | 7 of 346 failed: 2 production refusal-order defects, 5 test defects/pins; fixed |
| ANU test runGameTestServer (second) | 14c55b4 / ...-20261023-125643-8ab | 2 of 346 failed: neighbour item interference in two existing tests; isolated |
| ANU test runGameTestServer | 85e886c / HerbalWitch-m11-commerce-0.10.0-20261023-130225-baa | 519 unit, all 346 GameTests passed |
| ANU test build runGameTestServer (promotion) | 9d36ce6 / HerbalWitch-m11-commerce-0.10.0-20261023-130906-58b | 519 unit, build, all 346 GameTests passed |
| ANU commerceRestartTest | 85e886c / HerbalWitch-m11-commerce-0.10.0-20261023-130503-90b | two JVMs, 25 checks passed |

Pending: owner in-game review (placement preview, facings and item views, two clients buying the last lot,
owner logout during a sale, privacy of proceeds/ledger/requests, villager arrival and exhausted demand,
the board request-to-purchase flow, the commerce screen layout, Blockbench edit/recompile). The
integrated-host (level.dat) player checkpoint path is implemented but not automation-qualified.
