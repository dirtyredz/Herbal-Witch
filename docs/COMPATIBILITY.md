# Compatibility

Records the supported platform, what is actually tested, and each planned optional integration with the milestone it lands in. Nothing here is a promise of universal compatibility.

Supported platform: Minecraft 1.20.1, Forge 47.x (built and tested on 47.4.10), Java 17. Install on both server and clients.

## Tested now (0.1.0-dev.4)

Exact versions: Farmer's Delight 1.3.4, Thirst Was Taken 1.4.0, EMI 1.1.24+1.20.1, JEI 15.62.0.219.

- No required dependencies. The dedicated server boots via GameTests (`runGameTestServer`).
- **Headless (GameTest server):** Farmer's Delight stove heating (lit and unlit), the hearth in the FD
  heat tag, Thirst drink registration (hydration 6, quenched 3) and purity carry, with the optional
  mods loaded through `-PcompatProfile=all` and `-PcompatModsDir=<mods folder>`.
- **Compile and load only:** the EMI and JEI plugins compile against their APIs and load with the
  profiles `emi`, `jei`, `viewers`. Viewers are client-only, so categories, workstations and layout
  are **owner-checked** in the trial instance (no transfer buttons yet).
- No adapters for Curios, Supplementaries or AppleSkin (AppleSkin reads the energy tea's ordinary food data).

## Integrations

Milestones are derived from plan section 3; exact dependency versions are pinned from the trial instance when each integration is implemented.

| Integration | Planned behaviour | Milestone | Tested version |
|---|---|---|---|
| Farmer's Delight | Independent kettle and recipes; a lit FD stove counts as heat through an optional adapter/tag (unlit gives none); hearth heat offered to FD where its API/tag contract permits; no duplicate cooking pot | 3 | 1.3.4 |
| Thirst Was Taken | Teas registered via the installed version's drink mechanism; water-purity data kept; hydration applied once on consumption; without Thirst, vanilla/herbal effects only | 3 | 1.4.0 |
| Curios | Charms use a vanilla fallback when absent; with Curios, tested slots are used and the same charm effect cannot stack through both paths | 7 | not yet; pin the trial instance's version when implemented |
| Doggy Talents Next | Its dogs stay under its own ownership/AI; excluded from binding and capture by default; no conversion of trained dogs; vanilla wolves proceed independently; opt-in needs a later tested adapter | 8-9 | not yet; pin the trial instance's version when implemented |
| MineColonies | Shops are player shops, not colony buildings or simulated customers; an authorization adapter respects colony permissions for custom world/entity actions | 7 (authorization), 11 (shops) | not yet; pin the trial instance's version when implemented |
| Create 6 | Station item handlers may feed hoppers/funnels under documented side rules; no kinetic or fluid dependency; shop inventories and capture payloads never exposed as automation storage | 3 (stations), 11 (shop rule) | not yet; pin the trial instance's version when implemented |
| Supplementaries / Macaw's / Roundwood | Independent namespaced furniture; shared ordinary tags where fitting; no overrides of their blocks or recipes; test adjacency, support, waterlogging, doors | 5 | not yet; pin the trial instance's version when implemented |
| EMI / JEI | Separate optional client-only plugins on shared recipe descriptions; test neither, each alone, both; transfer buttons only after a tested server transfer endpoint | 3 (first categories), 4 (shared descriptions) | EMI 1.1.24+1.20.1, JEI 15.62.0.219 (owner check for viewers) |
| Oculus / Embeddium | Baked models first; restricted BERs and bounded particles; owner checks shader off/on, reloads, glass, shadows, item displays | 1 onward (owner check per decor wave) | not yet; pin the trial instance's version when implemented |
| AppleSkin | Normal food/item contracts; no separate HUD hook unless an observed need appears | 3 | no adapter; not separately tested |
| Claims / PvP | Custom actions route through the permission service and Forge events; supported claim adapters tested explicitly; recognized but unsupported protection systems disable affected automated actions by default; harmful player targeting off by default | 6-7 | not yet; pin the trial instance's version when implemented |

## Rule

Optional integrations stay optional. Foreign mods and their classes never ship in the jar. Herbal
Witch's own optional adapter classes do ship, guarded so they load only when their mod is present: they
live under `platform/compat/<integration>/` (or `client/compat/<viewer>/` for viewers). There is no
claim of universal compatibility.
