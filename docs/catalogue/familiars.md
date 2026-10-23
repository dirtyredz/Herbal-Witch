# Familiars (Milestone 9)

Eight species share an owner-private grimoire roster. Total bonds have no gameplay cap; three
mobile assignments per owner are allowed by default. A missing or unloaded creature is never
treated as dead. All IDs below have the `herbalwitch:` namespace.

| Species (`familiars/entity/<species>`) | Binding prerequisite | Native acquisition / power |
|---|---|---|
| cat | Native owner, 100 trust, five feeds | Vanilla cat; threat sense |
| raven | Keeper, 80 trust, four feeds | Wooded Overworld, weight 6; retrieval; crow is an appearance |
| owl | Keeper, 80 trust, four feeds | Wooded Overworld, weight 4; herb sense and nocturnal threat sense |
| fox | Keeper, 80 trust, four feeds | Vanilla fox; retrieval and hostile distraction |
| wolf | Native owner, 100 trust, five feeds | Vanilla wolf; hostile tracking and interception |
| snake | Keeper, 80 trust, four feeds | Warm/dry Overworld, weight 6; creature sense and venom |
| rabbit | Keeper, 80 trust, four feeds | Vanilla rabbit; herb sense and garden aid |
| bat | 60 trust, three feeds | Vanilla bat; dark creature sense; binding establishes ownership |

New wild species spawn in groups of one or two on safe ground, including daytime, without retrogen.
Their three `familiars/spawn_egg/<species>` eggs are creative-only and contain no bond state.
Binding accepts only supported adults without passengers, leashes, combat or unsafe conditions.
It preserves UUID, supported native traits and animal relationships; unsupported data is refused.
Bound animals cannot enter capture charms.

## Altar and progression

Use a valid altar and ritual circle. Binding requires the animal and owner within four blocks,
matching animal bait, fresh sage and an amethyst shard: 200 loaded ticks and 25 energy.
The default command is Stay. Paid actions use the witch's existing energy transaction.

| Action (`ritual/familiar/<action>`) | Offering | Duration / energy |
|---|---|---|
| revival | Moon essence, fresh yarrow, amethyst shard | 400 ticks / 50 |
| heartwood | Fresh yarrow, fresh sage, gold ingot | 200 ticks / 25 |
| spirit_well | Amethyst shard, fresh mugwort, moon essence | 200 ticks / 25 |
| bond_harmony | Fresh lavender | 200 ticks / 10 |

Levels require 0, 40, 100, 200 and 350 XP. Each level above the first adds one maximum health.
Care, nearby travel and safe home rest have bounded XP gates; ordinary XP is capped at ten per
24,000 loaded ticks. Harmony grants ten XP with its own 24,000-tick cooldown. At level three,
Heartwood adds four maximum health and Spirit Well adds ten reserve capacity, each once.
Increasing health capacity or reserve capacity does not refill it.

## Commands and homes

Follow, Guard Witch and Scout reserve mobile slots. Stay holds a safe local anchor. Passive clears
targets and suppresses magical work. Protect Home guards a verified assigned home. Return Home
travels only through loaded space and becomes Stay on arrival. Hazard retreat takes priority.
Ordinary commands and physical defense are free. Explicit recall costs five energy with a
100-tick cooldown and moves only a presently loaded entity to a checked nearby position.

Three `familiars/home/` blocks have unique placed-instance identities: bed (cat, fox, wolf, snake, rabbit), nest
(raven, owl), perch (raven, owl, bat). Bed uses three dark-oak planks, two green wool and two
string; nest uses four sticks, two wheat and two string; perch uses two dark-oak slabs, two sticks
and an iron nugget. Assignment checks compatibility, occupancy and permission. Breaking/replacing
a home does not transfer its identity. Unloaded homes remain pending and never force chunks.

## Equipment and magical work

The nearby detail menu has one species-gear slot, one existing charm slot and one task cargo stack.
Gear is `familiars/equipment/<key>`. Every recipe uses the charm workbench, 200 ticks and five energy.

| Gear | Materials | Effect |
|---|---|---|
| cat_bell | Leather, two string, gold nugget | Threat cooldown 200 → 160 ticks |
| raven_satchel | Leather, two string, rabbit hide | Cargo capacity 8 → 16 |
| owl_lens | Leather, two string, glass pane | Herb cooldown 200 → 160 ticks |
| fox_pouch | Leather, two string, sweet berries | Cargo capacity 8 → 16 |
| wolf_harness | Leather, two string, iron ingot | Two armor |
| snake_ring | Leather, two string, copper ingot | Venom cooldown 200 → 160 ticks |
| rabbit_sachet | Leather, two string, dried chamomile bundle | Garden cooldown 600 → 480 ticks |
| bat_chime | Iron nugget, two string, amethyst shard | Dark-sense cooldown 200 → 160 ticks |

The fox and bat recipe differences avoid collisions with existing unordered recipes (see Decisions).
Charm charges retain their M7 behavior; time-worn familiar charms consume a charge per 300 loaded seconds.

| Power | Energy / cooldown | Concrete result |
|---|---|---|
| Threat, herb, creature sense; hostile tracking | 3 / 200 ticks | Short-lived private, permission-filtered observations |
| Retrieval | 5 / 200 ticks | Whole eligible loose stack transferred into canonical cargo |
| Distraction | 4 / 300 ticks | Eligible hostile targets the fox briefly |
| Interception | 5 / 300 ticks | Guarding wolf reduces damage by 20% for 100 ticks |
| Venom | 4 / 200 ticks | Guarding snake's permitted bite uses shared poison attribution |
| Garden aid | 5 / 600 ticks | Actual growth of a loaded immature herb |
| Repel | 3 / 40 ticks | Guard pushes a permitted threat away from its defended anchor |

No result, denied permission or stale target costs nothing. Automatic powers start disabled.
Sense radius defaults to 32; observations expire after five seconds. Targets are opaque private
tokens, not client-authoritative entity choices. Retrieval defaults to owner-attributed simple,
stackable items from the retrieval tag; containers, arbitrary NBT and oversized stacks are refused.
Its permanent source-UUID receipt prevents an older item-entity save from recreating collected cargo.

Reserve starts empty with capacity 20. Explicit nearby charging moves five witch energy into five
reserve, without partial overfill. Only loaded Protect Home guards can use it while the owner is
offline; a maximum of eight guards per owner qualify. No offline recharge or elapsed-time catch-up.

## Preparations and recovery

Ten familiar buffs last 3,600 loaded ticks, refresh without stacking, and share a 20-tick application
gate. Guardian adds four health capacity and two armor; Swiftpaw increases movement speed 20%;
Nightwatch enables nocturnal threat sense; Moonveil uses concealment; Warding reduces eligible
elemental/magic damage 20%; Emberguard projects fire resistance; Venomleaf supplies shared poison;
Spiritbond discounts magical cost 20% and adds care XP; Hearthkeeper adds two armor near the guarded
home and shortens repel cooldown; Shadowstep is one successful checked escape of at most eight
blocks. The eleventh product, Familiar Salve, uses the existing capped animal-healing path.
Ordinary Venom Oil and Venomleaf share one coating slot and delayed permission checks.

Lethal ordinary damage retains a bounded recovery payload and canonical inventory without drops or
XP. Revival requires proven incapacitation, restores half health as Stay with a newer generation,
and clears temporary effects while preserving bond, gear and reserve. Administrative removal does
not create an ordinary revival claim. Ambiguous interrupted writes are preserved and quarantined,
never guessed, refunded or automatically respawned. Operator inspection/recovery is available under
`/herbalwitch familiar`; recovery cannot bypass unresolved economic receipts.

## Configuration and operational limits

`herbalwitch-familiars-server.toml` controls new binding/species, mobile slots (1–8), maximum record
bytes (4–128 KiB), transaction spacing (5–1,200 ticks), scans (1–32) and paths (1–16) per dimension
tick, guard radius (1–24), sense radius (1–48), offline guards (1–8), and public retrieval opt-in.
Defaults are three slots, 128 KiB, five ticks, eight scans, four paths, radii 16/32, eight guards and
private retrieval. Existing bonds remain recoverable when new binding is disabled.

The witchcraft showcase includes three functional homes, nine inert appearances and complete supply
barrels. Display equipment is cosmetic; displays have no owner, bond, cargo or recovery authority.
Headless qualification is recorded in Features; owner visual, shader and two-client review remains separate.

## Wand access (M10)

The [utility wand](tools.md) in Familiar Commands mode is an additional focus for the existing roster and detail menus. Target an owned familiar for detail, or use it without an entity target for the private roster. The same seven commands, home/mobile-cap checks, session nonces and paid bounded recall service apply; ordinary commands remain free even at zero energy. Changing or removing the focus ends access unless another supported focus remains held. Brooms carry only their player rider, with no automatic familiar transport.
