# Herb catalogue

Authoritative table of the 14 herbs (Milestone 2). Ids are `herbalwitch:botany/<group>/<herb>/{plant,seeds,fresh}`
and permanent. Groups are the habitat: temperate, dry, wet, shade, woodland. Module: `botany/` (docs/ARCHITECTURE.md).

## Herbs

| Herb | Id path | Group | Form | Traits | Starter | Discovery chance | Liked conditions | Worldgen rarity | Art reference |
|---|---|---|---|---|---|---|---|---|---|
| Lavender | `botany/temperate/lavender/` | temperate | bush | calming, attraction | yes | 1/32 | none | 1 per 8 chunks | `models/block/botany/temperate/lavender/` (young_0..2, mature) |
| Sage | `botany/temperate/sage/` | temperate | bush | protective, restorative | yes | 1/32 | none | 1 per 8 chunks | `models/block/botany/temperate/sage/` (young_0..2, mature) |
| Chamomile | `botany/temperate/chamomile/` | temperate | crop | calming, restorative | yes | 1/32 | none | 1 per 8 chunks | `models/block/botany/temperate/chamomile/` (young_0..2, mature) |
| Yarrow | `botany/temperate/yarrow/` | temperate | crop | restorative, protective | no | 1/32 | none | 1 per 8 chunks | `models/block/botany/temperate/yarrow/` (young_0..2, mature) |
| Rosemary | `botany/dry/rosemary/` | dry | bush | protective, sight | no | 1/32 | none | 1 per 8 chunks | `models/block/botany/dry/rosemary/` (young_0..2, mature) |
| Thyme | `botany/dry/thyme/` | dry | bush | protective, growth | no | 1/32 | none | 1 per 8 chunks | `models/block/botany/dry/thyme/` (young_0..2, mature) |
| Mint | `botany/wet/mint/` | wet | bush | restorative, speed | yes | 1/32 | rain | 1 per 8 chunks | `models/block/botany/wet/mint/` (young_0..2, mature) |
| Nettle | `botany/wet/nettle/` | wet | crop | growth, restorative | no | 1/32 | rain | 1 per 8 chunks | `models/block/botany/wet/nettle/` (young_0..2, mature) |
| Valerian | `botany/shade/valerian/` | shade | crop | calming, attraction | no | 1/32 | low sky (covered) | 1 per 16 chunks | `models/block/botany/shade/valerian/` (young_0..2, mature) |
| Mugwort | `botany/shade/mugwort/` | shade | crop | sight, binding | no | 1/32 | low sky (covered) | 1 per 16 chunks | `models/block/botany/shade/mugwort/` (young_0..2, mature) |
| Belladonna | `botany/woodland/belladonna/` | woodland | bush | toxic, sight | no | 1/64 | night, full moon | 1 per 32 chunks | `models/block/botany/woodland/belladonna/` (young_0..2, mature) |
| Nightshade | `botany/woodland/nightshade/` | woodland | bush | concealment, binding | no | 1/64 | night, full moon | 1 per 32 chunks | `models/block/botany/woodland/nightshade/` (young_0..2, mature) |
| Wolfsbane | `botany/woodland/wolfsbane/` | woodland | crop | toxic, protective | no | 1/64 | night, full moon | 1 per 32 chunks | `models/block/botany/woodland/wolfsbane/` (young_0..2, mature) |
| Mandrake | `botany/woodland/mandrake/` | woodland | crop | binding, growth | no | 1/64 | night, full moon | 1 per 32 chunks | `models/block/botany/woodland/mandrake/` (young_0..2, mature) |

## Look (0.2.0-dev.2)

Herbs are rooted in the soil. Every age is a 3D model of small cubes sampling the 32x32 swatch atlas
`textures/block/botany/parts.png` (owner ruling: no flat crossed sprites). The mature age (3) is
authored per herb, `block/botany/<group>/<herb>/mature`; ages 0-2 (`young_0..2`) are derived from it
by tools/models/botany/plants.mjs: the same plant scaled down toward its root, thinned, flowers absent
at age 0, green buds at age 1 and partly open at age 2. Particles use the fresh-herb sprite. Every
age's lowest geometry meets the bed's soil surface (soil top 14 = plant block y -2) and sits 1 px into
farmland and 2 px into full ground.

| Herb | Mature form |
|---|---|
| Lavender | Purple flower spikes over a leafy mound |
| Sage | Broad grey-green leaves with purple spikes |
| Chamomile | Low bushy mound with white daisies, yellow centres |
| Yarrow | Flat white umbels |
| Rosemary | Upright needled stems with pale flowers |
| Thyme | Dense low cubic-leaf bush with pink flowers |
| Mint | Paired leaves on upright stems, lilac tips |
| Nettle | Upright stems with drooping dark leaves |
| Valerian | Pale pink-white flower heads |
| Mugwort | Silver-green divided foliage with brown tips |
| Belladonna | Bushy dark leaves, shiny black berries, purple bells |
| Nightshade | Dark leaves with red berry clusters |
| Wolfsbane | Spikes of blue-purple hooded flowers |
| Mandrake | Basal rosette of long leaves, root crown, small purple flowers |

## Herb bed

Dark wooden planter (the armchair's wood): two horizontal plank rails per side with a groove, chunky
corner posts with end-grain caps and small gold rivets, lumpy soil at height 14 of 16: dusty
grey-brown when dry, near-black and wet when moist (any moisture above 0), on joined beds too. **Beds join:** a
side touching the same herb bed block at the same height shows soil instead of a rail; outer corners
keep a capped post, one-sided corners continue the rail, both-joined corners are soil. Moisture and
enrichment are unchanged. Old beds saved before 0.2.0 repair on their normal random tick.

## Habitat biomes (default)

Biome tags `herbalwitch:botany/habitat/<group>` drive both foraging and worldgen; packs can retune them.

| Group | Biomes |
|---|---|
| temperate | plains, sunflower plains, forest, flower forest, birch forest |
| dry | savanna, savanna plateau, windswept savanna |
| wet | swamp, mangrove swamp, river |
| shade | taiga, old-growth pine taiga, old-growth spruce taiga, dark forest |
| woodland | dark forest, old-growth pine taiga, old-growth spruce taiga |

## Shared rules

- **Growth:** ages 0..3 by vanilla random growth; growth needs crop light, but plants survive without
  light so worldgen can place them. Bone meal adds 1 to 2 ages, capped. Seeds plant only on farmland or
  the herb bed; wild patches stand on `minecraft:dirt`-tag soil.
- **Harvest yield:** use on a mature plant gives 2 fresh herbs + 1 seed. Crops reset to age 0, bushes to
  age 1. +1 fresh at 25% when favoured (habitat biome or a liked condition met); +1 fresh if the herb
  bed is enriched by a soil tonic (then cleared). Breaking gives 1 seed, and a mature plant also 2 fresh.
- **Starters:** wheat seeds + lilac, fern, sugar cane or dandelion craft lavender, sage, mint or
  chamomile seeds. Grass foraging drops starter seeds at 1/16.
- **Discovery:** breaking grass, tall grass, fern or large fern by a player (no shears, no silk touch)
  also picks one herb whose habitat tag contains the biome, at its discovery chance, doubled per liked
  condition met, capped at 1/8. Values live in `data/herbalwitch/herb_conditions/<group>/<herb>.json`.
- **Worldgen:** a random patch (8 tries, spread 4) of mature plants on air above `minecraft:dirt`-tag
  blocks, at the group's rarity, via per-group biome modifiers. No retrogen.
- **Progression:** every herb stays cultivable once acquired; conditions only improve discovery or yield.
- **Supplements** (not herbs): vanilla mushrooms, moss, roots, flowers, minerals, and resin.
