package io.github.dirtyredz.herbalwitch.data.client.botany.garden;

import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.GardenBedConnections;
import io.github.dirtyredz.herbalwitch.apothecary.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;

/**
 * The herb bed's multipart state. The authored parts ({@code block/botany/garden/parts/}) are drawn
 * for the north side and the north-east corner and turned to the others: an exposed side gets the
 * two-rail wall, a joined side soil; a corner with both sides exposed gets the capped post, with one
 * side exposed that side's rail running through, with both joined soil (plus the neighbours' rails
 * carried round it when its inner-corner flag is set: the diagonal bed is missing). The soil core is always
 * there. Soil parts come dry ({@code moisture=0}) or moist (generated children rebinding the soil).
 */
public final class HerbBedBlockstates {
    static final String PARTS = "block/botany/garden/parts/";
    /** Sides clockwise from north; side i is turned by i * 90 degrees. */
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private HerbBedBlockstates() {
    }

    public static void register(BlockStateProvider states) {
        HerbBedModels models = new HerbBedModels(states);
        MultiPartBlockStateBuilder bed = states.getMultipartBuilder(BotanyBlocks.herbBed());
        soil(bed, models, "core", 0, null, null, null, false);
        for (int i = 0; i < 4; i++) {
            int turn = i * 90;
            BooleanProperty side = GardenBedConnections.property(SIDES[i]);
            BooleanProperty next = GardenBedConnections.property(SIDES[(i + 1) % 4]);
            // the side's wall, or soil where it joins a neighbour
            bed.part().modelFile(models.wood("rail")).rotationY(turn).addModel().condition(side, false).end();
            soil(bed, models, "edge", turn, side, null, null, false);
            // the corner between this side and the next one clockwise
            bed.part().modelFile(models.wood("post")).rotationY(turn).addModel().condition(side, false).condition(next, false).end();
            bed.part().modelFile(models.wood("rail_end")).rotationY(turn).addModel().condition(side, false).condition(next, true).end();
            bed.part().modelFile(models.wood("rail_start")).rotationY((turn + 90) % 360).addModel()
                    .condition(side, true).condition(next, false).end();
            // both sides joined: soil, with the corner closed by the wall of the two neighbours when the diagonal is empty
            BooleanProperty inner = GardenBedConnections.innerCorner(i);
            bed.part().modelFile(models.wood("inner")).rotationY(turn).addModel()
                    .condition(side, true).condition(next, true).condition(inner, true).end();
            soil(bed, models, "corner_inner", turn, side, next, inner, true);
            soil(bed, models, "corner", turn, side, next, inner, false);
        }
    }

    /** A soil part, dry or moist, shown where {@code a} and {@code b} (when given) are joined. */
    private static void soil(MultiPartBlockStateBuilder bed, HerbBedModels models, String name, int turn, BooleanProperty a,
                             BooleanProperty b, BooleanProperty inner, boolean innerValue) {
        for (boolean moist : new boolean[] {false, true}) {
            ModelFile model = moist ? models.moist(name) : models.dry(name);
            MultiPartBlockStateBuilder.PartBuilder part = bed.part().modelFile(model).rotationY(turn).addModel();
            if (moist) {
                part.condition(HerbBedBlock.MOISTURE, 1, 2, 3, 4, 5, 6, 7);
            } else {
                part.condition(HerbBedBlock.MOISTURE, 0);
            }
            if (a != null) {
                part.condition(a, true);
            }
            if (b != null) {
                part.condition(b, true);
            }
            if (inner != null) {
                part.condition(inner, innerValue);
            }
            part.end();
        }
    }
}
