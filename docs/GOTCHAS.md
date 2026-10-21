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
- **Every generated directory must stay under 12 entries** (`GeneratedResourcesTest`). Adding a
  per-finish file variant (like the lit lamp) needs its own directory level.
- **Sprites only span UV 0..16.** Templates are single-block and carry explicit UVs at one UV unit per
  model pixel (two texels at 32 px). Glass faces are the one exception: they stretch the whole rim
  over each face. The model test enforces both.
- **Tint index is the contract between models and `DecorColorHandlers`:** 0 frame, 1 fabric. A face
  with no tint index keeps its texture colours. Register block and item handlers together.
- **Cutout only where needed.** The shelf template is `minecraft:cutout` for its bottle glass; the
  glass texture's alpha is strictly 0 or 255.
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
  over a built showcase is refused by the preflight; use a fresh flat plot.
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
