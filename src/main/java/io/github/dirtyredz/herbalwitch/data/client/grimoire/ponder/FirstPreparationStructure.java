package io.github.dirtyredz.herbalwitch.data.client.grimoire.ponder;

import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbGrowth;
import io.github.dirtyredz.herbalwitch.apothecary.stations.catalog.StationCatalog;
import io.github.dirtyredz.herbalwitch.data.client.grimoire.guide.GuideSceneBlocks;
import io.github.dirtyredz.herbalwitch.data.client.grimoire.guide.GuideSceneBlocks.Placement;
import io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationLayout;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.dirtyredz.herbalwitch.grimoire.guide.lessons.FirstPreparationLayout.FACING;

/**
 * The first-preparation work area at its starting state: a neutral floor, mature lavender on farmland, the
 * complete drying rack (empty), a mortar, the complete stove (unlit) and the kettle on its origin burner.
 * Blocks only: no block-entity payloads (inventories, jobs, owners) and no entities.
 */
public final class FirstPreparationStructure {
    static final String FLOOR = "minecraft:smooth_stone";
    static final String SOIL = "minecraft:farmland";

    private FirstPreparationStructure() {
    }

    /** Every block of the structure, validated against the registries and the layout's support rules. */
    public static List<Placement> placements() {
        List<Placement> work = new ArrayList<>();
        work.add(GuideSceneBlocks.block("herbalwitch:" + HerbCatalog.LAVENDER.plantPath(), FirstPreparationLayout.LAVENDER,
                Map.of("age", Integer.toString(HerbSpec.MAX_AGE))));
        work.addAll(GuideSceneBlocks.station(StationCatalog.DRYING_RACK, FirstPreparationLayout.RACK_ORIGIN, FACING,
                Map.of("active", "false")));
        work.add(GuideSceneBlocks.block("herbalwitch:" + StationCatalog.MORTAR.path(), FirstPreparationLayout.MORTAR,
                Map.of("facing", FACING.getSerializedName())));
        work.addAll(GuideSceneBlocks.station(StationCatalog.STOVE, FirstPreparationLayout.STOVE_ORIGIN, FACING,
                Map.of("lit", "false")));
        work.add(GuideSceneBlocks.block("herbalwitch:" + StationCatalog.KETTLE.path(), FirstPreparationLayout.KETTLE,
                Map.of("facing", FACING.getSerializedName())));

        List<Placement> all = new ArrayList<>();
        var size = FirstPreparationLayout.SIZE;
        for (int x = 0; x < size.getX(); x++) {
            for (int z = 0; z < size.getZ(); z++) {
                BlockPos pos = new BlockPos(x, 0, z);
                all.add(pos.equals(FirstPreparationLayout.SOIL)
                        ? GuideSceneBlocks.block(SOIL, pos, Map.of("moisture", "7"))
                        : GuideSceneBlocks.block(FLOOR, pos, Map.of()));
            }
        }
        all.addAll(work);
        validate(all);
        return all;
    }

    private static void validate(List<Placement> all) {
        Map<BlockPos, Placement> byPos = new HashMap<>();
        for (Placement placement : all) {
            if (!FirstPreparationLayout.contains(placement.pos())) throw new IllegalStateException("Outside the scene: " + placement);
            if (byPos.put(placement.pos(), placement) != null) throw new IllegalStateException("Two blocks at " + placement.pos());
        }
        for (Placement placement : all) {
            if (placement.pos().getY() == 0) continue;
            Placement below = byPos.get(placement.pos().below());
            if (below == null) throw new IllegalStateException("Unsupported scene block: " + placement);
            if (placement.grounded() && below.pos().getY() != 0) throw new IllegalStateException("Station cell off the floor: " + placement);
        }
        if (!HerbGrowth.canStandOn(byPos.get(FirstPreparationLayout.SOIL).state())) {
            throw new IllegalStateException("Lavender cannot stand on " + byPos.get(FirstPreparationLayout.SOIL));
        }
        Placement burner = byPos.get(FirstPreparationLayout.KETTLE.below());
        if (burner == null || !burner.properties().get("part").equals("0")
                || !burner.id().equals("herbalwitch:" + StationCatalog.STOVE.path())) {
            throw new IllegalStateException("The kettle must stand on the stove's origin burner");
        }
    }
}
