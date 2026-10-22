package io.github.dirtyredz.herbalwitch.data.decor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.decor.manifest.DecorPieces.Piece;
import io.github.dirtyredz.herbalwitch.data.fixture.ItemDisplays;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver.Resolved;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import io.github.dirtyredz.herbalwitch.decor.catalog.FinishCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureCatalog;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureSpec;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The single-design furniture (owner direction 2026-10-21): every delivered piece once, crafted
 * directly, never restyled; untinted compiled models that use only their own textures, with
 * trailing ivy; multiblocks drop from their origin only.
 */
class DecorResourcesTest {
    /** Textures a furniture model may draw without owning: the herb atlas and the bundle sprites. */
    private static boolean shared(String texture) {
        return texture.equals("herbalwitch:block/botany/parts") || texture.startsWith("herbalwitch:item/preparations/herbs/");
    }

    private static FurnitureSpec spec(Piece piece) {
        return FurnitureCatalog.byId(piece.id()).orElseThrow(() -> new AssertionError("not registered: " + piece.id()));
    }

    @Test
    void oneDesignAndEveryDeliveredPieceWithStablePaths() {
        assertEquals(List.of(FinishCatalog.COTTAGE), FinishCatalog.ALL);
        assertEquals(DecorManifest.DELIVERED, FurnitureCatalog.DELIVERED.name());
        assertEquals(DecorManifest.ids(), FurnitureCatalog.ALL.stream().map(FurnitureSpec::path).toList());
        for (Piece piece : DecorManifest.pieces()) {
            FurnitureSpec spec = spec(piece);
            assertEquals(piece.name(), spec.displayName(), piece.id());
            assertEquals(piece.wave(), spec.wave().name(), piece.id());
            assertEquals(piece.recipePath(), spec.recipePath(), piece.id());
            assertEquals(List.of(piece.width(), piece.depth(), piece.height()),
                    List.of(spec.mount().size()[0], spec.mount().size()[1], spec.mount().size()[2]), piece.id() + " footprint");
            assertEquals(piece.cells(), spec.mount().cells().size(), piece.id() + " cells");
        }
    }

    @Test
    void everyPieceCraftsDirectlyAndDropsOnceFromItsOrigin() {
        Map<String, Integer> counts = Map.of("small_wall_shelf", 2, "small_rug", 2);
        for (Piece piece : DecorManifest.pieces()) {
            String id = piece.path();
            JsonObject recipe = generated("data/herbalwitch/recipes/" + piece.recipePath() + ".json");
            assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString(), id);
            JsonObject result = recipe.getAsJsonObject("result");
            assertEquals("herbalwitch:" + id, result.get("item").getAsString(), id);
            assertEquals(counts.getOrDefault(piece.id(), 1), result.has("count") ? result.get("count").getAsInt() : 1, id);
            String loot = generated("data/herbalwitch/loot_tables/blocks/" + id + ".json").toString();
            assertTrue(loot.contains("\"name\":\"herbalwitch:" + id + "\"") && loot.contains("minecraft:survives_explosion"), id);
            FurnitureSpec spec = spec(piece);
            if (spec.behavior() == FurnitureBehavior.DOOR) {
                assertTrue(loot.contains("\"half\":\"lower\""), id + ": only the lower half drops");
            } else if (spec.multiblock()) {
                String property = spec.behavior() == FurnitureBehavior.BED ? "cell" : "part";
                assertTrue(loot.contains("\"" + property + "\":\"0\""), id + ": only the origin drops");
            }
        }
    }

    @Test
    void tagsCoverEveryPieceAndRugsAreNotAxeMineable() {
        JsonArray furniture = generated("data/herbalwitch/tags/blocks/furniture.json").getAsJsonArray("values");
        assertEquals(DecorManifest.ids().size(), furniture.size());
        String axe = generated("data/minecraft/tags/blocks/mineable/axe.json").getAsJsonArray("values").toString();
        for (Piece piece : DecorManifest.pieces()) {
            boolean rug = spec(piece).behavior() == FurnitureBehavior.RUG;
            assertEquals(!rug, axe.contains("\"herbalwitch:" + piece.path() + "\""), "axe " + piece.id());
        }
    }

    /** The models a piece's states use, and the 0.1.0 templates' exact state mapping. */
    @Test
    void blockstatesUseTheCompiledModels() {
        for (Piece piece : DecorManifest.pieces()) {
            String id = piece.path();
            JsonObject variants = generated("assets/herbalwitch/blockstates/" + id + ".json").getAsJsonObject("variants");
            String template = DecorManifest.TEMPLATES.get(piece.id());
            Set<String> models = new TreeSet<>();
            for (Map.Entry<String, JsonElement> v : variants.entrySet()) {
                String model = v.getValue().getAsJsonObject().get("model").getAsString();
                models.add(model);
                ModelResolver.resolve(model); // exists and resolves
                int y = v.getValue().getAsJsonObject().has("y") ? v.getValue().getAsJsonObject().get("y").getAsInt() : 0;
                assertTrue(y % 90 == 0, id + " " + v.getKey() + " turns " + y);
            }
            if (template != null) {
                String root = "herbalwitch:block/decor/templates/" + template;
                assertEquals(piece.id().equals("table_lamp") ? Set.of(root + "_off", root + "_on") : Set.of(root), models, id);
            } else {
                String root = "herbalwitch:block/decor/pieces/" + piece.family() + "/" + piece.group() + "/" + piece.id() + "/";
                models.forEach(m -> assertTrue(m.startsWith(root), id + " uses " + m));
                FurnitureSpec spec = spec(piece);
                int parts = (int) models.stream().filter(m -> m.substring(root.length()).startsWith("part_")).count();
                assertEquals(piece.cells() == 1 ? 0 : piece.cells(), parts, id + " part models");
                if (spec.behavior() == FurnitureBehavior.LIGHT && piece.cells() > 1) {
                    assertEquals(piece.cells(), models.stream().filter(m -> m.substring(root.length()).startsWith("lit_")).count(), id + " lit");
                }
            }
            if (!piece.id().equals("small_rug") && !piece.id().equals("table_lamp")) {
                Map<String, Integer> turns = Map.of("facing=north", 0, "facing=east", 90, "facing=south", 180, "facing=west", 270);
                for (Map.Entry<String, JsonElement> v : variants.entrySet()) {
                    String facing = v.getKey().replaceAll(".*(facing=[a-z]+).*", "$1");
                    if (spec(piece).behavior() == FurnitureBehavior.STATIC || spec(piece).behavior() == FurnitureBehavior.SEATING
                            || spec(piece).behavior() == FurnitureBehavior.STORAGE || spec(piece).behavior() == FurnitureBehavior.DISPLAY) {
                        JsonObject o = v.getValue().getAsJsonObject();
                        assertEquals(turns.get(facing), o.has("y") ? o.get("y").getAsInt() : 0, id + " " + v.getKey());
                    }
                }
            }
        }
    }

    /** Every model a piece shows is untinted Blockbench output using only its own textures; the set has vines. */
    @Test
    void modelsUseOnlyTheirOwnTexturesWithVines() {
        boolean vines = false;
        for (Piece piece : DecorManifest.pieces()) {
            JsonObject variants = generated("assets/herbalwitch/blockstates/" + piece.path() + ".json").getAsJsonObject("variants");
            Set<String> models = new TreeSet<>();
            variants.entrySet().forEach(v -> models.add(v.getValue().getAsJsonObject().get("model").getAsString()));
            String owner = "herbalwitch:block/pieces/" + spec(piece).artProject() + "/";
            for (String id : models) {
                TemplateChecks.checkAuthored(id);
                Resolved model = ModelResolver.resolve(id);
                for (Map.Entry<String, String> texture : model.textures().entrySet()) {
                    if (!texture.getKey().equals("particle")) {
                        assertTrue(texture.getValue().startsWith(owner) || shared(texture.getValue()),
                                id + " uses " + texture.getValue() + ", which its piece does not own");
                        assertFalse(texture.getValue().contains("floral"), id + ": no floral fabric");
                    }
                }
                vines |= model.textures().containsKey("ivy");
            }
        }
        assertTrue(vines, "no furniture model trails ivy");
    }

    @Test
    void onlyTheLitLampGlowsAndBothLampsShareTheirShape() {
        JsonArray off = ModelResolver.resolve("herbalwitch:block/decor/templates/table_lamp_off").elements();
        JsonArray on = ModelResolver.resolve("herbalwitch:block/decor/templates/table_lamp_on").elements();
        assertEquals(off.size(), on.size());
        int glowing = 0;
        for (int i = 0; i < off.size(); i++) {
            assertEquals(off.get(i).getAsJsonObject().get("from"), on.get(i).getAsJsonObject().get("from"));
            assertEquals(off.get(i).getAsJsonObject().get("to"), on.get(i).getAsJsonObject().get("to"));
            assertTrue(!off.get(i).getAsJsonObject().has("forge_data"), "the unlit lamp glows");
            JsonObject element = on.get(i).getAsJsonObject();
            if (element.has("forge_data")) {
                assertTrue(element.toString().contains("\"#lamp_shade\""), "only the shade glows");
                glowing++;
            } else {
                assertTrue(!element.toString().contains("\"#lamp_shade\""), "the whole shade glows");
            }
        }
        assertTrue(glowing >= 2, "the lit shade glows");
    }

    /** The item is the whole piece (a multiblock's assembly); its display transforms come from the Blockbench project. */
    @Test
    void everyItemIsItsWholePieceWithInheritedTransforms() {
        for (Piece piece : DecorManifest.pieces()) {
            String id = piece.path();
            Resolved item = ModelResolver.resolve("herbalwitch:item/" + id);
            String template = DecorManifest.TEMPLATES.get(piece.id());
            String expected;
            if (template != null) {
                expected = "herbalwitch:block/decor/templates/" + template + (piece.id().equals("table_lamp") ? "_off" : "");
            } else {
                String root = "herbalwitch:block/decor/pieces/" + piece.family() + "/" + piece.group() + "/" + piece.id() + "/";
                boolean light = spec(piece).behavior() == FurnitureBehavior.LIGHT;
                expected = root + (piece.cells() > 1 ? "assembly" : light ? "off" : "base");
            }
            assertEquals(expected, item.chain().get(1), id);
            ItemDisplays.assertInherited("herbalwitch:item/" + id);
        }
    }
}
