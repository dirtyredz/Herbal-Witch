# The Herbal Witch

Forge 1.20.1 mod: woodland herbal witchcraft (herbs, stations, brewing, familiars, rituals, brooms,
an apothecary trade) together with a cottage furniture catalogue. Install on **both** the server and
every client.

**Status: private development, `0.2.0-dev.6` local candidate (the art pass).** Contains Milestone 1 (cottage decor
in a single design), Milestone 2 (herbs and garden) and Milestone 3 (everyday preparation stations and
products), all redrawn in 0.2.0-dev.1. Owner review of the art and station UIs is pending. Later systems are listed in
docs/FEATURES.md. Not published; not in any pack.

## Supported versions

| Component | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x (built and tested on 47.4.10) |
| Java | 17 |
| Required dependencies | none |

## Content

- **Cottage decor** (tab "Cottage Decor"): five pieces in one dark-wood design, with forest green,
  magenta and plum accents, gold trim and ivy. Armchair, Apothecary Counter, Small Wall Shelf, Table Lamp
  (use to switch on, light 14), Small Rug. Craft them from dark oak (docs/catalogue/decor.md).
- **Herbs and garden** (tab "Gardening"): 14 herbs in five habitat groups, seeds, fresh herbs and the
  herb bed (docs/catalogue/herbs.md). Herbs stand rooted in the soil with a 3D mature form, and
  adjacent herb beds join into one planter.
- **Apothecary** (tab "Apothecary"): six stations (the drying rack and hearth are 2x2 multiblocks; the new Witch's Stove is a two-wide
  range), dried bundles, powders, seven drinks, a salve,
  oils, garden products and a finite oil lantern (docs/catalogue/stations.md, products.md).

## Getting started in survival

1. **Seeds.** Craft starter seeds (wheat seeds plus a companion plant, below), or break grass, tall
   grass, ferns and large ferns by hand or tool (not shears, not silk touch): about 1 in 16 drops a
   starter seed, and a herb that grows in that biome sometimes turns up too.
2. **Plant** them on a herb bed or farmland. Herbs grow in four ages and need crop light; bone meal helps.
3. **Harvest** a mature plant with use (right click): 2 fresh herbs and 1 seed, and the plant resets.
4. **Drying rack.** Fresh herb in, bundle out after 1,200 ticks (60 s). No heat.
5. **Mortar and pestle.** Bundle in, powder out (100 ticks).
6. **Hearth and kettle.** Fuel the hearth with any furnace fuel; set the kettle on the hearth's chimney top (or on a Witch's Stove burner). Powder or
   bundle plus a water bottle makes a drink.
7. **Infusion pot** on a hearth for salves, oils and garden infusions; **salve**: yarrow and chamomile
   in the pot with a honey bottle and a bowl.

Starter seed recipes (shapeless): wheat seeds + lilac = lavender seeds; + fern = sage seeds;
+ sugar cane = mint seeds; + dandelion = chamomile seeds. The herb bed (`D D` / `SSS`: dirt, wooden
slabs) is farmland that never dries to dirt and is never trampled; it needs water within 4 blocks
(or rain) for the crop to count it as moist.

### Station recipes

| Station | Recipe (rows top to bottom) | Ingredients |
|---|---|---|
| Herb drying rack | `TST` / `s s` / `TST` | sticks (T), string (S), wooden slabs (s) |
| Mortar and pestle | ` T ` / `SFS` / ` S ` | stick, 3 smooth stone, flint |
| Tea kettle | `I C` / `III` / ` I ` | 5 iron ingots, 1 copper ingot |
| Hearth | `B B` / `BCB` / `B B` | 6 bricks, a campfire |
| Witch's Stove | `III` / `ICI` / `B B` | 5 iron ingots, a campfire, 2 bricks |
| Infusion pot | `IGI` / `B B` / `B B` | 2 iron ingots, glass bottle, 4 bricks |
| Oil lantern | `N N` / `NTN` / ` G ` | iron nuggets, torch, glass bottle |

The kettle and infusion pot need heat from a lit hearth, Witch's Stove, campfire, soul campfire or magma block directly
below (one hopper may sit between).

### Kettle (steeping): two herbs or powders plus a water bottle, 200 heated ticks

| Result | Ingredients |
|---|---|
| Recovery tea | chamomile + yarrow |
| Energy tea | mint + sugar |
| Restful infusion | lavender + valerian |
| Warmth remedy | rosemary + blaze powder |
| Cooling remedy | mint + snowball |
| Mining tonic | thyme + iron nugget |
| Travel tonic | mint + sugar cane |

### Infusion pot: two ingredients, a base liquid and a glass bottle or bowl, 600 heated ticks

| Result | Ingredients, base, package |
|---|---|
| Healing salve | yarrow + chamomile, honey bottle, bowl |
| Lantern oil | rosemary + resin, plant oil, glass bottle |
| Fishing blend | nettle + wheat seeds, plant oil, glass bottle |
| Growth infusion | nettle + bone meal, water bottle, glass bottle |
| Pest repellent | lavender + sage, plant oil, glass bottle |
| Soil tonic | moss block + hanging roots, water bottle, glass bottle |

Plant oil comes from the mortar (4 wheat seeds + glass bottle). Resin drops from player-broken spruce
leaves (about 1 in 8, not shears, not silk touch). Details and effects: docs/catalogue/products.md.

## Showcase command

`/herbalwitch showcase [section]` builds a labelled sample of every registered block, for owner review
and testing. It needs permission level 2 (operators; a non-op, in survival or creative, and the console
are refused). Sections: `decor`, `herbs`, `garden`, `drying_rack`, `mortar`, `kettle`, `hearth`,
`stove`, `infusion_pot`, `preparations`; with no section it builds all of them.

- **Where:** the box starts 2 blocks east of you on your row and extends east and south. The floor is
  the block you stand on. Height 8 (floor layer plus 7 cleared). The full layout is about 78 x 8 x 28;
  hard caps are 128 x 8 x 128 and 65,536 writes.
- **Destructive:** the whole box is replaced: terrain (grass, dirt, stone, plants, leaves, logs, water) is cleared (no drops) and the floor laid (grass blocks under the garden and herbs, smooth stone elsewhere), then the floor,
  supports, samples and signs are placed. There is no undo.
- **Refuses** before any write if a chunk is unloaded, the box leaves the world border or build
  height, you may not build there, the box holds
  a block entity, an unbreakable block or an entity. A second run over a built showcase is refused;
  use a fresh plot.

## Optional integrations

None are required, and no foreign mod classes are in the jar.

- **Farmer's Delight** (tested 1.3.4): the hearth heats FD cooking blocks; a lit FD stove heats our
  kettle and infusion pot.
- **Thirst Was Taken** (1.4.0): the seven drinks hydrate; water purity carries into the tea.
- **EMI** (1.1.24+1.20.1) and **JEI** (15.62.0.219): four recipe categories each.

Details: docs/COMPATIBILITY.md.

## Build, datagen, test

```
cd mods/HerbalWitch
node tools/generate-textures.mjs
node tools/generate-models.mjs
node tools/compile-blockbench.mjs
node tools/generate-test-structures.mjs
.\gradlew.bat runData
.\gradlew.bat test build
.\gradlew.bat runGameTestServer
.\gradlew.bat runGameTestServer -PcompatProfile=all -PcompatModsDir=<mods folder with FD and Thirst jars>
```

Gradle runs the three art producers before resources and datagen (`generateArt`) and their checks in
`check` (`checkArt`); this needs Node 22+ on PATH (or `-PnodeExecutable=<path>`).

`-PcompatProfile` is one of `base`, `emi`, `jei`, `viewers`, `farmersdelight`, `thirst`, `all`. The owner
trial mods folder is `C:/Users/dirty/MultiMC/instances/Evil Eye Trial Cozy/.minecraft/mods`. The jar is
`build/libs/herbalwitch-1.20.1-<version>.jar` (reobfuscated). CLAUDE.md has the full headless list.

### Model tools

- `node tools/generate-models.mjs` writes the procedural families: herbs, herb beds and hung bundles.
- `node tools/compile-blockbench.mjs` compiles the Blockbench projects under `src/art/blockbench/`
  (stations, furniture, oil lantern) into models and the two textures they own. `--check` fails when
  committed output is stale.
- `node tools/preview-models.mjs --output <dir outside the repo> [--references <dir>]` renders headless
  previews (contact sheets, herbs on beds, item sheet, review scenes, reference comparisons, manifest).
  Check it with `node --test "tools/preview/test/*.test.mjs"`. Shading is approximate (no ambient
  occlusion, world light, shaders, animation or mipmaps).

### Editing pieces in Blockbench

The one-off pieces are Blockbench projects (desktop Blockbench 5.2.2). The workflow is in
[docs/BLOCKBENCH.md](docs/BLOCKBENCH.md). To see edits in game without a new jar, run
`node tools/compile-blockbench.mjs --dev-pack "C:/Users/dirty/MultiMC/instances/Evil Eye Trial Cozy/.minecraft" --watch`
and press F3+T in the trial instance.

## Owner trial

Copy the jar into the trial instance's `mods/` folder (MultiMC: Evil Eye Trial Cozy) with the game
closed, then follow the review checklist in [docs/ART.md](docs/ART.md).

## License

MIT (LICENSE). All textures and models are original, generated or authored for this mod.
