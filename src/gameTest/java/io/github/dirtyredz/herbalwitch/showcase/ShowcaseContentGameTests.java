package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlocks;
import io.github.dirtyredz.herbalwitch.decor.block.TableLampBlock;
import io.github.dirtyredz.herbalwitch.decor.block.WallShelfBlock;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.stations.registry.StationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.FULL;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.FULL_X;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.FULL_Z;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.ORIGIN;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.as;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.run;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.standing;
import static io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.stoneFloor;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;

/**
 * The full showcase as built by the real command: every registered furniture block once (lamps lit,
 * shelves on their walls), every herb at every age, the garden examples, sample barrels and signs.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShowcaseContentGameTests {
    private ShowcaseContentGameTests() {
    }

    @GameTest(template = FULL, timeoutTicks = 400)
    public static void theFullShowcaseHoldsEveryRegisteredBlock(GameTestHelper helper) throws Exception {
        stoneFloor(helper, FULL_X, FULL_Z);
        check(run(helper, as(standing(helper, "op_full", GameType.CREATIVE), 2), "herbalwitch showcase") > 0, "showcase failed");
        ShowcasePlan plan = ShowcasePlan.of(ShowcaseRegistry.sections());
        Map<Block, Integer> blocks = new HashMap<>();
        Set<String> herbAges = new HashSet<>();
        int signs = 0;
        int barrels = 0;
        for (int x = 0; x < plan.width(); x++) {
            for (int z = 0; z < plan.depth(); z++) {
                for (int y = 1; y < ShowcasePlan.HEIGHT; y++) {
                    BlockPos pos = ORIGIN.offset(x, y, z);
                    BlockState state = helper.getBlockState(pos);
                    blocks.merge(state.getBlock(), 1, Integer::sum);
                    if (state.getBlock() instanceof TableLampBlock) {
                        check(state.getValue(TableLampBlock.LIT), "unlit lamp at " + pos);
                    }
                    if (state.getBlock() instanceof WallShelfBlock) {
                        check(state.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "unsupported shelf at " + pos);
                    }
                    if (state.getBlock() instanceof HerbCropBlock crop) {
                        herbAges.add(crop.herb().id() + "@" + state.getValue(HerbCropBlock.AGE));
                    }
                    BlockEntity entity = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
                    if (entity instanceof SignBlockEntity sign) {
                        check(!sign.getFrontText().getMessage(0, false).getString().isBlank(), "blank sign at " + pos);
                        signs++;
                    } else if (entity instanceof Container container && state.is(Blocks.BARREL)) {
                        check(!container.isEmpty(), "empty sample barrel at " + pos);
                        barrels++;
                    }
                }
            }
        }
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            check(blocks.getOrDefault(entry.block().get(), 0) == 1, entry.block().getId() + " shown "
                    + blocks.getOrDefault(entry.block().get(), 0) + " times");
        }
        for (HerbSpec herb : HerbCatalog.ALL) {
            for (int age = 0; age <= HerbSpec.MAX_AGE; age++) {
                check(herbAges.contains(herb.id() + "@" + age), herb.id() + " age " + age + " missing");
            }
        }
        check(blocks.getOrDefault(BotanyBlocks.herbBed(), 0) >= 56, "herb beds " + blocks.get(BotanyBlocks.herbBed()));
        check(barrels == HerbCatalog.ALL.size() + 2, "sample barrels " + barrels); // one per herb, powders, products
        for (Block station : StationBlocks.all()) {
            check(blocks.getOrDefault(station, 0) >= 1, station + " missing from the showcase");
        }
        for (HerbSpec herb : HerbCatalog.ALL) {
            check(blocks.getOrDefault(PreparationBlocks.bundle(herb), 0) == 1, herb.id() + " bundle not hung once");
        }
        check(blocks.getOrDefault(PreparationBlocks.OIL_LANTERN.get(), 0) == 1, "no oil lantern");
        check(signs >= DecorBlocks.entries().size() + HerbCatalog.ALL.size() + ShowcaseRegistry.sections().size(), "signs " + signs);
        check(BotanyItems.all().size() > 0, "botany items");
        helper.succeed();
    }
}
