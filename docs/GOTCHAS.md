# Gotchas - The Herbal Witch

- **Never run `runClient` from an agent.** GameTests are the headless check; the owner verifies
  visuals, item transforms, shaders and two-client sync in the trial instance.
- **Client classes stay in `client/`.** `HerbalWitchClient` is a `Dist.CLIENT` subscriber; feature and
  `data/` code never reference it, so a dedicated server never loads client classes. The GameTest
  server is a dedicated server and would fail on a leak.
- **`ItemModelProvider.getBuilder(path)` only prepends `item/` when the path has no slash.** Our ids
  contain slashes, so pass `"item/" + path` explicitly, or item models land in `models/decor/...`.
- **runData does not delete outputs nobody generates any more** reliably. When a generated path
  changes, delete `src/generated` and regenerate; `GeneratedResourcesTest` fails on any stale file.
  Generation is deterministic: run it twice and compare bytes after changing a provider.
- **Every generated directory must stay under 12 entries** (`GeneratedResourcesTest`). Deeper variants
  get their own directory level.
- **No model JSON is hand-edited.** Two producers own disjoint directories and each deletes stray
  JSON only in its own: `tools/generate-models.mjs` (herbs, herb beds, hung bundles; edit
  `tools/models/`) and `tools/compile-blockbench.mjs` (stations, furniture templates, the oil
  lantern; edit the `.bbmodel` in `src/art/blockbench/`). Procedural UVs are position-based (one
  unit per model pixel, v = 16 - y on sides), so a motif painted at texture row r shows at model
  height 16 - r/2: move the motif, not the UV; bundle sprites and the parts atlas stretch instead.
  Blockbench UVs are authored and kept exactly (`TemplateChecks.checkAuthored` skips the density
  rule); the compiler never recomputes them from positions.
- **Blockbench is pinned to 5.2.2** (project format 5.0, "Java Block/Item", Format Version
  "1.9 - 1.21.5"). A save from a newer editor, a changed Format Version, meshes, group rotations,
  tints, `light_emission` or a second rotation axis are refused with the project, element and field
  named. Blockbench's `light_emission` does not exist in 1.20.1: glow is Forge's `forge_data`,
  added by the compiler from `piece.json` emissive groups (and a plain Java export would drop it, so
  nothing is exported by hand). Blockbench minifies saves by default; turn "Minified Project Files"
  off so diffs stay readable.
- **Every piece texture is the piece's own; only the herb atlas and bundle sprites are shared.**
  Every texture a project binds is in `piece.json` `owned_textures`, at
  `block/pieces/<project dir>/<variable>.png` (the catalogue's `owned_texture_dir` makes the compiler
  enforce it), and is written from the embedded bitmap. The two shared kinds (`block/botany/parts`, the
  `item/preparations/herbs/**/bundle` sprites) are previews that must match the procedural PNG pixel
  for pixel or the compile fails; after a palette change in `tools/art/`, run
  `node tools/compile-blockbench.mjs --refresh-previews`. Two variables of one piece that once shared a
  material (`wood` and `wood_v`, `iron` and `iron_top`) are now separate PNGs: paint both. Colour under fully
  transparent pixels is not compared (an editor canvas does not keep it).
- **Multiblock seams are the compiler's.** Rack, hearth and stove are authored whole; cells are cut
  in each element's unrotated frame, explicit UVs cropped (flips and UV rotations included), pivots
  moved with the cell, faces created by a cut omitted and cullfaces recomputed per cell (authored
  cullfaces count only in one-block pieces). A plane lying exactly on a seam belongs to both cells.
- **The dev-art pack is not the jar.** It overrides the compiled models, the owned textures and
  the item models and blockstates that show them, in `resourcepacks/HerbalWitch-dev-art/` of the
  instance given (never mods/, saves or config). New states, overlays, cells, collision or shapes
  need code, datagen and a jar; after datagen or a shared-texture change restart the watcher (it
  caches). The pack is client-side: each client needs its own copy. The trial instance is
  packwiz-managed; its installer does not track this folder, but re-publish if a launch removed it.
- **One texture density: 32 x 32, UVs in 16 units.** Every block and item texture is 32 x 32 and
  every GUI atlas 512 x 512 (0.2.0-dev.9); model coordinates and UVs never change with it, so a UV of
  4 units covers 8 texels. Blockbench creates new textures at the project resolution (16) by default:
  the compiler refuses them, so create them at 32. The item sprites and GUI atlases are drawn on a
  16 (256) grid and enlarged in `tools/art/catalogue.mjs`; a sprite drawn natively at 32 must leave
  that enlargement, or it is doubled to 64 and refused. GUI blits keep vanilla's 256 logical size
  (`GuiGraphics.blit` without a texture size): u/v and widths stay in GUI pixels, only the file is
  finer. Parity across a resolution change compares at the game's sampling (`--compare` accepts an
  exact integer nearest-neighbour enlargement, nothing else).
- **Faces may not span more than one texture.** A face longer than 16 pixels cannot have position
  UVs; multiblock pieces are split per cell (and the item assembly is the split pieces put back).
- **Rotations are one axis, 0 / +-22.5 / +-45 degrees.** A rotated element's `from`/`to` is checked
  unrotated; after rotation it may lean slightly out of its cell (the rack legs, the pestle).
- **Cutout only where needed.** A model is `minecraft:cutout` exactly when a texture it uses has
  transparent pixels (glass, ivy, runners, cloth, flame, bundle sprites); the compiler and
  `TemplateChecks` read that from the PNG, never from variable names. Alpha is strictly 0 or 255.
- **`GameTestHelper.destroyBlock` drops nothing** in 1.20.1; use `level.destroyBlock(pos, true)`.
- **GameTest pass/fail beacons leak light** into neighbouring tests (block light 3 was observed).
  Light assertions run inside a sealed stone room.
- **`GameTestHelper.relativePos` is not the inverse of `absolutePos`** in 1.20.1 (from Roundwood).
- **The datagen cache is not source.** `src/generated/resources/.cache/` is ignored and excluded from
  the jar.
- **Capabilities, SavedData, recipe reloads, capture tokens** (later milestones): capabilities need
  explicit death-clone and sync; SavedData must be marked dirty and kept on the Overworld; recipe
  reloads can remove a recipe mid-process; copied capture items must not duplicate a creature;
  Minecraft saves are not transactions across player, chunk and SavedData files.
- **Shared datagen outputs have one writer.** Two providers writing `lang/en_us.json`, a tag file,
  `global_loot_modifiers.json` or recipes overwrite each other (and vanilla names every
  RecipeProvider "Recipes", which the generator refuses twice). Features contribute to
  `HerbalWitchLanguageProvider`, `HerbalWitch{Block,Item}TagsProvider`, `HerbalWitchRecipeProvider`
  and `HerbalWitchLootModifierProvider`.
- **Herb plants survive without light.** Vanilla crops need light to survive, but light is not
  computed while worldgen places patches, so herbs check soil only (growth still needs light).
  GameTest areas have little sky light: growth tests put a light source beside the plant.
- **Showcase sections must not touch registries while planning.** Placements carry lazy
  `Supplier<BlockState>`s (lambdas, not `Blocks.X::defaultBlockState`, which evaluates `Blocks`
  eagerly) so the plan is unit-testable without a Minecraft bootstrap.
- **The showcase refuses an occupied plot.** Signs and barrels are block entities, so a second run
  over a built showcase is refused by the preflight; use a fresh plot.
- **Station recipes are exact.** Every filled input slot must be used by the recipe, so a stray item
  blocks processing; that keeps three-slot and one-slot grinding recipes unambiguous.
- **Recipe reloads cancel jobs whose recipe changed** (id or content fingerprint). Ingredients stay
  in their slots; progress restarts. A saved job is revalidated on its first tick after loading.
- **Vanilla `moveItemStackTo` merges into slots without asking `mayPlace`.** Station menus insert
  through each slot's own rules instead, or shift-click would fill output slots.
- **Containers come from recipes, not craft remainders.** Processing recipes list returned
  containers explicitly (`returns`); a consumed item's crafting remainder is not added.
- **Water purity rides only through an installed carrier.** Without Thirst Was Taken the tea has no
  purity tag; with it, the water's `Purity` int is copied unchanged (never upgraded by heating).
- **Compat runtime profiles need SRG refmap remapping** for the local FD/Thirst jars; build.gradle
  sets it only when `-PcompatProfile` uses them. Their mixin warnings about absent mods are harmless.
- **Never name a source directory `build/`, `run/`, `out/` or `bin/`.** The shared `.gitignore` ignores
  them anywhere in the tree, so the files are silently left out of commits (a standalone clone build
  caught `showcase/build/`, now `showcase/construction/`).
- **Decor has no tints.** Materials are plain textures; there is no colour handler to register, so a
  face's look is exactly its texture. A new look needs a new texture, not a tint index.
- **Herb roots are at model y = -2.** The bed's soil top is 14, so a plant one block up meets it at
  -2; farmland (15) buries one pixel, full ground two. `BotanyModelsTest` composes these heights:
  change the bed and the plant templates together.
- **Bed connections change only on placement, neighbour updates and random ticks.** A bed set with
  flags that skip neighbour updates (or saved before 0.2.0) keeps stale sides until its next random
  tick; `onPlace` only joins a newly placed bed, not a bed changing its own moisture.
- **Multiblock stations: place all cells or none.** Tests and the showcase place them through
  `StationBlock.placeStation` (or cell by cell, origin first); a lone origin with a missing cell is
  removed by the next shape update from that side. Only cell 0 has a block entity.
- **The stove's pipe reaches past its cells.** The stovepipe rises at the back, straddling the two
  cells, up to y 32 (the blocks above the stove, at their shared back edge); a full block placed
  there clips it. Vessels on the burners stay clear of it.
- **Coplanar faces z-fight, and the octagon trick made many.** Two boxes crossing to fake a rounded
  slab share their top and bottom planes over the centre; rotated chamfer walls share them too, with
  different UVs, so the tops flickered in game (0.2.0-dev.1). The octagons were rebuilt as tiled rows
  with chamfer walls 0.1 px inside an exposed top (now Blockbench geometry), and the z-fighting sweep (`tools/preview/report/zfighting.mjs`,
  run by the node tests and the previewer) fails on any same-facing overlap closer than 0.02 px,
  per model and per blockstate combination. Flush decorations need a real offset (0.1 px or more).
- **A colour change can erase a state.** The 0.2.0 soil restyle made dry soil dark too, so moist
  soil (only a shade darker) became indistinguishable and "watered" vanished although the blockstate
  still switched. Keep state-carrying textures far apart; `BotanyModelsTest` checks the soil gap.
- **A rotated leg pivots about its origin, not its foot.** Rack legs rotated about the cap swung
  their lower ends off the feet; pivot a leaning part where it must touch (its foot) and let the
  free end land where it may.
- **Stretched sprites on thin boxes leak.** A whole sprite squeezed onto a 1 px side or top face
  shows as coloured streaks next to the host block; dried bundles are flat sprite planes with only their two broad faces, inset 0.75 px from every cell boundary, for this reason (0.2.0-dev.5; bundles.test.mjs guards it).
- **Previews are approximations.** `tools/preview-models.mjs` shades faces like Minecraft but has no
  ambient occlusion, light, shaders, animation or item extrusion; its GUI orientation follows the
  transform maths and has not been compared with a client. The owner's in-game check still decides.
- **A partial block that occludes makes the world see-through.** The herb bed (0.2.0-dev.3) had no
  `noOcclusion()`, so neighbours culled the faces it only partly covered (rails are 1.75 px thick,
  the model is hollow) and an x-ray strip showed in game. Every block that is not a full cube must
  say `noOcclusion()` in its properties (beds, rugs, stations, furniture, bundles all do; `noCollission()` already implies it);
  `BlockOcclusionGameTests` fails for any herbalwitch block that occludes without a full-cube
  occlusion shape.
- **Capabilities are invalidated on the old player at clone.** `DiscoveryEvents.clone` calls
  `reviveCaps()`, copies, then `invalidateCaps()` in a `finally`, because the original's capabilities are
  already invalidated when `PlayerEvent.Clone` fires. Without it a death or End return empties the grimoire.
- **Grimoire client state is a cache, not a store.** `GrimoireClientState` is cleared on disconnect (a stale
  index or discovery set must not carry into the next world) and rebuilt on recipe and tag updates; recipe
  bodies come from vanilla recipe sync, not our channel.
- **Herb conditions are server-only.** Clients never read `HerbConditionReloadListener.current()`; they use the
  herb summaries `ContentSync` sends with the editorial definitions.
- **Missing counts allocate over the main inventory and offhand only**, one variant per requirement, shared
  stacks counted once, returned containers never counted. Open containers and the ender chest do not count,
  and the same scope drives discovery observation.
- **Stale teaching invitations fail closed.** Acceptance names the teacher and recipe and checks the recipe
  fingerprint and the teacher's session, so a changed recipe, a relog or a closed teacher reader makes an old
  invitation do nothing; a content reload cancels the open invitations whose recipe vanished or changed.
- **A removed recipe loses its page, not the player's discovery key.** The key stays in the capability and the
  page returns if the recipe does.
- **Fake players never join the player list.** Range and teaching checks go through `TeachingWorld`, the test
  seam, so GameTests can drive them. `FakePlayerFactory` caches by name, so `GrimoireFixtures` gives each test a
  unique fake-player name; reuse leaks one test's inventory and discoveries into the next.
