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
import net.minecraft.world.entity.player.Player;
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

    @GameTest(template = FULL, timeoutTicks = 400, batch = "herbalwitch_showcase_animal_spawn_failure")
    public static void failedAnimalSpawnRemovesOnlyThisInvocationsSamplesAndKeepsBlocks(GameTestHelper helper) throws Exception {
        ServerPlayer op = prepare(helper, "m8_showcase_failure");
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
        runChecked(helper, op, "herbalwitch showcase furniture");
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
        runChecked(helper, op, "herbalwitch showcase witchcraft");
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
        check(scan.barrels == HerbCatalog.ALL.size() + 4 + 5 + 1, "sample barrels " + scan.barrels);
        var animalArea = plan.areas().stream().filter(a -> a.id().equals("animals")).findFirst().orElseThrow();
        var supplies = (Container) helper.getLevel().getBlockEntity(helper.absolutePos(ORIGIN.offset(animalArea.x() + 24, 1, animalArea.z() + ShowcasePlan.HEADING_ROWS)));
        check(supplies != null && supplies.countItem(io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.AdvancedPreparations.item(
                io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.AdvancedProductCatalog.SLEEP)) == 16, "animal-care sleep supplies missing");
        var animalBox = new AABB(helper.absolutePos(ORIGIN), helper.absolutePos(ORIGIN.offset(plan.width(), ShowcasePlan.HEIGHT, plan.depth())));
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
        var homeArea = plan.areas().stream().filter(a -> a.id().equals("familiar_homes")).findFirst().orElseThrow();
        for (var entry : io.github.dirtyredz.herbalwitch.companions.familiars.registry.FamiliarHomes.ALL) {
            check(scan.blocks.getOrDefault(entry.block().get(), 0) == 1, entry.kind() + " home missing or duplicated");
            var relative = ORIGIN.offset(homeArea.x() + 1 + entry.kind().ordinal() * 3, 1, homeArea.z() + ShowcasePlan.HEADING_ROWS + 1);
            var home = (io.github.dirtyredz.herbalwitch.companions.familiars.home.FamiliarHomeBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(relative));
            check(home != null && home.writable() && home.occupant() == null && home.owner() == null,
                    "showcase home received a bond or cannot be claimed");
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
        runChecked(helper, op, "herbalwitch showcase");
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
        // the second GameTest batch reuses the first batch's plots, and a plot this large collects dropped items, falling blocks
        // and wandering animals: entities the preflight refuses
        helper.getLevel().getEntitiesOfClass(Entity.class, new AABB(
                helper.absolutePos(BlockPos.ZERO), helper.absolutePos(new BlockPos(FULL_X, 10, FULL_Z))).inflate(1),
                entity -> !(entity instanceof Player)).forEach(Entity::discard);
        return op;
    }

    private static void runChecked(GameTestHelper helper, ServerPlayer op, String command) throws Exception {
        // Rechecking the preflight after a failed build would report the build's own blocks, so report what the command said.
        java.util.List<String> said = new java.util.ArrayList<>();
        net.minecraft.commands.CommandSource capture = new net.minecraft.commands.CommandSource() {
            @Override public void sendSystemMessage(net.minecraft.network.chat.Component message) { said.add(message.getString()); }
            @Override public boolean acceptsSuccess() { return false; }
            @Override public boolean acceptsFailure() { return true; }
            @Override public boolean shouldInformAdmins() { return false; }
        };
        if (helper.getLevel().getServer().getCommands().performPrefixedCommand(as(op, 2).withSource(capture), command) == 0) {
            check(false, command + " failed: " + said);
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
