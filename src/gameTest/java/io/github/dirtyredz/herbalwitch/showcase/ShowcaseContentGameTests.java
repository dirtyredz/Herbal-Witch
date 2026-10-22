package io.github.dirtyredz.herbalwitch.showcase;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.botany.catalog.HerbSpec;
import io.github.dirtyredz.herbalwitch.apothecary.botany.crop.HerbCropBlock;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyBlocks;
import io.github.dirtyredz.herbalwitch.apothecary.botany.registry.BotanyItems;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationBlocks;
import io.github.dirtyredz.herbalwitch.decor.structure.DecorAssemblyBlock;
import io.github.dirtyredz.herbalwitch.decor.catalog.FurnitureBehavior;
import io.github.dirtyredz.herbalwitch.decor.behavior.DecorLightBlock;
import io.github.dirtyredz.herbalwitch.decor.showcase.DecorShowcaseSamples;
import io.github.dirtyredz.herbalwitch.decor.registry.DecorBlocks;
import io.github.dirtyredz.herbalwitch.grimoire.knowledge.discovery.DiscoveryCapability;
import io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireBlocks;
import io.github.dirtyredz.herbalwitch.showcase.command.ShowcaseCommand;
import io.github.dirtyredz.herbalwitch.showcase.construction.ShowcasePreflight;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseGroup;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
 * The two showcases as built by the real command: the furniture showcase holds every registered
 * furniture block once (lamps lit, shelves on their walls) and nothing else; the witchcraft showcase
 * holds every herb at every age, the garden examples, the stations, preparations, grimoire, altar,
 * circle, ward and charm samples, sample barrels and signs, and no furniture. The bare command builds
 * both, one separation apart.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ShowcaseContentGameTests {
    private ShowcaseContentGameTests() {
    }

    /** What a built box holds. */
    private static final class Scan {
        final Map<Block, Integer> blocks = new HashMap<>();
        final Set<String> herbAges = new HashSet<>();
        int signs;
        int barrels;
    }

    @GameTest(template = FULL, timeoutTicks = 400, batch = "herbalwitch_showcase_the_furniture_showcase_holds_every_decor_piece")
    public static void theFurnitureShowcaseHoldsEveryDecorPiece(GameTestHelper helper) throws Exception {
        ServerPlayer op = prepare(helper, "op_furniture");
        runChecked(helper, op, "herbalwitch showcase furniture", ShowcaseGroup.FURNITURE, ORIGIN);
        ShowcasePlan plan = plan(ShowcaseGroup.FURNITURE);
        Scan scan = scan(helper, ORIGIN, plan);
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            // every cell of each sample: the door also shows its three alternate states
            int samples = entry.spec().behavior() == FurnitureBehavior.DOOR ? 1 + DecorShowcaseSamples.DOOR_STATES.size() : 1;
            int cells = entry.spec().mount().cells().size() * samples;
            check(scan.blocks.getOrDefault(entry.block().get(), 0) == cells, entry.block().getId() + " shown in "
                    + scan.blocks.getOrDefault(entry.block().get(), 0) + " cells, expected " + cells);
        }
        check(scan.signs >= DecorBlocks.entries().size() + 1, "signs " + scan.signs);
        check(scan.herbAges.isEmpty() && scan.blocks.getOrDefault(BotanyBlocks.herbBed(), 0) == 0, "the furniture showcase holds herbs");
        for (Block station : StationBlocks.all()) {
            check(scan.blocks.getOrDefault(station, 0) == 0, station + " stands in the furniture showcase");
        }
        helper.succeed();
    }

    @GameTest(template = FULL, timeoutTicks = 400, batch = "herbalwitch_showcase_the_witchcraft_showcase_holds_everything_but_furniture")
    public static void theWitchcraftShowcaseHoldsEverythingButFurniture(GameTestHelper helper) throws Exception {
        ServerPlayer op = prepare(helper, "op_witchcraft");
        runChecked(helper, op, "herbalwitch showcase witchcraft", ShowcaseGroup.WITCHCRAFT, ORIGIN);
        ShowcasePlan plan = plan(ShowcaseGroup.WITCHCRAFT);
        Scan scan = scan(helper, ORIGIN, plan);
        for (DecorBlocks.Entry entry : DecorBlocks.entries()) {
            check(scan.blocks.getOrDefault(entry.block().get(), 0) == 0, entry.block().getId() + " stands in the witchcraft showcase");
        }
        for (HerbSpec herb : HerbCatalog.ALL) {
            for (int age = 0; age <= HerbSpec.MAX_AGE; age++) {
                check(scan.herbAges.contains(herb.id() + "@" + age), herb.id() + " age " + age + " missing");
            }
        }
        check(scan.blocks.getOrDefault(BotanyBlocks.herbBed(), 0) >= 56, "herb beds " + scan.blocks.get(BotanyBlocks.herbBed()));
        // one per herb, powders, products, grimoire and altar supplies; Milestone 7: three preparation barrels, circle supplies, charms
        check(scan.barrels == HerbCatalog.ALL.size() + 4 + 5, "sample barrels " + scan.barrels);
        for (Block piece : GrimoireBlocks.all()) {
            check(scan.blocks.getOrDefault(piece, 0) == 1, piece + " shown " + scan.blocks.getOrDefault(piece, 0) + " times");
        }
        Block altar = io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks.ALTAR.get();
        // the altar section's altar and the one inside the ritual circle
        check(scan.blocks.getOrDefault(altar, 0) == 8, "the altar shown in " + scan.blocks.getOrDefault(altar, 0) + " cells, expected 8");
        Block circle = io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks.CIRCLE.get();
        check(scan.blocks.getOrDefault(circle, 0) == io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog.LAYOUT.size(),
                "the circle shown in " + scan.blocks.getOrDefault(circle, 0) + " cells");
        Block ward = io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration.STONE.get();
        check(scan.blocks.getOrDefault(ward, 0) >= 1, "no ward stone");
        for (Block station : StationBlocks.all()) {
            check(scan.blocks.getOrDefault(station, 0) >= 1, station + " missing from the showcase");
        }
        for (HerbSpec herb : HerbCatalog.ALL) {
            check(scan.blocks.getOrDefault(PreparationBlocks.bundle(herb), 0) == 1, herb.id() + " bundle not hung once");
        }
        check(scan.blocks.getOrDefault(PreparationBlocks.OIL_LANTERN.get(), 0) == 1, "no oil lantern");
        check(scan.signs >= HerbCatalog.ALL.size() + ShowcaseRegistry.sections(ShowcaseGroup.WITCHCRAFT).size(), "signs " + scan.signs);
        check(BotanyItems.all().size() > 0, "botany items");
        helper.succeed();
    }

    /** The bare command: both showcases, the second one {@link ShowcaseCommand#SEPARATION} blocks behind the first, open ground between. */
    @GameTest(template = FULL, timeoutTicks = 600, batch = "herbalwitch_showcase_the_bare_command_builds_both_showcases_apart")
    public static void theBareCommandBuildsBothShowcasesApart(GameTestHelper helper) throws Exception {
        ServerPlayer op = prepare(helper, "op_both");
        ShowcasePlan furniture = plan(ShowcaseGroup.FURNITURE);
        ShowcasePlan witchcraft = plan(ShowcaseGroup.WITCHCRAFT);
        BlockPos second = ORIGIN.offset(0, 0, furniture.depth() + ShowcaseCommand.SEPARATION);
        if (run(helper, as(op, 2), "herbalwitch showcase") == 0) {
            var why = ShowcasePreflight.check(furniture, helper.getLevel(), op, helper.absolutePos(ORIGIN));
            check(false, "bare showcase failed: " + why.map(c -> c.getString()).orElse("furniture preflight passed"));
        }
        Scan first = scan(helper, ORIGIN, furniture);
        Scan other = scan(helper, second, witchcraft);
        Block armchair = DecorBlocks.entries().get(0).block().get();
        check(first.blocks.getOrDefault(armchair, 0) > 0, "no furniture in the first showcase");
        check(other.blocks.getOrDefault(armchair, 0) == 0, "furniture in the witchcraft showcase");
        check(other.blocks.getOrDefault(BotanyBlocks.herbBed(), 0) >= 56 && first.blocks.getOrDefault(BotanyBlocks.herbBed(), 0) == 0,
                "the herbs are not in the witchcraft showcase alone");
        // the separation stays untouched stone floor: no block above it
        for (int x = 0; x < furniture.width(); x++) {
            for (int z = furniture.depth(); z < furniture.depth() + ShowcaseCommand.SEPARATION; z++) {
                for (int y = 1; y < ShowcasePlan.HEIGHT; y++) {
                    check(helper.getBlockState(ORIGIN.offset(x, y, z)).isAir(), "the ground between the showcases was built on at " + x + "," + y + "," + z);
                }
            }
        }
        check(helper.getBlockState(ORIGIN.offset(0, 0, furniture.depth() + 1)).is(Blocks.STONE), "the ground between the showcases was relaid");
        helper.succeed();
    }

    private static ShowcasePlan plan(ShowcaseGroup group) throws Exception {
        return ShowcasePlan.of(ShowcaseRegistry.sections(group));
    }

    /** A flat floor, an operator at the entrance and no stray items from a neighbouring batch. */
    private static ServerPlayer prepare(GameTestHelper helper, String name) {
        stoneFloor(helper, FULL_X, FULL_Z);
        ServerPlayer op = standing(helper, name, GameType.CREATIVE);
        // the second GameTest batch reuses the first batch's plots: dropped items left there are entities the preflight refuses
        helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(
                helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(FULL_X, 10, FULL_Z))).inflate(1))
                .forEach(Entity::discard);
        // a plot this large collects wandering animals, which the preflight refuses as entities
        helper.getLevel().getEntitiesOfClass(Mob.class, new AABB(
                helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(FULL_X, 10, FULL_Z))).inflate(1))
                .forEach(Entity::discard);
        return op;
    }

    private static void runChecked(GameTestHelper helper, ServerPlayer op, String command, ShowcaseGroup group, BlockPos origin) throws Exception {
        if (run(helper, as(op, 2), command) == 0) {
            var why = ShowcasePreflight.check(plan(group), helper.getLevel(), op, helper.absolutePos(origin));
            check(false, command + " failed: " + why.map(c -> c.getString()).orElse("preflight passed"));
        }
        check(DiscoveryCapability.of(op).map(c -> c.state().size()).orElse(0) == 0, "building the showcase granted discoveries");
    }

    private static Scan scan(GameTestHelper helper, BlockPos origin, ShowcasePlan plan) {
        Scan scan = new Scan();
        for (int x = 0; x < plan.width(); x++) {
            for (int z = 0; z < plan.depth(); z++) {
                for (int y = 1; y < ShowcasePlan.HEIGHT; y++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = helper.getBlockState(pos);
                    scan.blocks.merge(state.getBlock(), 1, Integer::sum);
                    if (state.getBlock() instanceof DecorLightBlock) {
                        check(state.getValue(DecorLightBlock.LIT), "unlit light at " + pos);
                    }
                    if (state.getBlock() instanceof DecorAssemblyBlock) {
                        check(state.canSurvive(helper.getLevel(), helper.absolutePos(pos)), "unsupported furniture at " + pos);
                    }
                    if (state.getBlock() instanceof HerbCropBlock crop) {
                        scan.herbAges.add(crop.herb().id() + "@" + state.getValue(HerbCropBlock.AGE));
                    }
                    BlockEntity entity = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
                    if (entity instanceof SignBlockEntity sign) {
                        check(!sign.getFrontText().getMessage(0, false).getString().isBlank(), "blank sign at " + pos);
                        scan.signs++;
                    } else if (entity instanceof Container container && state.is(Blocks.BARREL)) {
                        check(!container.isEmpty(), "empty sample barrel at " + pos);
                        scan.barrels++;
                    }
                }
            }
        }
        return scan;
    }
}
