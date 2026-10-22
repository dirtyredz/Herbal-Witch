package io.github.dirtyredz.herbalwitch.data.preparations;

import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TextureDensity;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Milestone 7 preparations: 43 flat sprite items and the entity and weapon tags the effects read. */
class AdvancedResourcesTest {
    @Test
    void thereAreFortyThreeItemsEachWithItsOwnFlatSprite() {
        assertEquals(16, AdvancedManifest.ADVANCED.size());
        assertEquals(10, AdvancedManifest.FAMILIAR.size());
        assertEquals(17, AdvancedManifest.MIXTURES.size());
        Set<String> sprites = new HashSet<>();
        for (String path : AdvancedManifest.items()) {
            ModelResolver.Resolved item = ModelResolver.resolve("herbalwitch:item/preparations/" + path);
            assertEquals("minecraft:item/generated", item.root(), path);
            assertEquals("herbalwitch:item/preparations/" + path, item.texture("#layer0"), path);
            java.nio.file.Path png = ResourceFiles.MAIN_ASSETS.resolve("textures/item/preparations/" + path + ".png");
            TextureDensity.assertTextureSize(png, path);
            assertTrue(sprites.add(java.util.Arrays.toString(ResourceFiles.readPng(png).rgba())), path + " repeats another sprite");
        }
    }

    @Test
    void everyMixtureIsBottledIntoOneProductThatExists() {
        assertEquals(AdvancedManifest.MIXTURES.keySet().stream().map(m -> m.substring("mixtures/".length())).collect(java.util.stream.Collectors.toSet()),
                AdvancedManifest.BOTTLED.keySet());
        AdvancedManifest.BOTTLED.values().forEach(product -> assertTrue(AdvancedManifest.items().contains(product), product));
    }

    @Test
    void entityAndWeaponTags() {
        assertEquals("[\"minecraft:zombie\",\"minecraft:husk\",\"minecraft:drowned\",\"minecraft:skeleton\",\"minecraft:stray\","
                + "\"minecraft:wither_skeleton\",\"minecraft:phantom\"]",
                generated("data/herbalwitch/tags/entity_types/undead.json").getAsJsonArray("values").toString());
        assertEquals("[\"minecraft:spider\",\"minecraft:cave_spider\",\"minecraft:silverfish\",\"minecraft:endermite\"]",
                generated("data/herbalwitch/tags/entity_types/arthropods.json").getAsJsonArray("values").toString());
        assertEquals("[\"minecraft:ender_dragon\",\"minecraft:wither\",\"minecraft:warden\"]",
                generated("data/herbalwitch/tags/entity_types/hard_control_immune.json").getAsJsonArray("values").toString());
        assertEquals("[]", generated("data/herbalwitch/tags/entity_types/hard_control_allowed.json").getAsJsonArray("values").toString());
        assertEquals("[\"minecraft:villager\",\"minecraft:wandering_trader\"]",
                generated("data/herbalwitch/tags/entity_types/protected_entities.json").getAsJsonArray("values").toString());
        assertEquals("[\"#minecraft:swords\",\"#minecraft:axes\"]",
                generated("data/herbalwitch/tags/items/coatable_weapons.json").getAsJsonArray("values").toString());
    }
}
