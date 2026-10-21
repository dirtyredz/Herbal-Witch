# Station catalogue

Traces each of the 16 station groups in the design ("Distinct functional workstations") to blocks, owning module, recipe type, witch requirement and milestone. Every station has its own purpose, a small recipe interface, limited slots, clear output and grimoire recipe lookup.

Module paths are relative to `io.github.dirtyredz.herbalwitch`. Milestones are from plan section 3.

| Station | Purpose (design) | Blocks it covers | Owning module | Recipe type | Witch-only? | Milestone | Evidence |
|---|---|---|---|---|---|---|---|
| Herb drying rack | Dry and preserve fresh herbs into bundles | drying rack | `stations/everyday/drying_rack/` | drying | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, StationResourcesTest |
| Mortar and pestle | Grind herbs and minerals into powders, pigments, blinding dust | mortar and pestle | `stations/everyday/mortar/` | grinding | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, StationResourcesTest |
| Tea kettle | Steep teas, tonics, simple infusions; requires heat | tea kettle | `stations/everyday/kettle/` | steeping | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, CompatibilityGameTests |
| Fireplace / hearth | Heat for kettle, cauldron, infusion pot; home anchor and ward focus | hearth block; decor Fireplace links here | `stations/everyday/hearth/` (heat tags in `stations/heat/`); ward focus in `magic/ward/` | heat source (tag) | no (ward focus: witch) | 3 (heat); 7 (ward focus) | heat done (0.1.0-dev.3): StationProcessingGameTests, CompatibilityGameTests; ward focus M7 |
| Brewing cauldron | Liquid potions, throwables, animal brews, familiar tonics | cauldron | `stations/cauldron/` | cauldron brewing | yes (magical brewing) | 7 | not started |
| Infusion pot | Slowly prepare salves, oils, repellents, lantern oils | infusion pot | `stations/everyday/infusion_pot/` | infusion | no | 3 | done (0.1.0-dev.3): StationProcessingGameTests, PreparationUseGameTests |
| Distillation still | Refine rare essences and advanced extracts | still | `stations/still/` | distillation | yes (advanced brew) | 7 | not started |
| Apothecary bottling bench | Bottle, portion, label, package mixtures | bottling bench | `stations/bottling_bench/` | bottling | no (plan silent; assumed packaging only) | 7 | not started |
| Altar and ritual circle | Initiation, familiar binding, home blessings, revival, wards | altar (basic M6), ritual circle, decor Altar Table links here | `magic/ritual/` | rituals | yes (initiation: existing witch; founder ritual: survival) | 6 (altar); 7 (circle, rituals) | not started |
| Charm workbench | Sachets, amulets, animal capture charms, protection tokens | charm workbench, decor Work Table candidate | `stations/charm_workbench/` | charm work | assumed yes (magical); confirm at M7 | 7 | not started |
| Grimoire stand / witch bookshelf | Research herbs, recipes, rituals | grimoire stand, witch bookshelf | `grimoire/content/`, `grimoire/discovery/`, `grimoire/teaching/` | none (reads all recipes) | no | 4 | not started |
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
| Hearth | fuel, returned container | none (heat source) | burns fuel for its normal time | provides heat, LIT, light 13 | fuel from the sides; returns from the bottom | `B B` / `BCB` / `B B` (6 bricks, campfire) |
| Infusion pot | 2 ingredients, base liquid, package, result, return | infusion | 600 ticks | required | ingredients from above; base and package from the sides; result and return from the bottom | `IGI` / `B B` / `B B` (2 iron, glass bottle, 4 bricks) |

- **Water bottle:** the kettle accepts only the vanilla water potion (partial NBT match). The bottle
  becomes the drink and nothing is returned. The infusion pot returns the base's glass bottle.
- **Hearth:** burns any furnace fuel while loaded, no vessel needed; a lava bucket returns a bucket;
  no fire spread.
- **Heat:** a station needs a source (`herbalwitch:heat_sources`: hearth, campfire, soul campfire,
  magma block, optional Farmer's Delight stove) directly below, or one conductor
  (`herbalwitch:heat_conductors`: hopper, optional FD conductors) then a source. LIT must be true
  where the block has it; waterlogged gives none.
- **UI status lines:** Waiting, Needs heat, Working, Output full.
- **Recipes (50 processing):** drying 14 (one per herb); grinding 23 (14 bundles to powders, plant oil,
  fertilizer, flower dust, mushroom dust, four dyes, bone to 3 bone meal); steeping 7; infusion 6.
  Each is listed in docs/catalogue/products.md.
- **Reloads and saves:** a changed or removed recipe cancels the job and resets progress (items stay);
  saved jobs use schema 1; unloaded stations do not progress.

## Rules

- No universal machine superclass. Shared processing (`stations/process/`: reservations, job, completion) is used by composition; `StationBlock` and `StationMenu` share only block and menu boilerplate.
- Ordinary processing (drying, grinding, steeping, infusion) is open to non-witches and may be hopper-automated; magical jobs need an authorized witch nearby while progressing and at completion.
- Bottling is optional for teas: ordinary teas can finish in the kettle container. Bottling covers bulk portioning, packaging, labels and throwables.
- Prepared mixtures are bounded item data; there is no fluid system and no Create plumbing dependency.
- Recipe matching never consumes inputs; mutation happens separately, revalidated at completion (inputs, output capacity, witch, energy).
- Station inventories and progress live in the block entity; at most 12 source files per station directory, split into `block/`, `menu/`, `recipe/` when needed.
- Each station row closes only with implementation and acceptance evidence (milestone 12 qualification).
