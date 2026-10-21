package io.github.dirtyredz.herbalwitch.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.garden.GardenSoil;
import io.github.dirtyredz.herbalwitch.botany.garden.HerbBedBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.bed;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.placeOnFloor;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/** The herb bed: farmland-style moisture without drying to dirt or trampling. */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GardenBedGameTests {
    private GardenBedGameTests() {
    }

    @GameTest(template = TEMPLATE)
    public static void waterNearbyMoistensTheBedAndItDriesWithoutIt(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper, "bedder", GameType.SURVIVAL, Direction.NORTH);
        for (BlockPos wall : new BlockPos[] {new BlockPos(0, 1, 1), new BlockPos(2, 1, 1), new BlockPos(1, 1, 0), new BlockPos(1, 1, 2)}) {
            helper.setBlock(wall, Blocks.STONE);
        }
        helper.setBlock(new BlockPos(1, 1, 1), Blocks.WATER);
        check(placeOnFloor(helper, player, BotanyItems.HERB_BED.get(), new BlockPos(3, 1, 1)).consumesAction(), "bed not placed");
        check(helper.getBlockState(new BlockPos(3, 1, 1)).getValue(HerbBedBlock.MOISTURE) == GardenSoil.MAX_MOISTURE,
                "a bed placed by water starts moist");
        // Far from water (the template corner is more than four blocks away), random ticks dry it.
        BlockPos dry = new BlockPos(7, 1, 7);
        bed(helper, dry, true);
        if (!level.isRainingAt(helper.absolutePos(dry).above())) {
            for (int i = 0; i < GardenSoil.MAX_MOISTURE + 3; i++) {
                level.getBlockState(helper.absolutePos(dry)).randomTick(level, helper.absolutePos(dry), level.random);
            }
            check(helper.getBlockState(dry).getValue(HerbBedBlock.MOISTURE) == 0, "bed did not dry");
        }
        check(helper.getBlockState(dry).is(BotanyBlocks.herbBed()), "a dry bed must not turn into dirt");
        check(BotanyBlocks.herbBed().isFertile(BotanyBlocks.herbBed().defaultBlockState()
                .setValue(HerbBedBlock.MOISTURE, 3), level, helper.absolutePos(dry)), "moist beds feed growth");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void fallingOnABedDoesNotTrampleIt(GameTestHelper helper) {
        floor(helper);
        BlockPos pos = new BlockPos(4, 1, 4);
        bed(helper, pos, true);
        helper.setBlock(pos.above(), BotanyBlocks.plant(HerbCatalog.LAVENDER).getStateForAge(2));
        Pig pig = EntityType.PIG.create(helper.getLevel());
        BlockPos abs = helper.absolutePos(pos);
        pig.moveTo(abs.getX() + 0.5, abs.getY() + 1, abs.getZ() + 0.5);
        helper.getLevel().getBlockState(abs).getBlock().fallOn(helper.getLevel(), helper.getLevel().getBlockState(abs), abs, pig, 3.0F);
        check(helper.getBlockState(pos).is(BotanyBlocks.herbBed()), "bed trampled");
        check(helper.getBlockState(pos.above()).is(BotanyBlocks.plant(HerbCatalog.LAVENDER)), "plant lost");
        pig.discard();
        helper.succeed();
    }
}
