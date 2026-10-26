# The Herbal Witch

Forge 1.20.1 mod: woodland herbal witchcraft (herbs, stations, brewing, familiars, rituals, brooms,
an apothecary trade) together with a cottage furniture catalogue. Install on **both** the server and
every client.

**Status: private release candidate `1.0.0-rc.3`: additive guide Phase 1; owner in-game acceptance is pending. Not published; not in any pack. Protocol 14.**

Install **GuideME 20.1.15** (`guideme-20.1.15.jar`) on both sides. Hover lavender or valerian,
their prepared forms, a Restful Infusion or the drying rack/mortar/kettle/stove/hearth in an
inventory and **hold G** to open that item's page. Follow its links into "My first useful herb",
use live recipe cards, inspect the rotatable rack and arrange the kettle over the stove to make
your first infusion. Every topic links to the start page and lesson. No AE2, Create or Ponder is required.

Optionally install **Ponder 1.0.91 and Flywheel 1.0.5** on the client for the animated first
preparation: choose **Watch** in the lesson or hold **W** over an associated item. Without them,
Watch is hidden and all written instructions remain available. See [compatibility](docs/COMPATIBILITY.md).

The existing grimoire still opens normally for discoveries, research, teaching and the complete
reference. Phase 1 adds public instructions without changing book access or saved knowledge.

## The loop

Forage or grow fourteen herbs (seeds from grass, wild patches by habitat, herb beds with moisture and
soil tonic), dry them on the drying rack, grind them in the mortar, steep teas in the kettle over a
hearth, stove or campfire, and prepare salves and oils in the infusion pot. Read the grimoire for every
recipe, its station and what you are missing. Anyone can do all of that and buy a witch's goods.

Becoming a witch: the first player founds the world's lineage at the ritual altar; afterwards a witch
initiates a consenting player (operators can designate). Witches have an energy bar that grows with
practice and recovers by resting, meditating, sleeping and herbal tea (more in nature and under a full
moon). Energy powers the advanced stations (cauldron, copper still, bottling bench, charm workbench),
the ritual circle (Garden Blessing, Home Protection), ward stones, charms, familiar powers, the wand and,
if configured, broom flight.

Animals: herbal baits, calming powder, earned taming, healing salve and capture charms for safe
transport. Familiars: bind up to any number of trusted cats, ravens (and crows), owls, foxes, wolves,
snakes, rabbits and bats at the altar; give them homes (bed, nest, perch), seven commands (Follow,
Stay, Guard Witch, Protect Home, Scout, Passive, Return Home), equipment and eleven preparations; a
fallen familiar is revived at the altar. Tools: a broom stand crafts and repairs flying brooms; a wand
bench carves a wand with seven selectors. Commerce: a register on an apothecary counter, stock shelves
and jars, an order board, and villager customers.

**Controls:** broom: movement keys, Jump (up), Left Control (down), R (land), Sneak (dismount); wand: V
selects the mode. All are remappable. Commands (operators): `/herbalwitch showcase [group|section]`,
`/herbalwitch witch designate|inspect|founding ...`, `/herbalwitch capture ...`, `/herbalwitch shop ...`.

Reference: [features](docs/FEATURES.md), catalogues for [herbs](docs/catalogue/herbs.md),
[stations](docs/catalogue/stations.md), [products](docs/catalogue/products.md),
[witchcraft](docs/catalogue/witchcraft.md), [animals](docs/catalogue/animals.md),
[familiars](docs/catalogue/familiars.md), [tools](docs/catalogue/tools.md),
[commerce](docs/catalogue/commerce.md), [decor](docs/catalogue/decor.md);
[compatibility](docs/COMPATIBILITY.md).

## Installing and upgrading

Drop the jar into `mods/` on the server and every client (Minecraft 1.20.1, Forge 47.x, Java 17). Worlds
saved by any earlier private build open as they are: the release qualification opened the owner's real
0.2.0-dev.9 and 0.6.1 worlds and checked every block, block entity and player capability
([save compatibility](docs/qualification/save-compatibility.md)). There is no downgrade. Keep the
world's `data/` folder (capture ledger, familiar and shop shards) with its region and player files in
backups; restoring some files without the others is outside the guarantee.

## Qualification

[Release qualification](docs/qualification/README.md): design audit, save upgrades, restart and crash
boundaries, production dedicated server with optional mods and KubeJS, performance, localisation, art,
the [owner acceptance checklist](docs/qualification/owner-acceptance.md) and what is
[not release-ready](docs/qualification/release-readiness.md).

## Supported versions

| Component | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x (built and tested on 47.4.10) |
| Java | 17 |
| Required dependencies | none |

## Content

- **Cottage decor** (tab "Cottage Decor"): a 94-piece furniture catalogue in one dark-wood design, with
  forest green, magenta and plum accents, gold trim and ivy. Seven families: seating 13, storage 27,
  surfaces 15, lighting 8, textiles 6, fixtures 22 and openings 3. Every piece is crafted from dark oak
  and common vanilla items; the exact list, sizes, grids and mounting are in docs/catalogue/decor.md.
  The five 0.1.0 pieces (Armchair, Apothecary Counter, Small Wall Shelf, Table Lamp, Small Rug) are part of it.
  - *Placement:* hold the item and aim. A see-through preview of the whole piece with a green or red
    outline shows exactly where it will go; the whole piece is placed or nothing is. Wall pieces face away
    from the wall you aim at, and hanging pieces hang from the ceiling you aim at.
  - *Seating:* use a seat to sit on the seat you aim at; one sitter per seat.
  - *Storage:* cabinets hold 27 or 54 slots and are one shared inventory across all their cells; hoppers
    and other item handlers reach cabinets. Display shelves show one item per slot, drawn where it stands.
  - *Beds:* vanilla sleeping and respawn rules, one sleeper per bed (the canopy bed included).
  - *The door:* behaves like a vanilla door (open, close, redstone).
  - *Lights:* use to toggle; lit lights give light 14 and need no fuel.
  - Some pieces look like stations (work table, altar table, decorative drying rack, kitchen range,
    fireplace, firepit) but are decoration only (docs/catalogue/stations.md).
- **Herbs and garden** (tab "Gardening"): 14 herbs in five habitat groups, seeds, fresh herbs and the
  herb bed (docs/catalogue/herbs.md). Herbs stand rooted in the soil with a 3D mature form, and
  adjacent herb beds join into one planter.
- **Apothecary** (tab "Apothecary"): six everyday stations (the drying rack and hearth are 2x2 multiblocks; the new Witch's Stove is a two-wide
  range; the four advanced stations are below), dried bundles, powders, seven drinks, a salve,
  oils, garden products and a finite oil lantern (docs/catalogue/stations.md, products.md).
- **Witchcraft** (tab "Apothecary"): the ritual altar, the founding of the first lineage, initiation of
  consenting players, and the energy a witch recovers by resting, meditating, sleeping and drinking
  energy tea (see "Witchcraft" below; docs/catalogue/witchcraft.md).
- **Advanced brewing** (tab "Apothecary"): four witch stations (Witch's Cauldron, Copper Still, Bottling
  Bench, Charm Workbench), 16 advanced preparations (night vision, water breathing, resistance and healing
  draughts; poison, concealment, slowing, binding, weakening and sleep vials; venom, thorn and two
  repellent oils; blinding dust; moon essence), four-dose mixtures, and ten preparations for familiars that
  have their M9 consumer installed (see docs/catalogue/familiars.md).
- **Charms, the ritual circle and wards** (tab "Apothecary"): protective charms, a ring that unlocks the
  Garden Blessing and Home Protection rituals, and the ward stone (see "Charms, circle and wards" below).
- **Broom and wand** (tab "Apothecary"): two crafting stations, repairable owner-bound flight, and
  seven utility selectors. The grimoire explains acquisition, live costs, control bindings and the
  original ritual/familiar controls (see [tools](docs/catalogue/tools.md)).
- **Grimoire** (tab "Apothecary"): a portable book, a Grimoire Stand and a Witch's Bookshelf. They open the
  same recipe-backed reader (see "The Grimoire" below).

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

## Advanced brewing

The Witch's Cauldron, Copper Still, Bottling Bench and Charm Workbench are placed like the altar (one
item, previewed, checked) and show an `active` state while working. They share the processing rules of the
everyday stations (docs/catalogue/stations.md), with one difference: a recipe that needs a witch.

- **Witch recipes:** nothing is reserved or runs until a witch presses **Start** on the station's screen.
  The player who pressed it pays; progress continues only while they are online, alive, in the dimension
  and within 8 blocks, and **Resume** continues a paused batch. The energy is spent once, when the batch
  completes, and earns practice. **Cancel** consumes nothing. Others cannot take a batch over and can
  cancel it only once the payer's lease has ended. The recipe viewers and the grimoire show the witch
  requirement.
- **Heat:** the cauldron (600 ticks, a water base) and the still (800 ticks) need a heat source directly
  below, like the kettle. The bench (100 ticks) needs neither a witch nor heat; the workbench (200 ticks)
  needs a witch.
- **Mixtures:** the cauldron makes a four-dose mixture of a draught, a vial or a familiar tonic; the bench
  portions a whole mixture into four bottles. With gunpowder and paper the bench makes labelled vials,
  which are the thrown form.
- **Draughts:** Night Vision and Water Breathing (2:00), Resistance I (1:30) and Healing (3 hearts when
  hurt, one every 10 seconds).
- **Vials and dust (thrown):** Poison, Concealment, Slowing, Binding, Weakening, Sleep and Blinding Dust. A
  throw bursts in radius 3, reaches at most 32 targets with line of sight, and shares a 20-tick throw
  cooldown. Binding and Sleep hold a creature for 5 seconds and are refused by bosses and other
  hard-control-immune creatures; a creature just held gets Steadfast (100 ticks). Players are never
  affected unless `preparationPvp` and the server allow it; villagers and wandering traders are protected.
- **Oils:** Venom coats the blade in your off hand (8 poisoning hits or 1:00), Thorn pricks whoever strikes
  you (1:30), Undead Repellent and Arthropod Repellent make those creatures stop seeking you (1:30). Witch
  recipes at the infusion pot; Blinding Dust is a witch recipe at the mortar.
- **For familiars:** ten more preparations (Guardian's Brew, Swiftpaw Tonic, Nightwatch Infusion, Moonveil
  Elixir, Spiritbond Potion, Hearthkeeper's Brew, Shadowstep Draught, Warding Essence, Venomleaf Extract,
  Emberguard Oil) apply to owned bonded familiars. Their timers, effects and costs are documented in
  [the familiar reference](docs/catalogue/familiars.md).

## Charms, circle and wards

- **Charms (Charm Workbench):** the Protective Sachet (every mob, 15% softer, at most 1 heart), the Undead
  Amulet and the Arthropod Amulet (30%, at most 1.5 hearts). Worn in the off hand, or in a Curios `charm`
  slot when Curios is installed. A charm lasts 300 seconds of wearing, then breaks.
- **Ritual circle:** shapeless, 4 smooth stone slabs, 2 calcite, an amethyst shard and a gold nugget. One
  item places a 5x5 ring of 16 cells around the altar, with the same preview and checks as the altar. Its
  rituals need the ring complete; if it breaks, the ritual stops and nothing is consumed.
- **Garden Blessing:** nettle powder, bone meal and soil tonic; 15 energy, 200 ticks. Look at a herb bed,
  then open the altar: the herb beds in the 5x5 square around it (within 8 blocks) are enriched and the
  bottle comes back.
- **Home Protection:** sage powder and an amethyst shard; 20 energy, 200 ticks. Look at a ward stone, then
  open the altar: the stone is charged and the witch may bind it.
- **Ward stone:** shapeless, 4 stone bricks, an amethyst shard and a gold ingot. A witch within 8 blocks
  can activate it directly for 20 energy; it stays charged for 600 seconds of loaded time and cannot be
  topped up while charged. It keeps the undead, arthropods or both (the owner chooses) from targeting
  anyone inside a box: radius 8 (12 with a working hearth or Witch's Stove within 4 blocks) by height 4.
  Only the owner can deactivate it, release it (unbind) or change radius and profile. A witch may hold 4;
  active wards cannot overlap.
- **Rules:** the numbers above are defaults of `herbalwitch-server.toml` (sections `workings`, `wards`,
  `charms`); the grimoire quotes the live values.

## The Grimoire

A book of every herb, station and processing recipe in the mod, with your own discoveries marked. It is
advisory: nothing in the game is locked behind it, and the recipe viewers (EMI, JEI) still show everything.

- **Crafting (shapeless):** book + ink sac + any starter fresh herb = Grimoire. 3 dark oak slabs + 2 sticks +
  a Grimoire = Grimoire Stand. 6 dark oak planks + 2 books + a Grimoire = Witch's Bookshelf. The Grimoire item is
  used up in the block recipes; your knowledge never is.
- **Opening it:** use the Grimoire (stack of one, no stored knowledge: it opens the holder's own). The stand opens on
  the research filter; the bookshelf and the book open on the index. A block reader closes if you move more than 8
  blocks away or the block goes.
- **Discovery:** carrying a herb (any form) discovers that herb. Carrying a recipe's result, or being familiar
  with all its primary ingredients, discovers the recipe. Only the main inventory and offhand count. Knowledge is
  personal to each player, kept through death and relogging, and others never see it.
- **Research:** at a stand or bookshelf, one familiar primary ingredient is enough to research a recipe. It is
  instant, free and consumes nothing.
- **Missing ingredients:** each recipe page shows what you carry against what it needs (water bottles and returned
  containers included).
- **Teaching:** from a research block reader, invite a player within 8 blocks to learn one recipe you know. They
  get a chat notice and accept or decline from their book or a research reader; invitations expire after 30
  seconds and nobody is taught without accepting.
- **At a station:** processing stations, the hearth and the Witch's Stove show a "Grimoire" button while you carry a
  Grimoire. It opens a book panel beside the station on that station's recipes (or heat); it shows no missing counts,
  so open the grimoire for those.
- **Editorial pages:** guide text lives in data packs (`data/<ns>/grimoire_entries/`), prose only; recipe pages always
  come from the actual recipes.

## Witchcraft

Energy is the budget witchcraft spends. A witch has a capacity (100 to start, growing with
practice) and an energy level; non-witches have neither. The altar's rituals, witch recipes at the
advanced stations, the circle rituals and ward activation spend energy; familiar binding, recall, upgrades, revival and powers do too.

- **The altar:** `SAS` / `LGL` / `L L`: 2 smooth stone slabs (S), an amethyst shard (A), 4 dark oak
  logs (L), a gold ingot (G); Apothecary tab, broken fastest with an axe. One item places the whole
  altar (2 wide, 1 deep, 2 high) through the same previewed, checked placement as furniture. Both
  bottom cells need a sturdy floor.
- **Founding (the first witch):** nobody starts as a witch. One player founds the lineage at an altar
  while carrying fresh lavender, fresh sage, fresh mint, fresh chamomile, an amethyst shard and any
  written book (all consumed). The ritual runs 200 ticks with the altar open, in range and unhurt. The
  founder becomes a witch, receives one Grimoire (an empty main slot is required, otherwise nothing is
  consumed) and starts with full energy. Once any player has become a witch, founding is closed.
- **Initiation:** a witch at an altar invites a nearby player who can also use that altar. The recipient
  gets a chat line, opens the same altar and chooses "Accept and begin" or "Decline". The initiator
  brings fresh sage, fresh lavender and an amethyst shard and pays 50 energy; the recipient pays
  nothing. Both keep the altar open for 200 ticks. Nobody is initiated without accepting, and
  cancelling or interrupting consumes nothing. The initiator gains 50 practice.
- **Recovering energy:** resting still (1 per 100 ticks), meditating at an idle altar (2 per 20 ticks
  after a 40-tick settle, stopping at full), a completed night's sleep (+30, once per day) and a cup of
  energy tea (+25, one cup per 1,200 online ticks). A restful infusion prepares the next rewarded sleep
  for +30 more. Resting and meditating in nature biomes or under a full moon recover up to 25% more.
  Zero energy never harms a witch; paid actions are refused with a reason.
- **Practice and capacity:** an attunement (one fresh sage and 10 energy, 100 ticks) is solo practice.
  Ten practice thresholds (50 to 500) each add 10 capacity, to a final 200.
- **The HUD:** witches see an energy bar and number. `herbalwitch-client.toml` `[hud]`: `enabled`
  (true), `anchor` (nine-way, `BOTTOM_LEFT`), `offsetX` (8), `offsetY` (18, clears the chat input),
  `scale` (1.0, range 0.25 to 4), `numeric` (true), `recoveryIndicator` (true).
- **Rules:** the numbers above are defaults of `herbalwitch-server.toml` (per world). The grimoire's
  Witchcraft pages quote the live values. A valid edit cancels open invitations and running rituals.
- **Operators (permission level 2):** `/herbalwitch witch inspect <player>`; `/herbalwitch witch
  designate <player>` (an invitation with no initiator, offering or energy, accepted at any valid
  altar); `/herbalwitch witch founding inspect`; `/herbalwitch witch founding resolve <claim> close`
  (for a founding record flagged for review after a crash; see docs/catalogue/witchcraft.md).

## Showcase command

`/herbalwitch showcase [furniture|witchcraft|section]` builds a labelled sample of every registered block, for owner review
and testing. It needs permission level 2 (operators; a non-op, in survival or creative, and the console
are refused). Sections: `decor`, `herbs`, `garden`, `drying_rack`, `mortar`, `kettle`, `hearth`,
`stove`, `infusion_pot`, `preparations`, `grimoire`, `altar`, `cauldron`, `still`, `bottling_bench`,
`charm_workbench`, `advanced_preparations`, `ritual_circle`, `ward_stone`, `charms`. Two showcases are built separately:
`furniture` (the `decor` section: the 94 pieces) and `witchcraft` (every other section: herbs, stations, products, magic
and abilities). `/herbalwitch showcase furniture` or `... witchcraft` builds one; with no argument both are built, the
witchcraft one 8 blocks behind (south of) the furniture one with open ground between; a section name builds that section
alone. Each is checked before anything is written. The grimoire
  section (both research blocks, a barrel with two grimoires and supplies, signs) grants no knowledge.
The `altar` section (an idle altar, a supply barrel and signs) initiates nobody, touches no founding
record, grants no energy and starts no ritual.
The `decor` section is a footprint-aware grid of all 94 pieces with a sign beside each, with wall pieces on walls and
hanging pieces under ceilings, lights lit, storage stocked and the door in its four states.

- **Where:** the first box starts 2 blocks east of you on your row and extends east and south. The floor is
  the block you stand on. Height 8 (floor layer plus 7 cleared). Sections pack into rows no wider than 126. The
  furniture showcase is 43 x 8 x 28 and the witchcraft showcase 126 x 8 x 60 in two rows (63,193 writes), including sixteen live animal habitats (the single
  showcase was 121 x 8 x 38 before the split); hard caps per box are 128 x 8 x 128 and 65,536 writes.
- **Signs:** a sign for a block stands on the floor in the block's own front row, two blocks east of its footprint (your
  left as you face it), leaving one clear block like Roundwood, so it never hides the block; the block north of every sign stays open. Notes that
  annotate no block stand in a separate clear row with nothing behind them, and each section's heading has an open
  row below it. This holds for multiblock and wall-mounted pieces (the sign is at the piece's floor-level west edge).
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
- **EMI** (1.1.24+1.20.1) and **JEI** (15.62.0.219): the processing categories plus the ritual
  categories, from the same descriptions the grimoire uses; they show every recipe regardless of
  discovery, and mark the ones that need a witch.
- **Curios** (compile-only API 5.14.1): charms also fit a `charm` slot when it is installed. Without it
  they work from the off hand.
- **Claim mods:** FTB Chunks, Open Parties and Claims, Cadmus, Flan, GriefDefender and MineColonies are
  recognised; while one is loaded, harmful preparations and wards on entities are restricted.

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

`-PcompatProfile` is one of `base`, `emi`, `jei`, `viewers`, `farmersdelight`, `thirst`, `curios`, `doggy`, `all`. The jar is
`build/libs/herbalwitch-1.20.1-<version>.jar` (reobfuscated). CLAUDE.md has the full headless list.

### Model tools

- `node tools/generate-models.mjs` writes the procedural families: herbs, herb beds and hung bundles.
- `node tools/compile-blockbench.mjs` compiles the Blockbench projects under `src/art/blockbench/`
  (stations, furniture, oil lantern, the grimoire stand and bookshelf, the altar, the ritual circle and the ward stone) into models and the textures each piece owns. `--check` fails when
  committed output is stale.
- `node tools/preview-models.mjs --output <dir outside the repo> [--references <dir>]` renders headless
  previews (contact sheets, herbs on beds, item sheet, review scenes, reference comparisons, manifest).
  Check it with `node --test "tools/preview/test/*.test.mjs"`. Shading is approximate (no ambient
  occlusion, world light, shaders, animation or mipmaps).

### Editing pieces in Blockbench

The one-off pieces are Blockbench projects (desktop Blockbench 5.2.2). The workflow is in
[docs/BLOCKBENCH.md](docs/BLOCKBENCH.md). To see edits in game without a new jar, run
`node tools/compile-blockbench.mjs --dev-pack "<mod-test instance>/.minecraft" --watch`
and press F3+T in that instance.

## Owner trial

Mod testing happens on a dedicated mod-test instance holding a copy of the Evil Eye pack plus the build
under test (owner ruling 2026-10-22; the workspace session owns it, see the workspace CLAUDE.md). Never
install into an `Evil Eye Trial *` instance. Copy the jar into the mod-test instance's `mods/` folder
with the game closed, then follow [the owner acceptance checklist](docs/qualification/owner-acceptance.md), the art review in [docs/ART.md](docs/ART.md) and the Grimoire
checks in docs/FEATURES.md.

## License

MIT (LICENSE). All textures and models are original, generated or authored for this mod.

## Multiple familiars

Build trust with matching animal bait, then bind an eligible adult at the altar and circle. Open
your grimoire roster to choose a command or recall a loaded familiar. Nearby detail pages manage
one species accessory, one charm, cargo, observed power targets and prepaid guard reserve.
Use a grimoire on an owned home to assign its compatible resident. Friendly visitors are never
automatic targets. Missing or unloaded creatures never trigger replacement spawning.

Lethal damage retains an incapacitated bond for paid revival. Ambiguous interrupted transactions
remain quarantined; operators can inspect them with /herbalwitch familiar. The full acquisition,
command, crafting and recovery reference is [familiars.md](docs/catalogue/familiars.md).

The witchcraft showcase contains 26 appearances, including nine inert familiar displays and the inert
commerce villager, three functional homes and familiar supplies. Its measured bounds are 126 x 8 x 60; furniture
is 43 x 8 x 28. All labels retain the sign-beside-block rule.

## Release

The version lives only in `gradle.properties`. Prepare non-empty `docs/releases/<version>.md`
for a release, then obtain the owner's go-ahead to push and land on main. Push CI runs
`./gradlew test build verifyReleaseJar` plus `runData` and generated-resource drift detection; it publishes nothing.
After green main CI and owner authorization, the agent dispatches
`gh workflow run release.yml --ref main`. The workflow verifies again, requires successful Forge GameTests, builds
the final jar, creates its version tag, uploads a draft, reads every asset back and compares
SHA-256 before publishing the jar, its `.sha256` and `herbalwitch.pw.toml`.
The pack session consumes the published `.pw.toml`; the owner never publishes by hand.
Versions starting with `0.` or containing `-` are prereleases. Existing releases/tags are
refused, published bytes are never replaced, and stable latest never moves backward.
A failed draft requires deliberate recovery before retrying.

Local fast gate: `./gradlew --offline test build verifyReleaseJar` with cached dependencies.
Shared files are checked from the workspace with `tools/sync-mod-files.ps1 -Check -Mod <this-worktree>`.

CI enables datagen and Forge GameTests. Existing `generateArt`/`checkArt` wiring remains active
through resource processing, `runData` and `check`. Run `./gradlew --offline runData checkArt`
and confirm `git status --short -- src/generated src/main/resources` stays empty.
Qualification phases retain the shared `gameTestWorld(File)` call; long restart/profile
qualification stays on ANU and owner in-game acceptance remains required.

Clean hosted clones resolve the exact approved GuideME 20.1.15 CurseForge file (1173950/8204776).
For offline local builds, supply the existing jar in `build/guide-inputs` or use
`-PguideMeDir=<existing-jar-directory>`; no dependency is bundled. This rollout downloads nothing
locally. The release candidate is still private and unapproved for publication.
