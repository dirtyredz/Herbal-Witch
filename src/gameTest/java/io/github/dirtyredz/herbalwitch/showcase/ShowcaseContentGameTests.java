package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.brewing.product.registry.PreparationBlocks;
import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.behavior.lighting.DecorLightBlock;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSamples;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryCapability;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
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
        var op = standing(helper, "op_full", GameType.CREATIVE);
        // the second GameTest batch reuses the first batch's plots: dropped items left there are entities the preflight refuses
        helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(
                helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(FULL_X, 10, FULL_Z))).inflate(1))
                .forEach(net.minecraft.world.entity.Entity::discard);
        if (run(helper, as(op, 2), "herbalwitch showcase") == 0) {
            var why = io.github.dirtyredz.herbalwitch.showcase.construction.ShowcasePreflight.check(ShowcasePlan.of(ShowcaseRegistry.sections()),
                    helper.getLevel(), op, helper.absolutePos(io.github.dirtyredz.herbalwitch.fixture.ShowcaseFixtures.ORIGIN));
            check(false, "showcase failed: " + why.map(c -> c.getString()).orElse("preflight passed"));
        }
        check(DiscoveryCapability.of(op).map(c -> c.state().size()).orElse(0) == 0, "building the showcase granted discoveries");
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
                    if (state.getBlock() instanceof DecorLightBlock) {
                        check(state.getValue(DecorLightBlock.LIT), "unlit light at " + pos);
                    }
                    if (state.getBlock() instanceof DecorAssemblyBlock) {
                        check(state.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "unsupported furniture at " + pos);
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
            // every cell of each sample: the door also shows its three alternate states
            int samples = entry.spec().behavior() == FurnitureBehavior.DOOR ? 1 + DecorShowcaseSamples.DOOR_STATES.size() : 1;
            int cells = entry.spec().mount().cells().size() * samples;
            check(blocks.getOrDefault(entry.block().get(), 0) == cells, entry.block().getId() + " shown in "
                    + blocks.getOrDefault(entry.block().get(), 0) + " cells, expected " + cells);
        }
        for (HerbSpec herb : HerbCatalog.ALL) {
            for (int age = 0; age <= HerbSpec.MAX_AGE; age++) {
                check(herbAges.contains(herb.id() + "@" + age), herb.id() + " age " + age + " missing");
            }
        }
        check(blocks.getOrDefault(BotanyBlocks.herbBed(), 0) >= 56, "herb beds " + blocks.get(BotanyBlocks.herbBed()));
        check(barrels == HerbCatalog.ALL.size() + 4, "sample barrels " + barrels); // one per herb, powders, products, grimoire and altar supplies
        for (Block piece : GrimoireBlocks.all()) {
            check(blocks.getOrDefault(piece, 0) == 1, piece + " shown " + blocks.getOrDefault(piece, 0) + " times");
        }
        Block altar = io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks.ALTAR.get();
        check(blocks.getOrDefault(altar, 0) == 4, "the altar shown in " + blocks.getOrDefault(altar, 0) + " cells, expected 4");
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
