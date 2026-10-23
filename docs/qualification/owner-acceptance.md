# Owner acceptance checklist (Milestone 12, candidate 1.0.0-rc.1)

The owner's in-game pass over every milestone. Automated qualification (docs/qualification/README.md)
covers server behaviour headless; these cases cover what only a person at a real client can judge:
visuals, item transforms, screens, shaders, feel, two-client synchronisation and the integrated
(single-player) host. Every case starts **not run**. Mark it pass or fail and drop a screenshot or a
short note in the evidence folder; the agent reads the instance logs (`logs/latest.log`, crash
reports), so there is no need to send logs.

**Evidence folder:** `C:/Users/dirty/projects/artifacts/minecraft-mods/jobs/2026-10-23-herbal-witch-m12/acceptance/<case>/`

**Setup (once).** Use the dedicated mod-test instance `Herbal Witch Test` (never an `Evil Eye Trial *`
instance). Replace its `mods/herbalwitch-1.20.1-*.jar` with the candidate jar
`mods/HerbalWitch/build/libs/herbalwitch-1.20.1-1.0.0-rc.1.jar` (SHA-256 in docs/qualification/README.md)
and remove `resourcepacks/HerbalWitch-dev-art` if present (dev-art overrides would hide the jar's art).
Keep a copy of the instance's `saves/New World` before the first launch: it is a real 0.6.1 world and
case M12-1 opens it. Two-client cases need a second client joined to a LAN world or a dedicated server
started from `C:/Users/dirty/projects/artifacts/minecraft-mods/jobs/2026-10-23-herbal-witch-m12/inputs/forge-server`
with the candidate jar.

Columns: **Mode** S survival, C creative; **Host** I integrated (single player / LAN), D dedicated;
**Clients** how many players.

## M1 Foundation and the cottage design

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M1-1 | Install and join | S / D / 1 | Start the dedicated server with the candidate jar, join from the client | Both load; no missing-registry prompt; the client lists The Herbal Witch 1.0.0-rc.1 |
| M1-2 | Original pieces | C / I / 1 | Place the armchair, apothecary counter, small wall shelf, table lamp and small rug in every facing | Dark wood, plum, green, gold and ivy; models match the preview sheets; item views look right in hand, in a frame and in the hotbar |
| M1-3 | Support | S / I / 1 | Break the block under a lamp and behind a wall shelf | The piece drops as one item; nothing floats |
| M1-4 | Lamp sync | S / D / 2 | Player A toggles a table lamp | Player B sees the light and model change at once; it survives a relog |

## M2 Herbs and garden

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M2-1 | Acquisition | S / I / 1 | Break grass for seeds; craft the four starter seeds; find wild patches in each habitat biome | All fourteen herbs obtainable in survival; patches look natural |
| M2-2 | Growth stages | C / I / 1 | Plant every herb in a herb bed and bone-meal stage by stage | Every stage is 3D and cube-built, rooted in the soil, matching herbs-on-beds.png |
| M2-3 | Harvest | S / I / 1 | Use a mature crop herb and a mature bush herb | Fresh herbs drop; bushes stay at age 1, crops reset |
| M2-4 | Beds | S / I / 1 | Join beds in lines, an L and a ring with inner corners; water one; apply soil tonic | Rails merge into soil, inner corners close; watered soil clearly darker; enriched bed gives one extra herb |
| M2-5 | Showcase | C / I / 1 | `/herbalwitch showcase furniture` and `/herbalwitch showcase witchcraft` on flat ground | Every block appears labelled; nothing floats or clips |

## M3 Everyday stations and products

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M3-1 | Every station | S / I / 1 | Craft and use the drying rack, mortar, kettle, hearth, stove and infusion pot | Each GUI is readable at GUI scales 2-4; progress and heat indicators make sense |
| M3-2 | Heat | S / I / 1 | Kettle on a lit hearth, on a stove burner, on a campfire; then unlit | Works hot, shows needs-heat cold |
| M3-3 | Automation sides | S / I / 1 | Hopper into the top, out of the bottom of a mortar | Inputs in, outputs and returned bottles out; nothing else |
| M3-4 | Products | S / I / 1 | Drink every tea and tonic, use salve, oils, garden dusts, light the oil lantern | Effects as the grimoire says; bottles and bowls return |
| M3-5 | Hung bundles | C / I / 1 | Hang bundles from a ceiling and a wall | Flat 2D sprites, no colour streaks on neighbours |

## M4 Grimoire

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M4-1 | Learn by book | S / I / 1 | Follow drying, grinding and brewing using only the grimoire | Station, inputs and missing counts are exact (water bottles and returns included) |
| M4-2 | Research | S / I / 1 | Read at the grimoire stand and bookshelf | Shallow research unlocks entries; nothing else is gated |
| M4-3 | Teaching | S / D / 2 | A teaches B one recipe; B declines once, then accepts | Consent is explicit both ways; B's grimoire gains the page; relog keeps it |
| M4-4 | Viewers | S / I / 1 | Open stations with JEI only, EMI only, both, neither | Grimoire button works in all four; viewer categories show the witch requirement |

## M5 Furniture catalogue (94 pieces)

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M5-1 | Catalogue pass | C / I / 1 | Walk the furniture showcase against docs/catalogue/decor.md | Every piece present, one dark wood palette; note pieces that need repainting (see release-readiness R-ART-*) |
| M5-2 | Placement preview | S / I / 1 | Place multi-cell pieces on walls, ceilings, corners, carpets, in all four facings | The outline matches the result every time; red outline where it would not fit |
| M5-3 | Seating | S / D / 2 | Sit on chairs, benches, sofa, swing; both players on a two-seat piece | Pose and dismount are natural; no orphan seats after breaking a seated piece |
| M5-4 | Storage and displays | S / D / 2 | Both open the same cabinet; fill a display shelf | Contents sync; displayed items stand in their slots |
| M5-5 | Beds and doors | S / I / 1 | Sleep in the bed and canopy bed; open the door from both sides | Vanilla sleep and respawn; door collision correct |
| M5-6 | Lights | S / I / 1 | Toggle every light | Light level 14 when lit; models match states |
| M5-7 | Blockbench edit | - / - / - | Edit one project in Blockbench 5.2.2, recompile (docs/BLOCKBENCH.md) | The change appears after F3+T with the dev-art pack |

## M6 Initiation and energy

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M6-1 | Founding | S / I / 1 | Found the first lineage at the altar in a new world | One founder per world; the grimoire and energy bar appear |
| M6-2 | Initiation | S / D / 2 | A (witch) invites B; B declines, then accepts | B becomes a witch only after accepting |
| M6-3 | Energy | S / D / 2 | Spend, rest, meditate, sleep, drink energy tea; check B's HUD | Each recovery works; A never sees B's energy |
| M6-4 | Death and reconnect | S / D / 1 | Die with keepInventory off and on; relog | Witch status, energy and practice persist |
| M6-5 | HUD | S / I / 1 | GUI scales 1-4, with chat and boss bar | Bar stays readable and clear of other overlays |

## M7 Advanced brewing, charms, circle and wards

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M7-1 | Advanced stations | S / D / 2 | Use cauldron, still, bottling bench, charm workbench; B presses Start as a non-witch | Witch Start/Resume/Cancel work; non-witch is refused; active art shows |
| M7-2 | Vials | S / I / 1 | Throw every vial at mobs; one at a villager and a boss | Particles readable; bosses and villagers unaffected; second control within the steadfast window does nothing |
| M7-3 | Oils and coating | S / I / 1 | Venom on an off-hand blade; thorn oil and get hit | Poison on hit; attacker takes thorn damage once a second |
| M7-4 | Charms | S / I / 1 | Wear each charm in the off hand, then in a Curios slot | Protection applies once, charge drains, the charm breaks when spent |
| M7-5 | Circle and blessings | S / I / 1 | Place the circle in each facing; Garden Blessing and Home Protection | Preview equals result; targets are the aimed bed and stone |
| M7-6 | Ward | S / D / 2 | Set radius and profile; B (not owner) tries the controls; restart | Owner-only controls; boundary preview correct; ward still protects after restart |
| M7-7 | Non-witch use | S / D / 2 | B (non-witch) drinks and throws products A made | Products work for B |

## M8 Ordinary animals

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M8-1 | Baits and trust | S / I / 1 | Each bait on its species; feed over days | Animals come to the bait; trust rises; the card shows private progress |
| M8-2 | Calming and taming | S / I / 1 | Calming powder; earned taming of a horse and a wolf | Native ownership after earning it, never instantly |
| M8-3 | Capture | S / D / 2 | Capture a sleeping cow with a charm, B tries the filled charm, A releases it; copy the charm in creative | Only the captor releases; one cow; the copy does nothing |
| M8-4 | Exclusions | S / I / 1 | Try a bat, a villager, a modded animal | Refused with a clear message |
| M8-5 | Card UI | S / I / 1 | Inspect animals at several GUI scales | Readable private card |

## M9 Familiars

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M9-1 | Species | S / I / 1 | Bind a cat, raven (and crow look), owl, fox, wolf, snake, rabbit, bat | All eight bind at the altar; silhouettes and animations read well |
| M9-2 | Commands | S / I / 1 | Follow, Stay, Guard Witch, Protect Home, Scout, Passive, Return Home | Each behaves; a scout warns privately of hostiles |
| M9-3 | Homes | S / I / 1 | Bed, nest and perch; assign; break one | Rest and healing at home; a broken home clears cleanly |
| M9-4 | Equipment and preparations | S / I / 1 | Equip each gear; apply all eleven preparations | Gear shows on the right bone; effects as documented; a fox is hidden from distant hostiles until it distracts |
| M9-5 | Incapacitation and revival | S / I / 1 | Let a familiar fall in combat; revive at the altar | No death drops; revival restores it once |
| M9-6 | Two clients | S / D / 2 | A's familiars near B | B cannot command, see A's roster or receive A's warnings |

## M10 Broom and wand

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M10-1 | Broom | S / D / 2 | Craft, repair, mount, fly (Jump, Left Control, R to land, Sneak to dismount), run out of energy | Camera and animation feel right; landing is safe; B sees smooth motion |
| M10-2 | Wand modes | S / I / 1 | V to select; harvest, plant, water, collect, light, ward focus, familiar commands | Each mode does one bounded job; HUD shows the mode |
| M10-3 | Boundaries | S / D / 2 | Use the wand in spawn protection and on B's ward | Refused politely |

## M11 Commerce

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M11-1 | Open a shop | S / D / 1 | Counter, register, shelves and jars; link; name; price | Previews equal results; displays show stock pictures, never items |
| M11-2 | Concurrent buyers | S / D / 2 | Two non-witches buy the last lot at once | One succeeds, the other is told it sold out; emeralds conserved |
| M11-3 | Owner away | S / D / 2 | Owner logs out; B buys | Sale completes; owner collects proceeds later |
| M11-4 | Requests and villagers | S / D / 1 | Post a request; wait for villager customers | Villagers walk in, buy within budget, stop when exhausted |
| M11-5 | Privacy | S / D / 2 | B opens the register | B sees offers only, never ledger or proceeds |
| M11-6 | Single-player checkpoint | S / I / 1 | In a single-player world: buy and sell, then quit with Alt+F4 mid-session; reopen | Inventories and stock agree; no duplicated or lost emeralds. This is the one path automation cannot run (release-readiness R-HOST) |

## M12 Release qualification

| # | Case | Mode/Host/Clients | Steps | Expected |
|---|---|---|---|---|
| M12-1 | Historical world | S / I / 1 | Open the instance's existing 0.6.1 world with the candidate | Everything placed is still there; station jobs resumed or cancelled with items kept |
| M12-2 | Restart and reconnect | S / D / 2 | Stop and start the server mid-play; both reconnect | State intact; no duplicate familiars, animals or brooms |
| M12-3 | Reloads | S / I / 1 | `/reload` while a station works; F3+T resource reload | Jobs whose recipe changed cancel with items kept; models and screens reload |
| M12-4 | Shaders | S / I / 1 | Oculus shaders off and on around the cottage, stations and familiars | No missing faces, z-fighting or glow errors |
| M12-5 | End-to-end loop | S / D / 2 | A forages, dries, grinds, brews, bottles and stocks a shop; B (non-witch) buys and uses | The whole loop works in survival |
| M12-6 | Art consistency | C / I / 1 | Review the release-readiness art list | Decide repaint or accept for each listed piece |
