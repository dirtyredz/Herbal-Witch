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
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcasePlan;
import io.github.dirtyredz.herbalwitch.showcase.layout.ShowcaseRegistry;
import io.github.dirtyredz.herbalwitch.apothecary.stations.registry.StationBlocks;
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

    @GameTest(template = FULL, timeoutTicks = 400, batch = "herbalwitch_showcase_animal_spawn_failure")
    public static void failedAnimalSpawnRemovesOnlyThisInvocationsSamplesAndKeepsBlocks(GameTestHelper helper) throws Exception {
        stoneFloor(helper, FULL_X, FULL_Z);
        var op = standing(helper, "m8_showcase_failure", GameType.CREATIVE);
        var unrelated = helper.spawn(net.minecraft.world.entity.EntityType.COW, new BlockPos(0, 1, 40));
        var section = new io.github.dirtyredz.herbalwitch.companions.animals.showcase.AnimalShowcaseSection();
        var plan = ShowcasePlan.of(java.util.List.of(section));
        var origin = helper.absolutePos(ORIGIN);
        java.util.function.Consumer<net.minecraftforge.event.entity.EntityJoinLevelEvent> cancel = event -> {
            if (event.getLevel() == helper.getLevel() && event.getEntity().getType() == net.minecraft.world.entity.EntityType.WOLF)
                event.setCanceled(true);
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST, cancel);
        try {
            var result = io.github.dirtyredz.herbalwitch.showcase.construction.ShowcaseBuilder.build(plan, helper.getLevel(), op, origin);
            check(!result.complete() && result.entities() == 0 && result.placed() > 0, "partial build reported success");
            check(!unrelated.isRemoved() && helper.getLevel().getEntity(unrelated.getUUID()) == unrelated, "cleanup removed unrelated animal");
            var box = new net.minecraft.world.phys.AABB(origin, origin.offset(plan.width(), ShowcasePlan.HEIGHT, plan.depth()));
            check(helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, box).isEmpty(), "failed invocation left samples");
            check(helper.getLevel().getBlockState(origin).is(Blocks.SMOOTH_STONE), "partial build lost floor");
            helper.succeed();
        } finally { net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(cancel); unrelated.discard(); }
    }

    @GameTest(template = FULL, timeoutTicks = 400, batch = "herbalwitch_showcase_the_full_showcase_holds_every_registered_block")
    public static void theFullShowcaseHoldsEveryRegisteredBlock(GameTestHelper helper) throws Exception {
        stoneFloor(helper, FULL_X, FULL_Z);
        var op = standing(helper, "op_full", GameType.CREATIVE);
        // the second GameTest batch reuses the first batch's plots: dropped items left there are entities the preflight refuses
        helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(
                helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(FULL_X, 10, FULL_Z))).inflate(1))
                .forEach(net.minecraft.world.entity.Entity::discard);
        ShowcasePlan plan = ShowcasePlan.of(ShowcaseRegistry.sections());
        var why = io.github.dirtyredz.herbalwitch.showcase.construction.ShowcasePreflight.check(plan,
                helper.getLevel(), op, helper.absolutePos(ORIGIN));
        check(why.isEmpty(), "showcase preflight failed: " + why.map(c -> c.getString()).orElse(""));
        // A construction failure may leave blocks. Rechecking preflight then hides the original exception.
        check(run(helper, as(op, 2), "herbalwitch showcase") > 0, "showcase construction failed after successful preflight; see command error");
        check(DiscoveryCapability.of(op).map(c -> c.state().size()).orElse(0) == 0, "building the showcase granted discoveries");
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
        // one per herb, powders, products, grimoire and altar supplies; Milestone 7: three preparation barrels, circle supplies, charms
        check(barrels == HerbCatalog.ALL.size() + 4 + 5 + 1, "sample barrels " + barrels);
        var animalArea = plan.areas().stream().filter(a -> a.id().equals("animals")).findFirst().orElseThrow();
        var supplies = (Container) helper.getLevel().getBlockEntity(helper.absolutePos(ORIGIN.offset(animalArea.x() + 24, 1, animalArea.z() + 1)));
        check(supplies != null && supplies.countItem(io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations.item(
                io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog.SLEEP)) == 16, "animal-care sleep supplies missing");
        var animalBox = new net.minecraft.world.phys.AABB(helper.absolutePos(ORIGIN), helper.absolutePos(ORIGIN.offset(plan.width(), ShowcasePlan.HEIGHT, plan.depth())));
        var samples = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, animalBox);
        check(samples.size() == 16, "animal sample count " + samples.size());
        check(samples.stream().map(m -> net.minecraft.world.entity.EntityType.getKey(m.getType()).toString()).collect(java.util.stream.Collectors.toSet())
                .equals(java.util.Set.of("minecraft:cow", "minecraft:sheep", "minecraft:goat", "minecraft:pig", "minecraft:rabbit", "minecraft:chicken",
                        "minecraft:horse", "minecraft:donkey", "minecraft:mule", "minecraft:llama", "minecraft:cat", "minecraft:wolf", "minecraft:fox", "minecraft:parrot", "minecraft:axolotl", "minecraft:bat")), "animal species matrix differs");
        for (var sample : samples) {
            check(!sample.isNoAi() && sample.getActiveEffects().isEmpty(), "sample received disabled AI or effects");
            check(io.github.dirtyredz.herbalwitch.companions.animals.trust.AnimalCapability.of(sample).orElseThrow().state().isEmpty(), "sample received care state");
        }
        for (Block piece : GrimoireBlocks.all()) {
            check(blocks.getOrDefault(piece, 0) == 1, piece + " shown " + blocks.getOrDefault(piece, 0) + " times");
        }
        Block altar = io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks.ALTAR.get();
        // the altar section's altar and the one inside the ritual circle
        check(blocks.getOrDefault(altar, 0) == 8, "the altar shown in " + blocks.getOrDefault(altar, 0) + " cells, expected 8");
        Block circle = io.github.dirtyredz.herbalwitch.magic.ritual.registry.RitualBlocks.CIRCLE.get();
        check(blocks.getOrDefault(circle, 0) == io.github.dirtyredz.herbalwitch.magic.ritual.catalog.CircleCatalog.LAYOUT.size(),
                "the circle shown in " + blocks.getOrDefault(circle, 0) + " cells");
        Block ward = io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration.STONE.get();
        check(blocks.getOrDefault(ward, 0) >= 1, "no ward stone");
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
