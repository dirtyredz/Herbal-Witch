# Decisions - The Herbal Witch

Newest first.

## 2026-10-20 - Lit lamp models live beside the palette directories

`decor/lighting/table_lamp/lit/<palette>/<finish>` rather than `.../<palette>/lit/<finish>`: the
painted directory already holds 11 finish models, and a `lit/` entry would make 12. Rejected:
`<finish>_lit` siblings (doubles the leaf directory).

## 2026-10-20 - Coordinated tints over per-finish textures

Frame and fabric textures are neutral greys tinted per finish (tint index 0 frame, 1 fabric);
brass, glass and tinctures are untinted. Motifs therefore read tone-on-tone. Nine textures serve 90
blocks and upholstery can later become independently selectable without new art. Rejected: 18
pre-coloured fabric textures (multi-colour florals; ties fabric to frame forever). Revisit if the
owner wants multi-colour motifs (docs/ART.md).

## 2026-10-20 - Stonecutter restyling, crafting only the dark oak base

Five ordinary recipes make the dark oak base; the stonecutter converts any finish of a piece into any
other finish of the same piece, 1:1, free. Each restyle recipe lists the other 17 finishes, so no
recipe maps an item to itself and none changes the piece. Rejected: 18 stain items, fictional wood
species (walnut, rosewood and pale oak are appearances, not trees), per-finish crafting (90 recipes
of noise). An intentional initial simplification.

## 2026-10-20 - Ids are `decor/<family>/<piece>/<palette>/<finish>`

Registry ids carry slashes so every generated directory (blockstates, models, loot, recipes,
advancements) stays bounded below 12 entries as the catalogue grows to hundreds of pieces. Room
categories are catalogue metadata, not id segments. Ids are permanent once released.

## 2026-10-20 - One registered block per piece and finish, baked models

Plain blocks and items give stacks, drops, recipes, pick-block and viewer indexing for free, at the
cost of many small registry and model entries. Rejected: appearance NBT or a block entity with a
custom model loader. Block entity renderers are reserved for visible stored items, moving parts and
station contents.

## 2026-10-20 - 18 coordinated finishes, not a cross-product

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
