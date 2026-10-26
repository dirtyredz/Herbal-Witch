package io.github.dirtyredz.herbalwitch.data.client.grimoire.guide;

import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.SceneProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Map;

/**
 * Produces inert native scene markup from the same layouts used to place the actual stations. Geometry and
 * registry validation come from {@link GuideSceneBlocks}, shared with the Ponder structures; this class owns
 * only the GuideME markup.
 */
public final class GuideSceneProvider {
    /**
     * The one place scene size and centring are decided (pinned GuideME 20.1.15 SceneTagCompiler attributes).
     * GuideME already frames the camera on the block bounding box (rotation centre = world centre,
     * centerScene()), so zoom only scales the content-fitted box; fullWidth gives the viewport the whole page
     * width so the content sits centred horizontally instead of left-aligned. padding is left at its default:
     * the pinned getInt reader rejects the expression form the guide tests require.
     */
    public static final String SCENE_ATTRIBUTES = "interactive={true} zoom={3} fullWidth={true}";
    /** Stations stand one block up, on supports in layer 0, facing the viewer's default side. */
    private static final BlockPos ORIGIN = new BlockPos(0, 1, 0);

    private GuideSceneProvider() { }

    public static String dryingRack() {
        return scene(station(StationCatalog.DRYING_RACK, SceneProperties.ACTIVE, "true"));
    }

    public static String heat() {
        return scene(station(StationCatalog.STOVE, SceneProperties.LIT, "true")
                + block(GuideSceneBlocks.block("herbalwitch:" + StationCatalog.KETTLE.path(), ORIGIN.above(), Map.of(SceneProperties.FACING, "north"))));
    }

    private static String scene(String content) {
        return "<GameScene " + SCENE_ATTRIBUTES + ">\n" + content + "</GameScene>";
    }

    private static String station(StationSpec spec, String property, String value) {
        StringBuilder result = new StringBuilder();
        for (var cell : GuideSceneBlocks.station(spec, ORIGIN, Direction.NORTH, Map.of(property, value))) {
            if (cell.grounded()) result.append(block(GuideSceneBlocks.block("minecraft:stone", cell.pos().below(), Map.of())));
            result.append(block(cell));
        }
        return result.toString();
    }

    private static String block(GuideSceneBlocks.Placement placement) {
        // Unlike getFloat/getBoolean, the pinned getInt reader requires a string value.
        var pos = placement.pos();
        StringBuilder out = new StringBuilder("  <Block id=\"").append(placement.id()).append("\" x=\"").append(pos.getX())
                .append("\" y=\"").append(pos.getY()).append("\" z=\"").append(pos.getZ()).append('"');
        placement.properties().forEach((key, value) -> out.append(" p:").append(key).append("=\"").append(value).append('"'));
        return out.append(" />\n").toString();
    }
}
