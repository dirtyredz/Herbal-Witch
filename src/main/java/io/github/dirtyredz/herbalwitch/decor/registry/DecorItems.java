package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.structure.FurniturePlacementItem;
import io.github.dirtyredz.herbalwitch.platform.placement.StructurePlacement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * The item of every registered furniture block, under the same id: the checked structure placement
 * item, or a plain block item for the vanilla-carpet small rug.
 */
public final class DecorItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);

    private DecorItems() {
    }

    /** Registers one item per registered decor block. Called once, after the blocks. */
    static void registerBlockItems() {
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            ITEMS.register(entry.spec().path(), () -> entry.block().get() instanceof StructurePlacement
                    ? new FurniturePlacementItem(entry.block().get(), new Item.Properties())
                    : new BlockItem(entry.block().get(), new Item.Properties()));
        }
    }

    public static Item item(FurnitureSpec spec) {
        return DecorBlocks.block(spec).asItem();
    }

    /** Every furniture item, in creative-tab order. */
    public static List<Item> all() {
        return DecorBlocks.all().stream().map(block -> block.asItem()).toList();
    }
}
