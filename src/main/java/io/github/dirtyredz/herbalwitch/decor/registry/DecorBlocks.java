package io.github.dirtyredz.herbalwitch.decor.registry;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.Finish;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * The deferred block register for furniture and the lookup of a piece in a finish. What gets
 * registered is decided by the catalogue-driven registration ({@link ArtSpikeRegistration}).
 */
public final class DecorBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, HerbalWitch.MOD_ID);

    /** One registered piece in one finish. */
    public record Entry(FurnitureSpec spec, Finish finish, RegistryObject<Block> block) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private DecorBlocks() {
    }

    static void register(FurnitureSpec spec, Finish finish, Supplier<Block> factory) {
        ENTRIES.add(new Entry(spec, finish, BLOCKS.register(spec.path(finish), factory)));
    }

    /** Every registered piece and finish, in creative-tab order. */
    public static List<Entry> entries() {
        return Collections.unmodifiableList(ENTRIES);
    }

    public static Block block(FurnitureSpec spec, Finish finish) {
        for (Entry entry : ENTRIES) {
            if (entry.spec().equals(spec) && entry.finish().equals(finish)) {
                return entry.block().get();
            }
        }
        throw new IllegalArgumentException("not registered: " + spec.path(finish));
    }

    /** Every finish of one piece. */
    public static List<Block> of(FurnitureSpec spec) {
        return ENTRIES.stream().filter(e -> e.spec().equals(spec)).map(e -> e.block().get()).toList();
    }

    public static List<Block> all() {
        return ENTRIES.stream().map(e -> e.block().get()).toList();
    }
}
