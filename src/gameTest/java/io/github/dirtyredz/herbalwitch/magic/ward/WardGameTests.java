package io.github.dirtyredz.herbalwitch.magic.ward;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlock;
import io.github.dirtyredz.herbalwitch.magic.ward.block.WardStoneBlockEntity;
import io.github.dirtyredz.herbalwitch.magic.ward.catalog.WardCatalog;
import io.github.dirtyredz.herbalwitch.magic.ward.persistence.WardRecord;
import io.github.dirtyredz.herbalwitch.magic.ward.registry.WardRegistration;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardIndex;
import io.github.dirtyredz.herbalwitch.magic.ward.service.WardService;
import io.github.dirtyredz.herbalwitch.magic.ward.targeting.WardProtection;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.energy;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.practice;
import static io.github.dirtyredz.herbalwitch.fixture.MagicFixtures.setEnergy;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Ward stones end to end. Each test that charges a ward runs in its own batch: a charged ward's box is
 * wider than the gap between neighbouring GameTest plots, so two at once would refuse each other as
 * overlapping. Every test leaves no ward charged.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class WardGameTests {
    private static final BlockPos STONE = new BlockPos(4, 1, 4);
    private static final BlockPos NEAR = new BlockPos(4, 1, 2);

    private WardGameTests() {
    }

    private static BlockPos stone(GameTestHelper helper, BlockPos at) {
        helper.setBlock(at, WardRegistration.STONE.get().defaultBlockState());
        return helper.absolutePos(at);
    }

    private static WardRecord record(GameTestHelper helper, BlockPos abs) {
        return WardService.record(helper.getLevel(), abs).orElseThrow(() -> new net.minecraft.gametest.framework.GameTestAssertException(
                "the stone has no record"));
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_activation")
    public static void aWitchChargesAStoneOnceAndOnlyTheOwnerControlsIt(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = stone(helper, STONE);
        ServerPlayer novice = player(helper, "m7_ward_novice", NEAR);
        check(WardService.activate(novice, level, abs).equals("not_witch"), "a non-witch charged a ward");
        ServerPlayer owner = witch(helper, "m7_ward_owner", NEAR);
        int cost = EnergyService.units(WitchRules.server().workings().wardActivationCost());
        setEnergy(owner, cost - 1);
        check(WardService.activate(owner, level, abs).equals("not_enough_energy"), "charged without the energy");
        check(WardService.record(level, abs).isEmpty(), "a refused activation bound the stone");
        setEnergy(owner, cost * 2);
        int practice = practice(owner);
        check(WardService.activate(owner, level, abs).equals("activated"), "the owner could not charge it");
        check(energy(owner) == cost && practice(owner) == practice + cost, "paid " + (cost * 2 - energy(owner)) + " with practice "
                + (practice(owner) - practice));
        WardRecord record = record(helper, abs);
        check(record.ownedBy(owner.getUUID()) && record.charged() && WardService.indexed(record.id()), "not bound, charged and active");
        check(helper.getBlockState(STONE).getValue(WardStoneBlock.ACTIVE), "a charged stone does not show it");
        check(WardService.activate(owner, level, abs).equals("already_charged"), "a charged ward was topped up");
        check(energy(owner) == cost, "a refused top-up spent energy");
        ServerPlayer rival = witch(helper, "m7_ward_rival", NEAR.east());
        check(WardService.deactivate(rival, level, abs).equals("not_owner"), "another witch ended the ward");
        check(WardService.radius(rival, level, abs, -1).equals("not_owner"), "another witch resized it");
        check(WardService.release(rival, level, abs).equals("not_owner"), "another witch released it");
        int radius = record.radius();
        check(WardService.radius(owner, level, abs, -1).equals("radius") && record(helper, abs).radius() == radius - 1,
                "the owner could not shrink it");
        check(WardService.radius(owner, level, abs, 99).equals("radius")
                && record(helper, abs).radius() == WardService.maxRadius(level, abs), "growing is capped by the focus rule");
        int charge = record(helper, abs).charge();
        helper.runAfterDelay(10, () -> {
            check(record(helper, abs).charge() < charge, "the charge does not run down");
            check(WardService.deactivate(owner, level, abs).equals("deactivated"), "the owner could not end it");
            check(!WardService.indexed(record.id()) && !helper.getBlockState(STONE).getValue(WardStoneBlock.ACTIVE), "still active");
            check(WardService.release(owner, level, abs).equals("released") && record(helper, abs).owner().isEmpty(), "not released");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_targeting")
    public static void aChargedWardKeepsItsFamiliesFromChoosingPlayersInside(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = stone(helper, STONE);
        ServerPlayer owner = witch(helper, "m7_ward_keeper", NEAR);
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(1, 1, 7));
        spider.setNoAi(true);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(7, 1, 7));
        zombie.setNoAi(true);
        spider.setTarget(owner);
        check(spider.getTarget() == owner, "the spider could not choose a player beforehand");
        check(WardService.activate(owner, level, abs).equals("activated"), "not charged");
        WardRecord record = record(helper, abs);
        check(record.profile() == WardCatalog.Profile.BOTH, "a new ward does not guard against both families");
        zombie.setTarget(owner);
        check(zombie.getTarget() == null, "a zombie chose a player inside the ward");
        WardIndex.Entry entry = WardService.entry(record.id());
        for (int i = 0; i < 2 * entry.radius() / WardProtection.SLICE + 2; i++) {
            WardProtection.sweep(level, entry);
        }
        check(spider.getTarget() == null, "the sweep left the spider's earlier target");
        check(WardService.profile(owner, level, abs).equals("profile") && record(helper, abs).profile() == WardCatalog.Profile.UNDEAD,
                "the profile did not cycle to the undead");
        spider.setTarget(owner);
        check(spider.getTarget() == owner, "an undead-only ward kept a spider away");
        check(WardService.deactivate(owner, level, abs).equals("deactivated"), "cleanup");
        zombie.setTarget(owner);
        check(zombie.getTarget() == owner, "an ended ward still protects");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_bounds")
    public static void aSweepInspectsABoundedNumberOfMobs(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = stone(helper, STONE);
        ServerPlayer owner = witch(helper, "m7_ward_crowd", NEAR);
        for (int i = 0; i < WardCatalog.CHECKS_PER_SWEEP + 8; i++) {
            helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 1, 6)).setNoAi(true);
        }
        check(WardService.activate(owner, level, abs).equals("activated"), "not charged");
        WardIndex.Entry entry = WardService.entry(record(helper, abs).id());
        int most = 0;
        for (int i = 0; i < 2 * entry.radius() / WardProtection.SLICE + 2; i++) {
            most = Math.max(most, WardProtection.sweep(level, entry));
        }
        check(most == WardCatalog.CHECKS_PER_SWEEP, "a sweep inspected " + most + " mobs");
        check(WardService.deactivate(owner, level, abs).equals("deactivated"), "cleanup");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "herbalwitch_ward_lifecycle")
    public static void breakingRetiresTheWardAndACopiedIdIsInert(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = stone(helper, STONE);
        ServerPlayer owner = witch(helper, "m7_ward_mason", NEAR);
        check(WardService.activate(owner, level, abs).equals("activated"), "not charged");
        long id = record(helper, abs).id();
        BlockPos copy = stone(helper, new BlockPos(1, 1, 1));
        ((WardStoneBlockEntity) level.getBlockEntity(copy)).setRecordId(id);
        check(WardService.record(level, copy).isEmpty(), "a copied record id took over another stone's ward");
        check(WardService.indexed(id) && record(helper, abs).charged(), "the copy disturbed the real ward");
        level.destroyBlock(abs, true);
        check(!WardService.indexed(id), "a broken stone still protects");
        helper.setBlock(STONE, WardRegistration.STONE.get().defaultBlockState());
        check(WardService.record(level, abs).isEmpty(), "a new stone inherited the broken one's ward");
        helper.succeed();
    }
}
