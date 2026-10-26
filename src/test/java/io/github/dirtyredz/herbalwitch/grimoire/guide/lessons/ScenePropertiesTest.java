package io.github.dirtyredz.herbalwitch.grimoire.guide.lessons;

import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.block.StationBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.drying_rack.DryingRackBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.hearth.HearthBlock;
import io.github.dirtyredz.herbalwitch.apothecary.stations.everyday.stove.StoveBlock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The lessons restate block property names as plain strings (they may not import gameplay types); this ties each
 * one to the real block's property so a rename fails here instead of silently breaking a scene.
 */
class ScenePropertiesTest {
    @Test
    void namesMatchTheRealBlockProperties() {
        assertEquals(HerbCropBlock.AGE.getName(), SceneProperties.AGE);
        assertEquals(DryingRackBlock.ACTIVE.getName(), SceneProperties.ACTIVE);
        assertEquals(HearthBlock.LIT.getName(), SceneProperties.LIT);
        assertEquals(StoveBlock.LIT.getName(), SceneProperties.LIT);
        assertEquals(StationBlock.FACING.getName(), SceneProperties.FACING);
        assertEquals(StationCatalog.DRYING_RACK.layout().part().getName(), SceneProperties.PART);
        assertEquals(StationCatalog.STOVE.layout().part().getName(), SceneProperties.PART);
    }
}
