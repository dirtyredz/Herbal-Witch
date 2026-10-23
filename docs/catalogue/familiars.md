# Familiars (M9 implementation in progress)

The first phases supply canonical bond storage, binding and the private roster. Version remains 0.6.1 until all M9
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

Open **Familiars** from the grimoire reader. Pages contain at most 32 summaries and 24 KiB, with a
keyset cursor and owner revision. Stale actions refresh the page. Unloaded health/location is marked
last observed; no recovery data, receipts or inventories are sent. Select a row for an altar ritual,
or open its detail card while the loaded creature is in ordinary reach and sight. Clicking an owned
familiar with the book uses the same detail access through the existing animal interaction router.
Losing the book closes the roster; leaving reach closes detail. Neither action loads chunks.

Use the roster's command arrows to select an order, then press its named button. Follow starts beyond
six blocks and stops within three; Stay keeps its saved anchor; Guard Witch defends against hostile
threats to the owner or familiar. Protect Home patrols an assigned home, Scout visits at most four
safe waypoints, Passive wanders locally without retaliation, and Return Home becomes Stay on arrival.
Home orders require an assignment from the home phase. Controls/sleep, immediate hazards and retreat
below one-quarter health take precedence. Ordinary commands cost no energy.

Follow, Guard Witch and Scout share three mobile slots, including unloaded assignments. Logout or
owner death changes those assignments to Stay; dimension travel leaves followers waiting. Paths
replan no more often than every 20 ticks, stop after three failed attempts, and refuse unloaded search
regions. A fair dimension queue permits four paths and eight scans per tick; entity queries stop at
32 candidates and observations return at most eight results. Friendly visitors, pets and passive mobs
are never automatic combat targets.

**Recall** spends five energy, has a 100-tick cooldown, and moves an existing loaded familiar to a
checked nearby position. Transfer uses a newer generation, retained recovery payload and verified
player receipt. It never summons an absent familiar. Home, equipment and ability phases still follow.

`herbalwitch-familiars-server.toml` atomically installs validated settings: new binding/species switches,
mobile limit 1–8 (default 3), record limit up to 128 KiB, lifecycle interval at least five ticks,
scan/path budgets up to 32/16 (defaults 8/4), guard/sense radii up to 24/48 (defaults 16/32), offline
home guards up to eight, and public retrieval off by default. Lowering the mobile limit keeps oldest
reservations. Existing oversized records remain preserved if the byte limit is lowered; raise it to
allow a mutation that no longer fits. These settings do not erase existing bonds or their payloads.
