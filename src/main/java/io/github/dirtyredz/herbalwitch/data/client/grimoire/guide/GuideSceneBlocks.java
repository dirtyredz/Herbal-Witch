package io.github.dirtyredz.herbalwitch.data.client.grimoire.guide;

import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The block rules shared by every illustrated scene (GuideME diagrams and Ponder structures): a station
 * expands into its layout's cells with their {@code part} values, and every block id and property is checked
 * against the live registries during datagen. Geometry and validation only; no markup and no file format.
 */
public final class GuideSceneBlocks {
    /**
     * One validated block of a scene.
     *
     * @param pos        position in the scene
     * @param id         registered block id
     * @param properties explicit blockstate properties, sorted by name
     * @param grounded   whether this is a bottom-row station cell (it needs a support below)
     */
    public record Placement(BlockPos pos, String id, Map<String, String> properties, boolean grounded) {
        public Placement {
            properties = java.util.Collections.unmodifiableMap(new TreeMap<>(properties));
        }

        /** The block state this placement names: the block's default state with its explicit properties. */
        public BlockState state() {
            return GuideSceneBlocks.state(id, properties);
        }
    }

    private GuideSceneBlocks() {
    }

    /**
     * Every cell of a station whose origin (part 0) is at {@code origin}, facing {@code facing}, in part order.
     * Offsets come from the station's own layout; {@code extra} adds properties such as {@code lit}.
     */
    public static List<Placement> station(StationSpec spec, BlockPos origin, Direction facing, Map<String, String> extra) {
        var layout = spec.layout();
        if (layout == null) throw new IllegalArgumentException("Not a multiblock station: " + spec.path());
        List<Placement> cells = new ArrayList<>();
        for (int i = 0; i < layout.size(); i++) {
            Map<String, String> properties = new TreeMap<>(extra);
            properties.put("facing", facing.getSerializedName());
            properties.put(layout.part().getName(), Integer.toString(i));
            cells.add(placement(layout.cellPos(origin, facing, i), "herbalwitch:" + spec.path(), properties, layout.grounded(i)));
        }
        return cells;
    }

    /** One validated block. */
    public static Placement block(String id, BlockPos pos, Map<String, String> properties) {
        return placement(pos, id, properties, false);
    }

    private static Placement placement(BlockPos pos, String id, Map<String, String> properties, boolean grounded) {
        var placement = new Placement(pos, id, properties, grounded);
        placement.state();
        return placement;
    }

    /** Resolves a registered block's state, failing datagen on an unknown block, property or value. */
    public static BlockState state(String id, Map<String, String> properties) {
        var key = new ResourceLocation(id);
        if (!ForgeRegistries.BLOCKS.containsKey(key)) throw new IllegalStateException("Unknown guide block: " + id);
        BlockState state = ForgeRegistries.BLOCKS.getValue(key).defaultBlockState();
        for (var entry : new TreeMap<>(properties).entrySet()) {
            state = with(state, entry.getKey(), entry.getValue(), id);
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState with(BlockState state, String name, String value, String id) {
        @SuppressWarnings("unchecked")
        Property<T> property = (Property<T>) state.getBlock().getStateDefinition().getProperty(name);
        if (property == null) throw new IllegalStateException("Invalid guide state: " + id + " " + name + "=" + value);
        T parsed = property.getValue(value)
                .orElseThrow(() -> new IllegalStateException("Invalid guide state: " + id + " " + name + "=" + value));
        return state.setValue(property, parsed);
    }
}
