# Decisions - The Herbal Witch

Newest first.

## 2026-10-21 - Herb bed inner corners: four inner-corner flags (supersedes "no diagonal state")

Owner screenshot: where joined beds form an L, U or ring, the inside corner showed a notch above the
soil, because the corner part (both sides joined) only had wooden sides up to the soil and the two
neighbours' rails stopped short of each other. A multipart state cannot tell an interior corner from
an inside corner without the diagonal, so the bed gains four booleans (`inner_north_east`,
`inner_east_south`, `inner_south_west`, `inner_west_north`): set when both sides of the corner are
joined and no bed stands diagonally. They are computed with the sides (`GardenBedConnections`:
`refreshCorners`; in `updateShape` for side changes) and refreshed on the four diagonal beds when a
bed is placed or removed (`refreshDiagonals`, no neighbour update reaches a diagonal). The flag adds
the `wood/inner` part (the two neighbours' rails carried round the corner, same profile) and swaps the
wood-sided soil corner for `soil/corner_inner`. `tools/preview/test/bed-corners.test.mjs` floods a
slice of every arrangement (line, square, L, U, T, plus, ring, staircase) at five heights and fails
when the outside reaches the planter. Old saves lack the flag and look as before until their random
tick repairs them. Rejected: a taller corner box with no state (shows as a pillar inside a 2x2).

## 2026-10-21 - 0.2.0-dev.5: dried bundles back to 2D sprites, mortar at kettle size

Owner feedback on dev.4: the first, flatter bundle look was better than the cube bundles; the mortar
is too big beside the kettle. Bundles are the herb's own item sprite on zero-thickness cutout planes
(crossed for free-hanging ones), a small 3D tie, and a peg or hook; planes carry only their two broad
faces and stay 0.75 px inside every cell boundary, the fix for dev.1's bleed onto the host block.
The mortar is drawn at full size and scaled by 0.6 (about 9 px across, like the kettle), its hit
box shrunk to match. Supersedes the 3D bundle builder below (dev.2/dev.3). Rejected: keeping cubes
(the owner's call), 1 px thick sprite boxes (their top and bottom faces smeared onto neighbours).

## 2026-10-21 - 0.2.0-dev.3: slim stemmed bundles, rack legs meeting at an apex

Owner feedback on dev.2: the cube bundles read as stacked blobs and lost their stems, and the rack's
legs crossed under oversized caps. Bundles are now gathered stems, a twine tie, then thin kinked
stems with a few sparse leaf and floret cubes (about 20 elements, 4 px wide). Rack legs keep the
22.5-degree rotation rule, so they meet where their centre lines cross (17.5 px up) and a short
post carries on to a small cap; rejected: a shallower lean (not an allowed angle) and moving the
feet inward (cell layout). The leg texture was already the dark wood.

## 2026-10-21 - 0.2.0-dev.2: 3D herbs at every age, one dark wood, a z-fighting guard

From the owner's dev.1 feedback (docs/ART.md "Standing rules"). Young herb ages are derived from
each mature model (scaled toward the root, thinned, flowers absent, then buds, then partly open)
rather than authored three more times per herb, so a mature redesign carries through; the stage
sprites and the planted-cross template are gone. The armchair's dark wood replaced the warm wood as
`material/wood` (the texture name kept, `dark_wood` removed), so every wooden piece changed at
once. Z-fighting is guarded by a geometric sweep in the previewer (node tests and preview runs)
over every model and multipart combination, not by convention; organic models resolve their own
coincidences automatically, built pieces by hand. Dried bundles are one 3D builder shared by the
hung bundle block, rack, stove and counter (the four station bundle sprites are gone). Rejected:
authored young models per herb (42 more shapes to keep in step); a per-element size jitter to
dodge the check (hides the fault instead of removing it); keeping a separate warm wood.

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
between two pipelines). The lantern templates stay hand-authored (bundles are generated since dev.2).

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
by their random tick. (The inner-corner tradeoff here was reversed by the 2026-10-21 inner corner
flags above.) No chunk-load migration.

## 2026-10-21 - Herbs rooted 2 px into the ground; mature herbs are 3D

(Superseded 2026-10-21 by 3D young ages, see above.) Young ages use a planted cross whose roots are two pixels below the block, matching the bed's soil
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
