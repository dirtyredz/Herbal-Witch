# Apothecary commerce (Milestone 11)

Code: `src/main/java/io/github/dirtyredz/herbalwitch/apothecary/shop/`. Design and rationale:
[ARCHITECTURE.md](../ARCHITECTURE.md) ("Commerce"), [DECISIONS.md](../DECISIONS.md). Grimoire: ten
`herbalwitch:commerce/<topic>` guide pages (priority 1300) quoting the live limits below.

## Pieces

| Id | Mount | Shows | Recipe (shapeless, yields one) |
|---|---|---|---|
| `herbalwitch:shop/register` | on an apothecary counter | open/closed sign | 4 dark-oak planks, 2 iron ingots, 1 gold ingot, 1 paper, 1 redstone |
| `herbalwitch:shop/stock_shelf` | sturdy wall behind | three offers | 3 dark-oak slabs, 2 sticks, 1 gold nugget |
| `herbalwitch:shop/stock_jar` | sturdy surface below (a counter top counts) | one offer | 3 glass, 1 dark-oak slab, 1 gold nugget |
| `herbalwitch:shop/order_board` | sturdy wall behind | requests | 3 dark-oak planks, 2 paper, 1 ink sac |
| `herbalwitch:decor/surfaces/apothecary_counter` (existing) | | browse when linked | unchanged decor recipe |

All four are in the Apothecary tab, place through the shared checked placement (aimed face, preview),
drop their plain item (never a binding), refuse pistons and pick-block copies no data. Their block
entities hold an instance UUID, a shop claim and, for displays, a public sample projection; never stock
or proceeds. No currency item, deed, customer entity or profession exists.

## Shops

- An initiated witch uses an unbound register and names the shop (1-32 plain characters, at most 128
  UTF-8 bytes). It starts **closed**. Opening needs the loaded register and at least one enabled,
  stocked offer. Witch status is read once; no energy is spent; later status loss never blocks the owner.
- **Removing the register** (by hand, support loss or explosion) detaches the shop; stock and proceeds
  stay in its record. The owner reconnects it from a new unbound register: a newer binding generation
  is persisted first, and every older or copied register reference stops working.
- **Retire** needs zero stock, zero proceeds, no unresolved transfer and no active request; the id stays
  as a tombstone and is never reused.
- One record per shop: `data/herbalwitch_shop_<uuid>.dat` (schema 1, checksummed, verified writes).

## Management (owner only; operators get no menu bypass)

| Operation | Rule |
|---|---|
| Deposit | a chosen main-inventory/hotbar slot and amount; empty cursor; only tradeable, exact goods |
| Withdraw stock | a chosen stock slot and amount, into the main inventory; never dropped |
| Offer | from a stock slot's exact product: lot 1..min(64, stack size), price 1..64 emeralds, enabled; at most 12 |
| Proceeds | withdraw whole emeralds into the main inventory |
| Settings | open/close, public buying, villager customers, bans (32, by online name, stored by UUID), rename |
| Links | arm Counter/Display/Board/Unlink; the menu closes and the next click selects the fixture (200 ticks) |
| Displays | choose which offers a linked shelf (3) or jar (1) shows |
| Ledger | the latest 128 events, 32 per page; owner only |

Stock is 27 canonical slots; offers name exact keys (item and accepted data), never slot numbers.
Any offer change makes a new revision. Shop grids are read-only projections: no container slot, item
handler, hopper, comparator or familiar can reach them.

## Buying

Anyone not banned may browse an open shop at its register, a linked counter or a linked display
(within 4 blocks, with sight and use permission; adventure mode is fine) and buy **one displayed lot**
per request. The server derives the item, quantity, price and payment; plain untagged emeralds only;
the payment is removed first and the lot must fit the remaining inventory. Refused: stale offer
revision, insufficient payment, full inventory, full proceeds, sold out, the owner, creative,
spectator and fake players. No fees, change, emerald-block conversion or drops.

## Tradeable goods

From the explicit catalogues only: seeds and fresh herbs, dried bundles and powders, every everyday
product, the advanced and familiar preparations, and the protective charms. Never: emeralds, four-dose
mixtures, furniture, stations, brooms, wands, capture charms or filled tokens, anything with nested
inventories or capability data. Accepted item data is kept exactly and shown before buying: Thirst
`Purity` (0-3), the bottling label, a charm's `Used` charge. Any other tag (a custom name included),
more than 4 KiB or depth 8 is refused rather than stripped. See [products](products.md).

## Requests (order board)

A customer picks a tradeable product, quantity 1-64 and the most they will pay per lot, "plain only"
or "any kind". Nothing is paid, reserved or promised. Three active per customer per shop, sixteen per
shop, 200 commerce-clock ticks between a customer's requests, lifetime seven economy windows (config
1-28). The owner declines or associates a compatible offer (same item, accepted data, lot within the
remainder, price within the ceiling); the customer then buys that offer at its current price, and the
purchase advances the request in the same transaction. Changed or removed offers return the request to
open; cancellation and expiry move nothing.

## Villager customers

Off per shop until the owner enables it. A loaded, open shop selling a **one-item lot for one emerald**
of `preparations/drinks/recovery_tea`, `warmth_remedy`, `cooling_remedy` or
`preparations/salves/healing_salve` (no tag, or Thirst pristine purity 3) may attract an adult, awake,
untraded, unhurt, unridden, AI-driven villager within 16 blocks (not panicking, hiding, raiding or
working at its job site). It walks to within two blocks of the register with line of sight, then buys
once. The product is consumed: no villager inventory, trade, experience, reputation, discount or
bottle. Visits cancel on timeout, closed shop, changed offer, unload, lost eligibility or permission
(`PrincipalPermission` with the owner's UUID; unsupported claim mods suspend villager service).

| Setting (`herbalwitch-commerce-server.toml`) | Default | Bounds |
|---|---:|---:|
| `villagerCustomers.enabled` | true | |
| `economyWindow` (running ticks) | 24,000 | 24,000-168,000 |
| `worldSpending` / `ownerSpending` / `shopSpending` per window | 128 / 32 / 16 | 0-1024 / 0-128 / 0-64 |
| `villagerPurchases` per window | 2 | 0-4 |
| `visitInterval` / `visitDuration` | 1,200 / 200 | 200-24,000 / 40-600 |
| `concurrentVisits` (one per shop) | 16 | 1-32 |
| `shops.shopsPerOwner` / `shopsPerWorld` | 4 / 1,024 | 1-16 / 1-4,096 |
| `shops.proceedsCapacity` | 1,000,000 | 64-1,000,000 |
| `shops.orderLifetimeWindows` | 7 | 1-28 |

Fixed: 4 shop evaluations and 1 path request per tick, 16 candidates per evaluation, maps of 1,024
owners/shops and 4,096 villagers (full maps refuse, never evict). The demand record
`data/herbalwitch_npc_demand.dat` holds the commerce clock and per-window spending; a window resets
only after its full running time, so time commands, sleep, restarts and replacement registers grant
nothing. The budget is debited durably **before** the sale commits.

## Recovery

Every deposit, withdrawal, purchase and proceeds withdrawal: prepare (shop checkpoint, outgoing value
reserved) -> apply (inventory and receipt) -> verified player checkpoint -> shop commit. A shop with an
unresolved transfer refuses every economic operation and shows its transaction id; so does a player
whose receipt is pending. At restart, and when a pending player logs in or is verified again, the
transfer finalizes only on a player file that carries its receipt, rolls back only on a player file
that proves the untouched before-state, and otherwise stays quarantined.

```
/herbalwitch shop inspect <shop>
/herbalwitch shop transaction inspect <shop> <transaction>
/herbalwitch shop transaction reconcile <shop> <transaction>   # proven evidence only
/herbalwitch shop transaction discard <shop> <transaction>     # loses the reservation; grants nothing
```

Restart qualification: [tools/qualification/restart/commerce/README.md](../../tools/qualification/restart/commerce/README.md).
