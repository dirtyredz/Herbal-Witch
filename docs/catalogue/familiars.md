# Familiars (M9 implementation in progress)

The first phase supplies canonical bond storage and binding. Version remains 0.6.1 until all M9
phases, headless previews and the committed ANU acceptance are complete. Section 7 defaults of the
binding plan remain unchanged.

| Species | Required care | Ownership before binding |
|---|---|---|
| Cat, wolf | 100 trust, five feeds | Native owner |
| Fox, rabbit | 80 trust, four feeds | Keeper |
| Bat | 60 trust, three feeds | Trust only; binding establishes the exclusive owner |
| Raven, owl, snake | 80 trust, four feeds | Keeper; seed, meat and meat bait respectively |

Choose a creature before opening the altar, then use its familiar-ritual panel. Binding requires a
complete circle, witch and animal within four blocks, matching bait, fresh sage and amethyst. It
takes 200 loaded ticks and 25 energy; trust, ownership, safety and the exact offering are checked
throughout. Damage/control, a baby, a leash/passenger, unsupported data, unresolved capture identity,
lost sight or invalid structure refuses or cancels it. The result starts in Stay.

Authority is one `data/herbalwitch_familiar_<bond UUID>.dat` shard per bond, with a 128 KiB hard bound
and depth 16. Records are checksummed envelopes, not compressed vanilla NBT. Owner/entity indexes
are derived in batches of 32; there is no roster-size cap. Interrupted lifecycle records, incomplete
finalization and unresolved receipts quarantine instead of spawning or refunding anything. A player
has one monotonic receipt marker, never a copied roster. Preserve these files with player/entity data.

The five native-derived entities preserve UUID, native appearance, ownership, intrinsic gear and M8
care. Raven, owl and snake use dedicated wild/bound representations. Art, acquisition spawns and the
remaining gameplay phases are still in development; these declarations are not milestone acceptance.
