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

## The house style (0.2.0)

One set: stations and furniture share a hardware language so they read together.

| Role | Colours | Texture |
|---|---|---|
| Warm reddish-brown wood (stations, beds, chest) | #542A16 .. #AA5E32 | `material/wood` (grain along U), `wood_post` (along V), `wood_end` |
| Dark brown wood (armchair, shelf, lamp) | #3B2416, #5A3A22 | `material/dark_wood` |
| Charcoal iron (faint plum cast) | #2E2934 | `material/iron` |
| Gold (rivets, studs, bands, knobs) | #9A681E, #D4A03A, #F2C95A | `material/gold` |
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

**Shapes:** rounded silhouettes from stacked octagonal layers (`tools/models/shapes.mjs`: two
crossing boxes, or exact octagons with 45-degree corner walls), chunky square posts with caps,
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
  reddish-brown mantel posts and shelf with gold bands and caps, vines with purple flowers over the
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

## How the art is made

- **Textures** come only from `node tools/generate-textures.mjs` (seeded code in `tools/art/`).
  32 x 32 for materials (two texels per model pixel), 16 x 16 for young herb, seed, fresh-herb and
  product sprites; alpha is 0 or 255. Untinted: no tint index, no colour handler.
- **Models** for stations, furniture, the herb bed and herbs come from
  `node tools/generate-models.mjs` (`tools/models/`): position UVs at one unit per model pixel (so a
  painted motif drawn at texture row v appears at model height 16 - v), decal UVs for stretched
  sprites (ivy, runners, plaques, bundles, glass, flames, the rug), multiblocks authored as one
  assembly and split into cells. The bundle and lantern templates under `preparations/templates/`
  are still hand-authored JSON.
- **Herbs:** ages 0-2 are 16 px sprites on the planted cross; age 3 is a 3D model of small cubes over
  the `botany/parts.png` swatch atlas.

## Review (required before any jar swap)

1. `node tools/generate-textures.mjs` and `node tools/generate-models.mjs`, then `.\gradlew.bat runData`.
2. `node tools/preview-models.mjs --output <job folder>/previews/<name> --references <job folder>`:
   family sheets (iso and front at one scale), herbs on beds, item GUI sheet, scenes (joined beds,
   growth, multiblocks lit and cold, vessels on the stove, furniture) and reference comparisons.
   It fails on any missing texture, unbound variable or unrendered model.
3. Look at every changed family against the references and this guide; record findings in the job's
   notes. Any later art change needs new previews.

The previewer shows directional face shading only: no ambient occlusion, world light, shaders,
animation, mipmaps or held-item extrusion; emissive faces are drawn full bright, and the GUI
orientation follows the transform maths (not checked against a client).

## Owner review checklist (0.2.0-dev.1)

Status: **awaiting owner review.** Trial instance, creative; `/herbalwitch showcase` on a clear
plot, then walk it.

1. **Garden:** every herb age stands in the soil (bed and farmland); mature herbs bushy, vibrant,
   each recognisable; beds read as wood outside; the 2 x 2, line, L and ring join with posts only on
   the outside; break a bed in the middle of a row and the rails come back.
2. **Stations:** mortar (round smooth stone, moss, pestle), kettle and pot (plum, moons, gold) on
   the stove, the 2 x 2 rack with and without bundles, the 2 x 2 hearth lit and cold, the stove lit
   and cold. Place and break each multiblock (one item back, contents dropped once).
3. **Furniture:** armchair, counter, lamp lit and unlit, shelf, rug: dark witchy, not kid-like;
   ivy dense enough; all four facings.
4. **Context:** items in hand, GUI, frames; shaders off and on; F3+T; an existing dev.5 world's beds
   joining after a little while (random ticks repair them).

Feedback wanted: per piece keep / change (what); whether "more blocks" means new furniture pieces
(such as the 2-wide dresser) as well as richer models.
