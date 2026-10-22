package io.github.dirtyredz.herbalwitch.data.decor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.decor.manifest.DecorPieces.Piece;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.GENERATED;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.json;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every delivered piece has exactly one ordinary crafting recipe, fitting the 3 x 3 grid, and no two
 * of the mod's shaped recipes share a grid (pattern and ingredients, mirror images included), so no
 * craft is ambiguous. Collisions with vanilla's recipes are checked against the live recipe manager
 * by the decor GameTests.
 */
class DecorRecipeTest {
    /** A shaped grid as one string, each cell naming its ingredient; trimmed patterns compare as crafted. */
    static String grid(JsonObject recipe, boolean mirrored) {
        JsonArray pattern = recipe.getAsJsonArray("pattern");
        JsonObject key = recipe.getAsJsonObject("key");
        StringBuilder out = new StringBuilder();
        for (JsonElement row : pattern) {
            String r = row.getAsString();
            if (mirrored) {
                r = new StringBuilder(r).reverse().toString();
            }
            for (char c : r.toCharArray()) {
                out.append(c == ' ' ? "_" : key.get(String.valueOf(c)).toString()).append(',');
            }
            out.append('/');
        }
        return out.toString();
    }

    @Test
    void everyDeliveredPieceHasOneFittingRecipe() {
        for (Piece piece : DecorManifest.pieces()) {
            JsonObject recipe = generated("data/herbalwitch/recipes/" + piece.recipePath() + ".json");
            assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString(), piece.id());
            JsonArray pattern = recipe.getAsJsonArray("pattern");
            assertTrue(pattern.size() <= 3, piece.id());
            pattern.forEach(row -> assertTrue(row.getAsString().length() <= 3, piece.id()));
            assertEquals("herbalwitch:" + piece.path(), recipe.getAsJsonObject("result").get("item").getAsString(), piece.id());
        }
    }

    @Test
    void noTwoShapedRecipesOfTheModShareAGrid() throws IOException {
        Map<String, String> grids = new HashMap<>();
        List<Path> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(GENERATED.resolve("data/herbalwitch/recipes"))) {
            walk.filter(p -> p.toString().endsWith(".json")).forEach(files::add);
        }
        for (Path file : files) {
            JsonObject recipe = json(file);
            if (!"minecraft:crafting_shaped".equals(recipe.get("type").getAsString())) {
                continue;
            }
            String name = GENERATED.relativize(file).toString();
            for (boolean mirrored : new boolean[] {false, true}) {
                String other = grids.get(grid(recipe, mirrored));
                assertTrue(other == null || other.equals(name), name + " crafts from the same grid as " + other);
            }
            grids.put(grid(recipe, false), name);
        }
    }
}
