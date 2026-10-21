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
- **Models are generated: edit tools/models/, not the JSON.** `tools/generate-models.mjs` owns the
  station, decor-template, herb and herb-bed model directories and deletes any JSON there it did not
  write. UVs are position-based (one unit per model pixel, v = 16 - y on sides), so a motif painted
  at texture row r shows at model height 16 - r/2: move the motif, not the UV. Decal textures
  (ivy, runner, cloth, plaque, rug, flame, glass, bundles) stretch instead; `TemplateChecks`
  knows the list.
- **Faces may not span more than one texture.** A face longer than 16 pixels cannot have position
  UVs; multiblock pieces are split per cell (and the item assembly is the split pieces put back).
- **Rotations are one axis, 0 / +-22.5 / +-45 degrees.** A rotated element's `from`/`to` is checked
  unrotated; after rotation it may lean slightly out of its cell (the rack legs, the pestle).
- **Cutout only where needed.** A model is `minecraft:cutout` exactly when it uses an alpha texture
  (glass, ivy, runner, cloth, flame, bundles); those textures' alpha is strictly 0 or 255.
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
  different UVs, so the tops flickered in game (0.2.0-dev.1). Octagons now tile rows, chamfer walls
  sit 0.1 px inside an exposed top, and the z-fighting sweep (`tools/preview/report/zfighting.mjs`,
  run by the node tests and the previewer) fails on any same-facing overlap closer than 0.02 px,
  per model and per blockstate combination. Flush decorations need a real offset (0.1 px or more).
- **A colour change can erase a state.** The 0.2.0 soil restyle made dry soil dark too, so moist
  soil (only a shade darker) became indistinguishable and "watered" vanished although the blockstate
  still switched. Keep state-carrying textures far apart; `BotanyModelsTest` checks the soil gap.
- **A rotated leg pivots about its origin, not its foot.** Rack legs rotated about the cap swung
  their lower ends off the feet; pivot a leaning part where it must touch (its foot) and let the
  free end land where it may.
- **Stretched sprites on thin boxes leak.** A whole sprite squeezed onto a 1 px side or top face
  shows as coloured streaks next to the host block; hung bundles are 3D cubes for this reason.
- **Previews are approximations.** `tools/preview-models.mjs` shades faces like Minecraft but has no
  ambient occlusion, light, shaders, animation or item extrusion; its GUI orientation follows the
  transform maths and has not been compared with a client. The owner's in-game check still decides.
