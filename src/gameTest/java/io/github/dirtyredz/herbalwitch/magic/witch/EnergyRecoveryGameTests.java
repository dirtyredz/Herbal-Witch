package io.github.dirtyredz.herbalwitch.magic.witch;

import io.github.dirtyredz.herbalwitch.magic.witch.status.StatusFixtures;

import io.github.dirtyredz.herbalwitch.HerbalWitch;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.application.DrinkApplication;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.catalog.EverydayProductCatalog;
import io.github.dirtyredz.herbalwitch.apothecary.brewing.product.registry.PreparationItems;
import io.github.dirtyredz.herbalwitch.magic.witch.config.WitchRules;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyBalance;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyEvents;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.EnergyRecovery;
import io.github.dirtyredz.herbalwitch.magic.witch.energy.RecoveryConditions;
import io.github.dirtyredz.herbalwitch.magic.witch.status.SleepRecovery;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchCapability;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.player;
import static io.github.dirtyredz.herbalwitch.fixture.WitchFixtures.witch;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.TEMPLATE;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.check;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.floor;

/**
 * Recovery on a real server: energy tea for witches and everyone else (food, bottle, the saved
 * cooldown that refuses a second cup without consuming it), the restful infusion's sleep preparation,
 * the once-a-day sleep reward, and passive rest that needs stillness and adds the nature/moon bonus
 * measured at the player's own position.
 */
@GameTestHolder(HerbalWitch.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EnergyRecoveryGameTests {
    private static final BlockPos FEET = new BlockPos(4, 1, 4);

    private EnergyRecoveryGameTests() {
    }

    private static ItemStack drink(GameTestHelper helper, ServerPlayer player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        ItemStack after = stack.getItem().finishUsingItem(stack, helper.getLevel(), player);
        player.setItemInHand(InteractionHand.MAIN_HAND, after);
        return after;
    }

    private static ItemStack tea(int count) {
        return new ItemStack(PreparationItems.product(EverydayProductCatalog.ENERGY_TEA), count);
    }

    @GameTest(template = TEMPLATE)
    public static void energyTeaRestoresAWitchOnceThenWaitsForItsCooldown(GameTestHelper helper) {
        floor(helper);
        ServerPlayer w = witch(helper, "er_tea", FEET);
        StatusFixtures.debit(w, 6_000);
        w.getFoodData().setFoodLevel(10);
        ItemStack after = drink(helper, w, tea(2));
        check(WitchStatusService.energyUnits(w) == 4_000 + 2_500, "tea restores 25: " + WitchStatusService.energyUnits(w));
        check(w.getFoodData().getFoodLevel() == 10 + DrinkApplication.ENERGY_NUTRITION, "still food");
        check(after.getCount() == 1 && w.getInventory().countItem(Items.GLASS_BOTTLE) == 1, "one cup drunk, one bottle back");
        check(w.getCooldowns().isOnCooldown(after.getItem()), "the vanilla cooldown mirrors the saved one");
        int saved = WitchCapability.of(w).orElseThrow().state().teaCooldownTicks();
        check(saved == WitchRules.server().energy().teaCooldown(), "saved cooldown " + saved);
        ItemStack refused = drink(helper, w, after);
        check(refused.getCount() == 1 && WitchStatusService.energyUnits(w) == 6_500, "a cup during the cooldown is refused and kept");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void aNonWitchDrinksTeaAsFoodOnly(GameTestHelper helper) {
        floor(helper);
        ServerPlayer p = player(helper, "er_plain", FEET);
        p.getFoodData().setFoodLevel(10);
        drink(helper, p, tea(1));
        check(p.getFoodData().getFoodLevel() == 10 + DrinkApplication.ENERGY_NUTRITION, "food for everyone");
        check(!WitchStatusService.isWitch(p) && WitchStatusService.energyUnits(p) == 0, "no energy for a non-witch");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void restfulInfusionPreparesTheNextRewardedSleepOnce(GameTestHelper helper) {
        floor(helper);
        ServerPlayer w = witch(helper, "er_restful", FEET);
        StatusFixtures.debit(w, 9_000);
        w.getStats().setValue(w, Stats.CUSTOM.get(Stats.TIME_SINCE_REST), 50_000);
        drink(helper, w, new ItemStack(PreparationItems.product(EverydayProductCatalog.RESTFUL_INFUSION)));
        check(w.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) == 0, "the rest statistic still resets");
        check(WitchStatusService.energyUnits(w) == 1_000, "the infusion itself grants nothing now");
        check(WitchCapability.of(w).orElseThrow().state().restfulTicks() > 0, "the next sleep is prepared");
        long day = 10 + helper.getLevel().getGameTime() / SleepRecovery.TICKS_PER_DAY;
        SleepRecovery.Outcome first = EnergyEvents.sleptThrough(w, day * SleepRecovery.TICKS_PER_DAY, true);
        check(first.rewarded() && first.preparationUsed() && WitchStatusService.energyUnits(w) == 1_000 + 3_000 + 3_000,
                "a prepared night: " + first + " energy " + WitchStatusService.energyUnits(w));
        SleepRecovery.Outcome again = EnergyEvents.sleptThrough(w, day * SleepRecovery.TICKS_PER_DAY + 100, true);
        check(!again.rewarded(), "a second sleep the same day");
        check(!EnergyEvents.sleptThrough(w, (day - 3) * SleepRecovery.TICKS_PER_DAY, true).rewarded(), "time turned back");
        check(!EnergyEvents.sleptThrough(w, (day + 1) * SleepRecovery.TICKS_PER_DAY, false).rewarded(), "a short sleep");
        check(EnergyEvents.sleptThrough(w, (day + 1) * SleepRecovery.TICKS_PER_DAY, true).grantedUnits() == 3_000, "the next day");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void stillnessRestoresEnergyAndMovementResetsIt(GameTestHelper helper) {
        floor(helper);
        ServerPlayer w = witch(helper, "er_rest", FEET);
        StatusFixtures.debit(w, 5_000);
        EnergyBalance b = WitchRules.server().energy();
        int pct = RecoveryConditions.bonusPercent(w, b);
        for (int i = 0; i < b.restInterval() + 1; i++) {
            EnergyEvents.tick(w);
        }
        int expected = 5_000 + EnergyRecovery.withBonus(b.restAmount() * EnergyBalance.UNITS, pct);
        check(WitchStatusService.energyUnits(w) == expected, "one rest interval: " + WitchStatusService.energyUnits(w) + " expected " + expected);
        for (int i = 0; i < b.restInterval() - 10; i++) {
            EnergyEvents.tick(w);
        }
        w.moveTo(w.getX() + 1, w.getY(), w.getZ());
        for (int i = 0; i < 20; i++) {
            EnergyEvents.tick(w);
        }
        check(WitchStatusService.energyUnits(w) == expected, "moving restarted the interval");
        helper.succeed();
    }
}
