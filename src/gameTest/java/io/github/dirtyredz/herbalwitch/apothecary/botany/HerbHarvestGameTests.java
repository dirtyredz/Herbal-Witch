package io.github.dirtyredz.herbalwitch.apothecary.botany;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.BotanyFixtures.plantOnFarmland;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.click;
import static io.github.dirtyredz.herbalwitch.fixture.PlayerFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.clearDrops;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.dropped;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Harvest by hand on every herb (two fresh plus one seed, crops back to a sprout, bushes to age 1),
 * no payout from immature plants, breaking drops, and one payout per mature plant.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HerbHarvestGameTests {
    private HerbHarvestGameTests() {
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 200)
    public static void everyMatureHerbHarvestsAndRegrowsFromItsForm(GameTestHelper helper) {
        floor(helper);
        ServerPlayer player = player(helper, "harvester", GameType.SURVIVAL, Direction.NORTH);
        for (HerbSpec herb : HerbCatalog.ALL) {
            clearDrops(helper);
            BlockPos plant = plantOnFarmland(helper, herb, new BlockPos(4, 1, 4), HerbSpec.MAX_AGE);
            InteractionResult result = click(helper, player, plant);
            check(result.consumesAction(), herb.id() + " not harvested: " + result);
            int expectedAge = herb.form() == HerbSpec.Form.BUSH ? 1 : 0;
            check(helper.getBlockState(plant).getValue(HerbCropBlock.AGE) == expectedAge,
                    herb.id() + " regrows from age " + helper.getBlockState(plant).getValue(HerbCropBlock.AGE));
            int fresh = dropped(helper, BotanyItems.fresh(herb));
            check(fresh == 2 || fresh == 3, herb.id() + " gave " + fresh + " fresh");
            check(dropped(helper, BotanyItems.seeds(herb)) == 1, herb.id() + " gave " + dropped(helper, BotanyItems.seeds(herb)) + " seeds");
            // Using it again straight away finds an immature plant: no second payout.
            InteractionResult again = click(helper, player, plant);
            check(!again.consumesAction(), herb.id() + " paid out twice");
            check(dropped(helper, BotanyItems.fresh(herb)) == fresh, herb.id() + " second payout");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void breakingDropsOneSeedOrTheFullHarvest(GameTestHelper helper) {
        floor(helper);
        HerbSpec herb = HerbCatalog.NETTLE;
        BlockPos young = plantOnFarmland(helper, herb, new BlockPos(2, 1, 2), 1);
        BlockPos mature = plantOnFarmland(helper, herb, new BlockPos(6, 1, 6), HerbSpec.MAX_AGE);
        clearDrops(helper);
        helper.getLevel().destroyBlock(helper.absolutePos(young), true);
        check(dropped(helper, BotanyItems.seeds(herb)) == 1 && dropped(helper, BotanyItems.fresh(herb)) == 0,
                "immature break dropped " + dropped(helper, BotanyItems.seeds(herb)) + " seeds");
        clearDrops(helper);
        helper.getLevel().destroyBlock(helper.absolutePos(mature), true);
        check(dropped(helper, BotanyItems.seeds(herb)) == 1 && dropped(helper, BotanyItems.fresh(herb)) == 2,
                "mature break dropped " + dropped(helper, BotanyItems.fresh(herb)) + " fresh");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void adventurePlayersCannotHarvest(GameTestHelper helper) {
        floor(helper);
        ServerPlayer visitor = player(helper, "visitor", GameType.ADVENTURE, Direction.NORTH);
        BlockPos plant = plantOnFarmland(helper, HerbCatalog.SAGE, new BlockPos(4, 1, 4), HerbSpec.MAX_AGE);
        clearDrops(helper);
        click(helper, visitor, plant);
        check(helper.getBlockState(plant).getValue(HerbCropBlock.AGE) == HerbSpec.MAX_AGE, "adventure harvest changed the plant");
        check(dropped(helper, BotanyItems.fresh(HerbCatalog.SAGE)) == 0, "adventure harvest dropped herbs");
        helper.succeed();
    }
}
