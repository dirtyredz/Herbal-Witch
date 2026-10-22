package io.github.dirtyredz.herbalwitch.data.magic.ritual;

import com.google.gson.JsonObject;
import io.github.dirtyredz.herbalwitch.data.fixture.BlockstateModels;
import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TemplateChecks;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The ritual circle's generated data and compiled art against the independent manifest: its shapeless
 * recipe, origin-only loot, pickaxe mining, all sixteen cells in every facing and active state, and a
 * flat item sprite (a ring of sixteen cells has no single assembly to show).
 */
class CircleResourcesTest {
    private static final String[] FACINGS = {"north", "east", "south", "west"};

    @Test
    void onlyTheOriginDropsTheCircleAndAPickaxeMinesIt() {
        String text = generated("data/herbalwitch/loot_tables/blocks/" + CircleManifest.ID + ".json").toString();
        assertTrue(text.contains("\"part\":\"0\""), "origin-only condition: " + text);
        assertTrue(text.contains("minecraft:survives_explosion"), "explosion condition");
        assertTrue(text.contains("herbalwitch:" + CircleManifest.ID), text);
        assertTrue(generated("data/minecraft/tags/blocks/mineable/pickaxe.json").toString().contains("herbalwitch:" + CircleManifest.ID));
    }

    /** Every facing, cell and active state shows exactly its part (and its overlay while active), turned to the facing. */
    @Test
    void everyCellFacingAndActiveStateComposes() {
        JsonObject states = generated("assets/herbalwitch/blockstates/" + CircleManifest.ID + ".json");
        for (int f = 0; f < FACINGS.length; f++) {
            for (int part = 0; part < CircleManifest.CELLS; part++) {
                for (boolean active : new boolean[] {false, true}) {
                    List<BlockstateModels.Applied> applied = BlockstateModels.select(states,
                            Map.of("facing", FACINGS[f], "part", String.valueOf(part), "active", String.valueOf(active)));
                    List<BlockstateModels.Applied> expected = new ArrayList<>();
                    expected.add(new BlockstateModels.Applied("herbalwitch:block/ritual/circle/part_" + part, f * 90));
                    if (active) {
                        expected.add(new BlockstateModels.Applied("herbalwitch:block/ritual/circle/active_" + part, f * 90));
                    }
                    assertEquals(expected, applied, FACINGS[f] + " part " + part + " active " + active);
                }
            }
        }
    }

    @Test
    void theCompiledModelsAreAuthoredWithinOneCell() {
        assertEquals(2 * CircleManifest.CELLS, CircleManifest.models().size());
        for (String model : CircleManifest.models()) {
            assertTrue(ResourceFiles.MAIN_ASSETS.resolve("models/" + model).toFile().isFile(), model);
            assertTrue(TemplateChecks.checkAuthored("herbalwitch:" + model.replace(".json", "")) > 0, model);
        }
        assertFalse(ResourceFiles.MAIN_ASSETS.resolve("models/block/ritual/circle/assembly.json").toFile().exists(), "the circle has no assembly");
    }

    @Test
    void theItemIsAFlatSprite() {
        ModelResolver.Resolved item = ModelResolver.resolve("herbalwitch:item/" + CircleManifest.ID);
        assertEquals("minecraft:item/generated", item.root());
        assertEquals("herbalwitch:item/" + CircleManifest.ID, item.texture("#layer0"));
        assertTrue(ResourceFiles.MAIN_ASSETS.resolve("textures/item/" + CircleManifest.ID + ".png").toFile().isFile());
    }
}
