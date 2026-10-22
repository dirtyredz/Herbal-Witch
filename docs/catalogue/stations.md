# Station catalogue

Traces each of the 16 station groups in the design ("Distinct functional workstations") to blocks, owning module, recipe type, witch requirement and milestone. Every station has its own purpose, a small recipe interface, limited slots, clear output and grimoire recipe lookup.

Module paths are relative to `io.github.dirtyredz.herbalwitch`. Milestones are from plan section 3.

| Station | Purpose (design) | Blocks it covers | Owning module | Recipe type | Witch-only? | Milestone | Evidence |
|---|---|---|---|---|---|---|---|
| Herb drying rack | Dry and preserve fresh herbs into bundles | drying rack | `stations/everyday/drying_rack/` | drying | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, StationResourcesTest |
| Mortar and pestle | Grind herbs and minerals into powders, pigments, blinding dust | mortar and pestle | `stations/everyday/mortar/` | grinding | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, StationResourcesTest |
| Tea kettle | Steep teas, tonics, simple infusions; requires heat | tea kettle | `stations/everyday/kettle/` | steeping | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, CompatibilityGameTests |
| Fireplace / hearth | Heat for kettle, cauldron, infusion pot; home anchor and ward focus | hearth block (2x2); Witch's Stove (2 wide); decor Fireplace links here | `stations/everyday/hearth/`, `stations/stove/` (heat tags in `stations/heat/`); ward focus in `magic/ward/` | heat source (tag) | no (ward focus: witch) | 3 (heat); 7 (ward focus) | heat done (0.1.0-dev.3): StationProcessingGameTests, CompatibilityGameTests; ward focus M7 |
| Brewing cauldron | Liquid potions, throwables, animal brews, familiar tonics | cauldron | `stations/cauldron/` | cauldron brewing | yes (magical brewing) | 7 | not started |
| Infusion pot | Slowly prepare salves, oils, repellents, lantern oils | infusion pot | `stations/everyday/infusion_pot/` | infusion | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, PreparationUseGameTests |
| Distillation still | Refine rare essences and advanced extracts | still | `stations/still/` | distillation | yes (advanced brew) | 7 | not started |
| Apothecary bottling bench | Bottle, portion, label, package mixtures | bottling bench | `stations/bottling_bench/` | bottling | no (plan silent; assumed packaging only) | 7 | not started |
| Altar and ritual circle | Initiation, familiar binding, home blessings, revival, wards | altar (basic M6), ritual circle, decor Altar Table links here | `magic/ritual/` | rituals | yes (initiation: existing witch; founder ritual: survival) | 6 (altar); 7 (circle, rituals) | not started |
| Charm workbench | Sachets, amulets, animal capture charms, protection tokens | charm workbench, decor Work Table candidate | `stations/charm_workbench/` | charm work | assumed yes (magical); confirm at M7 | 7 | not started |
| Grimoire stand / witch bookshelf | Research herbs, recipes, rituals | grimoire stand, witch bookshelf | `grimoire/access/block/` (with `grimoire/content/`, `grimoire/knowledge/`) | none (reads all recipes) | no | 4 | done (0.3.0): automated acceptance passed (unit tests, GameTests); owner checks pending |
| Familiar beds, nests and perches | Assign rest locations and recovery | bed, nest, perch | `companions/familiars/home/` | none | owner must be bound witch | 9 | not started |
| Home ward stone | Define guarded home area and targeting settings | ward stone | `magic/ward/` | none (ward activation, 20 energy default) | yes | 7 | not started |
| Apothecary counter, register, shelves and jars | Stock, sell, display, track goods | counter, register, stock shelves, jars; decor Apothecary Counter links here | `shop/block/` (with `shop/catalog/`, `shop/trade/`, `shop/order/`, `shop/persistence/`) | none | witch runs shop; non-witch inspects and buys | 11 | not started |
| Broom crafting stand | Craft and repair rideable flying brooms | broom stand | `magic/broom/` + `stations/broom_stand/` | broom work | yes (broom is witch-only) | 10 | not started |
| Wand carving bench | Create utility wands for everyday tasks | wand bench | `magic/wand/` + `stations/wand_bench/` | wand carving | assumed yes (wand uses energy); confirm at M10 | 10 | not started |

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
- **Recipes (50 processing):** drying 14 (one per herb); grinding 23 (14 bundles to powders, plant oil,
  fertilizer, flower dust, mushroom dust, four dyes, bone to 3 bone meal); steeping 7; infusion 6.
  Each is listed in docs/catalogue/products.md.
- **Reloads and saves:** a changed or removed recipe cancels the job and resets progress (items stay);
  saved jobs use schema 1; unloaded stations do not progress.

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

## Rules

- No universal machine superclass. Shared processing (`stations/process/`: reservations, job, completion) is used by composition; `StationBlock` and `StationMenu` share only block and menu boilerplate.
- Ordinary processing (drying, grinding, steeping, infusion) is open to non-witches and may be hopper-automated; magical jobs need an authorized witch nearby while progressing and at completion.
- Bottling is optional for teas: ordinary teas can finish in the kettle container. Bottling covers bulk portioning, packaging, labels and throwables.
- Prepared mixtures are bounded item data; there is no fluid system and no Create plumbing dependency.
- Recipe matching never consumes inputs; mutation happens separately, revalidated at completion (inputs, output capacity, witch, energy).
- Station inventories and progress live in the block entity; at most 12 source files per station directory, split into `block/`, `menu/`, `recipe/` when needed.
- Each station row closes only with implementation and acceptance evidence (milestone 12 qualification).
