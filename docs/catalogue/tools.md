# Broom and wand

Private M10 tools use the existing witch energy, ritual and familiar services. The grimoire displays live synchronized costs and recipe requirements; numbers below are defaults.

## Acquisition

All IDs below have the `herbalwitch:` namespace. Station items and blocks use `stations/tools/broom_stand/station` and `stations/tools/wand_bench/station`; tools use `magic/broom/broom` and `magic/wand/wand`.

| Recipe | Ingredients | Time / energy |
|---|---|---|
| Broom stand, crafting table (shapeless) | Crafting table, 2 dark oak planks, 2 sticks, string, iron ingot | Ordinary crafting |
| Wand bench, crafting table (shapeless) | Crafting table, 2 dark oak planks, flint, gold nugget | Ordinary crafting |
| `processing/broom_work/craft` | 2 sticks, 3 wheat, 2 string | 200 ticks / 10 energy |
| `processing/broom_work/repair` | Worn broom in base slot, 2 wheat, string | 100 ticks / 5 energy |
| `processing/wand_carving/craft` | 2 sticks, amethyst shard, gold nugget | 100 ticks / 5 energy |

Both two-cell stations use checked placement, support and obstruction rules. A nearby witch presses Start; inputs reserve while working, and payment occurs once at successful completion. Resume revalidates the attending operator. Cancel releases reservations. A full output or unavailable payer pauses completion. Repair restores up to 200 of 600 condition, preserving the broom's name, unknown item metadata and accrued wear/energy fractions. Ordinary automation cannot authorize a magical job.

## Broom

Deploy the item, then use the physical broom to mount. Only its deploying owner may mount or recover it; riding requires witch status. Owner sneak-use packs a grounded, empty broom into an empty inventory slot. Transfer ownership by giving the packed item to another player for deployment.

Movement keys steer, Jump ascends, Left Control descends, R requests landing, and Sneak dismounts. Descend and land are remappable. Opening a screen or losing focus sends neutral controls. The server validates bounded input against the current rider and lease, expires stale input, and controls movement; vanilla vehicle-position packets cannot reposition it.

Defaults: horizontal speed 8 blocks/second, vertical speed 4, acceleration 8, turn rate 120 degrees/second, landing speed 2. Upkeep is zero by default, configurable up to 20 energy/second. Paid active flight, including hovering, accrues one wear per 200 ticks; zero-cost flight still wears. At 600 wear, repair is required. Lowering the wear interval charges previously accrued ticks at the new interval on the next paid flight tick, bounded by 600 wear. Packing and repair preserve both fractional accumulators. Passive upkeep awards no practice.

Depletion, disabled rules, stale input and lifecycle exits end powered flight and initiate loaded-terrain descent. Logout, death and dimension changes leave the broom in its source dimension. It never loads chunks or crosses portals. Collision checks include rider headroom. Dismount protection is bounded to one fall within the configured interval (default 200 ticks); it is not void rescue. A saved mounted broom belongs to the chunk, never the player's RootVehicle copy. Restart clears riding and input leases. Normal Minecraft cross-file saves are not arbitrary-power-loss atomic item/entity transfers.

## Wand

Press V to select one of seven persistent modes. The five local utilities default to one energy per successful action, ten ticks of shared actor cooldown and five blocks of reach, further limited by the player's interaction reach. Refusals cost nothing. Casting while riding a broom is refused.

| Selector | Action and limits |
|---|---|
| Harvest | Mature catalogue herbs and vanilla wheat, carrots, potatoes and beetroot. Shares hand herb yield/enrichment rules; vanilla drops fund one replant seed. |
| Plant | Native placement of an eligible offhand herb seed, wheat/beetroot seed, carrot or potato. Retains survival, placement events and seed consumption. |
| Water | Moistens farmland or herb beds without changing other state. |
| Collect | Visible eligible drops within two blocks of the aim point; examines at most 16 entities and accepts at most 8 stacks, retaining overflow and partial remainders. |
| Light | Vanilla candles, candle cakes and campfires, or actual fueled hearths/stoves. No arbitrary fire and no fabricated fuel. |
| Ritual/Ward Focus | Opens existing altar, circle or ward controls. Existing sessions, offerings, consent, ownership and prices remain authoritative. |
| Familiar Commands | Owned familiar detail when targeted, otherwise the private roster. All seven M9 commands and paid recall use existing menu/session authority. |

Permissions use the existing action-specific seams and Forge events. No universal claim-mod or third-party crop compatibility is promised. See [compatibility](../COMPATIBILITY.md), [witchcraft](witchcraft.md) and [familiars](familiars.md).

## Showcase and qualification

The witchcraft group exposes `broom_stand`, `wand_bench`, `broom` and `wand` separately. Stations are stocked but unstarted. Adjacent signs label fresh/worn brooms, a practice strip, wand supplies, mature crops/herbs, a dry bed and an unlit candle. Construction grants no witch status, energy or owner assignment.

Headless art includes the four editable, texture-owning Blockbench projects, station states, held transforms and an actual vanilla seated-player geometry preview. [Restart qualification](../../tools/qualification/restart/magic-tools/README.md) uses separate server processes. Automated acceptance evidence lives in [FEATURES](../FEATURES.md). Live camera/animation, GUI scales, shaders, F3+T and two-client latency remain owner checks in the dedicated mod-test instance.
