# Decisions - The Herbal Witch

## 2026-10-26 - Optional animated first preparation before release

Deliver one Ponder lesson alongside the complete written GuideME equivalent. Watch is hidden when
Ponder is absent; native G-hover stays available. Native W-hover and Watch share the same inert
storyboard. Lessons 2–11 remain future work, with no placeholder scenes. The owner approved the
lesson in game, including its final 0.85 scene scale; qualification work preserves that appearance.

Pin standalone Ponder 1.0.91 exactly and Flywheel 1.0.5 explicitly, without transitives, shading,
jar-in-jar or a Create dependency. The build declares Create's Maven repository in
`gradle/ponder.gradle`, also applied by hosted CI. Only the opt-in `ponder` runtime profile adds
these libraries; `base` and `all` do not. The protected PonderUI scene-list constructor needs a
small adapter because its public factory selects a component, not an exact lesson. Broaden the
supported Ponder range only after checking that API and qualifying it.

Use named shared captions and deterministic compressed structures from the sole Java datagen
writer. Illustrate the bundled recipe, label the shortened waiting and direct readers to live cards
for current recipes. Keep client station tickers null and all gameplay services out of scenes.
No saved identities, protocol or version changes accompany this addition. This supersedes the
earlier Phase 1 deferral of Ponder below; release authorization remains separate.

## 2026-10-25 - Shared hosted CI and deliberate releases

Adopt the workspace template as byte-identical standalone files. Pushes verify without publishing;
owner-authorized release dispatch after green main CI verifies, builds, drafts, hashes readback
and publishes immutable assets. Gradle resolves the exact release manifest beside the staged jar.

Datagen and Forge GameTests are enabled; art producers/checks keep their existing Gradle wiring.
The shared gameTestWorld(File) stages the canonical datapack for both GameTests and existing
qualification phases; restart/profile qualification stays on ANU. Clean hosted clones resolve GuideME
from the exact CurseForge project/file already approved in COMPATIBILITY.md (1173950/8204776).
An explicit guideMeDir or existing build/guide-inputs jar retains the local flatDir route for offline
builds. This adds no version upgrade, shading or dependency download to the local gate.

## 2026-10-25 - Additive GuideME first-herb lesson

Owner-feedback correction: keep the walkthrough and add one focused page for each of its fourteen
item associations. The 20.1.15 ItemIndex hard-codes a null anchor, so per-item pages are required
for precise hold-G and ingredient routes. Replace native crafting cards with live HerbalCrafting
cards sharing bounded tooltip slots with processing cards: pinned DocumentScreen's leftward
overflow adjustment can produce a negative x. No library fork, mixin or screen subclass is needed.

Require the pack's exact GuideME 20.1.15, with no AE2/Create dependency and no bundled library classes.
This qualifies earlier standalone/no-shared-runtime wording: HerbalWitch remains independent of
workspace libraries and other gameplay mods, but uses an external documentation engine.
Standalone Ponder 1.0.91 (Flywheel 1.0.5) is the approved optional direction for later animation work;
Phase 1 adds no Ponder dependency or integration.

Keep the book, saved IDs, discoveries and research/teaching unchanged. The new public lesson is
always readable and grants no progression. Use native GuideME item associations and rotatable
scenes; do not subclass its internal screens. Actual 1.20.1 API inspection confirmed per-guide
TagCompiler extensions, item_ids frontmatter and GameScene's p: blockstate attributes.
Recipes resolve live through RecipeView, never copied amounts in prose.

For this slice, datagen reads authored guide_sources Markdown and produces complete
native guide pages with validated catalogue associations and scene layouts. Separate roots avoid
two writers for one packaged resource. Broader topic contribution, search-fact and Ponder layers
remain later phases; introducing them now would add unused scaffolding.
The pinned loader expects base-language pages directly under guide/ and localized overrides under
guide/_<locale>/, unlike the plan's guide/en_us/ suggestion. Authored English stays in the source root.

## 2026-10-23 - M12 release qualification: evidence from real worlds and real servers

- **Qualification is not publication.** `1.0.0-rc.1` names a private candidate that passed automated
  qualification; owner acceptance (docs/qualification/owner-acceptance.md), publishing and pack
  integration remain the owner's decisions. Items that only a client or the owner can close are listed in
  docs/qualification/release-readiness.md, not hidden behind the version.
- **Historical saves are the owner's real worlds**, copied read-only from the MultiMC instances
  (0.2.0-dev.9 and 0.6.1), not worlds generated by rebuilding old commits: real played state beats a
  synthetic fixture, and no candidate code can shape them. Foreign pack content is replaced by vanilla
  stand-ins before the candidate opens them, and every substitution is counted in the receipt.
- **One dedicated-JVM phase factory** (`gradle/qualification.gradle`) replaced three copied restart
  scripts; every harness (restart, save upgrade, performance) is a few lines over it, and all stay out of
  `build`/`check`.
- **Production-server checks use the installed Forge server and the reobfuscated jar** (no userdev,
  no test classes), driven through the console; foreign mods' own data-pack errors are recorded, not
  failed, and any exception whose stack names Herbal Witch fails the profile.
- **A write-locked capture ledger keeps its records authoritative for world joins.** Refusing a join
  removes the entity for good, so the old rule (an unwritable ledger refuses every eligible animal)
  emptied every farm loaded after one failed write. Now only an unreadable ledger refuses, and only
  transported incarnations (generation above zero); never-transported animals always join. The residual
  risk, a pre-capture source copy from an unsaved chunk while the ledger is unreadable, is inside the
  already documented "inconsistent backups are outside the guarantee".
- **Fox stealth** (promised by the design, missing until M12) reuses the shared concealment effect as a
  species trait instead of a new power: hostiles cannot pick a fox out from farther than four blocks,
  and a distracting fox gives it up first so distraction still works.
- **Performance gates were frozen before measuring** and the protocol was shortened for the shared ANU
  machine (docs/qualification/performance.md); thresholds are not relaxed after a result.
- **GameTests run in a superflat world** (`gradle/gametest-world.gradle`). Forge's GameTest server is a
  dedicated server, and with no server.properties it generated normal terrain, placing every
  plot in deepslate caves; the random failures M12 chased (falling gravel, cave mobs, terrain in a
  villager's path) came from there. Rejected: hardening each test against terrain (endless, and the
  world would still differ from the performance protocol). A guard GameTest fails the suite on any other
  world type.
- **Design interpretations kept for the owner to confirm** (not approvals): the hearth's "home anchor"
  is its ward-focus role; "optional experimentation" is advisory discovery; modded-mob adapters stay
  optional and absent.

## 2026-10-23 - M11 apothecary commerce: emeralds, per-shop authority, conservative recovery

Commerce lives under `apothecary/shop/` (not a top-level `shop/` as the plan placed it): the Java root
was already at its breadth cap, and commerce is an apothecary trade. Client and datagen homes nest under
the existing `stations/` folders for the same reason. The four menus the plan named are one zero-slot
`ShopMenu` with four client views (browse, manage, orders, create): every view shares the same session,
nonce, sequence and access checks, so separate menu types would duplicate validation. Config is
feature-owned (`ShopServerConfig`), as every earlier feature's is; there is no `platform/config`.

- **Currency** is ordinary untagged emeralds, whole-emerald prices, one lot per request, no change,
  fees or emerald-block conversion. No currency item, deed or external economy adapter.
- **Authority**: one verified shard per shop, owner and register/fixture indexes derived from records.
  Block entities hold only an instance UUID and a claim; a claim binds only when the record's binding
  names the exact dimension, position, instance and generation. Reconnecting persists a newer generation
  first. Unreadable or newer-schema shards stay preserved and unavailable; absence is never a fresh shop.
- **One transfer path** (prepare, apply, verified player checkpoint, commit) for deposits, withdrawals,
  purchases and proceeds. Outgoing value is reserved at prepare and incoming value only becomes
  spendable at commit. While any transfer is unresolved the whole shop refuses economic operations
  (simpler and stricter than per-player locks; at most one transfer per shop exists at a time).
- **Recovery is proof-based**: finalize only on a saved player file carrying the receipt; roll back
  only on a saved file one sequence earlier whose affected slots equal the before-state; otherwise
  quarantine for an operator (reconcile from proof, or discard the reservation as a recorded loss).
  A runtime player-checkpoint failure quarantines immediately; online pending players are re-verified
  every 200 ticks and resolve only after a fresh verified save.
- **Actual villager customers supersede the original "no simulated customers" default.** Real
  adult villagers walk to an open, loaded register through their own brain's walk target (no goal
  replacement: villagers are Brain-driven, so the plan's goal class became a walk-target scheduler),
  buy four default preparations at one item for one emerald, and the product is consumed as demand.
  NPC emeralds are an explicit bounded source (world/owner/shop/villager budgets per running-tick
  window). The budget debit is durable before the sale commits; an interrupted sale may burn budget,
  never refund it or replay it.
- **Requests are nonbinding**: no payment, reservation or delivery. A purchase that names an offered
  request advances it inside the same transfer.
- **Showcase**: an inert villager sample needed the shared entity spawner to accept a PROTECTED harm
  denial for inert samples only (spawning a demonstration is not harm); every other denial still refuses.
- Rejected: per-player shop locks (more state, same safety), a private player-inventory file (a second
  authority), vanilla trade offers for villagers (would grant XP/reputation and resale), automatic
  refunds on uncertainty (can duplicate value).

## 2026-10-23 - M10 wear timing and recipe-owned repair

The owner delegated resolution of the M10 accumulator conflict. Item schema 1 accepts
wearTicks 0..23,999: the longest supported wear interval is 24,000 loaded flying ticks.
The 200-tick default is unchanged. Packing and repair preserve the accumulator and energy
remainder; lowering the live interval accounts for accrued ticks on the next powered tick,
clamping wear at 600. This avoids silently discarding time or rounding configurable intervals.

Broom recipes implement the existing pure ResultDecorator contract. The process job applies
that recipe-owned transformation after its station decorator, both before capacity checks and
completion. Repair matching rejects pristine or unsupported brooms and includes the repair
amount in JSON, recipe sync and fingerprints. A live result note supplies repair semantics to
both recipe viewers and the grimoire. The normal processing transaction retains reservations,
payer validation and the only energy debit. Tool stations reuse AdvancedStationBlock and
StructurePlacementItem; their menus reuse the standard Start/Resume/Cancel panel.

## 2026-10-23 - Home assignment belongs to the bond

Bed, nest and floor perch share one placement/block implementation and one identity-only block entity.
The bond owns the dimension, position and home-instance UUID; a derived index reserves that instance
even while its familiar is unloaded or incapacitated. The block's occupant is a repairable cache.
Breaking a loaded home clears the assignment, while an unloaded or unknown-schema home cannot prove
absence. Replacement blocks receive new identities, and dropped items contain no assignment.

Home selection uses the private roster with a bounded, same-level menu context. Ordinary safe rest
heals one health per 100 loaded ticks, including offline rest, without energy or reserve. Internal rest
clock changes do not invalidate every in-flight owner intent. Section 7 defaults are unchanged.

## 2026-10-22 - Loaded command work and UUID permission checks

Seven stable command names share one exclusive goal, with bounded fair queues for scans and paths.
Mobile reservations live in a derived canonical index, including unloaded bonds; logout, death and
restart clear them to Stay. Lowering the configured cap retains the oldest reservations. Configuration
reload publishes one validated snapshot; lowering record size never overwrites an existing larger
shard, but refuses its mutations until the limit permits them again.

Offline automation uses the shared UUID principal permission boundary, including vanilla spawn
protection and conservative refusal with unsupported claims. Existing M7 hostile targeting keeps its
own policy. Familiar target selection and each attack repeat both checks. Recall is an explicit
loaded-source generation transfer with the existing player receipt and WitchStatusService payment;
absence never permits reconstruction. No section 7 balance defaults changed.

## 2026-10-22 - Per-bond authority and conservative cross-file binding

M9 stores one bounded verified shard per bond, with incremental derived indexes and no roster cap.
Unknown/corrupt shards are preserved and block mutations. Interrupted conversions retain payloads and
quarantine; neither an unloaded creature nor a missing player-side receipt permits replay or refund.
A single monotonic player receipt is persisted through vanilla's single-player save and read back.
That method is protected in 1.20.1, so the feature uses Forge's remapping helper for the pinned SRG
method `PlayerList.m_6765_`, then forces and verifies the saved marker. It does not save every player,
replace player NBT serialization, or introduce a player roster capability. Runtime qualification must
exercise this mapping on the dedicated server.

Altar actions become registered descriptions instead of a closed enum, so companions owns familiar
rituals without reverse imports into witchcraft. Built-in behavior remains in its original handlers;
the same session, cancellation and payment boundaries apply to contributions.

Newest first.

## 2026-10-22 - Shared prerequisites for M9, with M8 file compatibility

Extract verified filesystem operations into platform persistence, preserving the capture envelope
byte for byte and leaving transaction phases, dirty-state handling and quarantine in capture.
Extract live object identities into companions lifecycle: source lookup also checks loaded level UUID
maps, and late removal of an old object cannot evict a replacement. Neither shared layer owns payloads
or decides that an absent entity is dead.

The familiar guide has an explicit ownership handoff beside the application seam. Brewing supplies
only a lazy fallback; the bonding feature registers its source at its own priority. Reject a second
owner rather than overriding entries after the grimoire's first-key-wins assembly. This checkpoint
does not enable familiar gameplay or promote the milestone version.

## 2026-10-22 - M8 care and verified transport candidate

- Keep all section 7 defaults except the already documented goat Brain correction. Damage blocks
  feeding/adoption/capture for 600 loaded ticks; powder uses the explicit 100-tick reapplication gate,
  and healing uses its independent 200-tick target gate. This follows the action-specific care rules.
- Capture uses one versioned checksummed SavedData file and one serialized writer. No secondary
  journal or mutable item payload. Missing adapters, corrupt snapshots and ambiguous insertion fail
  closed. Whole-world external rollback is explicitly outside the guarantee.
- The RELEASED checkpoint retains its payload until verification succeeds; then ordinary save
  compacts it. A restart before compaction conservatively quarantines the retained payload. This can
  require operator recovery after an otherwise completed release, preserving recovery evidence and
  avoiding automatic replay after a failed final verification.
- Exact vanilla adapters preserve full native serialization and refuse unknown capabilities/mod
  data. No speculative modded capture, familiar ownership or claimed compatibility profiles.
- `AnimalEntries` owns priority 1100. Rule reload invalidates content and private cards. Protocol 5
  adds two bounded animal messages; existing M7 effects/production and the shared permission seam remain.
- Nine Blockbench-owned sprite drafts use the cottage palette; sixteen isolated live showcase
  specimens add a bounded entity construction phase. Owner visuals and ANU/restart qualification are pending.
- Filled charms have a separate server-issued item identity, with no recipe or creative-tab entry,
  instead of the early plan's model-property override. Both states are non-stackable; only the ledger
  token authorizes release. This keeps authored item parents and transforms in their Blockbench projects.
- `gradle.properties` remains 0.6.1 until the spec's complete automated acceptance permits 0.7.0.
- New recipe IDs group under `processing/animals/{grinding,charm_work}/` to keep the already crowded
  grinding directory below its entry ceiling. Existing recipe IDs are unchanged. Resource guards
  count directories containing generated files; datagen's empty, untracked remnants are irrelevant.

## 2026-10-22 - M8 foundation: exact species and lazy care state

`companions/` uses the remaining root grouping slot and will also hold M9 familiars. Ordinary
relationships remain in an animal capability; familiar bond authority will stay separate. The
entity-tag provider is extracted to the shared tag writers with M7 memberships unchanged.
Malformed or future care data is preserved and disables mutation. Inspection does not allocate
a relationship, and the eight-player default capacity never evicts an existing record.

Deviation from the older M8 plan: goats also use vanilla Brain behavior (`Goat.getBrain` and
`customServerAiStep` in the mapped 1.20.1 code). Like axolotls, they are excluded from generic
goal-selector attraction/calming; sleep remains the intended settling route for transport.
All other section 7 defaults are retained. Version remains 0.6.1 while M8 acceptance is pending.

## 2026-10-22 - Showcase split into furniture and witchcraft; signs beside their block

- The owner found furniture among the products and abilities, and the signs hiding their blocks. The
  showcase is now two plans (`ShowcaseGroup`: `furniture` = `decor`, `witchcraft` = everything else, the
  default for any new section), each its own plot with its own preflight and bounds; the bare command
  builds both, 8 blocks apart, after checking both. The section registration seam is unchanged.
- Sign rule: one cell west of the block, in the block's front row (never north of it), with an open cell
  between samples; notes annotating no block stand in a front row with nothing behind them; headings
  keep an open row. Rejected: a sign on a post behind the block (the block hides it), a sign raised above
  it (breaks for wall and ceiling pieces), a per-section arrangement (no single rule to test).

## 2026-10-22 - Review round 2: one-way ward package, numeric grimoire priorities

- `WardService` is the top of `magic/ward/service` (server lifecycle, ticks); activation, controls,
  focus and lifecycle depend on `WardIndex.loaded()` and `WardContext` only (guarded in
  `DependencyDirectionTest`). Stone lookup and the charged look live on the stone side
  (`WardStoneBlockEntity.at`, `WardStoneBlock.show`); indexing lives in `WardLifecycle`.
- The feature-naming `GrimoireContent.Order` enum is replaced by a thread-safe `Contributions` holder:
  each feature owns a numeric `PRIORITY` (spaced by 100, same order as before), so M8/M9 add a
  constant instead of editing core. Recipe providers and discovery forms use the same holder.
- Rejected: one central priority table (it would again name every feature in core).

## 2026-10-22 - 0.6.1: stable extension contracts before M8/M9

- Group cultivation, preparations and processing under `apothecary/`; flatten station leaves.
  The parent owns no implementation. Resource IDs, saves, registration order and protocol 4 stay fixed.
- Features own their grimoire sources and register them with explicit reading order. Core consumers
  use neutral summaries, discovery forms and live recipe views. This supersedes the M5 and M7
  decisions below that forbade features from importing grimoire contracts. The fixed herb wire DTO
  stays explicit; an extensible transport is deferred.
- Shared values live in `platform/{recipe,inventory,time}`; online lookup/enumeration lives in
  `platform/player/OnlinePlayers`. Policies stay in features. Typed recipe subclasses remain small
  adapters for registered types and serializer bounds.
- Durable witch mutation stays in `magic/witch/status`; paid operations use `WitchStatusService.spend`.
  Participant preparation precedes debit. False results and runtime exceptions restore durable state
  and invoke participant rollback; transient disturbance remains. Founding/designation keep their
  no-energy conversion path. This does not provide crash atomicity or arbitrary world rollback.
- Ward activation, controls, focus, index/ticks and reconciliation have separate owners. Preparations
  share one drink/oil adapter and lazy timed-effect facts, retaining explicit special cases and labels.
- Four screens share panel routing; recipe text and recipe-body rendering have dedicated homes.
  Escape reaching the screen and failed-spend rollback on exception are the only intended corrections.
- Art primitives are neutral modules; feature motifs/layouts keep feature homes. Distinct outline
  algorithms and the reader atlas must retain exact PNG bytes, not merely similar appearance.
- Singleton feature `grimoire/`, targeting and integration packages express ownership and remain.
  Catalogue dispatch, TeachingNotifier/client-cache splits, nullable policy/slot layout and broader
  furniture placement/storage predicates and migration scripts remain P2 follow-ups; this refactor adds no M8-M12 gameplay.

## 2026-10-22 - 0.6.0: advanced brewing, charms, ritual circle and ward stones (Milestone 7)

Grouped because the choices lean on one another; each is short. These are the points where the
implementation departs from the M7 plan, or where the plan was silent. Numbers: docs/catalogue/witchcraft.md,
docs/catalogue/stations.md, docs/catalogue/products.md.

- **Menu-button clicks, not new packets.** Station Start/Resume/Cancel and the ward settings (deactivate,
  release, radius, profile) use vanilla `clickMenuButton`; the plan's `StationPackets` and `WardPackets`
  were not written. One validated server path (menu id, access range, rate limit of 5 per 20 ticks) and
  no wire change for them. Rejected: new messages, which would need their own bounds and checks.
- **One config: `WorkingRules` inside `WitchRules`.** The M7 numbers (processing percent, garden and
  home costs and ticks, ward numbers, charm charge, preparation PvP) sit in `magic/witch/config/WorkingRules`,
  so one config section group, one sync and one fingerprint cover them. `WitchRulesUpdate` grew, so
  `HerbalWitchNetwork.PROTOCOL` is "4". Rejected: a second rules record and sync.
- **Authorization under `apothecary/stations/process/authorization/`; shared `ProcessStationEntity`.** `apothecary/stations/`
  was at its breadth cap, so the operator, the magical policy and the operations live inside
  `apothecary/stations/process/`. The block-entity base `apothecary/stations/block/ProcessStationEntity` is shared and the four
  everyday process stations moved onto it. A single `AdvancedStationBlock` and `AdvancedStationEntity`
  serve all four advanced stations (a station is a descriptor and a menu, not a block class).
- **Inputs are reserved only after Start.** A witch recipe reserves and runs nothing until a witch presses
  Start; one batch per Start and the starter is the payer. Others may cancel only a batch whose lease
  ended (the payer offline, dead, away or in another dimension). Cancel consumes nothing; energy is debited
  once, at completion. Rejected: reserving on insert, which would let a non-witch lock a station.
- **Timed effect state uses `MobEffects`.** Binding, sleep, steadfast, concealment, dazzled, thorns and the
  repellents are vanilla effects, so persistence, expiry and milk-clearing are vanilla's. Rejected: a custom
  codec and capability for effect timers.
- **The circle item is a flat sprite; the compiler gained `multiblock.assembly: false`.** The 5x5 ring is
  placed whole through the item (with preview), but the item model is a sprite, so no 5x5 item assembly is
  compiled. The 32 compiled cell models are a documented exception to the 12-file directory cap.
- **An unknown job schema is preserved byte for byte.** `ProcessJob` is SCHEMA 2 (schema 1 loads). A newer
  schema is never rewritten: the station idles until its inventory changes. Rejected: dropping the job.
- **Historical, superseded by 0.6.1: preparation pages lived in `grimoire/content/reference/`.** `AdvancedPreparationEntries` is
  grimoire code reading the brewing catalogues, because brewing must not import the grimoire.
- **The witch picks the ritual target by looking at it, then opens the altar.** Garden Blessing targets a
  herb bed and Home Protection a ward stone; `client/magic/ritual/RitualTargets` reads the aimed block before the
  altar opens, so there is no target-picking screen. Rejected: a selection mode inside the altar menu.
- **Wards.** Release unbinds the stone (it can be bound again). The area is a box (radius by height 4), not a
  sphere. The focus is any functional hearth or stove within 4 blocks, rechecked every sweep (ownership of
  the hearth is not modelled). Recognised claim mods loaded means entity actions are restricted, because
  the mod cannot ask them; `protected_entities` defaults to villager and wandering trader; fake players are
  treated as players. A copied record id is inert, and breaking a stone retires its record.
- **Smaller calls.** The charm wear counter is per player (a per-player tick counter, charge as seconds of
  wearing); showcase sections pack into rows no wider than 126 (`ShowcasePlan.ROW_WIDTH`) and the GameTest
  template is 128 x 10 x 64; the cauldron and still need a heat source below; labelled vials are an NBT
  flag on recipe variants (gunpowder and paper variants), not separate items.

## 2026-10-22 - 0.5.0: initiation and energy (Milestone 6)

Grouped because the choices lean on one another; each is short. Procedures and numbers:
docs/catalogue/witchcraft.md.

- **Rules are synced to clients for page numbers.** The grimoire's Witchcraft pages quote the live server
  rules (`WitchRulesUpdate`); prose never hard-codes a number and a test enforces it. Rejected: fixed
  numbers in prose, which a config edit would make false.
- **The tea cooldown is saved for every player.** Energy tea is food for everyone; the 1,200-tick
  cooldown is an authoritative saved value for every player and counts online ticks, so a non-witch
  cannot be used to skip it and a witch cannot relog around it. The vanilla item cooldown only mirrors
  it. A cup started or finished during it is refused and nothing is consumed.
- **No InitiationRequest or InitiationUpdate messages.** Consent requests go through the menu-bound
  `AltarRequest` and invitation state rides `AltarUpdate`; notifications are chat lines. One request
  path means one set of checks (menu id, session nonce, rules fingerprint, rate limit).
- **Shared `StructurePlacementItem` and one preview.** The altar needed exactly furniture's checked,
  previewed placement, so the body of `FurniturePlacementItem` moved to
  `platform/placement/StructurePlacementItem` and the preview to `client/placement/` (any structure item
  is previewed). `platform/geometry/CellShapes` and `platform/network/Messages` and `RateLimit` were
  extracted the same way. Rejected: a second placement item and preview for the altar.
- **`magic/` grouping parent.** `witch` and `ritual` live under `magic/` (`magic/witch`, `magic/ritual`)
  to keep the Java root at a readable width; the plan named a top-level `witch/`. A deliberate
  deviation, and it matches the module map's existing `magic/` home for rituals.
- **Founder exclusivity over crash ambiguity.** The first conversion by any route closes founding for
  good. A durable COMMITTING claim is written right before the irreversible completion; a world that
  loads in COMMITTING, or an unreadable record, is NEEDS_REVIEW and stays closed until an operator
  resolves it, with nothing replayed. Minecraft saves are not transactional across player and world
  files, so a crash between the claim and the player save cannot be made certain. Rejected: automatic
  reopening, which could found two lineages.
- **Attunement is solo practice.** One fresh sage and 10 energy, 100 ticks, awards practice, no partner
  (still available at full practice, labelled "no further capacity"). Practice comes from successfully
  spent energy; a witch grows by doing, not by waiting.
- **The restful infusion prepares sleep.** It resets TIME_SINCE_REST and forces no sleep, as before; for
  witches it prepares the next rewarded sleep (+30, non-stacking, re-drinking refreshes the 24,000-tick
  duration). A non-rewarded sleep does not consume it. Rejected: instant energy, which duplicates energy
  tea and rewards drinking over resting.
- **Offerings come from the payer's own inventory (zero-slot menu).** The altar menu has no slots; the
  exact stacks are captured at the start and consumed at completion from the main inventory and
  offhand. Nothing can be stored in the altar, so nothing is lost or dropped when it breaks. Tradeoff:
  moving a captured offering stack cancels the ritual.
- **A distinct functional altar beside the decorative altar table.** The M5 `decor/fixtures/altar_table`
  stays decoration; its page now links the functional altar. Rejected: converting the table, which would
  change ids worlds already use.
- **Explicit consent, designation included.** Initiation needs an invitation the recipient accepts; the
  operator's `designate` is also an invitation (no initiator, offering or energy), accepted at any valid
  altar. Nobody becomes a witch by proximity, command or accident.
- **A survival founder and no NPC.** The first witch is a player who gathers the offering and performs
  the founding, in any time, moon or dimension, once per world. Rejected: a witch NPC or quest giver that
  would own the lineage and make a server depend on one entity.

## 2026-10-22 - 0.4.0: the full furniture catalogue (Milestone 5, waves A, B, C)

Grouped because the choices lean on one another; each is short. Implemented overnight by agents from the
M5 plan's defaults; the owner's visual review of the 89 agent-drafted pieces is still pending.

- **94 canonical pieces, permanent ids.** The five 0.1.0 pieces plus 89 additions (A 22, B 30, C 37) in seven
  families, ids `decor/<family>/<piece>`. Repeated labels (benches, mirrors, consoles, shelves, rugs, small
  tables, archways) stay aliases; no finish variants, appearance NBT or extra door/arch finishes. Waves are
  catalogue metadata (`FurnitureCatalog.DELIVERED`), so each checkpoint registered only completed waves.
- **Composition, not a furniture machine.** A piece is a behaviour (static, seating, storage, display, light,
  bed, door, rug) composed with a mount (support and cells). One block class per behaviour; the shared
  structure lifecycle lives in `DecorAssemblyBlock`, the placement in `platform/placement`. Beds and the door
  extend vanilla's `BedBlock` and `DoorBlock` so sleeping, spawn and door rules stay vanilla's.
- **Placement is checked and transactional.** One pure resolver serves the client preview and the server; the
  whole piece is placed or nothing, never somewhere else; Forge's snapshot capture defers neighbour updates,
  fires one multi-place event and restores everything (the item included) on cancellation. No placement packet.
  Rejected: sneak-click modes and automatic relocation (owner rule: Hammer-style targeting, preview = result).
- **Fixed-id resource exception.** Blockstates, item models and loot of the seating, storage, surfaces and
  fixtures families exceed the twelve-entry rule because their file names are block ids; only those twelve
  directories are exempt, each checked against the exact manifest (`DecorManifest.REGISTRY_DIRECTORIES`).
  Recipes, advancements, models and Blockbench projects are grouped (`<family>/<group>/`) instead. Rejected:
  renaming ids or raising the cap.
- **Capacity defaults.** Cabinets 27 slots; sideboard, china cabinet, dresser, wardrobe, trunk, rolltop desk
  and storage cabinet 54. Displays one item per slot: small wall shelf 3; medium wall shelf, short standing
  shelf and corner shelf 6; the rest 9. Cabinets take hoppers through any cell; displays stay manual. Drawers
  modelled on decorative pieces (desks, vanity, curio cabinet) hold nothing.
- **One sleeper per bed, the canopy included.** Its frame cells route to the sleeping pair; paired sleepers
  would need their own occupancy design. Tradeoff: a two-wide canopy sleeps its owner off-centre.
- **Stationary seats.** The swing and desk chair do not move or turn; seats are invisible, never-saved
  entities, so no world can keep an orphan seat.
- **Lookalikes stay decorative.** The work table, altar table, decorative drying rack, kitchen range,
  fireplace and firepit are furniture; their grimoire pages link the working station. Lights need no fuel and
  heat nothing; plumbing, the planter and the well simulate nothing.
- **The bookshelf stays two pieces.** `decor/storage/bookshelf` stores and shows items; the grimoire's
  `grimoire/bookshelf` remains a storage-free research block.
- **The small shelf lost its bottles.** As a display, modelled bottles would pass for stored items; old shelves
  upgrade to an empty inventory (never free bottles).
- **Agent-drafted art.** Every new piece is a Blockbench project drafted by agents with a shared kit (owned
  copies of the armchair's materials, the dark wood palette); collision boxes were derived once from the
  drafts and are now hand-edited Java. Owner refinement is a later art activity.
- **Structure.** `decor/` groups into `structure/` (block, layout, support, access, placement item, shapes)
  and `behavior/` (seating, storage, sleeping, openings, lighting) to keep every level under ten entries;
  the plan's flatter `decor/{block,layout,support,...}` siblings were a deliberate deviation. The grimoire
  source was `grimoire/content/reference/DecorEntries` (grimoire -> decor), not `decor/grimoire/`.
  That historical dependency rule is superseded by the 0.6.1 feature-owned contracts above.

## 2026-10-22 - 0.3.0: the grimoire is advisory, shallow and recipe-backed (Milestone 4)

Grouped because the choices lean on one another; each is short.

- **Discovery is advisory.** Discovered or not, nothing is gated: crafting, processing, automation and the
  EMI/JEI viewers behave exactly as before, and later magic must not read grimoire knowledge as permission.
  The grimoire is a guide that remembers what the player has met, not a progression lock.
- **Research is shallow.** At a stand or bookshelf, one familiar primary ingredient is enough to research a
  recipe; it is instant, free and consumes nothing (recipes with no primary ingredient use base or package).
- **Observation policy.** Any herb form carried discovers the herb; a carried result discovers the recipe; all
  primary ingredients familiar discovers the recipe. Only the main inventory and offhand count, the same scope
  the missing counts use.
- **Teaching asks for consent.** One recipe per invitation, from a research block reader only, to a player
  within 8 blocks in the same dimension. The recipient gets a passive chat notice and accepts or declines from
  a book or a research reader; invitations expire after 600 ticks, one outgoing per teacher and one incoming
  per recipient, 100-tick cooldown, duplicates never refresh. Acceptance revalidates reader, range, teacher
  session and knowledge and the recipe fingerprint. Nothing is taught by proximity, and a stale invitation
  cannot teach a changed recipe.
- **Pages are recipe-backed.** Recipe pages come from the registered recipes; editorial data
  (`grimoire_entries`) supplies prose, grouping, links and hints only, mechanics fields are rejected, and a
  recipe page never depends on editorial data, so a pack that removes a recipe removes its page and cannot
  invent one.
- **Discovery ownership is independent.** The grimoire capability (`herbalwitch:grimoire/discoveries`) stays
  the authority over witch status (Milestone 6); founding and initiation contribute through
  `DiscoveryService` and `GrimoireContentSource`, not by moving the data. Unknown kinds and unreadable data are
  preserved rather than dropped.
- **The research blocks hold nothing.** The stand and the Witch's Bookshelf are single cells with no block
  entity and no storage. The Milestone 5 storage/display
  bookshelf is a separate piece with its own id (docs/catalogue/decor.md); the two never merge.
- **The station panel shows no counts.** The "Grimoire" button on station and hearth screens opens a one-column
  panel beside the station and leaves the station menu untouched; the panel tells the player to open the
  grimoire for missing counts.
- **Package grouping deviates from the plan.** The plan listed `grimoire/{block,item,menu,discovery,research,
  teaching,ingredients}`; with the content, network, registry and showcase packages that is more than the
  10-entry level cap, so `access/{item,block,menu}`, `knowledge/{discovery,research,teaching}` and
  `reading/ingredients` group them. Ids are unaffected. Extra files beyond the plan: `ChunkAssembly`,
  `GrimoireCodec`, `ContentSync`, `ServerGrimoireContent`, `reference/{GuideEntries,HerbForms}`, `Familiarity`,
  `TeachingWorld` and `widgets/TextButton`.
- **Viewers unchanged.** EMI and JEI still show every recipe regardless of discovery, with no transfer handlers
  and no hiding; they share `ProcessingCategories` with the grimoire so the station mapping exists once.

## 2026-10-21 - 0.2.0-dev.9: the whole set paints at 32 x 32 (two texels per model unit)

Owner ruling: every Herbal Witch texture is 32 x 32, twice vanilla's pixel density, so finer detail can
be painted in Blockbench. This supersedes the mixed rule in docs/ART.md ("32 x 32 for materials, 16 x 16
for young herb, seed, fresh-herb and product sprites"). The rule is "two texels per model unit": model
geometry and UVs stay in Minecraft's 16-unit space; block and item textures are 32 x 32 and the GUI
atlases 512 x 512 (vanilla's 256-unit sheet; the screens' `blit` calls already measure in that logical
256, so no code changed). It lives once in `tools/art/density.mjs` (mirrored by the Java
`TextureDensity`) and is enforced by the texture producer (every manifest entry), the Blockbench
compiler (every embedded texture, owned or shared preview), `TemplateChecks` (every texture a model
uses) and `TextureDensityTest` plus `tools/art/test/density.test.mjs` (every PNG shipped), so a 16 x 16
texture cannot return beside 32 x 32 ones.

The Blockbench pieces' textures, the house wood and gold, the soil and the herb atlas were already
32 x 32 (since 0.2.0-dev.1) and did not change; the Blockbench projects keep their 16 x 16 project
resolution with 32 x 32 textures, which is what makes the editor paint at two texels per pixel. What
moved: the 74 item sprites (seeds, fresh herbs, bundles, powders, products) and the five GUI atlases,
plus the five embedded bundle previews in the drying rack, stove and apothecary chest. They are exact
2x nearest-neighbour enlargements: the generators still draw on the old grid and `catalogue.mjs`
enlarges (`drawnAt1x`), so the generators remain the source and the game shows the same pixels.
Evidence: `compile-blockbench.mjs --compare` against dev.8 (textures compared at the game's sampling,
which now accepts an exact integer enlargement and nothing else) reports appearance identical, and the
preview sheets render byte-identical before and after. Rejected: doubling everything again to 64 (the
block textures were already at the target density; 64 would be four times vanilla's); per-texture
densities (a sprite at 16 beside a 32 block reads as a mismatched, coarser piece, and the checks could
not tell a slip from intent); redrawing sprites natively at 32 now (that is art, left to the owner or a
later art pass).

## 2026-10-21 - 0.2.0-dev.8: every texture of a Blockbench piece belongs to that piece

In dev.6 only the rug and lamp shade were piece-owned; wood, gold, iron, glaze and the rest were shared
house materials whose embedded copy was a preview and whose painting was refused. The owner ruled that
shared textures "are not working across all": a piece must be hand-paintable whole. Every texture a
Blockbench piece binds is now its own PNG at `textures/block/pieces/<group>/<piece>/<variable>.png`,
starting as a pixel-identical copy of the material it replaces (`tools/blockbench/migration/own-textures.mjs`
did the move; appearance parity against the dev.7 output is `node tools/compile-blockbench.mjs --compare
<baseline> --texture-map <alias.json>`). One PNG per texture variable, as in the editor: a piece with
`wood` and `wood_v` has two files, and painting one leaves the other. `catalog.json` declares
`owned_texture_dir`; with it, `piece.json` validation requires every binding to be owned at exactly that
path, so a shared material cannot creep back. The particle texture now names one of the piece's own
textures (the rug's was the shared wood).

Stays shared: the herb atlas `block/botany/parts` and the `item/preparations/herbs/**/bundle` sprites,
because the dried bundles on the rack, stove and apothecary chest are generated attachments from the
procedural herbs (one bundle source); their embedded copies remain previews checked pixel for pixel. Also
shared: `material/wood`, `wood_post`, `wood_end` and `gold`, which the procedural herb bed and hung
bundles bind. Every other house material and all station materials lost their last user, so their
producers (and the motif and pattern code only they used) were removed; the dev.7 palette now lives in
the pieces' own PNGs, and `--refresh-previews` concerns only the two shared kinds. Cost: ~71 PNGs instead
of ~35, and a repaint of a material across pieces is now a per-piece job.

Also fixed: Blockbench 5.2.2 saves display rotation angles wrapped to -180..180 (225 becomes -135), so
an unchanged save compiled to a different (equivalent) model; the compiler now normalises display
rotations to 0..360, and the genuine kettle save is a committed fixture.

## 2026-10-21 - 0.2.0-dev.6: one-off pieces become Blockbench projects (hybrid pipeline)

The owner makes subtle hand adjustments to the kettle, pot, furniture and the other one-offs, and
generator code buried their shape. The twelve one-off pieces (six stations, five decor pieces, the oil
lantern) are now Blockbench projects under `src/art/blockbench/`: a `.bbmodel` holds geometry, explicit
UVs, display transforms and embedded textures; a sidecar `piece.json` holds the Minecraft side (output
paths, texture bindings, owned textures, bounds, glow and exclusion groups, multiblock cells and overlays,
attachments). `tools/compile-blockbench.mjs` compiles them deterministically into the same resource
paths as before, and Blockbench is never run by the build. Herbs, herb beds, hung bundles and the shared
materials (wood, metals, glaze, fabrics, glass, ivy, botany atlas) stay procedural; the projects embed
copies of materials only as editor previews, checked pixel-for-pixel against the canonical PNG. Only
the rug and lamp-shade textures are painted in Blockbench. Each producer writes only its declared paths
(`tools/art/output.mjs`).

The compiler is a narrow parser of Blockbench's internal format, pinned to desktop Blockbench 5.2.2,
project format 5.0, "Java Block/Item" 1.9 - 1.21.5, per-face UVs and embedded textures. It rejects
meshes, animations, group rotations, tint, light_emission, illegal rotations and unknown versions; the
cost is format maintenance, since Blockbench documents that the format can change. Dried bundles stay
generated from `tools/models/preparations/bundles.mjs`: in the project they are "attachment" groups
whose pivot is the anchor, and compilation regenerates the bundle there (one bundle source, at the cost
that Blockbench shows a preview). Multiblocks are authored whole and split by the compiler into cells and
overlays. Item display transforms moved from Java into each project's Display settings and are compiled
into the item's parent model, so a display edit needs no datagen. A dev resource pack
(`--dev-pack ... --watch`) shows edits in game after F3+T. The migration was appearance-preserving
(270 models, 461 block states and 50 textures identical to dev.5); no gameplay changed. Rejected:
manual Java JSON export from Blockbench (two editable truths, loses forge_data and render_type,
multiblock cells hand-split); running Blockbench headless in the build (an Electron editor, not a CLI,
fragile in CI); a custom Blockbench plugin now (deferred until live attachment previews or editing Forge
properties in Blockbench is needed); one project per generated state (states and cells are mechanical
derivations of one design); migrating herbs, beds and bundles now (a later change can feed their base
looks from Blockbench).

## 2026-10-21 - Herb bed inner corners: four inner-corner flags (supersedes "no diagonal state")

Owner screenshot: where joined beds form an L, U or ring, the inside corner showed a notch above the
soil, because the corner part (both sides joined) only had wooden sides up to the soil and the two
neighbours' rails stopped short of each other. A multipart state cannot tell an interior corner from
an inside corner without the diagonal, so the bed gains four booleans (`inner_north_east`,
`inner_east_south`, `inner_south_west`, `inner_west_north`): set when both sides of the corner are
joined and no bed stands diagonally. They are computed with the sides (`GardenBedConnections`:
`refreshCorners`; in `updateShape` for side changes) and refreshed on the four diagonal beds when a
bed is placed or removed (`refreshDiagonals`, no neighbour update reaches a diagonal). The flag adds
the `wood/inner` part (the two neighbours' rails carried round the corner, same profile) and swaps the
wood-sided soil corner for `soil/corner_inner`. `tools/preview/test/bed-corners.test.mjs` floods a
slice of every arrangement (line, square, L, U, T, plus, ring, staircase) at five heights and fails
when the outside reaches the planter. Old saves lack the flag and look as before until their random
tick repairs them. Rejected: a taller corner box with no state (shows as a pillar inside a 2x2).

## 2026-10-21 - 0.2.0-dev.5: dried bundles back to 2D sprites, mortar at kettle size

Owner feedback on dev.4: the first, flatter bundle look was better than the cube bundles; the mortar
is too big beside the kettle. Bundles are the herb's own item sprite on zero-thickness cutout planes
(crossed for free-hanging ones), a small 3D tie, and a peg or hook; planes carry only their two broad
faces and stay 0.75 px inside every cell boundary, the fix for dev.1's bleed onto the host block.
The mortar is drawn at full size and scaled by 0.6 (about 9 px across, like the kettle), its hit
box shrunk to match. Supersedes the 3D bundle builder below (dev.2/dev.3). Rejected: keeping cubes
(the owner's call), 1 px thick sprite boxes (their top and bottom faces smeared onto neighbours).

## 2026-10-21 - 0.2.0-dev.3: slim stemmed bundles, rack legs meeting at an apex

Owner feedback on dev.2: the cube bundles read as stacked blobs and lost their stems, and the rack's
legs crossed under oversized caps. Bundles are now gathered stems, a twine tie, then thin kinked
stems with a few sparse leaf and floret cubes (about 20 elements, 4 px wide). Rack legs keep the
22.5-degree rotation rule, so they meet where their centre lines cross (17.5 px up) and a short
post carries on to a small cap; rejected: a shallower lean (not an allowed angle) and moving the
feet inward (cell layout). The leg texture was already the dark wood.

## 2026-10-21 - 0.2.0-dev.2: 3D herbs at every age, one dark wood, a z-fighting guard

From the owner's dev.1 feedback (docs/ART.md "Standing rules"). Young herb ages are derived from
each mature model (scaled toward the root, thinned, flowers absent, then buds, then partly open)
rather than authored three more times per herb, so a mature redesign carries through; the stage
sprites and the planted-cross template are gone. The armchair's dark wood replaced the warm wood as
`material/wood` (the texture name kept, `dark_wood` removed), so every wooden piece changed at
once. Z-fighting is guarded by a geometric sweep in the previewer (node tests and preview runs)
over every model and multipart combination, not by convention; organic models resolve their own
coincidences automatically, built pieces by hand. Dried bundles are one 3D builder shared by the
hung bundle block, rack, stove and counter (the four station bundle sprites are gone). Rejected:
authored young models per herb (42 more shapes to keep in step); a per-element size jitter to
dodge the check (hides the fault instead of removing it); keeping a separate warm wood.

## 2026-10-21 - Art pass 0.2.0: an independent house style for stations (supersedes "fixed station appearances")

Stations no longer borrow the furniture's fabric colours (the pink and green ceramic stripes): they
are warm wood, charcoal iron, stone and plum glaze with gold hardware and moon/star/flower motifs,
matching the owner's reference pieces (docs/ART.md). Furniture keeps the single COTTAGE design,
restyled in the same hardware language. Rejected: retinting the old stations.

## 2026-10-21 - Cuboid models are generated from code (tools/generate-models.mjs)

Station, furniture, herb and herb-bed models went from about 20 hand-placed elements to 40-80 with
rounded octagonal layers, rotations and multiblock cells. They are now written by a deterministic
Node generator: one place per piece, position UVs that keep textures continuous across elements
and cells, decal UVs for stretched sprites, and automatic cell splitting. The JSON stays checked
in under `src/main/resources` (like the generated textures) and passes the same template tests.
Rejected: hand-editing 80-element JSON; Java datagen for geometry (verbose, and models would split
between two pipelines). The lantern templates stay hand-authored (bundles are generated since dev.2).

## 2026-10-21 - Headless model previews gate art changes

`tools/preview-models.mjs` renders every state, item and review scene (CPU rasterizer, directional
shading, no client) and compares owner references side by side; it fails on missing textures or
unrendered models. Art changes are reviewed on its sheets before a jar goes to the trial instance.
It does not model ambient occlusion, lighting, shaders or animation; the owner still checks those.

## 2026-10-21 - Multiblock rack, hearth and witch's stove; one origin cell

The owner asked for a bigger rack and gave 2-wide references for the rack, hearth and a cooking
stove. The rack and hearth are 2 x 2, the stove 2 x 1 (`platform/multiblock`). Only cell 0 holds
the block entity, ticks and drops the item; any cell opens it; breaking any cell removes all (one
item, contents once, nothing in creative); lit/active is copied to every cell so overlays and heat
work everywhere. Block entities, menus and recipes are unchanged; the stove reuses the hearth's
block entity type, menu and screen (same fuel rules). A vessel heats on the hearth's chimney top;
the stove's burners are the intended cooking surface. Tradeoff: a dev.5 world's single-block rack or
hearth stays a lone origin cell until replaced. Rejected: a deeper single-block rack (cannot read
as bigger in one block), a 1-block hearth with a decorative surround (no room for the chimney
breast).

## 2026-10-21 - Rack bundles show occupancy, not the herb

Four fixed bundles (green, lavender, white, yellow) hang while the rack holds anything. Showing the
actual herb would need a block entity renderer or a state per herb. Rejected for this pass.

## 2026-10-21 - Joined herb beds: four side properties, multipart, no diagonal state

Beds join where the same bed stands beside them at the same height; corners follow from the two
sides that meet. Parts are authored for one side and corner and turned by the blockstate. Beds
connect on placement (including a plain setBlock), on neighbour changes, and old beds are repaired
by their random tick. (The inner-corner tradeoff here was reversed by the 2026-10-21 inner corner
flags above.) No chunk-load migration.

## 2026-10-21 - Herbs rooted 2 px into the ground; mature herbs are 3D

(Superseded 2026-10-21 by 3D young ages, see above.) Young ages use a planted cross whose roots are two pixels below the block, matching the bed's soil
surface (14) and sitting one pixel into farmland and two into full ground; the plant needs no
state for what it stands on. Mature herbs are cube models over a swatch atlas (the owner's
reference), so their stage sprites were removed. Rejected: per-support plant states; a taller bed
soil (the frame would hide less of the plant).

## 2026-10-21 - Showcase command: plan, preflight, build; refuses occupied plots

`/herbalwitch showcase` needs op level 2, builds a pure bounded plan, runs a preflight, and only then
writes. It clears the box above the floor quietly and has no undo, so it refuses a plot holding block
entities (a built showcase) rather than guess. Rejected: auto-offset to a free plot, undo snapshots.

## 2026-10-21 - Apothecary creative tab is owned by stations

Stations depend on preparations, never the reverse, so the tab that lists both lives in `apothecary/stations/`.

## 2026-10-21 - Thirst by reflection, Farmer's Delight by tags, EMI/JEI compile-only

The Thirst adapter looks up two named members (its event class and `addDrink`) with no compile
dependency; Thirst 1.4.0's `addDrink` is a no-op, so drinks register through its value event.
Farmer's Delight needs tags only. EMI and JEI APIs come from maven as compile-only and their plugin
classes load only with the mod. No foreign classes ship in the jar.

## 2026-10-21 - Energy tea and restful infusion: witch-energy benefits staged to M6

Both ship now as ordinary items (food value, cooldown, rest-statistic reset). The +25 and +30 energy
effects wait for the energy system in Milestone 6. (Delivered in 0.5.0; the restful infusion prepares
the next rewarded sleep rather than granting energy at once, see the 0.5.0 entry.)

## 2026-10-21 - Fixed station appearances; a finite oil lantern consumes lantern oil

Each station has one authored look (superseded for appearance by the 0.2.0 house style above). The oil lantern (one dose,
6,000 loaded ticks) gives lantern oil a consumer.

## 2026-10-21 - Stations grouped under `apothecary/stations/everyday/`

The directory breadth cap would be exceeded by five stations plus shared code. Magical stations get
their own group later.

## 2026-10-21 - Composition over a machine superclass; small shared bases

`ProcessStation`, `ProcessJob` and `ProcessInventory` are composed by each station. Only block and
menu boilerplate is shared as `StationBlock`/`StationMenu`. Rejected: a universal machine superclass.

## 2026-10-21 - In-slot reservations, exact matching, explicit returns, no catch-up, recipe change cancels

Ingredients stay in their slots and are reserved there; every filled input slot must be used by the
recipe; returned containers come only from the recipe's `returns`; unloaded stations do not
progress; a changed or removed recipe cancels the job and resets progress while items stay.

## 2026-10-21 - Herbs survive without light

Worldgen has no light data when it places patches, so herbs check soil only. Growth still needs light.

## 2026-10-21 - Accessible starter seeds and grass foraging

Wheat seeds plus a companion plant craft the four starter seeds, and breaking grass can drop them, so
any world can start without a lucky find.

## 2026-10-21 - Grouped permanent herb ids; habitat preferences in data

Herb ids are `botany/<group>/<herb>/...` with five permanent groups, which keeps every generated
directory under the cap. Liked conditions and discovery chances are data, retunable without renames.

## 2026-10-21 - One cottage design replaces 18 finishes

The owner rejected every colour option (2026-10-21: "All of the colour options are terrible."). The mod
now has one dark-wood design with deep green and hot pink accents; `FinishCatalog` keeps a single
entry so designs can return. Rejected: keeping the finishes, tint layers.

## 2026-10-20 - Lit lamp models live beside the palette directories

Superseded 2026-10-21 by One cottage design replaces 18 finishes (no palette directories remain).

`decor/lighting/table_lamp/lit/<palette>/<finish>` rather than `.../<palette>/lit/<finish>`: the
painted directory already holds 11 finish models, and a `lit/` entry would make 12. Rejected:
`<finish>_lit` siblings (doubles the leaf directory).

## 2026-10-20 - Coordinated tints over per-finish textures

Superseded 2026-10-21 by One cottage design replaces 18 finishes (decor is untinted).

Frame and fabric textures are neutral greys tinted per finish (tint index 0 frame, 1 fabric);
brass, glass and tinctures are untinted. Motifs therefore read tone-on-tone. Nine textures serve 90
blocks and upholstery can later become independently selectable without new art. Rejected: 18
pre-coloured fabric textures (multi-colour florals; ties fabric to frame forever). Revisit if the
owner wants multi-colour motifs (docs/ART.md).

## 2026-10-20 - Stonecutter restyling, crafting only the dark oak base

Superseded 2026-10-21 by One cottage design replaces 18 finishes (nothing left to restyle).

Five ordinary recipes make the dark oak base; the stonecutter converts any finish of a piece into any
other finish of the same piece, 1:1, free. Each restyle recipe lists the other 17 finishes, so no
recipe maps an item to itself and none changes the piece. Rejected: 18 stain items, fictional wood
species (walnut, rosewood and pale oak are appearances, not trees), per-finish crafting (90 recipes
of noise). An intentional initial simplification.

## 2026-10-20 - Ids are `decor/<family>/<piece>/<palette>/<finish>`

Superseded 2026-10-21 by One cottage design replaces 18 finishes (ids are now `decor/<family>/<piece>`).

Registry ids carry slashes so every generated directory (blockstates, models, loot, recipes,
advancements) stays bounded below 12 entries as the catalogue grows to hundreds of pieces. Room
categories are catalogue metadata, not id segments. Ids are permanent once released.

## 2026-10-20 - One registered block per piece and finish, baked models

Plain blocks and items give stacks, drops, recipes, pick-block and viewer indexing for free, at the
cost of many small registry and model entries. Rejected: appearance NBT or a block entity with a
custom model loader. Block entity renderers are reserved for visible stored items, moving parts and
station contents.

## 2026-10-20 - 18 coordinated finishes, not a cross-product

Superseded 2026-10-21 by One cottage design replaces 18 finishes.

Each finish = one frame material and colour + one fabric colour, following the concept sheet's 18
swatches (7 natural, 11 painted/material). Rejected: 18 woods x 18 upholsteries (324 per piece).
Frame and fabric stay separate layers, so independent upholstery remains possible later.

## 2026-10-20 - Standalone; no RedzCore dependency

Similar-looking registration and networking scaffolding is not enough reason for a shared library.
Patterns from Roundwood (build, datagen, generators, GameTest fixtures) and DankRecipeTransfer
(request validation) are adapted locally. Revisit only when two mods need the same maintained code.

## 2026-10-20 - Integrated decor, staged development, one complete release

Witchcraft and the cottage furniture ship as one mod. Development runs through private milestones
(docs/FEATURES.md); the first public release, if any, contains every agreed system. Release timing is
the owner's decision.

## 2026-10-20 - Platform: Minecraft 1.20.1, Forge 47.4.10, Java 17, ForgeGradle 6.0.54

Matches the workspace's other mods and the owner's pack. Mod id `herbalwitch`, package
`io.github.dirtyredz.herbalwitch`, MIT.

## 2026-10-23 - Equipment receipts and recipe distinction

Canonical equipment transfers retain cursor escrow and a completed receipt watermark checked against the saved player at startup. Missing, older or divergent receipt evidence quarantines the bond without replay or refund. Native equipment and Curios never provide a second familiar charm.

The plan gives fox pouch and raven satchel identical workbench inputs. The workbench has no recipe selector and requires unique inputs. Pending owner preference, fox pouch uses a sweet berry instead of rabbit hide; raven satchel retains rabbit hide. Leather, two string, 200 ticks and 5 energy remain unchanged.

The specified bat chime also duplicates the landed M8 capture charm exactly. Bat chime replaces leather with an iron nugget, retaining its amethyst shard and two string, 200 ticks and 5 energy. M8 capture recipes remain unchanged. Quantity changes alone cannot distinguish recipes because processing accepts at least the required count.

M9 recovery distinguishes actual lethal gameplay damage from administrative deletion. A lethal damage checkpoint retires the live incarnation without loot; revival restores half maximum health, clears familiar temporary buffs and keeps prepaid reserve unchanged. Level-two recovery commands never directly spawn or invent payloads, and cannot release unresolved inventory/payment escrow.

M9 pickup decisions: retrieval accepts one whole eligible loose stack that fits cargo, rather than
creating a partial remainder with a second recovery authority. A stack above 8 (16 with matching
gear), or above remaining compatible capacity, is a no-op. The source UUID's immutable retirement
receipt survives cargo collection and is independent of later player receipt watermarks. Failures
retain item data in pending bond/player escrow and never automatically replay a grant or refund.

M9 restart qualification uses deterministic checkpoint replay between distinct dedicated server JVMs. It captures actual verified shard/player-save bytes around replacement and re-admits saved entity NBT through production events. It establishes conservative identity/inventory recovery at those boundaries; it does not claim host power-loss durability or random OS-kill coverage.

M9 qualification found a player capability lifecycle gap: Forge invalidates handles on dimension
transfer and revives its dispatcher, but the provider's old LazyOptional stays invalid. Witch and
Grimoire discovery providers now renew the handle on a subsequent dispatcher query while retaining
the same durable state. The real cross-dimension recall test asserts preserved energy and knowledge.
Curios 5.14.1 attaches an exact `{Curios: []}` inventory payload to slotless animals. Native capture
and familiar snapshots preserve that inert shape; populated slots, extra fields, malformed tags and
all other foreign capabilities remain refused. This is an empty-payload contract, not Curios storage
support for familiars. No foreign runtime classes enter the validator.
M10 flight authority: a broom never assigns vanilla vehicle-position authority to its rider. Dedicated intent packets carry only nonce, sequence and flags. Default flight costs zero; configured costs still use the existing spend transaction. No client prediction is promised: observed entity movement uses server snapshots. The saved physical item is not a crash-atomic transfer across player/chunk files; ordinary Minecraft save limitations apply.

Passive broom maintenance uses the same `WitchStatusService.spend` rollback/debit transaction with practice disabled by its participant. Other paid actions keep the default practice policy. This prevents optional flight upkeep from becoming a passive progression farm.

M10 presentation uses the authored broom item model for both held and deployed rendering, resolved through the current model manager on each render so reloads replace textures/models. Input is server-authoritative with client interpolation and no prediction. Seven wand selectors delegate focus and familiar commands to existing menus; the wand creates neither a second ritual executor nor a second familiar roster. Tool showcase sections preserve the combined write budget by compacting inert display plots.

The broom opts out of vanilla `hasExactlyOnePlayerPassenger` save ownership. Minecraft uses that predicate to exclude mounted vehicles from chunk persistence, embed them in player RootVehicle NBT, and remove them on logout. Returning false for this physical vehicle keeps one chunk-owned entity in all three paths; ordinary passenger capacity and authenticated rider checks are independent. The restart harness reproduced the missing mounted entity before this correction.

The final showcase has all four specified command sections: broom_stand, wand_bench, broom and wand. To retain the existing two-row packing algorithm and write cap, empty familiar homes occupy a narrow vertical column and inert familiar display columns are closer together. Their blocks, contents and entity count are unchanged; tool samples remain separate from the unstarted crafting stations.

M10 rider alignment uses the same authored item geometry at 0.55 blocks above the entity origin. The player riding offset remains 0.2; vanilla player offset and scale put seated hips at approximately 0.553. A headless native-player geometry test and three reviewed previews guard this relationship.

## 2026-10-24 - Match Roundwood showcase labels (1.0.0-rc.2)

Roundwood 2da57e6 places standing sample signs two columns to the viewer's left, in the sample's front row, facing the viewer. Herbal Witch uses a fixed north-facing layout, so this is east
of the sample with one clear cell. `ShowcasePlacement.sampleLabel` owns that arithmetic; sections
provide their eastern footprint edge, including supports. Headings and instruction-only notes
keep explicit positions. This supersedes the October 22 west-side rule. Habitat spacing and
nearby instruction positions reserve room for the moved signs. Preparation and grimoire displays use
narrower rows/columns, and altar/charm notes use separate rows, to retain the combined write cap; no RedzCore dependency is added.
