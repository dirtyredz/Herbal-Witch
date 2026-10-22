package io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;

/**
 * Prepared forms of each herb: a dried bundle (also a placeable decoration) and a ground powder,
 * ids {@code preparations/herbs/<group>/<herb>/{bundle,powder}}. Every catalogue herb has both.
 */
public final class HerbalMaterialCatalog {
    private HerbalMaterialCatalog() {
    }

    public static String directory(HerbSpec herb) {
        return "preparations/herbs/" + herb.habitat().directory() + "/" + herb.id();
    }

    public static String bundlePath(HerbSpec herb) {
        return directory(herb) + "/bundle";
    }

    public static String powderPath(HerbSpec herb) {
        return directory(herb) + "/powder";
    }
}
