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
  are **owner-checked** in the mod-test instance (no transfer buttons yet).
- No adapters for Supplementaries or AppleSkin (AppleSkin reads the energy tea's ordinary food data). The Curios adapter arrived in 0.6.0 (see "Advanced brewing, charms, circle and wards" below).

## Grimoire and the viewers (0.3.0, Milestone 4)

- **Shared descriptions.** The grimoire, EMI and JEI all describe processing recipes through
  `apothecary/stations/catalog` (`ProcessingCategories` holds the one recipe-type-to-station mapping, with each
  category's heated flag), so a station's categories cannot differ between them.
- **Viewers are not gated by discovery.** EMI and JEI still show every recipe whether or not the player has
  discovered it; nothing is hidden. There are still no transfer handlers (no transfer buttons).
- **No hard dependency.** The grimoire needs neither viewer; the "Grimoire" button on station screens works
  with neither, either or both installed (an owner check).
- **Profile evidence:** the viewer profiles (`emi`, `jei`, `viewers`) and `all` were **not run for 0.3.0**;
  the table below still records the 0.1.0-dev.4 / 0.2.0 evidence. They are pending.

## Integrations

Milestones are derived from plan section 3; exact dependency versions are pinned from the trial instance when each integration is implemented.

| Integration | Planned behaviour | Milestone | Tested version |
|---|---|---|---|
| Farmer's Delight | Independent kettle and recipes; a lit FD stove counts as heat through an optional adapter/tag (unlit gives none); hearth heat offered to FD where its API/tag contract permits; no duplicate cooking pot | 3 | 1.3.4 |
| Thirst Was Taken | Teas registered via the installed version's drink mechanism; water-purity data kept; hydration applied once on consumption; without Thirst, vanilla/herbal effects only | 3 | 1.4.0 |
| Curios | Charms use a vanilla fallback when absent (the off hand); with Curios, the `charm` slot is used and the same charm effect cannot stack through both paths | 7 | compile-only against 5.14.1+1.20.1; the adapter is registered only when Curios is loaded; not run with Curios installed (owner check) |
| Doggy Talents Next | Its dogs stay under its own ownership/AI; excluded from binding and capture by default; no conversion of trained dogs; vanilla wolves proceed independently; opt-in needs a later tested adapter | 8-9 | not yet; pin the trial instance's version when implemented |
| MineColonies | Shops are player shops, not colony buildings or simulated customers; an authorization adapter respects colony permissions for custom world/entity actions | 7 (authorization), 11 (shops) | M7: MineColonies is a recognised claim mod, so entity actions are restricted while it is loaded; no colony-permission adapter; not run against it |
| Create 6 | Station item handlers may feed hoppers/funnels under documented side rules; no kinetic or fluid dependency; shop inventories and capture payloads never exposed as automation storage | 3 (stations), 11 (shop rule) | not yet; pin the trial instance's version when implemented |
| Supplementaries / Macaw's / Roundwood | Independent namespaced furniture; shared ordinary tags where fitting; no overrides of their blocks or recipes; test adjacency, support, waterlogging, doors | 5 | not yet (furniture is implemented in 0.4.0, see "Furniture" above; this mod was not run against it); pin the trial instance's version |
| EMI / JEI | Separate optional client-only plugins on shared recipe descriptions; test neither, each alone, both; transfer buttons only after a tested server transfer endpoint | 3 (first categories), 4 (shared descriptions) | EMI 1.1.24+1.20.1, JEI 15.62.0.219 (owner check for viewers) |
| Oculus / Embeddium | Baked models first; restricted BERs and bounded particles; owner checks shader off/on, reloads, glass, shadows, item displays | 1 onward (owner check per decor wave) | not yet; pin the trial instance's version when implemented |
| AppleSkin | Normal food/item contracts; no separate HUD hook unless an observed need appears | 3 | no adapter; not separately tested |
| Claims / PvP | Custom actions route through the permission service and Forge events; supported claim adapters tested explicitly; recognized but unsupported protection systems disable affected automated actions by default; harmful player targeting off by default | 6-7 | M6 altar use routes through `ActionPermissionService.mayUse` and Forge events only, no claim adapter (see "Witchcraft"); M7 entity actions go through `EntityActionPermission` (PvP off by default, recognised claim mods restrict them); not yet tested against a claim mod; pin the trial instance's version when implemented |

## Furniture (0.4.0, Milestone 5)

What the 94-piece furniture catalogue does with other mods today (docs/catalogue/decor.md):

- **Claims and protection mods:** placing a piece fires Forge's multi-place event once for the whole piece,
  so a claim mod can cancel it and the piece, its item and the neighbouring blocks are all restored.
  Using furniture (sitting, opening a cabinet, toggling a light, the door, sleeping) follows Forge's
  interaction events, but not the build permission; building permission applies to placement only.
- **Item automation (Create funnels, hoppers, other item-handler mods):** cabinets expose the Forge item
  handler on every cell, all backed by the one shared inventory. Display shelves expose none; they are
  filled and emptied by hand.
- **Beds and the door** are vanilla `BedBlock` and `DoorBlock` subclasses, so vanilla sleeping, respawn and
  door rules apply.
- **Not tested:** the optional-mod profiles (EMI, JEI, Farmer's Delight, Thirst) were not rerun for this
  milestone, and no furniture mod (Supplementaries, Macaw's, Roundwood), shader pack or claim mod was run
  against the catalogue. The owner checks neighbouring-block occlusion and shaders in the trial instance.

## Witchcraft (0.5.0, Milestone 6)

- **No new optional integrations.** The altar, energy and initiation need no foreign mod, and no foreign
  classes were added.
- **Thirst Was Taken:** the Thirst profile still applies to energy tea. Hydration from the Thirst adapter is
  unchanged; the energy benefit for witches is independent of it.
- **Protection mods:** altar use goes through `ActionPermissionService.mayUse` (use, not build: loaded,
  world border, build height and spawn protection; adventure mode may use the altar) plus Forge's
  interaction events. Placing the altar is a normal checked structure placement. There is no universal
  claims-mod support and no claim adapter; a claim mod that cancels interaction events stops altar use.
- **Not tested:** the optional-mod profiles were not rerun for this milestone, and no claim mod was run
  against the altar.

## Advanced brewing, charms, circle and wards (0.6.0, Milestone 7)

- **Curios (optional, compile-only API 5.14.1+1.20.1).** `platform/compat/curios/CuriosCharmAdapter` is
  registered only when Curios is loaded; the jar holds no Curios classes.
  `data/herbalwitch/curios/entities/charms.json` gives players a `charm` slot and the item tag
  `curios:charm` lists the charms. Without Curios a charm works from the off hand. The Curios path was not
  run headless (no profile loads it); it is an owner check.
- **Claim and protection mods.** Harmful entity actions (thrown vials, weapon coatings, thorns, binding,
  sleep, ward suppression) go through `EntityActionPermission`. While a recognised claim mod is loaded
  (FTB Chunks, Open Parties and Claims, Cadmus, Flan, GriefDefender, MineColonies) those actions are
  restricted, because the mod cannot be asked; there is no per-claim adapter. Harm to players needs
  `preparationPvp`, server PvP and team rules to allow it. Villagers and wandering traders are protected by
  the `protected_entities` tag; fake players are treated as players.
- **Viewers.** JEI and EMI gained ritual categories (`client/compat/ritual/RitualCardLayout`) and show the
  witch requirement on processing recipes. Compile-and-load only; the viewer profiles were not rerun for
  this milestone and the categories are owner checks.
- **Not tested:** the optional-mod profiles (EMI, JEI, Farmer's Delight, Thirst, Curios) were not rerun for
  this milestone, and no claim mod was run against the wards or the vials.

## Rule

Optional integrations stay optional. Foreign mods and their classes never ship in the jar. Herbal
Witch's own optional adapter classes do ship, guarded so they load only when their mod is present: they
live under `platform/compat/<integration>/` (or `client/compat/<viewer>/` for viewers). There is no
claim of universal compatibility.
