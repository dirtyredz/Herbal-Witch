# Product catalogue

Traces every product category in the design (everyday, combat, animal care, familiar preparations) and the 14 named herbs to station, delivery, witch requirement, effect and milestone. Milestone 3 and 7 rows are done and carry the exact effect (the recipes are the generated JSON under `data/herbalwitch/recipes/processing/`, listed by the grimoire); M8 animal products are a qualification candidate below. Other rows are planned. "Planned effect" is the design intent plus plan section 7 duration defaults.

Duration defaults: utility 60-120 s, combat control 5-10 s, familiar buffs 180 s. Same effects refresh, not stack. Sleep wakes on damage; bosses and protected targets are immune to hard control by default. Harmful player targeting is off by default (PvP). Stations: see stations.md. Non-witches can buy and use finished products; "Witch-only?" refers to making them.

## Everyday products

| Product | Category | Station | Delivery | Witch-only? | Planned effect | Milestone | Status |
|---|---|---|---|---|---|---|---|
| Healing salves | healing | Infusion pot | salve | no | usable only when hurt: +4 health, 32 ticks, 10 s cooldown, bowl back. Infusion pot: yarrow + chamomile, honey bottle, bowl (600 ticks) | 3 | done |
| Recovery teas | healing | Tea kettle | drink | no | Regeneration I 90 s. Kettle: chamomile + yarrow + water bottle (200 ticks) | 3 | done |
| Energy-restoring teas | energy | Tea kettle | drink | no (use needs a witch to benefit) | food 4 / 0.5 saturation for everyone; for witches +25 energy (0.5.0). One cup per 1,200 online ticks (60 s) for every player, a saved cooldown (see "Energy tea and restful infusion" below). Kettle: mint + sugar + water bottle | 3 / 6 | done |
| Restful sleep infusions | energy | Tea kettle | drink | no | resets the time-since-rest statistic (phantoms), forces no sleep; for witches it prepares the next rewarded sleep (+30 more, 0.5.0). Kettle: lavender + valerian + water bottle | 3 / 6 | done |
| Warmth remedy | environment | Tea kettle | drink | no | Warmth 90 s (freezing never builds, clears). Kettle: rosemary + blaze powder + water bottle | 3 | done |
| Cooling remedy | environment | Tea kettle | drink | no | Fire Resistance 60 s. Kettle: mint + snowball + water bottle | 3 | done |
| Night vision potion | utility | Witch's Cauldron | drink | yes | Night Vision Draught: Night Vision 2:00 | 7 | done (0.6.0) |
| Underwater breathing potion | utility | Witch's Cauldron | drink | yes | Water Breathing Draught: Water Breathing 2:00 | 7 | done (0.6.0) |
| Mining tonic | tonics | Tea kettle | drink | no | Haste I 90 s. Kettle: thyme + iron nugget + water bottle | 3 | done |
| Travel tonic | tonics | Tea kettle | drink | no | Speed I 90 s. Kettle: mint + sugar cane + water bottle | 3 | done |
| Lantern oil | household | Infusion pot | oil | no | fills the oil lantern (one dose, 6,000 loaded ticks, light 14; refused while burning). Infusion pot: rosemary + resin, plant oil, glass bottle | 3 | done |
| Fishing blend | household | Infusion pot | oil | no | Luck I 90 s. Infusion pot: nettle + wheat seeds, plant oil, glass bottle | 3 | done |
| Protective sachet | household | Charm workbench | charm | yes | mob blows land 15% softer (at most 1 heart); worn in the off hand or a Curios `charm` slot; charge 300 s of wearing, then it breaks | 7 | done (0.6.0) |
| Garden fertilizer | garden | Mortar and pestle | dust | no | one bone meal application to a clicked crop, consumed only on success. Mortar: nettle powder + bone meal + moss block -> 4 | 3 | done |
| Growth infusion | garden | Infusion pot | oil | no | brings one immature Herbal Witch herb to maturity, bottle back. Infusion pot: nettle + bone meal, water bottle, glass bottle | 3 | done |
| Crop-yield blessing | garden | Altar and ritual circle | ritual | yes | Garden Blessing: enriches the herb beds of the 5x5 square around a chosen bed within 8 blocks (the soil tonic's effect); nettle powder + bone meal + soil tonic, 15 energy, 200 ticks, bottle returned | 7 | done (0.6.0) |
| Pest repellent | garden | Infusion pot | oil | no | 90 s; silverfish and endermites cannot target you, existing targets within 8 blocks cleared. Infusion pot: lavender + sage, plant oil, glass bottle | 3 | done |
| Soil tonic | garden | Infusion pot | oil | no | enriches one herb bed: next mature harvest +1 fresh herb, bottle back. Infusion pot: moss block + hanging roots, water bottle, glass bottle | 3 | done |
| Flower growth dust | garden | Mortar and pestle | dust | no | a random small vanilla flower on a clicked grass block. Mortar: chamomile powder + dandelion + bone meal -> 2 | 3 | done |
| Mushroom growth dust | garden | Mortar and pestle | dust | no | a brown or red mushroom where it can survive. Mortar: mugwort powder + brown/red mushroom + bone meal -> 2 | 3 | done |
| Home protection ward | home | Home ward stone | ritual | yes | Home Protection ritual (sage powder + amethyst shard, 20 energy, 200 ticks) charges a ward stone the witch may bind; direct activation by a witch costs 20 energy; the ward keeps chosen mobs from targeting inside its box (docs/catalogue/witchcraft.md) | 7 | done (0.6.0) |
| Dyes | home | Mortar and pestle | dust | no | purple (lavender), green (sage), yellow (chamomile), black (nightshade), 1 powder each. Mortar | 3 | done |
| Decorative herbal bundles | home | Herb drying rack | charm | no | 14 bundles, one per herb; hang from ceilings or walls, never floors. Drying rack: fresh herb, 1,200 ticks | 3 | done |

## Combat and enemy mixtures

Balanced by duration, recipe cost, cooldown and mob resistance. PvP targeting is off by default (`preparationPvp`); villagers and wandering traders are protected (`protected_entities`); creatures in `hard_control_immune` are never bound or put to sleep; a creature just controlled gets Steadfast (100 ticks). Thrown vials and dust burst in radius 3, hit at most 32 targets with line of sight, and share a 20-tick throw cooldown (at most 4 in flight per thrower, 128 per dimension).

| Product | Category | Station | Delivery | Witch-only? | Planned effect | Milestone | Status |
|---|---|---|---|---|---|---|---|
| Poison vials | offence | Witch's Cauldron | throwable | yes | Poison Vial: Poison I 0:08 | 7 | done (0.6.0) |
| Venom oils | offence | Infusion pot | oil | yes | Venom Oil coats the blade in the off hand: 8 poisoning hits or 1:00 (familiar attacks wait for M9) | 7 | done (0.6.0) |
| Blinding powder | control | Mortar and pestle | dust | yes | Blinding Dust, thrown: blinds; creatures cannot find anyone beyond 4 blocks, 0:05 | 7 | done (0.6.0) |
| Concealment smoke | control | Witch's Cauldron | throwable | yes | Concealment Vial: a smoke burst; foes cannot find those in it beyond 4 blocks, 0:08 | 7 | done (0.6.0) |
| Slowing mixture | control | Witch's Cauldron | throwable | yes | Slowing Vial: Slowness II 0:08 | 7 | done (0.6.0) |
| Binding mixture | control | Witch's Cauldron | throwable | yes | Binding Vial: holds a creature in place, 0:05 | 7 | done (0.6.0) |
| Weakening mixture | control | Witch's Cauldron | throwable | yes | Weakening Vial: Weakness I 0:08 | 7 | done (0.6.0) |
| Temporary sleep mixture | control | Witch's Cauldron | throwable | yes | Sleep Vial: a creature sleeps until struck, 0:05 | 7 | done (0.6.0) |
| Thorn retaliation oil | defence | Infusion pot | oil | yes | Thorn Oil: whoever strikes you is pricked, 1:30 | 7 | done (0.6.0) |
| Defensive resistance draught | defence | Witch's Cauldron | drink | yes | Resistance Draught: Resistance I 1:30 | 7 | done (0.6.0) |
| Healing mixture | defence | Witch's Cauldron | drink | yes | Healing Draught: restores 3 hearts when hurt; one every 10 seconds | 7 | done (0.6.0) |
| Mob-specific repellent mixture | defence | Infusion pot | oil | yes | Undead Repellent and Arthropod Repellent: the undead (or spiders and crawling pests) will not seek you, 1:30 | 7 | done (0.6.0) |
| Mob-specific protective charm | defence | Charm workbench | charm | yes | Undead Amulet (undead blows 30% softer, at most 1.5 hearts) and Arthropod Amulet (spider and pest bites, same), charge 300 s of wearing | 7 | done (0.6.0) |

The cauldron makes each drink and vial as a four-dose Mixture; the Bottling Bench portions a mixture into
four bottles (adding gunpowder and paper makes a labelled vial). `AdvancedProductCatalog` holds the 16
advanced preparations (the four drinks, six vials, four oils, Blinding Dust and Moon Essence),
`MixtureCatalog` the 17 mixtures (4 drinks, 6 vials, 7 familiar tonics).

## Ordinary animal products (M8 candidate)

Finished products work for non-witches. All care is server-authoritative and successful handling costs
no energy. The [animal catalogue](animals.md) records species support, trust, ownership and transport
rules; the live grimoire lists the current recipes. Version promotion awaits ANU runtime/restart qualification.

| Product | Station and recipe | Use |
|---|---|---|
| Grain, root, seed, fish, meat and berry bait | Mortar: two matching food-tag ingredients + valerian powder → four bait, 100 ticks | Matching species only; a rewarded feed grants 20 personal trust, once per 1,200 loaded ticks. Holding bait attracts supported goal-AI animals without granting trust. |
| Calming powder | Mortar: lavender powder + chamomile powder → four, 100 ticks | Settles supported goal-AI animals for 200 loaded ticks; no trust, healing, anger removal or active refresh. |
| Animal capture charm | Charm workbench: two string + leather + amethyst shard → one, 200 ticks; witch production, 10 energy | Non-stackable empty charm becomes a server-issued filled charm only after durable capture. Safe release consumes that claim and returns the empty charm. |
| Filled animal capture charm | Successful capture only; no recipe or creative-tab entry | Separate non-stackable presentation of the same transport claim; copied items cannot duplicate animals. |
| Existing healing salve | Existing infusion-pot recipe remains unchanged | Targeted use on an injured wild/own supported animal restores up to 4 health, returns a bowl, and starts a 200-loaded-tick target cooldown; no trust gain. Player self-use is unchanged. |
| Existing Sleep Vial | Existing witch-cauldron/bottling recipe remains unchanged | M7 sleep also settles eligible own/wild ordinary animals for capture. Damage wakes them and ordinary hard-control immunity still applies. |

## Familiar preparations

Recipes landed in milestone 7 (0.6.0) as `FamiliarPreparationCatalog` items; their effects on familiars land in milestone 9. Until M9 installs `FamiliarPreparationApplications.install` the ten new items refuse to be used, with "available with familiar bonding". Temporary buffs last 180 s. Permanent upgrades come only from a small named altar-ritual list. Familiar powers consume energy and/or have cooldowns.

| Product | Category | Station | Delivery | Witch-only? | Planned effect | Milestone | Status |
|---|---|---|---|---|---|---|---|
| Guardian's Brew | familiar brew | Witch's Cauldron | drink | yes | health and defense | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Swiftpaw Tonic | familiar brew | Witch's Cauldron | drink | yes | movement speed | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Nightwatch Infusion | familiar brew | Witch's Cauldron | drink | yes | nighttime detection | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Moonveil Elixir | familiar brew | Witch's Cauldron | drink | yes | temporary stealth | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Warding Essence | familiar brew | Distillation still | essence | yes | elemental/magic resistance (sage powder + quartz, 15 energy) | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Emberguard Oil | familiar brew | Infusion pot | oil | yes | fire resistance | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Venomleaf Extract | familiar brew | Distillation still | essence | yes | poison attacks (belladonna powder + spider eye, 15 energy) | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Spiritbond Potion | familiar brew | Witch's Cauldron | drink | yes | stronger bond and energy synergy | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Hearthkeeper's Brew | familiar brew | Witch's Cauldron | drink | yes | enhanced home defense | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |
| Healing Salve | familiar brew | Infusion pot | salve | no | restore health (animal variant of everyday salve); the catalogue lists the existing everyday Healing Salve as the eleventh entry, no new item | 7 / 9 | catalogue entry done (0.6.0); animal use planned (M8/M9) |
| Shadowstep Draught | familiar brew | Witch's Cauldron | drink | yes | brief repositioning | 7 / 9 | item and recipe done (0.6.0); effect planned (M9) |

Moon Essence (a still recipe: mugwort powder, amethyst shard, water bottle, glass bottle, 10 energy) is the
ingredient the strongest brews take.

## Grimoire lookup (0.3.0)

Every processing recipe above has a recipe-backed page in the grimoire (drying, grinding, steeping and
infusion), built from the registered recipes themselves (`StationRecipeEntries`); there are no hand-written
product pages. A product is discovered when it is carried, or when
all of a recipe's primary ingredients are familiar. Discovery is advisory only: product effects, cooldowns
and delivery are exactly as in the tables above, and nothing is gated by knowledge.

## Energy tea and restful infusion (0.5.0, Milestone 6)

The two drinks that carry witch energy. Numbers are defaults of `herbalwitch-server.toml`
(docs/catalogue/witchcraft.md); the grimoire quotes the live values.

| | Energy tea | Restful infusion |
|---|---|---|
| Non-witch | food 4 / 0.5 saturation, as before; hydration from the Thirst adapter if installed | resets the time-since-rest statistic, forces no sleep, as before |
| Witch | the same food, plus +25 energy (capped at capacity) | the same reset, plus it prepares the next rewarded sleep: +30 more energy, non-stacking; drinking again refreshes the 24,000-tick duration |
| Cooldown | 1,200 online ticks, authoritative and saved for every player; a cup started or finished during it is refused and nothing is consumed. The vanilla item cooldown only mirrors it and is restored after login or respawn | none |
| Notes | at zero energy a witch can drink it; nothing hurts at zero | a sleep that is not rewarded (not a strictly newer day, or under 100 ticks asleep) does not consume the preparation |

## Herbs

The 14 herbs (Milestone 2, done) are tabulated in [herbs.md](herbs.md): ids, groups, forms, traits,
habitats, discovery chances and worldgen rarity.

Supplements (not herbs): vanilla mushrooms, moss, roots, flowers, minerals, and a resin ingredient. No extra herbs are invented to reach "about 20".
