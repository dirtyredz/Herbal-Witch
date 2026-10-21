# The Herbal Witch

Forge 1.20.1 mod: woodland herbal witchcraft (herbs, stations, brewing, familiars, rituals, brooms,
an apothecary trade) together with a coordinated cottage furniture catalogue. Install on **both** the
server and every client.

**Status: private development, Milestone 1 (`0.1.0-dev.1`).** This build contains only the furniture
art spike: five pieces in 18 finishes, to approve the visual system before mass production. The
witchcraft systems arrive in later milestones (docs/FEATURES.md). Not published; not in any pack.

## Supported versions

| Component | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.x (built and tested on 47.4.10) |
| Java | 17 |
| Dependencies | none |

## Milestone 1 content

Five pieces, each in all 18 finishes (90 blocks), in the **The Herbal Witch: Cottage Decor** creative tab:

| Piece | Behaviour |
|---|---|
| Armchair | Faces you when placed. Decorative for now (sitting comes with the seating family). |
| Apothecary Counter | Faces you when placed; its top holds a lamp. Decorative for now (shop counters come later). |
| Small Wall Shelf | Hangs on the wall you aim at (needs a solid wall face); drops if the wall goes. Decorative bottles. |
| Table Lamp | Needs a solid surface below (floor, counter). Use it to switch it on (light 14) or off. |
| Small Rug | Carpet behaviour: needs a block below, breaks instantly. |

**Finishes:** natural Dark Oak, Walnut, Cherry, Rosewood, Pale Oak, Spruce, Birch; painted/material
Black, Stone, Moss, Sage Green, Forest Green, Plum, Burgundy, Dusty Pink, Blush, Cream, Lilac. Each
finish pairs a frame with its own upholstery colour. Finishes are appearance only.

## Recipes

| Result | Recipe |
|---|---|
| Dark Oak Armchair | `PWP / PPP / S S`: dark oak planks, any wool, sticks |
| Dark Oak Apothecary Counter | `SSS / PNP / PPP`: dark oak slabs, planks, a gold nugget |
| 2 Dark Oak Small Wall Shelves | `SSS / T T`: dark oak slabs, sticks |
| Dark Oak Table Lamp | wool over a torch over a dark oak slab |
| 2 Dark Oak Small Rugs | `C S C`: two carpets (any colour) and string |
| Any other finish | Stonecutter: any finish of a piece becomes any other finish of that piece, 1 for 1 |

## Build, datagen, test

```
cd mods/HerbalWitch
node tools/generate-textures.mjs
node tools/generate-test-structures.mjs
.\gradlew.bat runData
.\gradlew.bat test build
.\gradlew.bat runGameTestServer
```

The jar is `build/libs/herbalwitch-1.20.1-<version>.jar` (reobfuscated). See CLAUDE.md for the full
headless verification list.

## Owner trial

Copy the jar into the trial instance's `mods/` folder (MultiMC: Evil Eye Trial Cozy) with the game
closed, then follow the art-review checklist in [docs/ART.md](docs/ART.md).

## License

MIT (LICENSE). All textures and models are original, generated or authored for this mod.
