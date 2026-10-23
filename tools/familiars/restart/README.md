# Familiar restart qualification

`familiarRestartTest` is the named ANU-only heavier check. It runs two sequential Forge GameTest
server JVMs in a fresh `run/familiar-restart/<uuid>/` directory, then validates their evidence.
It is deliberately absent from `check` and `build`; never run these servers locally.

From the Minecraft workspace:

```powershell
.\tools\anu-test.ps1 -Mod HerbalWitch -Ref <committed-branch> -Path <worktree> -Tasks familiarRestartTest -Wait
```

The preparation process uses actual binding, equipment menu clicks, loaded recall, lethal damage,
revival, item pickup and cargo collection. A thread-confined test-source observer copies the exact
shard and contemporaneous player-save bytes immediately before/after atomic replacement. It retains
old native/current entity NBT as stale chunk input. A deliberately failed final verification must
make the live shard read-only. The observer is removed at batch cleanup, including failed tests.

The verification process reopens each checkpoint with the production index/receipt loader and runs
real entity admission against it. It checks preserved canonical inventories and payloads, rejects old
sources/generations, and quarantines duplicate current incarnations. The source-item retirement file
must reject the older item NBT after restart. Isolated damaged-schema/checksum fixtures must remain
byte-identical and block writes; an older player save must quarantine economic authority. A 97-record
index must require four batches of at most 32 records.

This is deterministic checkpoint-boundary replay across processes, not random OS termination or a
claim of power-loss durability of the host filesystem. No codec-only result is counted as a restart.
Evidence stays beside the isolated world: `evidence/prepared.json`, `verified.json`, numbered
checkpoint directories and their original entity/player files. The Node report requires distinct JVM
process IDs, every lifecycle phase, 20–192 checkpoints and all required checks before reporting PASS.
The ordinary server suite continues to cover actual transactions and denied actions independently.

The transport/owner fixture additionally captures a real M8 capture-release-to-binding conversion. The second JVM rejects its copied old token and stale source entity, preserves released generation one, keeps the owner-death command at Stay, and resolves an unloaded home as pending without loading its terrain. Logout/reconnect use the Forge event bus; owner death uses actual lethal damage. The ordinary suite separately tests real cross-dimension owner movement and paid recall.
