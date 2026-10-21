package io.github.dirtyredz.herbalwitch.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.garden.GardenBedConnections;
import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.bed;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.placeOnFloor;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Herb beds join: a bed connects on each side where the same bed block stands at the same height,
 * whether placed by a player or set directly; removing a neighbour restores the side; diagonal,
 * stacked and other blocks never join; joining keeps moisture and enrichment; rotation and
 * mirroring turn the connections; a random tick repairs a bed whose connections are stale.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GardenBedConnectionGameTests {
    private GardenBedConnectionGameTests() {
    }

    /** The joined sides of the bed at {@code pos}, as a string like "N.E." for quick comparison. */
    private static String sides(GameTestHelper helper, BlockPos pos) {
        BlockState s = helper.getBlockState(pos);
        StringBuilder out = new StringBuilder();
        for (Direction d : new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            if (s.getValue(GardenBedConnections.property(d))) {
                out.append(d.getName().charAt(0));
            }
        }
        return out.toString();
    }

    @GameTest(template = TEMPLATE)
    public static void playerPlacedBedsJoinInRowsSquaresAndRings(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "gardener", GameType.SURVIVAL, Direction.NORTH);
        // a ring of eight around an empty centre (1..3, 1..3)
        for (int x = 1; x <= 3; x++) {
            for (int z = 1; z <= 3; z++) {
                if (x != 2 || z != 2) {
                    check(placeOnFloor(helper, player, BotanyItems.HERB_BED.get(), new BlockPos(x, 1, z)).consumesAction(), "bed not placed");
                }
            }
        }
        check(sides(helper, new BlockPos(1, 1, 1)).equals("es"), "corner " + sides(helper, new BlockPos(1, 1, 1)));
        check(sides(helper, new BlockPos(2, 1, 1)).equals("ew"), "edge " + sides(helper, new BlockPos(2, 1, 1)));
        check(sides(helper, new BlockPos(3, 1, 3)).equals("nw"), "far corner " + sides(helper, new BlockPos(3, 1, 3)));
        check(sides(helper, new BlockPos(1, 1, 2)).equals("ns"), "west middle " + sides(helper, new BlockPos(1, 1, 2)));
        // removing a neighbour restores the border; putting it back joins again
        helper.setBlock(new BlockPos(2, 1, 1), Blocks.AIR);
        check(sides(helper, new BlockPos(1, 1, 1)).equals("s"), "border not restored " + sides(helper, new BlockPos(1, 1, 1)));
        bed(helper, new BlockPos(2, 1, 1), false);
        check(sides(helper, new BlockPos(2, 1, 1)).equals("ew"), "a directly set bed did not join " + sides(helper, new BlockPos(2, 1, 1)));
        check(sides(helper, new BlockPos(1, 1, 1)).equals("es"), "neighbour did not rejoin");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void onlyTheSameBedBesideItJoinsAndStateIsKept(GameTestHelper helper) {
        floor(helper);
        BlockPos centre = new BlockPos(4, 1, 4);
        bed(helper, centre, true);
        helper.setBlock(centre.north(), Blocks.FARMLAND);
        helper.setBlock(centre.east().north(), BotanyBlocks.herbBed().defaultBlockState()); // diagonal
        helper.setBlock(centre.above(), Blocks.AIR);
        bed(helper, centre.below().east(), false); // not at the same height: inside the floor row
        check(sides(helper, centre).isEmpty(), "joined a non-bed, a diagonal or a lower bed: " + sides(helper, centre));
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(centre);
        level.setBlock(abs, level.getBlockState(abs).setValue(HerbBedBlock.ENRICHED, true), 3);
        bed(helper, centre.west(), false);
        BlockState joined = helper.getBlockState(centre);
        check(joined.getValue(GardenBedConnections.WEST), "did not join the west bed");
        check(joined.getValue(HerbBedBlock.ENRICHED) && HerbBedBlock.isMoist(joined), "joining lost moisture or enrichment");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void rotationMirroringAndStaleRepair(GameTestHelper helper) {
        floor(helper);
        BlockState north = BotanyBlocks.herbBed().defaultBlockState().setValue(GardenBedConnections.NORTH, true);
        check(north.rotate(Rotation.CLOCKWISE_90).getValue(GardenBedConnections.EAST), "rotation did not turn the joined side");
        check(north.mirror(Mirror.LEFT_RIGHT).getValue(GardenBedConnections.SOUTH), "mirroring did not flip the joined side");
        // a stale bed (as saved before beds joined) is repaired by its random tick, moisture unchanged
        BlockPos a = new BlockPos(2, 1, 2);
        BlockPos b = a.east();
        bed(helper, a, false);
        bed(helper, b, false);
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(a);
        level.setBlock(abs, BotanyBlocks.herbBed().defaultBlockState(), 2 | 16); // no neighbour updates
        check(sides(helper, a).isEmpty(), "fixture did not make the bed stale");
        level.getBlockState(abs).randomTick(level, abs, level.random);
        check(sides(helper, a).equals("e"), "random tick did not repair the connection: " + sides(helper, a));
        helper.succeed();
    }
}
