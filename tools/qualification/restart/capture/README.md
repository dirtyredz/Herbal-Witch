# Capture restart qualification (Milestone 8, qualified in M12)

`captureRestartTest` is ANU-only and excluded from `check` and `build`; never run it locally.

```powershell
.\tools\anu-test.ps1 -Mod HerbalWitch -Ref <committed-branch> -Path <worktree> -Tasks captureRestartTest -Wait
```

**Prepare** (first dedicated JVM) runs eleven real captures and releases through `CaptureService` and
stops each one at a ledger boundary: before the PREPARED or final write starts, before the atomic
replace, and after the replace (before read-back verification), plus one completed capture and one
completed release (persisted by the ordinary compacting save). The stop is a thrown `Error` from the
test-source `SnapshotProbe`, so no production `catch` block runs and nothing more is written: the
ledger file copied into `evidence/<scenario>/ledger.dat` is exactly what a process that died there
leaves. `case.nbt` keeps the animal as its chunk would hold it (the source before capture, the
inserted incarnation during release), the issued token and the held item.

**Verify** (second JVM) restores each ledger, lets the production loader reconcile it, and drives the
production paths. Expectations: nothing durable leaves the source animal free to return; a durable
PREPARED or un-compacted RELEASED record is quarantined, every stale incarnation and held token is
refused, and operator recovery yields exactly one animal of the next generation; a stored capture whose
item never arrived is reissued; a stored token releases once and a copy never does; a compact release
admits only the released incarnation. A corrupt ledger stays byte-identical, refuses transport and
transported incarnations, and still admits never-transported animals.

**Limits.** Deterministic boundary replay, not random power loss. Chunk and player files are not
written in step with the ledger (Minecraft saves are not transactions); the scenarios model the stale
chunk copies such a crash leaves.
