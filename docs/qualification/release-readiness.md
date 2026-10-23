# Release readiness register (Milestone 12)

Everything that stands between the qualified candidate and a public 1.0.0, with its owner and next step.
Automated qualification passing (README) makes a **candidate**; these items decide a **release**.
Severity: **blocker** (release cannot happen with it open), **owner** (needs the owner's eyes or ruling),
**known limit** (documented and accepted unless the owner says otherwise).

| ID | Severity | Item | Evidence / where | Next step | Docket |
|---|---|---|---|---|---|
| R-OWNER | blocker | Owner in-game acceptance of every milestone (M1-M12): visuals, transforms, screens, shaders, two clients, single player | docs/qualification/owner-acceptance.md (all cases not run) | Owner runs the checklist in the `Herbal Witch Test` instance | dk-7754f072 |
| R-HOST | blocker | The integrated host's commerce player checkpoint (`level.dat` Player tag) is implemented but cannot run on a dedicated test server, so no automated run has proved it | GOTCHAS "integrated host"; commerce restart README | Owner case M11-6 (single-player crash mid-trade); a failure is a P0 | dk-b002e503 |
| R-ART-1 | owner | Umbrella stand is orange barrel wood, off the set's single dark wood | previews/rc1 decor sheet; docs/ART.md release sweep | Owner repaints in Blockbench or accepts | dk-790e589a |
| R-ART-2 | owner | Agent first drafts awaiting the owner's art pass: 89 M5 pieces, altar, circle, ward, advanced stations, M8 animal pieces, familiar homes/gear/rigs, broom stand, wand bench, shop pieces; cauldron and still read orange (copper) | docs/ART.md; previews/rc1 | Owner case M5-1, M12-6 | dk-d9d25d58 (M8 part) |
| R-DESIGN-1 | owner | Design interpretations to confirm: hearth "home anchor" = ward focus; "optional experimentation" = advisory discovery; modded-mob adapters optional and absent | docs/qualification/design-audit.md (D-3-15, D-13-12, D-7-10) | Owner rules: accept, or schedule work | dk-342d2ab0 |
| R-COMPAT | known limit | Recipe viewer screens, Curios slot UI, shaders (Oculus/Embeddium) and two-client sync are client-only and owner-checked; Create, Supplementaries, Macaw's, Roundwood, MineColonies and claim mods are untested and unsupported beyond the documented conservative rules | docs/COMPATIBILITY.md | Owner cases M4-4, M7-4, M12-4 | - |
| R-PERF | PERF_SEVERITY | PERF_SUMMARY | docs/qualification/performance.md | PERF_NEXT | PERF_DOCKET |
| R-CRASH | known limit | Crash safety is deterministic boundary replay across processes (capture, familiar, tools, commerce), not random power loss; Minecraft saves are not transactions across player, chunk and data files | restart READMEs; save-compatibility.md | none | - |
| R-STRUCT | known limit | Structural P2 debt, no behaviour impact: grimoire/client single-file jobs, brewing registry catch-alls, process station seams, small cleanups, witch-status residue | Docket | Schedule after release | dk-1aa5c349, dk-9aa8a47a, dk-1967a7e5, dk-21d69e74, dk-41d0eae3 |
| R-UNVERIFIED | owner | Behaviours with code but no automated proof: familiar names (vanilla name tag, no rename UI), "broom is not a weapon", two simultaneous real players | design-audit.md (D-8-3, D-11-4, D-14-42) | Owner cases M9-1, M10-1, M11-2 | - |

Fixed during M12 (no longer open): a write-locked capture ledger deleted ordinary animals on chunk load;
herds or dropped items hid hostiles from familiar guards, scouts and senses; foxes lacked their promised
stealth; the seat and thrown-preparation entities had no English name. Each has a regression test.
