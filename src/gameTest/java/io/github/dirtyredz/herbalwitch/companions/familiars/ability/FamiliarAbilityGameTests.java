package io.github.dirtyredz.herbalwitch.companions.familiars.ability;

import io.github.dirtyredz.herbalwitch.companions.familiars.ability.action.FamiliarCombatPowers;
import io.github.dirtyredz.herbalwitch.companions.familiars.bond.data.FamiliarStore;
import io.github.dirtyredz.herbalwitch.companions.familiars.fixture.FamiliarFixtures;
import io.github.dirtyredz.herbalwitch.magic.witch.status.WitchStatusService;
import io.github.dirtyredz.herbalwitch.platform.player.OnlinePlayers;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.gametest.*;
import java.util.*;
import static io.github.dirtyredz.herbalwitch.fixture.WorldFixtures.*;

@GameTestHolder("herbalwitch") @PrefixGameTestTemplate(false)
public final class FamiliarAbilityGameTests {
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_power_payment", timeoutTicks = 100)
    public static void noResultCostsNothingAndSuccessfulPowerPaysOnceWithSpiritbond(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "power_payment_" + UUID.randomUUID(), b -> {
            try (var online = OnlinePlayers.register(List.of(b.owner()))) {
                var before = b.record(); int energy = WitchStatusService.energyUnits(b.owner());
                check(!FamiliarAbilityService.perform(b.mob(), FamiliarPower.THREATS, () -> false), "empty power succeeded");
                check(b.record().equals(before) && WitchStatusService.energyUnits(b.owner()) == energy, "empty power mutated bond or energy");
                var store = FamiliarStore.get(b.owner().server); store.update(b.record().change(s -> {
                    var buffs = s.getCompound("buffs"); buffs.putInt("spiritbond", 3600); s.put("buffs", buffs);
                }), false);
                int[] results = {0};
                check(FamiliarAbilityService.perform(b.mob(), FamiliarPower.THREATS, () -> { results[0]++; return true; }), "paid power refused");
                check(WitchStatusService.energyUnits(b.owner()) == energy - 240, "spiritbond cost not fixed point");
                check(!FamiliarAbilityService.perform(b.mob(), FamiliarPower.THREATS, () -> { results[0]++; return true; }) && results[0] == 1, "cooldown repeated effect");
                check(!b.record().state().getBoolean("autoPowers"), "automatic powers enabled without owner request");
                store.update(b.record().change(s -> { s.putString("command", "passive"); s.remove("powerCooldowns"); }), false);
                check(!FamiliarAbilityService.perform(b.mob(), FamiliarPower.THREATS, () -> { results[0]++; return true; }) && results[0] == 1,
                        "Passive performed queued work");
                h.succeed();
            }
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_power_targets", timeoutTicks = 100)
    public static void distractionRejectsPassiveTargetsAndPaysForOneHostile(GameTestHelper h) {
        FamiliarFixtures.bound(h, "fox", "power_targets_" + UUID.randomUUID(), b -> {
            try (var online = OnlinePlayers.register(List.of(b.owner()))) {
                var pig = h.spawn(EntityType.PIG, new BlockPos(7,1,5));
                var enemy = h.spawn(EntityType.PILLAGER, new BlockPos(8,1,5)); enemy.setNoAi(true);
                int energy = WitchStatusService.energyUnits(b.owner());
                check(!FamiliarCombatPowers.distract(b.mob(), pig) && pig.getTarget() == null, "passive distraction permitted");
                check(WitchStatusService.energyUnits(b.owner()) == energy, "denied distraction spent energy");
                check(FamiliarCombatPowers.distract(b.mob(), enemy) && enemy.getTarget() == b.mob(), "hostile distraction refused");
                check(WitchStatusService.energyUnits(b.owner()) == energy - 400, "distraction wrong cost");
                check(!FamiliarCombatPowers.distract(b.mob(), enemy), "distraction repeated");
                h.succeed();
            }
        });
    }
    @GameTest(template = TEMPLATE, batch = "herbalwitch_familiar_reserve_charge", timeoutTicks = 100)
    public static void reserveChargeDebitsFiveAndNeverOverfills(GameTestHelper h) {
        FamiliarFixtures.bound(h, "cat", "reserve_charge_" + UUID.randomUUID(), b -> {
            b.owner().getInventory().setItem(0, new net.minecraft.world.item.ItemStack(io.github.dirtyredz.herbalwitch.grimoire.registry.GrimoireItems.BOOK.get()));
            b.owner().moveTo(b.mob().position().add(1, 0, 0));
            h.runAfterDelay(6, () -> {
                check(FamiliarReserve.charge(b.owner(), b.bond()).equals("reserve_charged"), "reserve charge refused");
                check(b.record().state().getInt("reserve") == 500 && WitchStatusService.energyUnits(b.owner()) == 7000, "reserve conservation failed");
                var store = FamiliarStore.get(b.owner().server); store.update(b.record().change(s -> s.putInt("reserve", 1800)), false);
                var before = b.record();
                check(FamiliarReserve.charge(b.owner(), b.bond()).equals("reserve_full"), "partial reserve charge permitted");
                check(b.record().equals(before) && WitchStatusService.energyUnits(b.owner()) == 7000, "full reserve mutated"); h.succeed();
            });
        });
    }
}
