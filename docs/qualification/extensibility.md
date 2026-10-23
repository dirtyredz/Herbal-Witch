# Configuration, datapack and KubeJS review (Milestone 12)

What a server owner or pack maker can change, where it lives, when it applies and what it does to
existing saves. Per-key defaults and meaning stay in the catalogues; this is the map.

## Configuration files

All server files are per world (`<world>/serverconfig/`), validated as a whole, and installed on the
server thread. A valid edit installs the new rules and bumps a revision; an invalid whole-file edit
keeps the previous rules and logs why (Forge itself corrects single out-of-range values to the default).
Nothing below deletes stored value when a limit is lowered.

| File | Owner (code) | Sections and keys | Applies | Lowering a limit or disabling |
|---|---|---|---|---|
| `herbalwitch-server.toml` | `magic/witch/config/WitchServerConfig` (+ `magic/broom/config`, `magic/wand/config`) | `initiation` (founding/initiation switches, ticks, invite expiry/cooldown, cost), `capacity`, `recovery` (rest, meditation, sleep, tea, restful, nature/moon bonus, cap), `workings` (`processingPercent`, Garden Blessing, Home Protection, `preparationPvp`), `wards`, `charms`, `broom` (enabled, speeds, `energyPerSecond`, wear, `fallProtection`), `wand` (enabled, reach, cooldown, collection bounds) | Live; synced to clients (rules packet); cancels pending invitations and running rituals | Energy is clamped, never refilled; founding/initiation switches stop new conversions only; existing wards keep their records (limits gate new activations) |
| `herbalwitch-animals-server.toml` | `companions/animals/config/AnimalServerConfig` | relationships, attraction, calming, `earnedTaming`, `newCaptures`, `moddedParticipation`, per-animal/AI budgets, payload and snapshot bounds, stored/lifetime capture limits, `transactionInterval` | Live; AI and inspection invalidate lazily | `newCaptures=false` still allows stored release and operator recovery (CaptureCheckpointTest, AnimalRecoveryGameTests) |
| `herbalwitch-familiars-server.toml` | `companions/familiars/config/FamiliarServerConfig` | `newBindings`, `bindingSpecies`, `publicRetrieval`, `mobilePerOwner` (1-8, default 3), record bytes, transaction interval, scan/path budgets, guard/sense radius, offline guards | Live; queued work and private marks are cancelled by revision | A lower mobile cap demotes the newest mobile familiars to Stay; acquisition switches never delete bonds (FamiliarConfigGameTests) |
| `herbalwitch-commerce-server.toml` | `apothecary/shop/config/ShopServerConfig` | `shops` (per owner/world, proceeds capacity, order lifetime), `villagerCustomers` (enabled, economy window, world/owner/shop spending, purchases, visit interval/duration/concurrency) | Live; sessions and customer visits invalidated by revision | Existing shops, stock and proceeds stay; new shops and visits obey the new limits (ShopDemandGameTests, NpcDemandTest) |
| `herbalwitch-client.toml` | `client/magic/witch/WitchClientConfig` | `hud` (enabled, anchor, offsets, scale, numeric, recovery indicator) | Live, client only | n/a |

Boss immunity and harmful-control eligibility are data, not toml: the entity tags
`hard_control_immune` and `hard_control_allowed`.

## Datapack seams

| Seam | Location (`data/herbalwitch/...`) | Reload | Notes |
|---|---|---|---|
| Processing recipes (11 types: drying, grinding, steeping, infusion, cauldron, distillation, bottling, charm_work, broom_work, wand_carving, animals) | `recipes/processing/<type>/` | `/reload` | Exact-slot recipes with `count`, optional `returns`, `requirements` (`witch`, `energy`). A changed or removed recipe cancels running jobs that used it; items stay (ProcessBehaviourGameTests, save-upgrade behave phase). Viewers and the grimoire re-project. |
| Heat | `tags/blocks/heat_sources`, `heat_conductors` | `/reload` | Farmer's Delight stove offered through `data/server/compat` tags |
| Herb conditions | `herb_conditions/<group>/<herb>.json` | `/reload` | Server only; clients get summaries through content sync |
| Grimoire prose | `grimoire_entries/{guide,stations}` | `/reload` | Editorial text; rules are quoted from synced numbers |
| Animals | `tags/entity_types/animals/*`, `tags/items/animals/*` | `/reload` | Tags select among existing adapters; they never add an ownership, AI or serialization adapter |
| Familiar retrieval | `tags/items/familiars/*` | `/reload` | Small-object allowlist |
| Protection | `tags/entity_types/{protected_entities,undead,arthropods,repelled_pests}` | `/reload` | |
| Worldgen | `worldgen/`, `forge/biome_modifier/` | Restart (new chunks only) | Herb habitats |
| Loot | `loot_tables/`, `loot_modifiers/` (grass foraging, resin) | `/reload` | |

## KubeJS

Tested on the production server (`qualifyDedicatedServer`, profile `kubejs`): KubeJS
2001.6.5-build.26, Rhino 2001.2.3-build.10, Architectury 9.2.14 (hashes in `tools/qualification/server`
evidence). The script `tools/qualification/server/kubejs/server_scripts/herbalwitch_qualification.js`
adds a grinding recipe with `event.custom(...)` and removes one with `event.remove({id})`; the recipe
manager reports both, and a mortar placed by command grinds the added recipe on the running server.
KubeJS recipe events run at datapack load (server start or `/reload`). There is no bespoke KubeJS API,
binding or event; custom recipes use the same JSON as datapacks.

## Not supported

- Arbitrary modded animals or familiars: `moddedParticipation` lets tags include them in care only
  where an exact vanilla adapter exists; no modded capture, binding or ownership adapter ships.
- External economies: commerce uses plain emeralds (DECISIONS M11).
- Claim mods: recognised claim mods restrict harmful and automated actions; no per-claim adapter.
