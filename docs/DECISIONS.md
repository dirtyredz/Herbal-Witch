# Decisions - The Herbal Witch

Newest first.

## 2026-10-21 - Art pass 0.2.0: an independent house style for stations (supersedes "fixed station appearances")

Stations no longer borrow the furniture's fabric colours (the pink and green ceramic stripes): they
are warm wood, charcoal iron, stone and plum glaze with gold hardware and moon/star/flower motifs,
matching the owner's reference pieces (docs/ART.md). Furniture keeps the single COTTAGE design,
restyled in the same hardware language. Rejected: retinting the old stations.

## 2026-10-21 - Cuboid models are generated from code (tools/generate-models.mjs)

Station, furniture, herb and herb-bed models went from about 20 hand-placed elements to 40-80 with
rounded octagonal layers, rotations and multiblock cells. They are now written by a deterministic
Node generator: one place per piece, position UVs that keep textures continuous across elements
and cells, decal UVs for stretched sprites, and automatic cell splitting. The JSON stays checked
in under `src/main/resources` (like the generated textures) and passes the same template tests.
Rejected: hand-editing 80-element JSON; Java datagen for geometry (verbose, and models would split
between two pipelines). The bundle and lantern templates stay hand-authored.

## 2026-10-21 - Headless model previews gate art changes

`tools/preview-models.mjs` renders every state, item and review scene (CPU rasterizer, directional
shading, no client) and compares owner references side by side; it fails on missing textures or
unrendered models. Art changes are reviewed on its sheets before a jar goes to the trial instance.
It does not model ambient occlusion, lighting, shaders or animation; the owner still checks those.

## 2026-10-21 - Multiblock rack, hearth and witch's stove; one origin cell

The owner asked for a bigger rack and gave 2-wide references for the rack, hearth and a cooking
stove. The rack and hearth are 2 x 2, the stove 2 x 1 (`platform/multiblock`). Only cell 0 holds
the block entity, ticks and drops the item; any cell opens it; breaking any cell removes all (one
item, contents once, nothing in creative); lit/active is copied to every cell so overlays and heat
work everywhere. Block entities, menus and recipes are unchanged; the stove reuses the hearth's
block entity type, menu and screen (same fuel rules). A vessel heats on the hearth's chimney top;
the stove's burners are the intended cooking surface. Tradeoff: a dev.5 world's single-block rack or
hearth stays a lone origin cell until replaced. Rejected: a deeper single-block rack (cannot read
as bigger in one block), a 1-block hearth with a decorative surround (no room for the chimney
breast).

## 2026-10-21 - Rack bundles show occupancy, not the herb

Four fixed bundles (green, lavender, white, yellow) hang while the rack holds anything. Showing the
actual herb would need a block entity renderer or a state per herb. Rejected for this pass.

## 2026-10-21 - Joined herb beds: four side properties, multipart, no diagonal state

Beds join where the same bed stands beside them at the same height; corners follow from the two
sides that meet. Parts are authored for one side and corner and turned by the blockstate. Beds
connect on placement (including a plain setBlock), on neighbour changes, and old beds are repaired
by their random tick. Tradeoff: an inner corner around an empty diagonal shows the two rails meeting
(closed by the corner part's wooden sides) instead of a post; no chunk-load migration.

## 2026-10-21 - Herbs rooted 2 px into the ground; mature herbs are 3D

Young ages use a planted cross whose roots are two pixels below the block, matching the bed's soil
surface (14) and sitting one pixel into farmland and two into full ground; the plant needs no
state for what it stands on. Mature herbs are cube models over a swatch atlas (the owner's
reference), so their stage sprites were removed. Rejected: per-support plant states; a taller bed
soil (the frame would hide less of the plant).

## 2026-10-21 - Showcase command: plan, preflight, build; refuses occupied plots

`/herbalwitch showcase` needs op level 2, builds a pure bounded plan, runs a preflight, and only then
writes. It clears the box above the floor quietly and has no undo, so it refuses a plot holding block
entities (a built showcase) rather than guess. Rejected: auto-offset to a free plot, undo snapshots.

## 2026-10-21 - Apothecary creative tab is owned by stations

Stations depend on preparations, never the reverse, so the tab that lists both lives in `stations/`.

## 2026-10-21 - Thirst by reflection, Farmer's Delight by tags, EMI/JEI compile-only

The Thirst adapter looks up two named members (its event class and `addDrink`) with no compile
dependency; Thirst 1.4.0's `addDrink` is a no-op, so drinks register through its value event.
Farmer's Delight needs tags only. EMI and JEI APIs come from maven as compile-only and their plugin
classes load only with the mod. No foreign classes ship in the jar.

## 2026-10-21 - Energy tea and restful infusion: witch-energy benefits staged to M6

Both ship now as ordinary items (food value, cooldown, rest-statistic reset). The +25 and +30 energy
effects wait for the energy system in Milestone 6.

## 2026-10-21 - Fixed station appearances; a finite oil lantern consumes lantern oil

Each station has one authored look (superseded for appearance by the 0.2.0 house style above). The oil lantern (one dose,
6,000 loaded ticks) gives lantern oil a consumer.

## 2026-10-21 - Stations grouped under `stations/everyday/`

The directory breadth cap would be exceeded by five stations plus shared code. Magical stations get
their own group later.

## 2026-10-21 - Composition over a machine superclass; small shared bases

`ProcessStation`, `ProcessJob` and `ProcessInventory` are composed by each station. Only block and
menu boilerplate is shared as `StationBlock`/`StationMenu`. Rejected: a universal machine superclass.

## 2026-10-21 - In-slot reservations, exact matching, explicit returns, no catch-up, recipe change cancels

Ingredients stay in their slots and are reserved there; every filled input slot must be used by the
recipe; returned containers come only from the recipe's `returns`; unloaded stations do not
progress; a changed or removed recipe cancels the job and resets progress while items stay.

## 2026-10-21 - Herbs survive without light

Worldgen has no light data when it places patches, so herbs check soil only. Growth still needs light.

## 2026-10-21 - Accessible starter seeds and grass foraging

Wheat seeds plus a companion plant craft the four starter seeds, and breaking grass can drop them, so
any world can start without a lucky find.

## 2026-10-21 - Grouped permanent herb ids; habitat preferences in data

Herb ids are `botany/<group>/<herb>/...` with five permanent groups, which keeps every generated
directory under the cap. Liked conditions and discovery chances are data, retunable without renames.

## 2026-10-21 - One cottage design replaces 18 finishes

The owner rejected every colour option (2026-10-21: "All of the colour options are terrible."). The mod
now has one dark-wood design with deep green and hot pink accents; `FinishCatalog` keeps a single
entry so designs can return. Rejected: keeping the finishes, tint layers.

## 2026-10-20 - Lit lamp models live beside the palette directories

Superseded 2026-10-21 by One cottage design replaces 18 finishes (no palette directories remain).

`decor/lighting/table_lamp/lit/<palette>/<finish>` rather than `.../<palette>/lit/<finish>`: the
painted directory already holds 11 finish models, and a `lit/` entry would make 12. Rejected:
`<finish>_lit` siblings (doubles the leaf directory).

## 2026-10-20 - Coordinated tints over per-finish textures

Superseded 2026-10-21 by One cottage design replaces 18 finishes (decor is untinted).

Frame and fabric textures are neutral greys tinted per finish (tint index 0 frame, 1 fabric);
brass, glass and tinctures are untinted. Motifs therefore read tone-on-tone. Nine textures serve 90
blocks and upholstery can later become independently selectable without new art. Rejected: 18
pre-coloured fabric textures (multi-colour florals; ties fabric to frame forever). Revisit if the
owner wants multi-colour motifs (docs/ART.md).

## 2026-10-20 - Stonecutter restyling, crafting only the dark oak base

Superseded 2026-10-21 by One cottage design replaces 18 finishes (nothing left to restyle).

Five ordinary recipes make the dark oak base; the stonecutter converts any finish of a piece into any
other finish of the same piece, 1:1, free. Each restyle recipe lists the other 17 finishes, so no
recipe maps an item to itself and none changes the piece. Rejected: 18 stain items, fictional wood
species (walnut, rosewood and pale oak are appearances, not trees), per-finish crafting (90 recipes
of noise). An intentional initial simplification.

## 2026-10-20 - Ids are `decor/<family>/<piece>/<palette>/<finish>`

Superseded 2026-10-21 by One cottage design replaces 18 finishes (ids are now `decor/<family>/<piece>`).

Registry ids carry slashes so every generated directory (blockstates, models, loot, recipes,
advancements) stays bounded below 12 entries as the catalogue grows to hundreds of pieces. Room
categories are catalogue metadata, not id segments. Ids are permanent once released.

## 2026-10-20 - One registered block per piece and finish, baked models

Plain blocks and items give stacks, drops, recipes, pick-block and viewer indexing for free, at the
cost of many small registry and model entries. Rejected: appearance NBT or a block entity with a
custom model loader. Block entity renderers are reserved for visible stored items, moving parts and
station contents.

## 2026-10-20 - 18 coordinated finishes, not a cross-product

Superseded 2026-10-21 by One cottage design replaces 18 finishes.

Each finish = one frame material and colour + one fabric colour, following the concept sheet's 18
swatches (7 natural, 11 painted/material). Rejected: 18 woods x 18 upholsteries (324 per piece).
Frame and fabric stay separate layers, so independent upholstery remains possible later.

## 2026-10-20 - Standalone; no RedzCore dependency

Similar-looking registration and networking scaffolding is not enough reason for a shared library.
Patterns from Roundwood (build, datagen, generators, GameTest fixtures) and DankRecipeTransfer
(request validation) are adapted locally. Revisit only when two mods need the same maintained code.

## 2026-10-20 - Integrated decor, staged development, one complete release

Witchcraft and the cottage furniture ship as one mod. Development runs through private milestones
(docs/FEATURES.md); the first public release, if any, contains every agreed system. Release timing is
the owner's decision.

## 2026-10-20 - Platform: Minecraft 1.20.1, Forge 47.4.10, Java 17, ForgeGradle 6.0.54

Matches the workspace's other mods and the owner's pack. Mod id `herbalwitch`, package
`io.github.dirtyredz.herbalwitch`, MIT.
