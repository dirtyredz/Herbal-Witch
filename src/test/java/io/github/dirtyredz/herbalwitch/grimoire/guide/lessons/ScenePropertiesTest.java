package io.github.dirtyredz.herbalwitch.grimoire.guide.lessons;

import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The lessons restate block property names as plain strings (they may not import gameplay types); this ties each
 * one to the real block's declaration so a rename fails here instead of silently breaking a scene. Block classes
 * need a game bootstrap that plain JUnit does not have, so each declaration is read from source and resolved to
 * its property name; the scenes themselves also fail loudly on a name a block does not have.
 */
class ScenePropertiesTest {
    private static final Path STATIONS = Path.of("src/main/java/io/github/dirtyredz/herbalwitch/apothecary/stations");

    @Test
    void namesMatchTheRealBlockProperties() throws IOException {
        assertEquals(BlockStateProperties.AGE_3.getName(), SceneProperties.AGE);
        assertDeclared("src/main/java/io/github/dirtyredz/herbalwitch/apothecary/botany/crop/HerbCropBlock.java",
                "AGE", "BlockStateProperties.AGE_3");

        assertDeclared(STATIONS.resolve("everyday/drying_rack/DryingRackBlock.java").toString(),
                "ACTIVE", "BooleanProperty.create(\"" + SceneProperties.ACTIVE + "\")");

        assertEquals(BlockStateProperties.LIT.getName(), SceneProperties.LIT);
        assertDeclared(STATIONS.resolve("everyday/hearth/HearthBlock.java").toString(), "LIT", "BlockStateProperties.LIT");
        assertDeclared(STATIONS.resolve("everyday/stove/StoveBlock.java").toString(), "LIT", "HearthBlock.LIT");

        assertEquals(BlockStateProperties.HORIZONTAL_FACING.getName(), SceneProperties.FACING);
        assertDeclared(STATIONS.resolve("block/StationBlock.java").toString(), "FACING", "HorizontalDirectionalBlock.FACING");

        assertEquals(StationCatalog.DRYING_RACK.layout().part().getName(), SceneProperties.PART);
        assertEquals(StationCatalog.STOVE.layout().part().getName(), SceneProperties.PART);
    }

    private static void assertDeclared(String file, String field, String expression) throws IOException {
        var declaration = Pattern.compile("public static final \\w+ " + field + " = ([^;]+);").matcher(Files.readString(Path.of(file)));
        assertTrue(declaration.find(), file + " no longer declares " + field);
        assertEquals(expression, declaration.group(1).trim(), file + " " + field + " changed; update SceneProperties");
    }
}
