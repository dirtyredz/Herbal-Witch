# Station catalogue

Traces each of the 16 station groups in the design ("Distinct functional workstations") to blocks, owning module, recipe type, witch requirement and milestone. Every station has its own purpose, a small recipe interface, limited slots, clear output and grimoire recipe lookup.

Module paths are relative to `io.github.dirtyredz.herbalwitch`. Milestones are from plan section 3.

| Station | Purpose (design) | Blocks it covers | Owning module | Recipe type | Witch-only? | Milestone | Evidence |
|---|---|---|---|---|---|---|---|
| Herb drying rack | Dry and preserve fresh herbs into bundles | drying rack | `apothecary/stations/everyday/drying_rack/` | drying | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, StationResourcesTest |
| Mortar and pestle | Grind herbs and minerals into powders, pigments, blinding dust | mortar and pestle | `apothecary/stations/everyday/mortar/` | grinding | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, StationResourcesTest |
| Tea kettle | Steep teas, tonics, simple infusions; requires heat | tea kettle | `apothecary/stations/everyday/kettle/` | steeping | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, CompatibilityGameTests |
| Fireplace / hearth | Heat for kettle, cauldron, infusion pot; home anchor and ward focus | hearth block (2x2); Witch's Stove (2 wide); decor Fireplace links here | `apothecary/stations/everyday/hearth/`, `apothecary/stations/stove/` (heat tags in `apothecary/stations/heat/`); ward focus in `magic/ward/` | heat source (tag) | no (ward focus: witch) | 3 (heat); 7 (ward focus) | heat done (0.1.0-dev.3): StationProcessingGameTests, CompatibilityGameTests; ward focus M7 |
| Brewing cauldron | Liquid potions, throwables, animal brews, familiar tonics | Witch's Cauldron | `apothecary/stations/advanced/cauldron/` | `cauldron` | yes (magical brewing) | 7 | done (0.6.0): MagicalProcessGameTests, AdvancedStationGameTests; see "Advanced stations" below |
| Infusion pot | Slowly prepare salves, oils, repellents, lantern oils | infusion pot | `apothecary/stations/everyday/infusion_pot/` | infusion | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, PreparationUseGameTests |
| Distillation still | Refine rare essences and advanced extracts | Copper Still | `apothecary/stations/advanced/still/` | `distillation` | yes (advanced brew) | 7 | done (0.6.0): MagicalProcessGameTests, AdvancedStationGameTests |
| Apothecary bottling bench | Bottle, portion, label, package mixtures | Bottling Bench | `apothecary/stations/advanced/bottling_bench/` | `bottling` | no (packaging only; decided at M7) | 7 | done (0.6.0): AdvancedStationGameTests |
| Altar and ritual circle | Initiation, familiar binding, home blessings, revival, wards | altar (M6), ritual circle, decor Altar Table links here | `magic/ritual/` | rituals | yes (initiation: existing witch; founder ritual: survival) | 6 (altar); 7 (circle, rituals) | altar done (0.5.0): see "The ritual altar" below; circle and its two rituals done (0.6.0): see "The ritual circle" below |
| Charm workbench | Sachets, amulets, animal capture charms, protection tokens | Charm Workbench | `apothecary/stations/advanced/charm_workbench/` | `charm_work` | yes (magical; decided at M7) | 7 | done (0.6.0) for the three protective charms: AdvancedStationGameTests, CharmGameTests; capture charms wait for M8 |
| Grimoire stand / witch bookshelf | Research herbs, recipes, rituals | grimoire stand, witch bookshelf | `grimoire/access/block/` (with `grimoire/content/`, `grimoire/knowledge/`) | none (reads all recipes) | no | 4 | done (0.3.0): automated acceptance passed (unit tests, GameTests); owner checks pending |
| Familiar beds, nests and perches | Assign rest locations and recovery | bed, nest, perch | `companions/familiars/home/` | none | familiar owner | 9 | functional placement, assignment and safe rest qualified |
| Home ward stone | Define guarded home area and targeting settings | ward stone | `magic/ward/` | none (ward activation, 20 energy default) | yes | 7 | done (0.6.0): WardGameTests, WardCodecTest, WardIndexTest; see "Ward stone" below |
| Apothecary counter, register, shelves and jars | Stock, sell, display, track goods | counter, register, stock shelves, jars; decor Apothecary Counter links here | `shop/block/` (with `shop/catalog/`, `shop/trade/`, `shop/order/`, `shop/persistence/`) | none | witch runs shop; non-witch inspects and buys | 11 | not started |
| Broom crafting stand | Craft and repair rideable flying brooms | broom stand | `magic/broom/` + `apothecary/stations/broom_stand/` | broom work | yes (broom is witch-only) | 10 | not started |
| Wand carving bench | Create utility wands for everyday tasks | wand bench | `magic/wand/` + `apothecary/stations/wand_bench/` | wand carving | assumed yes (wand uses energy); confirm at M10 | 10 | not started |

## Milestone 3 stations in detail

Recipe types are `herbalwitch:processing/{drying,grinding,steeping,infusion}`. Block ids are
`herbalwitch:stations/<station>/station`. Every recipe lists ingredients (counts 1..64), duration
(1..72000 ticks), and at most 2 returned containers. Every filled input slot must be used by the recipe.

| Station | Slots | Recipe type | Duration | Heat | Automation sides | Assembly recipe |
|---|---|---|---|---|---|---|
| Drying rack | input, output | drying | 1,200 ticks | none | input from above; output from the bottom | `TST` / `s s` / `TST` (sticks T, string S, wooden slabs s) |
| Mortar and pestle | 3 ingredients, output, returned container | grinding | 100 ticks | none | ingredients from above; products and returns from the bottom | ` T ` / `SFS` / ` S ` (stick, 3 smooth stone, flint) |
| Tea kettle | 2 ingredients, 1 water bottle, drink, return | steeping | 200 ticks | required | ingredients from above; water bottle from the sides; drink and return from the bottom | `I C` / `III` / ` I ` (5 iron, 1 copper) |
| Hearth | fuel, returned container | none (heat source) | burns fuel for its normal time | provides heat, LIT, light 13 on every cell | fuel from the sides; returns from the bottom | `B B` / `BCB` / `B B` (6 bricks, campfire) |
| Witch's Stove | fuel, returned container (the hearth's) | none (heat source) | burns fuel like the hearth | two burners on top, each heats a vessel on it while lit | as the hearth | `III` / `ICI` / `B B` (5 iron, campfire, 2 bricks) |
| Infusion pot | 2 ingredients, base liquid, package, result, return | infusion | 600 ticks | required | ingredients from above; base and package from the sides; result and return from the bottom | `IGI` / `B B` / `B B` (2 iron, glass bottle, 4 bricks) |

- **Water bottle:** the kettle accepts only the vanilla water potion (partial NBT match). The bottle
  becomes the drink and nothing is returned. The infusion pot returns the base's glass bottle.
- **Hearth:** burns any furnace fuel while loaded, no vessel needed; a lava bucket returns a bucket;
  no fire spread. A vessel heats on its chimney top, two blocks up.
- **Witch's Stove:** a two-block-wide range that burns fuel exactly like the hearth (it reuses the
  hearth's block entity type, menu and screen). Each burner heats a vessel standing on it while lit.
  In `herbalwitch:heat_sources` and Farmer's Delight's `heat_sources` tag. The kettle and infusion pot
  showcases stand on a fuelled stove.
- **Heat:** a station needs a source (`herbalwitch:heat_sources`: hearth, campfire, soul campfire,
  magma block, the Witch's Stove, optional Farmer's Delight stove) directly below, or one conductor
  (`herbalwitch:heat_conductors`: hopper, optional FD conductors) then a source. LIT must be true
  where the block has it; waterlogged gives none.
- **UI status lines:** Waiting, Needs heat, Working, Output full.
- **Recipes (50 processing in 0.3.0; 113 since 0.6.0):** drying 14 (one per herb); grinding 24 (14 bundles
  to powders, plant oil, fertilizer, flower dust, mushroom dust, four dyes, bone to 3 bone meal, and the
  witch recipe for blinding dust); steeping 7; infusion 11 (the six everyday recipes and five witch oils:
  venom, thorn, the two repellents and the familiar Emberguard Oil); cauldron 17; distillation 3; bottling 34; charm work 3. Each is listed in
  docs/catalogue/products.md.
- **Reloads and saves:** a changed or removed recipe cancels the job and resets progress (items stay);
  saved jobs use schema 2 (schema 1 loads; a newer schema is preserved and the station idles until its
  inventory changes); unloaded stations do not progress.

## Advanced stations (Milestone 7, 0.6.0)

Recipe types are `herbalwitch:processing/{cauldron,distillation,bottling,charm_work}`; block ids are
`herbalwitch:stations/<station>/station`. The four share `AdvancedStationBlock` and `AdvancedStationEntity`
(`apothecary/stations/advanced/`), place through the shared previewed `StructurePlacementItem` and show an `active`
blockstate while working. `StationCatalog` lists them after the six everyday stations.

| Station | Size | Recipe type | Duration | Heat | Witch | Assembly recipe (shapeless) |
|---|---|---|---|---|---|---|
| Witch's Cauldron | 1 block | cauldron | 600 ticks | required, below | yes (Start) | cauldron, copper ingot, amethyst shard |
| Copper Still | 1 wide, 2 tall | distillation | 800 ticks | required, below | yes (Start) | 4 copper ingots, 2 glass, iron ingot, amethyst shard |
| Bottling Bench | 2 wide, 1 deep | bottling | 100 ticks | none | no | crafting table, 2 dark oak planks, 2 glass bottles, iron nugget |
| Charm Workbench | 2 wide, 1 deep | charm_work | 200 ticks | none | yes (Start) | crafting table, 2 dark oak planks, 2 string, gold ingot, amethyst shard |

- **Cauldron:** a water base makes a four-dose mixture of a utility, combat or familiar preparation
  (17 mixtures). **Still:** a base plus a glass bottle; three recipes: moon essence, Venomleaf Extract
  and Warding Essence. **Bottling Bench:** portions one whole mixture into four bottles; adding gunpowder and paper makes
  the thrown variant a labelled vial (the label is an NBT flag on the result). **Charm Workbench:** the
  Protective Sachet and the two amulets.
- **Witch recipes:** a recipe with `"requirements": {"witch": true, "energy": N}` is MAGICAL (see
  "Rules"). The panel on the screen shows Start, Resume, Cancel and the energy cost; the status line adds
  Needs witch, Needs operator and Needs energy to Waiting, Needs heat, Working and Output full. The mortar
  (blinding dust) and the infusion pot (venom, thorn, undead repellent and arthropod repellent oils, and
  the familiar Emberguard Oil) also carry witch recipes.
- **Placement, teardown and heat** follow the everyday multiblocks: every cell replaceable, a sturdy floor
  under grounded cells, one item dropped from the origin, any cell opens the origin's menu, `active`
  shared by every cell. The cauldron and still need a heat source directly below (the rules above).
- **Ownership and range:** the player who pressed Start is the payer. Progress runs only while they are
  online, alive, in the dimension and within 8 blocks; otherwise the batch pauses (Resume continues it).
  Others cannot take it over and can cancel it only after the payer's lease has ended. Start, Resume and
  Cancel are rate-limited to 5 per 20 ticks.

## Research blocks (Milestone 4, 0.3.0)

| Block | Id | Opens on | Recipe | Look |
|---|---|---|---|---|
| Grimoire Stand | `herbalwitch:grimoire/stand` | the research filter | 3 dark oak slabs + 2 sticks + a Grimoire (shapeless) | Crossed foot, pedestal with gold collars, tilted board holding an open book, ribbon and ivy |
| Witch's Bookshelf | `herbalwitch:grimoire/bookshelf` | the index | 6 dark oak planks + 2 books + a Grimoire (shapeless) | Framed carcass, three shelves of volumes, crescent bookend, side moon, ivy |

Ids are permanent. Both are open to ordinary players (no witch requirement) and share one behaviour
(`ResearchAccessBlock`): one cell, horizontal facing, sturdy centre support below, drops itself once, no block
entity, no inventory, no automation sides, `noOcclusion()`. They have no processing recipe type (they read
all recipes and run no job) and no storage; the Milestone 5 storage/display bookshelf is a different piece
(docs/catalogue/decor.md). The reader closes when the block is gone, unloaded, more than 8 blocks away or
`mayInteract` fails. Research there is free and instant, and teaching invitations start only from these
readers. The portable Grimoire (`herbalwitch:grimoire/book`, stack of one) opens the index anywhere and
the station screens show a "Grimoire" button while it is carried. Models are first drafts, see docs/ART.md.

## The ritual altar (Milestone 6, 0.5.0)

| Block | Id | Footprint | Recipe | Interface |
|---|---|---|---|---|
| Ritual Altar | `herbalwitch:magic/ritual/altar` (block, item, block entity and menu share it) | 2 wide x 1 deep x 2 high; cells in canonical order, part 0 the origin | `SAS` / `LGL` / `L L`: 2 smooth stone slabs, amethyst shard, 4 dark oak logs, gold ingot; Apothecary tab, axe | Zero-slot menu: no inventory, offerings come from the user's own main inventory and offhand |

- **Placement:** one item places the whole altar through the shared checked placement
  (`StructurePlacementItem`) with the shared preview; both bottom cells need a sturdy floor.
- **Teardown:** breaking any cell, losing support or an explosion removes the altar with one drop (the
  origin's loot); creative drops nothing; pistons refuse it. It is `noOcclusion`.
- **State:** `active` is a blockstate on every cell while a ritual runs (light 7, visual only, no heat).
- **Use:** the user must be alive, not a spectator, in the same dimension, with the whole altar loaded,
  eyes within 4 blocks of the footprint with line of sight, and pass `ActionPermissionService.mayUse`
  (adventure mode is allowed). One operation per altar and one per player; an invitation does not lock it.
- **Actions:** founding, initiation, meditation, attunement (docs/catalogue/witchcraft.md). Each has an
  explicit start and is cancelled by closing the menu, leaving range or being hurt.
- **Screen:** 252 x 200, two columns (status and actions on the left, consent on the right), a progress
  bar, the last notice, and a Grimoire button that opens the book beside it on the altar page.
- **Ritual circle:** since 0.6.0 the altar has an optional circle; see "The ritual circle" below. The
  altar's own four actions need no circle.

## The ritual circle (Milestone 7, 0.6.0)

| Block | Id | Footprint | Recipe | Interface |
|---|---|---|---|---|
| Ritual Circle | `herbalwitch:magic/ritual/circle` | a 5 x 5 square ring of 16 cells around the altar | shapeless: 4 smooth stone slabs, 2 calcite, amethyst shard, gold nugget | none; used through the altar |

- **Placement:** one item places the whole ring through the shared checked placement with the shared
  preview (`client/magic/ritual/CirclePlacementPreview`). The item is a flat sprite; the 32 compiled models
  (`part_<n>`, `active_<n>`) are an enumerated exception to the 12-file cap (STRUCTURE.md).
- **Teardown:** breaking any cell, losing support or an explosion removes the whole circle with one
  drop; `active` (every cell) glows while a circle ritual runs.
- **Validation:** circle rituals need a complete circle. It is validated every 20 ticks; a broken circle
  cancels the ritual and consumes nothing, and unloaded cells pause it.
- **Rituals** (altar actions; numbers in docs/catalogue/witchcraft.md): Garden Blessing and Home
  Protection. The witch looks at the herb bed or the ward stone, then opens the altar
  (`client/magic/ritual/RitualTargets`).

## Ward stone (Milestone 7, 0.6.0)

| Block | Id | Footprint | Recipe | Interface |
|---|---|---|---|---|
| Ward Stone | `herbalwitch:magic/ward/stone` | 1 block, `active` while the ward is active | shapeless: 4 stone bricks, amethyst shard, gold ingot | menu with owner controls (deactivate, release, radius, profile) |

Rules and numbers: docs/catalogue/witchcraft.md ("Wards"). The ward record lives in the world-wide
`WardSavedData`, not in the block.

## Look and size (0.2.0-dev.2)

House style: the armchair's dark brown wood, charcoal iron, gold rivets, bands and studs, gold crescent moons
and four-point stars, pink flowers on olive vines, dark plum surfaces, rounded octagonal silhouettes
from stacked layers. Previews and recipes are unchanged by the art pass; owner review is pending.

| Station | Size | Look |
|---|---|---|
| Mortar and pestle | 1 block | Mottled grey-taupe stone bowl, octagonal with chamfered corners, tapering to a narrow foot on a stepped square plinth; stepped rim, moss and small pink flowers, bright ground herbs inside, a knob-headed stone pestle leaning out |
| Tea kettle | 1 block (a modest 9 px vessel, about 11 tall) | Bulbous plum body painted with gold moons, stars and pink flowers on olive vines; beaded copper-and-gold rim, stepped lid with gold studs and knob, arched copper handle with gold bands, angled spout with a gold nozzle. Stands on heat |
| Infusion pot | 1 block (a modest 10.5 px pot, 11 tall) | The kettle's sister: squat round plum pot, banded copper-and-gold rims, gold clasps, domed lid painted with a moon, gold knob on a post, copper loop handles with gold caps, short gold feet, small gold plaque. Stands on heat |
| Drying rack | 2 wide x 2 tall | Wooden A-frames at each end whose legs stand on iron feet and lean in to meet at an apex under a small iron cap (never crossing), iron bands and gold rivets; a top beam with gold strap clips and hooks. While it holds anything (input or output) four flat 2D dried-bundle sprites hang from the beam (green leafy, lavender, white-flowered, yellow-flowered); they show occupancy, not which herb |
| Hearth | 2 wide x 2 tall | Gothic fireplace: charcoal stone-brick firebox under a stepped arch, iron grate, wooden mantel posts and shelf with gold bands and caps, ivy over the mantel corners, solid brick behind, dark chimney breast with a gold moon-and-stars plaque, floor slabs, fire tools, a log crate. Lit on every cell (light 13) |
| Witch's Stove | 2 wide x 1 | Charcoal iron cooking range with wooden corner posts and gold caps; two burners on top, gold-framed firebox door, two drawers with gold stars, a towel rail with a red cloth bearing a gold crescent, a rail with a hanging pan, spoon and herb bundle, a stovepipe rising out of the back of the body between the burners |

**Multiblocks.** The drying rack and hearth have the same block entity, menu and recipes as before.
Layouts are named in `StationCatalog` (`StationSpec.layout`). Placing needs every cell replaceable
and a sturdy floor under grounded cells (the rack: both feet), so the rack needs room for all four
cells. Breaking any cell removes all of them. Only the origin cell (part 0) holds the block entity
and drops the item (once, never in creative); any cell opens the origin's menu; lit/active is shared
by every cell. The item shows `models/block/stations/<station>/assembly`.

## Decor lookalikes (0.4.0)

Some furniture in the decor catalogue (docs/catalogue/decor.md) resembles a station. Each is a plain
decorative piece with its own id, no block entity, no recipe type and no heat or processing; the
grimoire page of the decor piece links the working station instead.

| Decor piece (id under `decor/`) | Looks like | The working station |
|---|---|---|
| Decorative Drying Rack (`fixtures/drying_rack_decor`) | hanging herb frame | Herb Drying Rack (`apothecary/stations/everyday/drying_rack/`): dries bundles |
| Fireplace (`fixtures/fireplace`) and Firepit (`fixtures/firepit`) | hearth surround and fire | Hearth (`apothecary/stations/everyday/hearth/`): the heat source for kettle and infusion pot. The firepit lights up and toggles like a lamp but heats nothing |
| Kitchen Range (`fixtures/stove_oven`) | range with an oven door | Witch's Stove (`apothecary/stations/stove/`): two burners, hearth fuel rules |
| Work Table (`surfaces/work_table`) | broad worktable | Mortar and pestle (grinding); the Charm Workbench (`apothecary/stations/advanced/charm_workbench/`, 0.6.0) is a separate working station |
| Altar Table (`fixtures/altar_table`) | dark table with moon motifs | The functional Ritual Altar (`magic/ritual/altar`, 0.5.0) for founding, initiation and meditation, and its Ritual Circle (0.6.0); the infusion pot for preparing |

The recipes differ on purpose: for example the decor rack is string, sticks and slabs, and the
Kitchen Range takes a furnace but never smelts. Dried bundles remain flat 2D sprites on the working
rack and elsewhere; the decorative rack does not change that.

## Rules

M9 familiar homes are functional one-block pieces, not processing stations. Bed, nest and perch use
the shared checked floor-placement flow, open an owner-only grimoire assignment session and support
ordinary loaded rest. They expose no item handler. Exact compatibility and recipes are in
[familiars.md](familiars.md); identity and assignment never travel in the dropped item.

- No universal machine superclass. Shared processing (`apothecary/stations/process/`: reservations, job, completion) is used by composition; `StationBlock` and `StationMenu` share only block and menu boilerplate.
- Ordinary processing (drying, grinding, steeping, infusion, and the bottling bench) is open to non-witches and may be hopper-automated; magical jobs need an authorized witch nearby while progressing and at completion. Each recipe type has an execution policy (ordinary, magical or either) and a recipe's `requirements` (witch, energy) decide the rest.
- A witch recipe reserves and runs nothing until a witch presses Start (a vanilla menu button); the energy is debited once, at completion, through `WitchStatusService`, and the starter is the payer.
- Bottling is optional for teas: ordinary teas can finish in the kettle container. Bottling covers bulk portioning, packaging, labels and throwables.
- Prepared mixtures are bounded item data; there is no fluid system and no Create plumbing dependency.
- Recipe matching never consumes inputs; mutation happens separately, revalidated at completion (inputs, output capacity, witch, energy). `ProcessMatcher` matches, `ProcessCommit` mutates.
- Station inventories and progress live in the block entity; at most 12 source files per station directory, split into `block/`, `menu/`, `recipe/` when needed.
- Each station row closes only with implementation and acceptance evidence (milestone 12 qualification).
