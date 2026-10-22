package io.github.dirtyredz.herbalwitch.stations.process.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

/**
 * What a recipe asks of whoever runs it, beyond its ingredients: whether a witch must start it and the
 * whole energy one batch costs (before the server's processing multiplier). Immutable and part of the
 * recipe's JSON, wire format, fingerprint and description; an absent {@code "requirements"} object is
 * {@link #NONE}, ordinary zero-cost processing that any player or hopper may run.
 *
 * <pre>{"requirements": {"witch": true, "energy": 10}}</pre>
 *
 * @param witch  whether an initiated witch must explicitly start each batch and pay for it
 * @param energy whole energy per batch; zero unless {@code witch}
 */
public record ProcessRequirements(boolean witch, int energy) {
    /** Ordinary processing. */
    public static final ProcessRequirements NONE = new ProcessRequirements(false, 0);
    /** The largest energy one batch may name. */
    public static final int MAX_ENERGY = 1000;

    public ProcessRequirements {
        if (energy < 0 || energy > MAX_ENERGY) {
            throw new JsonParseException("energy outside 0.." + MAX_ENERGY);
        }
        if (!witch && energy > 0) {
            throw new JsonParseException("only a witch-required recipe may cost energy");
        }
    }

    /** A magical recipe: a witch starts it and pays {@code energy} per batch. */
    public static ProcessRequirements witch(int energy) {
        return new ProcessRequirements(true, energy);
    }

    public boolean magical() {
        return witch;
    }

    /** Reads the optional {@code requirements} object of a recipe. */
    public static ProcessRequirements read(JsonObject recipe) {
        if (!recipe.has("requirements")) {
            return NONE;
        }
        JsonObject json = GsonHelper.getAsJsonObject(recipe, "requirements");
        for (String key : json.keySet()) {
            if (!key.equals("witch") && !key.equals("energy")) {
                throw new JsonParseException("unknown requirement '" + key + "'");
            }
        }
        return new ProcessRequirements(GsonHelper.getAsBoolean(json, "witch", false), GsonHelper.getAsInt(json, "energy", 0));
    }

    /** Adds the {@code requirements} object, only for a magical recipe (ordinary JSON stays as before). */
    public void write(JsonObject recipe) {
        if (this.equals(NONE)) {
            return;
        }
        JsonObject json = new JsonObject();
        json.addProperty("witch", witch);
        json.addProperty("energy", energy);
        recipe.add("requirements", json);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeBoolean(witch);
        buf.writeVarInt(energy);
    }

    public static ProcessRequirements fromNetwork(FriendlyByteBuf buf) {
        boolean witch = buf.readBoolean();
        int energy = buf.readVarInt();
        try {
            return new ProcessRequirements(witch, energy);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    /**
     * The energy a batch costs after the server's processing multiplier (percent, rounded up so a
     * non-zero cost never becomes free).
     */
    public int effectiveEnergy(int percent) {
        if (energy == 0 || percent <= 0) {
            return 0;
        }
        return (int) Math.min(MAX_ENERGY * 10L, (energy * (long) percent + 99) / 100);
    }

    /** Which requirements a recipe type accepts. */
    public enum Policy {
        /** Ordinary recipes only (drying, steeping, bottling). */
        ORDINARY,
        /** Witch-required recipes only (cauldron, distillation, charm work). */
        MAGICAL,
        /** Either (grinding, infusion). */
        EITHER;

        public void check(ProcessRequirements requirements) {
            if (this == ORDINARY && requirements.magical()) {
                throw new JsonParseException("this station runs ordinary recipes only");
            }
            if (this == MAGICAL && !requirements.magical()) {
                throw new JsonParseException("this station runs witch-required recipes only");
            }
        }
    }
}
