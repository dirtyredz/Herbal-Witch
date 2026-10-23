# Save compatibility (Milestone 12)

What a world saved by an earlier Herbal Witch keeps when the candidate opens it, and how that is proven.

## Guarantee

Forward only: a world saved cleanly by any earlier private build (0.1.0 onward) opens in the candidate
with every Herbal Witch block, state, block entity, inventory and player capability intact. There is
no downgrade path. Persisted formats are versioned and an unknown version is preserved, unavailable
and never reset (station jobs, animal care, capture ledger, familiar shards, shop shards).
Interrupted durable transfers recover by proof (restart harnesses below). Arbitrary inconsistent
backups (player, chunk and SavedData files from different moments) are outside the guarantee.

## Real historical worlds (`saveUpgradeTest`)

The fixtures are the owner's own worlds, copied read-only from the MultiMC instances, not saves made by
the candidate's code:

| Fixture | Source | Played with | Content carried |
|---|---|---|---|
| owner-0.2.0-dev.9 | `Evil Eye Trial Cozy/saves/New World` | herbalwitch 0.2.0-dev.9 (pack instance) | 1,809 blocks (14 herbs at all ages, 389 herb beds, every station of the time, the five 0.1.0 pieces, hung bundles, oil lanterns), 49 block entities (schema-1 jobs, fuel, inventories), 1 player |
| owner-0.6.1 | `Herbal Witch Test/saves/New World` | herbalwitch 0.6.1 | 472 blocks across ~90 furniture pieces, altar, circle, ward stone, every station incl. advanced; 65 block entities incl. three running schema-2 witch jobs (cauldron, still, charm workbench) and 47 stocked storage/display pieces; 1 player with schema-1 witch status and discoveries |

`tools/qualification/save-upgrade/fixture.mjs` keeps every finished chunk holding our content plus a
one-chunk margin. The pack's other mods are absent on a Herbal Witch-only server, so their content is
replaced before the candidate sees it (foreign blocks by stone, foreign biomes by plains; foreign block
entities, chunk capabilities, scheduled ticks, structure references and foreign items inside our
inventories dropped) and counted in each fixture's `receipt.json` with the SHA-256 of every source file.
Proto chunks (still generating) are not carried: the candidate would finish generating them.

Three candidate JVMs per fixture: **upgrade** loads every listed chunk and player file, opens every
block entity as a player would and re-saves through the candidate's codecs; **reload** does it again;
**behave** runs the first tick of every saved station job. `compare.mjs` then requires: every block,
id and saved property kept (new properties take defaults); nothing appears; every block-entity field
kept except reviewed migrations; player capabilities kept; the second save byte-equal to the first;
three distinct JVMs; every first job tick conserves items, unchanged recipes resume, changed ones cancel.

Reviewed migrations (`tools/qualification/save-upgrade/expectations.json`): station `Job.Schema` 1 to 2;
the pre-0.4.0 small wall shelf gains an **empty** block entity on first use (its modelled bottles never
become items); the herb bed's four inner-corner properties appear (default false); a door's
`waterlogged=false` from a foreign pack mod is dropped (Herbal Witch never declared it).

Result: both fixtures pass (README for job ids). Evidence: ANU `run/save-upgrade/<uuid>/` and the local
copies under `C:/Users/dirty/projects/artifacts/minecraft-mods/jobs/2026-10-23-herbal-witch-m12/runs/`.

## Later formats (candidate across processes)

Formats introduced after 0.6.1 are qualified by restart harnesses that save in one dedicated JVM and
reconcile in another; no historical world with these records exists to copy:

| Format | Harness | Proves |
|---|---|---|
| M8 capture ledger (HWC8 envelope) | `captureRestartTest` | Eleven capture/release boundaries; quarantine and recovery to exactly one animal; corrupt ledger preserved |
| M9 familiar shards and receipts | `familiarRestartTest` | 76 checkpoint snapshots across two JVMs |
| M10 broom entity, packed tools, interrupted repair | `magicToolsRestartTest` | One broom, no restored lease, paused repair, no offline spend |
| M11 shop shards, player receipts, demand | `commerceRestartTest` | 24 boundary scenarios, conservation, corrupt shard |

The candidate changed no persisted codec after 0.10.0 (the M12 fixes touch join admission, a species
flag and language), so 0.10.0 saves are the candidate's own format.

## Gaps

- The integrated host's commerce checkpoint (`level.dat` Player tag) cannot run on a dedicated test
  server: owner case M11-6.
- No historical world held initiated witches, wards with records, captures or familiars; their codecs
  are covered by codec unit tests (WitchCodecTest, DiscoveryCodecTest, WardCodecTest, AnimalCodecTest,
  ShopCodecTest, BroomCodecTest) and by the restart harnesses, not by a historical save.
