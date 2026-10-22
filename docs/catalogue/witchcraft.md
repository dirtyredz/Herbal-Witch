# Witchcraft catalogue (Milestone 6, 0.5.0)

Exact ids, offerings, actions, rules and the operator procedure for initiation and energy. The
source of truth is the code (`magic/witch/`, `magic/ritual/`); if they disagree the code wins. How it
works: docs/ARCHITECTURE.md ("Witchcraft"); why: docs/DECISIONS.md ("0.5.0"); the player's view: README
"Witchcraft". Paths are under `io.github.dirtyredz.herbalwitch`.

## Ids

| Thing | Id | Notes |
|---|---|---|
| Ritual Altar | `herbalwitch:magic/ritual/altar` | block, item, block entity and menu share the id; permanent |
| Witch capability | `herbalwitch:witch/status` | server players only, schema 1 |
| Energy nature biomes | `herbalwitch:witch/energy/nature` | biome tag: the union of the temperate, wet, shade and woodland habitat tags |
| Founding record | `data/herbalwitch_founding.dat` | Overworld `FoundingSavedData`, schema 1 |
| Config | `herbalwitch-server.toml` (per world), `herbalwitch-client.toml` | rules and HUD |
| Command | `/herbalwitch witch ...` | permission level 2 |
| Grimoire pages | `witchcraft/status`, `witchcraft/energy`, `witchcraft/recovery`, altar (station kind), `ritual/{founding,initiation,meditation,attunement}` | category "Witchcraft" |

The altar's recipe is `SAS` / `LGL` / `L L` (2 smooth stone slabs, amethyst shard, 4 dark oak logs, gold
ingot), Apothecary tab, broken with an axe. Footprint and teardown: docs/catalogue/stations.md.

## Actions at the altar

| Action | Who | Offering (all consumed, from the payer's main inventory and offhand) | Energy | Duration | Result |
|---|---|---|---|---|---|
| Founding | a non-witch, once per world | fresh lavender, fresh sage, fresh mint, fresh chamomile, an amethyst shard, any valid written book (its contents are never read) | none | 200 ticks (`foundingTicks`) | the player becomes a witch, receives exactly one Grimoire, starts at full energy |
| Initiation | a witch, for a consenting non-witch | fresh sage, fresh lavender, an amethyst shard (the initiator's) | 50 (`initiationCost`), paid by the initiator only | 200 ticks (`initiationTicks`) | the recipient becomes a witch; the initiator gains 50 practice |
| Meditation | a witch, at an idle altar | none | none (restores) | explicit start; 40-tick settle, then 2 per 20 ticks, stops at full | passive rest is suspended while meditating |
| Attunement | a witch | one fresh sage | 10 (`attunementCost`) | 100 ticks (`attunementTicks`) | awards practice; still available at full practice, labelled "no further capacity" |

Rules shared by every action:

- **Access:** alive, non-spectator, same dimension, the whole altar loaded, eyes within 4 blocks of the
  footprint with line of sight, and `ActionPermissionService.mayUse` (use, not build: loaded, border,
  height, spawn protection; adventure mode is allowed).
- **Concurrency:** one operation per altar and one per player. Invitations do not lock the altar.
- **Requests:** rate-limited to 5 per second per player, bounded to 4 KiB, and must name the open menu id,
  the session nonce and the current rules fingerprint. A stale or spoofed request does nothing.
- **Cancellation** consumes nothing: closing the menu, leaving range, death, logout, dimension travel,
  damage, lost access, the altar broken or unloaded, a rules or data reload, or the offering stacks moved.
  Rituals that need two players need both to keep the menu open, in range and unhurt.
- **Founding specifics:** any time, moon or dimension. Completion needs an empty main-inventory slot for
  the Grimoire; a full inventory blocks completion and nothing is consumed (the book is never dropped).
- **Initiation specifics:** a witch invites a nearby non-witch who can also use that altar. One outgoing
  invitation per witch, one incoming per recipient, 600-tick expiry (`inviteExpiry`), 100-tick cooldown
  (`inviteCooldown`), duplicates never extend. The recipient gets a chat line, opens the same altar and
  chooses "Accept and begin" or "Decline". Acceptance revalidates both players, the altar, sessions,
  status, the initiator's offering and 50 energy, then takes the invitation by its exact id (replays and
  spoofed ids do nothing). The recipient pays nothing. An interrupted initiation needs a fresh invitation.
- **Grimoire:** completing founding or initiation grants the witchcraft and action page discoveries
  through `DiscoveryService`. They are records, never permission.

## Energy and practice

Energy and practice are stored as fixed-point hundredths. Capacity is derived, never saved: the initial
capacity plus `capacityPerThreshold` for each practice threshold reached.

| Practice threshold | Capacity after |
|---|---|
| start | 100 |
| 50 | 110 |
| 100 | 120 |
| 150 | 130 |
| 200 | 140 |
| 250 | 150 |
| 300 | 160 |
| 350 | 170 |
| 400 | 180 |
| 450 | 190 |
| 500 | 200 (final) |

Practice comes from successfully spent energy. Zero energy never hurts or removes status; paid
operations refuse with a reason.

### Recovery

| Source | Rule | Default |
|---|---|---|
| Passive rest | still within 0.1 block of where the rest began; sprinting, swimming, flying, riding, being hurt, using an item or spending energy reset it | 1 per 100 ticks |
| Meditation | at an idle altar; explicit start; passive rest suspended | 40-tick settle, then 2 per 20 ticks, stops at full |
| Sleep | `SleepFinishedTimeEvent` (the server's sleep-completion path), only for players asleep 100 ticks or more; day = new day time / 24000; only a strictly newer day rewards; backward time cannot reopen a day | +30 |
| Energy tea | food for everyone; the energy is for witches only; an authoritative saved cooldown for every player | +25, cooldown 1,200 online ticks |
| Restful infusion | still resets TIME_SINCE_REST and forces no sleep; for witches prepares the next rewarded sleep, non-stacking, re-drinking refreshes the duration; a non-rewarded sleep does not consume it | +30 more, 24,000 ticks |
| Bonus | evaluated at the player's own loaded position at most once a second, only for rest and meditation | see below |

Sleep notes: entering a bed, setting spawn, resetting TIME_SINCE_REST and `/time set` do nothing. M5
beds qualify because they are vanilla-compatible beds.

Bonus (summed, then capped): a biome in `herbalwitch:witch/energy/nature` +10%; a visible full moon at
night in a normal-sky dimension +15%; the cap is 25%.

### State rules

- Missing data is a fresh non-witch. A missing or unsupported schema, or a malformed field, is preserved
  unchanged, the player is treated as a non-witch and nothing changes; it is logged and
  `/herbalwitch witch inspect` shows it.
- The capability copies once on death and End return regardless of keepInventory. Activities clear on
  death, logout and dimension change.
- Snapshots go only to the owner: a new epoch on login, respawn and dimension change, an incrementing
  revision, at most every 5 ticks, immediately after a conversion or ritual result. The client ignores an
  older epoch or revision and clears on disconnect and local player replacement.
- `WitchStatusService` is the only mutation API (check, debit, recover, award practice, convert,
  snapshot, restore).

## Configuration defaults

`herbalwitch-server.toml` (per world). The whole set is validated together; an invalid edit keeps the
previous rules and logs. A valid change cancels pending invitations and running rituals, clamps energy
without refilling it, and resends the rules to clients.

| Section | Key | Default |
|---|---|---|
| `initiation` | `foundingEnabled` | true |
| `initiation` | `initiationEnabled` | true |
| `initiation` | `foundingTicks` | 200 |
| `initiation` | `initiationTicks` | 200 |
| `initiation` | `inviteExpiry` | 600 |
| `initiation` | `inviteCooldown` | 100 |
| `initiation` | `initiationCost` | 50 |
| `capacity` | `initialCapacity` | 100 |
| `capacity` | `practiceThresholds` | 50, 100, 150, 200, 250, 300, 350, 400, 450, 500 |
| `capacity` | `capacityPerThreshold` | 10 (final capacity 200) |
| `capacity` | `attunementCost` | 10 |
| `capacity` | `attunementTicks` | 100 |
| `recovery` | `restInterval` | 100 |
| `recovery` | `restAmount` | 1 |
| `recovery` | `meditationSettle` | 40 |
| `recovery` | `meditationInterval` | 20 |
| `recovery` | `meditationAmount` | 2 |
| `recovery` | `sleepAmount` | 30 |
| `recovery` | `teaAmount` | 25 |
| `recovery` | `teaCooldown` | 1200 |
| `recovery` | `restfulBonus` | 30 |
| `recovery` | `restfulDuration` | 24000 |
| `recovery` | `natureBonus` | 10 (percent) |
| `recovery` | `moonBonus` | 15 (percent) |
| `recovery` | `bonusCap` | 25 (percent) |

`herbalwitch-client.toml` `[hud]`: `enabled` true; `anchor` `BOTTOM_LEFT` (nine-way); `offsetX` 8;
`offsetY` 18 (clears the chat input); `scale` 1.0 (range 0.25 to 4); `numeric` true;
`recoveryIndicator` true.

Clients receive the rules (`WitchRulesUpdate`), so the grimoire quotes them; page prose never
hard-codes a number and a test enforces it.

## Founding record and operator procedure

`FoundingSavedData` states: OPEN; CLAIMED (transient: a restart reopens); COMMITTING (a durable claim
written straight to the file right before the irreversible completion; if that write fails the
completion aborts before anything is consumed); COMPLETE; NEEDS_REVIEW (a world loaded in COMMITTING, or
an unreadable record). The first conversion by any route closes OPEN and voids a CLAIMED attempt. There
is no automatic reopening. One claim per world.

Commands (level 2):

| Command | Effect |
|---|---|
| `/herbalwitch witch inspect <player>` | the player's witch state, including a preserved unreadable record |
| `/herbalwitch witch designate <player>` | an invitation with no initiator, offering or energy, accepted at any valid altar |
| `/herbalwitch witch founding inspect` | the founding record's state and claim |
| `/herbalwitch witch founding resolve <claim> close` | NEEDS_REVIEW to COMPLETE; nothing is replayed |

Recovering from NEEDS_REVIEW (a crash between the durable founding claim and the player save):

1. `/herbalwitch witch founding inspect` and note the claim id.
2. Decide what happened: `/herbalwitch witch inspect <player>` for the founder. If the founder is a
   witch, the crash fell after the player save and only the record needs closing. If not, the founding
   was lost with the crash and the founder's offering and Grimoire were never granted.
3. `/herbalwitch witch founding resolve <claim> close` closes the record as COMPLETE. Nothing is
   replayed: no item, energy or status is granted or removed.
4. If the founder's status must be restored, `/herbalwitch witch designate <founder>` and let them accept
   at any valid altar.

An unreadable record cannot be resolved by command; it stays preserved for manual repair.

## Showcase and tests

Showcase section `altar` (7 wide): the idle altar facing the entrance, a supply barrel (2 lavender, 4
sage, mint, chamomile, 2 amethyst, a valid signed book, a grimoire, 2 energy tea, 2 restful infusions)
and signs (Witch's Altar, Use the altar, Founding, Supplies, Initiation, Recovery, Practice). Building
it initiates nobody, touches no founding record, grants no energy and starts no ritual. Full layout 126
x 8 x 28.

Tests: pure unit tests under `src/test/.../magic/` and `client/witch/hud/`; resource tests
`data/magic/{ritual,witch}`; GameTests under `src/gameTest/.../magic/{witch,ritual}` (WitchLifecycle,
EnergyRecovery, AltarPlacement, AltarAction, AltarShowcase). Not covered: a true out-of-process server
restart; the vanilla sleep path (tested through `EnergyEvents.sleptThrough`).
