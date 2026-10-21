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
