# Editing pieces in Blockbench - The Herbal Witch

The owner's workflow for hand-adjusting the one-off pieces and seeing each save in game after
F3+T, with no datagen and no new jar. How it works: docs/ARCHITECTURE.md ("Art pipeline"); why:
docs/DECISIONS.md (0.2.0-dev.6); traps: docs/GOTCHAS.md.

M10 first drafts: `magic/{broom,wand}/` and `stations/tools/{broom_stand,wand_bench}/`.
Each owns six 32px textures. The broom's item parent is also its ridden geometry; authored
display transforms apply only to item poses. Station sidecars cut the two supported cells.

| Piece | Project |
|---|---|
| Tea kettle, infusion pot, mortar and pestle | `src/art/blockbench/stations/{kettle,infusion_pot,mortar}/` |
| Drying rack, hearth, Witch's Stove (multiblocks) | `src/art/blockbench/stations/{drying_rack,hearth,stove}/` |
| Armchair, apothecary chest (`apothecary_counter`), wall shelf, table lamp, small rug (the 0.1.0 pieces) | `src/art/blockbench/decor/<piece>/` |
| The 89 pieces added in 0.4.0 (first drafts) | `src/art/blockbench/decor/pieces/<family>/<group>/<piece>/` (see below) |
| Oil lantern | `src/art/blockbench/preparations/oil_lantern/` |
| Grimoire Stand, Witch's Bookshelf (first drafts by an agent, for you to refine) | `src/art/blockbench/grimoire/{stand,bookshelf}/` |
| Ritual altar (0.5.0, first draft by an agent, 2 x 1 x 2 multiblock) | `src/art/blockbench/ritual/altar/` |
| Witch's Cauldron, Copper Still, Bottling Bench, Charm Workbench (0.6.0, first drafts by an agent) | `src/art/blockbench/stations/{cauldron,still,bottling_bench,charm_workbench}/` |
| Ritual circle (0.6.0, first draft by an agent, a 5 x 5 ring of 16 cells) | `src/art/blockbench/ritual/circle/` |
| Ward stone (0.6.0, first draft by an agent) | `src/art/blockbench/ward/ward_stone/` |
| Familiar bed, nest and floor perch (M9 first drafts) | `src/art/blockbench/familiars/homes/{bed,nest,perch}/` |

### The 0.4.0 furniture projects

The 89 pieces added in 0.4.0 are agent first drafts for you to refine. Each is a project at
`src/art/blockbench/decor/pieces/<family>/<group>/<piece>/` (for example
`decor/pieces/seating/chairs/dining_chair/`); the family, group and piece come from the catalogue
(docs/catalogue/decor.md lists every path). Those with several cells are authored whole and cut by the
compiler, like the station multiblocks, using a 3D cell convention:

- **Cell 0** is x 0..16, y 0..16, z 0..16. The next cell to the viewer's **right** is at **x - 16**, **up**
  is **y + 16**, and **back** is **z + 16**. The piece faces north as drawn.
- `piece.json` `multiblock.cells` lists the cells as `[right, up, depth]` in the catalogue's order, the
  same order the Java mount uses.
- **Variants:** a light with a `glow` group has a `lit_<n>` model per cell (the glow group shows only
  while lit); the door has `mirrored_<n>`. Author the door **closed and left-hinged**, its leaf at z 13..16;
  the compiler writes the `mirrored_<n>` variant for the other hinge.
- **Item normalisation:** an assembly reaching beyond -16..32 in any direction is moved inside the item
  frame and its display translations corrected, so large pieces fit the hand, GUI and ground.
- **Display shelves keep empty boards.** Where each item stands is in the Java `DisplaySpec` (anchors per
  slot on a board), not in the model, so moving or resizing a board needs an agent to move its anchors.
  Do not add modelled items to a shelf; the game draws the real ones.
- **Collision lives in Java** (the `Shapes` classes) and does not follow the model; a reshaped piece needs
  an agent to update it.
- Seats follow the same convention: the sitter's position per seat is in Java `SeatLayout`, in the same
  north-facing pixels.

Each folder holds `<piece>.bbmodel` (what you edit) and `piece.json` (the Minecraft side: output
files, texture bindings, which groups glow; edited by agents, not in Blockbench). Herbs, herb beds
and the hung bundle blocks stay generated (`tools/models/`).

The two grimoire pieces own their textures like the rest: the stand has `wood`, `wood_v`, `gold`, `plum`,
`pages`, `ivy` and `pink`; the bookshelf `wood`, `wood_v`, `gold`, `plum`, `pages`, `ivy` and `books`, each at
`textures/block/pieces/grimoire/<piece>/<name>.png`. Stand: crossed foot, pedestal with gold collars, tilted
board holding an open book, ribbon and ivy. Bookshelf: framed carcass, three shelves of volumes, crescent
bookend, side moon, ivy. They have no groups the game treats specially. The grimoire's book sprite and reader
atlas are procedural (`tools/art/content/grimoire/book.mjs` and `tools/art/interface/grimoire.mjs`), not painted in Blockbench.

The ritual altar (`ritual/altar`) is authored whole like the other multiblocks and cut into
`part_0..3`. It owns fourteen textures at `textures/block/pieces/ritual/altar/`: `wood`, `wood_v`, `stone`,
`iron`, `plum`, `pink`, `gold`, `brass`, `linen`, `ivy`, `flame`, `glow`, `moon`, `runes`. Its `active`
group (candle flames, glowing rune strips, the moon glow, motes) is an emissive overlay named in
`piece.json` `overlays`; it shows only while a ritual runs and compiles to `active_2` and `active_3`.
Collision lives in Java (`AltarShapes`, from the assembly boxes) and does not follow the model; a reshaped
altar needs an agent to update it. The glow is currently orange (the hearth's glow texture); repaint the
`glow` texture if you want violet. Preview evidence:
`C:/Users/dirty/projects/artifacts/minecraft-mods/jobs/2026-10-22-herbal-witch-m6/previews/`.

### The 0.6.0 pieces

The four advanced stations, the ritual circle and the ward stone are agent first drafts, authored whole and
compiled like the other pieces; each owns its textures at
`textures/block/pieces/<stations|ritual|ward>/<piece>/` (lists in docs/ART.md). Notes for editing them:

- The still, the bottling bench and the charm workbench are multiblocks (1 x 2 tall, 2 x 1 and 2 x 1);
  keep the cells in the order `piece.json` `multiblock.cells` lists, as for every multiblock. The cauldron
  and the ward stone are single blocks with a `base` and an `active` model.
- **The ritual circle sets `multiblock.assembly: false` in `piece.json`.** The ring is 16 cells around the
  altar and the item is a flat sprite, so the compiler writes the `part_<n>` and `active_<n>` models for
  the cells (32 files) and no `assembly` model. Do not add one: a 5 x 5 ring is too large for one item
  model. The sprite is made by `tools/art/content/magic/sprites.mjs`, not in Blockbench.
- The `active` groups are emissive overlays that show only while the piece is working (the ward stone
  while its ward is active), as for the altar.
- Collision lives in Java (`CauldronShapes`, `StillShapes`, `BottlingBenchShapes`, `CharmWorkbenchShapes`,
  `RitualCircleShapes`, `WardStoneShapes`) and does not follow the model; a reshaped piece needs an agent to
  update it.

## Once: install and set up

1. **Install desktop Blockbench 5.2.2** (the Windows installer from
   <https://github.com/JannisX11/blockbench/releases/tag/v5.2.2>). No plugin is needed. If
   Blockbench offers an update, decline it: the pipeline reads project format 5.0 exactly and names
   the version it found when a newer editor saved a file. A later version is adopted deliberately
   (new fixtures, a parser check), not by accident.
2. In **File > Preferences > Settings...**, category **Export**: leave **Embed Textures** on (the
   default), and turn **Minified Project Files** off so a save stays readable in git diffs (the pipeline
   reads either form).
3. **Open a terminal in the mod folder** (`C:\Users\dirty\projects\minecraft-mods\mods\HerbalWitch`)
   on the branch you are editing, and bring the art up to date once:
   `.\gradlew.bat runData` (it runs the art producers first).
4. **Start the watcher** and leave it running while you edit:

   ```
   node tools/compile-blockbench.mjs --dev-pack "<mod-test instance>/.minecraft" --watch
   ```

   Use the dedicated mod-test instance (a copy of the Evil Eye pack plus the build under test, owner ruling
   2026-10-22; the workspace session owns it), never an `Evil Eye Trial *` instance. It publishes
   `resourcepacks/HerbalWitch-dev-art/` in that instance (and touches nothing else
   there), then prints `ready for F3+T`. Without `--watch` it publishes once and exits.
5. **In game, once:** Options > Resource Packs, move **HerbalWitch-dev-art** to Selected and put it
   at the **top** of the list, then Done. The game remembers the choice.

### First session: prove the editor and the parser agree

Open `stations/kettle/kettle.bbmodel`, change nothing, and **Save Project** (below). The watcher
should print `published ... ready for F3+T` (an unchanged save publishes nothing new). A genuine save of
the kettle is committed as `tools/blockbench/test/fixtures/editor-save/kettle.bbmodel`;
`project.test.mjs` checks that such a save still compiles to the committed models.

## The edit loop

1. **Open** the piece: File > Open Model, pick its `.bbmodel`. Check File > **Project...**: the format
   is **Java Block/Item** and **Format Version** reads **1.9 - 1.21.5** (do not change it). The
   Textures panel lists the materials; they are embedded, with no file path.
2. **Edit** (see "What you can change" below).
3. **Save Project**: File > Save Project, **Ctrl+Alt+S**. (Ctrl+S, "Save Model", also writes the
   `.bbmodel` for a project opened from one.) Never export Java JSON by hand: the compiler writes the
   game files, including what Blockbench's own export would lose.
4. Wait for `ready for F3+T` in the terminal (about two seconds), then press **F3+T** in game.
   A red `NOT published` message means the game keeps the last good version; fix what it names and
   save again.

### What you can change

- **Geometry:** move, resize, add or delete cubes. Rotations are one axis, in 22.5-degree steps up
  to 45 degrees (Blockbench enforces this in this format version). Keep cubes inside the piece's
  bounds (one block; the multiblocks span their cells).
- **UVs:** per-face UV rectangles, flips and 90-degree UV rotations; clearing a face's texture
  removes the face.
- **Display:** the Display tab (top right) sets how the item looks in the hand, GUI, ground and item
  frame. It is saved in the project and needs nothing else.
- **Textures you own:** every texture in a piece (wood, gold, iron, copper, plum, glaze, fabrics, glass,
  ivy, stone, the rug, the lamp shade...) belongs to that piece alone and is paintable: paint it in
  Blockbench and save, and only that piece changes. Each is its own texture in the Textures panel, so
  a piece that has `wood` and `wood_v` (or `iron` and `iron_top`) has two to paint. They are exported
  to `textures/block/pieces/<group>/<piece>/<name>.png`. Keep each texture at 32 x 32 (see
  "Painting at 32 x 32") and its pixels purely transparent or opaque. The one exception: the herb atlas (`parts`) and
  the `bundle_*` sprites are the procedural herbs' art, drawn into the dried bundles; painting those
  is refused. Shared textures cannot be added to a piece.
- **Groups** are named for what they are (body, lid, handle, shade, frames/legs...). Some groups mean
  something to the game:
  - `shade` in the table lamp glows when the lamp is lit; `lit` in the oil lantern (coal and flames)
    shows only while lit, and glows.
  - `fire` in the hearth and stove is the overlay shown while lit (it glows); `bundles` in the drying
    rack shows while it holds something.
  - `bundle`, `bundle_0`..`bundle_3` (stove, apothecary chest, drying rack) are **bundle anchors**:
    the dried bundle is generated from the shared bundle art at the group's pivot. Move the whole
    group to move the bundle; the cubes inside it are only a preview and are ignored.
  Keep these names; rename or regroup anything else freely.
- **Multiblocks** (drying rack and hearth 2x2, stove 2x1) are edited whole, in place: the origin
  cell is x 0..16, the second column x -16..0, the second row y 16..32 (the stovepipe may rise to
  y 32 above the stove). The compiler cuts the assembly into the game's cells; a cube may cross a
  seam.

### Painting at 32 x 32

The set paints at **two texels per model pixel**, twice vanilla's density (docs/DECISIONS.md,
0.2.0-dev.9). What you see in Blockbench:

- **Every texture is 32 x 32** (right-click a texture > Properties shows 32 x 32). The brush paints
  one texel, which is half a model pixel, so a 1 px rivet is 2 x 2 texels and a line can be half a
  pixel wide. Blockbench's painting grid (View menu) and the UV editor's grid show the
  texels.
- **Geometry and UVs stay in 16 units.** The UV editor still measures faces in the 16-unit space: a
  face 4 px wide spans 4 UV units and 8 texels. File > **Project...** keeps its texture size (UV
  resolution) at **16 x 16**; do not change it to 32.
- **New textures:** create them at **32 x 32** (Textures panel > Create Texture, Resolution 32). A
  texture at any other size is refused (`every texture is 32 x 32`), so a 16 x 16 one cannot slip
  in beside the others. If Blockbench offers to change the project's resolution or rescale UVs to
  match a texture, decline. Do not resize existing textures.
- **The procedural art is not painted in Blockbench.** The item sprites (seeds, fresh herbs, bundles,
  powders, products), the GUI atlases, the herb atlas, the herb bed soil and the shared wood and gold
  come from `tools/art/` and are 32 x 32 too. The sprites and GUI atlases are still drawn on their
  old 16-pixel grid and enlarged, so they look blocky at 32 until someone draws them finer.

## When a save is refused

| Message | Meaning |
|---|---|
| `the embedded preview differs from the shared ...` | The herb atlas or a bundle sprite was painted. Undo it; every other texture is yours to paint. |
| `... is not supported` (mesh, group rotation, tint, inflate, light_emission, a second rotation axis) | That Blockbench feature does not exist in Minecraft 1.20.1 block models; use cubes and element rotations. |
| `project format 5.x` / `Minecraft target ...` | Blockbench was updated, or the project's Format Version changed; see "Once" step 1 and the Project dialog. |
| `N x N; every texture is 32 x 32` | A texture is not 32 x 32 (a new texture made at Blockbench's default 16, or a resized one). Recreate it at 32 x 32. |
| `coplanar faces overlap` | Two faces lie in the same plane and would flicker (z-fight). Move one by 0.1 px or more. |
| `leaves the permitted bounds` | A cube left the piece's space; bring it back, or ask for the bounds to change. |
| `not valid JSON` | The file was read mid-save; the watcher retries by itself. |

Some changes are not artwork and need an agent (code, datagen and a new jar): a new state or overlay
group, a different cell layout, collision or hit boxes (the shapes live in Java and do not follow
the model), or a piece that should stand on or support something differently. After datagen or a
shared-texture change, restart the watcher.

## Finishing

1. Stop the watcher (Ctrl+C).
2. Write the edits into the mod and check them:

   ```
   node tools/compile-blockbench.mjs
   node --test "tools/art/test/*.test.mjs" "tools/blockbench/test/*.test.mjs" "tools/preview/test/*.test.mjs"
   node tools/preview-models.mjs --output <job folder>/previews/<name> --references <job folder>
   .\gradlew.bat test build
   ```

   (An agent normally does this and reviews the sheets.)
3. Commit the `.bbmodel` files together with the regenerated models and textures.
4. Once the next jar is installed, move **HerbalWitch-dev-art** back to Available (or delete the
   folder) and press F3+T: the game shows the jar's art again.

The pack is client-side only; a second client sees the edits only with its own copy of the pack.

## Living creatures (M9 drafts)

The separate `src/art/entities/catalog.json` lists raven (including crow), owl and snake. Each owns a Blockbench 5.2.2 modded-entity project in format 5.0 plus `rig.json`. Cuboids use named parent-before-child bones, pivots and box UVs. Meshes, per-cube rotations and authored animation clips are refused. Runtime reads compiled JSON and applies procedural idle, walk/slither, flight, rest and attack. The block compiler still rejects entity projects.

Creature atlases use a logical 32-unit square exported at 64 x 64, preserving two texels per model unit. Block/item textures remain 32 x 32. Native species retain vanilla textures and variants. Compile with `node tools/compile-entity-models.mjs`; `--check` verifies without writing. Gradle includes both. Eight equipment projects live at `src/art/blockbench/familiars/equipment/<gear>/`.

Creative wild-species eggs live at src/art/blockbench/familiars/spawn_egg/<raven|owl|snake>/.
They use the ordinary block/item compiler and owned 32px textures, not the living-rig parser.
Current block compiler inventory: 133 projects, 438 models and 856 owned textures.
Native familiar rendering uses vanilla models/textures with resettable cosmetic rest/guard poses.
`FamiliarNativePoseTest` exports actual model vertices and attachment matrices to
`build/familiar-native-previews/`. After `test`, run `node tools/preview-native-familiars.mjs --output
<job>/previews/native-poses`; review its 20 idle/walk-or-flight/rest/guard poses. The runtime and preview
share attachment positions, scales and pitch. Vanilla textures are read only from the cached dependency
and are never copied into shipped assets. These samples do not replace owner animation/shader review.
