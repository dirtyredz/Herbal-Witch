package io.github.dirtyredz.herbalwitch.data.stations.shop;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.GENERATED;
import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.json;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Exact commerce recipes, every facing (and register state), plain drops, axe tags and resolvable compiled models. */
class ShopResourcesTest {
    private static final java.nio.file.Path ASSETS = java.nio.file.Path.of("src/main/resources/assets/herbalwitch");

    @Test
    void recipesAreExactShapelessSinglePieces() {
        Map<String, Map<String, Integer>> expected = Map.of(
                "register", Map.of("dark_oak_planks", 4, "iron_ingot", 2, "gold_ingot", 1, "paper", 1, "redstone", 1),
                "stock_shelf", Map.of("dark_oak_slab", 3, "stick", 2, "gold_nugget", 1),
                "stock_jar", Map.of("glass", 3, "dark_oak_slab", 1, "gold_nugget", 1),
                "order_board", Map.of("dark_oak_planks", 3, "paper", 2, "ink_sac", 1));
        for (String piece : ShopManifest.PIECES) {
            JsonObject recipe = json(GENERATED.resolve("data/herbalwitch/recipes/shop/" + piece + ".json"));
            assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString());
            assertEquals("herbalwitch:shop/" + piece, recipe.getAsJsonObject("result").get("item").getAsString());
            assertFalse(recipe.getAsJsonObject("result").has("count") && recipe.getAsJsonObject("result").get("count").getAsInt() != 1);
            Map<String, Integer> actual = new TreeMap<>();
            recipe.getAsJsonArray("ingredients").forEach(e -> actual.merge(e.getAsJsonObject().get("item").getAsString().substring("minecraft:".length()), 1, Integer::sum));
            assertEquals(expected.get(piece), actual, piece);
        }
    }

    @Test
    void blockstatesCoverEveryFacingAndRegisterStateWithCompiledModels() {
        for (String piece : ShopManifest.PIECES) {
            JsonObject variants = json(GENERATED.resolve("assets/herbalwitch/blockstates/shop/" + piece + ".json")).getAsJsonObject("variants");
            Set<String> models = new HashSet<>();
            variants.entrySet().forEach(e -> models.add(e.getValue().getAsJsonObject().get("model").getAsString()));
            if (piece.equals("register")) {
                assertEquals(8, variants.size());
                for (String facing : List.of("north", "east", "south", "west")) {
                    assertTrue(variants.has("facing=" + facing + ",open=false"), facing);
                    assertTrue(variants.has("facing=" + facing + ",open=true"), facing);
                }
                assertEquals(Set.of("herbalwitch:block/shop/register/closed", "herbalwitch:block/shop/register/open"), models);
            } else {
                assertEquals(Set.of("facing=north", "facing=east", "facing=south", "facing=west"), variants.keySet());
                assertEquals(Set.of("herbalwitch:block/shop/" + piece + "/base"), models);
            }
            for (String model : models) assertTrue(Files.isRegularFile(ASSETS.resolve("models/" + model.substring("herbalwitch:".length()) + ".json")), model);
            JsonObject item = json(GENERATED.resolve("assets/herbalwitch/models/item/shop/" + piece + ".json"));
            assertEquals(1, item.size(), "item models only name their parent");
            assertTrue(models.contains(item.get("parent").getAsString()), piece);
        }
    }

    @Test
    void dropsArePlainItemsAndEveryPieceIsAxeMineable() {
        for (String piece : ShopManifest.PIECES) {
            String loot = json(GENERATED.resolve("data/herbalwitch/loot_tables/blocks/shop/" + piece + ".json")).toString();
            assertTrue(loot.contains("herbalwitch:shop/" + piece));
            assertFalse(loot.contains("copy_nbt") || loot.contains("set_nbt") || loot.contains("copy_name"), piece + " drop carries data");
        }
        String axe = json(GENERATED.resolve("data/minecraft/tags/blocks/mineable/axe.json")).toString();
        for (String piece : ShopManifest.PIECES) assertTrue(axe.contains("herbalwitch:shop/" + piece), piece);
    }

    @Test
    void ownedTexturesAndTheScreenAtlasExist() {
        for (String piece : ShopManifest.PIECES) {
            assertTrue(Files.isDirectory(ASSETS.resolve("textures/block/pieces/shop/" + piece)), piece);
        }
        assertTrue(Files.isRegularFile(ASSETS.resolve("textures/gui/shop/shop.png")));
    }
}
