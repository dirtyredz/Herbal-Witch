package io.github.dirtyredz.herbalwitch.grimoire.knowledge.research;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.GrimoireContent;
import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeView;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Explicit research at a research block: instant, free, and consuming nothing. It decides only;
 * the caller (the reading session) grants the discovery, so research never writes knowledge on its
 * own and works the same at the stand and the bookshelf.
 */
public final class ResearchService {
    /** The outcome, also the chat message key {@code grimoire.herbalwitch.research.<id>}. */
    public enum Result {
        RESEARCHED,
        ALREADY_KNOWN,
        NOT_A_RECIPE,
        NO_FAMILIAR_INGREDIENT,
        NEEDS_RESEARCH_BLOCK;

        public String messageKey() {
            return "grimoire." + HerbalWitch.MOD_ID + ".research." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private ResearchService() {
    }

    /**
     * Whether the player may learn {@code key} by research now.
     *
     * @param atResearchBlock the reader was opened at a stand or bookshelf, and is still valid
     * @param knows           the player's current knowledge
     */
    public static Result check(ServerPlayer player, GrimoireContent content, EntryKey key, boolean atResearchBlock,
                               Predicate<EntryKey> knows) {
        if (!atResearchBlock) {
            return Result.NEEDS_RESEARCH_BLOCK;
        }
        Optional<RecipeView> recipe = content.recipe(key);
        if (recipe.isEmpty()) {
            return Result.NOT_A_RECIPE;
        }
        if (knows.test(key)) {
            return Result.ALREADY_KNOWN;
        }
        Familiarity familiarity = new Familiarity(Familiarity.carried(player), knows);
        return Familiarity.eligibility(recipe.get().description()).researchable(familiarity)
                ? Result.RESEARCHED : Result.NO_FAMILIAR_INGREDIENT;
    }

    /** Whether research would succeed, for the research filter (no side effects). */
    public static boolean researchable(GrimoireContent content, EntryKey key, Familiarity familiarity, Predicate<EntryKey> knows) {
        return !knows.test(key) && content.recipe(key)
                .map(r -> Familiarity.eligibility(r.description()).researchable(familiarity)).orElse(false);
    }
}
