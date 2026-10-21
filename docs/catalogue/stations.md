# Station catalogue

Traces each of the 16 station groups in the design ("Distinct functional workstations") to blocks, owning module, recipe type, witch requirement and milestone. Every station has its own purpose, a small recipe interface, limited slots, clear output and grimoire recipe lookup.

Module paths are relative to `io.github.dirtyredz.herbalwitch`. Milestones are from plan section 3.

| Station | Purpose (design) | Blocks it covers | Owning module | Recipe type | Witch-only? | Milestone | Evidence |
|---|---|---|---|---|---|---|---|
| Herb drying rack | Dry and preserve fresh herbs into bundles | drying rack | `stations/drying_rack/` | drying | no | 3 | not started |
| Mortar and pestle | Grind herbs and minerals into powders, pigments, blinding dust | mortar and pestle | `stations/mortar/` | grinding | no | 3 | not started |
| Tea kettle | Steep teas, tonics, simple infusions; requires heat | tea kettle | `stations/kettle/` | steeping | no | 3 | not started |
| Fireplace / hearth | Heat for kettle, cauldron, infusion pot; home anchor and ward focus | hearth block; decor Fireplace links here | `stations/hearth/` (heat adapter); ward focus in `magic/ward/` | heat source (tag) | no (ward focus: witch) | 3 (heat); 7 (ward focus) | not started |
| Brewing cauldron | Liquid potions, throwables, animal brews, familiar tonics | cauldron | `stations/cauldron/` | cauldron brewing | yes (magical brewing) | 7 | not started |
| Infusion pot | Slowly prepare salves, oils, repellents, lantern oils | infusion pot | `stations/infusion_pot/` | infusion | no | 3 | not started |
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

## Rules

- No universal machine superclass. Extract `stations/process/` helpers (inventory reservation, progress, completion) only after two implemented stations duplicate them.
- Ordinary processing (drying, grinding, steeping, infusion) is open to non-witches and may be hopper-automated; magical jobs need an authorized witch nearby while progressing and at completion.
- Bottling is optional for teas: ordinary teas can finish in the kettle container. Bottling covers bulk portioning, packaging, labels and throwables.
- Prepared mixtures are bounded item data; there is no fluid system and no Create plumbing dependency.
- Recipe matching never consumes inputs; mutation happens separately, revalidated at completion (inputs, output capacity, witch, energy).
- Station inventories and progress live in the block entity; at most 12 source files per station directory, split into `block/`, `menu/`, `recipe/` when needed.
- Each station row closes only with implementation and acceptance evidence (milestone 12 qualification).
