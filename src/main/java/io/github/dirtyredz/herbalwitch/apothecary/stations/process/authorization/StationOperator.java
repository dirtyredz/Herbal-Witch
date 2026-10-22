package io.github.dirtyredz.herbalwitch.apothecary.stations.process.authorization;

import io.github.dirtyredz.herbalwitch.apothecary.stations.process.recipe.ProcessRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.UUID;

/**
 * Who authorised one batch of a magical recipe, and exactly what they authorised: the payer, the
 * recipe identity and content fingerprint, and the effective energy that batch costs. A batch stays
 * authorised only while all three still describe the matched recipe; a reloaded recipe, a changed
 * configuration multiplier or a different recipe invalidates it. Saved with the job.
 *
 * @param player      the payer's UUID
 * @param recipe      the recipe id
 * @param fingerprint the recipe's content fingerprint when started
 * @param energy      the effective energy one batch costs, whole energy
 */
public record StationOperator(UUID player, ResourceLocation recipe, String fingerprint, int energy) {
    /** Whether this authorisation still covers {@code current} at the current cost. */
    public boolean covers(ProcessRecipe current, int currentEnergy) {
        return current.getId().equals(recipe) && current.fingerprint().equals(fingerprint) && currentEnergy == energy;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Player", player);
        tag.putString("Recipe", recipe.toString());
        tag.putString("Fingerprint", fingerprint);
        tag.putInt("Energy", energy);
        return tag;
    }

    /** A saved operator, or empty for anything malformed (the batch then needs a new start). */
    public static Optional<StationOperator> load(CompoundTag tag) {
        if (!tag.hasUUID("Player") || !tag.contains("Recipe") || !tag.contains("Fingerprint")) {
            return Optional.empty();
        }
        ResourceLocation recipe = ResourceLocation.tryParse(tag.getString("Recipe"));
        int energy = tag.getInt("Energy");
        if (recipe == null || energy < 0) {
            return Optional.empty();
        }
        return Optional.of(new StationOperator(tag.getUUID("Player"), recipe, tag.getString("Fingerprint"), energy));
    }
}
