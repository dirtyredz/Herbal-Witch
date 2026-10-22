# Ordinary animals (M8 candidate)

Public care works for witches and non-witches. This is ordinary animal handling; animals remain mortal.
Version promotion to 0.7.0 awaits the orchestrator's dedicated-server and restart qualification.

| Species | Bait | Earned ownership | Added attraction/calming | Capture |
|---|---|---|---|---|
| Cow, sheep | Grain | Keeper | Yes | Yes |
| Goat | Grain | Keeper | No: native Brain | Yes, using sleep |
| Pig, rabbit | Root | Keeper | Yes | Yes |
| Chicken | Seed | Keeper | Yes | Yes |
| Horse, donkey, mule, llama | Grain | Native horse taming | Yes | Yes |
| Cat | Fish | Native taming | Yes | Yes |
| Wolf | Meat | Native taming | Yes | Yes |
| Fox | Berry | Keeper; native trusted UUIDs retained | Yes | Yes |
| Parrot | Seed | Native taming | Yes | Yes |
| Axolotl | Fish | Keeper | No: native Brain | Yes, using sleep; release into water |
| Bat | Berry | Trust only for later M9 | No: native flight | No |

Direct matching bait gives 20 personal trust and one successful feed, once per 1,200 **loaded** ticks.
Trust caps at 100, never passively decays, and is private to each player/animal pair. Default capacity
is eight relationships; full history is never evicted. Holding bait attracts supported animals but
does not feed, breed or reward trust. Ordinary vanilla food retains its behavior.

Open a grimoire on the animal to inspect your relationship. Keeper adoption requires 80 trust and
four feeds; native taming requires 100 trust and five feeds. The explicit card action costs one
additional matching bait from main inventory or offhand (creative preserves that bait). Native pets
keep vanilla sitting/following/riding; a keeper grants persistence, without familiar commands.
Native owners take precedence, conflicting owners are refused, and foreign pets remain owner-only.

Successful damage from a player or their pet subtracts 40 from that player's existing trust. Any
successful damage starts a 600-loaded-tick feeding/adoption/capture delay, wakes M7 sleep and ends
calming. Calming reapplication waits 100 ticks. The existing healing salve restores up to four health
with a 200-tick target cooldown, returns its bowl, and grants no trust. Full health and denied care
consume nothing. Self-use of the salve is unchanged.

Calming powder stops navigation for 200 loaded ticks using a temporary MOVE goal. It retains native
goals and attacker memory; panic can resume. Fire, freezing, drowning, riding/passengers, ordered sit,
an attack target, loss of permission or the source player leaving ends it. It cannot refresh itself.
Existing M7 sleep lasts 100 ticks and retains its damage wake and 100-tick steadfast immunity.

## Production

All grinding takes 100 ticks. Two matching food ingredients plus one valerian powder make four bait.
Grain uses wheat; root carrot/beetroot; seed wheat/melon/pumpkin/beetroot seeds; fish raw cod/salmon/
tropical fish; meat raw beef/pork/chicken/mutton/rabbit; berry sweet berries/glow berries/apple.
Lavender powder plus chamomile powder makes four calming powder.

Two string, leather and an amethyst shard make one empty capture charm at the charm workbench in
200 ticks, with the existing witch-production requirement and 10 energy. The station spends through
`WitchStatusService.spend`; handling animals has no energy cost. Bait and powder stack to 64. Both
charm states stack to one. Filled charms are issued only from the server ledger.

## Transport and operator recovery

Capture needs a real player holding an empty charm, a tested eligible animal, normal reach/sight,
permission, ownership or 80 personal trust/four feeds, and active calm or sleep. Recent damage,
attacking, environmental danger, passengers, riding and leashes refuse it. Native owners bypass only
the trust requirement. Babies preserve their age. Unknown capabilities or persistent mod data are
refused before removal; unsupported stored data is retained. M9 must supply its own lifecycle policy
before bonded animals can be transported.

Use a filled charm against a block face. Its single adjacent-cell destination must be within reach,
loaded, inside the border/build height, collision-free and permitted. Land animals need safe support;
axolotls need a full water habitat. There is no search for another destination and no chunk loading.
The held charm changes in place even with a full inventory or in creative. Stored animals do not tick.

`data/herbalwitch_captures.dat` in the Overworld is the sole authority. It is a versioned checksummed
envelope containing bounded NBT, written through a serialized temp/force/atomic-replace/read-back path.
It is intentionally not an ordinary compressed NBT file. Keep it with the world's entity/player data
in backups; inconsistent filesystem rollback is outside the guarantee.

Capture checkpoints PREPARED before discarding the exact source, then STORED before issuing a token.
Release checkpoints PREPARED, admits one exact entity object, then checkpoints RELEASED before changing
the held item. Final release retains its payload until verification succeeds; ordinary save compacts
the permanent UUID/generation tombstone afterward. Startup quarantines prepared or un-compacted final
records. This may conservatively interrupt a completed release after a crash, but never auto-spawns it.
Write uncertainty locks transport and keeps dirty state; corrupt files are preserved, never reset.
Stale loaded generations and copied tokens cannot authorize another release.

Operators (permission level 2) use:

- `/herbalwitch capture inspect <animal-uuid>`: phase, generation, captor and source/destination; no raw payload.
- `/herbalwitch capture reissue <animal-uuid> <player>`: rotate a STORED token for its original captor.
- `/herbalwitch capture recover <animal-uuid> <player>`: advance an interrupted/quarantined generation,
  retire indexed prior incarnations without drops, and issue a stored token to the original captor.

Issuance needs a free inventory slot and a verified checkpoint. Old copies become invalid. Commands
never spawn directly or revive a compact released record. Preserve a corrupt/unsupported ledger for
inspection; deleting it discards replay protection and is not a recovery procedure.

## Participation and budgets

`herbalwitch-animals-server.toml` independently controls relationships, attraction, calming, earned
taming, new captures and modded participation. New-capture disable leaves existing release/recovery
available. Live entity tags under `animals/` select eligibility, exclusion, exactly one bait group,
attractable/calmable and capturable membership; item food tags control recipes. Tags cannot supply
missing AI/serialization adapters. Modded animals default off; Doggy Talents dogs are excluded.

Defaults / hard ceilings: relationships 8/16; bait radius 8/12; candidates per query 8/16; attracted
per holder 8/16; active goals per dimension 256/512; candidate checks per tick 128/256; paths per tick
16/32; payload 32/64 KiB; NBT depth 16/32; stored per captor 16/64; stored per world 128/512; lifetime
records 4,096/16,384; snapshot 8/16 MiB. One global transaction at a time, 20-tick interval (minimum 5).
Quarantine counts toward storage, and released tombstones never expire. Requests cap at five per
second and 4 KiB; private cards update at most every ten ticks unless an operation completes.

The `animals` showcase adds sixteen labeled adult specimens in separate glass habitats, water for
axolotl and enclosed parrot/bat flight. Supplies contain all usable care items, salve, sleep and a
grimoire. No trust, owner, witch status, discovery or active effect is pre-granted.
