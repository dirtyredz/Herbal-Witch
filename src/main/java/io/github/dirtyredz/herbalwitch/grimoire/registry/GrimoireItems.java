package io.github.dirtyredz.herbalwitch.grimoire.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog;
import io.github.dirtyredz.herbalwitch.grimoire.catalog.GrimoireCatalog.ResearchPiece;
import io.github.dirtyredz.herbalwitch.grimoire.access.item.GrimoireItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

/** The portable grimoire ({@code grimoire/book}, stack size 1) and the research blocks' items. */
public final class GrimoireItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, HerbalWitch.MOD_ID);

    public static final RegistryObject<Item> BOOK = ITEMS.register(GrimoireCatalog.BOOK_PATH,
            () -> new GrimoireItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    static {
        for (ResearchPiece piece : GrimoireCatalog.RESEARCH_PIECES) {
            ITEMS.register(piece.path(), () -> new BlockItem(GrimoireBlocks.block(piece), new Item.Properties()));
        }
    }

    private GrimoireItems() {
    }

    public static boolean isGrimoire(ItemStack stack) {
        return stack.is(BOOK.get());
    }

    public static Item item(ResearchPiece piece) {
        return GrimoireBlocks.block(piece).asItem();
    }

    /** Tab order: the book, then the research blocks. */
    public static List<Item> all() {
        List<Item> items = new ArrayList<>();
        items.add(BOOK.get());
        GrimoireCatalog.RESEARCH_PIECES.forEach(p -> items.add(item(p)));
        return items;
    }
}
