package io.github.dirtyredz.herbalwitch.decor.catalog;

import net.minecraft.world.level.material.MapColor;

/**
 * One furniture design: the look every registered piece shares. Today there is exactly one (the
 * cottage design, docs/ART.md); the type stays so alternative designs can return later as their
 * own catalogue entries without changing how pieces are described.
 *
 * @param id          stable design id
 * @param displayName English name of the design
 * @param mapColor    the colour furniture in this design shows on maps
 */
public record Finish(String id, String displayName, MapColor mapColor) {
}
