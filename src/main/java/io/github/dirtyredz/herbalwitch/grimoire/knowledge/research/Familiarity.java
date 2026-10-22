package io.github.dirtyredz.herbalwitch.grimoire.knowledge.research;

import io.github.dirtyredz.herbalwitch.platform.recipe.RecipeIngredients;

import io.github.dirtyredz.herbalwitch.grimoire.content.EntryKey;
import io.github.dirtyredz.herbalwitch.grimoire.content.reference.DiscoveryForms;
import io.github.dirtyredz.herbalwitch.platform.recipe.ProcessRecipeDescription;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Which ingredients a player is familiar with right now: an ingredient is familiar when a stack the
 * player carries passes its real {@link Ingredient#test} (so NBT-sensitive ingredients such as
 * water bottles are respected), or when any registered form of a herb the player knows passes it.
 * Carried means the main inventory and the offhand; armour, the cursor and nested containers are
 * not looked into.
 */
public final class Familiarity implements Predicate<Ingredient> {
    private final List<ItemStack> carried;
    private final List<ItemStack> knownForms;

    public Familiarity(List<ItemStack> carried, Predicate<EntryKey> knows) {
        this.carried = List.copyOf(carried);
        List<ItemStack> forms = new ArrayList<>();
        for (EntryKey key : DiscoveryForms.keys()) {
            if (knows.test(key)) {
                forms.addAll(DiscoveryForms.formStacks(key));
            }
        }
        this.knownForms = List.copyOf(forms);
    }

    /** Main inventory then offhand, non-empty, as copies. */
    public static List<ItemStack> carried(Player player) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                out.add(stack.copy());
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.isEmpty()) {
                out.add(stack.copy());
            }
        }
        return out;
    }

    /** An order-sensitive hash of what is carried (item, count, tag), so unchanged inventories skip re-evaluation. */
    public static long fingerprint(List<ItemStack> carried) {
        long hash = 1;
        for (ItemStack stack : carried) {
            hash = hash * 31 + System.identityHashCode(stack.getItem());
            hash = hash * 31 + stack.getCount();
            hash = hash * 31 + (stack.getTag() == null ? 0 : stack.getTag().hashCode());
        }
        return hash;
    }

    public List<ItemStack> carried() {
        return carried;
    }

    @Override
    public boolean test(Ingredient ingredient) {
        return carried.stream().anyMatch(ingredient) || knownForms.stream().anyMatch(ingredient);
    }

    /** The recipe's rules: primary ingredients decide, base and package when there are none. */
    public static ResearchEligibility<Ingredient> eligibility(ProcessRecipeDescription description) {
        return new ResearchEligibility<>(RecipeIngredients.primaries(description), RecipeIngredients.secondaries(description));
    }
}
