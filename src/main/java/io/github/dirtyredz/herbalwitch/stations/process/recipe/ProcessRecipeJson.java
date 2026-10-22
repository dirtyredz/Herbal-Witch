package io.github.dirtyredz.herbalwitch.stations.process.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The single JSON and network format of every processing recipe, with per-type bounds. Datagen
 * writes through {@link #write}, the server reads through {@link #read}, clients through
 * {@link #fromNetwork}; all three enforce the same limits.
 *
 * <pre>
 * {"ingredients": [{"ingredient": {...}, "count": 1}, ...],
 *  "base": {"ingredient": {...}, "count": 1},          (steeping, infusion)
 *  "packaging": {"ingredient": {...}, "count": 1},     (infusion)
 *  "result": {"item": "...", "count": 1, "nbt": "..."},
 *  "returns": [{"item": "minecraft:glass_bottle"}],
 *  "duration": 200,
 *  "requirements": {"witch": true, "energy": 10}}      (magical recipes only: ProcessRequirements)
 * </pre>
 */
public final class ProcessRecipeJson {
    public static final int MAX_COUNT = 64;
    public static final int MAX_DURATION = 72_000;
    public static final int MAX_RETURNS = 2;

    /**
     * What a recipe type allows.
     *
     * @param maxIngredients input slots of the station
     * @param base           whether a base liquid is required
     * @param packaging      whether a package is required
     * @param heated         whether the station needs heat (recorded in the description, not in JSON)
     * @param requirements   which requirements the type accepts: ordinary, magical or either
     */
    public record Bounds(int maxIngredients, boolean base, boolean packaging, boolean heated, ProcessRequirements.Policy requirements) {
        /** An ordinary-only type. */
        public Bounds(int maxIngredients, boolean base, boolean packaging, boolean heated) {
            this(maxIngredients, base, packaging, heated, ProcessRequirements.Policy.ORDINARY);
        }
    }

    private ProcessRecipeJson() {
    }

    public static ProcessRecipeDescription read(JsonObject json, Bounds bounds) {
        JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
        List<ProcessRecipeDescription.Counted> ingredients = new ArrayList<>();
        for (JsonElement e : array) {
            ingredients.add(counted(e.getAsJsonObject()));
        }
        Optional<ProcessRecipeDescription.Counted> base = json.has("base")
                ? Optional.of(counted(GsonHelper.getAsJsonObject(json, "base"))) : Optional.empty();
        Optional<ProcessRecipeDescription.Counted> packaging = json.has("packaging")
                ? Optional.of(counted(GsonHelper.getAsJsonObject(json, "packaging"))) : Optional.empty();
        ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true, true);
        List<ItemStack> returns = new ArrayList<>();
        if (json.has("returns")) {
            for (JsonElement e : GsonHelper.getAsJsonArray(json, "returns")) {
                returns.add(CraftingHelper.getItemStack(e.getAsJsonObject(), true, true));
            }
        }
        int duration = GsonHelper.getAsInt(json, "duration");
        ProcessRequirements requirements = ProcessRequirements.read(json);
        return validate(new ProcessRecipeDescription(ingredients, base, packaging, result, returns, duration, bounds.heated(), requirements),
                bounds);
    }

    private static ProcessRecipeDescription.Counted counted(JsonObject json) {
        Ingredient ingredient = Ingredient.fromJson(GsonHelper.getNonNull(json, "ingredient"), false);
        return new ProcessRecipeDescription.Counted(ingredient, GsonHelper.getAsInt(json, "count", 1));
    }

    /** Rejects anything outside the type's bounds; the same checks run for JSON and network input. */
    public static ProcessRecipeDescription validate(ProcessRecipeDescription d, Bounds bounds) {
        if (d.ingredients().isEmpty() || d.ingredients().size() > bounds.maxIngredients()) {
            throw new JsonParseException("expected 1 to " + bounds.maxIngredients() + " ingredients, got " + d.ingredients().size());
        }
        List<ProcessRecipeDescription.Counted> all = new ArrayList<>(d.ingredients());
        d.base().ifPresent(all::add);
        d.packaging().ifPresent(all::add);
        for (ProcessRecipeDescription.Counted c : all) {
            if (c.ingredient().isEmpty() || c.count() < 1 || c.count() > MAX_COUNT) {
                throw new JsonParseException("empty ingredient or count outside 1.." + MAX_COUNT);
            }
        }
        if (d.base().isPresent() != bounds.base()) {
            throw new JsonParseException(bounds.base() ? "a base liquid is required" : "this station has no base slot");
        }
        if (d.packaging().isPresent() != bounds.packaging()) {
            throw new JsonParseException(bounds.packaging() ? "packaging is required" : "this station has no packaging slot");
        }
        if (d.result().isEmpty() || d.result().getCount() > d.result().getMaxStackSize()) {
            throw new JsonParseException("empty or oversized result");
        }
        if (d.returns().size() > MAX_RETURNS || d.returns().stream().anyMatch(ItemStack::isEmpty)) {
            throw new JsonParseException("at most " + MAX_RETURNS + " non-empty returns");
        }
        if (d.duration() < 1 || d.duration() > MAX_DURATION) {
            throw new JsonParseException("duration outside 1.." + MAX_DURATION);
        }
        bounds.requirements().check(d.requirements());
        return d;
    }

    public static JsonObject write(ProcessRecipeDescription d) {
        JsonObject json = new JsonObject();
        JsonArray ingredients = new JsonArray();
        d.ingredients().forEach(c -> ingredients.add(counted(c)));
        json.add("ingredients", ingredients);
        d.base().ifPresent(c -> json.add("base", counted(c)));
        d.packaging().ifPresent(c -> json.add("packaging", counted(c)));
        json.add("result", stack(d.result()));
        if (!d.returns().isEmpty()) {
            JsonArray returns = new JsonArray();
            d.returns().forEach(r -> returns.add(stack(r)));
            json.add("returns", returns);
        }
        json.addProperty("duration", d.duration());
        d.requirements().write(json);
        return json;
    }

    private static JsonObject counted(ProcessRecipeDescription.Counted c) {
        JsonObject json = new JsonObject();
        json.add("ingredient", c.ingredient().toJson());
        json.addProperty("count", c.count());
        return json;
    }

    private static JsonObject stack(ItemStack stack) {
        JsonObject json = new JsonObject();
        json.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (stack.getCount() != 1) {
            json.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            json.addProperty("nbt", stack.getTag().toString());
        }
        return json;
    }

    public static ProcessRecipeDescription fromNetwork(FriendlyByteBuf buf, Bounds bounds) {
        int n = buf.readVarInt();
        if (n < 1 || n > bounds.maxIngredients()) {
            throw new IllegalArgumentException("ingredient count " + n);
        }
        List<ProcessRecipeDescription.Counted> ingredients = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            ingredients.add(counted(buf));
        }
        Optional<ProcessRecipeDescription.Counted> base = buf.readBoolean() ? Optional.of(counted(buf)) : Optional.empty();
        Optional<ProcessRecipeDescription.Counted> packaging = buf.readBoolean() ? Optional.of(counted(buf)) : Optional.empty();
        ItemStack result = buf.readItem();
        int r = buf.readVarInt();
        if (r < 0 || r > MAX_RETURNS) {
            throw new IllegalArgumentException("return count " + r);
        }
        List<ItemStack> returns = new ArrayList<>();
        for (int i = 0; i < r; i++) {
            returns.add(buf.readItem());
        }
        int duration = buf.readVarInt();
        ProcessRequirements requirements = ProcessRequirements.fromNetwork(buf);
        try {
            return validate(new ProcessRecipeDescription(ingredients, base, packaging, result, returns, duration, bounds.heated(),
                    requirements), bounds);
        } catch (JsonParseException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private static ProcessRecipeDescription.Counted counted(FriendlyByteBuf buf) {
        Ingredient ingredient = Ingredient.fromNetwork(buf);
        return new ProcessRecipeDescription.Counted(ingredient, buf.readVarInt());
    }

    public static void toNetwork(FriendlyByteBuf buf, ProcessRecipeDescription d) {
        buf.writeVarInt(d.ingredients().size());
        d.ingredients().forEach(c -> counted(buf, c));
        buf.writeBoolean(d.base().isPresent());
        d.base().ifPresent(c -> counted(buf, c));
        buf.writeBoolean(d.packaging().isPresent());
        d.packaging().ifPresent(c -> counted(buf, c));
        buf.writeItem(d.result());
        buf.writeVarInt(d.returns().size());
        d.returns().forEach(buf::writeItem);
        buf.writeVarInt(d.duration());
        d.requirements().toNetwork(buf);
    }

    private static void counted(FriendlyByteBuf buf, ProcessRecipeDescription.Counted c) {
        c.ingredient().toNetwork(buf);
        buf.writeVarInt(c.count());
    }
}
