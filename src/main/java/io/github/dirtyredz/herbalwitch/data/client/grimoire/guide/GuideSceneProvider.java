package io.github.dirtyredz.herbalwitch.data.client.grimoire.guide;

import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/** Produces inert native scene markup from the same layouts used to place the actual stations. */
public final class GuideSceneProvider {
    /**
     * The one place scene size and centring are decided (pinned GuideME 20.1.15 SceneTagCompiler attributes).
     * GuideME already frames the camera on the block bounding box (rotation centre = world centre,
     * centerScene()), so zoom only scales the content-fitted box; fullWidth gives the viewport the whole page
     * width so the content sits centred horizontally instead of left-aligned. padding is left at its default:
     * the pinned getInt reader rejects the expression form the guide tests require.
     */
    public static final String SCENE_ATTRIBUTES = "interactive={true} zoom={3} fullWidth={true}";

    private GuideSceneProvider() { }

    public static String dryingRack() {
        return scene(station(StationCatalog.DRYING_RACK, "active", "true"));
    }

    public static String heat() {
        return scene(station(StationCatalog.STOVE, "lit", "true")
                + block("herbalwitch:" + StationCatalog.KETTLE.path(), 0, 2, 0, Map.of("facing", "north")));
    }

    private static String scene(String content) {
        return "<GameScene " + SCENE_ATTRIBUTES + ">\n" + content + "</GameScene>";
    }

    private static String station(StationSpec spec, String property, String value) {
        StringBuilder result = new StringBuilder();
        var cells = spec.layout().cells();
        for (int i = 0; i < cells.size(); i++) {
            var cell = cells.get(i);
            if (cell.up() == 0) result.append(block("minecraft:stone", -cell.right(), 0, cell.depth(), Map.of()));
            result.append(block("herbalwitch:" + spec.path(), -cell.right(), cell.up() + 1, cell.depth(),
                    Map.of("facing", "north", spec.layout().part().getName(), Integer.toString(i), property, value)));
        }
        return result.toString();
    }

    private static String block(String id, int x, int y, int z, Map<String, String> properties) {
        var key = new ResourceLocation(id);
        if (!ForgeRegistries.BLOCKS.containsKey(key)) throw new IllegalStateException("Unknown guide block: " + id);
        var block = ForgeRegistries.BLOCKS.getValue(key);
        // Unlike getFloat/getBoolean, the pinned getInt reader requires a string value.
        StringBuilder out = new StringBuilder("  <Block id=\"").append(id).append("\" x=\"").append(x)
                .append("\" y=\"").append(y).append("\" z=\"").append(z).append('"');
        properties.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            var property = block.getStateDefinition().getProperty(entry.getKey());
            if (property == null || property.getValue(entry.getValue()).isEmpty()) {
                throw new IllegalStateException("Invalid guide state: " + id + " " + entry);
            }
            out.append(" p:").append(entry.getKey()).append("=\"").append(entry.getValue()).append('"');
        });
        return out.append(" />\n").toString();
    }
}
