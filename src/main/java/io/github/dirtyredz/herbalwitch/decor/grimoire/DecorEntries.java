package io.github.dirtyredz.herbalwitch.decor.grimoire;

import io.github.dirtyredz.herbalwitch.apothecary.stations.grimoire.StationEntries;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContentSource;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireEntry;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * One public reference page per delivered furniture piece (decor's contribution to the grimoire,
 * 0.4.0): its editorial blurb, then facts read from the catalogue (size in blocks, how it stands,
 * what using it does, seats or slots, how to get it). The decorative lookalikes of working stations
 * say so and link the real station. Readable before any discovery; grants nothing.
 */
public final class DecorEntries implements GrimoireContentSource {
    /** Where this source reads in the index (lower first); spaced so a later feature slots between. */
    public static final int PRIORITY = 500;

    private static final String PREFIX = "grimoire." + HerbalWitch.MOD_ID + ".decor.";
    public static final String SIZE = PREFIX + "size";
    public static final String CRAFTED = PREFIX + "crafted";
    public static final String LOOKALIKE = PREFIX + "lookalike";
    public static final String FUNCTIONAL_ALTAR = PREFIX + "functional_altar";
    /**
     * The functional altar's page (Milestone 6), named by key so decor's reference never depends on
     * the ritual feature; the link is dropped if that page is absent.
     */
    public static final EntryKey ALTAR = EntryKey.of(EntryKey.Kind.STATION, HerbalWitch.MOD_ID + ":magic/ritual/altar");
    /** Decorative pieces that resemble a working station, and the station they point to. */
    public static final Map<String, StationSpec> LOOKALIKES = Map.of(
            "drying_rack_decor", StationCatalog.DRYING_RACK,
            "fireplace", StationCatalog.HEARTH,
            "firepit", StationCatalog.HEARTH,
            "stove_oven", StationCatalog.STOVE,
            "work_table", StationCatalog.MORTAR,
            "altar_table", StationCatalog.INFUSION_POT);

    public static EntryKey key(FurnitureSpec spec) {
        return EntryKey.of(EntryKey.Kind.FURNITURE, HerbalWitch.MOD_ID + ":" + spec.path());
    }

    /** The editorial blurb's language key. */
    public static String blurb(FurnitureSpec spec) {
        return PREFIX + spec.family().directory() + "." + spec.id();
    }

    /** The mounting fact's key (by support). */
    public static String mount(FurnitureSpec spec) {
        return PREFIX + "mount." + spec.support().name().toLowerCase(Locale.ROOT);
    }

    /** The behaviour fact's key. */
    public static String use(FurnitureSpec spec) {
        return PREFIX + "use." + spec.behavior().name().toLowerCase(Locale.ROOT);
    }

    @Override
    public void contribute(GrimoireContent.Inputs inputs, Consumer<GrimoireEntry> out) {
        for (FurnitureSpec spec : FurnitureCatalog.ALL) {
            int[] size = spec.mount().size();
            List<Component> body = new ArrayList<>();
            body.add(Component.translatable(blurb(spec)));
            body.add(Component.translatable(SIZE, size[0], size[1], size[2]));
            body.add(Component.translatable(mount(spec)));
            body.add(Component.translatable(use(spec), spec.seats() + spec.slots()));
            body.add(Component.translatable(CRAFTED));
            List<EntryKey> links = new ArrayList<>();
            StationSpec station = LOOKALIKES.get(spec.id());
            if (station != null) {
                body.add(Component.translatable(LOOKALIKE));
                links.add(StationEntries.key(station));
            }
            if (spec.id().equals("altar_table")) {
                body.add(Component.translatable(FUNCTIONAL_ALTAR));
                links.add(ALTAR);
            }
            out.accept(GrimoireEntry.page(key(spec), GrimoireContent.FURNITURE, DecorBlocks.block(spec).getName(),
                    DecorBlocks.block(spec).asItem().getDefaultInstance(), body, links));
        }
    }
}
