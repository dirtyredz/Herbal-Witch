package io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe;

import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessInventory;
import io.github.dirtyredz.herbalwitch.apothecary.stations.process.ProcessLayout;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TagsUpdatedEvent;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Reload-aware recipe lookup for stations. Recipes of a type are listed once per recipe manager
 * (sorted by id, so ties resolve deterministically) and cached; a station looks them up only when
 * its inventory changed or a reload happened ({@link #generation()}), never on every tick.
 */
public final class ProcessRecipeIndex {
    private static final AtomicLong GENERATION = new AtomicLong();
    private static final Map<RecipeType<?>, Cached<?>> CACHE = new IdentityHashMap<>();

    private record Cached<R>(RecipeManager manager, long generation, List<R> recipes) {
    }

    private ProcessRecipeIndex() {
    }

    /** Bumped whenever server data (recipes and tags) reloads. */
    public static long generation() {
        return GENERATION.get();
    }

    /** Forge bus listener: server data reload (start-up and {@code /reload}). */
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) {
            GENERATION.incrementAndGet();
        }
    }

    /** Every recipe of the type, sorted by id. */
    @SuppressWarnings("unchecked")
    public static synchronized <R extends ProcessRecipe> List<R> recipes(Level level, RecipeType<R> type) {
        RecipeManager manager = level.getRecipeManager();
        Cached<?> cached = CACHE.get(type);
        if (cached == null || cached.manager() != manager || cached.generation() != generation()) {
            List<R> sorted = manager.getAllRecipesFor(type).stream()
                    .sorted(Comparator.comparing(r -> r.getId().toString())).toList();
            cached = new Cached<>(manager, generation(), sorted);
            CACHE.put(type, cached);
        }
        return (List<R>) cached.recipes();
    }

    /** The first recipe (by id) that the inventory runs, with its reservation. */
    public static <R extends ProcessRecipe> Optional<Match<R>> find(Level level, RecipeType<R> type,
                                                                      ProcessInventory inventory, ProcessLayout layout) {
        for (R recipe : recipes(level, type)) {
            int[] reservation = recipe.reserve(inventory, layout);
            if (reservation != null) {
                return Optional.of(new Match<>(recipe, reservation));
            }
        }
        return Optional.empty();
    }

    /** Whether the stack is an ingredient, base or package of any recipe of the type (slot filtering). */
    public static boolean isUsed(Level level, RecipeType<? extends ProcessRecipe> type, ProcessInventory.Role role, ItemStack stack) {
        for (ProcessRecipe recipe : recipes(level, type)) {
            ProcessRecipeDescription d = recipe.description();
            boolean used = switch (role) {
                case INPUT -> d.ingredients().stream().anyMatch(c -> c.ingredient().test(stack));
                case BASE -> d.base().filter(c -> c.ingredient().test(stack)).isPresent();
                case PACKAGING -> d.packaging().filter(c -> c.ingredient().test(stack)).isPresent();
                default -> false;
            };
            if (used) {
                return true;
            }
        }
        return false;
    }

    /** A recipe and the per-slot counts it reserves. */
    public record Match<R extends ProcessRecipe>(R recipe, int[] reservation) {
    }
}
