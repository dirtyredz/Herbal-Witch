# Decisions - The Herbal Witch

Newest first.

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
- **Preparation grimoire pages live in `grimoire/content/reference/`.** `AdvancedPreparationEntries` is
  grimoire code reading the brewing catalogues, because brewing must not import the grimoire.
- **The witch picks the ritual target by looking at it, then opens the altar.** Garden Blessing targets a
  herb bed and Home Protection a ward stone; `client/ritual/RitualTargets` reads the aimed block before the
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
  source is `grimoire/content/reference/DecorEntries` (grimoire -> decor), not `decor/grimoire/`, which
  would have made decor import grimoire against the dependency rule.

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

Herb ids are `apothecary/botany/<group>/<herb>/...` with five permanent groups, which keeps every generated
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
