package io.github.dirtyredz.herbalwitch.decor.structure;

import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacementItem;
import net.minecraft.world.level.block.Block;

/**
 * The item of every furniture piece: the shared checked structure placement
 * ({@link StructurePlacementItem}), so a piece is placed whole, where the preview shows it, or not
 * at all. A type of its own so furniture items stay recognisable (the small rug keeps vanilla's).
 */
public class FurniturePlacementItem extends StructurePlacementItem {
    public FurniturePlacementItem(Block block, Properties properties) {
        super(block, properties);
    }
}
