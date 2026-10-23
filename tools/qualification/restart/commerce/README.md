# Commerce restart qualification

`commerceRestartTest` is the ANU-only named heavier check for Milestone 11. It starts two sequential
dedicated GameTest JVMs in a fresh `run/commerce-restart/<uuid>/` world and then validates their
evidence with `verify-report.mjs`. It is excluded from `check` and `build`; never run it locally.

```powershell
.\tools\anu-test.ps1 -Mod HerbalWitch -Ref <committed-branch> -Path <worktree> -Tasks commerceRestartTest -Wait
```

**Prepare** runs real transfers through the production path (deposit, purchase, stock withdrawal,
proceeds withdrawal) and stops each one at a durable boundary: before the prepare checkpoint is
written, after it but before the player save, after the player save, before the shop commit is
written, and after a normal commit. The stop is a thrown `Error` from a test-source probe (the shared
writer's `SnapshotProbe` or the player checkpoint seam), so no production `catch` block runs and nothing
more is written: the files on disk are exactly those of a process that died there. The probe copies the
shop shard and the player's saved data at that instant into `evidence/<scenario>/`. Villager sales stop
before and after their durable budget debit and copy the demand record too. An older player file and a
missing player file accompany one prepared purchase.

**Verify** restores each scenario's bytes into an isolated store directory (and the player's file into
the world's `playerdata/`), indexes them with the production `ShopStore` and runs the production startup
reconciliation. It asserts: unwritten prepares leave no transaction; prepared transfers whose player
file proves an untouched before-state roll back; a receipt-bearing player file finalizes exactly once;
older or missing player files keep the transaction quarantined; a debited villager sale finalizes and
an undebited one rolls back; a corrupt shard stays byte-identical and unavailable. For player transfers
it also checks conservation of usable value (player inventory, stock, proceeds and any quarantined
reservation) against the totals recorded before the transfer.

The report requires distinct JVM process IDs, all 24 scenarios and the corrupt-shard check.

**Limits.** This is deterministic boundary replay across processes, not random power loss. The
integrated-server host path (`level.dat`'s `Player` tag, written and verified by
`ShopPlayerCheckpoint.host`) cannot run on a dedicated test server and remains **unqualified** by
automation; it needs an owner check in a single-player world. Menu sessions are in-memory only, so a
request replayed after a restart has no session to act on; the replay GameTests cover stale and
duplicate requests within a running server.
