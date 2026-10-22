package io.github.dirtyredz.herbalwitch.apothecary.botany.catalog;

import java.util.List;

/**
 * One herb: its permanent identity and growth form. Plain data; registration, datagen and the
 * showcase read it. Ids are {@code botany/<group>/<herb>/{plant,seeds,fresh}} and never change.
 *
 * @param id          stable herb segment, e.g. {@code lavender}
 * @param displayName English name, e.g. {@code Lavender}
 * @param habitat     permanent group (and default habitat)
 * @param form        crop (harvest resets to age 0) or bush (resets to age 1)
 * @param traits      fictional ingredient traits, in display order
 * @param starter     one of the four herbs reachable from any world (starter recipes, grass foraging)
 */
public record HerbSpec(String id, String displayName, HerbHabitat habitat, Form form, List<HerbTrait> traits,
                       boolean starter) {
    public static final int MAX_AGE = 3;

    public enum Form {
        CROP(0),
        BUSH(1);

        private final int harvestResetAge;

        Form(int harvestResetAge) {
            this.harvestResetAge = harvestResetAge;
        }

        /** Age a mature plant returns to after a harvest by hand. */
        public int harvestResetAge() {
            return harvestResetAge;
        }
    }

    public HerbSpec {
        traits = List.copyOf(traits);
    }

    /** {@code botany/<group>/<herb>}. */
    public String directory() {
        return "botany/" + habitat.directory() + "/" + id;
    }

    public String plantPath() {
        return directory() + "/plant";
    }

    public String seedsPath() {
        return directory() + "/seeds";
    }

    public String freshPath() {
        return directory() + "/fresh";
    }
}
