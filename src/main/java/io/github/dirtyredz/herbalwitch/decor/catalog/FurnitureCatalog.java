package io.github.dirtyredz.herbalwitch.decor.catalog;

import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec.Wave;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.FixturesCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.LightingCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.OpeningsCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SeatingCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.StorageCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.SurfacesCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.pieces.TextilesCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * The whole furniture catalogue: every piece of the seven family catalogues, in creative-tab order
 * (family by family), and the delivered subset. Waves are metadata: a piece of a wave later than
 * {@link #DELIVERED} is described but not registered, so a wave checkpoint contains only completed
 * wave content.
 */
public final class FurnitureCatalog {
    /** The last delivered wave; every piece up to and including it is registered. */
    public static final Wave DELIVERED = Wave.B;

    /** Every described piece, delivered or not, in creative-tab order. */
    public static final List<FurnitureSpec> DESCRIBED = Stream.of(SeatingCatalog.ALL, SurfacesCatalog.ALL, StorageCatalog.ALL,
            LightingCatalog.ALL, TextilesCatalog.ALL, FixturesCatalog.ALL, OpeningsCatalog.ALL).flatMap(List::stream).toList();

    /** Every registered piece, in creative-tab order. */
    public static final List<FurnitureSpec> ALL = DESCRIBED.stream().filter(p -> p.wave().compareTo(DELIVERED) <= 0).toList();

    private FurnitureCatalog() {
    }

    public static Optional<FurnitureSpec> byId(String id) {
        return ALL.stream().filter(p -> p.id().equals(id)).findFirst();
    }

    /** The registered pieces a wave delivered, in catalogue order. */
    public static List<FurnitureSpec> withWave(Wave wave) {
        return ALL.stream().filter(p -> p.wave() == wave).toList();
    }

    /** The registered pieces of one behaviour, in catalogue order. */
    public static List<FurnitureSpec> withBehavior(FurnitureBehavior behavior) {
        List<FurnitureSpec> out = new ArrayList<>();
        for (FurnitureSpec spec : ALL) {
            if (spec.behavior() == behavior) {
                out.add(spec);
            }
        }
        return out;
    }
}
