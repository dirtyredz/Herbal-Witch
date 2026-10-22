# Decor catalogue

The exact furniture catalogue as of 0.4.0 (Milestone 5): 94 canonical pieces in seven families. Block and
item ids are `herbalwitch:decor/<family>/<piece>`, one cottage design (no finish segment). The source of
truth is the family catalogues in `decor/catalog/pieces/` (Java) and the recipes in
`data/server/decor/recipes/`; this page was extracted from them, so if they
disagree the code wins. How it works: docs/ARCHITECTURE.md ("Decor"); why: docs/DECISIONS.md ("0.4.0").

| Family | Pieces | Wave split (0.1.0 / A / B / C) |
|---|---|---|
| seating | 13 | 1 / 2 / 7 / 3 |
| storage | 27 | 1 / 9 / 8 / 9 |
| surfaces | 15 | 1 / 6 / 7 / 1 |
| lighting | 8 | 1 / 0 / 0 / 7 |
| textiles | 6 | 1 / 0 / 0 / 5 |
| fixtures | 22 | 0 / 5 / 8 / 9 |
| openings | 3 | 0 / 0 / 0 / 3 |

## How to read the tables

- **Wave:** 0.1.0 is the original five pieces; A (kitchen, dining, apothecary, storage), B (living,
  bedroom, office, entryway) and C (bath, garden, lighting, shelves, textiles, openings) were added in
  0.4.0. Waves are catalogue metadata, not separate releases.
- **Size** is width x depth x height in cells. **Behaviour:** decorative (static), seating, cabinet
  (storage with an inventory), display (one item per slot), light (toggles, light 14), bed, door, rug.
- **Mounting** (support): floor, wall, ceiling, corner, carpet (on a floor like vanilla carpet) or free
  (no support requirement). Every supported cell is checked at placement and when neighbours change.
- **Seats / slots:** seats per piece; a cabinet's 27 or 54 slots; a display's 3, 6 or 9 single-item slots.
- **Crafting grid:** rows separated by `/`, a space is an empty cell, keys follow the colon. Each recipe
  makes one piece except the small wall shelf and small rug (two). "Any wool" and "any carpet" are the
  vanilla tags.
- **Blockbench project:** relative to `src/art/blockbench/decor/pieces/<family>/` for 0.4.0 pieces (a
  `<group>/<piece>/` folder holding `<piece>.bbmodel` and `piece.json`), or to `src/art/blockbench/` for the
  five 0.1.0 pieces (docs/BLOCKBENCH.md).
- **Status:** implemented and headless-verified (unit tests, GameTests on the ANU CI runner). "Owner art
  review pending" means the 0.4.0 art is an agent first draft and the owner's visual review has not
  happened (docs/ART.md).

## Aliases, lookalikes and drawers

- **Aliases are not separate pieces.** The concept sheet repeats some illustrations (Bench in four rooms,
  Mirror, Console Table, Small Table, Wall Shelf, Archway, Rugs); each maps to one id in the tables below.
  There are no finish variants.
- **Decorative drawers store nothing.** Drawers modelled on the writing desk, vanity table, curio cabinet
  (`tv_stand`) and similar pieces are art only; storage is the cabinet pieces. Plumbing (sink, tub, shower,
  commode, washstand), the icebox, mirror, planter box and wishing well simulate nothing.
- **Lookalikes are not stations.** The work table, altar table, decorative drying rack, kitchen range,
  fireplace and firepit are furniture; none works as the station it resembles (docs/catalogue/stations.md,
  "Decor lookalikes"). Their grimoire pages link the working station. The altar table stays decorative
  in 0.5.0 (unchanged id, recipe and behaviour); only its grimoire page now links the functional Ritual
  Altar (`magic/ritual/altar`, docs/catalogue/stations.md), which is a separate block, not a furniture piece.
- **The bookshelf stays two pieces.** `decor/storage/bookshelf` is a nine-slot display; the grimoire's
  `grimoire/bookshelf` is a storage-free research block with its own id, block and model.
- **Names differ from ids in places:** `tv_stand` is the Curio Cabinet, `fridge` the Icebox,
  `stove_oven` the Kitchen Range, `toilet` the Commode, `sink_vanity` the Washstand, `decorative_well` the
  Wishing Well, `room_divider` the Folding Screen, `vanity_chair` the Vanity Stool, `outdoor_chair` the
  Garden Chair, `bed` the Cottage Bed. Ids are permanent.

## The catalogue

### Seating

| Id | Name | Wave | Size w x d x h | Behaviour | Mounting | Seats / slots | Crafting grid | Blockbench project | Status |
|---|---|---|---|---|---|---|---|---|---|
| `decor/seating/armchair` | Armchair | 0.1.0 | 1 x 1 x 1 | seating | free | seats 1 | `PWP/PPP/S S`: P dark oak planks, W any wool, S stick | `decor/armchair` | implemented (0.1.0) |
| `decor/seating/dining_chair` | Dining Chair | A | 1 x 1 x 1 | seating | floor | seats 1 | `P  /PWP/T T`: P dark oak planks, W any wool, T stick | `chairs/dining_chair` | implemented (0.4.0), owner art review pending |
| `decor/seating/bench` | Bench | A | 2 x 1 x 1 | seating | floor | seats 2 | `P P/WWW/T T`: P dark oak planks, W any wool, T stick | `benches/bench` | implemented (0.4.0), owner art review pending |
| `decor/seating/sofa` | Sofa | B | 3 x 1 x 1 | seating | floor | seats 3 | `WWW/WWW/PPP`: P dark oak planks, W any wool | `lounges/sofa` | implemented (0.4.0), owner art review pending |
| `decor/seating/loveseat` | Loveseat | B | 2 x 1 x 1 | seating | floor | seats 2 | `W W/WWW/PPP`: P dark oak planks, W any wool | `lounges/loveseat` | implemented (0.4.0), owner art review pending |
| `decor/seating/chaise_lounge` | Chaise Lounge | B | 1 x 2 x 1 | seating | floor | seats 1 | `WW /WWW/PTP`: P dark oak planks, W any wool, T stick | `lounges/chaise_lounge` | implemented (0.4.0), owner art review pending |
| `decor/seating/vanity_chair` | Vanity Stool | B | 1 x 1 x 1 | seating | floor | seats 1 | ` W /PWP/T T`: P dark oak planks, W any wool, T stick | `chairs/vanity_chair` | implemented (0.4.0), owner art review pending |
| `decor/seating/desk_chair` | Desk Chair | B | 1 x 1 x 1 | seating | floor | seats 1 | `P  /PWP/ I `: P dark oak planks, W any wool, I iron ingot | `chairs/desk_chair` | implemented (0.4.0), owner art review pending |
| `decor/seating/reading_chair` | Reading Chair | B | 1 x 1 x 1 | seating | floor | seats 1 | `WPW/WWW/T T`: P dark oak planks, W any wool, T stick | `chairs/reading_chair` | implemented (0.4.0), owner art review pending |
| `decor/seating/entry_bench` | Entry Bench | B | 2 x 1 x 1 | seating | floor | seats 2 | `TGT/SSS/T T`: S dark oak slab, G gold nugget, T stick | `benches/entry_bench` | implemented (0.4.0), owner art review pending |
| `decor/seating/garden_bench` | Garden Bench | C | 2 x 1 x 1 | seating | floor | seats 2 | `SSS/I I`: S dark oak slab, I iron ingot | `benches/garden_bench` | implemented (0.4.0), owner art review pending |
| `decor/seating/outdoor_chair` | Garden Chair | C | 1 x 1 x 1 | seating | floor | seats 1 | `S  /SSS/I I`: S dark oak slab, I iron ingot | `chairs/outdoor_chair` | implemented (0.4.0), owner art review pending |
| `decor/seating/garden_swing` | Garden Swing | C | 3 x 2 x 3 | seating | floor | seats 2 | `PCP/C C/SWS`: P dark oak planks, C chain, S dark oak slab, W any wool | `benches/garden_swing` | implemented (0.4.0), owner art review pending |

### Storage

| Id | Name | Wave | Size w x d x h | Behaviour | Mounting | Seats / slots | Crafting grid | Blockbench project | Status |
|---|---|---|---|---|---|---|---|---|---|
| `decor/storage/small_wall_shelf` | Small Wall Shelf | 0.1.0 | 1 x 1 x 1 | display | wall | 3 slots | `SSS/T T` (makes 2): S dark oak slab, T stick | `decor/wall_shelf` | implemented (0.1.0) |
| `decor/storage/lower_cabinet` | Lower Cabinet | A | 1 x 1 x 1 | cabinet | floor | 27 slots | `SSS/PHP/PPP`: S dark oak slab, P dark oak planks, H chest | `cabinets/lower_cabinet` | implemented (0.4.0), owner art review pending |
| `decor/storage/upper_cabinet` | Upper Cabinet | A | 1 x 1 x 1 | cabinet | wall | 27 slots | `PPP/PHP/T T`: P dark oak planks, H chest, T stick | `cabinets/upper_cabinet` | implemented (0.4.0), owner art review pending |
| `decor/storage/pantry` | Pantry | A | 1 x 1 x 2 | cabinet | floor | 27 slots | `PSP/PHP/PSP`: S dark oak slab, P dark oak planks, H chest | `cabinets/pantry` | implemented (0.4.0), owner art review pending |
| `decor/storage/sideboard` | Sideboard | A | 2 x 1 x 1 | cabinet | floor | 54 slots | `SSS/PHP/PHP`: S dark oak slab, P dark oak planks, H chest | `cabinets/sideboard` | implemented (0.4.0), owner art review pending |
| `decor/storage/china_cabinet` | China Cabinet | A | 2 x 1 x 2 | cabinet | floor | 54 slots | `PGP/HGH/PPP`: P dark oak planks, G glass, H chest | `cabinets/china_cabinet` | implemented (0.4.0), owner art review pending |
| `decor/storage/wine_rack` | Wine Rack | A | 1 x 1 x 2 | cabinet | floor | 27 slots | `PBP/BHB/PBP`: P dark oak planks, B glass bottle, H chest | `cabinets/wine_rack` | implemented (0.4.0), owner art review pending |
| `decor/storage/ingredient_cabinet` | Ingredient Cabinet | A | 1 x 1 x 2 | cabinet | floor | 27 slots | `GPG/PHP/GPG`: P dark oak planks, G gold nugget, H chest | `cabinets/ingredient_cabinet` | implemented (0.4.0), owner art review pending |
| `decor/storage/open_shelves` | Open Shelves | A | 1 x 1 x 2 | display | floor | 9 slots | `TST/TST/TST`: S dark oak slab, T stick | `shelves/open_shelves` | implemented (0.4.0), owner art review pending |
| `decor/storage/potion_shelf` | Potion Shelf | A | 1 x 1 x 2 | display | floor | 9 slots | `TST/TBT/TST`: S dark oak slab, T stick, B glass bottle | `shelves/potion_shelf` | implemented (0.4.0), owner art review pending |
| `decor/storage/nightstand` | Nightstand | B | 1 x 1 x 1 | cabinet | floor | 27 slots | ` S /PHP/T T`: S dark oak slab, P dark oak planks, H chest, T stick | `bedroom/nightstand` | implemented (0.4.0), owner art review pending |
| `decor/storage/dresser` | Dresser | B | 2 x 1 x 1 | cabinet | floor | 54 slots | `SSS/HGH/PPP`: S dark oak slab, P dark oak planks, H chest, G gold nugget | `bedroom/dresser` | implemented (0.4.0), owner art review pending |
| `decor/storage/wardrobe` | Wardrobe | B | 2 x 1 x 2 | cabinet | floor | 54 slots | `PPP/HPH/PGP`: P dark oak planks, H chest, G gold nugget | `bedroom/wardrobe` | implemented (0.4.0), owner art review pending |
| `decor/storage/trunk` | Trunk | B | 2 x 1 x 1 | cabinet | floor | 54 slots | `NPN/HGH/PPP`: N iron nugget, P dark oak planks, H chest, G gold nugget | `bedroom/trunk` | implemented (0.4.0), owner art review pending |
| `decor/storage/rolltop_desk` | Rolltop Desk | B | 2 x 1 x 2 | cabinet | floor | 54 slots | `PPP/HBH/PSP`: P dark oak planks, H chest, B book, S dark oak slab | `study/rolltop_desk` | implemented (0.4.0), owner art review pending |
| `decor/storage/filing_cabinet` | Filing Cabinet | B | 1 x 1 x 2 | cabinet | floor | 27 slots | `PNP/PHP/PNP`: P dark oak planks, N iron nugget, H chest | `study/filing_cabinet` | implemented (0.4.0), owner art review pending |
| `decor/storage/shoe_cabinet` | Shoe Cabinet | B | 1 x 1 x 1 | cabinet | floor | 27 slots | `SSS/PHP`: S dark oak slab, P dark oak planks, H chest | `bedroom/shoe_cabinet` | implemented (0.4.0), owner art review pending |
| `decor/storage/bookshelf` | Bookshelf | B | 1 x 1 x 2 | display | floor | 9 slots | `PBP/S S/PBP`: P dark oak planks, B book, S dark oak slab | `study/bookshelf` | implemented (0.4.0), owner art review pending |
| `decor/storage/linen_cabinet` | Linen Cabinet | C | 1 x 1 x 2 | cabinet | floor | 27 slots | `PWP/PHP/PWP`: P dark oak planks, W any wool, H chest | `bedroom/linen_cabinet` | implemented (0.4.0), owner art review pending |
| `decor/storage/storage_cabinet` | Storage Cabinet | C | 2 x 1 x 2 | cabinet | floor | 54 slots | `PHP/PPP/PHP`: P dark oak planks, H chest | `study/storage_cabinet` | implemented (0.4.0), owner art review pending |
| `decor/storage/wall_shelf_medium` | Medium Wall Shelf | C | 2 x 1 x 1 | display | wall | 6 slots | `SSS/SSS/T T`: S dark oak slab, T stick | `shelves/wall_shelf_medium` | implemented (0.4.0), owner art review pending |
| `decor/storage/wall_shelf_large` | Large Wall Shelf | C | 2 x 1 x 2 | display | wall | 9 slots | `SSS/T T/SSS`: S dark oak slab, T stick | `shelves/wall_shelf_large` | implemented (0.4.0), owner art review pending |
| `decor/storage/corner_shelf` | Corner Shelf | C | 1 x 1 x 2 | display | corner | 6 slots | `S  /SS /SSS`: S dark oak slab | `shelves/corner_shelf` | implemented (0.4.0), owner art review pending |
| `decor/storage/free_standing_shelf_short` | Short Standing Shelf | C | 1 x 1 x 1 | display | floor | 6 slots | `T T/SSS/T T`: S dark oak slab, T stick | `shelves/free_standing_shelf_short` | implemented (0.4.0), owner art review pending |
| `decor/storage/free_standing_shelf_medium` | Standing Shelf | C | 1 x 1 x 2 | display | floor | 9 slots | `TST/T T/TST`: S dark oak slab, T stick | `shelves/free_standing_shelf_medium` | implemented (0.4.0), owner art review pending |
| `decor/storage/free_standing_shelf_large` | Tall Standing Shelf | C | 1 x 1 x 3 | display | floor | 9 slots | `TST/TST/T T`: S dark oak slab, T stick | `shelves/free_standing_shelf_large` | implemented (0.4.0), owner art review pending |
| `decor/storage/ladder_shelf` | Ladder Shelf | C | 1 x 1 x 2 | display | floor | 9 slots | `TS /TS /TSS`: S dark oak slab, T stick | `shelves/ladder_shelf` | implemented (0.4.0), owner art review pending |

### Surfaces

| Id | Name | Wave | Size w x d x h | Behaviour | Mounting | Seats / slots | Crafting grid | Blockbench project | Status |
|---|---|---|---|---|---|---|---|---|---|
| `decor/surfaces/apothecary_counter` | Apothecary Counter | 0.1.0 | 1 x 1 x 1 | decorative | free | - | `SSS/PNP/PPP`: S dark oak slab, P dark oak planks, N gold nugget | `decor/apothecary_counter` | implemented (0.1.0) |
| `decor/surfaces/kitchen_counter` | Kitchen Counter | A | 1 x 1 x 1 | decorative | floor | - | `XXX/PSP/PPP`: X smooth stone slab, P dark oak planks, S dark oak slab | `kitchen/kitchen_counter` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/corner_counter` | Corner Counter | A | 1 x 1 x 1 | decorative | floor | - | `XXX/PPS/PPP`: X smooth stone slab, P dark oak planks, S dark oak slab | `kitchen/corner_counter` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/kitchen_island` | Kitchen Island | A | 2 x 1 x 1 | decorative | floor | - | `LLL/T T/S S`: L dark oak log, T stick, S dark oak slab | `kitchen/kitchen_island` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/dining_table` | Dining Table | A | 3 x 2 x 1 | decorative | floor | - | `SSS/PCP/T T`: S dark oak slab, P dark oak planks, C any carpet, T stick | `tables/dining_table` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/serving_cart` | Serving Cart | A | 1 x 1 x 1 | decorative | floor | - | `NSN/NSN/I I`: N gold nugget, S dark oak slab, I iron ingot | `kitchen/serving_cart` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/work_table` | Work Table | A | 2 x 1 x 1 | decorative | floor | - | `SSS/PIP/P P`: S dark oak slab, P dark oak planks, I iron ingot | `desks/work_table` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/coffee_table` | Coffee Table | B | 2 x 1 x 1 | decorative | floor | - | `SCS/T T`: S dark oak slab, C any carpet, T stick | `tables/coffee_table` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/side_table` | Side Table | B | 1 x 1 x 1 | decorative | floor | - | ` S / T /TTT`: S dark oak slab, T stick | `tables/side_table` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/console_table` | Console Table | B | 2 x 1 x 1 | decorative | floor | - | `SSS/T T/T T`: S dark oak slab, T stick | `tables/console_table` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/tv_stand` | Curio Cabinet | B | 2 x 1 x 1 | decorative | floor | - | `SSS/GPG/P P`: S dark oak slab, G glass pane, P dark oak planks | `desks/tv_stand` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/vanity_table` | Vanity Table | B | 2 x 1 x 2 | decorative | floor | - | `PGP/SSS/P P`: P dark oak planks, G glass, S dark oak slab | `desks/vanity_table` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/writing_desk` | Writing Desk | B | 2 x 1 x 1 | decorative | floor | - | `SFS/PSP/P P`: S dark oak slab, F feather, P dark oak planks | `desks/writing_desk` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/map_table` | Map Table | B | 2 x 2 x 1 | decorative | floor | - | `SMS/PSP/P P`: S dark oak slab, M map, P dark oak planks | `tables/map_table` | implemented (0.4.0), owner art review pending |
| `decor/surfaces/garden_table` | Garden Table | C | 1 x 1 x 1 | decorative | floor | - | `SSS/NIN/ I `: S dark oak slab, N iron nugget, I iron ingot | `tables/garden_table` | implemented (0.4.0), owner art review pending |

### Lighting

| Id | Name | Wave | Size w x d x h | Behaviour | Mounting | Seats / slots | Crafting grid | Blockbench project | Status |
|---|---|---|---|---|---|---|---|---|---|
| `decor/lighting/table_lamp` | Table Lamp | 0.1.0 | 1 x 1 x 1 | light | floor | - | column `W/T/S`: W any wool, T torch, S dark oak slab | `decor/table_lamp` | implemented (0.1.0) |
| `decor/lighting/wall_sconce` | Wall Sconce | C | 1 x 1 x 1 | light | wall | - | ` R /NPN`: R torch, N gold nugget, P dark oak planks | `lamps/wall_sconce` | implemented (0.4.0), owner art review pending |
| `decor/lighting/hanging_lantern` | Hanging Lantern | C | 1 x 1 x 1 | light | ceiling | - | `GCG/ L `: G gold nugget, C chain, L lantern | `lamps/hanging_lantern` | implemented (0.4.0), owner art review pending |
| `decor/lighting/chandelier` | Chandelier | C | 2 x 2 x 1 | light | ceiling | - | `GCG/KKK`: G gold ingot, C chain, K candle | `lamps/chandelier` | implemented (0.4.0), owner art review pending |
| `decor/lighting/ceiling_light` | Ceiling Light | C | 1 x 1 x 1 | light | ceiling | - | ` C /GLG`: C chain, G gold nugget, L lantern | `lamps/ceiling_light` | implemented (0.4.0), owner art review pending |
| `decor/lighting/floor_lamp` | Floor Lamp | C | 1 x 1 x 2 | light | floor | - | `WRW/ T / S `: W any wool, R torch, T stick, S dark oak slab | `lamps/floor_lamp` | implemented (0.4.0), owner art review pending |
| `decor/lighting/post_lantern` | Post Lantern | C | 1 x 1 x 3 | light | floor | - | ` L / T /SSS`: L lantern, T stick, S dark oak slab | `lamps/post_lantern` | implemented (0.4.0), owner art review pending |
| `decor/lighting/pendant_light` | Pendant Light | C | 1 x 1 x 2 | light | ceiling | - | ` C / C / L `: C chain, L lantern | `lamps/pendant_light` | implemented (0.4.0), owner art review pending |

### Textiles

| Id | Name | Wave | Size w x d x h | Behaviour | Mounting | Seats / slots | Crafting grid | Blockbench project | Status |
|---|---|---|---|---|---|---|---|---|---|
| `decor/textiles/small_rug` | Small Rug | 0.1.0 | 1 x 1 x 1 | rug | carpet | - | `CSC` (makes 2): C any carpet, S string | `decor/rug` | implemented (0.1.0) |
| `decor/textiles/medium_rug` | Medium Rug | C | 2 x 2 x 1 | rug | carpet | - | `CCC/CSC`: C any carpet, S string | `rugs/medium_rug` | implemented (0.4.0), owner art review pending |
| `decor/textiles/large_rug` | Large Rug | C | 3 x 3 x 1 | rug | carpet | - | `CCC/CSC/CCC`: C any carpet, S string | `rugs/large_rug` | implemented (0.4.0), owner art review pending |
| `decor/textiles/curtain` | Curtain | C | 2 x 1 x 2 | decorative | ceiling | - | `TTT/W W/W W`: T stick, W any wool | `drapes/curtain` | implemented (0.4.0), owner art review pending |
| `decor/textiles/room_divider` | Folding Screen | C | 3 x 1 x 2 | decorative | floor | - | `PWP/PWP/T T`: P dark oak planks, W any wool, T stick | `drapes/room_divider` | implemented (0.4.0), owner art review pending |
| `decor/textiles/hanging_divider` | Hanging Divider | C | 3 x 1 x 2 | decorative | ceiling | - | `CCC/W W/W W`: C chain, W any wool | `drapes/hanging_divider` | implemented (0.4.0), owner art review pending |

### Fixtures

| Id | Name | Wave | Size w x d x h | Behaviour | Mounting | Seats / slots | Crafting grid | Blockbench project | Status |
|---|---|---|---|---|---|---|---|---|---|
| `decor/fixtures/kitchen_sink` | Kitchen Sink | A | 1 x 1 x 1 | decorative | floor | - | `CBC/PPP/PPP`: C copper ingot, B bucket, P dark oak planks | `kitchen/kitchen_sink` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/stove_oven` | Kitchen Range | A | 1 x 1 x 1 | decorative | floor | - | `NIN/IFI/III`: N gold nugget, I iron ingot, F furnace | `kitchen/stove_oven` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/fridge` | Icebox | A | 1 x 1 x 2 | decorative | floor | - | `PIP/PKP/PIP`: P dark oak planks, I iron ingot, K packed ice | `kitchen/fridge` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/drying_rack_decor` | Decorative Drying Rack | A | 2 x 1 x 2 | decorative | floor | - | `TRT/TRT/S S`: T stick, R string, S dark oak slab | `apothecary/drying_rack_decor` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/altar_table` | Altar Table | A | 2 x 1 x 1 | decorative | floor | - | `CGC/SSS/P P`: C candle, G gold ingot, S dark oak slab, P dark oak planks | `apothecary/altar_table` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/bed` | Cottage Bed | B | 1 x 2 x 1 | bed | free | sleeps 1 | `T T/PBP`: T stick, P dark oak planks, B any bed | `bedroom/bed` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/canopy_bed` | Canopy Bed | B | 2 x 2 x 3 | bed | free | sleeps 1 | `TWT/T T/TBT`: T stick, W any wool, B any bed | `bedroom/canopy_bed` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/fireplace` | Fireplace | B | 2 x 1 x 2 | decorative | floor | - | `BBB/B B/BLB`: B bricks, L dark oak log | `hearth/fireplace` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/mirror` | Mirror | B | 1 x 1 x 2 | decorative | wall | - | `PGP/PIP/PGP`: P dark oak planks, G glass, I iron ingot | `hall/mirror` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/globe_stand` | Globe Stand | B | 1 x 1 x 1 | decorative | floor | - | `GCG/ T /SSS`: G gold nugget, C compass, T stick, S dark oak slab | `hall/globe_stand` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/coat_rack` | Coat Rack | B | 1 x 1 x 2 | decorative | floor | - | `TTT/ T / S `: T stick, S dark oak slab | `hall/coat_rack` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/key_hooks` | Key Hooks | B | 1 x 1 x 1 | decorative | wall | - | `NNN/SSS`: N iron nugget, S dark oak slab | `hall/key_hooks` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/umbrella_stand` | Umbrella Stand | B | 1 x 1 x 1 | decorative | floor | - | `C C/C C/CCC`: C copper ingot | `hall/umbrella_stand` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/clawfoot_tub` | Clawfoot Tub | C | 1 x 2 x 1 | decorative | floor | - | `Q Q/QQQ/G G`: Q quartz, G gold nugget | `bathroom/clawfoot_tub` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/shower_stall` | Shower Stall | C | 1 x 1 x 2 | decorative | floor | - | `IBI/G G/QQQ`: I iron ingot, B bucket, G glass pane, Q quartz | `bathroom/shower_stall` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/toilet` | Commode | C | 1 x 1 x 1 | decorative | floor | - | `P  /QQQ/ Q `: P dark oak planks, Q quartz | `bathroom/toilet` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/sink_vanity` | Washstand | C | 1 x 1 x 1 | decorative | floor | - | `QBQ/PPP/P P`: Q quartz, B bucket, P dark oak planks | `bathroom/sink_vanity` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/towel_rack` | Towel Rack | C | 1 x 1 x 1 | decorative | wall | - | `NWN`: N iron nugget, W any wool | `bathroom/towel_rack` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/planter_box` | Planter Box | C | 2 x 1 x 1 | decorative | floor | - | `PDP/PPP`: P dark oak planks, D dirt | `garden/planter_box` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/trellis` | Trellis | C | 1 x 1 x 2 | decorative | floor | - | `T T/ T /T T`: T stick | `garden/trellis` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/decorative_well` | Wishing Well | C | 2 x 2 x 3 | decorative | floor | - | `SSS/T T/XBX`: S dark oak slab, T stick, X cobblestone, B bucket | `garden/decorative_well` | implemented (0.4.0), owner art review pending |
| `decor/fixtures/firepit` | Firepit | C | 2 x 2 x 1 | light | floor | - | ` C /XXX`: C campfire, X cobblestone | `hearth/firepit` | implemented (0.4.0), owner art review pending |

### Openings

| Id | Name | Wave | Size w x d x h | Behaviour | Mounting | Seats / slots | Crafting grid | Blockbench project | Status |
|---|---|---|---|---|---|---|---|---|---|
| `decor/openings/door` | Cottage Door | C | 1 x 1 x 2 | door | floor | - | `D/G`: D dark oak door, G gold nugget | `frames/door` | implemented (0.4.0), owner art review pending |
| `decor/openings/window` | Cottage Window | C | 1 x 1 x 2 | decorative | wall | - | `PGP/GTG/PGP`: P dark oak planks, G glass pane, T stick | `frames/window` | implemented (0.4.0), owner art review pending |
| `decor/openings/archway` | Archway | C | 3 x 1 x 3 (open centre) | decorative | floor | - | `XXX/X X/X X`: X stone bricks | `frames/archway` | implemented (0.4.0), owner art review pending |

## Design

There is one cottage design (2026-10-21), restyled in 0.2.0-dev.1 (the art pass; owner review
pending; docs/ART.md): the armchair's dark brown wood throughout (since 0.2.0-dev.2), forest green,
magenta and plum, gold bands, rivets and studs, ivy with small pink flowers. It replaces the former 18
finishes; `FinishCatalog` keeps a single entry so a design can return later. The 89 pieces added in 0.4.0
use the same palette as agent first drafts. Every piece is crafted directly from dark oak and common
vanilla inputs (the grids above).

### Look (0.2.0-dev.1)

History: the look of the five pieces that existed at the time.

- **Armchair:** dark brown frame with chunky capped posts and magenta inlays, gold and plum bands;
  forest-green velvet seat and tall back with magenta tufting studs and piping; draped arm and head
  runners with pointed hems; dense trailing ivy with small pink flowers.
- **Apothecary Counter:** apothecary chest in the dark house wood with black iron corner brackets and gold
  rivets; two deep-green drawer fronts in gold frames with chunky gold knobs; gold studs along the top
  rail; a magenta runner draped over the top and down the front; a dried bundle on a gold ring; ivy.
- **Small Wall Shelf:** iron brackets with gold rivets, a gold edge, richer ivy. It carried modelled
  bottles until 0.4.0, when it became a display and the bottles were removed (docs/ART.md).
- **Table Lamp:** faceted magenta-and-plum shade with gold bands on a dark stem with gold and plum
  bands; the lit shade glows.
- **Small Rug:** dark green woven field, magenta and plum woven border with a gold line, a gold moon
  with stars and small pink flowers at its heart, gold tassels at each end.

## Related, not part of the furniture families

- The **functional hearth** and **drying rack** (stations, Milestone 3; both 2x2 multiblocks) are working
  blocks. The decor Fireplace, Firepit and Decorative Drying Rack are separate furniture pieces whose
  grimoire pages link to them (docs/catalogue/stations.md).
- **Herb beds** (botany) and the **oil lantern** (preparations) are not furniture.
- **Witch's Bookshelf (grimoire, Milestone 4, 0.3.0)** is a research block, not furniture: id
  `herbalwitch:grimoire/bookshelf`, no block entity and no storage, opens the grimoire index and researches
  recipes (docs/catalogue/stations.md). `decor/storage/bookshelf` is a separate storage/display piece with
  its own id; the two never share an id, a block or a model.

## Counting

The concept sheet (13 room/category groups) is not an exact manifest of ~120 unique models. Repeated
illustrations map to one piece id unless the form differs, which gives 94 pieces: the five 0.1.0 pieces
plus 89 added in 0.4.0 (wave A 22, wave B 30, wave C 37).
