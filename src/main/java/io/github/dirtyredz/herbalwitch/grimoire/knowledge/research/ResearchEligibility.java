package io.github.dirtyredz.herbalwitch.grimoire.knowledge.research;

import java.util.List;
import java.util.function.Predicate;

/**
 * When a recipe becomes known, as pure rules over a recipe's requirements and a familiarity test.
 * Quantities never matter: familiarity is about which ingredients, not how many.
 *
 * <ul>
 *   <li>Automatic discovery: every primary ingredient is familiar.</li>
 *   <li>Research at a research block: at least one primary ingredient is familiar.</li>
 *   <li>A recipe with no primary ingredients uses its base and package instead (all of them for
 *       automatic discovery, any one for research).</li>
 * </ul>
 *
 * @param <T> requirement type (an {@code Ingredient} in game, anything in tests)
 */
public final class ResearchEligibility<T> {
    private final List<T> primary;
    private final List<T> secondary;

    /**
     * @param primary   the counted station ingredients
     * @param secondary the base liquid and the package
     */
    public ResearchEligibility(List<T> primary, List<T> secondary) {
        this.primary = List.copyOf(primary);
        this.secondary = List.copyOf(secondary);
    }

    private List<T> deciding() {
        return primary.isEmpty() ? secondary : primary;
    }

    /** Known without study: every deciding requirement is familiar (never for a recipe with none). */
    public boolean automatic(Predicate<T> familiar) {
        List<T> deciding = deciding();
        return !deciding.isEmpty() && deciding.stream().allMatch(familiar);
    }

    /** May be researched: at least one deciding requirement is familiar. */
    public boolean researchable(Predicate<T> familiar) {
        return deciding().stream().anyMatch(familiar);
    }
}
