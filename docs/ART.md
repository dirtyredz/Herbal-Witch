# Art - The Herbal Witch

Direction, the house style, the owner's reference pieces, how models and textures are made, how
art is reviewed, and the owner checklist.

## Owner direction

- **2026-10-21 (dev.4):** "All of the colour options are terrible." The 18 finishes are gone; one
  design, "cottage": dark wood, deep green and hot pink accents, a little dark purple, vines, plain
  fabric, no flower patterns on cushions.
- **2026-10-21 (dev.5 feedback, owner and wife):** more detail, more blocks; vines far too simple;
  the potion shelf is good, the other furniture "a little kid-like" and needs dark witch vibes;
  every herb (above all the mature stage) needs more detail and vibrancy; herbs float above the bed
  soil; beds should join; the bed's outside "doesn't even look like wood"; the drying rack must be
  bigger and more detailed; the mortar round and smooth stone, not striped; the hearth and infusion
  pot "missed the mark - why pink and green stripes"; machines need their own dark, gothy,
  witchy-forest look, and roundness (like the kettle). This pass (0.2.0-dev.1) answers all of it.
- **2026-10-21 (0.2.0-dev.1 feedback, ten items):** young herbs were flat pale crossed sprites; the
  chest lid, planters and rack were bright orange next to the dark armchair; the rack legs floated
  above their feet; the kettle and pot were too big on the stove; the stovepipe sat on a corner post;
  the hearth's back had holes; furniture tops flickered (z-fighting); watered beds no longer looked
  wet; hung bundles bled onto their host block. 0.2.0-dev.2 answers all ten and the standing rules
  below come from it.
- **2026-10-21 (0.2.0-dev.2 feedback, bundles and rack):** hung bundles were thick stacked blobs with
  no stems; the rack's iron corner caps were huge and its A-frame legs crossed in an X. The legs'
  texture was already the dark wood (`wood_post`, same palette as `wood`); the orange in the
  screenshot is sunlit wood under the owner's shader, so judge colour against the neighbouring
  wood block, not against an unlit preview. 0.2.0-dev.3 answers it.
- **2026-10-21 (0.2.0-dev.9):** the whole set paints at 32 x 32, twice vanilla's pixel density, so the
  owner can paint finer detail in Blockbench. The piece and material textures were already 32 x 32;
  the item sprites (seeds, fresh herbs, bundles, powders, products) and the GUI atlases moved to 2x as
  exact nearest-neighbour enlargements, so nothing looks different until something is repainted.

## Standing rules (owner, from the 0.2.0-dev.1 feedback)

- **Every growth stage is cube-built 3D** at the mature model's level of detail: scaled down toward
  the root, fewer or no flowers, growing toward the mature form. No flat crossed sprites for plants.
  Keep the cube budget garden-friendly (young ages lighter than mature, mature about 60 at most).
- **One wood: the armchair's dark wood** for the whole set (furniture, planters, the drying rack,
  station posts and mantels). There is no light or orange wood.
- **Stove-top pieces are modest** (kettle and pot about 9-10 px across, 11 tall), so they read as
  vessels sitting on a stove, not blocks stacked on it. Chimneys and pipes rise out of the body,
  never perched on a post or bracket.
- **No overlapping coplanar faces facing the same way**, in any model or blockstate combination.
  `node --test "tools/preview/test/*.test.mjs"` and `tools/preview-models.mjs` fail on them.
- **Watered soil must read at a glance:** moist far darker than dry, the two on either side of the
  rails' wood (`BotanyModelsTest` checks the luminance gap).
- **Dried bundles are 2D sprites, inset so they never touch neighbour faces** (owner, after dev.4:
  the first, flatter look was better than the cube bundles). Each herb's own bundle sprite
  is stretched over a zero-thickness cutout plane (two crossed planes where it hangs free), with a
  small 3D pink tie and, on a block, a wooden peg. A plane has only its two broad faces (no
  up/down/edge faces to smear) and keeps at least 0.75 px from every cell boundary, which is what
  stopped dev.1's sprites bleeding onto the host block (`tools/preview/test/bundles.test.mjs`
  guards it). `tools/models/preparations/bundles.mjs` is the one source for every bundle. The
  growing herbs in beds stay cube-built 3D.
- **Rack leg pairs meet at an apex and never cross:** each end's two legs lean in (22.5 degrees)
  until their centre lines meet, then a short post rises to a small iron cap (about 4 x 4 x 3.5 px,
  nothing larger). No X, no leg ending past the other, no big caps.
- **Stove-top vessels share a size:** the mortar and pestle is the kettle's size (about 9 px across,
  ~10-11 tall); its Blockbench project holds the scaled geometry (dev.5 drew it full size and scaled
  it by 0.6).

## The house style (0.2.0)

One set: stations and furniture share a hardware language so they read together.

**Density: 32 x 32.** Every block and item texture is 32 x 32, two texels per model pixel (twice
vanilla's); GUI atlases are 512 x 512 (vanilla's 256 sheet at two texels per GUI pixel). Geometry and
UVs stay in Minecraft's 16-unit space. One density for the whole set: a 16 x 16 texture next to 32 x 32
ones reads as a coarser, mismatched piece, so the producers, the compiler and the tests refuse it
(`tools/art/density.mjs`, `TextureDensity`). Detail finer than one model pixel is the point: a rivet,
a stitch or a grain line can be half a pixel wide.

Since 0.2.0-dev.8 every Blockbench piece owns its textures (`block/pieces/<piece>/<name>.png`, starting as
copies of the house materials below); the `Texture` column names the original material and the shared
wood and gold the procedural models still use.

| Role | Colours | Texture |
|---|---|---|
| Dark brown wood, the armchair's (everything wooden) | #3B2416 .. #5A3A22 | `material/wood` (grain along U), `wood_post` (along V), `wood_end` (shared by the herb bed and hung bundles) |
| Charcoal iron (faint plum cast) | #2E2934 | `material/iron` |
| Gold (rivets, studs, bands, knobs) | #9A681E, #D4A03A, #F2C95A | `material/gold` (shared by the herb bed) |
| Copper (handles, beaded rims) | #6E2E16 .. #C86834 | `material/copper` |
| Dark plum (vessels, lids) | #2E1C36 .. #4A2E54 | `material/plum`, `stations/plum_painted`, `plum_lid` |
| Charcoal stone (hearth) | #34303A .. #4A4550 | `stations/stone_dark` |
| Mottled grey-taupe stone (mortar) | #686058 .. #AAA092 | `stations/mortar_stone`, `plinth_stone`, `pestle_stone` |
| Forest-green velvet | #1C3826 .. #284A32 | `material/velvet` |
| Magenta / hot pink, plum | #C43080, #D8407A; #4A2452 | `fabric_pink`, `trim_purple`, `runner` |
| Ivy, olive vines | #22421E .. #78A048; olive #546022 .. #96A042 | `material/ivy` (cutout) |

**Motifs** (`tools/art/motifs.mjs`): gold crescent moons, gold four-point stars, small pink or
magenta four- or five-petal flowers on olive vine sprigs. They belong on vessels, plaques, runners,
the rug and the stove's cloth. Cushion fabric stays plain (tufting studs and piping only).

**Shapes:** rounded silhouettes from stacked octagonal layers (rows of boxes tiled across the
octagon, with 45-degree chamfer walls set 0.1 px inside an exposed top; never two crossing boxes,
which z-fight), chunky square posts with caps,
protruding hardware (rivets, knobs, clasps, hooks), draped runners with pointed hems, hanging herb
bundles. Rotations only on one axis at 0, +-22.5 or +-45 degrees. Budgets: about 64 elements for a
station cell or furniture piece, about 50 for a mature herb (a garden is many plants).

**No pink/green fabric stripes on machines.** Fabric belongs to furniture; machines are wood, iron,
stone, plum glaze, gold and fire.

## Owner reference pieces (described in words; images in the art-pass job folder)

- **Apothecary dresser (furniture target):** 2-wide low chest in warm mid-brown planked wood, thick
  overhanging top, recessed plinth; black iron corner brackets at all eight corners with gold
  rivets; four deep-green drawer fronts in raised frames with chunky gold knobs; gold studs along
  the top rail; a magenta runner draped over the top and down the front with a gold border, gold
  tassels and an embroidered gold crescent with small pink flowers; dried herb bundles on gold rings
  at the side and a red-gold tassel charm. Saturated colour against dark iron; many small
  protruding details. (0.2.0 gives the one-block apothecary counter this language; the 2-wide piece
  itself is an open question for the owner.)
- **Armchair (also the vine standard):** dark brown frame, chunky capped corner posts with magenta
  inlays and thin gold and plum bands; forest-green velvet seat and tall back, magenta diamond studs
  with gold centres, magenta piping, small gold tabs; draped arm runners and a head runner with
  pointed hems, a small pink flower and gold tassel tips; lush ivy of many small leaves with small
  pink five-petal flowers climbing the back posts, over the top rail, down the front posts and arms.
- **Mortar and pestle:** chunky mottled grey-taupe octagonal bowl tapering to a narrow foot on a
  stepped square plinth, stepped rim, moss and small pink flowers creeping over it, bright ground
  herbs inside, a thick knob-headed octagonal pestle leaning out.
- **Tea kettle:** dark plum octagonal pot of stacked tapering layers; stepped domed lid with a gold
  knob and studs; beaded copper-and-gold rim; curved copper handle with gold bands; angled spout with
  a gold square nozzle; gold moons and stars and pink flowers on olive vines around the body.
- **Infusion pot:** the kettle's sister: squat round plum pot, domed lid with a gold knob on a post,
  thick banded copper-and-gold rims, gold clasps, copper loop handles with gold caps, short gold
  feet, moons, stars and flowers on body and lid, a small gold plaque.
- **Drying rack:** 2 blocks wide, about 2 tall: splayed wooden A-frame legs at each end, black iron
  caps, bands and feet with gold rivets, a top beam with gold strap clips, four twine-tied bundles
  on gold hooks (green leafy, lavender, white-flowered, yellow-flowered).
- **Hearth:** about 2 wide: charcoal stone-brick firebox with a glowing fire and an iron grate,
  reddish-brown mantel posts (built in the house dark wood) and shelf with gold bands and caps, vines with purple flowers over the
  mantel corners, a dark stone chimney breast with a gold moon-and-stars plaque and gold trim, floor
  slabs in front, fire tools on one side and a log crate on the other.
- **Witch's stove:** 2-wide charcoal iron range with wooden corner posts and gold caps; two burners
  (glowing when lit); gold-framed firebox door, two drawers with gold star emblems, a gold towel
  rail with a dark red cloth bearing a gold crescent; a side rail with a pan, spoon and herb bundle;
  a black stovepipe with gold bands; the pot and kettle on top.
- **Herb bed and herbs:** warm planter, two plank rails per side with a groove, chunky corner posts
  rising just above the rails with dark caps and gold rivets, dark lumpy soil just below the rim,
  the plant in the soil. Joined beds are one planter: rails and posts only on the outside. Mature
  herbs are bushy 3D cube plants filling most of the cell, each in its own silhouette and colour.

## Grimoire pieces (0.3.0, first drafts)

The Grimoire Stand and the Witch's Bookshelf are Blockbench projects, **authored by an agent as first
drafts for the owner to refine** in Blockbench, like the other one-offs. They follow the house style
(dark wood, gold, plum, ivy) and the standing rules above (one dark wood, no coplanar overlaps, rotations on
one axis at 0, +-22.5 or +-45).

| Piece | Project | Silhouette | Owned textures (`textures/block/pieces/grimoire/<piece>/`) |
|---|---|---|---|
| Grimoire Stand | `src/art/blockbench/grimoire/stand/` | Crossed foot, a pedestal with gold collars, a tilted board holding an open book, a ribbon and ivy | `wood`, `wood_v`, `gold`, `plum`, `pages`, `ivy`, `pink` |
| Witch's Bookshelf | `src/art/blockbench/grimoire/bookshelf/` | Framed carcass, three shelves of volumes, a crescent bookend, a moon on the side, ivy | `wood`, `wood_v`, `gold`, `plum`, `pages`, `ivy`, `books` |

Each piece owns every texture (no shared house materials), so painting one never changes the other or any
station; models compile to `models/block/grimoire/<piece>/base.json`. The book item sprite
(`item/grimoire/book.png`, 32 x 32) and the reader atlas (`gui/grimoire/reader.png`, 512 x 512) are
procedural, in `tools/art/grimoire/{book,interface}.mjs`, drawn on the old grid and enlarged like the other
sprites. Preview scenes `grimoire-study`, `grimoire-stand` and `grimoire-bookshelf-back` are in
`tools/preview/scenes/catalogue.mjs`.

Review expectations: run the three producers and the previewer, read the grimoire scenes against this guide
(silhouette, one dark wood, nothing flat or kid-like), then the owner checks model transforms, readable UI,
targeting, support, neighbour occlusion and shaders in game. Expect repainting and reshaping; the drafts
are a starting point, not a final look.

## Furniture catalogue (0.4.0, agent first drafts)

The 89 pieces added in 0.4.0 (docs/catalogue/decor.md) are first drafts made by agents for the owner to
refine; owner art review is pending for all of them. They were built with one shared kit from owned copies
of the armchair's materials: the single dark wood palette, forest green, plum, gold and ivy. Every piece owns
its textures (the kit copies them; nothing is shared between pieces), so painting one never changes another.
Dried bundles stay flat 2D sprites; the decorative drying rack does not turn them into cubes. The small
wall shelf's modelled bottles were removed because it now displays real items and modelled bottles would pass
for stored ones. Collision boxes were derived once from the drafts and are hand-edited Java afterwards, so a
reshaped model needs an agent to update them. Wave previews (drafts per piece, montages per wave) are in
`C:/Users/dirty/projects/artifacts/minecraft-mods/jobs/2026-10-22-herbal-witch-m5/previews/`.

## How the art is made

- **Textures** come from `node tools/generate-textures.mjs` (seeded code in `tools/art/`) for the
  procedural families: herbs, soil, the herb atlas, sprites, GUI atlases, and the house wood and gold
  the herb bed and hung bundles use. Every texture of every Blockbench piece is that piece's own,
  painted in its project and exported by the compiler to `textures/block/pieces/<group>/<piece>/<name>.png`
  (nothing else uses it, so painting never reaches another piece). Every texture is 32 x 32 (two
  texels per model pixel; GUI atlases 512 x 512). The materials, soil and herb atlas are drawn at
  32; the item sprites and GUI atlases are still drawn on their old 16 x 16 (256 x 256) grid and enlarged
  2x nearest-neighbour by `tools/art/catalogue.mjs` (`drawnAt1x`); to give one finer detail, draw it
  natively at 32 and drop the enlargement for it. Alpha is 0 or 255. Untinted: no tint index, no
  colour handler.
- **One-off pieces are hand-editable** (owner direction: subtle adjustments in Blockbench). The six
  stations, the furniture (five pieces, and 89 more since 0.4.0), the oil lantern and the two grimoire blocks are Blockbench projects
  (`src/art/blockbench/`, workflow in docs/BLOCKBENCH.md) compiled by
  `node tools/compile-blockbench.mjs`: authored per-face UVs (stretched, flipped or rotated as
  drawn), embedded textures, the item Display, named groups, multiblocks drawn whole and cut into
  cells by the compiler. Every texture in a project is paintable; only the herb atlas and the bundle sprites
  (`parts`, `bundle_<herb>`) are shared previews, because dried bundles inside the rack, stove and
  apothecary chest are generated attachments: Blockbench shows a preview where the compiled model
  shows the bundle.
  Constraints: cubes only, one rotation axis at 0, +-22.5 or +-45 degrees, no tints, the piece's
  bounds, no coplanar overlaps.
- **Procedural models** for the herb bed, herbs and hung bundles come from
  `node tools/generate-models.mjs` (`tools/models/`): position UVs at one unit per model pixel (so
  a painted motif drawn at texture row v appears at model height 16 - v) and stretched sprites for
  bundles and the parts atlas.
- **Herbs:** every age is a 3D model of small cubes over the `botany/parts.png` swatch atlas; ages
  0-2 are derived from the authored mature form (tools/models/botany/plants.mjs). Dried bundles use
  the same atlas (tools/models/preparations/bundles.mjs).
- **Faces:** the generator drops faces buried inside a solid box and organic models (herbs) pull the
  smaller of two coplanar faces in by 0.12 px; the Blockbench pieces keep their faces exactly as
  authored (the migration kept dev.5's omissions), so a new overlap is fixed in Blockbench.

## Review (required before any jar swap)

1. `node tools/generate-textures.mjs`, `node tools/generate-models.mjs` and
   `node tools/compile-blockbench.mjs`, then `.\gradlew.bat runData` (Gradle runs the three
   producers first anyway).
2. `node tools/preview-models.mjs --output <job folder>/previews/<name> --references <job folder>`:
   family sheets (iso and front at one scale), herbs on beds, item GUI sheet, scenes (joined beds,
   growth, multiblocks lit and cold, vessels on the stove, furniture) and reference comparisons.
   It fails on any missing texture, unbound variable, unrendered model or z-fighting face pair.
3. Look at every changed family against the references and this guide; record findings in the job's
   notes. Any later art change needs new previews. The sheets are rendered from the compiled models
   (with bundles expanded), not from the Blockbench view; the manifest records the hashes of the
   `.bbmodel` sources and the compiled models it shows.

The previewer shows directional face shading only: no ambient occlusion, world light, shaders,
animation, mipmaps or held-item extrusion; emissive faces are drawn full bright, and the GUI
orientation follows the transform maths (not checked against a client).

## Owner review checklist (0.2.0-dev.6)

**dev.6 (Blockbench authoring):** no visual change intended; the pieces now come from Blockbench
projects and compare identical to dev.5 headlessly. Check once in the mod-test instance (workspace CLAUDE.md: never an `Evil Eye Trial *` instance): open a piece
in Blockbench 5.2.2, save it unchanged and reopen it; make a small reversible geometry edit, a rug or
lamp-shade texel edit and a Display edit, and see each after F3+T without restarting or a new jar
(docs/BLOCKBENCH.md); all facings, lit/active states, multiblock seams, the kettle and pot on the
stove, neighbour occlusion, held/frame/GUI views and shaders; two clients reloading the same pack.

Status: **awaiting owner review.** Since dev.4: dried bundles are 2D inset sprites again (dev.5), the mortar is the kettle's size. Since dev.2: slim stemmed hung bundles, rack legs meeting at an apex under small caps. Since dev.1: 3D growth stages, one dark wood, rack legs on their
feet, smaller kettle and pot, stovepipe out of the stove's back, solid hearth back, no flickering
tops, wet-looking watered beds (joined too), 3D hung bundles (no leaking onto the host block). Mod-test instance, creative; `/herbalwitch showcase` on a clear
plot, then walk it.

1. **Garden:** every herb age stands in the soil (bed and farmland); mature herbs bushy, vibrant,
   each recognisable; beds read as wood outside; the 2 x 2, line, L and ring join with posts only on
   the outside; break a bed in the middle of a row and the rails come back; every young age is 3D
   and grows; put water within four blocks of a joined 2 x 2 and every soil goes dark and wet.
2. **Stations:** mortar (round smooth stone, moss, pestle), kettle and pot (plum, moons, gold) on
   the stove, the 2 x 2 rack with and without bundles, the 2 x 2 hearth lit and cold, the stove lit
   and cold. Place and break each multiblock (one item back, contents dropped once).
3. **Furniture:** armchair, counter, lamp lit and unlit, shelf, rug: dark witchy, not kid-like;
   ivy dense enough; all four facings.
4. **Context:** items in hand, GUI, frames; shaders off and on; F3+T; an existing dev.5 world's beds
   joining after a little while (random ticks repair them).

5. **Grimoire (0.3.0 first drafts):** the stand and bookshelf in all four facings, on a floor and refused
   without support, beside walls and full blocks (no x-ray strips), held and GUI views of the book, the
   reader at normal GUI scales (readable text, recipe pages), the station "Grimoire" button; shaders
   off and on.

Feedback wanted: per piece keep / change (what); whether "more blocks" means new furniture pieces
(such as the 2-wide dresser) as well as richer models.
