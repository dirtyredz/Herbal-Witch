package io.github.dirtyredz.herbalwitch.grimoire.content;

import java.util.ArrayList;
import java.util.List;

/** Independent M8 additions; the original 0.6.1 pin remains untouched. */
final class ContentOrderAnimals {
    static final List<String> ADDITIONS = List.of(
            "recipe|herbalwitch:processing/animals/grinding/berry",
            "recipe|herbalwitch:processing/animals/grinding/calming_powder",
            "recipe|herbalwitch:processing/animals/grinding/fish",
            "recipe|herbalwitch:processing/animals/grinding/grain",
            "recipe|herbalwitch:processing/animals/grinding/meat",
            "recipe|herbalwitch:processing/animals/grinding/root",
            "recipe|herbalwitch:processing/animals/grinding/seed",
            "recipe|herbalwitch:processing/animals/charm_work/capture_charm",
            "guide|herbalwitch:animals/care");

    private ContentOrderAnimals() {}

    static List<String> expected() {
        var expected = new ArrayList<>(ContentOrderBaseline.KEYS);
        // Recipe contribution priority 700, category order then lexical recipe id within each category.
        expected.addAll(expected.indexOf("recipe|herbalwitch:processing/grinding/black_dye"), ADDITIONS.subList(0, 7));
        expected.add(expected.indexOf("recipe|herbalwitch:processing/charm_work/arthropod_amulet"), ADDITIONS.get(7));
        // Animal guide priority 1100 follows the priority-1000 charm guide.
        expected.add(ADDITIONS.get(8));
        return List.copyOf(expected);
    }
}
