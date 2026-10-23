# Magic tool restart qualification

The ANU-only named heavier task `magicToolsRestartTest` starts two sequential dedicated GameTest JVMs against one fresh disposable world under `run/magicTools-restart/<uuid>`. It is excluded from ordinary build/check and must never run locally.

Queue the committed SHA through the workspace's `tools/anu-test.ps1 -Mod HerbalWitch -Ref <sha> -Path <worktree> -Tasks magicToolsRestartTest -Wait`, or use `tools/test/magic-tools-restart.ps1 -WorkspaceRoot <workspace> -Ref <sha>`.

Prepare places a real owner-bound broom, issues an input lease, saves a container holding a named worn broom and all seven wand preferences, and starts an attended repair. The normal server save and shutdown persist the world. Verify loads those actual chunks in another JVM, waits for entity admission, checks exactly one broom and no resumed lease, confirms every packed field and preference, and verifies the interrupted repair remains paused without charging. Cancellation must release the original named broom. An old nonce cannot move the recovered vehicle.

Evidence records distinct process IDs and each required assertion. The locator/expectation NBT is not used to recreate the broom, barrel or station. Player state is separately replayed from its actual serialized NBT so the saved repair payer can exercise the normal cancellation API. This qualifies normal clean restart; it does not promise atomic item/entity transfers under arbitrary power loss across Minecraft's separate chunk/player files. Owner visuals and two-client network presentation remain dedicated-instance checks.
