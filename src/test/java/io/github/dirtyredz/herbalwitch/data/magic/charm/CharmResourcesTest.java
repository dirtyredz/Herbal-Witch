package io.github.dirtyredz.herbalwitch.data.magic.charm;

import io.github.dirtyredz.herbalwitch.data.fixture.ModelResolver;
import io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles;
import io.github.dirtyredz.herbalwitch.data.fixture.TextureDensity;
import org.junit.jupiter.api.Test;

import static io.github.dirtyredz.herbalwitch.data.fixture.ResourceFiles.generated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The protective charms' generated data and art: a flat sprite item each, and the Curios charm slot tag naming all three. */
class CharmResourcesTest {
    @Test
    void everyCharmIsAFlatSpriteAtTheSetDensity() {
        for (String id : CharmManifest.CHARMS.keySet()) {
            String path = CharmManifest.item(id);
            ModelResolver.Resolved item = ModelResolver.resolve("herbalwitch:item/" + path);
            assertEquals("minecraft:item/generated", item.root(), path);
            assertEquals("herbalwitch:item/" + path, item.texture("#layer0"), path);
            TextureDensity.assertTextureSize(ResourceFiles.MAIN_ASSETS.resolve("textures/item/" + path + ".png"), path);
        }
    }

    @Test
    void theCuriosCharmTagNamesEveryCharmInCatalogueOrder() {
        StringBuilder expected = new StringBuilder("[");
        CharmManifest.CHARMS.keySet().forEach(id -> expected.append(expected.length() > 1 ? "," : "").append("\"herbalwitch:")
                .append(CharmManifest.item(id)).append('"'));
        assertEquals(expected + "]", generated(CharmManifest.CURIOS_TAG).getAsJsonArray("values").toString());
        assertTrue(generated(CharmManifest.CURIOS_TAG).keySet().contains("values"));
    }
}
